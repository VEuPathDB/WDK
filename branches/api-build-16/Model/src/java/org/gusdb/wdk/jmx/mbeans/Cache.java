package org.gusdb.wdk.jmx.mbeans;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import javax.sql.DataSource;

/**
 * MBean representing the WDK database cache.
 *
 * Performs SQL queries of the cache tables. Uses the WDK's
 * queryPlatform (the application database).
 *
 * @see org.gusdb.wdk.model.WdkModel#getQueryPlatform()
 */
public class Cache extends BeanBase implements CacheMBean   {

  DataSource dataSource;
  
  public Cache() {
    super();
    DBPlatform platform = wdkModel.getQueryPlatform();
    dataSource = platform.getDataSource();
  }

  /**
   * Returns the count of QUERYRESULT tables in
   * the user schema. Specifically,
   *
   * <pre>
   * {@code
   * select                               
   * count(table_name) cache_count        
   * from user_tables                     
   * where table_name like 'QUERYRESULT%' 
   * }
   * </pre>
   */
  public String getcache_table_count() {
    StringBuffer sql = new StringBuffer();

    sql.append(" select                                ");
    sql.append(" count(table_name) cache_count         ");
    sql.append(" from user_tables                      ");
    sql.append(" where table_name like 'QUERYRESULT%'  ");
    
    return query(sql.toString());
  }

  /**
   * execute given SQL string
   *
   * @param SQL to execute
   * @return String result
   */
  private String query(String sql) {
    String value = null;
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      ps = SqlUtils.getPreparedStatement(dataSource, sql);
      rs = ps.executeQuery();
     if (rs.next()) {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        for (int i=1; i<numColumns+1; i++) {
          String columnName = rsmd.getColumnName(i).toLowerCase();
          value = rs.getString(columnName);
        }
      }
    } catch (WdkModelException | SQLException sqle) {
      logger.fatal(sqle);
    } finally {
        SqlUtils.closeResultSet(rs);
    }
    return value;
  }

  /**
   * MBean operation to reset WDK database cache through
   * call to CacheFactory resetCache().
   *
   * @see org.gusdb.wdk.model.dbms.CacheFactory#resetCache
   */
  public void resetWdkCache() {
    try {
      CacheFactory factory = wdkModel.getResultFactory().getCacheFactory();      
      factory.resetCache(true, true);
    } catch (Exception e) {
        // TODO: something
    }
  }
}
