package org.gusdb.gus.wdk.model.query;

import java.util.Collection;
import java.util.Iterator;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;

import java.io.IOException;
import java.io.File;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

import org.xml.sax.SAXException;

public class SqlResultFactory {

    DataSource dataSource;
    RDBMSPlatformI platform;
    String cacheSchemaName;
    String cacheTableName;
    String cacheFullName;

    public SqlResultFactory(DataSource dataSource, RDBMSPlatformI platform,
			    String cacheSchemaName, String cacheTableName) {
	this.dataSource = dataSource;
	this.platform = platform;
	this.cacheSchemaName = cacheSchemaName;
	this.cacheTableName = cacheTableName;
	this.cacheFullName = platform.getFullTableName(cacheSchemaName, 
						       cacheTableName);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////   public  /////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public ResultSet getResult(SqlQueryInstance instance) throws SQLException {
	return instance.getIsCacheable()?
	    getCachedResult(instance) : getDirectResult(instance);
    }

    /**
     * @param numParams Number of parameters allowed in a cached query
     */
    public void createCache(int numParams) throws SQLException {
	String newline = System.getProperty( "line.separator" );

	// Format sql to create table
	StringBuffer sqlb = new StringBuffer();
	String tblName = cacheSchemaName + "." + cacheTableName;
	sqlb.append("create table " + tblName + 
		    " (query_id number(12) not null, query_name varchar2(100) not null, ");
	
	for (int i=0;i < numParams; i++) {
	    sqlb.append("param" + i + " varchar2(25), ");
	}
	sqlb.append("result_table varchar2(30) not null, start_time date not null, end_time date, dataset_name varchar2(100), session_id varchar2(50))");

	// Execute it
	System.err.println(newline + "Making cache table " + tblName 
			   + " with sql:" + newline + sqlb.toString()
			   + newline);

	SqlUtils.execute(dataSource, sqlb.toString());
	System.err.println("Done" + newline);

	// Create sequence 
	platform.createSequence(tblName + "_pkseq", 1, 1);
	System.err.println("Creating sequence " + tblName + "_pkseq"
			   + newline);
	System.err.println("Done" + newline);
    }

    /**
     * Remove all cached entries; it first deletes all the temporary 
     * tables (found by querying the Queries table) and then deletes all
     * of its rows from the Queries table.  
     *
     * This is *not* transaction safe.  A better way would be to copy the
     * names of the result tables to a "dropThese" table and delete
     * the associated rows in the cache table both within one transaction; then
     * separately, as a post-process, drop the tables in the dropThese table.
     * 
     */    
    public void resetCache() throws SQLException {

	// Query for the names of all cached result tables
	//
	StringBuffer s = new StringBuffer();
	s.append("select result_table from " + cacheFullName);

	String tables[] = 
	    SqlUtils.runStringArrayQuery(dataSource, s.toString());
	int nTables = tables.length;
	int nDropped = 0;

	System.err.println("Attempting to drop " + nTables + " results tables");
	for (int i =0;i < nTables;++i) {
	    try {
		platform.dropTable(cacheSchemaName, tables[i]);
		nDropped++;
	    } catch (SQLException e) {}
	}

	System.err.println("Succeeded in dropping " + nDropped);
	System.err.println("Deleting all rows from " + cacheFullName);

	SqlUtils.execute(dataSource, "delete from " + cacheFullName);
    }

    /**
     * Drop all tables and sequences associated with the cache
     */    
    public void dropCache() throws SQLException {
	resetCache();
	System.err.println("Dropping table " + cacheFullName);
	platform.dropTable(cacheSchemaName, cacheTableName);
	System.err.println("Dropping sequence " + cacheFullName + "_pkseq");
	platform.dropSequence(cacheFullName + "_pkseq");
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////   protected   /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    protected ResultSet getDirectResult(SqlQueryInstance instance) throws SQLException {
	ResultSet resultSet = null;

	try {
	    resultSet = SqlUtils.getResultSet(dataSource, instance.getSql());

	} catch (SQLException e) { 
	    System.err.println("");
	    System.err.println("Failed running query:");
	    System.err.println("\"" + instance.getSql() + "\"");
	    System.err.println("");
	    SqlUtils.closeResultSet(resultSet);
	    throw e;
	}
	return resultSet;
    }

    protected ResultSet getCachedResult(QueryInstance instance) throws SQLException {
	String resultTable = getResultTable(instance);
	if (resultTable == null) {
	    resultTable = insertQueryInstance(instance);
	    platform.createTableFromQueryInstance(dataSource, resultTable, (SqlQueryInstance)instance);
	    finishQueryInstance(instance);
	}
	return fetchCachedResult(resultTable);
    }

    /**
     * Return the name of the database table that contains a 
     * cached query result.  Returns null if no cached query
     * result is stored in the database (either because it was
     * never there or it has been expired.)  Simply looks up the
     * name of the table in the "Queries" table, which is essentially
     * a mapping from query name + query parameters to temporary
     * table name.
     *
     * @param instance  The instance of the query
     */
    protected String getResultTable(QueryInstance instance)  throws SQLException {

	// Construct SQL query to retrieve the requested table's name
	//
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("select result_table from " + cacheFullName
		    + " where ");
	sqlb.append(instanceWhereClause(instance));
	sqlb.append(" and end_time IS NOT NULL"); 
	String resultTableName = SqlUtils.runStringQuery(dataSource, sqlb.toString());
	return (resultTableName == null) ? 
	    null : platform.getFullTableName(cacheSchemaName,resultTableName);
    }

