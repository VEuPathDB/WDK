package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	String questionFullName = cmdLine.getOptionValue("question");
	
	String[] params = null;
	boolean haveParams = cmdLine.hasOption("params");
	if (haveParams){
	    params = cmdLine.getOptionValues("params");
	}

	boolean toXml = cmdLine.hasOption("toXml");
	String xmlFileName = cmdLine.getOptionValue("toXml");
	String[] rows = cmdLine.getOptionValues("rows");

	if (toXml) {
	    if (xmlFileName == null || xmlFileName.equals("")) usage(cmdName, options);
	} else {
	    if (rows == null || rows.length == 0) usage(cmdName, options);
	    validateRowCount(rows);
	}

	try {
        
	    File schemaFile = new File(System.getProperty("schemaFile"));
	    Reference ref = new Reference(questionFullName);
	    String questionSetName = ref.getSetName();
	    String questionName = ref.getElementName();
	    WdkModel wdkModel = WdkModel.construct(cmdLine.getOptionValue("model"));

	    QuestionSet questionSet = wdkModel.getQuestionSet(questionSetName);
	    Question question = questionSet.getQuestion(questionName);

	    Hashtable paramValues = new Hashtable();
	    if (haveParams){
		paramValues = parseParamArgs(params); 
	    }

	    // this is suspicious
	    //Query query = question.getQuery();
	    //query.setIsCacheable(new Boolean(true));
	    int pageCount = 1;

	    if (toXml) {
		writeSummaryAsXml(question, paramValues, xmlFileName);
		return;
	    } 
		
	    for (int i = 0; i < rows.length; i+=2){
		int nextStartRow = Integer.parseInt(rows[i]);
		int nextEndRow = Integer.parseInt(rows[i+1]);

		Answer answer = question.makeAnswer(paramValues, nextStartRow, nextEndRow);
		answer.printAsTable();

		// this is wrong.  it only shows one attribute query, not
		// all.  Fix this in Answer by saving a list of attribute
		// queries, not just one.
		if (cmdLine.hasOption("showQuery")) {
		    System.out.println(getLowLevelQuery(answer));
		    return;
		}

		System.out.println("Printing Record Instances on page " + pageCount);
		System.out.println(answer.printAsTable());

		pageCount++;
	    }		
        } catch (WdkUserException e) {
            System.err.println(e.formatErrors());
            System.exit(1);
	}catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	    
    }

    private static void writeSummaryAsXml (Question question, Hashtable paramValues, String xmlFile)
	throws WdkModelException, WdkUserException, IOException {
	Answer answer = question.makeAnswer(paramValues, 1, 10);
	int resultSize = answer.getResultSize();
	answer = question.makeAnswer(paramValues, 1, resultSize);
	FileWriter fw = new FileWriter(new File(xmlFile), false);

	String newline = System.getProperty("line.separator");
        String ident = "    ";

	fw.write("<" + question.getFullName() + ">" + newline);
	fw.close();
	fw = new  FileWriter(new File(xmlFile), true);
	while (answer.hasMoreRecordInstances()) {
	    RecordInstance ri = answer.getNextRecordInstance();
	    fw.write(ri.toXML(ident) + newline);
	}
	fw.write("</" + question.getFullName() + ">" + newline);
	fw.close();
    }

    private static String getLowLevelQuery(Answer answer) throws WdkModelException {
	 QueryInstance instance = answer.getAttributesQueryInstance();
	 String query =  instance.getLowLevelQuery();
	 String newline = System.getProperty( "line.separator" );
	 String newlineQuery = query.replaceAll("^\\s\\s\\s", newline);
	 newlineQuery = newlineQuery.replaceAll("(\\S)\\s\\s\\s", "$1" + newline);
	 return newline + newlineQuery + newline;
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

	// question name
	addOption(options, "question", "The full name (set.element) of the question to run.");
	
	//rows to return
	Option rows = new Option("rows", "The start and end pairs of the summary rows to return. Ignored when toXml is turned on, but required otherwise.");
	rows.setArgs(Option.UNLIMITED_VALUES);
	options.addOption(rows);

	// show query
	Option showQuery = new Option("showQuery", "Show the query as it will be run (with parameter values in place).");
	options.addOption(showQuery);

	// output XML
	Option toXml = new Option("toXml", true, "output summary in XML format to given file");
	options.addOption(toXml);

	//params
	Option params = new Option("params", true, "Space delimited list of param_name param_value ....");
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
	if (params[0].equals("NONE")){
	    return h;
	}
	else {
	    if (params.length % 2 != 0) {
		throw new IllegalArgumentException("The -params option must be followed by key value pairs only");
	    }
	    for (int i=0; i<params.length; i+=2) {
		h.put(params[i], params[i+1]);
	    }
	    return h;
	}
    }

    static void usage(String cmdName, Options options) {

	String newline = System.getProperty( "line.separator" );
	String cmdlineSyntax = 
	    cmdName + 
	    " -model model_name" +
	    " -question full_question_name" +
            " [-rows start end]" +
            " [-showQuery]" +
            " [-toXml <xmlFile>]" +
	    " -params param_1_name param_1_value ...";

	String header = 
	    newline + "Print a summary found in a WDK Model xml file. Options:" ;

	String footer = "";

	//	PrintWriter stderr = new PrintWriter(System.err);
	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
    }

}
