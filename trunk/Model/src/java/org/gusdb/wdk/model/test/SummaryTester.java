package org.gusdb.gus.wdk.model.test;

import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.Summary;
import org.gusdb.gus.wdk.model.SummaryInstance;
import org.gusdb.gus.wdk.model.SummarySet;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.WdkUserException;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class SummaryTester {
    
    public static void main(String[] args) {
	
	String cmdName = System.getProperties().getProperty("cmdName");
	
	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);
	
	File modelConfigXmlFile = 
	    new File(cmdLine.getOptionValue("configFile"));
	File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
        File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));
	System.err.println("SummaryTester: model xml file: " + cmdLine.getOptionValue("modelXmlFile"));
	System.err.println("SummaryTester: model prop file: " + cmdLine.getOptionValue("modelPropFile"));
	String summarySetName = cmdLine.getOptionValue("summarySetName");
	String summaryName = cmdLine.getOptionValue("summaryName");
	String[] rows = cmdLine.getOptionValues("rows");

	validateRowCount(rows);
	
	String[] params = null;
	boolean haveParams = cmdLine.hasOption("params");
	if (haveParams){
	    params = cmdLine.getOptionValues("params");
	}
	
	
	try {
        
	    File schemaFile = new File(System.getProperty("schemaFile"));
	    WdkModel wdkModel = 
		ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL());

	    wdkModel.configure(modelConfigXmlFile);
	    wdkModel.setResources();

	    System.err.println("SummaryTester: getting summary " + summaryName);
	    SummarySet summarySet = wdkModel.getSummarySet(summarySetName);
	    Summary summary = summarySet.getSummary(summaryName);

	    if (haveParams){
		Hashtable paramValues = parseParamArgs(params);
		Query query = summary.getQuery();
		query.setIsCacheable(new Boolean(true));
		System.err.println("SummaryTester: going through rows");
		int pageCount = 1;
		
		for (int i = 0; i < rows.length; i+=2){
		    int nextStartRow = Integer.parseInt(rows[i]);
		    int nextEndRow = Integer.parseInt(rows[i+1]);

		    SummaryInstance si = summary.makeSummaryInstance(paramValues, nextStartRow, nextEndRow);
		    System.err.println("SummaryTester: have " + summary.getTotalLength(paramValues) + " total rows");		    
		    System.err.println("Printing Record Instances on page " + pageCount);
		    si.print();
		    si.printAsTable();
		    pageCount++;
		}
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

	//summarySetName
	addOption(options, "summarySetName", "The name of the summarySet in which to find the summary");
	//summaryName
	addOption(options, "summaryName", "the name of the summary to run");
	//rows to return
	Option rows = new Option("rows", "the start and end pairs of the Record Instance rows to return");
	rows.setArgs(Option.UNLIMITED_VALUES);
	rows.setRequired(true);
	options.addOption(rows);
	//params
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
	    " -summarySetName summary_set_name" +
	    " -summaryName summary_name";

	String header = 
	    newline + "Print a summary found in a WDK Model xml file. Options:" ;

	String footer = "";

	//	PrintWriter stderr = new PrintWriter(System.err);
	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
    }

}
