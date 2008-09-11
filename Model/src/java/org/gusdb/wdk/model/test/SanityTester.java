package org.gusdb.wdk.model.test;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.ParamValuesSet;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.xml.XmlAnswer;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.implementation.SqlUtils;
import org.xml.sax.SAXException;

import com.sun.org.apache.bcel.internal.classfile.Attribute;

/**
 * SanityTester.java " [-project project_id]" +
 * 
 * 
 * Main class for running the sanity tests, which is a way to test all Queries
 * and RecordClasss in a wdk model to make sure they work as intended and their
 * results fall within an expected range, even over the course of code base
 * development. See the usage() method for parameter information, and see the
 * gusDb.org wiki page for the structure and content of the sanity test.
 * 
 * Created: Mon August 23 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-23 12:31:12 -0400 (Tue, 23 Aug
 *          2005) $Author$
 */
public class SanityTester {

    int queriesPassed = 0;
    int queriesFailed = 0;
    int recordsPassed = 0;
    int recordsFailed = 0;
    int questionsPassed = 0;
    int questionsFailed = 0;
    int testCount = 0;
    Integer skipTo; // skip tests before this one
    Integer stopAfter; // stop after this test
    static final String newline = System.getProperty("line.separator");

    WdkModel wdkModel;
    boolean verbose;
    boolean failuresOnly;
    boolean indexOnly;
    String modelName;
    private Boolean doNotTest;
    private static final int MAXROWS = 1000000000;

    public static final String BANNER_LINE_top = "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv";
    public static final String BANNER_LINE_bot = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^";

    private static final Logger logger = Logger.getLogger(SanityTester.class);
    
