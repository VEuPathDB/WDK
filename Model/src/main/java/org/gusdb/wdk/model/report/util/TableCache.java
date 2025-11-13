package org.gusdb.wdk.model.report.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;

/**
 * a class to manage a disk cache for reporters that output wdk tables.  
 * the db table has columns for PK, number of rows and a clob, 
 * which is the reporter-specific format for the table value of a specific record instance.
 * reporters that use the cache populate this table as they acquire data for specific records.
 * the db table to use for the cache is determined by a property of the reporter in the model xml
 * file.  the wdk has no facility for clearing the cache.  this seems to be the responsibility 
 * of the managers of the database, upon new releases of the application data.
 */
public class TableCache {

  public static final String PROPERTY_TABLE_CACHE = "table_cache";
  public static final String PROPERTY_RECORD_ID_COLUMN = "record_id_column";

  private final String[] _pkColumns;
  private final DatabaseInstance _cacheDb;
  private final String _querySql;
  private final String _insertSql;

  // managed DB objects; caller is responsible for these objects via open() and close() methods
  private boolean _dbOpen = false;
  private PreparedStatement _psQuery = null;
  private PreparedStatement _psInsert = null;
  private boolean _uncommittedRows = false;

  public TableCache(RecordClass recordClass, DatabaseInstance cacheDb, String dbTableName) {
    _pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    _cacheDb = cacheDb;
    _querySql = getQuerySql(_pkColumns, dbTableName);
    _insertSql = getInsertSql(_pkColumns, dbTableName, _cacheDb.getPlatform());
  }

  public void open() throws SQLException {
    if (!_dbOpen) {
      _dbOpen = true;
      DataSource dataSource = _cacheDb.getDataSource();
      _psQuery = SqlUtils.getPreparedStatement(dataSource, _querySql, SqlUtils.Autocommit.OFF);
      _psInsert = SqlUtils.getPreparedStatement(dataSource, _insertSql, SqlUtils.Autocommit.ON);
    }
  }

  public void close() {
    if (_dbOpen) {
      SqlUtils.closeStatement(_psQuery);
      _psQuery = null;
      SqlUtils.closeStatement(_psInsert);
      _psInsert = null;
      _dbOpen = false;
    }
  }

  private static String getQuerySql(String[] pkColumns, String cacheTableName) {
    StringBuilder sqlQuery = new StringBuilder("SELECT row_count, content FROM ")
    .append(cacheTableName).append(" WHERE ");
    for (String column : pkColumns) {
      sqlQuery.append(column).append(" = ? AND ");
    }
    sqlQuery.append(" table_name = ?");
    return sqlQuery.toString();
  }

  private static String getInsertSql(String[] pkColumns, String cacheTableName, DBPlatform platform) {
    StringBuilder sqlInsert = new StringBuilder("INSERT INTO ");
    sqlInsert.append(cacheTableName).append(" (wdk_table_id, ");
    for (String column : pkColumns) {
      sqlInsert.append(column).append(", ");
    }
    sqlInsert.append(" table_name, row_count, content) VALUES (");
    sqlInsert.append(platform.getNextIdSqlExpression("apidb", "wdkTable"));
    sqlInsert.append(", ");
    for (int i = 0; i < pkColumns.length; i++) {
      sqlInsert.append("?, ");
    }
    sqlInsert.append("?, ?, ?)");
    return sqlInsert.toString();
  }

  public static String getCacheTableName(Map<String, String> properties) {
    return properties.get(PROPERTY_TABLE_CACHE);
  }

  public TwoTuple<Integer, String> getCachedTableValue(RecordInstance record, String tableName) throws SQLException {
    Map<String, String> pkValues = record.getPrimaryKey().getValues();
    long start = System.currentTimeMillis();
    for (int index = 1; index <= _pkColumns.length; index++) {
      Object value = pkValues.get(_pkColumns[index - 1]);
      _psQuery.setObject(index, value);
    }
    _psQuery.setString(_pkColumns.length + 1, tableName);
    ResultSet rs = null;
    try {
      rs = _psQuery.executeQuery();
      QueryLogger.logEndStatementExecution(_querySql, "wdk-report-full-select-count", start);
      if (rs.next()) {
        int rowCount = rs.getInt("row_count");
        String content = _cacheDb.getPlatform().getClobData(rs, "content");
        return new TwoTuple<Integer, String>(rowCount, content);
      }
      // no rows returned means no value in cache
      return null;
    }
    finally {
      SqlUtils.closeResultSetOnly(rs);
    }
  }

  // NOTE: will do insert even if value already present, resulting in multiple rows if so
  public void insertTableValue(RecordInstance record, String tableName,
      TwoTuple<Integer, String> tableData) throws SQLException {
    if (tableData.getFirst() > 0) {
      // insert into table cache if table had >0 rows
      Map<String, String> pkValues = record.getPrimaryKey().getValues();
      int index;
      for (index = 1; index <= _pkColumns.length; index++) {
        Object value = pkValues.get(_pkColumns[index - 1]);
        _psInsert.setObject(index, value);
      }
      _psInsert.setString(index++, tableName);
      _psInsert.setInt(index++, tableData.getFirst());
      _cacheDb.getPlatform().setClobData(_psInsert, index++, tableData.getSecond(), false);
      _psInsert.addBatch();
      _uncommittedRows = true;
    }
  }

  public void flushBatch() throws SQLException {
    if (_uncommittedRows) {
      long start = System.currentTimeMillis();
      _psInsert.executeBatch();
      QueryLogger.logEndStatementExecution(_insertSql, "wdk-report-full-insert", start);
    }
  }

}
