package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.Record;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.RecordSet;
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
 * Main class for running the sanity tests, which is a way to test all Queries and Records in
 * a wdk model to make sure they work as intended and their results fall within an expected range,
 * even over the course of code base development.  See the usage() method for parameter information,
 * and see the gusDb.org wiki page for the structure and content of the sanity test.
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: dbarkan $
 */
public class SanityTester {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /*
     * Wdk model that contains the Queries and Records to be tested.
     */ 
    WdkModel wdkModel;

    /**
     * Root model object containing information about the tests to run, created from a model xml file.
     * Every Query and Record in <code>wdkModel</code> must be represented here.
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
        
        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);
        
        File modelConfigXmlFile = 
            new File(cmdLine.getOptionValue("configFile"));
        File sanityXmlFile = new File(cmdLine.getOptionValue("sanityXmlFile"));
        File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));
	File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
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
    
	    if (verbose) System.err.println(sanityModel.toString());
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
     * Checks to make sure every Query and Record in the wdkModel is represented in the sanity test.
     * If a query or record is in the sanity test but not the model then that will be caught in the
     * other validation tests.
     */
    private void runExistenceTest(QueryTester queryTester, boolean verbose){
	System.err.println("Sanity Test:  Checking to make sure all Queries and Records in model " + queryTester.getWdkModel().getName() +
			   " are represented in the test\n");
	QuerySet querySets[] = queryTester.getWdkModel().getAllQuerySets();
	if (querySets != null){
	    for (int i = 0; i < querySets.length; i++){
		QuerySet nextQuerySet = querySets[i];
		Query queries[] = nextQuerySet.getQueries();
		for (int j = 0; j < queries.length; j++){
		    Query nextQuery = queries[j];
		    if (!sanityModel.hasSanityQuery(nextQuerySet.getName() + "." + nextQuery.getName())){
			System.err.println("Sanity Test Failed!  Query " + nextQuerySet.getName() + "." + nextQuery.getName() +
					   " is not represented in the sanity test\n");
		    }
		    else {
			if (verbose){
			    System.err.println("Query " + nextQuerySet.getName() + "." + nextQuery.getName() +
					       " is accounted for in the sanity test\n");
			} 
		    }
		}
	    }
	}
	RecordSet recordSets[] = queryTester.getWdkModel().getAllRecordSets();
	for (int i = 0; i < recordSets.length; i++){
	    RecordSet nextRecordSet = recordSets[i];
	    Record records[] = nextRecordSet.getRecords();
	    if (records != null){
		for (int j = 0; j < records.length; j++){
		    Record nextRecord = records[j];
		    if (!sanityModel.hasSanityRecord(nextRecordSet.getName() + "." + nextRecord.getName())){
			System.err.println("Sanity Test Failed!  Record " + nextRecordSet.getName() + "." + nextRecord.getName() + 
					   " is not represented in the sanity test\n");
		    }
		    else {
			if (verbose){
			    System.err.println("Record " + nextRecordSet.getName() + "." + nextRecord.getName() +
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
	System.err.println("Sanity Test:  Checking queries\n");
	
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
		    nextQueryReference = new Reference(nextSanityQuery.getTwoPartName());
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
			System.err.println(BANNER_LINE);
			System.err.println("***QUERY " + nextQueryReference.getSetName() + "." + nextQueryReference.getElementName() + 
					   " FAILED!***  It returned " + counter + " rows--not within expected range (" + sanityMin + " - " + sanityMax + ")");
			printFailureMessage(nextSanityQuery, cmdLine);
			System.err.println(BANNER_LINE + "\n");
			queryFail++;
		    }
		    else {
			System.err.println("Query " + nextQueryReference.getSetName() + "." + nextQueryReference.getElementName() +
					   " passed--returned " + counter + " rows, within expected range (" + sanityMin + " - " + sanityMax + ")\n");
		    }
		    queryPass++;
		}
	     	catch(Exception e){
		    queryFail++;
		    System.err.println(BANNER_LINE);
		    System.err.println("***QUERY " + nextQueryReference.getSetName() + "." + nextQueryReference.getElementName() + " FAILED!***  It threw an exception.");
		    printFailureMessage(nextSanityQuery, cmdLine);
		    System.err.println(BANNER_LINE + "\n");
		}
	    }
	}
	Integer result[] = new Integer[2];
	result[0] = new Integer(queryPass);
	result[1] = new Integer(queryFail);
	return result;
    }

    /**
     * Processes each Record (by simply calling its print method, which exercises all of the queries within that record)
     * provided in the sanity test.  The test fails if an exception is thrown.
     *
     * @param return a two-value array where the first entry is the number of records that passed the test and
     *               the second is the number of records that failed.
     */		
    private Integer[] runRecordValidationTest(QueryTester queryTester, boolean verbose, CommandLine cmdLine){
	//DTB -- this could probably be refactored to combine with the query validation method
	System.err.println("Sanity Test:  Checking records\n");

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
		    nextRecordReference = new Reference(nextSanityRecord.getTwoPartName());
		    RecordSet nextRecordSet = queryTester.getWdkModel().getRecordSet(nextRecordReference.getSetName());
		    Record nextRecord = nextRecordSet.getRecord(nextRecordReference.getElementName());
		    RecordInstance nextRecordInstance = nextRecord.makeRecordInstance();
		    nextRecordInstance.setPrimaryKey(nextSanityRecord.getPrimaryKey().toString());
		    
		    String riString = nextRecordInstance.print();
		    System.err.println("Record " + nextRecordReference.getSetName() + "." + nextRecordReference.getElementName() + " passed\n");
		    if (verbose) System.err.println(riString + "\n");
		    recordPass++;
		}
	    	catch (Exception wme){
		    recordFail++;
		    System.err.println(BANNER_LINE);
		    System.err.println("***RECORD " + nextRecordReference.getSetName() + "." + nextRecordReference.getElementName() + " FAILED!***");
		    printFailureMessage(nextSanityRecord, cmdLine);
		    System.err.println(BANNER_LINE + "\n");
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
	    
	    File modelConfigXmlFile = 
		new File(cmdLine.getOptionValue("configFile"));
	    File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));
	    File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
	    
	    String fullModelConfigXml = modelConfigXmlFile.getAbsolutePath();
	    String fullModelProp = modelPropFile.getAbsolutePath();
	    String fullModelXml = modelXmlFile.getAbsolutePath();
	
	    String globalArgs = "-configFile " + fullModelConfigXml + " -modelPropFile " + fullModelProp + " -modelXmlFile " + fullModelXml;
	    String command = element.getCommand(globalArgs);
	    message.append(command);

	    System.err.println(message.toString());

	}
	catch (Exception e){
	    System.err.println("An error occurred when attempting to create a message explaining a previous error");
	    System.err.println(e.getMessage());
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
	resultLine.append("Overall Result:  Sanity Test " + result + "\n");
	resultLine.append(queryPassed + " queries passed, " + queryFailed + " queries failed\n");
 	resultLine.append(recordPassed + " records passed, " + recordFailed + " records failed");
	System.err.println(resultLine.toString());
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

	// config file
	addOption(options, "configFile", "the model config .xml file");
	// sanity file
	addOption(options, "sanityXmlFile", "An .xml file that specifies queries and records in the sanity test.");
	// model xml file
	addOption(options, "modelXmlFile", "An .xml file that specifies WDK Model.");
	// model prop file
	addOption(options, "modelPropFile", "A .prop file that specifies key=value pairs to substitute into the model file.");
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
	    " -modelPropFile model_prop_file" + 
            " -sanityXmlFile sanity_xml_file" +
	    " -modelXmlFile model_xml_file" +
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
    
