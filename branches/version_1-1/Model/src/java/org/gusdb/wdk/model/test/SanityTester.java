package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.implementation.ModelXmlParser;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * SanityTester.java
 *
 * Main class for running the sanity tests, which is a way to test all Queries and RecordClasss in
 * a wdk model to make sure they work as intended and their results fall within an expected range,
 * even over the course of code base development.  See the usage() method for parameter information,
 * and see the gusDb.org wiki page for the structure and content of the sanity test.
 *
 * Created: Mon August 23 12:00:00 2004 EST
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: dbarkan $
 */
public class SanityTester {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /*
     * Wdk model that contains the Queries and RecordClasss to be tested.
     */ 
    WdkModel wdkModel;

    /**
     * Root model object containing information about the tests to run, created from a model xml file.
     * Every Query and RecordClass in <code>wdkModel</code> must be represented here.
     */
    SanityModel sanityModel;

    /**
     * Result Factory to be given to <code>wdkModel</code> to fetch query results.
     */
    ResultFactory resultFactory;

    /**
     * Line to be used in formatting messages containing information about test failures.
     */
    public static final String BANNER_LINE = "***********************************************************";


    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public SanityTester(SanityModel sanityModel){
	this.sanityModel = sanityModel;
    }


    // ------------------------------------------------------------------
    // Static Methods
    // ------------------------------------------------------------------

    /**
     * Creates model objects using given command line arguments and XML parsers; 
     * creates a new Sanity Tester and uses it to run the standard sanity tests.
     */

    //DTB -- this could be changed to incorporate Angel's ModelMaker class
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
	File sanityXmlFile = new File(configDir, modelName + "-sanity.xml");
       
        boolean verbose = cmdLine.hasOption("verbose");
        	    
	try {

            File schemaFile = new File(System.getProperty("schemaFile"));

	    WdkModel wdkModel = ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL(), modelConfigXmlFile.toURL());
	    QueryTester queryTester = new QueryTester(wdkModel);
	    
	    //make Sanity Model
	    File sanitySchemaFile = new File(System.getProperty("sanitySchemaFile"));

            SanityModel sanityModel = 
                SanityTestXmlParser.parseXmlFile(sanityXmlFile.toURL(), modelPropFile.toURL(), sanitySchemaFile.toURL());

	    sanityModel.validateQueries();

            SanityTester sanityTester = new SanityTester(sanityModel);

	    //run tests
	    sanityTester.runExistenceTest(queryTester, verbose);
	    Integer queryResults[] = sanityTester.runQueryValidationTest(queryTester, verbose, cmdLine);
	    Integer recordResults[] = sanityTester.runRecordValidationTest(queryTester, verbose, cmdLine);
    
