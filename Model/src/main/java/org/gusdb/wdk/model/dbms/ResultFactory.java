package org.gusdb.wdk.model.dbms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.plugin.DelayedResultException;

public class ResultFactory {

  private static Logger logger = Logger.getLogger(ResultFactory.class);

  private static final boolean USE_INSTANCE_INFO_CACHE = true;
  private static final boolean COMPUTE_CACHE_TABLE_STATISTICS = false;

  private static final InMemoryCache<String, Optional<InstanceInfo>> INSTANCE_INFO_CACHE = new InMemoryCache<>();

  public interface CacheTableCreator {

    /**
     * Creates a new cache table in the application database and populates rows with a result
     * 
     * @param appDb application database in which cache table will be created
     * @param tableName name of table
     * @param instanceId id of query cache table
     * @return result message, if any
     * @throws DelayedResultException 
     */
    Optional<String> createCacheTableAndInsertResult(DatabaseInstance appDb, String tableName, long instanceId) throws WdkModelException, DelayedResultException;

    /**
     * @return list of columns on which an index should be added after table creation
     */
    String[] getCacheTableIndexColumns();

    /**
     * @return name of the query whose result is being cached
     */
    String getQueryName();
  }

  private final DatabaseInstance _appDb;

  public ResultFactory(DatabaseInstance appDb) {
    _appDb = appDb;
  }

  public InstanceInfo cacheResults(String checksum, CacheTableCreator tableCreator) throws WdkModelException, DelayedResultException {

    Optional<InstanceInfo> instanceInfo = getInstanceInfo(checksum);
    if (instanceInfo.isPresent()) {
      // results with this checksum already cached
      return instanceInfo.get();
    }

    // instance doesn't exist; create cache
    InstanceInfo newInstanceRow = createCache(checksum, tableCreator);
    insertInstanceRow(newInstanceRow);
    return newInstanceRow;
  }

  public Optional<InstanceInfo> getInstanceInfo(String checksum)
      throws WdkModelException {
    try {
      // will update the cache if the info has unknown_instance_id
      return USE_INSTANCE_INFO_CACHE ?
          INSTANCE_INFO_CACHE.getValue(InstanceInfoFetcher.getKey(checksum), new InstanceInfoFetcher(_appDb)) :
          InstanceInfoFetcher.getInstanceInfo(_appDb, checksum);
    }
    catch (ValueProductionException e) {
      return WdkModelException.unwrap(e);
    }
  }

  private InstanceInfo createCache(String checksum, CacheTableCreator tableCreator)
      throws WdkModelException, DelayedResultException {
    try {
      DataSource dataSource = _appDb.getDataSource();
      long instanceId = _appDb.getPlatform().getNextId(dataSource, null, CacheFactory.TABLE_INSTANCE);
      String cacheTable = getCacheTableName(instanceId);
      Optional<String> resultMessage = tableCreator.createCacheTableAndInsertResult(_appDb, cacheTable, instanceId);
      InstanceInfo instanceInfo = new InstanceInfo(instanceId, cacheTable, tableCreator.getQueryName(), checksum, resultMessage);
      createCacheTableIndex(cacheTable, tableCreator.getCacheTableIndexColumns());
      computeTableStatistics(dataSource, cacheTable);
      return instanceInfo;
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not create cache", e);
    }
  }

  private void computeTableStatistics(DataSource dataSource, String cacheTable) throws SQLException {
    if (COMPUTE_CACHE_TABLE_STATISTICS) {
      long start = System.currentTimeMillis();
      _appDb.getPlatform().computeThenLockStatistics(dataSource, _appDb.getDefaultSchema(), cacheTable);
      QueryLogger.logEndStatementExecution("whatever the platform uses for computing stats", cacheTable + "__gather_table_stats", start);
    }
  }

