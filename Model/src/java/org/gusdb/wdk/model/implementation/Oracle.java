package org.gusdb.wdk.model.implementation;


import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkLogManager;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

/**
 * An implementation of RDBMSPlatformI for Oracle 8i.  
 *
 * @author Steve Fischer
 * @version $Revision$ $Date$ $Author$
 */
public class Oracle implements RDBMSPlatformI {
    
    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.Oracle");
    
    private DataSource dataSource;
    private GenericObjectPool connectionPool;
    

    public Oracle() {}

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
     * Write the output of a query into a table, to which will be added a 
     * column "i" numbering the rows.
     */
    public void createTableFromQuerySql(DataSource dataSource,
					     String tableName, 
					     String sql) throws SQLException {
	
	//Initialize the table with the results of <code>sql</code>
	String newSql = "create table " + tableName + " as " + sql;
	
	SqlUtils.execute(dataSource, newSql);

	//Add "i" to the table and initialize each row in that column to be rownum
	String alterSql = "alter table " + tableName + " add i number(12)";

	SqlUtils.execute(dataSource, alterSql);

	String rownumSql = "update " + tableName + " set i = rownum";
	
	SqlUtils.execute(dataSource, rownumSql);
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.RDBMSPlatformI#createDataSource(java.lang.String, java.lang.String, java.lang.String)
     */
    public void init(String url, String user, String password, Integer minIdle,
		     Integer maxIdle, Integer maxWait, Integer maxActive, 
		     Integer initialSize) throws SQLException {
        
	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
	this.connectionPool = new GenericObjectPool(null);
                
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
        
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
	System.err.println("max wait: " + maxWait);
	connectionPool.setMaxWait(maxWait.intValue());
	connectionPool.setMaxIdle(maxIdle.intValue());
	connectionPool.setMinIdle(minIdle.intValue());
	connectionPool.setMaxActive(maxActive.intValue());
	//no initial size yet
        
	this.dataSource = dataSource;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.RDBMSPlatformI#close()
     */
    public void close() throws WdkModelException {
        try {
            connectionPool.close();
        }
        catch (Exception exp) {
            throw new WdkModelException(exp);
        }
    }
}


