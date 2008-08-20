/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * @author Jerric Gao
 * 
 */
public final class SqlUtils {

    private static final Logger logger = Logger.getLogger(SqlUtils.class);

    /**
     * Close the resultSet and the underlying statement, connection
     * 
     * @param resultSet
     * @throws SQLException
     * @throws SQLException
     */
    public static void closeResultSet(ResultSet resultSet) throws SQLException {
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
    }

    /**
     * Close the statement and underlying connection
     * 
     * @param stmt
     * @throws SQLException
     */
    public static void closeStatement(Statement stmt) throws SQLException {
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
                connection.close();
            }
        }
    }

    public static PreparedStatement getPreparedStatement(DataSource dataSource,
            String sql) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            return ps;
        } catch (SQLException ex) {
            logger.error("Failed to prepare query: '" + sql + "'");
            closeStatement(ps);
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
     */
    public static int executeUpdate(DataSource dataSource, String sql)
            throws SQLException {
        Statement stmt = null;
        try {
            Connection connection = dataSource.getConnection();
            stmt = connection.createStatement();
            return stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.error("Failed to run nonQuery: '" + sql + "'");
            throw ex;
        } finally {
            closeStatement(stmt);
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
     */
    public static ResultSet executeQuery(DataSource dataSource, String sql)
            throws SQLException {
        ResultSet resultSet = null;
        try {
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            resultSet = stmt.executeQuery(sql);
            return resultSet;
        } catch (SQLException ex) {
            logger.error("Failed to run query: '" + sql + "'");
            closeResultSet(resultSet);
            throw ex;
        }
    }

    /**
     * Run the scalar value and returns a single value. If the query returns no
     * rows or more than one row, a SQLException will be thrown; if the query
     * returns a single row with many columns, the value in the first column
     * will be returned.
     * 
     * @param dataSource
     * @param sql
     * @return
     * @throws SQLException
     */
    public static Object executeScalar(DataSource dataSource, String sql)
            throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = executeQuery(dataSource, sql);
            Object value = null;
            if (resultSet.next()) value = resultSet.getObject(1);
            return value;
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
     * private constructor, make sure SqlUtils cannot be instanced.
     */
    private SqlUtils() {}
}
