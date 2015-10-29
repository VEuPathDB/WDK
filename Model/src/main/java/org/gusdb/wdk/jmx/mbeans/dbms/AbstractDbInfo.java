package org.gusdb.wdk.jmx.mbeans.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;

public abstract class AbstractDbInfo implements DbInfo {

  private static final Logger logger = Logger.getLogger(AbstractDbInfo.class);

  private final DatabaseInstance _database;
  private final DataSource _dataSource;
  private final HashMap<String, String> _databaseAttributes;

  public AbstractDbInfo(DatabaseInstance db) {
    _database = db;
    _dataSource = db.getDataSource();
    _databaseAttributes = new HashMap<String, String>();
  }

  /**
   * Returns SQL string for querying desired information about the database
   * such as version, user login, etc. Results will be parsed into
   * a Map with column header used for the map key and field values for
   * the map value. Typically you will want to only return simple 
   * key/value text pairs.
   */
  protected abstract String getMetaDataSql();
  /**
   * Returns SQL to retrieve the database host IP and/or host name.
   * This should probably be deprecated in favor of inclusion in getMetaDataSql().
   */
  protected abstract String getServerNameSql();
  /**
   * SQL for looking up any dblink definitions. Can return null if
   * dblinks are not supported.
   */
  protected abstract String getDblinkSql();
  /**
   * SQL for validating dblink. Can return null if
   * dblinks are not supported.
   */
  protected abstract String getDbLinkValidationSql(String dblink);

  public HashMap<String, String> getDatabaseAttributes() {
    return _databaseAttributes;
  }

  @Override
  public void populateDatabaseMetaDataMap(HashMap<String, String> metaDataMap) {
    String sql = getMetaDataSql();
    if (sql == null) return;

    ResultSet rs = null;
    PreparedStatement ps = null;
    logger.debug("querying database for misc. information");    
    try {
      ps = SqlUtils.getPreparedStatement(_dataSource, sql);
      rs = ps.executeQuery();
      if (rs.next()) {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        for (int i=1; i<numColumns+1; i++) {
          String columnName = rsmd.getColumnName(i).toLowerCase();
          metaDataMap.put(columnName, rs.getString(columnName) );
        }
      }
    }
    catch (SQLException sqle) {
      logger.error(sqle);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rs);
    }
  }

  @Override
  public void populateConnectionPoolDataMap(HashMap<String, String> poolMap) {
    poolMap.put("pool", Integer.toString(_database.getIdleCount()));
  }

  @Override
  public void populateServernameDataMap(HashMap<String, String> servernameDataMap) {
    String sql = getServerNameSql();
    if (sql == null) return;

    PreparedStatement ps = null;
    ResultSet rs = null;
    logger.debug("querying database for servername information");    

    try {
      ps = SqlUtils.getPreparedStatement(_dataSource, sql);
      String dbVendor = ps.getConnection().getMetaData().getDatabaseProductName();
      rs = ps.executeQuery();
      if (rs.next()) {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        for (int i=1; i<numColumns+1; i++) {
          String columnName = rsmd.getColumnName(i).toLowerCase();
          servernameDataMap.put(columnName, rs.getString(columnName) );
        }
      }
      if (dbVendor.equals("Oracle")) {
        // if no SQLException was caught then an ACL for UTL_INADDR must be in place.
        servernameDataMap.put("is_allowed_utl_inaddr", "true");
      }
    }
    catch (SQLException e) {
      if ( e.getMessage().startsWith("ORA-24247") ) {
        // oracle user needs an ACL for UTL_INADDR
        servernameDataMap.put("is_allowed_utl_inaddr", "false");
      }
      logger.error("Failed attempting\n" + sql + "\n", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rs);
    }
  }

  @Override
  public void populateDblinkList(ArrayList<Map<String, String>> dblinkList) {
    String sql = getDblinkSql();
    if (sql == null) return;

    ResultSet rs = null;
    PreparedStatement ps = null;

    try {
      ps = SqlUtils.getPreparedStatement(_dataSource, sql);
      rs = ps.executeQuery();
      while (rs.next()) {
        HashMap<String, String> map = new HashMap<String, String>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        for (int i=1; i<numColumns+1; i++) {
          String columnName = rsmd.getColumnName(i).toLowerCase();
          map.put(columnName, rs.getString(columnName) );
        }
        dblinkList.add(map);
      }
    }
    catch (SQLException sqle) {
      logger.error(sqle);
    }
    catch (Exception e) {
      logger.error("NPE ", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rs);
    }

    updateDblinkListWithValidity(dblinkList);
  }

  /**
    * Run a test query for each link in the given ArrayList of dblinks and
    * add an 'isValid' column with one of the follow values:
    * 
    * 0 if the link is valid. More precisely, the query
    * 'select 1 from dual@<link>' returns a row.
    *
    * 1 if the link is invalid. More precisely, if the link test throws an SqlException, as
    * is typical for bad username/password or when the host name could not be resolved.
    *
    * -1 if the link could not be tested. More precisely, throws a SQLSyntaxErrorException
    * as can happen for "ORA-02020: too many database links in use".
    */
  public void updateDblinkListWithValidity(ArrayList<Map<String, String>> dblinkList) {

    for (Map<String,String> map : dblinkList) {
      String sql = getDbLinkValidationSql(map.get("db_link"));
      if (sql == null) return;

      ResultSet rs = null;
      PreparedStatement ps = null;
      String columnName = "isValid";

      try {
        ps = SqlUtils.getPreparedStatement(_dataSource, sql);
        rs = ps.executeQuery();

        while (rs.next()) {
          map.put(columnName, "1" );
        }

        /** Call commit() to avoid "ORA-02020: too many database links in use" when 
          * the number of configured links exceeds the database's 'open_links'
          * parameter value. I do not understand why this alone is sufficient
          * and why I do not have to followup with 'alter session close database link ...'
          * as I would in an interactive session.
        **/
        ps.getConnection().commit();
      }
      catch (java.sql.SQLSyntaxErrorException sqee) {
        logger.error("Error while trying DB link validation SQL", sqee);
        map.put(columnName, "-1" );
      }
      catch (SQLException sqle) {
        logger.error("Error while trying DB link validation SQL", sqle);
        map.put(columnName, "0" );
      }
      catch (Exception e) {
        logger.error("Error while trying DB link validation SQL", e);
      }
      finally {
        SqlUtils.closeResultSetAndStatement(rs);
      }
    }
  }
}
