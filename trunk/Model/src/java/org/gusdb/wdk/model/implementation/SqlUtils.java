package org.gusdb.gus.wdk.model.implementation;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sql.DataSource;

public class SqlUtils {

    public static ResultSet getResultSet(DataSource dataSource, String sql) throws SQLException {
	try {
	    Connection connection = dataSource.getConnection();
	    Statement stmt = connection.createStatement();
	    return stmt.executeQuery(sql);
	} catch (SQLException sqlE) {
	    System.err.println("Failed attempting to execute sql: '" + sql + "'");
	    throw sqlE;
	}
    }

    public static void closeResultSet(ResultSet resultSet) throws SQLException {
	if (resultSet != null) {
	    Statement stmt = resultSet.getStatement();
	    Connection connection = stmt.getConnection();
	    
	    try { resultSet.close(); } catch(Exception e) { }
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
	    if (resultSet.next()) result = resultSet.getString(1);
	} catch (SQLException e) {
	    System.err.println("Failed attempting to execute sql: '" + sql + "'");
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
	    System.err.println("Failed attempting to execute sql: '" + sql + "'");
	    throw e;
	} finally {
	    try { resultSet.close(); } catch(Exception e2) { }
	    try { stmt.close(); } catch(Exception e2) { }
	    try { connection.close(); } catch(Exception e2) { }
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
	    try { connection.close(); } catch(Exception e2) { }
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
	    try { stmt.close(); } catch(Exception e2) { }
	    try { connection.close(); } catch(Exception e2) { }
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
	    try { stmt.close(); } catch(Exception e2) { }
	    try { connection.close(); } catch(Exception e2) { }
	}
    }

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

}
