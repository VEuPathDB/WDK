package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class SqlResultFactory {

    DataSource dataSource;
    RDBMSPlatformI platform;
    String schemaName;
    String instanceTableName;
    String instanceTableFullName;

    public SqlResultFactory(DataSource dataSource, RDBMSPlatformI platform,
			    String schemaName, String instanceTableName) {
	this.dataSource = dataSource;
	this.platform = platform;
	this.schemaName = schemaName;
	this.instanceTableName = instanceTableName;
	this.instanceTableFullName = platform.getTableFullName(schemaName, 
						       instanceTableName);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////   public  /////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public ResultSet getResult(SimpleSqlQueryInstance instance) throws SQLException {
	return instance.getIsCacheable()?
	    getCachedResult(instance) : getUncachedResult(instance);
    }

    /**
     * @return Full name of table containing result
     */
    public String getResultAsTable(SimpleSqlQueryInstance instance) throws SQLException {
	return instance.getIsCacheable()?
	    getCachedResultTable(instance) : getNewResultTable(instance);
    }

    /**
     * @param numParams Number of parameters allowed in a cached query
     */
    public void createCache(int numParams) throws SQLException {
	String newline = System.getProperty( "line.separator" );

	// Format sql to create table
	StringBuffer sqlb = new StringBuffer();
	String tblName = schemaName + "." + instanceTableName;
	sqlb.append("create table " + tblName + 
		    " (query_id number(12) not null, query_name varchar2(100) not null, cached number(1) not null,");
	
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
	s.append("select result_table from " + instanceTableFullName);

	String tables[] = 
	    SqlUtils.runStringArrayQuery(dataSource, s.toString());
	int nTables = tables.length;
	int nDropped = 0;

	System.err.println("Attempting to drop " + nTables + " results tables");
	for (int i =0;i < nTables;++i) {
	    try {
		platform.dropTable(schemaName, tables[i]);
		nDropped++;
	    } catch (SQLException e) {}
	}

	System.err.println("Succeeded in dropping " + nDropped);
	System.err.println("Deleting all rows from " + instanceTableFullName);

	SqlUtils.execute(dataSource, "delete from " + instanceTableFullName);
    }

    /**
     * Drop all tables and sequences associated with the cache
     */    
    public void dropCache() throws SQLException {
	resetCache();
	System.err.println("Dropping table " + instanceTableFullName);
	platform.dropTable(schemaName, instanceTableName);
	System.err.println("Dropping sequence " + instanceTableFullName + "_pkseq");
	platform.dropSequence(instanceTableFullName + "_pkseq");
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////   protected   /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    protected ResultSet getUncachedResult(SimpleSqlQueryInstance instance) throws SQLException {
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

    /**
     * @return Full table name of the result table
     */
    protected String getNewResultTable(SimpleSqlQueryInstance instance) throws SQLException {
	String resultTable = insertQueryInstance(instance);
	platform.createTableFromQuerySql(dataSource, resultTable, 
					 instance.getSql());
	finishQueryInstance(instance);
	return resultTable;
    }

    protected ResultSet getCachedResult(SimpleSqlQueryInstance instance) throws SQLException {
	String resultTable = getCachedResultTable(instance);	
	return fetchCachedResult(resultTable);
    }

    /**
     * @return The full name of the database table that contains a 
     * cached query result.  Returns null if no cached query
     * result is stored in the database (either because it was
     * never there or it has been expired.) 
     *
     * @param instance  The instance of the query
     */
    protected String getCachedResultTable(SimpleSqlQueryInstance instance)  throws SQLException {
	String resultTableFullName;

	// Construct SQL query to retrieve the requested table's name
	//
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("select result_table from " + instanceTableFullName
		    + " where cached = 1 and ");
	sqlb.append(instanceWhereClause(instance));
	sqlb.append(" and end_time IS NOT NULL"); 
	String resultTableName = SqlUtils.runStringQuery(dataSource, sqlb.toString());
	if (resultTableName == null) {
	    resultTableFullName = getNewResultTable(instance);
	} else {
	    resultTableFullName = 
		platform.getTableFullName(schemaName,resultTableName);    
	}

	return resultTableFullName;
    }

    /**
     * Record in the database that a query has been started (by 
     * entering an appropriate row into the Queries table.)  Returns 
     * the (automatically generated) name of a table to which the query 
     * results should be written.
     *
     * @return Full table name of result table
     */
    protected String insertQueryInstance(SimpleSqlQueryInstance instance) throws SQLException {
	return insertQueryInstance(instance, null, null);
    }

    /**
     * This variant of the method is called when the query has a dataset
     * param.  In that case, the sessionId and datasetName must be recorded
     * in the table that tracks the queries.
     *
     * NOTE: We could omit the table_name column completely and simply 
     *        adopt a naming convention based on the primary key value.
     *
     * @return Full table name of result table
     */
    protected String insertQueryInstance(SimpleSqlQueryInstance instance, 
					 String sessionId, String datasetName) throws SQLException {

	// format result table name
	String resultTableName = null;
	String nextID = platform.getNextId(schemaName, instanceTableName);
	if (nextID == null) nextID = "1"; 
	resultTableName = "query_result_" + nextID;

	// format values
	String queryName = "'" + instance.getQuery().getName() + "'";
	sessionId = (sessionId != null)? ("'" + sessionId + "'") : "null";
	datasetName = (datasetName != null)? ("'" +datasetName + "'") : "null";
	String pVals = formatInstanceParamVals(instance);
	int cached = instance.getIsCacheable()? 1 : 0;

	// format insert statement
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("insert into " + instanceTableFullName +
		    " (query_id, query_name, cached, session_id, dataset_name");
	Collection instanceValues = instance.getValues();
	for (int i = 0;i < instanceValues.size();++i) {
	    sqlb.append(", param" + i);
	}
	sqlb.append(", result_table, start_time) values (");
	sqlb.append(nextID + ", " + queryName + ", " + cached + ", " 
		    + sessionId + ", " + 
		    datasetName + ", " + pVals +  
		    "'" + resultTableName + "', sysdate)"); 

	int numRows = SqlUtils.executeUpdate(dataSource,sqlb.toString());

	// This may happen because a parameter value is too large
	// to fit in the corresponding column of the Queries table.
	//
	if (numRows != 1) {
	    String err = "insert failed: '" + sqlb.toString() + "'";
	}

	return platform.getTableFullName(schemaName, resultTableName);
    }

    protected String formatInstanceParamVals(SimpleSqlQueryInstance instance) {
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
    protected boolean finishQueryInstance(SimpleSqlQueryInstance instance) throws SQLException {
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("update " + instanceTableFullName 
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
    protected String instanceWhereClause(SimpleSqlQueryInstance instance) {
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
	s.append("select result_table from " + instanceTableFullName + " where " +
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

	String cmdName = System.getProperties().getProperty("cmdName");

	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);

	File modelConfigXmlFile = 
	    new File(cmdLine.getOptionValue("configFile"));
	boolean newCache = cmdLine.hasOption("new");
	boolean resetCache = cmdLine.hasOption("reset");
	boolean dropCache = cmdLine.hasOption("drop");

	try {
	    // read config info
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    Integer maxQueryParams = modelConfig.getMaxQueryParams();
	    String instanceTable = modelConfig.getQueryInstanceTable();
	    String platformClass = modelConfig.getPlatformClass();
	    
	    DataSource dataSource = 
		setupDataSource(connectionUrl, login, password);

	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.setDataSource(dataSource);

	    SqlResultFactory factory = 
		new SqlResultFactory(dataSource, platform, login, instanceTable);

	    if (newCache) factory.createCache(maxQueryParams.intValue());
	    else if (resetCache) factory.resetCache();
	    else if (dropCache) factory.dropCache();

	} catch (Exception e) {
	    System.err.println("FAILED");
	    System.err.println("");
	    e.printStackTrace();
	    System.exit(1);
        } 
    }

    static Options declareOptions() {
	Options options = new Options();

	// config file
	Option configFile = OptionBuilder
	    .withArgName("configFile")
	    .hasArg()
	    .withDescription("an .xml file that specifies a ModelConfig object")
	    .isRequired()
	    .create("configFile");
	options.addOption(configFile);

	// operation
	Option newQ = new Option("new", "create a new query cache");

	Option resetQ = new Option("reset","reset the query cache");

	Option dropQ = new Option("drop", "drop the query cache");

	OptionGroup operation = new OptionGroup();
	operation.setRequired(true);
	operation.addOption(newQ);
	operation.addOption(resetQ);
	operation.addOption(dropQ);
	options.addOptionGroup(operation);

	return options;
    }

    static CommandLine parseOptions(String cmdName, Options options, 
				    String[] args) {

	CommandLineParser parser = new BasicParser();
	CommandLine cmdLine = null;
	try {
	    // parse the command line arguments
	    cmdLine = parser.parse( options, args );
	}
	catch( ParseException exp ) {
	    // oops, something went wrong
	    System.err.println("");
	    System.err.println( "Parsing failed.  Reason: " + exp.getMessage() ); 
	    System.err.println("");
	    usage(cmdName, options);
	}

	return cmdLine;
    }

    static void usage(String cmdName, Options options) {

	String newline = System.getProperty( "line.separator" );
	String cmdlineSyntax = 
	    cmdName + 
	    " -configFile config_file" +
	    " -new|-reset|-drop";

	String header = 
	    newline + "Create, reset or drop a query cache. The name of the cache table is found in the configFile (the table is placed in the schema owned by login).  Resetting the cache drops all results tables and deletes all rows from the cache table.  Dropping the cache first resets it then drops the cache table and sequence." + newline + newline + "Options:" ;

	String footer = "";

	//	PrintWriter stderr = new PrintWriter(System.err);
	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
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
