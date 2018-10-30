package org.gusdb.wdk.model.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;

public class ResultFactory {

  private static Logger logger = Logger.getLogger(ResultFactory.class);

  public static class InstanceInfo {
    long instanceId;
    String message;
    final long creationDate;
    public InstanceInfo(int instanceId, String message) {
      this.instanceId = instanceId;
      this.message = message;
      creationDate = new Date().getTime();
    }
    
  }

  public static final int UNKNOWN_INSTANCE_ID = 0;

  private static boolean USE_INSTANCE_INFO_CACHE = true;
  private static InMemoryCache<String, InstanceInfo> INSTANCE_INFO_CACHE = new InMemoryCache<String, InstanceInfo>();
  private final InstanceInfoFetcher instanceInfoFetcher;

  private DatabaseInstance database;
  private DBPlatform platform;
  private CacheFactory cacheFactory;

  public ResultFactory(WdkModel wdkModel) {
    this.database = wdkModel.getAppDb();
    this.platform = database.getPlatform();
    this.cacheFactory = new CacheFactory(wdkModel, database);
    this.instanceInfoFetcher = new InstanceInfoFetcher(this); 
  }

  public CacheFactory getCacheFactory() {
    return cacheFactory;
  }

  public String getCachedSql(QueryInstance<?> queryInstance, boolean performSorting)
      throws WdkModelException {

    InstanceInfo instanceInfo =  getInstanceInfo(queryInstance);
    Query query = queryInstance.getQuery();

    long instanceId = instanceInfo.instanceId;
    try {
      // logger.debug(" ..... EXISTING table, instanceid?: " + instanceId + "(if 0 we need a new one)");
      if (instanceId == UNKNOWN_INSTANCE_ID) { // instance doesn't exist
        // logger.debug("creating cache instance and cache...");
        // create cache
        instanceId = createCache(queryInstance);
        // logger.debug(" ..... EXISTING table, NEW instanceid: " + instanceId);
        createCacheInstance(queryInstance, instanceId);
      }

    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
        //logger.debug("   ..... FINAL instanceid: " + instanceId);
    queryInstance.setInstanceId(instanceId);

    // get the cached sql
    StringBuilder sql = new StringBuilder("SELECT * ");
    sql.append(" FROM ").append(getCacheTableName(instanceId));
    //    sql.append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_ID);
    //sql.append(" = ").append(instanceId);

    if (performSorting) {
      // append sorting columns to the sql
      Map<String, Boolean> sortingMap = query.getSortingMap();
      boolean firstSortingColumn = true;
      for (String column : sortingMap.keySet()) {
        if (firstSortingColumn) {
          sql.append(" ORDER BY ");
          firstSortingColumn = false;
        } else {
          sql.append(", ");
        }
        String order = sortingMap.get(column) ? " ASC " : " DESC ";
        sql.append(column).append(order);
      }

      // append row id as the last sorting column
      sql.append((sortingMap.size() > 0) ? ", " : " ORDER BY ");
      sql.append(CacheFactory.COLUMN_ROW_ID + " ASC ");
    }

    return sql.toString();
  }

  public ResultList getCachedResults(QueryInstance<?> queryInstance, boolean performSorting)
      throws WdkModelException {
    String sql = getCachedSql(queryInstance, performSorting);
    logger.debug("Performing the following SQL against WDK Cache: " + sql);

    // get the resultList
    try {
      DataSource dataSource = database.getDataSource();
      ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql.toString(),
          queryInstance.getQuery().getFullName() + "__select-cache");
      return new SqlResultList(resultSet);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to retrieve results from WDK Cache.", e);
    }
  }

  private InstanceInfo getInstanceInfo(QueryInstance<?> instance)
      throws WdkModelException {
    try {
      // get the query instance id; null if not exist
      String checksum = instance.getChecksum();
      
      // will update the cache if the info has unknown_instance_id
      InstanceInfo instanceInfo = USE_INSTANCE_INFO_CACHE ?
          INSTANCE_INFO_CACHE.getValue(InstanceInfoFetcher.getKey(checksum), instanceInfoFetcher) :
          getInstanceInfo(checksum);
      instance.setResultMessage(instanceInfo.message);
      return instanceInfo;
    }
    catch (ValueProductionException e) {
      throw (WdkModelException)e.getCause();
    }
  }