    public SanityTester(String modelName, boolean verbose, Integer skipTo,
            Integer stopAfter, boolean failuresOnly, boolean indexOnly)
            throws WdkModelException, WdkUserException {
        this.wdkModel = WdkModel.construct(modelName);
        this.verbose = verbose;
        this.failuresOnly = failuresOnly;
        this.indexOnly = indexOnly;
        this.modelName = modelName;
        this.skipTo = skipTo;
        this.stopAfter = stopAfter;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private void testQuestionSets() throws SQLException, WdkModelException {

        System.out.println("Sanity Test:  Checking questions" + newline);

	for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
	    if (questionSet.getDoNotTest()) continue;

	    for (Question question : questionSet.getQuestions()) {
		Query query = question.getQuery();
		if (query.getDoNotTest() || query.getQuerySet().getDoNotTest()) continue;
		for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
		    testQuestion(questionSet, question, paramValuesSet);
		}
	    }
	}
    }

    private void testQuestion(QuestionSet questionSet, Question question,
			      ParamValuesSet paramValuesSet) {

	testCount++;
	if (skipTo != null && testCount < skipTo) return;
	if (stopAfter != null && testCount > stopAfter) return;
	if (indexOnly) {
	    System.out.println(" [test: " + testCount + "]"
			       + " QUESTION " + question.getFullName()
			       + " (query " + question.getQuery().getFullName() + ")"
			       + newline);
	    return;
	}
	long start = System.currentTimeMillis();
	int sanityMin = paramValuesSet.getMinRows();
	int sanityMax = MAXROWS;
	boolean passed = false;
	String status = " FAILED!";
	String prefix = "***";
	String returned = "";
	String expected = "";
	Exception caughtException = null;

	try {
	    Answer answer = question.makeAnswer(paramValuesSet.getParamValues(), 1, 100);

	    int resultSize = answer.getResultSize();

	    // get the summary attribute list
	    Map<String, AttributeField> summary = answer.getSummaryAttributes();
	    // iterate through the page and try every summary attribute of
	    // each record
	    while (answer.hasMoreRecordInstances()) {
		RecordInstance record = answer.getNextRecordInstance();
		// for (RecordInstance record : answer.getRecordInstances()) {
		StringBuffer sb = new StringBuffer();
		for (String attrName : summary.keySet()) {
		    sb.append(record.getAttributeValue(attrName));
		    sb.append('\t');
		}
		logger.debug("Record: " + sb.toString());
	    }

	    passed = (resultSize >= sanityMin && resultSize <= sanityMax);

	    returned = " It returned " + resultSize + " rows. ";
	    if (sanityMin != 1 || sanityMax != MAXROWS)
		expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

	} catch (Exception e) {
	    returned = " It threw an exception.";
	    caughtException = e;
	} finally {
	    long end = System.currentTimeMillis();
	    if (passed) {
		queriesPassed++;
		prefix = "";
		status = " passed.";
	    }
	    else {
		queriesFailed++;
	    }
	    if (!passed) System.out.println(BANNER_LINE_top);
	    String cmd = " [ wdkSummary -model " + wdkModel.getName()
		+ " -question " + question.getFullName() 
		+ " -rows 1 100"
		+ " -params " + paramValuesSet.getCmdLineString()
		+ " ] ";

	    String msg = prefix + ((end - start) / 1000F)
		+ " [test: " + testCount + "]" 
		+ " QUESTION " + question.getFullName()
		+ " (query " + question.getQuery().getFullName() + ")"
		+ status 
		+ returned
		+ expected
		+ cmd
		+ newline;
	    if (!passed || !failuresOnly) System.out.println(msg);
	    if (caughtException != null)
		caughtException.printStackTrace(System.err);
	    if (!passed) System.out.println(BANNER_LINE_bot + newline);

	    // check the connection usage
            RDBMSPlatformI platform = wdkModel.getRDBMSPlatform();
	    if (platform.getActiveCount() > 0) {
		System.err.println("Connection leak ("
				   + platform.getActiveCount() + ") for question: "
				   + question.getFullName());
	    }
	}
    }

    private void testQuerySets(String queryType) throws SQLException, WdkModelException {

        System.out.println("Sanity Test:  Checking " + queryType + " queries"
			   + newline);

	for (QuerySet querySet : wdkModel.getAllQuerySets()) {
	    if (!querySet.getQueryType().equals(queryType)
		  || querySet.getDoNotTest()) continue;

	    int minRows = 0;
	    if (queryType.equals("attribute")) {
		/*
		// discover number of entities expected in each attribute query
		String cardinalitySql = querySet.getCardinalitySql();
		ResultSet rs = 
		    SqlUtils.executeQuery(wdkModel.getRDBMSPlatform().getDataSource(), 
					  cardinalitySql);
		rs.next();
		minRows = rs.getInt(1);
		SqlUtils.closeResultSet(rs);
		*/
	    }

	    for (Query query : querySet.getQueries()) {
		if (query.getDoNotTest()) continue;
		for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
		    if (!queryType.equals("attribute"))
			minRows = paramValuesSet.getMinRows();
		    testQuery(querySet, query, queryType, minRows, paramValuesSet);
		}
	    }
	}
    }

    private void testQuery(QuerySet querySet, Query query, String queryType,
			   int minRows, ParamValuesSet paramValuesSet) {

	String typeUpperCase = queryType.toUpperCase();

	testCount++;
	if (skipTo != null && testCount < skipTo) return;
	if (stopAfter != null && testCount > stopAfter) return;
	if (indexOnly) {
	    System.out.println(" [test: " + testCount + "] " 
			       + typeUpperCase + " QUERY " 
			       + query.getFullName()
			       + newline);
	    return;
	}

	boolean passed = false;
	String prefix = "***";
	String status = " FAILED!";
	int sanityMin = minRows;
	int sanityMax = MAXROWS;
	int counter = 0;
	long start = System.currentTimeMillis();
	String returned = "";
	String expected = "";
	Exception caughtException = null;

	try {
	    QueryTester queryTester = new QueryTester(wdkModel);
	    ResultList rs = 
		queryTester.getResult(querySet.getName(),
				      query.getName(),
				      paramValuesSet.getParamValues());

	    while (rs.next()) { counter++; }
	    rs.close();

	    passed = (counter >= sanityMin && counter <= sanityMax);

	    returned = " It returned " + counter + " rows. ";
	    if (sanityMin != 1 || sanityMax != MAXROWS)
		expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

	} catch (Exception e) {
	    returned = " It threw an exception.";
	    caughtException = e;
	} finally {
	    long end = System.currentTimeMillis();
	    if (passed) {
		queriesPassed++;
		prefix = "";
		status = " passed.";
	    }
	    else {
		queriesFailed++;
	    }
	    if (!passed) System.out.println(BANNER_LINE_top);

	    String cmd = " [ wdkQuery -model " + wdkModel.getName()
		+ " -query " + query.getFullName() 
		+ " -params " + paramValuesSet.getCmdLineString()
		+ " ] ";

	    String msg = prefix + ((end - start) / 1000F)
		+ " [test: " + testCount + "]" 
		+ " " + typeUpperCase + " QUERY " + query.getFullName()
		+ status 
		+ returned
		+ expected
		+ cmd
		+ newline;
	    if (!passed || !failuresOnly) System.out.println(msg);
	    if (caughtException != null)
		caughtException.printStackTrace(System.err);
	    if (!passed) System.out.println(BANNER_LINE_bot + newline);
	}
    }

    /**
     * @param queryResult
     *        a two-value array where the first entry is the number of queries
     *        that passed the test and the second is the number of queries that
     *        failed.
     * 
     * @param recordResult
     *        a two-value array where the first entry is the number of records
     *        that passed the test and the second is the number of records that
     *        failed.
     * 
     * @param return
     *        true if one or more tests failed; false otherwise.
     */

    private boolean printSummaryLine() {

        boolean failedOverall = (queriesFailed > 0 || recordsFailed > 0 || questionsFailed > 0);
        String result = failedOverall ? "FAILED" : "PASSED";

        int totalPassed = queriesPassed + recordsPassed + questionsPassed;
        int totalFailed = queriesFailed + recordsFailed + questionsFailed;

        StringBuffer resultLine = new StringBuffer(
                "***Sanity test summary***" + newline);
        resultLine.append("Skipped to: " + skipTo + newline);
        resultLine.append("Stopped after: " + stopAfter + newline);
        resultLine.append("Total Passed: " + totalPassed + newline);
        resultLine.append("Total Failed: " + totalFailed + newline);
        resultLine.append("   " + queriesPassed + " queries passed, "
                + queriesFailed + " queries failed" + newline);
        resultLine.append("   " + recordsPassed + " records passed, "
                + recordsFailed + " records failed" + newline);
        resultLine.append("   " + questionsPassed + " questions passed, "
                + questionsFailed + " questions failed" + newline);
        resultLine.append("Sanity Test " + result + newline);
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
        addOption(
                options,
                "model",
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml), the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

        // verbose
        Option verbose = new Option("verbose",
                "Print out more information while running test.");
        options.addOption(verbose);

        // verbose
        Option skipTo = new Option("skipTo", true,
                "Skip tests before this one.  Provide the integer test number.");
        options.addOption(skipTo);

        Option stopAfter = new Option("stopAfter", true,
                "Stop after this test.  Provide the integer test number.");
        options.addOption(stopAfter);

        Option failuresOnly = new Option("failuresOnly",
                "Only print failures only.");
        options.addOption(failuresOnly);

        Option indexOnly = new Option("indexOnly",
                "Only print an index of the tests.");
        options.addOption(indexOnly);
        return options;
    }

    static CommandLine parseOptions(String cmdName, Options options,
            String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.out.println("");
            System.out.println("Parsing failed.  Reason: " + exp.getMessage());
            System.out.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName
                + " -model model_name"
                + " [-verbose] [-skipTo testnum] [-stopAfter testnum]  [-failuresOnly | -indexOnly";

        String header = newline
                + "Run a test on all queries and records in a wdk model."
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

    // private static Logger logger = Logger.getLogger(SanityTester.class);

    public static void main(String[] args) throws WdkModelException,
            SAXException, IOException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
						  SQLException,					  
            WdkUserException {
        String cmdName = System.getProperty("cmdName");
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        String skipToStr = cmdLine.getOptionValue("skipTo");

        Integer skipTo = skipToStr == null ? null : Integer.decode(skipToStr);

        String stopAfterStr = cmdLine.getOptionValue("stopAfter");

        Integer stopAfter = stopAfterStr == null ? null
                : Integer.decode(stopAfterStr);

        boolean verbose = cmdLine.hasOption("verbose");

        boolean failuresOnly = cmdLine.hasOption("failuresOnly");

        boolean indexOnly = cmdLine.hasOption("indexOnly");

        SanityTester sanityTester = new SanityTester(modelName, 
                verbose, skipTo, stopAfter, failuresOnly,
                indexOnly);

       	sanityTester.testQuerySets(QuerySet.TYPE_VOCAB);
	sanityTester.testQuerySets(QuerySet.TYPE_ATTRIBUTE);
	sanityTester.testQuerySets(QuerySet.TYPE_TABLE);
	sanityTester.testQuestionSets();
	if (sanityTester.printSummaryLine()) {
	    System.exit(1);
        }
    }
}
