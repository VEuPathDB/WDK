package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.controller.WdkLogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class SqlUtils {
    

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.gus.wdk.model.implementation.SqlUtils");
    
    public static ResultSet getResultSet(DataSource dataSource, String sql) throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            logger.finest("Success in executing sql in getResultSet: '" + sql + "'");
            return rs;
        } catch (SQLException sqlE) {
            logger.severe("Failed attempting to execute sql in getResultSet: '" + sql + "'");
            throw sqlE;
        }
    }
    
    /**
     * Gets a result set using a PreparedStatement.  Since the statement is prepared, it is likely that 
     * the user is intending to use it more than once before closing it.  It is thus up to the user to close
     * the PreparedStatement using <code>closeStatement</code> when finished.
     */
    public static int getResultSet(DataSource dataSource, PreparedStatement prepStmt) throws SQLException {
        
        int result = -1;
        result = prepStmt.executeUpdate();
        return result;
    }
    
    
    
    public static PreparedStatement getPreparedStatement(DataSource dataSource, String sql)throws SQLException{
        Connection connection = dataSource.getConnection();
        PreparedStatement prepStmt = connection.prepareStatement(sql);
        return prepStmt;
    }
    
    public static void closeResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            Statement stmt = resultSet.getStatement();
            try { resultSet.close(); } catch(Exception e) { }
            closeStatement(stmt);
        }
    }
    
    public static void closeStatement(Statement stmt) throws SQLException{
        if (stmt != null){
            Connection connection = stmt.getConnection();
            try { stmt.close(); } catch(Exception e) { }
            try { connection.close(); } catch(Exception e) { }
        }
    }
    
    /**
     * Execute a JDBC query that returns a single string 
     * (and return the string!)
     */
    public static String runStringQuery(DataSource dataSource, String sql) throws SQLException {
        ResultSet resultSet = null;
        String result = null;
        
        try {
            resultSet = getResultSet(dataSource, sql);
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed attempting to execute sql in runStringQuery: '" + sql + "'");
            e.printStackTrace(System.err);
            throw e;
        } finally {
            closeResultSet(resultSet);
        }
        
        return result;
    }
    
    
    /**
     * Execute a JDBC query that returns a single Integer and return the Integer.
     */
    public static Integer runIntegerQuery(DataSource dataSource, String sql) throws SQLException {
        ResultSet resultSet = null;
        Integer result = null;
        
        try {
            resultSet = getResultSet(dataSource, sql);
            if (resultSet.next()) result = new Integer(resultSet.getInt(1));
        } catch (SQLException e) {
            System.err.println("Failed attempting to execute sql in runIntegerQuery: '" + sql + "'");
            e.printStackTrace(System.err);
            throw e;
        } finally {
            closeResultSet(resultSet);
        }
        
        return result;
    }
    
    /**
     * Execute a JDBC query that returns a list of strings,
     * and return the strings in an array.
     */
    public static String[] runStringArrayQuery(DataSource dataSource, String sql) throws SQLException {
        ResultSet resultSet = null;
        Connection connection = null;
        Statement stmt = null;
        Vector v = new Vector();
        
        try {
            connection = dataSource.getConnection();
            stmt = connection.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) v.addElement(resultSet.getString(1));
        } catch (SQLException e) {
            System.err.println("Failed attempting to execute sql in runStringArrayQuery: '" + sql + "'");
            throw e;
        } finally {
            closeResultSet(resultSet);
        }
        
        String result[] = new String[v.size()];
        v.copyInto(result);
        return result;
    }
    
    /**
     * @return the list of column names from the select statement
     */
    public static String[] getColumnNames(DataSource dataSource, String sql) throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ArrayList colNames = new ArrayList();
        
        try {
            connection = dataSource.getConnection();
            stmt = connection.prepareStatement(sql);
            ResultSetMetaData metaData = stmt.getMetaData();
            int colCount = metaData.getColumnCount();
            for (int i=0; i<colCount; i++) {
                colNames.add(metaData.getColumnName(i));
            }
        } finally {
            closeStatement(stmt);
        }
        
        return (String[])colNames.toArray();	
    }
    
    /**
     * Perform a JDBC insert/update/delete.
     *
     * @return The number of rows affected.
     */
    public static int executeUpdate(DataSource dataSource, String sql) throws SQLException {
        int result = -1;
        Connection connection = null;
        Statement stmt = null;
        
        try {
            connection = dataSource.getConnection();
            stmt = connection.createStatement();
            result = stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Failed executing sql:");
            System.err.println(sql);
            System.err.println("");
            throw e;
        } finally {
            closeStatement(stmt);
        }
        
        return result;
    }
    
    /**
     * Execute an SQL statement
     *
     * @return Value as described in java.sql.statement.execute()
     */
    public static boolean execute(DataSource dataSource, String sql) throws SQLException {
        Connection connection = null;
        Statement stmt = null;
        
        try {
            connection = dataSource.getConnection();
            stmt = connection.createStatement();
            return stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed executing sql:");
            System.err.println(sql);
            System.err.println("");
            throw e;
        } finally {
            closeStatement(stmt);
        }
    }
    
    
    // TODO: this method and writeResultSet should be factored
    public static void printResultSet(ResultSet rs) throws SQLException {
        try {
            int colCount = rs.getMetaData().getColumnCount();
            int count = 0;
            while (rs.next() && count++ <= 100) {
                for (int i=1; i<=colCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }
    
    public static void writeResultSet(ResultSet rs, StringBuffer buf) throws SQLException {
        String newline = System.getProperty( "line.separator" );
        try {
            int colCount = rs.getMetaData().getColumnCount();
            int count = 0;
            while (rs.next() && count++ <= 100) {
                for (int i=1; i<=colCount; i++) {
                    buf.append(rs.getString(i) + "\t");
                }
                buf.append(newline);
            }
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }

    /**
     * @param url
     * @param user
     * @param password
     * @param maxWait
     * @return
     */
    public static DataSource createDataSource(String connectURI, String login, String password, int maxWait) {
        
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        
        if (maxWait >= 0) {
            connectionPool.setMaxWait(maxWait);
        }
        
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, login, password);
        
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        
        return dataSource;
    }
    
}
