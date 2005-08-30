/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Random;

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

    public static TestUtility getInstance() throws WdkModelException,
            MalformedURLException {
        if (utility == null) utility = new TestUtility();
        return utility;
    }

    /**
     * @throws WdkModelException
     * @throws MalformedURLException
     * 
     */
    private TestUtility() throws WdkModelException, MalformedURLException {
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

    private SanityModel loadSanityModel() throws MalformedURLException,
            WdkModelException {
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
            sb.append(option.getArgName());
            sb.append(' ');
            sb.append(option.getValue());
            if (!option.isRequired()) sb.append(']');
        }

        String cmdlineSyntax = sb.toString();

        String header = newline
                + "Print a record found in a WDK Model xml file. Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

}
