package org.gusdb.wdk.model.implementation;


import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkLogManager;
import org.gusdb.wdk.model.ResultFactory;

import java.io.Serializable;
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
 * An implementation of RDBMSPlatformI for PostgreSQL 
 *
 * @author Angel Pizarro
 * @version $Revision$ $Date$ $Author$
 */
public class PostgreSQL implements RDBMSPlatformI, Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8999951914815274776L;

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.PostgreSQL");
    
    private DataSource dataSource;
    private GenericObjectPool connectionPool;

    public PostgreSQL() {}

    public DataSource getDataSource(){
	return dataSource;
    }

    public String getTableFullName(String schemaName, String tableName) {
	return schemaName + "." + tableName;
    }

    public String getTableAliasAs() {
	return "AS ";
    }

    public String getNextId(String schemaName, String tableName) throws SQLException  {
        String sql = "select nextval(' " + schemaName + "." + tableName + 
        "_pkseq ')";
        String nextId = SqlUtils.runStringQuery(dataSource, sql);
        logger.finest("getNextId is: "+nextId+" after running "+sql);
        return nextId;
    }
    
    public String cleanStringValue(String val) {
	return val.replaceAll("'", "''");
    }

    public String getNumberDataType() {
        return "numeric";
    }
    
    public String getClobDataType() {
        return "text";
    }
    
    public String getVarcharDataType(int length) {
        return "varchar(" + length + ")";
    }

    public String getCurrentDateFunction() {
	//	return "select LOCALTIMESTAMP(0)";
	return "LOCALTIMESTAMP(0)";
    }
    
    public boolean checkTableExists(String tableName) throws SQLException{
	
	String[] parts = tableName.split("\\.");
	String owner = parts[0];
	String realTableName =  parts[1];

	String sql = "select tableowner, tablename from pg_tables where schemaname='" + owner + 
	    "' and tablename='" + realTableName.toLowerCase() + "'";

	String result = SqlUtils.runStringQuery(dataSource, sql);
	
	boolean tableExists = result == null? false : true;
	return tableExists;
    }
    
    public void createSequence(String sequenceName, int start, int increment) throws SQLException {
	String sql = "create sequence " + sequenceName + " start " + start +
	    " increment " + increment  ;
	SqlUtils.execute(dataSource, sql);
    }

    public void dropSequence(String sequenceName) throws SQLException {

	String sql = "drop sequence " + sequenceName;
	SqlUtils.execute(dataSource, sql);
    }

  /**
     * @return count of removed rows
     */
    public int dropTable(String fullTableName) throws SQLException {
        // String sql = "truncate table " + fullTableName;
        // SqlUtils.executeUpdate(dataSource, sql);

        String sql = "drop table " + fullTableName;

        return SqlUtils.executeUpdate(dataSource, sql);
    }
    
    /**
     * Write the output of a query into a table, to which will be added a 
     * column "i" numbering the rows.
     */
    public void createResultTable(DataSource dataSource,
				  String tableName, 
				  String sql) throws SQLException {
	
	//Initialize the table with the results of <code>sql</code>
	String newSql = "create table " + tableName + " as " + sql;
	
	SqlUtils.execute(dataSource, newSql);

	addIndexColumn(dataSource, tableName);

    }

    public  void addIndexColumn(DataSource dataSource, String tableName) throws SQLException {

	//Add "i" to the table and initialize each row in that column to be rownum
	String alterSql = "alter table " + tableName + " add " + ResultFactory.RESULT_TABLE_I + " numeric(12)";

	SqlUtils.execute(dataSource, alterSql);

	// Create a temporary  sequence for the table
	this.createSequence(tableName + "_sq",1,1);

	try {
	    String rownumSql = "update " + tableName + " set " 
		+ ResultFactory.RESULT_TABLE_I + " = nextval('" + tableName + "_sq')" ;
	    SqlUtils.execute(dataSource, rownumSql);
	} finally {
	    // drop the temporary sequence 
	    this.dropSequence(tableName + "_sq");
	}
    }

    
    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.RDBMSPlatformI#createDataSource(java.lang.String, java.lang.String, java.lang.String)
     */
    public void init(String url, String user, String password, Integer minIdle,
		     Integer maxIdle, Integer maxWait, Integer maxActive, 
		     Integer initialSize, String fileName) throws WdkModelException {

	try{
	    DriverManager.registerDriver(new org.postgresql.Driver());
	    System.setProperty("jdbc.drivers","org.postgresql.Driver");
	    this.connectionPool = new GenericObjectPool(null);
	    
	    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
	    
        // variable never used
	    // PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
	    
	    PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
	    
	    this.dataSource = dataSource;
	}
	catch (Exception sqle){
	    throw new WdkModelException("\n\n*************ERROR***********\nCould not connect to database.\nIt is possible that you are using an incorrect url for connecting to the database or that your login or password is incorrect.\nPlease check " + fileName + " and make sure all information provided there is valid.\n(This is the most likely cause of the error; note it could be something else)\n\n", sqle);
	    
	}

	connectionPool.setMaxWait(maxWait.intValue());
	connectionPool.setMaxIdle(maxIdle.intValue());
	connectionPool.setMinIdle(minIdle.intValue());
	connectionPool.setMaxActive(maxActive.intValue());
	 
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

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.RDBMSPlatformI#getMinus()
     */
    public String getMinus() {
        // TODO Auto-generated method stub
        return "EXCEPT";
    }
    
    public int getActiveCount() {
        return connectionPool.getNumActive();
    }
 
    public int getIdleCount() {
        return connectionPool.getNumIdle();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.RDBMSPlatformI#getTableCount(java.lang.String)
     */
    public int getTableCount(String tableNamePattern) throws SQLException {
        throw new UnsupportedOperationException("Method not supported in PostgreSQL");
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.RDBMSPlatformI#forceDropTables(java.lang.String)
     */
    public int forceDropTables(String tableNamePattern) throws SQLException {
        throw new UnsupportedOperationException("Method not supported in PostgreSQL");
    }
}


