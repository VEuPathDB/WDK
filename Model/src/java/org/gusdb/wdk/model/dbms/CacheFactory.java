/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class CacheFactory {

  private static final String CACHE_TABLE_PREFIX = "QueryResult";

  private static final String TABLE_QUERY = "Query";

  static final String COLUMN_QUERY_ID = "query_id";
  private static final String COLUMN_QUERY_NAME = "query_name";
  private static final String COLUMN_QUERY_CHECKSUM = "query_checksum";
  private static final String COLUMN_TABLE_NAME = "table_name";

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
    // create index tables for query and query instance;
    createQueryTable();
    createQueryInstanceTable();

    // create the id sequence for the query & instance index
    String sequenceName = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      platform.createSequence(dataSource, sequenceName, 1, 1);
    }
    catch (Exception ex) {
      logger.error("Cannot create sequence [" + sequenceName + "]. " + ex.getMessage());
    }

    sequenceName = TABLE_QUERY + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      platform.createSequence(dataSource, sequenceName, 1, 1);
    }
    catch (Exception ex) {
      logger.error("Cannot create sequence [" + sequenceName + "]. " + ex.getMessage());
    }
  }

  public void resetCache(boolean purge, boolean forceDrop) {
    // drop cache tables and we are done
    dropCacheTables(purge, forceDrop);
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

    // drop index tables and sequences
    try {
      platform.dropTable(dataSource, null, TABLE_QUERY, purge);
    }
    catch (Exception ex) {
      logger.error("Cannot drop table [" + TABLE_QUERY + "]. " + ex.getMessage());
    }

    String querySeq = TABLE_QUERY + DBPlatform.ID_SEQUENCE_SUFFIX;
    try {
      SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + querySeq, "wdk-cache-drop-query-seq");
    }
    catch (Exception ex) {
      logger.error("Cannot drop sequence [" + querySeq + "]. " + ex.getMessage());
    }
  }

  public void dropCache(int instanceId, boolean purge) {
    // get cache table name
    String sql = "SELECT q." + COLUMN_TABLE_NAME 
        + " FROM " + TABLE_QUERY + " q, " + TABLE_INSTANCE + " qi " 
        + " WHERE q." + COLUMN_QUERY_ID + " = qi." + COLUMN_QUERY_ID 
        + "   AND qi." + COLUMN_INSTANCE_ID + " = " + instanceId;
    String cacheTable;
    try {
      cacheTable = (String) SqlUtils.executeScalar(dataSource, sql, "wdk-cache-select-table-name");
    }
    catch (Exception ex) {
      logger.error("Cannot get cache table for query instance " + instanceId, ex);
      return;
    }

    // no need to drop cache table, since it may be used by other queries
    // from the same question; just delete rows from instance table.
    sql = "DELETE FROM " +cacheTable + " WHERE " + COLUMN_INSTANCE_ID + " = " + instanceId;
    try {
      SqlUtils.executeUpdate(dataSource, sql, "wdk-cache-delete-by-instance");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from [" + cacheTable + "]. ", ex);
    }

    // delete the instance index
    sql = "DELETE FROM " + TABLE_INSTANCE + " WHERE " + COLUMN_INSTANCE_ID + " = " + instanceId;
    try {
      SqlUtils.executeUpdate(dataSource, sql, "wdk_cache_delete_instance_index");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. ", ex);
    }
  }

  public void dropCache(String queryName, boolean purge) {
    String whereClause = " FROM " + TABLE_QUERY + " WHERE " + COLUMN_QUERY_NAME +" ='" + queryName + "'";
    // get cache table name
    String cacheTable = null;
    String sql = "SELECT " + COLUMN_TABLE_NAME + whereClause; 
    try {
      cacheTable = (String) SqlUtils.executeScalar(dataSource, sql, "wdk-select-cache-table-by-query");
      platform.dropTable(dataSource, null, cacheTable, purge);
    }
    catch (Exception ex) {
      logger.error("Cannot drop table [" + cacheTable + "] for query: " + queryName, ex);
    }
    
    // delete instance index
    sql = "DELETE FROM " + TABLE_INSTANCE + " WHERE " + COLUMN_QUERY_ID + " IN " 
        + "(SELECT " + COLUMN_QUERY_ID + whereClause + ")"; 
    try {
      SqlUtils.executeUpdate(dataSource, sql, "wdk-cache-delete-instances-by-name");
    }
    catch (SQLException ex) {
      logger.error("Cannot delete rows from " + TABLE_INSTANCE + " for query " + queryName, ex);
    }

    // delete query index
    sql = "DELETE " + whereClause;
    try {
      SqlUtils.executeUpdate(dataSource, sql, "wdk-cache-delete-query-by-name");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from " + TABLE_QUERY + " for query " + queryName, ex);
    }
    finally {}
  }

  public void showCache() throws SQLException {
    // get query instance summary
    StringBuffer sqlInstance = new StringBuffer("SELECT ");
    sqlInstance.append("i.").append(COLUMN_QUERY_ID).append(", ");
    sqlInstance.append("q.").append(COLUMN_QUERY_NAME).append(", ");
    sqlInstance.append("q.").append(COLUMN_TABLE_NAME).append(", ");
    sqlInstance.append("count(*) AS instances FROM ");
    sqlInstance.append(TABLE_INSTANCE).append(" i, ");
    sqlInstance.append(TABLE_QUERY).append(" q ");
    sqlInstance.append(" WHERE q.").append(COLUMN_QUERY_ID);
    sqlInstance.append(" = i.").append(COLUMN_QUERY_ID);
    sqlInstance.append(" GROUP BY i.").append(COLUMN_QUERY_ID).append(", ");
    sqlInstance.append(" q.").append(COLUMN_QUERY_NAME).append(", ");
    sqlInstance.append(" q.").append(COLUMN_TABLE_NAME);
    ResultSet resultSet = SqlUtils.executeQuery(dataSource, sqlInstance.toString(),
        "wdk-cache-instance-summary");
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
    SqlUtils.closeResultSetAndStatement(resultSet);
  }

  private void createQueryTable() {
    // create the cache index table
    StringBuffer sql = new StringBuffer("CREATE TABLE ");
    sql.append(TABLE_QUERY).append(" ( ");
    sql.append(COLUMN_QUERY_ID).append(" ");
    sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
    sql.append(COLUMN_QUERY_NAME).append(" ");
    sql.append(platform.getStringDataType(200)).append(" NOT NULL, ");
    sql.append(COLUMN_QUERY_CHECKSUM).append(" ");
    sql.append(platform.getStringDataType(40)).append(" NOT NULL, ");
    sql.append(COLUMN_TABLE_NAME).append(" ");
    sql.append(platform.getStringDataType(30)).append(" NOT NULL, ");
    sql.append(" CONSTRAINT PK_").append(COLUMN_QUERY_ID);
    sql.append(" PRIMARY KEY (").append(COLUMN_QUERY_ID).append(") )");
    try {
      SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-cache-create-query");
    }
    catch (Exception ex) {
      logger.error("Cannot create table [" + TABLE_QUERY + "]. " + ex.getMessage());
    }
  }

  private void createQueryInstanceTable() {
    // create the cache index table
    StringBuffer sql = new StringBuffer("CREATE TABLE ");
    sql.append(TABLE_INSTANCE).append(" ( ");

    // define columns
    sql.append(COLUMN_INSTANCE_ID).append(" ");
    sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
    sql.append(COLUMN_QUERY_ID).append(" ");
    sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
    sql.append(COLUMN_INSTANCE_CHECKSUM).append(" ");
    sql.append(platform.getStringDataType(40)).append(" NOT NULL, ");
    sql.append(COLUMN_PARAMS).append(" ");
    sql.append(platform.getClobDataType()).append(", ");
    sql.append(COLUMN_RESULT_MESSAGE).append(" ");
    sql.append(platform.getClobDataType());

    // define primary key
    sql.append(", CONSTRAINT PK_").append(COLUMN_INSTANCE_ID);
    sql.append(" PRIMARY KEY (").append(COLUMN_INSTANCE_ID).append("), ");

    // define foreign key to Query table
    sql.append(" CONSTRAINT FK_").append(COLUMN_QUERY_ID);
    sql.append(" FOREIGN KEY (").append(COLUMN_QUERY_ID).append(")");
    sql.append(" REFERENCES ").append(TABLE_QUERY);
    sql.append("(").append(COLUMN_QUERY_ID).append(") )");
    try {
      SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-cache-create-instance");
    }
    catch (Exception ex) {
      logger.error("Cannot create table [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }
  }

  private void dropCacheTables(boolean purge, boolean forceDrop) {
    // get a list of cache tables
    StringBuffer sql = new StringBuffer("SELECT DISTINCT ");
    sql.append(COLUMN_TABLE_NAME).append(" FROM ").append(TABLE_QUERY);

    ResultSet resultSet = null;
    Set<String> cacheTables = new LinkedHashSet<String>();
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(), "wdk-cache-select-cache-table");
      while (resultSet.next()) {
        cacheTables.add(resultSet.getString(COLUMN_TABLE_NAME));
      }
    }
    catch (Exception ex) {
      logger.error("Cannot query on table [" + TABLE_QUERY + "]. " + ex.getMessage());
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
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

    // delete rows from cache index table
    try {
      SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TABLE_INSTANCE, "wdk-cache-delete-instances");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. " + ex.getMessage());
    }
    try {
      SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TABLE_QUERY, "wdk-cache-delete-queries");
    }
    catch (Exception ex) {
      logger.error("Cannot delete rows from [" + TABLE_QUERY + "]. " + ex.getMessage());
    }

    if (forceDrop)
      dropDanglingTables();
  }

  private void dropDanglingTables() {
    String schema = wdkModel.getModelConfig().getAppDB().getLogin();
    try {
      String[] tables = platform.queryTableNames(dataSource, schema, CACHE_TABLE_PREFIX + "%");
      logger.info("Dropping " + tables.length + " dangling tables...");
      for (String table : tables) {
        try {
          SqlUtils.executeUpdate(dataSource, "DROP TABLE " + table, "wdk-cache-drop-dangling-cache");
        }
        catch (Exception ex) {
          logger.error("Cannot drop table [" + table + "]. " + ex.getMessage());
        }
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage());
    }
  }

  public QueryInfo getQueryInfo(Query query) throws WdkModelException {
    String checksum = getChecksum(query);
    String queryName = query.getFullName();
    QueryInfo queryInfo = new QueryInfo(queryName, checksum);

    StringBuffer sql = new StringBuffer("SELECT * FROM " + TABLE_QUERY);
    sql.append(" WHERE ").append(COLUMN_QUERY_NAME).append(" = ? ");
    sql.append("   AND ").append(COLUMN_QUERY_CHECKSUM).append(" = ? ");
    sql.append(" ORDER BY " + COLUMN_QUERY_ID);

    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
      ps.setString(1, queryName);
      ps.setString(2, checksum);
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(sql.toString(), "wdk-cache-select-query-info", start, resultSet);

      if (resultSet.next()) {
        queryInfo.setExist(true);

        queryInfo.setQueryId(resultSet.getInt(COLUMN_QUERY_ID));
        queryInfo.setCacheTable(resultSet.getString(COLUMN_TABLE_NAME));
      }
      else {
        queryInfo.setExist(false);

        int queryId = platform.getNextId(dataSource, null, TABLE_QUERY);
        queryInfo.setQueryId(queryId);
        queryInfo.setCacheTable(CACHE_TABLE_PREFIX + queryId);
      }
      logger.debug("Get QueryInfo: " + queryInfo);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to check query info.", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
      if (resultSet == null)
        SqlUtils.closeStatement(ps);
    }

    return queryInfo;
  }

  /**
   * The only info we need for the query checksum is the columns to make sure we have correct columns to store
   * info we need.
   * 
   * @param query
   * @return
   * @throws JSONException
   * @throws WdkModelException
   */
  private String getChecksum(Query query) throws WdkModelException {
    JSONObject jsQuery = new JSONObject();
    try {
      jsQuery.put("name", query.getFullName());

      JSONArray jsColumns = new JSONArray();
      for (Column column : query.getColumns()) {
        jsColumns.put(column.getJSONContent());
      }
      jsQuery.put("columns", jsColumns);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return Utilities.encrypt(jsQuery.toString());
  }

  public void createQueryInfo(QueryInfo queryInfo) {
    // query already exists don't need to create
    if (queryInfo.isExist())
      return;

    PreparedStatement psInsert = null;
    try {
      // cache table doesn't exist, create one
      StringBuffer sql = new StringBuffer("INSERT INTO ");
      sql.append(TABLE_QUERY).append(" (");
      sql.append(COLUMN_QUERY_ID).append(", ");
      sql.append(COLUMN_QUERY_NAME).append(", ");
      sql.append(COLUMN_QUERY_CHECKSUM).append(", ");
      sql.append(COLUMN_TABLE_NAME).append(") ");
      sql.append("VALUES (?, ?, ?, ?)");

      long start = System.currentTimeMillis();
      psInsert = SqlUtils.getPreparedStatement(dataSource, sql.toString());
      psInsert.setInt(1, queryInfo.getQueryId());
      psInsert.setString(2, queryInfo.getQueryName());
      psInsert.setString(3, queryInfo.getQueryChecksum());
      psInsert.setString(4, queryInfo.getCacheTable());
      psInsert.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(), "wdk-cache-insert-instance", start);

      queryInfo.setExist(true);
    }
    catch (SQLException e) {
      throw new WdkRuntimeException("Unable to create update table.", e);
    }
    finally {
      SqlUtils.closeStatement(psInsert);
    }
  }
}
