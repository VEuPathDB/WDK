package org.gusdb.wdk.model.dbms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;

public class CacheFactory {

  static final String CACHE_TABLE_PREFIX = "QueryResult";

  static final String COLUMN_QUERY_ID = "query_id";
  static final String COLUMN_QUERY_NAME = "query_name";
  static final String COLUMN_TABLE_NAME = "table_name";

  static final String TABLE_INSTANCE = "QueryInstance";

  public static final String COLUMN_INSTANCE_ID = "wdk_instance_id";
  public static final String COLUMN_ROW_ID = "wdk_row_id";
  static final String COLUMN_INSTANCE_CHECKSUM = "instance_checksum";
  static final String COLUMN_PARAMS = "params";
  static final String COLUMN_RESULT_MESSAGE = "result_message";

  private static Logger logger = Logger.getLogger(CacheFactory.class);

  private WdkModel wdkModel;
  private DBPlatform platform;
  private DataSource dataSource;

  public CacheFactory(WdkModel wdkModel, DatabaseInstance database) {
    this.wdkModel = wdkModel;
    this.platform = database.getPlatform();
    this.dataSource = database.getDataSource();
  }

  public void createCache() {
  
    createQueryInstanceTable();

    // create the id sequence for the query & instance index
    String sequenceName = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      platform.createSequence(dataSource, sequenceName, 1, 1);
    }
    catch (Exception ex) {
      logger.error("Cannot create sequence [" + sequenceName + "]. " + ex.getMessage());
      System.exit(1);
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

  public void recreateCache(boolean purge, boolean forceDrop) {
    // drop cache;
    dropCache(purge, forceDrop);
    // create them back
    createCache();
  }

  public void dropCache(boolean purge, boolean forceDrop) {
    // drop cache tables
    dropCacheTables(purge, forceDrop);

    try {
      platform.dropTable(dataSource, null, TABLE_INSTANCE, purge);
    }
    catch (Exception ex) {
      logger.error("Cannot drop table [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }
    String instanceSeq = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + instanceSeq, "wdk-cache-drop-instance-seq");
    }
    catch (Exception ex) {
      logger.error("Cannot drop sequence [" + instanceSeq + "]. " + ex.getMessage());
    }
    
    // drop step analysis results cache
    dropStepAnalysisCache(purge);
  }

  public void dropCache(int instanceId, boolean purge) {

    // delete the instance index
    String sql = "DELETE FROM " + TABLE_INSTANCE + " WHERE " + COLUMN_INSTANCE_ID + " = " + instanceId;
    try {
      SqlUtils.executeUpdate(dataSource, sql, "wdk_cache_delete_instance_index");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. ", ex);
    }

    // drop result table
    String cacheTable = ResultFactory.getCacheTableName(instanceId);
    try {
      platform.dropTable(dataSource, null, cacheTable, purge);
    }
    catch (Exception ex) {
      logger.error("Cannot crop table " + cacheTable, ex);
    }
  }

  public void dropCache(String queryName, boolean purge) {
    dropCacheTables(purge, " where " + COLUMN_QUERY_NAME + " = '" + queryName + "'");

  }

  // TODO: fix this method
  public void showCache() throws SQLException {
    // get query instance summary
    StringBuffer sqlInstance = new StringBuffer("SELECT ");
    sqlInstance.append("i.").append(COLUMN_QUERY_ID).append(", ");
    sqlInstance.append("q.").append(COLUMN_QUERY_NAME).append(", ");
    sqlInstance.append("q.").append(COLUMN_TABLE_NAME).append(", ");
    sqlInstance.append("count(*) AS instances FROM ");
    sqlInstance.append(TABLE_INSTANCE).append(" i, ");
    //    sqlInstance.append(TABLE_QUERY).append(" q ");
    sqlInstance.append(" WHERE q.").append(COLUMN_QUERY_ID);
      sqlInstance.append(" = i.").append(COLUMN_QUERY_ID);
    sqlInstance.append(" GROUP BY i.").append(COLUMN_QUERY_ID).append(", ");
    sqlInstance.append(" q.").append(COLUMN_QUERY_NAME).append(", ");
    sqlInstance.append(" q.").append(COLUMN_TABLE_NAME);
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sqlInstance.toString(), "wdk-cache-instance-summary");
      System.err.println("========================= Cache Stattistics =========================");
      int queryCount = 0;
      while (resultSet.next()) {
        int queryId = resultSet.getInt(COLUMN_QUERY_ID);
        String queryName = resultSet.getString(COLUMN_QUERY_NAME);
        String cacheTable = resultSet.getString(COLUMN_TABLE_NAME);
        int instanceCount = resultSet.getInt("instances");
  
        String sqlSize = "SELECT count(*) FROM " + cacheTable;
        Object objSize = SqlUtils.executeScalar(dataSource, sqlSize, "wdk-cache-query-size");
        int size = Integer.parseInt(objSize.toString());
  
        System.err.println("CACHE [" + queryId + "] " + queryName + ": " + instanceCount +
            " instances, total " + size + " rows");
        queryCount++;
      }
      System.err.println();
      System.err.println("Total: " + queryCount + " cache tables.");
      System.err.println("====================== End of Cache Stattistics ======================");
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  private void createQueryInstanceTable() {
    // create the cache index table
    StringBuffer sql = new StringBuffer("CREATE TABLE ");
    sql.append(TABLE_INSTANCE).append(" ( ");

    // define columns
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
      SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-cache-create-instance");
    }
    catch (Exception ex) {
      logger.error("Cannot create table [" + TABLE_INSTANCE + "]. " + ex.getMessage());
      System.exit(1);
    }
  }

