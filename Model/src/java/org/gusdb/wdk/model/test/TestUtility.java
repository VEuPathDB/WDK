/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Aug 29, 2005
 */
public class TestUtility {

    protected static TestUtility utility;

    protected WdkModel wdkModel;
    protected SanityModel sanityModel;
    protected Random rand;

    public static void main(String[] args) {
        String cmdName = System.getProperty("cmdName");

        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        if (!cmdLine.hasOption("model")) {
            usage(cmdName, options);
        }

        if (!cmdLine.hasOption("testCase")) {
            // no test case assigned, run all tests
            TestRunner.run(suite());
        } else {
            String testCase = cmdLine.getOptionValue("testCase");
            if (testCase.equalsIgnoreCase("JUnitUserTest")) {
                TestRunner.run(JUnitUserTest.suite());
            } else if (testCase.equalsIgnoreCase("JUnitBooleanExpressionTest")) {
                TestRunner.run(JUnitBooleanExpressionTest.suite());
            } else { // unknow test cases
                System.err.println("Unknown test case: " + testCase);
            }
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(JUnitBooleanExpressionTest.suite());
        suite.addTest(JUnitUserTest.suite());
        return suite;
    }

    public static TestUtility getInstance()
            throws WdkModelException, MalformedURLException {
        if (utility == null) utility = new TestUtility();
        return utility;
    }

    /**
     * @throws WdkModelException
     * @throws MalformedURLException
     * 
     */
    public TestUtility() throws WdkModelException, MalformedURLException {
        super();
        wdkModel = loadWdkModel();
        sanityModel = loadSanityModel();
        rand = new Random(System.currentTimeMillis());
    }

    /**
     * @return Returns the rand.
     */
    public Random getRandom() {
        return this.rand;
    }

    /**
     * @return Returns the sanityModel.
     */
    public SanityModel getSanityModel() {
        return this.sanityModel;
    }

    /**
     * @return Returns the wdkModel.
     */
    public WdkModel getWdkModel() {
        return this.wdkModel;
    }

    private WdkModel loadWdkModel() throws WdkModelException {
        // load wdk model
        String modelName = System.getProperty("model");
        return WdkModel.construct(modelName);
    }

    private SanityModel loadSanityModel()
            throws MalformedURLException, WdkModelException {
        String modelName = System.getProperty("model");
        File configDir = new File(System.getProperties().getProperty(
                "configDir"));
        File sanityXmlFile = new File(configDir, modelName + "-sanity.xml");
        File modelPropFile = new File(configDir, modelName + ".prop");
        File sanitySchemaFile = new File(System.getProperty("sanitySchemaFile"));

        SanityModel sanityModel = SanityTestXmlParser.parseXmlFile(
                sanityXmlFile.toURL(), modelPropFile.toURL(),
                sanitySchemaFile.toURL());
        return sanityModel;
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
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static Options declareOptions(Map<String, String> optionDefs) {
        Options options = new Options();

        // model name
        for (String optionName : optionDefs.keySet()) {
            String optionDesc = optionDefs.get(optionName);
            addOption(options, optionName, optionDesc);
        }

        // verbose
        Option verbose = new Option("verbose",
                "Print out more information while running test.");
        options.addOption(verbose);

        return options;
    }

    private static void addOption(Options options, String argName, String desc) {

        Option option = new Option(argName, true, desc);
        option.setRequired(true);
        option.setArgName(argName);
        options.addOption(option);
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");

        StringBuffer sb = new StringBuffer();

        // add command name
        sb.append(cmdName);

        // add command syntax
        for (Object objOption : options.getOptions()) {
            Option option = (Option) objOption;
            if (option.isRequired()) sb.append(" -");
            else sb.append(" [-");
            sb.append(option.getOpt());
            sb.append(' ');
            sb.append(option.getOpt());
            if (!option.isRequired()) sb.append(']');
        }

        String cmdlineSyntax = sb.toString();

        String header = "Run Unit test cases. Options:";

        String footer = " ";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

    private static Options declareOptions() {
        Options options = new Options();

        // model name
        addOption(
                options,
                "model",
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

        // test case
        Option testCase = new Option("testCase", true,
                "(Optional) The specific test case to be executed. ");
        testCase.setArgName("testCase");
        options.addOption(testCase);

        return options;
    }
}
