package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.RDBMSPlatformI;

import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * An implementation of RDBMSPlatformI for Oracle 8i.  
 *
 * @author Steve Fischer
 * @version $Revision$ $Date$ $Author$
 */
public class Oracle implements RDBMSPlatformI {
    
    private static final Logger logger = Logger.getLogger("org.gusdb.gus.wdk.model.implementation.Oracle");
    
    private DataSource dataSource;

    public Oracle() {}

    public void setDataSource(DataSource dataSource) {
	this.dataSource = dataSource;
    }

    public DataSource getDataSource(){
	return dataSource;
    }

    public String getTableFullName(String schemaName, String tableName) {
	return schemaName + "." + tableName;
    }

    public String getNextId(String schemaName, String tableName) throws SQLException  {
        String sql = "select " + schemaName + "." + tableName + 
        "_pkseq.nextval from dual";
        String nextId = SqlUtils.runStringQuery(dataSource, sql);
        logger.finest("getNextId is: "+nextId+" after running "+sql);
        return nextId;
    }

    public String cleanStringValue(String val) {
	return val.replaceAll("'", "''");
    }

    public String getCurrentDateFunction() {
	return "sysdate";
    }
    
    public boolean checkTableExists(String tableName) throws SQLException{
	
	String[] parts = tableName.split("\\.");
	String owner = parts[0];
	String realTableName =  parts[1];

	String sql = "select owner, table_name from all_tables where owner='" + owner.toUpperCase() + 
	    "' and table_name='" + realTableName.toUpperCase() + "'";
	
	String result = SqlUtils.runStringQuery(dataSource, sql);
	
	boolean tableExists = result == null? false : true;
	return tableExists;
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


