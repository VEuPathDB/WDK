package org.gusdb.wdk.model;


import org.gusdb.wdk.model.implementation.SqlResultList;
import org.gusdb.wdk.model.implementation.SqlUtils;
import org.gusdb.wdk.model.Column;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
   How the QueryInstance table works.

   A QueryInstance is "persistent" (ie, a row is added, and a result table is 
   created), if any of these applies (in priority order):

     - the query is cacheable
          - the "cached" bit is set
          - dispose of row/resultTable when whole cache is dropped (new data)

     - the query will be in the history (even if not cacheable, to provide a 
       stable history, ie, results don't change even if the db did)
          - dispose of row/resultTable when user's history is purged

     - the query is being used in a boolean operation
          - this depends on the query being in the history

   A row in QueryInstance is, by definition, persistent.  If its "cacheable"
   flag is on, its cacheable.  If not, it is still available to the history.

   We can think of "cacheability" as "shared for all users."  
 */


public class ResultFactory {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.ResultFactory");
    
    RDBMSPlatformI platform;
    String schemaName;
    String instanceTableName;
    String instanceTableFullName;

    public ResultFactory(RDBMSPlatformI platform,
			 String schemaName, String instanceTableName) {
	this.platform = platform;
	this.schemaName = schemaName;
	this.instanceTableName = instanceTableName;
	this.instanceTableFullName = platform.getTableFullName(schemaName, 
						       instanceTableName);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////   public  /////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public synchronized ResultList getResult(QueryInstance instance) throws WdkModelException{
	ResultList resultList = instance.getIsPersistent()?
	    getPersistentResult(instance) : instance.getNonpersistentResult();
	return resultList;
    }
    
    /**
     * @return Full name of table containing result
     */
    public synchronized String getResultAsTable(QueryInstance instance) throws WdkModelException {
	return getResultTable(instance);
    }

    public synchronized String getSqlForCache(QueryInstance instance) throws WdkModelException {
	Column columns[] = instance.getQuery().getColumns();
	StringBuffer sqlb = new StringBuffer("select ");
	
	int columnLength = columns.length;

	for (int i = 0; i < columnLength - 1; i++){
	    Column nextColumn = columns[i];
	    sqlb.append(nextColumn.getName() + ", ");
	}
	sqlb.append(columns[columnLength - 1].getName() + " ");
	    
	String resultTable = getResultTable(instance); //ensures instance is inserted into cache
	sqlb.append("from " + resultTable);

	return sqlb.toString();
    }

    /**
     * @param numParams Number of parameters allowed in a cached query
     */
    public void createCache(int numParams, boolean noSchemaOutput) throws WdkModelException {
	String newline = System.getProperty( "line.separator" );
	
	String nameToUse = (noSchemaOutput == true ? instanceTableName : instanceTableFullName);

	// Format sql to create table
	StringBuffer sqlb = new StringBuffer();
	String tblName = schemaName + "." + instanceTableName;
	
	sqlb.append("create table " + tblName + 
		    " (query_instance_id number(12) not null, query_name varchar2(100) not null, cached number(1) not null,");
	
	sqlb.append("result_table varchar2(30), start_time date not null, end_time date, dataset_name varchar2(100), session_id varchar2(50), ");
	for (int i=0;i < numParams -1; i++) {
	    sqlb.append("param" + i + " varchar2(200), ");
	}
	sqlb.append("param" + numParams + " varchar2(200))");
	
	// Execute it
	System.out.println(newline + "Making cache table " + nameToUse + newline);
	
	logger.fine("Using sql: " + sqlb.toString());
	try {
	    SqlUtils.execute(platform.getDataSource(), sqlb.toString());
	    logger.fine("Done" + newline);
	    
	    // Create sequence 
	    platform.createSequence(tblName + "_pkseq", 1, 1);
	    System.out.println("Creating sequence " + nameToUse + "_pkseq"
			       + newline);
	    System.out.println("Done" + newline);
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
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
     */    
    public synchronized void resetCache(boolean noSchemaOutput) throws WdkModelException {

	// Query for the names of all cached result tables
	//
	String nameToUse = (noSchemaOutput == true ? instanceTableName : instanceTableFullName);
	StringBuffer s = new StringBuffer();
	s.append("select result_table from " + instanceTableFullName);
	String tables[] = null; 
	try {
	    tables = SqlUtils.runStringArrayQuery(platform.getDataSource(), s.toString());
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
	int nTables = tables.length;
	int nDropped = 0;

	System.out.println("Attempting to drop " + nTables + " results tables");
	for (int i =0;i < nTables;++i) {
	    try {
		platform.dropTable(schemaName, tables[i]);
		nDropped++;
	    } catch (SQLException e) {}
	}

	System.out.println("Succeeded in dropping " + nDropped);
	System.out.println("Deleting all rows from " + nameToUse);

	try {
	    SqlUtils.execute(platform.getDataSource(), "delete from " + instanceTableFullName);
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }

    /**
     * Drop all tables and sequences associated with the cache
     */    
    public synchronized void dropCache(boolean noSchemaOutput) throws WdkModelException {
	try {
	    resetCache(noSchemaOutput);
	    String nameToUse = (noSchemaOutput == true ? instanceTableName : instanceTableFullName);
	    System.out.println("Dropping table " + nameToUse);
	    platform.dropTable(schemaName, instanceTableName);
	    System.out.println("Dropping sequence " + nameToUse + "_pkseq");
	    platform.dropSequence(instanceTableFullName + "_pkseq");
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////   protected   /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    RDBMSPlatformI getRDBMSPlatform() {
	return platform;
    }

    /**
     * @return Full table name of the result table
     */
    private String getNewResultTable(QueryInstance instance) throws WdkModelException {
	
	//populates cache if not already in there

	// add row to QueryInstance table
	Integer queryInstanceId = getQueryInstanceId(instance);
	if (queryInstanceId == null){
	    queryInstanceId = insertQueryInstance(instance);
	}	    
	
	String resultTableName = "query_result_" + queryInstanceId;
	StringBuffer sql = new StringBuffer();
	sql.append("update " + instanceTableFullName + " set result_table = '" + resultTableName + "'");
	sql.append(" where query_instance_id = " + queryInstanceId.toString());
	int numRows = 0;
	try {
	    numRows = SqlUtils.executeUpdate(platform.getDataSource(), sql.toString());
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
	// write result into result table
	instance.writeResultToTable(resultTableName, this);
	// update row in QueryInstance table with final timestamp
	finishQueryInstance(instance);

	return resultTableName;
    }

    private ResultList getPersistentResult(QueryInstance instance) throws WdkModelException {
	String resultTable = getResultTable(instance);

	ResultSet rs = fetchCachedResult(resultTable);
	return new SqlResultList(instance, resultTable, rs);
    }

    /**
     * @return The full name of the database table that contains a 
     * cached query result.  Returns null if no cached query
     * result is stored in the database (either because it was
     * never there or it has been expired.) 
     *
     * @param instance  The instance of the query
     */
    private String getResultTable(QueryInstance instance)  throws WdkModelException {
	String resultTableFullName;

	// Construct SQL query to retrieve the requested table's name
	//
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("select result_table from " + instanceTableFullName + " where ");
	
	if (instance.getIsCacheable()){
	    sqlb.append("cached = 1 and end_time IS NOT NULL and "); 
	}
	sqlb.append(instanceWhereClause(instance));

	String resultTableName = null;
	try {
	    resultTableName = SqlUtils.runStringQuery(platform.getDataSource(), sqlb.toString());
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}

	if (resultTableName == null) {
	    resultTableFullName = getNewResultTable(instance);
	} else {
	    if (instance.getQueryInstanceId() == null){ //instance result is in cache but is newly created object
		instance.setQueryInstanceId(getQueryInstanceId(instance));
	    }
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
    private Integer insertQueryInstance(QueryInstance instance) throws WdkModelException {
	return insertQueryInstance(instance, null, null);
    }

    /**
     * Documentation needs update: but note this does not restrict on whether the
     * instance is cacheable.  Sets the ID in the QueryInstance by side-effect.
    
     * This variant of the method is called when the query has a dataset
     * param.  In that case, the sessionId and datasetName must be recorded
     * in the table that tracks the queries.
     *
     * NOTE: We could omit the table_name column completely and simply 
     *        adopt a naming convention based on the primary key value.
     *
     * @return Full table name of result table
     */
    private Integer insertQueryInstance(QueryInstance instance, 
					 String sessionId, String datasetName) throws WdkModelException {

	String nextID = null;
	try {
	    nextID = platform.getNextId(schemaName, instanceTableName);
	    
	} catch (SQLException e) {
	    logger.finest("Got an SQLException");
	    throw new WdkModelException(e);
	}
	if (nextID == null) {
	    nextID = "1"; 
	}

	// format values
	String queryName = "'" + instance.getQuery().getFullName() + "'";
	sessionId = (sessionId != null)? ("'" + sessionId + "'") : "null";
	datasetName = (datasetName != null)? ("'" +datasetName + "'") : "null";
	String pVals = formatInstanceParamVals(instance);
	int cached = instance.getIsCacheable()? 1 : 0;

	// format insert statement
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("insert into " + instanceTableFullName +
		    " (query_instance_id, query_name, cached, session_id, dataset_name, start_time");
	Collection instanceValues = instance.getCacheValues();
	for (int i = 0;i < instanceValues.size();++i) {
	    sqlb.append(", param" + i);
	}
	sqlb.append(") values (");
	sqlb.append(nextID + ", " + queryName + ", " + cached + ", " 
		    + sessionId + ", " + 
		    datasetName + ", sysdate" + pVals+ ")"); 
	
	int numRows = 0;

	try {
	    numRows = SqlUtils.executeUpdate(platform.getDataSource(),sqlb.toString());
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}

	// This may happen because a parameter value is too large
	// to fit in the corresponding column of the Queries table.
	//
	if (numRows != 1) {
	    String err = "insert failed: '" + sqlb.toString() + "'";
	}
	Integer finalId = new Integer(nextID);
	instance.setQueryInstanceId(finalId);
	return finalId;
    }

    private String formatInstanceParamVals(QueryInstance instance) throws WdkModelException {
	StringBuffer sb = new StringBuffer();
	
	int count = 0;
	Iterator paramValues = instance.getCacheValues().iterator();
	while (paramValues.hasNext()) {
	    String val = (String)paramValues.next();
	    String cleaned = platform.cleanStringValue(val);

        //if (count != 0) {
            sb.append(", ");
        //}
	    sb.append("'" + cleaned + "'");
        count++;
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
    private boolean finishQueryInstance(QueryInstance instance) throws WdkModelException {
	StringBuffer sqlb = new StringBuffer();
	sqlb.append("update " + instanceTableFullName 
		    + " set end_time = " + platform.getCurrentDateFunction());
	sqlb.append(" where ");
	sqlb.append(instanceWhereClause(instance));
	boolean ok = false;
	try {
	    ok = SqlUtils.executeUpdate(platform.getDataSource(), sqlb.toString()) == 1;
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
	return ok;
    }

    /**
     * Create a "where" clause ("where" and/or "and" not included) that selects
     * from the cache table the row corresponding to a particular query 
     * instance (if any).
     */
    private String instanceWhereClause(QueryInstance instance) throws WdkModelException{
	StringBuffer sb = new StringBuffer();
	Iterator iter = instance.getCacheValues().iterator();

	sb.append(" query_name = '" + instance.getQuery().getFullName() + "'");

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
    private boolean checkIfDatasetNameInUse(String sessionId, 
					   String datasetName) throws WdkModelException {
	StringBuffer s = new StringBuffer();
	s.append("select result_table from " + instanceTableFullName + " where " +
		 "dataset_name = '" + datasetName + "' and " +
		 "session_id = '" + sessionId + "'");

	String tables[] = null;
	try {
	    tables = SqlUtils.runStringArrayQuery(platform.getDataSource(), s.toString());
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
	return tables.length != 0;
    }

    private ResultSet fetchCachedResult(String resultTable) throws WdkModelException {
	
	String sql = "select * from " + resultTable;

 	ResultSet rs = null;
	try {
	    rs = SqlUtils.getResultSet(platform.getDataSource(), sql);
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
	return rs;
    }

    private Integer getQueryInstanceId(QueryInstance instance) throws WdkModelException{
	
	Integer queryInstanceId = instance.getQueryInstanceId();
	if (queryInstanceId == null){
	    
	    StringBuffer sqlb = new StringBuffer();
	    sqlb.append("select query_instance_id from " + instanceTableFullName + " where "); 
	    sqlb.append(instanceWhereClause(instance));
		    
	    try {
	        queryInstanceId = SqlUtils.runIntegerQuery(platform.getDataSource(), sqlb.toString());
	    } catch (SQLException e) {
		throw new WdkModelException(e);
	    }
	}
	return queryInstanceId;
    }

    //////////////////////////////////////////////////////////////////////
    ///// Static methods
    //////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

	String cmdName = System.getProperties().getProperty("cmdName");
        File configDir = 
	    new File(System.getProperties().getProperty("configDir"));

	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);

	String modelName = cmdLine.getOptionValue("model");

        File modelConfigXmlFile = new File(configDir, modelName+"-config.xml");
	boolean newCache = cmdLine.hasOption("new");
	boolean resetCache = cmdLine.hasOption("reset");
	boolean dropCache = cmdLine.hasOption("drop");
	boolean noSchemaOutput = cmdLine.hasOption("noSchemaOutput");

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
	    
	    Integer maxIdle = modelConfig.getMaxIdle();
	    Integer minIdle = modelConfig.getMinIdle();
	    Integer maxWait = modelConfig.getMaxWait();
	    Integer maxActive = modelConfig.getMaxActive();
	    Integer initialSize = modelConfig.getInitialSize();

	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.init(connectionUrl, login, password, minIdle, maxIdle, maxWait, maxActive, initialSize, modelConfigXmlFile.getAbsolutePath());

	    ResultFactory factory =
		new ResultFactory(platform, login, instanceTable);

	    if (newCache) factory.createCache(maxQueryParams.intValue(), noSchemaOutput);
	    else if (resetCache) factory.resetCache(noSchemaOutput);
	    else if (dropCache) factory.dropCache(noSchemaOutput);

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
	addOption(options, "model", "the name of the model.  This is used to find the Model config file ($GUS_HOME/config/model_name-config.xml)");

	// operation
	Option newQ = new Option("new", "create a new query cache");

	Option resetQ = new Option("reset","reset the query cache");

	Option dropQ = new Option("drop", "drop the query cache");

	Option noSchema = new Option("noSchemaOutput", "remove references to the schema when printing out messages regarding a table");

	OptionGroup operation = new OptionGroup();
	operation.setRequired(true);
	operation.addOption(newQ);
 	operation.addOption(resetQ);
	operation.addOption(dropQ);
	options.addOption(noSchema);
	options.addOptionGroup(operation);

	return options;
    }

    private static void addOption(Options options, String argName, String desc) {
        
        Option option = new Option(argName, true, desc);
        option.setRequired(true);
        option.setArgName(argName);
        
        options.addOption(option);
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
	    " -model model_name -new|-reset|-drop [-noSchemaOutput]";

	String header = 
	    newline + "Create, reset or drop a query cache. The name of the cache table is found in the Model config file (the table is placed in the schema owned by login).  Resetting the cache drops all results tables and deletes all rows from the cache table.  Dropping the cache first resets it then drops the cache table and sequence." + newline + newline + "Options:" ;

	String footer = "";

	//	PrintWriter stderr = new PrintWriter(System.err);
	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
    }
}
