package org.gusdb.gus.wdk.model.test;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.SqlEnumParam;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;
import org.gusdb.gus.wdk.model.implementation.SqlQueryInstance;
import org.gusdb.gus.wdk.model.implementation.SqlQuery;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;
import org.gusdb.gus.wdk.model.implementation.SqlUtils;
import org.gusdb.gus.wdk.model.QueryNameList;
import org.gusdb.gus.wdk.model.QueryName;

import java.io.File;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;


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

    public ResultList getResult(String querySetName, String queryName, 
			       Hashtable paramHash, 
			       boolean useCache) throws Exception, QueryParamsException {
	QuerySet querySet 
	    = wdkModel.getQuerySet(querySetName);
	Query query = querySet.getQuery(queryName);
	QueryInstance instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return instance.getResult();
    }

    /*public ResultSet getResultPage(String querySetName, String queryName, 
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
	}*/

    public String getResultAsTable(String querySetName, String queryName, Hashtable paramHash, boolean useCache) throws Exception, QueryParamsException {
	QuerySet querySet 
	    = wdkModel.getQuerySet(querySetName);
	Query  query = querySet.getQuery(queryName);
	QueryInstance instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return ((SqlQueryInstance)instance).getResultAsTable();
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   protected methods   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    void displayQuery(Query query) throws Exception {
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
    
    String formatParamPrompt(Param param) throws Exception {
        
        String newline = System.getProperty( "line.separator" );
        
        String prompt = "  " + param.getPrompt();
        
        if (param instanceof SqlEnumParam) {
            SqlEnumParam enumParam = (SqlEnumParam)param;
            prompt += " (chose one";
            if (enumParam.getMultiPick().booleanValue()) prompt += " or more"; 
            prompt += "):";
            Map hash = enumParam.getKeysAndValues(resultFactory);
            Iterator keys = hash.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String)keys.next();
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
        File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
        
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
                ModelXmlParser.parseXmlFile(modelXmlFile);
            ResultFactory resultFactory = wdkModel.getResultFactory();
            SqlResultFactory sqlResultFactory = 
                new SqlResultFactory(dataSource, platform, 
                        login, instanceTable);
            resultFactory.setSqlResultFactory(sqlResultFactory);
            QueryTester tester = new QueryTester(wdkModel, resultFactory);
            
            // if no params supplied, show the query prompts
            if (!haveParams) {
                Query query = null;
                if (paging) {
		    //record set stuff eventually?
                } else {
		    query = 
                        wdkModel.getQuerySet(querySetName).
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
                } 
		/*else if (paging) {
                    ResultSet rs = tester.getResultPage(querySetName, 
                            queryName, 
                            Integer.parseInt(rows[0]),
                            Integer.parseInt(rows[1]),
                            paramHash,
                            useCache);
                    SqlUtils.printResultSet(rs);
		    }*/ 
		else {
		    Query temp = 
                        wdkModel.getQuerySet(querySetName).
                        getQuery(queryName);

		    ResultList rs = tester.getResult(querySetName, 
						     queryName, paramHash,
						     useCache);
		    rs.print();
                }
            }
	    runQueryNameListTest(tester, wdkModel, querySetName);
        } catch (QueryParamsException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } 
    }
    

    private static void runQueryNameListTest(QueryTester queryTester, WdkModel wdkModel, String querySetName){

	QueryNameList queryNameLists[] = wdkModel.getAllQueryNameLists();
	if (queryNameLists != null){
	    for (int i = 0; i < queryNameLists.length; i++){
		QueryNameList nextQueryNameList = queryNameLists[i];
		
		QueryName queries[] = nextQueryNameList.getQueryNames();
		
		if (queries != null){
		    for (int j = 0; j < queries.length; j++){
			QueryName nextQueryName = queries[j];
			String nextQuerySetName = nextQueryName.getQuerySetName();
			String realQueryName = nextQueryName.getQueryName();
			try {
			 
			    //			    if (wdkModel.hasPageableQuerySet(nextQuerySetName)){
				//PageableQuerySet pqs = wdkModel.getPageableQuerySet(nextQuerySetName);
				//PageableQueryI pq = pqs.getQuery(realQueryName);
				//queryTester.displayQuery(pq);
			    //}
			      //since it passed all checks; queySetName has to be simpleQuerySet
			    QuerySet qs = wdkModel.getQuerySet(nextQuerySetName);
			    Query q = qs.getQuery(realQueryName);
			    queryTester.displayQuery(q);
			    
			}
			catch (Exception e){
			    System.err.println(e.getMessage());
			    e.printStackTrace();
			}
			    
		    }
		}
	    }
	}
    }

    private static void addOption(Options options, String argName, String desc) {
        
        Option option = new Option(argName, true, desc);
        option.setRequired(true);
        option.setArgName(argName);
        
        options.addOption(option);
    }
    
    
    static Options declareOptions() {
	Options options = new Options();

    // config file
    addOption(options, "configFile", "An .xml file that specifies a ModelConfig object.");
    // query set file
    addOption(options, "modelXmlFile", "An .xml file that specifies a container of Query set objects.");
    // record set name
    addOption(options, "querySetName", "The name of the query set in which to find the query");
    // record name
    addOption(options, "queryName", "The name of the query to run.");

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
    Option params = new Option("params", true, "space delimited list of param_name param_value ....");
    params.setArgName("params");
    params.setArgs(Option.UNLIMITED_VALUES);
    //params.setValueSeparator(',');
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
            " -modelXmlFile model_xml_file" +
            " -querySetName query_set_name" +
            " -queryName query_name" +
            " [-dontCache]" +
            " [-returnTable | -returnSize | -rows start end]" +
            " [-params param_1_name,param_1_value,...]";
        
        String header = 
            newline + "Run a query found in a WDK Model xml file.  If run without -params, displays the parameters for the specified query" + newline + newline + "Options:" ;
        
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
    
