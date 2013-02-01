/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.QueryMonitor;

/**
 * @author Jerric Gao
 * 
 */
public final class SqlUtils {

  private static final Logger logger = Logger.getLogger(SqlUtils.class);

  private static final Set<String> queryNames = new HashSet<>();

  /**
   * Close the resultSet and the underlying statement, connection
   * 
   * @param resultSet
   * @throws SQLException
   * @throws SQLException
   */
  public static void closeResultSet(ResultSet resultSet) {
    try {
      if (resultSet != null) {
        // close the statement in any way
        Statement stmt = null;
        try {
          try {
            stmt = resultSet.getStatement();
          } finally {
            resultSet.close();
          }
        } finally {
          closeStatement(stmt);
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Close the statement and underlying connection
   * 
   * @param stmt
   * @throws SQLException
   */
  public static void closeStatement(Statement stmt) {
    try {
      if (stmt != null) {
        // close the connection in any way
        Connection connection = null;
        try {
          try {
            connection = stmt.getConnection();
          } finally {
            stmt.close();
          }
        } finally {
          if (connection != null)
            connection.close();
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static PreparedStatement getPreparedStatement(DataSource dataSource,
      String sql) throws WdkModelException {
    Connection connection = null;
    PreparedStatement ps = null;
    try {
      connection = dataSource.getConnection();
      ps = connection.prepareStatement(sql);
      ps.setFetchSize(100);
      return ps;
    } catch (SQLException ex) {
      logger.error("Failed to prepare query:\n" + sql);
      closeStatement(ps);

      if (ps == null && connection != null)
        closeQuietly(connection);
      throw new WdkModelException("Unable to get prepared statement.", ex);
    }
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param dataSource
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static boolean executePreparedStatement(WdkModel wdkModel,
      PreparedStatement stmt, String sql, String name) throws SQLException,
      WdkUserException, WdkModelException {
    try {
      long start = System.currentTimeMillis();
      boolean result = stmt.execute();
      verifyTime(wdkModel, sql, name, start);
      return result;
    } catch (SQLException ex) {
      logger.error("Failed to execute statement: \n" + sql);
      throw ex;
    }
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param dataSource
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static int executeUpdate(WdkModel wdkModel, DataSource dataSource,
      String sql, String name) throws WdkModelException {
    Connection connection = null;
    Statement stmt = null;
    try {
      long start = System.currentTimeMillis();
      connection = dataSource.getConnection();
      stmt = connection.createStatement();
      int result = stmt.executeUpdate(sql);
      verifyTime(wdkModel, sql, name, start);
      return result;
    } catch (SQLException ex) {
      logger.error("Failed to run nonQuery:\n" + sql);
      throw new WdkModelException("Failed to execute update: " + sql, ex);
    } finally {
      closeStatement(stmt);
      if (stmt == null && connection != null)
        closeQuietly(connection);
    }
  }

  /**
   * execute the update using an open connection, and returns the number of rows
   * affected. Use this if you have a connection you want to use again such as
   * one that is autocommit=false
   * 
   * @param connection
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static int executeUpdate(WdkModel wdkModel, Connection connection,
      String sql, String name) throws SQLException, WdkUserException,
      WdkModelException {
    Statement stmt = null;
    try {
      long start = System.currentTimeMillis();
      stmt = connection.createStatement();
      int result = stmt.executeUpdate(sql);
      verifyTime(wdkModel, sql, name, start);
      return result;
    } catch (SQLException ex) {
      logger.error("Failed to run nonQuery:\n" + sql);
      throw ex;
    } finally {
      if (stmt != null)
        stmt.close();
    }
  }

  /**
   * Run a query and returns a resultSet. the calling code is responsible for
   * closing the resultSet using the helper method in SqlUtils.
   * 
   * @param dataSource
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static ResultSet executeQuery(WdkModel wdkModel,
      DataSource dataSource, String sql, String name) throws WdkModelException {
    return executeQuery(wdkModel, dataSource, sql, name, 100);
  }

  public static ResultSet executeQuery(WdkModel wdkModel,
      DataSource dataSource, String sql, String name, int fetchSize)
      throws WdkModelException {
    ResultSet resultSet = null;
    Connection connection = null;
    try {
      long start = System.currentTimeMillis();
      connection = dataSource.getConnection();
      Statement stmt = connection.createStatement();
      stmt.setFetchSize(fetchSize);
      resultSet = stmt.executeQuery(sql);
      verifyTime(wdkModel, sql, name, start);
      return resultSet;
    } catch (SQLException ex) {
      logger.error("Failed to run query:\n" + sql);
      if (resultSet == null && connection != null)
        closeQuietly(connection);
      closeResultSet(resultSet);
      throw new WdkModelException("Failure executing query: " + sql, ex);
    }
  }

  /**
   * Run the scalar value and returns a single value. If the query returns no
   * rows or more than one row, a WdkModelException will be thrown; if the query
   * returns a single row with many columns, the value in the first column will
   * be returned.
   * 
   * @param dataSource
   * @param sql
   * @return the first column of the first row in the result
   * @throws SQLException
   *           database or query failure
   * @throws WdkModelException
   * @throws WdkModelException
   *           query returns no row
   * @throws WdkUserException
   */
  public static Object executeScalar(WdkModel wdkModel, DataSource dataSource,
      String sql, String name) throws WdkModelException {
    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(wdkModel, dataSource, sql, name);
      if (!resultSet.next())
        throw new WdkModelException("The SQL doesn't return any row:\n" + sql);
      return resultSet.getObject(1);
    } catch (SQLException e) {
      throw new WdkModelException("Unable to execute scalar query: " + sql, e);
    } finally {
      closeResultSet(resultSet);
    }
  }

  public static Set<String> getColumnNames(ResultSet resultSet)
      throws SQLException {
    Set<String> columns = new LinkedHashSet<String>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    int count = metaData.getColumnCount();
    for (int i = 0; i < count; i++) {
      columns.add(metaData.getColumnName(i));
    }
    return columns;
  }

  /**
   * Escapes the input string for use in LIKE clauses to allow matching special
   * chars
   * 
   * @param value
   * @return the input value with special characters escaped
   */
  public static String escapeWildcards(String value) {
    return value.replaceAll("%", "{%}").replaceAll("_", "{_}");
  }

  public static void verifyTime(WdkModel wdkModel, String sql, String name,
      long fromTime) throws WdkModelException {
    // verify the name
    if (name.length() > 100 || name.indexOf('\n') >= 0) {
      StringWriter writer = new StringWriter();
      new Exception().printStackTrace(new PrintWriter(writer));
      logger.warn("The name of the sql is suspicious, name: '" + name
          + "', trace:\n" + writer.toString());
    }

    double seconds = (System.currentTimeMillis() - fromTime) / 1000D;
    logger.trace("SQL [" + name + "] executed in " + seconds + " seconds.");
    logger.trace(sql);

    if (seconds < 0) {
      logger.error("code error, negative exec time:");
      new Exception().printStackTrace();
    }
    QueryMonitor monitor = wdkModel.getQueryMonitor();
    // convert the time to seconds
    // log time & sql for slow query. goes to warn log
    if (seconds >= monitor.getSlow() && !monitor.isIgnoredSlow(sql)) {
      logger.warn("SLOW QUERY LOG [" + name + "]: " + seconds + " seconds.\n"
          + sql);

      // // also send email to admin
      // String email = wdkModel.getModelConfig().getAdminEmail();
      // if (email != null) {
      // String subject = "[" + wdkModel.getProjectId()
      // + "] Super Slow Query [" + name + "] " + seconds
      // + " seconds";
      //
      // Calendar cal = Calendar.getInstance();
      // SimpleDateFormat sdf = new SimpleDateFormat(
      // "yyyy-MM-dd HH:mm:ss");
      // String content = "<p>Recorded: "
      // + sdf.format(cal.getTime()) + "</p>\n<p>" + sql
      // + "</p>";
      // Utilities.sendEmail(wdkModel, email, email, subject,
      // content);
      // }
    }

    // log time for baseline query, and only sql for the first time. goes to
    // info log
    if (seconds >= monitor.getBaseline() && !monitor.isIgnoredBaseline(sql)) {
      String message = "QUERY LOG [" + name + "]: " + seconds + " seconds.";
      synchronized (queryNames) {
        if (!queryNames.contains(name)) {
          queryNames.add(name);
          message += "\n" + sql;
        }
      }
      logger.info(message);
    }
  }

  public static void closeQuietly(Wrapper... wrappers) {
    for (Wrapper wrap : wrappers) {
      if (wrap != null) {
        if (wrap instanceof DatabaseResultStream) {
          try {
            ((DatabaseResultStream) wrap).close();
          } catch (IOException e) {}
        }
        if (wrap instanceof ResultSet) {
          try {
            ((ResultSet) wrap).close();
          } catch (SQLException e) {}
        }
        if (wrap instanceof CallableStatement) {
          try {
            ((CallableStatement) wrap).close();
          } catch (SQLException e) {}
        }
        if (wrap instanceof PreparedStatement) {
          try {
            ((PreparedStatement) wrap).close();
          } catch (SQLException e) {}
        }
        if (wrap instanceof Statement) {
          try {
            ((Statement) wrap).close();
          } catch (SQLException e) {}
        }
        if (wrap instanceof Connection) {
          try {
            ((Connection) wrap).close();
          } catch (SQLException e) {}
        }
      }
    }
  }

  public static void attemptRollback(Connection connection) {
    try {
      connection.rollback();
    } catch (SQLException e) {
      logger.error("Could not roll back transaction!", e);
    }
  }

  /**
   * private constructor, make sure SqlUtils cannot be instanced.
   */
  private SqlUtils() {}

}
