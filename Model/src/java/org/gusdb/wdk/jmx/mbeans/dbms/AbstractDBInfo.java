package org.gusdb.wdk.jmx.mbeans.dbms;

import java.sql.Connection;
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

/**
  * Abstract class for collecting information about databases used by
  * the WDK. Subclasses define the database-specific SQL for querying
  * metadata. The DataSource for querying is provided by the instantiated
  * WDK model.
  */
public abstract class AbstractDBInfo {

  protected HashMap<String, String> databaseAttributes;
  private static final Logger logger = Logger.getLogger(AbstractDBInfo.class);
  DataSource datasource;

  public AbstractDBInfo() {
    databaseAttributes = new HashMap<String, String>();
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
   * Returns SQL to retreive the database host IP and/or host name.
   * This should probably be deprecated in favor of inclusion in getMetaDataSql().
   */
  protected abstract String getServerNameSql();
  /**
   * SQL for looking up any dblink definitions. Can return null if
   * dblinks are not supported.
   */
  protected abstract String getDblinkSql();

  protected void setDatabaseAttributes(HashMap<String, String> databaseAttributes) {
    this.databaseAttributes = databaseAttributes;
  }

  public HashMap<String, String> getDatabaseAttributes() {
    return databaseAttributes;
  }

  public void setDatasource(DataSource datasource) {
    this.datasource = datasource;
  }
  
  public void populateDatabaseMetaDataMap(HashMap<String, String> metaDataMap) {
    String sql = getMetaDataSql();
    if (sql == null) return;

    ResultSet rs = null;
    PreparedStatement ps = null;
    logger.debug("querying database for misc. information");    
    try {
      ps = SqlUtils.getPreparedStatement(datasource, sql);
      rs = ps.executeQuery();
     if (rs.next()) {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        for (int i=1; i<numColumns+1; i++) {
          String columnName = rsmd.getColumnName(i).toLowerCase();
          metaDataMap.put(columnName, rs.getString(columnName) );
        }
      }
    } catch (SQLException sqle) {
      logger.error(sqle);
    } finally {
        SqlUtils.closeResultSetAndStatement(rs);
    }
  }

  public void populateServernameDataMap(HashMap<String, String> servernameDataMap) {
    String sql = getServerNameSql();
    if (sql == null) return;
    
    Connection connection = null;
    ResultSet rs = null;
    PreparedStatement ps = null;
    logger.debug("querying database for servername information");    

    try {
      connection = datasource.getConnection();
      String dbVendor = connection.getMetaData().getDatabaseProductName();
      ps = SqlUtils.getPreparedStatement(datasource, sql);
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
    } catch (SQLException e) {
        if ( e.getMessage().startsWith("ORA-24247") ) {
          // oracle user needs an ACL for UTL_INADDR
          servernameDataMap.put("is_allowed_utl_inaddr", "false");
        }
        logger.error("Failed attempting\n" + sql + "\n" + e);
    } finally {
        SqlUtils.closeResultSetAndStatement(rs);
        try {
            if (connection != null) connection.close();
        } catch(SQLException ex) {
            logger.error(ex);
        }
    }  
  }

  public void populateDblinkList(ArrayList<Map<String, String>> dblinkList) {
    String sql = getDblinkSql();
    if (sql == null) return;

    ResultSet rs = null;
    PreparedStatement ps = null;

    try {
      ps = SqlUtils.getPreparedStatement(datasource, sql);
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
    } catch (SQLException sqle) {
      logger.error(sqle);
    } catch (Exception e) {
        logger.error("NPE ", e);
    } finally {
        SqlUtils.closeResultSetAndStatement(rs);
    }

  }
  
}