  private void dropCacheTables(boolean purge, boolean forceDrop) {
    dropCacheTables(purge, "");
    if (forceDrop) dropDanglingTables(purge);

  }

  private void dropCacheTables(boolean purge, String whereClause) {

    // get a list of cache tables
    StringBuffer sql = new StringBuffer("SELECT DISTINCT ");
    sql.append(COLUMN_TABLE_NAME).append(" FROM ").append(TABLE_INSTANCE).append(" " + whereClause);
    ResultSet resultSet = null;
    Set<String> cacheTables = new LinkedHashSet<String>();
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(), "wdk-cache-select-cache-table");
      while (resultSet.next()) {
        cacheTables.add(resultSet.getString(COLUMN_TABLE_NAME));
      }
    }
    catch (Exception ex) {
      logger.error("Cannot query on table [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }

    // delete rows from cache index table
    try {
      SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TABLE_INSTANCE + " " + whereClause, "wdk-cache-delete-instances");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }

    // drop the cache tables
    for (String cacheTable : cacheTables) {
      try {
        platform.dropTable(dataSource, null, cacheTable, purge);
      }
      catch (Exception ex) {
        logger.error("Cannot drop table [" + cacheTable + "]. " + ex.getMessage());
      }
    }
  }

  private void dropDanglingTables(boolean purge) {
    String schema = wdkModel.getModelConfig().getAppDB().getLogin();
    try {
      String[] tables = platform.queryTableNames(dataSource, schema, CACHE_TABLE_PREFIX + "%");
      logger.info("Dropping " + tables.length + " dangling tables...");
      for (String table : tables)  platform.dropTable(dataSource, null, table, purge);
    }
    catch (Exception ex) {
      logger.error(ex.getMessage());
    }
  }
 
  private void createStepAnalysisCache() {
    try {
      logger.info("Creating step analysis results cache.");
      wdkModel.getStepAnalysisFactory().createResultsTable();
    }
    catch (Exception ex) {
      logger.error("Unable to create step analysis results cache. " + ex.getMessage());
      System.exit(1);
    }
  }

  private void clearStepAnalysisCache() {
    try {
      logger.info("Clearing step analysis results cache.");
      wdkModel.getStepAnalysisFactory().clearResultsTable();
    }
    catch (Exception ex) {
      logger.error("Unable to clear step analysis results cache. " + ex.getMessage());
    }
  }

  private void dropStepAnalysisCache(boolean purge) {
    try {
      logger.info("Dropping step analysis results cache (purge = " + purge + ").");
      wdkModel.getStepAnalysisFactory().dropResultsTable(purge);
    }
    catch (Exception ex) {
      logger.error("Unable to drop step analysis results cache (purge = " + purge + "). " + ex.getMessage());
    }
  }

}
