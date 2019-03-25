package org.gusdb.wdk.model.dbms;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;

/**
 * This class is responsible for maintenance of the structure of a "single" WDK
 * cache, meaning the set of cached query and analysis results belonging to a
 * single user in a single application database.  Thus it can be used to create,
 * reset, and drop an entire cache, but is NOT responsible for the creation of
 * cache instance tables or insertion of cached results into those tables.  For
 * that responsibility, see {@link org.gusdb.wdk.model.dbms.ResultFactory}.
 */
public class CacheFactory {

  private static Logger LOG = Logger.getLogger(CacheFactory.class);

  // table which maintains refs to result tables (only one of these tables exists)
  static final String TABLE_INSTANCE = "QueryInstance";

  // columns of the QueryInstance table
  public static final String COLUMN_INSTANCE_ID = "wdk_instance_id";
  static final String COLUMN_QUERY_NAME = "query_name";
  static final String COLUMN_TABLE_NAME = "table_name";
  static final String COLUMN_INSTANCE_CHECKSUM = "instance_checksum";
  static final String COLUMN_PARAMS = "params";
  static final String COLUMN_RESULT_MESSAGE = "result_message";

  // prefix for result tables (full name is this, followed by an integer (instance_id)
  static final String CACHE_TABLE_PREFIX = "QueryResult";

  // columns of the QueryResultX tables
  //   includes wdk_instance_id + all columns returned by the Query
  public static final String COLUMN_ROW_ID = "wdk_row_id";

  private final DatabaseInstance _appDb;
  private final StepAnalysisFactory _stepAnalysisFactory;

  public CacheFactory(WdkModel wdkModel) {
    _appDb = wdkModel.getAppDb();
    _stepAnalysisFactory = wdkModel.getStepAnalysisFactory();
  }

  public void createCache() throws WdkModelException {
  
    createQueryInstanceTable();

    // create the id sequence for the query & instance index
    String sequenceName = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      _appDb.getPlatform().createSequence(_appDb.getDataSource(), sequenceName, 1, 1);
    }
    catch (SQLException ex) {
      String message = "Cannot create sequence [" + sequenceName + "]. ";
      LOG.error(message + ex.getMessage());
      throw new WdkModelException(message, ex);
    }