  public InstanceInfo getInstanceInfo(String checksum) throws WdkModelException {

    StringBuilder sql = new StringBuilder("SELECT ");
    sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(", ");
    sql.append(CacheFactory.COLUMN_RESULT_MESSAGE);
    sql.append(" FROM ").append(CacheFactory.TABLE_INSTANCE);
    sql.append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_CHECKSUM);
    sql.append(" = '").append(checksum).append("'");
    sql.append(" ORDER BY " + CacheFactory.COLUMN_INSTANCE_ID);

    DataSource dataSource = database.getDataSource();
    ResultSet resultSet = null;
    //logger.debug("testing if we should reuse a wdk_instance_id in cache table: " +  sql.toString());
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(),
          "wdk-check-instance-exist");

      InstanceInfo instanceInfo = new InstanceInfo(UNKNOWN_INSTANCE_ID, null);
      if (resultSet.next()) {
        instanceInfo.instanceId = resultSet.getInt(CacheFactory.COLUMN_INSTANCE_ID);
        instanceInfo.message = platform.getClobData(resultSet, CacheFactory.COLUMN_RESULT_MESSAGE);
      } 
      return instanceInfo;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to check instance ID.", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  private long createCache(QueryInstance<?> instance)
      throws WdkModelException, SQLException {
    
    DataSource dataSource = database.getDataSource();
    long instanceId = platform.getNextId(dataSource, null, CacheFactory.TABLE_INSTANCE);
    //String schema = database.getDefaultSchema();
    String cacheTable = getCacheTableName(instanceId);
    instance.createCache(cacheTable, instanceId);
    createCacheTableIndex(cacheTable, instance.getQuery());
    /*
    long start = System.currentTimeMillis();
    platform.computeThenLockStatistics(dataSource, schema, cacheTable);
    QueryLogger.logEndStatementExecution("whatever the platform uses for computing stats", cacheTable + "__gather_table_stats", start);
    */
    return instanceId;
  }

  private void createCacheTableIndex(String cacheTable, Query query) throws WdkModelException {

    String[] indexColumns = query.getIndexColumns();

    if (indexColumns != null && indexColumns.length > 0) {

      StringBuffer sqlId = new StringBuffer("CREATE INDEX ");
      sqlId.append(cacheTable).append("_idx01 ON ").append(cacheTable).append(" (").append(indexColumns[0]);

      for (int i=1; i <indexColumns.length; i++)  sqlId.append(", ").append(indexColumns[i]);

      sqlId.append(")");

      try {
        DataSource dataSource = database.getDataSource();
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


  private void createCacheInstance(QueryInstance<?> instance,
      long instanceId) throws WdkModelException {
    StringBuffer sql = new StringBuffer("INSERT INTO ");
    sql.append(CacheFactory.TABLE_INSTANCE).append(" (");
    sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(", ");
    sql.append(CacheFactory.COLUMN_QUERY_NAME).append(", ");
    sql.append(CacheFactory.COLUMN_TABLE_NAME).append(", ");
    sql.append(CacheFactory.COLUMN_INSTANCE_CHECKSUM).append(", ");
    sql.append(CacheFactory.COLUMN_RESULT_MESSAGE);
    sql.append(") VALUES (?, ?, ?, ?, ?)");

    PreparedStatement ps = null;
    try {
      DataSource dataSource = database.getDataSource();
      ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
      ps.setLong(1, instanceId);
      ps.setString(2, instance.getQuery().getFullName());
      ps.setString(3, getCacheTableName(instanceId));
      ps.setString(4, instance.getChecksum());
      platform.setClobData(ps, 5, instance.getResultMessage(), false);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new WdkModelException("Unable to add cache instance.", e);
    } finally {
      // close the statement manually, since we cannot close the
      // connection; it's not committed yet.
      SqlUtils.closeStatement(ps);
    }
  }

}
