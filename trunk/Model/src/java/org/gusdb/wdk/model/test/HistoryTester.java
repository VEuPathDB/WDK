package org.gusdb.wdk.model.test;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * HistoryTester.java " -model modelName" +
 * 
 * 
 * Main class for running the history tests, which is a way to test all users
 * and its history answers/questions.
 * 
 * Created: Aug. 20, 2005
 * 
 * @author Jerric Gao
 * @version 1.7.0.0
 */
public class HistoryTester {

    int usersPassed = 0;
    int usersFailed = 0;

    WdkModel wdkModel;
    boolean verbose;
    HistoryModel historyModel;

    public static final String BANNER_LINE = "***********************************************************";

    public HistoryTester(String modelName, HistoryModel historyModel,
            boolean verbose) throws WdkModelException {
        this.wdkModel = WdkModel.construct(modelName);
        this.historyModel = historyModel;
        this.verbose = verbose;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    /**
     * Checks to make sure every User and its historical Answers/Questions are
     * working
     */
    private void existenceTest() {
        // TEST
        System.out.println("========== History Test: Checking historical questions...");

        // get sanity user
        SanityUser[] users = historyModel.getSanityUsers();
        for (SanityUser user : users) {
            // get sanity user question
            SanityUserAnswer[] answers = user.getUserAnswers();
            for (SanityUserAnswer answer : answers) {
                // get the question associated with this answer
                SanityQuestion sanityQuestion = answer.getSanityQuestion();

                // loop on the questions in the model to see if it matches
                boolean match = false;
                QuestionSet[] questionSets = wdkModel.getAllQuestionSets();
                for (QuestionSet questionSet : questionSets) {
                    Question[] questions = questionSet.getQuestions();
                    for (Question question : questions) {
                        if (question.getFullName().equalsIgnoreCase(
                                sanityQuestion.getName())) {
                            match = true;
                            break;
                        }
                    }
                    if (match) break;
                }
                if (!match)
                    System.err.println("History Test failed: Question "
                            + user.getUserID() + "." + answer.getName() + "."
                            + sanityQuestion.getName()
                            + " is not presented in the model!");
            }
        }
    }

    private void userTest() {
        System.out.println("========== History Test: Creating users...");

        // create users test
        SanityUser[] sanityUsers = historyModel.getSanityUsers();
        for (SanityUser sanityUser : sanityUsers) {
            // create user
            User user = wdkModel.createUser(sanityUser.getUserID());

            // check if the user's history failed running
            boolean result = executeHistory(user, sanityUser);
            if (result) usersPassed++;
            else usersFailed++;
        }

        // TEST
        System.out.println("========== History Test: Getting users...");

        // get user test
        for (SanityUser sanityUser : sanityUsers) {
            User user = wdkModel.getUser(sanityUser.getUserID());

            // check if the user's history failed running
            boolean result = executeHistory(user, sanityUser);
            if (result) usersPassed++;
            else usersFailed++;
        }

        // TEST
        System.out.println("========== History Test: Deleting users...");

        // delete user test
        for (SanityUser sanityUser : sanityUsers) {
            boolean result = wdkModel.deleteUser(sanityUser.getUserID());
            if (result) usersPassed++;
            else usersFailed++;
        }
    }

    private boolean executeHistory(User user, SanityUser sanityUser) {
        System.out.println("Now testing on user: " + user.getUserID());

        boolean isUserFailed = false;

        // create user answers
        SanityUserAnswer[] sanityAnswers = sanityUser.getUserAnswers();
        for (SanityUserAnswer sanityAnswer : sanityAnswers) {
            // get question and execute it
            SanityQuestion sanityQuestion = sanityAnswer.getSanityQuestion();
            // get model question from sanity question
            try {
                Reference questionRef = new Reference(sanityQuestion.getRef());
                QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
                Question question = questionSet.getQuestion(questionRef.getElementName());

                // run question
                Answer answer = question.makeAnswer(
                        sanityQuestion.getParamHash(),
                        sanityQuestion.getPageStart(),
                        sanityQuestion.getPageEnd());

                // create UserAnswer
                user.addAnswer(answer);

                // check condition
                int resultSize = answer.getResultSize();

                // count results; check if sane
                int sanityMin = sanityQuestion.getMinOutputLength().intValue();
                int sanityMax = sanityQuestion.getMaxOutputLength().intValue();

                if (resultSize < sanityMin || resultSize > sanityMax) {
                    System.out.println(BANNER_LINE);
                    System.out.println("***QUESTION " + user.getUserID() + "."
                            + sanityAnswer.getName() + "."
                            + sanityQuestion.getName()
                            + " FAILED!***  It returned " + resultSize
                            + " rows--not within expected range (" + sanityMin
                            + " - " + sanityMax + ")");
                    printFailureMessage(sanityQuestion);
                    System.out.println(BANNER_LINE + "\n");
                    isUserFailed = true;
                    break;
                } else {
                    System.out.println("Question " + user.getUserID() + "."
                            + sanityAnswer.getName() + "."
                            + sanityQuestion.getName() + " passed--returned "
                            + resultSize + " rows, within expected range ("
                            + sanityMin + " - " + sanityMax + ")\n");
                }

            } catch (WdkModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // System.err.println(e);
            } catch (WdkUserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // System.err.println(e);
            }
        }
        if (sanityAnswers.length == 0) isUserFailed = false;
        return !isUserFailed;
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

            String globalArgs = "-model " + wdkModel.getName();
            String command = element.getCommand(globalArgs);
            message.append(command);

            System.out.println(message.toString());

        } catch (Exception e) {
            System.out.println("An error occurred when attempting to create a "
                    + "message explaining a previous error");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean printSummaryLine() {

        boolean failedOverall = (usersFailed > 0);
        String result = failedOverall ? "FAILED" : "PASSED";

        StringBuffer resultLine = new StringBuffer(
                "***history test summary***\n");
        resultLine.append(usersPassed + " users passed, " + usersFailed
                + " users failed\n");
        resultLine.append("History Test " + result + "\n");
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
        addOption(options, "model",
                "the name of the model.  This is used to find the Model XML "
                        + "file ($GUS_HOME/config/model_name.xml), the Model "
                        + "property file ($GUS_HOME/config/model_name.prop), "
                        + "the History Test file "
                        + "($GUS_HOME/config/model_name-history.xml) "
                        + "and the Model config file "
                        + "($GUS_HOME/config/model_name-config.xml)");

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
                + "Run a test on all users and their historical "
                + "answers/questions in runtime, using a provided sanity model, "
                + "to ensure that the course of development hasn't dramatically "
                + "affected wdk functionality." + newline + newline
                + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

    public static void main(String[] args) {

        String cmdName = System.getProperties().getProperty("cmdName");
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        File configDir = new File(System.getProperties().getProperty(
                "configDir"));
        File historyXmlFile = new File(configDir, modelName + "-history.xml");
        File modelPropFile = new File(configDir, modelName + ".prop");

        boolean verbose = cmdLine.hasOption("verbose");

        try {
            File historySchemaFile = new File(
                    System.getProperty("historySchemaFile"));

            HistoryModel historyModel = HistoryTestXmlParser.parseXmlFile(
                    historyXmlFile.toURL(), modelPropFile.toURL(),
                    historySchemaFile.toURL());

            HistoryTester sanityTester = new HistoryTester(modelName,
                    historyModel, verbose);

            sanityTester.existenceTest();
            sanityTester.userTest();

            if (verbose) System.out.println(historyModel.toString());
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
