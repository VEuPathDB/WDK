package org.gusdb.wdk.model.implementation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class SqlUtils {

	private static DefaultFederation instance = new SqlUtilsWSWRSXML(); //SqlUtilsJDBC() or SqlUtilsWSWRSXML() or SqlUtilsWSCRSBinary() default federation implementation

	//setting up federation approach by reading the config from ComponentDBConnection
	public void SqlUtils() {
		if(ComponentDBConnection.getFEDERATION() == null) return;
		if(ComponentDBConnection.getFEDERATION().equalsIgnoreCase("JDBC")) {
			instance = new SqlUtilsJDBC();
			return;
		}
		if(ComponentDBConnection.getFEDERATION().equalsIgnoreCase("WSXML")) {
			instance = new SqlUtilsWSWRSXML();
			return;
		}
		if(ComponentDBConnection.getFEDERATION().equalsIgnoreCase("WSBINARY")) {
			instance = new SqlUtilsWSCRSBinary();
			return;
		}
	}
	
	public static void registerFederation(DefaultFederation instanceD) {
		instance = instanceD;
	}


	public static ResultSet getResultSet(DataSource dataSource, String sql)
			throws SQLException {
		return instance.getResultSet(dataSource, sql);
	}

	/**
	 * Gets a result set using a PreparedStatement. Since the statement is
	 * prepared, it is likely that the user is intending to use it more than
	 * once before closing it. It is thus up to the user to close the
	 * PreparedStatement using <code>closeStatement</code> when finished.
	 */
	public static int getResultSet(DataSource dataSource,
			PreparedStatement prepStmt) throws SQLException {
		return instance.getResultSet(dataSource, prepStmt);
	}

	public static PreparedStatement getPreparedStatement(DataSource dataSource,
			String sql) throws SQLException {
		return instance.getPreparedStatement(dataSource, sql);
	}

	public static void closeResultSet(ResultSet resultSet) throws SQLException {
		instance.closeResultSet(resultSet);
	}

	public static void closeStatement(Statement stmt) throws SQLException {
		instance.closeStatement(stmt);
	}

	/**
	 * Execute a JDBC query that returns a single string (and return the
	 * string!)
	 */
	public static String runStringQuery(DataSource dataSource, String sql)
			throws SQLException {
		return instance.runStringQuery(dataSource, sql);
	}

	/**
	 * Execute a JDBC query that returns a single Integer and return the
	 * Integer.
	 */
	public static Integer runIntegerQuery(DataSource dataSource, String sql)
			throws SQLException {
		return instance.runIntegerQuery(dataSource, sql);
	}

	/**
	 * Execute a JDBC query that returns a list of strings, and return the
	 * strings in an array.
	 */
	public static String[] runStringArrayQuery(DataSource dataSource, String sql)
			throws SQLException {
		return instance.runStringArrayQuery(dataSource, sql);
	}

	/**
	 * @return the list of column names from the select statement
	 */
	public static String[] getColumnNames(DataSource dataSource, String sql)
			throws SQLException {
		return instance.getColumnNames(dataSource, sql);
	}

	/**
	 * Perform a JDBC insert/update/delete.
	 * 
	 * @return The number of rows affected.
	 */
	public static int executeUpdate(DataSource dataSource, String sql)
			throws SQLException {
		return instance.executeUpdate(dataSource, sql);
	}

	/**
	 * Execute an SQL statement
	 * 
	 * @return Value as described in java.sql.statement.execute()
	 */
	public static boolean execute(DataSource dataSource, String sql)
			throws SQLException {
		return instance.execute(dataSource, sql);
	}

	// TODO: this method and writeResultSet should be factored
	public static void printResultSet(ResultSet rs) throws SQLException {
		instance.printResultSet(rs);
	}

	public static void writeResultSet(ResultSet rs, StringBuffer buf)
			throws SQLException {
		instance.writeResultSet(rs, buf);
	}

	public static synchronized void showConnectionCount() {
		instance.showConnectionCount();
	}

}
