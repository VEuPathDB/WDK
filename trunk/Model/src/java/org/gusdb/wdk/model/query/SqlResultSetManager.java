package org.gusdb.gus.wdk.model.query;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import oracle.jdbc.driver.OracleDriver;


public class SqlResultSetManager {

    DataSource dataSource;

    public SqlResultSetManager(DataSource dataSource) {
	this.dataSource = dataSource;
    }

    public ResultSet getResult(SqlQueryInstance instance) throws SQLException {
	ResultSet resultSet = null;
        Connection connection = null;
        Statement stmt = null;
			
	try {
	    connection = dataSource.getConnection();
	    stmt = connection.createStatement();
	    resultSet = stmt.executeQuery(instance.getSql());

	} catch (SQLException e) { 
	    System.err.println("");
	    System.err.println("Failed running query:");
	    System.err.println("\"" + instance.getSql() + "\"");
	    System.err.println("");
	    try { resultSet.close(); } catch(Exception e2) { }
	    try { stmt.close(); } catch(Exception e2) { }
	    try { connection.close(); } catch(Exception e2) { }
	    throw e;
	}
	return resultSet;
    }

    public void closeResultSet(ResultSet resultSet) throws SQLException {
	Statement stmt = resultSet.getStatement();
	Connection connection = stmt.getConnection();
	
	try { resultSet.close(); } catch(Exception e) { }
	try { stmt.close(); } catch(Exception e) { }
	try { connection.close(); } catch(Exception e) { }
    }
}