  private void createCacheTableIndex(String cacheTable, String[] indexColumns) throws WdkModelException {

    if (indexColumns != null && indexColumns.length > 0) {

      // build index SQL
      StringBuilder sqlId = new StringBuilder("CREATE INDEX ")
          .append(cacheTable)
          .append("_idx01 ON ")
          .append(cacheTable)
          .append(" (")
          .append(indexColumns[0]);
      for (int i=1; i <indexColumns.length; i++) {
        sqlId.append(", ").append(indexColumns[i]);
      }
      sqlId.append(")");

      try {
        DataSource dataSource = _appDb.getDataSource();
        SqlUtils.executeUpdate(dataSource, sqlId.toString(),
            cacheTable + "__create-cache-index01");
      }
      catch (SQLException e) {
        throw new WdkModelException("Could not create cache table index.", e);
      }
    }
  }

  static String getCacheTableName(long instanceId) {
    return CacheFactory.CACHE_TABLE_PREFIX + instanceId;
  }

  private void insertInstanceRow(InstanceInfo instanceInfo) throws WdkModelException {

    String sql = new StringBuilder("INSERT INTO ")
        .append(CacheFactory.TABLE_INSTANCE).append(" (")
        .append(CacheFactory.COLUMN_INSTANCE_ID).append(", ")
        .append(CacheFactory.COLUMN_TABLE_NAME).append(", ")
        .append(CacheFactory.COLUMN_QUERY_NAME).append(", ")
        .append(CacheFactory.COLUMN_INSTANCE_CHECKSUM).append(", ")
        .append(CacheFactory.COLUMN_RESULT_MESSAGE)
        .append(") VALUES (?, ?, ?, ?, ?)")
        .toString();

    Integer[] types = {
        Types.BIGINT,  // instance id
        Types.VARCHAR, // table name
        Types.VARCHAR, // query name
        Types.VARCHAR, // checksum
        Types.CLOB     // result message
    };

    try {
      new SQLRunner(_appDb.getDataSource(), sql, "insert-cache-index-row")
        .executeStatement(new Object[] {
          instanceInfo.getInstanceId(),
          instanceInfo.getTableName(),
          instanceInfo.getQueryName(),
          instanceInfo.getChecksum(),
          instanceInfo.getResultMessage().orElse(null)
        }, types);
    }
    catch (Exception e) {
      WdkModelException.unwrap(e);
    }
  }

  public String getCachedSql(long instanceId) {
    return new StringBuilder("SELECT * FROM ").append(getCacheTableName(instanceId)).toString();
  }

  public String getCachedSortedSql(long instanceId, Map<String,Boolean> sortingMap) {

    StringBuilder sql = new StringBuilder(getCachedSql(instanceId));

    // append sorting columns to the sql
    //Map<String, Boolean> sortingMap = query.getSortingMap();
    boolean firstSortingColumn = true;
    for (String column : sortingMap.keySet()) {
      if (firstSortingColumn) {
        sql.append(" ORDER BY ");
        firstSortingColumn = false;
      }
      else {
        sql.append(", ");
      }
      String order = sortingMap.get(column) ? " ASC " : " DESC ";
      sql.append(column).append(order);
    }

    // append row id as the last sorting column
    sql.append((sortingMap.size() > 0) ? ", " : " ORDER BY ");
    sql.append(CacheFactory.COLUMN_ROW_ID + " ASC ");
    return sql.toString();
  }

  public ResultList getCachedResults(long instanceId) throws WdkModelException {
    return getResult(instanceId, getCachedSql(instanceId));
  }

  public ResultList getCachedSortedResults(long instanceId, Map<String,Boolean> sortingMap)
      throws WdkModelException {
    return getResult(instanceId, getCachedSortedSql(instanceId, sortingMap));
  }

  private ResultList getResult(long instanceId, String sql) throws WdkModelException {
    try {
      logger.debug("Performing the following SQL against WDK Cache: " + sql);
      DataSource dataSource = _appDb.getDataSource();
      ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql, "select-cache-id-" + instanceId);
      return new SqlResultList(resultSet);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to retrieve results from WDK Cache.", e);
    }
  }
}
