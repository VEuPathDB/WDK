package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.SummaryInstance;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.BooleanQueryInstance;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.QuerySet;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * BooleanQueryTester.java
 *
 * Created: Wed October 6 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: dbarkan $
 *
 */

public class BooleanQueryTester {
    
    public static void main(String[] args) {
	
	String cmdName = System.getProperties().getProperty("cmdName");
	
	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);
	
	File modelConfigXmlFile = 
	    new File(cmdLine.getOptionValue("configFile"));
	File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
        File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));

	String operation = cmdLine.getOptionValue("operation");
	
	String[] firstQueryParams = null;
	boolean haveFirstQueryParams = cmdLine.hasOption("firstQueryParams");
	if (haveFirstQueryParams){
	    firstQueryParams = cmdLine.getOptionValues("firstQueryParams");
	}

	String[] secondQueryParams = null;
	boolean haveSecondQueryParams = cmdLine.hasOption("secondQueryParams");
	if (haveSecondQueryParams){
	    secondQueryParams = cmdLine.getOptionValues("secondQueryParams");
	}
		
	try {
        
	    File schemaFile = new File(System.getProperty("schemaFile"));

	    WdkModel wdkModel = 
		ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL(), modelConfigXmlFile.toURL());
	    
	    Reference firstQueryRef = new Reference(cmdLine.getOptionValue("firstQueryName"));
	    Reference secondQueryRef = new Reference(cmdLine.getOptionValue("secondQueryName"));

	    if (haveFirstQueryParams && haveSecondQueryParams){
		Hashtable firstParamValues = parseParamArgs(firstQueryParams);
		Hashtable secondParamValues = parseParamArgs(secondQueryParams);

		Query firstQuery = wdkModel.getQuerySet(firstQueryRef.getSetName()).getQuery(firstQueryRef.getElementName());
		Query secondQuery = wdkModel.getQuerySet(secondQueryRef.getSetName()).getQuery(secondQueryRef.getElementName());

		firstQuery.setIsCacheable(new Boolean(true));
		secondQuery.setIsCacheable(new Boolean(true));
		
		QueryInstance firstQueryInstance = firstQuery.makeInstance();
		QueryInstance secondQueryInstance = secondQuery.makeInstance();
		
		firstQueryInstance.setValues(firstParamValues);
		secondQueryInstance.setValues(secondParamValues);
		ResultList firstRL = firstQueryInstance.getResult();
		ResultList secondRL = secondQueryInstance.getResult();
	
		BooleanQueryInstance booleanQI = wdkModel.makeBooleanQueryInstance();
		
		booleanQI.init(firstQueryInstance, secondQueryInstance, operation);
		
		ResultList rl = booleanQI.getResult();
		rl.print();

	    }
	    else {
		usage(cmdName, options);
	    }
        } catch (WdkUserException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
	}catch (Exception e) {
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

	addOption(options, "firstQueryName", "Two part name of the first query in the boolean query");

	addOption(options, "secondQueryName", "Two part name of the second query in the boolean query");

	addOption(options, "operation", "The boolean operation to be performed on this query");

	Option firstQueryParams = new Option("firstQueryParams", true, "space delimited list of param_name param_value ....");
	firstQueryParams.setArgName("firstQueryParams");
	firstQueryParams.setArgs(Option.UNLIMITED_VALUES);
	options.addOption(firstQueryParams);

	Option secondQueryParams = new Option("secondQueryParams", true, "space delimited list of param_name param_value ....");
	secondQueryParams.setArgName("secondQueryParams");
	secondQueryParams.setArgs(Option.UNLIMITED_VALUES);
	options.addOption(secondQueryParams);

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

    static void validateRowCount(String[] rows){
	if (rows.length %2 !=0){
	    throw new IllegalArgumentException("The -rows option must be followed by pairs of row numbers (each pair representing the start and end of a page");
	}
    }


    static Hashtable parseParamArgs(String[] params) {

	Hashtable h = new Hashtable();

	if (params.length % 2 != 0) {
	    throw new IllegalArgumentException("The -params option must be followed by key value pairs only");
	}
	for (int i=0; i<params.length; i+=2) {
	    h.put(params[i], params[i+1]);
	}
	return h;

    }

    static void usage(String cmdName, Options options) {

	String newline = System.getProperty( "line.separator" );
	String cmdlineSyntax = 
	    cmdName + 
	    " -configFile config_file" +
	    " -modelXmlFile model_xml_file" +
	    " -questionSetName summary_set_name" +
	    " -questionName summary_name";

	String header = 
	    newline + "BOOLEAN QUERY TESTER.  NEED TO UPDATE USAGE";

	String footer = "";

	//	PrintWriter stderr = new PrintWriter(System.err);
	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
    }

}
