package org.gusdb.wdk.model.test;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.*;
import org.gusdb.wdk.model.xml.XmlAnswer;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;

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

    WdkModel wdkModel;
    boolean verbose;
    SanityModel sanityModel;
    String modelName;

    public static final String BANNER_LINE = "***********************************************************";

    public SanityTester(String modelName, SanityModel sanityModel,
            boolean verbose) throws WdkModelException {
        this.wdkModel = WdkModel.construct(modelName);
        this.sanityModel = sanityModel;
        this.verbose = verbose;
        this.modelName = modelName;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    /**
     * Checks to make sure every Query and RecordClass in the wdkModel is
     * represented in the sanity test. If a query or recordClass is in the
     * sanity test but not the model then that will be caught in the other
     * validation tests.
     */
    private void existenceTest() {

        QuerySet querySets[] = wdkModel.getAllQuerySets();
        for (int i = 0; i < querySets.length; i++) {
            QuerySet nextQuerySet = querySets[i];
            Query queries[] = nextQuerySet.getQueries();
            for (int j = 0; j < queries.length; j++) {
                Query nextQuery = queries[j];
                if (!sanityModel.hasSanityQuery(nextQuerySet.getName() + "."
                        + nextQuery.getName())) {
                    System.out.println("Sanity Test Failed!  Query "
                            + nextQuerySet.getName() + "."
                            + nextQuery.getName()
                            + " is not represented in the sanity test\n");
                    queriesFailed++;
                }
            }
        }

        RecordClassSet recordClassSets[] = wdkModel.getAllRecordClassSets();
        for (int i = 0; i < recordClassSets.length; i++) {
            RecordClassSet nextRecordClassSet = recordClassSets[i];
            RecordClass recordClasses[] = nextRecordClassSet.getRecordClasses();
            if (recordClasses != null) {
                for (int j = 0; j < recordClasses.length; j++) {
                    RecordClass nextRecordClass = recordClasses[j];
                    if (!sanityModel.hasSanityRecord(nextRecordClassSet.getName()
                            + "." + nextRecordClass.getName())) {
                        System.out.println("Sanity Test Failed!  RecordClass "
                                + nextRecordClassSet.getName() + "."
                                + nextRecordClass.getName()
                                + " is not represented in the sanity test\n");
                        recordsFailed++;
                    }
                }
            }
        }

        QuestionSet questionSets[] = wdkModel.getAllQuestionSets();
        for (int i = 0; i < questionSets.length; i++) {
            QuestionSet nextQuestionSet = questionSets[i];
            Question questions[] = nextQuestionSet.getQuestions();
            for (int j = 0; j < questions.length; j++) {
                Question nextQuestion = questions[j];
                if (!sanityModel.hasSanityQuestion(nextQuestionSet.getName()
                        + "." + nextQuestion.getName())) {
                    System.out.println("Sanity Test Failed!  Question "
                            + nextQuestionSet.getName() + "."
                            + nextQuestion.getName()
                            + " is not represented in the sanity test\n");
                    questionsFailed++;
                }
            }
        }

        // check existence of XmlQuestions
        for (XmlQuestionSet questionSet : wdkModel.getXmlQuestionSets()) {
            for (XmlQuestion question : questionSet.getQuestions()) {
                if (!sanityModel.hasSanityXmlQuestion(question.getFullName())) {
                    System.out.println("Sanity Test Failed! Xml Question "
                            + question.getFullName()
                            + " is not represented in the sanity test\n");
                    questionsFailed++;
                }
            }
        }
    }

    /**
     * Runs each query provided in the sanity test model (which is also each
     * query in the wdk model). Compares the results returned by the query to
     * the expected range provided in the sanity model. The test fails if the
     * result is outside the expected range or if an exception is thrown.
     */
    private void queriesTest() {
        System.out.println("Sanity Test:  Checking queries\n");

        Reference queryRef = null;
        SanityQuery queries[] = sanityModel.getAllSanityQueries();

        for (int i = 0; i < queries.length; i++) {
            try {
                // get model query from sanity query
                queryRef = new Reference(queries[i].getRef());
                QuerySet nextQuerySet = wdkModel.getQuerySet(queryRef.getSetName());
                Query nextQuery = nextQuerySet.getQuery(queryRef.getElementName());

                // run query
                QueryTester queryTester = new QueryTester(wdkModel);
                ResultList rs = queryTester.getResult(queryRef.getSetName(),
                        queryRef.getElementName(), queries[i].getParamHash());

                // count results; check if sane
                int sanityMin = queries[i].getMinOutputLength().intValue();
                int sanityMax = queries[i].getMaxOutputLength().intValue();
                int counter = 0;

                while (rs.next()) {
                    counter++;
                }
                // rs.close();
                if (counter < sanityMin || counter > sanityMax) {
                    System.out.println(BANNER_LINE);
                    System.out.println("***QUERY " + queryRef.getSetName()
                            + "." + queryRef.getElementName()
                            + " FAILED!***  It returned " + counter
                            + " rows--not within expected range (" + sanityMin
                            + " - " + sanityMax + ")");
                    printFailureMessage(queries[i]);
                    System.out.println(BANNER_LINE + "\n");
                    queriesFailed++;
                } else {
                    System.out.println("Query " + queryRef.getSetName() + "."
                            + queryRef.getElementName() + " passed--returned "
                            + counter + " rows, within expected range ("
                            + sanityMin + " - " + sanityMax + ")\n");
                    queriesPassed++;
                }
            } catch (Exception e) {
                queriesFailed++;
                System.out.println(BANNER_LINE);
                System.out.println("***QUERY " + queryRef.getSetName() + "."
                        + queryRef.getElementName()
                        + " FAILED!***  It threw an exception.");
                printFailureMessage(queries[i]);
                System.out.println(BANNER_LINE + "\n");
            }
        }
    }

    /**
     * Processes each RecordClass (by simply calling its print method, which
     * exercises all of the queries within that recordClass) provided in the
     * sanity test. The test fails if an exception is thrown.
     * 
     */
    private void recordsTest() {

        System.out.println("Sanity Test:  Checking records\n");

        Reference recordRef = null;
        SanityRecord records[] = sanityModel.getAllSanityRecords();

        for (int i = 0; i < records.length; i++) {

            try {
                recordRef = new Reference(records[i].getRef());
                RecordClassSet nextRecordClassSet = wdkModel.getRecordClassSet(recordRef.getSetName());
                RecordClass nextRecordClass = nextRecordClassSet.getRecordClass(recordRef.getElementName());
                RecordInstance nextRecordInstance = nextRecordClass.makeRecordInstance();

                nextRecordInstance.setPrimaryKey(records[i].getProjectID(),
                        records[i].getPrimaryKey());

                String riString = nextRecordInstance.print();
                System.out.println("Record " + recordRef.getSetName() + "."
                        + recordRef.getElementName() + " passed\n");
                if (verbose) System.out.println(riString + "\n");
                recordsPassed++;
            } catch (Exception wme) {
                recordsFailed++;
                System.out.println(BANNER_LINE);
                System.out.println("***RECORD " + recordRef.getSetName() + "."
                        + recordRef.getElementName() + " FAILED!***");
                printFailureMessage(records[i]);
                System.out.println(BANNER_LINE + "\n");
            }
        }
    }

    /**
     * Runs each question provided in the sanity test model (which is also each
     * query in the wdk model). Compares the results returned by the question to
     * the expected range provided in the sanity model. The test fails if the
     * result is outside the expected range or if an exception is thrown.
     */
    private void questionsTest() {
        System.out.println("Sanity Test:  Checking questions\n");

        Reference questionRef = null;
        SanityQuestion questions[] = sanityModel.getAllSanityQuestions();

        for (int i = 0; i < questions.length; i++) {
            try {
                // get model question from sanity question
                questionRef = new Reference(questions[i].getRef());
                QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
                Question question = questionSet.getQuestion(questionRef.getElementName());

                // run question
                Answer answer = question.makeAnswer(
                        questions[i].getParamHash(),
                        questions[i].getPageStart(), questions[i].getPageEnd());

                int resultSize = answer.getResultSize();

                // count results; check if sane
                int sanityMin = questions[i].getMinOutputLength().intValue();
                int sanityMax = questions[i].getMaxOutputLength().intValue();

                if (resultSize < sanityMin || resultSize > sanityMax) {
                    System.out.println(BANNER_LINE);
                    System.out.println("***QUESTION "
                            + questionRef.getSetName() + "."
                            + questionRef.getElementName()
                            + " FAILED!***  It returned " + resultSize
                            + " rows--not within expected range (" + sanityMin
                            + " - " + sanityMax + ")");
                    printFailureMessage(questions[i]);
                    System.out.println(BANNER_LINE + "\n");
                    questionsFailed++;
                } else {
                    System.out.println("Question " + questionRef.getSetName()
                            + "." + questionRef.getElementName()
                            + " passed--returned " + resultSize
                            + " rows, within expected range (" + sanityMin
                            + " - " + sanityMax + ")\n");
                    questionsPassed++;
                }
            } catch (Exception e) {
                queriesFailed++;
                e.printStackTrace();
                System.out.println(BANNER_LINE);
                System.out.println("***QUESTION " + questionRef.getSetName()
                        + "." + questionRef.getElementName()
                        + " FAILED!***  It threw an exception.");
                printFailureMessage(questions[i]);
                System.out.println(BANNER_LINE + "\n");
            }
        }
    }

    /**
     * Runs each question provided in the sanity test model (which is also each
     * query in the wdk model). Compares the results returned by the question to
     * the expected range provided in the sanity model. The test fails if the
     * result is outside the expected range or if an exception is thrown.
     */
    private void xmlQuestionsTest() {
        System.out.println("Sanity Test:  Checking XML questions\n");

        Reference questionRef = null;
        SanityXmlQuestion questions[] = sanityModel.getSanityQuestions();

        for (int i = 0; i < questions.length; i++) {
            try {
                // get model question from sanity question
                questionRef = new Reference(questions[i].getRef());
                XmlQuestionSet questionSet = wdkModel.getXmlQuestionSet(questionRef.getSetName());
                XmlQuestion question = questionSet.getQuestion(questionRef.getElementName());

                // if need to use external xml data source
                String xmlData = questions[i].getXmlData();
                if (xmlData != null) question.setXmlDataURL(xmlData);
                
                // run question
                XmlAnswer answer = question.makeAnswer(
                        null,
                        questions[i].getPageStart(), questions[i].getPageEnd());

                int resultSize = answer.getResultSize();

                // count results; check if sane
                int sanityMin = questions[i].getMinOutputLength();
                int sanityMax = questions[i].getMaxOutputLength();

                if (resultSize < sanityMin || resultSize > sanityMax) {
                    System.out.println(BANNER_LINE);
                    System.out.println("***XML QUESTION "
                            + questionRef.getSetName() + "."
                            + questionRef.getElementName()
                            + " FAILED!***  It returned " + resultSize
                            + " rows--not within expected range (" + sanityMin
                            + " - " + sanityMax + ")");
                    printFailureMessage(questions[i]);
                    System.out.println(BANNER_LINE + "\n");
                    questionsFailed++;
                } else {
                    System.out.println("XmlQuestion "
                            + questionRef.getSetName() + "."
                            + questionRef.getElementName()
                            + " passed--returned " + resultSize
                            + " rows, within expected range (" + sanityMin
                            + " - " + sanityMax + ")\n");
                    questionsPassed++;
                }
            } catch (Exception e) {
                queriesFailed++;
                System.out.println(BANNER_LINE);
                System.out.println("***QUESTION " + questionRef.getSetName()
                        + "." + questionRef.getElementName()
                        + " FAILED!***  It threw an exception.");
                printFailureMessage(questions[i]);
                System.out.println(BANNER_LINE + "\n");
            }
        }
    }

    /**
     * Prints out a command to run so the user can test failures outside of the
     * sanity test.
     */
    private void printFailureMessage(SanityElementI element) {
        try {
            StringBuffer message = new StringBuffer("To test "
                    + element.getType() + " " + element.getName()
                    + ", run the following command: \n ");

            String globalArgs = "-model " + modelName;
            String command = element.getCommand(globalArgs);
            message.append(command);

            System.out.println(message.toString());

        } catch (Exception e) {
            System.out.println("An error occurred when attempting to create a message explaining a previous error");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param queryResult a two-value array where the first entry is the number
     *        of queries that passed the test and the second is the number of
     *        queries that failed.
     * 
     * @param recordResult a two-value array where the first entry is the number
     *        of records that passed the test and the second is the number of
     *        records that failed.
     * 
     * @param return true if one or more tests failed; false otherwise.
     */

    private boolean printSummaryLine() {

        boolean failedOverall = (queriesFailed > 0 || recordsFailed > 0 || questionsFailed > 0);
        String result = failedOverall ? "FAILED" : "PASSED";

        StringBuffer resultLine = new StringBuffer(
                "***Sanity test summary***\n");
        resultLine.append(queriesPassed + " queries passed, " + queriesFailed
                + " queries failed\n");
        resultLine.append(recordsPassed + " records passed, " + recordsFailed
                + " records failed\n");
        resultLine.append(questionsPassed + " questions passed, "
                + questionsFailed + " questions failed\n");
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
        addOption(
                options,
                "model",
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml), the Model property file ($GUS_HOME/config/model_name.prop), the Sanity Test file ($GUS_HOME/config/model_name-sanity.xml) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

        // verbose
        Option verbose = new Option("verbose",
                "Print out more information while running test.");
        options.addOption(verbose);

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
        String cmdlineSyntax = cmdName + " -model model_name" + " -verbose";

        String header = newline
                + "Run a test on all queries and records in a wdk model, using a provided sanity model, to ensure that the course of development hasn't dramatically affected wdk functionality."
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

    static Logger logger = Logger.getRootLogger();

    public static void main(String[] args) {

	BasicConfigurator.configure(); // logger
	logger.setLevel(Level.ERROR);

        String cmdName = System.getProperties().getProperty("cmdName");
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        File configDir = new File(System.getProperties().getProperty(
                "configDir"));
        File sanityXmlFile = new File(configDir, modelName + "-sanity.xml");
        File modelPropFile = new File(configDir, modelName + ".prop");

        boolean verbose = cmdLine.hasOption("verbose");

        try {
            File sanitySchemaFile = new File(
                    System.getProperty("sanitySchemaFile"));

            SanityModel sanityModel = SanityTestXmlParser.parseXmlFile(
                    sanityXmlFile.toURL(), modelPropFile.toURL(),
                    sanitySchemaFile.toURL());

            sanityModel.validateQueries();
            sanityModel.validateQuestions();

            SanityTester sanityTester = new SanityTester(modelName,
                    sanityModel, verbose);

            sanityTester.existenceTest();
            sanityTester.queriesTest();
            sanityTester.recordsTest();
            sanityTester.questionsTest();
            sanityTester.xmlQuestionsTest();

            if (verbose) System.out.println(sanityModel.toString());
            if (sanityTester.printSummaryLine()) {
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
