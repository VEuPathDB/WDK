package org.gusdb.gus.wdk.model.test;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import javax.sql.DataSource;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.QueryI;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.SimpleQueryI;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;
import org.gusdb.gus.wdk.model.PageableQuerySet;
import org.gusdb.gus.wdk.model.PageableQueryI;
import org.gusdb.gus.wdk.model.PageableQueryInstanceI;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.implementation.QuerySetParser;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;


import org.gusdb.gus.wdk.model.SqlEnumParam;
import org.gusdb.gus.wdk.model.implementation.SqlUtils;

import org.gusdb.gus.wdk.model.implementation.SimpleSqlQueryInstance;

//import org.gusdb.gus.wdk.model.;
//import org.gusdb.gus.wdk.model.;

public class QueryTester {

    WdkModel wdkModel;
    ResultFactory resultFactory;

    public QueryTester(WdkModel wdkModel, 
		       ResultFactory resultFactory) {
	this.wdkModel = wdkModel;
	this.resultFactory = resultFactory;
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   public methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public ResultSet getResult(String querySetName, String queryName, 
			       Hashtable paramHash, 
			       boolean useCache) throws Exception, QueryParamsException {
	SimpleQuerySet simpleQuerySet 
	    = wdkModel.getSimpleQuerySet(querySetName);
	SimpleQueryI query = simpleQuerySet.getQuery(queryName);
	SimpleQueryInstanceI instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return instance.getResult();
    }

    public ResultSet getResultPage(String querySetName, String queryName, 
				   int startRow, int endRow,
				   Hashtable paramHash, 
				   boolean useCache) throws Exception, QueryParamsException {
	PageableQuerySet pageableQuerySet 
	    = wdkModel.getPageableQuerySet(querySetName);
	PageableQueryI query = pageableQuerySet.getQuery(queryName);
	PageableQueryInstanceI instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return instance.getResult(startRow, endRow);
    }

    public String getResultAsTable(String querySetName, String queryName, Hashtable paramHash, boolean useCache) throws Exception, QueryParamsException {
	SimpleQuerySet simpleQuerySet 
	    = wdkModel.getSimpleQuerySet(querySetName);
	SimpleQueryI query = simpleQuerySet.getQuery(queryName);
	SimpleQueryInstanceI instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return ((SimpleSqlQueryInstance)instance).getResultAsTable();
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   protected methods   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    void displayQuery(QueryI query) throws Exception {
	String newline = System.getProperty( "line.separator" );
	System.out.println(newline + "Query: " + 
			   query.getDisplayName() + newline);

	System.out.println("Parameters");

	Param[] params = query.getParams();

	for (int i=0; i<params.length; i++) {
	    System.out.println(formatParamPrompt(params[i]));
	}
	System.out.println("");
    }

    Hashtable parseParamArgs(String[] params) {
	Hashtable h = new Hashtable();
	if (params.length % 2 != 0) {
	    throw new IllegalArgumentException("The -params option must be followed by key value pairs only");
	}
	for (int i=0; i<params.length; i+=2) {
	    h.put(params[i], params[i+1]);
	}
	return h;
    }

    void printResultSet(ResultSet rs) throws SQLException{
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

    String formatParamPrompt(Param param) throws Exception {

	String newline = System.getProperty( "line.separator" );

	String prompt = "  " + param.getPrompt();

	if (param instanceof SqlEnumParam) {
	    SqlEnumParam enumParam = (SqlEnumParam)param;
	    prompt += " (chose one";
	    if (enumParam.getMultiPick().booleanValue()) prompt += " or more"; 
	    prompt += "):";
	    Hashtable hash = enumParam.getKeysAndValues(resultFactory);
	    Enumeration keys = hash.keys();
	    while (keys.hasMoreElements()) {
		String key = (String)keys.nextElement();
		prompt += newline + "    " + key + " = " + hash.get(key);
	    }
	} 

	else if (param instanceof StringParam) {
	    StringParam stringParam = (StringParam)param;
	    if (stringParam.getSample() != null)
		prompt += " (" + stringParam.getSample() + ")";
	    prompt += ":";
	} 

	else {
	    prompt = param.getPrompt() + ":";
	}

	return prompt;
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
	
	String cmdName = System.getProperties().getProperty("cmdName");

	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);

	File modelConfigXmlFile = 
	    new File(cmdLine.getOptionValue("configFile"));
	File querySetFile = new File(cmdLine.getOptionValue("querySetFile"));

	String querySetName = cmdLine.getOptionValue("querySetName");
	String queryName = cmdLine.getOptionValue("queryName");
	boolean useCache = !cmdLine.hasOption("dontCache");
	boolean returnResultAsTable = cmdLine.hasOption("returnTable");
	boolean haveParams = cmdLine.hasOption("params");
	boolean paging = cmdLine.hasOption("rows");
	String[] params = null;
	if (haveParams) params = cmdLine.getOptionValues("params");
	String[] rows = null;
	if (paging) rows = cmdLine.getOptionValues("rows");

	try {
	    // read config info
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    String instanceTable = modelConfig.getQueryInstanceTable();
	    String platformClass = modelConfig.getPlatformClass();
	    
	    DataSource dataSource = 
		setupDataSource(connectionUrl,login, password);
	
	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.setDataSource(dataSource);
       
	    WdkModel wdkModel = 
		QuerySetParser.parseXmlFile(querySetFile);
	    ResultFactory resultFactory = wdkModel.getResultFactory();
	    SqlResultFactory sqlResultFactory = 
		new SqlResultFactory(dataSource, platform, 
				     login, instanceTable);
	    resultFactory.setSqlResultFactory(sqlResultFactory);
	    QueryTester tester = new QueryTester(wdkModel, resultFactory);

	    // if no params supplied, show the query prompts
	    if (!haveParams) {
		QueryI query;
		if (paging) {
		    query = 
			wdkModel.getPageableQuerySet(querySetName).
			getQuery(queryName);
		} else {
		    query = 
			wdkModel.getSimpleQuerySet(querySetName).
			getQuery(queryName);
		}
		tester.displayQuery(query);
	    } 

	    // else, run the query with the supplied params
	    else {
		Hashtable paramHash = tester.parseParamArgs(params);
		if (returnResultAsTable) {
		    String table = tester.getResultAsTable(querySetName, 
							   queryName, 
							   paramHash,
							   useCache);
		    System.out.println(table);
		} else if (paging) {
		    ResultSet rs = tester.getResultPage(querySetName, 
							queryName, 
							Integer.parseInt(rows[0]),
							Integer.parseInt(rows[1]),
							paramHash,
							useCache);
		    tester.printResultSet(rs);
		} else {
		    ResultSet rs = tester.getResult(querySetName, 
						    queryName, paramHash,
						    useCache);
		    tester.printResultSet(rs);
		}
	    }
	} catch (QueryParamsException e) {
	    System.err.println(e.formatErrors());
	    System.exit(1);
	} catch (Exception e) {
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
	    .withDescription("An .xml file that specifies a ModelConfig object.")
	    .isRequired()
	    .create("configFile");
	options.addOption(configFile);

	// query set file
	Option querySetFile = OptionBuilder
	    .withArgName("querySetFile")
	    .hasArg()
	    .withDescription("An .xml file that specifies a container of Query set objects.")
	    .isRequired()
	    .create("querySetFile");
	options.addOption(querySetFile);

	// query set name
	Option querySetName = OptionBuilder
	    .withArgName("querySetName")
	    .hasArg()
	    .withDescription("The name of the query set in which to find the query.")
	    .isRequired()
	    .create("querySetName");
	options.addOption(querySetName);

	// query name
	Option queryName = OptionBuilder
	    .withArgName("queryName")
	    .hasArg()
	    .withDescription("The name of the query to run.")
	    .isRequired()
	    .create("queryName");
	options.addOption(queryName);

	// use cache
	Option useCache = new Option("dontCache","Do not use the cache for this query (even if it is cache enabled).");
	options.addOption(useCache);

	OptionGroup specialOperations = new OptionGroup();

	// return table
	Option returnTable = new Option("returnTable", "Place the result in a table and return the name of the table.");
	specialOperations.addOption(returnTable);

	// return result size
	Option returnSize = new Option("returnSize", "For pageable queries only: return the total size of the result.");
	specialOperations.addOption(returnSize);

	// rows to return
	Option rows = new Option("rows", "For pageable queries only: provide the start and end rows to return.");
	rows.setArgs(2);
	specialOperations.addOption(rows);

	options.addOptionGroup(specialOperations);

	// params
	Option params = OptionBuilder
	    .withArgName("params")
	    .hasArgs()
	    .withDescription("Comma delimited list of param_name,param_value,....")
	    .create("params");
	options.addOption(params);


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
	    " -querySetFile query_set_file" +
	    " -querySetName query_set_name" +
	    " -queryName query_name" +
	    " [-dontCache]" +
	    " [-returnTable | -returnSize | -rows start end]" +
	    " [-params param_1_name,param_1_value,...]";

	String header = 
	    newline + "Run a query found in a query set xml file.  If run without -params, displays the parameters for the specified query" + newline + newline + "Options:" ;

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
    
