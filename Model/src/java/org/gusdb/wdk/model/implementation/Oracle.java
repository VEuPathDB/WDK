package org.gusdb.gus.wdk.model.implementation;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.gusdb.gus.wdk.model.RDBMSPlatformI;

/**
 * An implementation of RDBMSPlatformI for Oracle 8i.  
 *
 * @author Steve Fischer
 * @version $Revision$ $Date$ $Author$
 */
public class Oracle implements RDBMSPlatformI {
    
    DataSource dataSource;

    public Oracle() {}

    public void setDataSource(DataSource dataSource) {
	this.dataSource = dataSource;
    }

    public String getTableFullName(String schemaName, String tableName) {
	return schemaName + "." + tableName;
    }

    public String getNextId(String schemaName, String tableName) throws SQLException  {
	String sql = "select " + schemaName + "." + tableName + 
	    "_pkseq.nextval from dual";
	return SqlUtils.runStringQuery(dataSource, sql);
    }

    public String cleanStringValue(String val) {
	return val.replaceAll("'", "''");
    }

    public String getCurrentDateFunction() {
	return "sysdate";
    }

    public void createSequence(String sequenceName, int start, int increment) throws SQLException {

	String sql = "create sequence " + sequenceName + " start with " +
	    start + " increment by " + increment;
	SqlUtils.execute(dataSource, sql);
    }

    public void dropSequence(String sequenceName) throws SQLException {

	String sql = "drop sequence " + sequenceName;
	SqlUtils.execute(dataSource, sql);
    }

    /**
     * @return count of removed rows
     */
    public int dropTable(String schemaName, String tableName) throws SQLException  {
	String sql = "truncate table " + schemaName + "." + tableName;

	SqlUtils.executeUpdate(dataSource, sql);
	
	sql = "drop table " + schemaName + "." + tableName;
	
	return SqlUtils.executeUpdate(dataSource, sql);
    }
    
    /**
     * Transform an SQL query into an equivalent query that will write its
     * output (along with a column "i" numbering the rows) into a table.
     */
    public void createTableFromQuerySql(DataSource dataSource,
					     String tableName, 
					     String sql) throws SQLException {
	sql = sql.replaceAll("\\s+from\\s+", ", rownum as i from ");

	// Construct a 'create as' statement that will number the rows in
	// the query (using the ROWNUM pseudocolumn) and write the entire
	// shebang into the specified table, <code>tableName</code>.

	String newSql = "create table " + tableName + " as " + sql;

	SqlUtils.execute(dataSource, newSql);
    }
}