	    if (verbose) System.out.println(sanityModel.toString());
	    boolean failedOverall = sanityTester.printSummaryLine(queryResults, recordResults);
	    if (failedOverall){
		System.exit(1);
	    }
	    
	    
        } catch (Exception e) {
	    System.err.println(e);
            e.printStackTrace();
            System.exit(1);
        } 
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    /**
     * Checks to make sure every Query and RecordClass in the wdkModel is represented in the sanity test.
     * If a query or recordClass is in the sanity test but not the model then that will be caught in the
     * other validation tests.
     */
    private void runExistenceTest(QueryTester queryTester, boolean verbose){
	System.out.println("Sanity Test:  Checking to make sure all Queries and RecordClasss in model " + queryTester.getWdkModel().getName() +
			   " are represented in the test\n");
	QuerySet querySets[] = queryTester.getWdkModel().getAllQuerySets();
	if (querySets != null){
	    for (int i = 0; i < querySets.length; i++){
		QuerySet nextQuerySet = querySets[i];
		Query queries[] = nextQuerySet.getQueries();
		for (int j = 0; j < queries.length; j++){
		    Query nextQuery = queries[j];
		    if (!sanityModel.hasSanityQuery(nextQuerySet.getName() + "." + nextQuery.getName())){
			System.out.println("Sanity Test Failed!  Query " + nextQuerySet.getName() + "." + nextQuery.getName() +
					   " is not represented in the sanity test\n");
		    }
		    else {
			if (verbose){
			    System.out.println("Query " + nextQuerySet.getName() + "." + nextQuery.getName() +
					       " is accounted for in the sanity test\n");
			} 
		    }
		}
	    }
	}
	RecordClassSet recordClassSets[] = queryTester.getWdkModel().getAllRecordClassSets();
	for (int i = 0; i < recordClassSets.length; i++){
	    RecordClassSet nextRecordClassSet = recordClassSets[i];
	    RecordClass recordClasses[] = nextRecordClassSet.getRecordClasses();
	    if (recordClasses != null){
		for (int j = 0; j < recordClasses.length; j++){
		    RecordClass nextRecordClass = recordClasses[j];
		    if (!sanityModel.hasSanityRecord(nextRecordClassSet.getName() + "." + nextRecordClass.getName())){
			System.out.println("Sanity Test Failed!  RecordClass " + nextRecordClassSet.getName() + "." + nextRecordClass.getName() + 
					   " is not represented in the sanity test\n");
		    }
		    else {
			if (verbose){
			    System.out.println("RecordClass " + nextRecordClassSet.getName() + "." + nextRecordClass.getName() +
					       " is accounted for in the sanity test\n");
			} 
		    }
		}
	    }
	}
    }

    /**
     * Runs each query provided in the sanity test model (which is also each query in the wdk model).  Compares the
     * results returned by the query to the expected range provided in the sanity model.  The test fails if the
     * result is outside the expected range or if an exception is thrown.
     *
     * @param return a two-value array where the first entry is the number of queries that passed the test and
     *               the second is the number of queries that failed.
     */		
    private Integer[] runQueryValidationTest(QueryTester queryTester, boolean verbose, CommandLine cmdLine){
	System.out.println("Sanity Test:  Checking queries\n");
	
	Reference nextQueryReference = null;
	int queryPass = 0;
	int queryFail = 0;
	SanityQuery nextSanityQuery = null;
	SanityQuery queries[] = sanityModel.getAllSanityQueries();

	if (queries != null){
	    for (int i = 0; i < queries.length; i++){
		try{    
		    //get model query from sanity query
		    nextSanityQuery = queries[i];
		    nextQueryReference = new Reference(nextSanityQuery.getRef());
		    QuerySet nextQuerySet = queryTester.getWdkModel().getQuerySet(nextQueryReference.getSetName());
		    Query nextQuery = nextQuerySet.getQuery(nextQueryReference.getElementName());
		    
		    //run query
		    ResultList rs = queryTester.getResult(nextQueryReference.getSetName(), nextQueryReference.getElementName(),
							  nextSanityQuery.getParamHash(), true);
		    
		    //count results; check if sane
		    int sanityMin = nextSanityQuery.getMinOutputLength().intValue();
		    int sanityMax = nextSanityQuery.getMaxOutputLength().intValue();
		    int counter = 0;
		    
		    while (rs.next()){
			counter++;
		    }
		    //		    rs.close();
		    if (!(sanityMin <= counter && counter <= sanityMax)){
			System.out.println(BANNER_LINE);
			System.out.println("***QUERY " + nextQueryReference.getSetName() + "." + nextQueryReference.getElementName() + 
					   " FAILED!***  It returned " + counter + " rows--not within expected range (" + sanityMin + " - " + sanityMax + ")");
			printFailureMessage(nextSanityQuery, cmdLine);
			System.out.println(BANNER_LINE + "\n");
			queryFail++;
		    }
		    else {
			System.out.println("Query " + nextQueryReference.getSetName() + "." + nextQueryReference.getElementName() +
					   " passed--returned " + counter + " rows, within expected range (" + sanityMin + " - " + sanityMax + ")\n");
		    }
		    queryPass++;
		}
	     	catch(Exception e){
		    queryFail++;
		    System.out.println(BANNER_LINE);
		    System.out.println("***QUERY " + nextQueryReference.getSetName() + "." + nextQueryReference.getElementName() + " FAILED!***  It threw an exception.");
		    printFailureMessage(nextSanityQuery, cmdLine);
		    System.out.println(BANNER_LINE + "\n");
		}
	    }
	}
	Integer result[] = new Integer[2];
	result[0] = new Integer(queryPass);
	result[1] = new Integer(queryFail);
	return result;
    }

    /**
     * Processes each RecordClass (by simply calling its print method, which exercises all of the queries within that recordClass)
     * provided in the sanity test.  The test fails if an exception is thrown.
     *
     * @param return a two-value array where the first entry is the number of records that passed the test and
     *               the second is the number of records that failed.
     */		
    private Integer[] runRecordValidationTest(QueryTester queryTester, boolean verbose, CommandLine cmdLine){
	//DTB -- this could probably be refactored to combine with the query validation method
	System.out.println("Sanity Test:  Checking records\n");

	int recordPass = 0;
	int recordFail = 0;
	SanityRecord nextSanityRecord = null;
	Reference nextRecordReference = null;
	SanityRecord records[] = sanityModel.getAllSanityRecords();
	    
	if (records != null){
	    for (int i = 0; i < records.length; i++){
		
		try {
		    WdkModel wdkModel = queryTester.getWdkModel();
		    
		    //get model record from sanity record
		    nextSanityRecord = records[i];
		    nextRecordReference = new Reference(nextSanityRecord.getRef());
		    RecordClassSet nextRecordClassSet = queryTester.getWdkModel().getRecordClassSet(nextRecordReference.getSetName());
		    RecordClass nextRecordClass = nextRecordClassSet.getRecordClass(nextRecordReference.getElementName());
		    RecordInstance nextRecordInstance = nextRecordClass.makeRecordInstance();
		    nextRecordInstance.setPrimaryKey(nextSanityRecord.getPrimaryKey().toString());
		    
		    String riString = nextRecordInstance.print();
		    System.out.println("Record " + nextRecordReference.getSetName() + "." + nextRecordReference.getElementName() + " passed\n");
		    if (verbose) System.out.println(riString + "\n");
		    recordPass++;
		}
	    	catch (Exception wme){
		    recordFail++;
		    System.out.println(BANNER_LINE);
		    System.out.println("***RECORD " + nextRecordReference.getSetName() + "." + nextRecordReference.getElementName() + " FAILED!***");
		    printFailureMessage(nextSanityRecord, cmdLine);
		    System.out.println(BANNER_LINE + "\n");
		} 
	    }
	}
	Integer result[] = new Integer[2];
	result[0] = new Integer(recordPass);
	result[1] = new Integer(recordFail);
	return result;
    }
    
    /**
     * Prints out a command to run so the user can test failures outside of the sanity test.
     */
    private void printFailureMessage(SanityElementI element, CommandLine cmdLine){
	try{
	    StringBuffer message = new StringBuffer("To test " + element.getType() + " " + element.getName() + ", run the following command: \n ");
	    
	    String modelName = cmdLine.getOptionValue("model");
	
	    String globalArgs = "-model " + modelName;
	    String command = element.getCommand(globalArgs);
	    message.append(command);

	    System.out.println(message.toString());

	}
	catch (Exception e){
	    System.out.println("An error occurred when attempting to create a message explaining a previous error");
	    System.out.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * @param queryResult a two-value array where the first entry is the number of queries that passed the test and
     *                    the second is the number of queries that failed.
     *
     * @param recordResult a two-value array where the first entry is the number of records that passed the test and
     *                     the second is the number of records that failed.
     *
     * @param return       true if one or more tests failed; false otherwise.
     */
    
    private boolean printSummaryLine(Integer queryResult[], Integer recordResult[]){

	int queryPassed = queryResult[0].intValue();
	int queryFailed = queryResult[1].intValue();

	int recordPassed = recordResult[0].intValue();
	int recordFailed = recordResult[1].intValue();
		
	boolean failedOverall = (queryFailed > 0 || recordFailed > 0);
	String result = failedOverall ? "FAILED" : "PASSED";

	StringBuffer resultLine = new StringBuffer("***Sanity test summary***\n");
	resultLine.append(queryPassed + " queries passed, " + queryFailed + " queries failed\n");
 	resultLine.append(recordPassed + " records passed, " + recordFailed + " records failed\n");
	resultLine.append("Sanity Test " + result + "\n");
	System.out.println(resultLine.toString());
	return failedOverall;
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
	addOption(options, "model", "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml), the Model property file ($GUS_HOME/config/model_name.prop), the Sanity Test file ($GUS_HOME/config/model_name-sanity.xml) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

	//verbose
	Option verbose = new Option("verbose","Print out more information while running test.");
	options.addOption(verbose);
	
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
            System.out.println("");
            System.out.println( "Parsing failed.  Reason: " + exp.getMessage() ); 
            System.out.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }   

    static void usage(String cmdName, Options options) {
        
        String newline = System.getProperty( "line.separator" );
        String cmdlineSyntax = 
            cmdName + 
            " -model model_name" +
	    " -verbose";
        
        String header = 
            newline + "Run a test on all queries and records in a wdk model, using a provided sanity model, to ensure that the course of development hasn't dramatically affected wdk functionality." + newline + newline + "Options:" ;
        
        String footer = "";
        
        //	PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

    
}
    