    // create results table for step analysis
    createStepAnalysisCache();
  }

  public void resetCache(boolean purge, boolean forceDrop) {
    // drop cache tables
    dropCacheTables(purge, forceDrop);
    // clear step analysis results
    clearStepAnalysisCache();
  }

  public void recreateCache(boolean purge, boolean forceDrop) throws WdkModelException {
    // drop cache;
    dropCache(purge, forceDrop);
    // create them back
    createCache();
  }

  public void dropCache(boolean purge, boolean forceDrop) {
    // drop cache tables
    dropCacheTables(purge, forceDrop);

    try {
      _appDb.getPlatform().dropTable(_appDb.getDataSource(), null, TABLE_INSTANCE, purge);
    }
    catch (Exception ex) {
      LOG.error("Cannot drop table [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }
    String instanceSeq = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      SqlUtils.executeUpdate(_appDb.getDataSource(), "DROP SEQUENCE " + instanceSeq, "wdk-cache-drop-instance-seq");
    }
    catch (Exception ex) {
      LOG.error("Cannot drop sequence [" + instanceSeq + "]. " + ex.getMessage());
    }
    
    // drop step analysis results cache
    dropStepAnalysisCache(purge);
  }

  public void dropCache(int instanceId, boolean purge) {

    // delete the instance index
    String sql = "DELETE FROM " + TABLE_INSTANCE + " WHERE " + COLUMN_INSTANCE_ID + " = " + instanceId;
    try {
      SqlUtils.executeUpdate(_appDb.getDataSource(), sql, "wdk_cache_delete_instance_index");
    }
    catch (Exception ex) {
      LOG.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. ", ex);
    }

    // drop result table
    String cacheTable = ResultFactory.getCacheTableName(instanceId);
    try {
      _appDb.getPlatform().dropTable(_appDb.getDataSource(), null, cacheTable, purge);
    }
    catch (Exception ex) {
      LOG.error("Cannot crop table " + cacheTable, ex);
    }
  }

  public void dropCache(String queryName, boolean purge) {
    dropCacheTables(purge, " where " + COLUMN_QUERY_NAME + " = '" + queryName + "'");
  }

  // tuple<table-size, map<instance-id, instance-size>>>
  private static class CacheTableStatistics extends ThreeTuple<String, Integer, Map<Long, Long>> {
    public CacheTableStatistics(String queryName, Integer totalSize, Map<Long, Long> instanceSizes) {
      super(queryName, totalSize, instanceSizes);
    }
    public String getQueryName() { return getFirst(); }
    public int getTotalSize() { return getSecond(); }
    public int getInstanceCount() { return getThird().size(); }
    public Map<Long, Long> getInstanceSizes() { return getThird(); }
  }

  // map<table-name, tuple<query-name, table-stats>>
  private static class CacheStatistics extends HashMap<String, CacheTableStatistics> {
    public int getTableCount() { return size(); }
    public int getInstanceCount() {
      return values().stream().mapToInt(stats -> stats.getInstanceCount()).sum();
    }
    public int getRecordCount() {
      return values().stream().mapToInt(stats -> stats.getTotalSize()).sum();
    }
  }

  public void showCache() throws WdkModelException {
    try {
      CacheStatistics cacheStats = collectCacheStatistics();
      StringBuilder output = new StringBuilder()
          .append("========================= Cache Statistics =========================" + NL + NL)
          .append("Number of Cache Tables: " + cacheStats.getTableCount() + NL)
          .append("Number of Instances: " + cacheStats.getInstanceCount() + NL)
          .append("Number of Records: " + cacheStats.getRecordCount() + NL + NL);
        
      for (String tableName : cacheStats.keySet()) {
        CacheTableStatistics tableStats = cacheStats.get(tableName);
        output
          .append("========== Table " + tableName + " Statistics ==========" + NL + NL)
          .append("Query Name: " + tableStats.getQueryName() + NL)
          .append("Number of Instances: " + tableStats.getInstanceCount() + NL)
          .append("Number of Records: " + tableStats.getTotalSize() + NL)
          .append("Instance sizes:" + NL);
        for (Entry<Long, Long> count : tableStats.getInstanceSizes().entrySet()) {
          output
            .append("   wdk_instance_id ")
            .append(count.getKey())
            .append(": ")
            .append(count.getValue())
            .append(" records" + NL);
        }
        output.append(NL + "========== End of " + tableName + " Statistics ==========" + NL + NL);
      }
      output.append("====================== End of Cache Stattistics ======================");
      System.err.println(output.toString());
    }
    catch (Exception e) {
      WdkModelException.unwrap(e);
    }
  }

  private CacheStatistics collectCacheStatistics() throws SQLRunnerException {
    String summarySql = new StringBuilder()
        .append("SELECT DISTINCT ")
        .append(COLUMN_QUERY_NAME).append(", ")
        .append(COLUMN_TABLE_NAME)
        .append(" FROM ").append(TABLE_INSTANCE)
        .append(" ORDER BY ").append(COLUMN_INSTANCE_ID)
        .toString();

    return new SQLRunner(_appDb.getDataSource(),
      summarySql, "wdk-cache-instance-summary").executeQuery(
        resultSet ->  {
          CacheStatistics stats = new CacheStatistics();
          while (resultSet.next()) {
            String queryName = resultSet.getString(COLUMN_QUERY_NAME);
            String cacheTable = resultSet.getString(COLUMN_TABLE_NAME);
            stats.put(cacheTable, collectCacheTableStatistics(cacheTable, queryName));
          }
          return stats;
        }
      );
  }

  private CacheTableStatistics collectCacheTableStatistics(String cacheTable, String queryName) throws SQLRunnerException {
    final String columnInstanceIdSize = "instanceIdSize";
    String tableSql = new StringBuilder()
        .append("SELECT ")
        .append(COLUMN_INSTANCE_ID).append(", ")
        .append("COUNT(").append(COLUMN_INSTANCE_ID).append(") AS ").append(columnInstanceIdSize)
        .append(" FROM ").append(cacheTable)
        .append(" GROUP BY ").append(COLUMN_INSTANCE_ID)
        .append(" ORDER BY ").append(COLUMN_INSTANCE_ID)
        .toString();

    return new SQLRunner(_appDb.getDataSource(),
      tableSql, "wdk-cache-table-summary").executeQuery(
        resultSet -> {
          Map<Long,Long> instanceSizes = new HashMap<>();
          int totalSize = 0;
          while (resultSet.next()) {
            long instanceId = resultSet.getLong(COLUMN_INSTANCE_ID);
            long instanceSize = resultSet.getLong(columnInstanceIdSize);
            instanceSizes.put(instanceId, instanceSize);
            totalSize += instanceSize;
          }
          return new CacheTableStatistics(queryName, totalSize, instanceSizes);
        }
      );
  }

  private void createQueryInstanceTable() throws WdkModelException {
    // create the cache index table
    StringBuilder sql = new StringBuilder("CREATE TABLE ");
    sql.append(TABLE_INSTANCE).append(" ( ");

    // define columns
    DBPlatform platform = _appDb.getPlatform();
    sql.append(COLUMN_INSTANCE_ID).append(" ");
    sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
    sql.append(COLUMN_QUERY_NAME).append(" ");
    sql.append(platform.getStringDataType(200)).append(" NOT NULL, ");
    sql.append(COLUMN_TABLE_NAME).append(" ");
    sql.append(platform.getStringDataType(30)).append(" NOT NULL, ");
    sql.append(COLUMN_INSTANCE_CHECKSUM).append(" ");
    sql.append(platform.getStringDataType(40)).append(" NOT NULL, ");
    sql.append(COLUMN_PARAMS).append(" ");
    sql.append(platform.getClobDataType()).append(", ");
    sql.append(COLUMN_RESULT_MESSAGE).append(" ");
    sql.append(platform.getClobDataType());

    // define primary key
    sql.append(", CONSTRAINT PK_").append(COLUMN_INSTANCE_ID);
    sql.append(" PRIMARY KEY (").append(COLUMN_INSTANCE_ID).append(")) ");

    try {
      SqlUtils.executeUpdate(_appDb.getDataSource(), sql.toString(), "wdk-cache-create-instance");
    }
    catch (SQLException ex) {
      String message = "Cannot create table [" + TABLE_INSTANCE + "]. ";
      LOG.error(message + ex.getMessage());
      throw new WdkModelException(message, ex);
    }
  }

  private void dropCacheTables(boolean purge, boolean forceDrop) {
    dropCacheTables(purge, "");
    if (forceDrop) dropDanglingTables(purge);

  }

  private void dropCacheTables(boolean purge, String whereClause) {

    // get a list of cache tables
    StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
    sql.append(COLUMN_TABLE_NAME).append(" FROM ").append(TABLE_INSTANCE).append(" " + whereClause);
    ResultSet resultSet = null;
    Set<String> cacheTables = new LinkedHashSet<String>();
    try {
      resultSet = SqlUtils.executeQuery(_appDb.getDataSource(), sql.toString(), "wdk-cache-select-cache-table");
      while (resultSet.next()) {
        cacheTables.add(resultSet.getString(COLUMN_TABLE_NAME));
      }
    }
    catch (Exception ex) {
      LOG.error("Cannot query on table [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }

    // delete rows from cache index table
    try {
      SqlUtils.executeUpdate(_appDb.getDataSource(), "DELETE FROM " + TABLE_INSTANCE + " " + whereClause, "wdk-cache-delete-instances");
    }
    catch (Exception ex) {
      LOG.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }

    // drop the cache tables
    for (String cacheTable : cacheTables) {
      try {
        _appDb.getPlatform().dropTable(_appDb.getDataSource(), null, cacheTable, purge);
      }
      catch (Exception ex) {
        LOG.error("Cannot drop table [" + cacheTable + "]. " + ex.getMessage());
      }
    }
  }

  private void dropDanglingTables(boolean purge) {
    String schema = _appDb.getConfig().getLogin();
    try {
      String[] tables = _appDb.getPlatform().queryTableNames(_appDb.getDataSource(), schema, CACHE_TABLE_PREFIX + "%");
      LOG.info("Dropping " + tables.length + " dangling tables...");
      for (String table : tables)  _appDb.getPlatform().dropTable(_appDb.getDataSource(), null, table, purge);
    }
    catch (Exception ex) {
      LOG.error(ex.getMessage());
    }
  }
 
  private void createStepAnalysisCache() throws WdkModelException {
    try {
      LOG.info("Creating step analysis results cache.");
      _stepAnalysisFactory.createResultsTable();
    }
    catch (WdkModelException ex) {
      LOG.error("Unable to create step analysis results cache. " + ex.getMessage());
      throw ex;
    }
  }

  private void clearStepAnalysisCache() {
    try {
      LOG.info("Clearing step analysis results cache.");
      _stepAnalysisFactory.clearResultsTable();
    }
    catch (Exception ex) {
      LOG.error("Unable to clear step analysis results cache. " + ex.getMessage());
    }
  }

  private void dropStepAnalysisCache(boolean purge) {
    try {
      LOG.info("Dropping step analysis results cache (purge = " + purge + ").");
      _stepAnalysisFactory.dropResultsTable(purge);
    }
    catch (Exception ex) {
      LOG.error("Unable to drop step analysis results cache (purge = " + purge + "). " + ex.getMessage());
    }
  }

}
