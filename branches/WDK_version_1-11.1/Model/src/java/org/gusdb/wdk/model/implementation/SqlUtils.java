package org.gusdb.wdk.model.implementation;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;


public class SqlUtils {
    
    // Added by Jerric - debug flag
    private static boolean debug = false;

    //private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.SqlUtils");
    private static final Logger logger = Logger.getLogger(SqlUtils.class);
    
    // set this variable to true will start a separate thread to monitor the
    // connection usage
    private static boolean createShowThread = false;
    
    public static ResultSet getResultSet(DataSource dataSource, String sql) throws SQLException {

        // TEST
        if (debug) System.out.println("<==getResultSet==>: " + sql);

        Statement stmt = null;
        try {
	    Connection connection = dataSource.getConnection();
        showConnectionCount();

        stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            //logger.debug("Success in executing sql in getResultSet: '" + sql + "'");
            return rs;
        } catch (SQLException sqlE) {
            logger.error("Failed attempting to execute sql in getResultSet: '" + sql + "'");
            closeStatement(stmt);
            throw sqlE;
        }
    }
    
    /**
     * Gets a result set using a PreparedStatement.  Since the statement is prepared, it is likely that 
     * the user is intending to use it more than once before closing it.  It is thus up to the user to close
     * the PreparedStatement using <code>closeStatement</code> when finished.
     */
    public static int getResultSet(DataSource dataSource, PreparedStatement prepStmt) throws SQLException {

        // TEST
        if (debug) System.out.println("<==getResultSetPrepared==>: " + prepStmt);
        
        int result = -1;
        result = prepStmt.executeUpdate();
        return result;
    }
    
    
    
    public static PreparedStatement getPreparedStatement(DataSource dataSource, String sql)throws SQLException{

        // TEST
        if (debug) System.out.println("<==getPreparedStatement==>: " + sql);

        Connection connection = dataSource.getConnection();
        showConnectionCount();
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
    
    public static void closeStatement(Statement stmt) throws SQLException {
        // TEST
        if (debug) System.out.println("<== CloseConnection ==>");

        if (stmt != null) {
            Connection connection = stmt.getConnection();
            try {
                stmt.close();
            } catch (Exception e) {}
            try {
                connection.close();
                showConnectionCount();
            } catch (Exception e) {}
        }
    }
    
    /**
     * Execute a JDBC query that returns a single string 
     * (and return the string!)
     */
    public static String runStringQuery(DataSource dataSource, String sql) throws SQLException {
        ResultSet resultSet = null;
        String result = null;

        // TEST
        if (debug) System.out.println("<==runStringQuery==>: " + sql);
        
        try {
            resultSet = getResultSet(dataSource, sql);
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            // System.err.println("Failed attempting to execute sql in runStringQuery: '" + sql + "'");
            logger.error("Failed attempting to execute sql in runStringQuery: '" + sql + "'");
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

        // TEST
        if (debug) System.out.println("<==runIntegerQuery==>: " + sql);
        
        try {
            resultSet = getResultSet(dataSource, sql);
            if (resultSet.next()) result = new Integer(resultSet.getInt(1));
        } catch (SQLException e) {
            // System.err.println("Failed attempting to execute sql in runIntegerQuery: '" + sql + "'");
            logger.error("Failed attempting to execute sql in runIntegerQuery: '" + sql + "'");
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
        Vector<String> v = new Vector<String>();

        // TEST
        if (debug) System.out.println("<==runStringArrayQuery==>: " + sql);
               
        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) v.addElement(resultSet.getString(1));
        } catch (SQLException e) {
            // System.err.println("Failed attempting to execute sql in runStringArrayQuery: '" + sql + "'");
            logger.error("Failed attempting to execute sql in runStringArrayQuery: '" + sql + "'");
            throw e;
        } finally {
            closeResultSet(resultSet);
        }
        
        String result[] = new String[v.size()];
        v.toArray(result);
        return result;
    }
    
    /**
     * @return the list of column names from the select statement
     */
    public static String[] getColumnNames(DataSource dataSource, String sql) throws SQLException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ArrayList<String> colNames = new ArrayList<String>();

        // TEST
        if (debug) System.out.println("<==getColumnNames==>: " + sql);

        try {
            connection = dataSource.getConnection();
            showConnectionCount();
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

        // TEST
        if (debug) System.out.println("<==executeUpdate==>: " + sql);

        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.createStatement();
            result = stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Failed executing sql:\n" + sql);
            logger.error("Failed attempting to execute sql in executeUpdate: '" + sql + "'");
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

        // TEST
        if (debug) System.out.println("<==execute==>: " + sql);
       
        try {
            connection = dataSource.getConnection();
            showConnectionCount();
            stmt = connection.createStatement();
            return stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed executing sql:");
            System.err.println(sql);
            System.err.println("");
            logger.error("Failed attempting to execute sql in executeUpdate: '" + sql + "'");
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

    public static synchronized void showConnectionCount() {
        if (createShowThread) {
            createShowThread = false;
            Thread t = new Thread() {

                public void run() {
                    logger.debug("Logging connections.");
                    while (true) {
                        WdkModel model = WdkModel.INSTANCE;
                        if (model != null) {
                            RDBMSPlatformI platform = model.getPlatform();
                            if (platform != null)
                                logger.debug("Connections: ("
                                        + platform.getActiveCount() + ", "
                                        + platform.getIdleCount() + ")");
                        }
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            // TODO Auto-generated catch block
                            ex.printStackTrace();
                        }
                    }
                }
            };
            t.start();
        }
    }
}
