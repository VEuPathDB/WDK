package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.BooleanQueryInstance;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.BooleanQuestionNode;

import org.gusdb.wdk.model.test.TestBooleanTree;

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
 * BooleanQuestionTester.java
 *
 * WDK Testing class that creates a recursive boolean Question and prints
 * out the Answer.  Currently, this uses data returned by the TestBooleanTree class.
 *
 * Created: Wed October 6 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: dbarkan $
 *
 */

public class BooleanQuestionTester {
    
    // ------------------------------------------------------------------
    // Main
    // ------------------------------------------------------------------

    public static void main(String[] args) {
	
	String cmdName = System.getProperties().getProperty("cmdName");
	File configDir = 
	    new File(System.getProperties().getProperty("configDir"));

	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);

	String modelName = cmdLine.getOptionValue("model");
	
	File modelConfigXmlFile = new File(configDir, modelName+"-config.xml");
        File modelXmlFile = new File(configDir, modelName + ".xml");
        File modelPropFile = new File(configDir, modelName + ".prop");

	String[] rows = cmdLine.getOptionValues("rows");
		
	validateRowCount(rows);

	try {
	    
	    File schemaFile = new File(System.getProperty("schemaFile"));

	    //create model
	    WdkModel wdkModel = 
		ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL(), modelConfigXmlFile.toURL());
	    
	    int startRow = Integer.parseInt(rows[0]);
	    int endRow = Integer.parseInt(rows[1]);
	
	    //create recursive question tree
	    BooleanQuestionNode topNode = TestBooleanTree.getTestTree(wdkModel);
	    System.err.println(topNode.toString());
	    //init recursive method
	    BooleanQuestionNode.setAllValues(topNode);

	    //	    runGrowTest(topNode, "01", wdkModel);
	   
	    Question topQuestion = topNode.getQuestion();
	    int pageCount = 1;
	    for (int i = 0; i < rows.length; i+=2){

		int nextStartRow = Integer.parseInt(rows[i]);
		int nextEndRow = Integer.parseInt(rows[i+1]);
		
		Answer answer = topQuestion.makeAnswer(topNode.getValues(), nextStartRow, nextEndRow);
		System.out.println("Printing Record Instances on page " + pageCount);
		answer.printAsTable();
		pageCount++;
	    }
	    

	}
	catch (WdkUserException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
	}catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private static void runGrowTest(BooleanQuestionNode topNode, String nodeId, WdkModel model) throws WdkModelException, WdkUserException{
	
	BooleanQuestionNode found = topNode.find(nodeId);
	System.out.println("BooleanQuestionTester.runGrowTest: Tree before growing\n " + topNode.toString());
	System.out.println("BooleanQuestionTester.runGrowTest:  Found node " + found.toString());
	found.grow(TestBooleanTree.makeNewLeafNode(model), "Union", model);
	System.out.println("BooleanQuestionTester.runGrowTest:  New tree after growing\n " + topNode.toString());
    }



    private static void addOption(Options options, String argName, String desc) {
	
	Option option = new Option(argName, true, desc);
	option.setRequired(true);
	option.setArgName(argName);
	
	options.addOption(option);
    }
        
    static Options declareOptions() {
	Options options = new Options();

	// model name
	addOption(options, "model", "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

	// rows
	Option rows = new Option("rows", "the start and end pairs of the summary rows to return");
	rows.setArgs(Option.UNLIMITED_VALUES);
	rows.setRequired(true);
	options.addOption(rows);

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
	    " -model model_name" +
	    " -rows start_1 end_1...";
	   
	String header = 
	    newline + "prints out the Answer to a recursive boolean Question" + newline + newline + "Options: ";

	String footer = "";

	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
    }
}
