package org.gusdb.wdk.model.test;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.implementation.SqlQueryInstance;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class QueryTester {

    WdkModel wdkModel;

    public QueryTester(WdkModel wdkModel){
	this.wdkModel = wdkModel;
    }


    //////////////////////////////////////////////////////////////////////
    /////////////   public methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public ResultList getResult(String querySetName, String queryName, 
			       Hashtable paramHash, 
			       boolean useCache) throws WdkModelException, WdkUserException {
	QuerySet querySet 
	    = wdkModel.getQuerySet(querySetName);
	Query query = querySet.getQuery(queryName);
	QueryInstance instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return instance.getResult();
    }

    public String getResultAsTable(String querySetName, String queryName, Hashtable paramHash, boolean useCache) throws WdkModelException, WdkUserException {
	QuerySet querySet 
	    = wdkModel.getQuerySet(querySetName);
	Query  query = querySet.getQuery(queryName);
	QueryInstance instance = query.makeInstance();
	instance.setIsCacheable(useCache);
	instance.setValues(paramHash);
	return ((SqlQueryInstance)instance).getResultAsTable();
    }

    public WdkModel getWdkModel(){
	return this.wdkModel;
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   protected methods   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    void displayQuery(Query query) throws WdkModelException {
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
    
    String formatParamPrompt(Param param) throws WdkModelException {
        
        String newline = System.getProperty( "line.separator" );
        
        String prompt = "  " + param.getPrompt();
        
        if (param instanceof FlatVocabParam) {
            FlatVocabParam enumParam = (FlatVocabParam)param;
            prompt += " (chose one";
            if (enumParam.getMultiPick().booleanValue()) prompt += " or more"; 
            prompt += "):";
            String[] vocab = enumParam.getVocab();
	    for (int i=0; i<vocab.length; i++) {
                String term = vocab[i];
                prompt += newline + "    " + term + " (" + 
		    enumParam.getInternalValue(term) + ")";
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
        File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));
        
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

	    File schemaFile = new File(System.getProperty("schemaFile"));
            WdkModel wdkModel = 
                ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL(), modelConfigXmlFile.toURL());

	    QueryTester tester = new QueryTester(wdkModel);
            
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
        } catch (WdkUserException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
        } catch (Exception e) {
	    System.err.println(e);
            e.printStackTrace();
            System.exit(1);
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
	addOption(options, "configFile", "the model config .xml file");

	// model file
	addOption(options, "modelXmlFile", "An .xml file that specifies WDK Model.");
	// model prop file
	addOption(options, "modelPropFile", "A .prop file that specifies key=value pairs to substitute into the model file.");
	
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
}
    