    /**
     * Record in the database that a query has been started (by 
     * entering an appropriate row into the Queries table.)  Returns 
     * the (automatically generated) name of a table to which the query 
     * results should be written.
     */
    protected String insertQueryInstance(QueryInstance instance) throws SQLException {
	return insertQueryInstance(instance, null, null);
    }

     /**
     * This variant of the method is called when the query has a dataset
     * param.  In that case, the sessionId and datasetName must be recorded
     * in the table that tracks the queries.
     *
     * NOTE: We could omit the table_name column completely and simply 
     *        adopt a naming convention based on the primary key value.
     */
    protected String insertQueryInstance(QueryInstance instance,
					 String sessionId, String datasetName) throws SQLException {

	// format result table name
	String resultTableName = null;
	String nextID = platform.getNextId(cacheSchemaName, cacheTableName);
	if (nextID == null) nextID = "1"; 
	resultTableName = "query_result_" + nextID;

	// format values
	String queryName = "'" + instance.getQuery().getName() + "'";
	sessionId = (sessionId != null)? ("'" + sessionId + "'") : "null";
	datasetName = (datasetName != null)? ("'" +datasetName + "'") : "null";
	String pVals = formatInstanceParamVals(instance);

	// format insert statement
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("insert into " + cacheFullName +
		    " (query_id, query_name, session_id, dataset_name");
	Collection instanceValues = instance.getValues();
	for (int i = 0;i < instanceValues.size();++i) {
	    sqlb.append(", param" + i);
	}
	sqlb.append(", result_table, start_time) values (");
	sqlb.append(nextID + ", " + queryName + ", " + sessionId + ", " + 
		    datasetName + ", " + pVals +  
		    "'" + resultTableName + "', sysdate)"); 

	int numRows = SqlUtils.executeUpdate(dataSource,sqlb.toString());

	// This may happen because a parameter value is too large
	// to fit in the corresponding column of the Queries table.
	//
	if (numRows != 1) {
	    String err = "insert failed: '" + sqlb.toString() + "'";
	}

	return platform.getFullTableName(cacheSchemaName, resultTableName);
    }

    protected String formatInstanceParamVals(QueryInstance instance) {
	StringBuffer sb = new StringBuffer();

	Iterator paramValues = instance.getValues().iterator();
	while (paramValues.hasNext()) {
	    String val = (String)paramValues.next();
	    String cleaned = platform.cleanStringValue(val);
	    sb.append("'" + cleaned + "', ");
	}
	return sb.toString();
    }

    /**
     * Record in the cache that a previously inserted
     * query instance has run, finished and
     * written result to a result table.  Updates the appropriate row in the
     * cache.
     * 
     * @return Whether the operation succeeded.
     */
    protected boolean finishQueryInstance(QueryInstance instance) throws SQLException {
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("update " + cacheFullName 
		    + " set end_time = " + platform.getCurrentDateFunction());
	sqlb.append(" where ");
	sqlb.append(instanceWhereClause(instance));
	//	System.err.println("WebDb: Running + '" + sqlb.toString() + "'");
	return (SqlUtils.executeUpdate(dataSource, sqlb.toString()) == 1);
    }

    /**
     * Create a "where" clause ("where" and/or "and" not included) that selects
     * from the cache table the row corresponding to a particular query 
     * instance (if any).
     */
    protected String instanceWhereClause(QueryInstance instance) {
	StringBuffer sb = new StringBuffer();
	Iterator iter = instance.getValues().iterator();

	sb.append(" query_name = '" + instance.getQuery().getName() + "'");

	int i = 0;
	while (iter.hasNext()) {
	    String val = (String)iter.next();
	    String cleaned = platform.cleanStringValue(val);
	    sb.append(" and param" + i++ + " = '");
	    sb.append(cleaned);
	    sb.append("'");
	}
	return sb.toString();
    }

    /**
     * @return true if dataset name is in use for this session id
     */
    protected boolean checkIfDatasetNameInUse(String sessionId, 
					   String datasetName) throws SQLException {
	StringBuffer s = new StringBuffer();
	s.append("select result_table from " + cacheFullName + " where " +
		 "dataset_name = '" + datasetName + "' and " +
		 "session_id = '" + sessionId + "'");

	String tables[] = SqlUtils.runStringArrayQuery(dataSource, s.toString());
	return tables.length != 0;
    }

    protected ResultSet fetchCachedResult(String resultTable) throws SQLException {
	String sql = "select * from " + resultTable;
	return SqlUtils.getResultSet(dataSource, sql);
    }

    //////////////////////////////////////////////////////////////////////
    ///// Static methods
    //////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

	File modelConfigXmlFile = new File(args[0]);

	String operation = args[1];

	try {
	    // read config info
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    Integer maxQueryParams = modelConfig.getMaxQueryParams();
	    String cacheTable = modelConfig.getQueryCacheTable();
	    String platformClass = modelConfig.getPlatformClass();
	    
	    DataSource dataSource = 
		setupDataSource(connectionUrl, login, password);

	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.setDataSource(dataSource);

	    SqlResultFactory factory = 
		new SqlResultFactory(dataSource, platform, login, cacheTable);

	    if (operation.equals("--new")) {
		factory.createCache(maxQueryParams.intValue());	
	    } else if (operation.equals("--reset")) {
		factory.resetCache();
	    } else if (operation.equals("--drop")) {
		factory.dropCache();
	    } else {
		System.err.println("Must provide '--new', '--reset' or '--drop' as second argument");
		System.err.println("");
	    }

	} catch (Exception e) {
	    System.err.println("FAILED");
	    System.err.println("");
	    e.printStackTrace();
	    System.exit(1);
        } 
    }

    static DataSource setupDataSource(String connectURI, String login, 
				      String password)  {

	//	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool connectionPool = new GenericObjectPool(null);

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, login, password);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
    }
}
