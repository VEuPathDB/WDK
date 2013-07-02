package org.gusdb.wdk.model.report;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * @author Charles Treatman
 *
 * Prints the properties and config options
 * used by a Reporter to the console
 */
public class DumperHelp {

    public static void main(String[] args) throws Exception {
	String cmdName = System.getProperty("cmdName");

	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);

	String modelName = cmdLine.getOptionValue("model");
	String reporterName = cmdLine.getOptionValue("reporter");
	String questionName = cmdLine.getOptionValue("question");

	String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
	WdkModel wdkModel = WdkModel.construct(modelName, gusHome);
	User user = wdkModel.getSystemUser();

	Question question = (Question) wdkModel.resolveReference(questionName);

	Map<String, String> params = new LinkedHashMap<String,String>();
	fillInParams(params, question);

	Map<String, String> emptyConfig = new LinkedHashMap<String,String>();

	AnswerValue answer = question.makeAnswerValue(user, params, true, 0);
	Reporter reporter = answer.createReport(reporterName, emptyConfig);

	System.out.println("Help for reporter: " + reporterName);
	System.out.println();
	System.out.println(reporter.getDescription());
	System.out.println();
	System.out.println("Properties: ");
	System.out.println(reporter.getPropertyInfo());
	System.out.println("Config options: ");
	System.out.println(reporter.getConfigInfo());
	System.out.println();
    }

    static void fillInParams(Map<String,String> params, Question question)
	throws WdkModelException, NoSuchAlgorithmException, SQLException,
	       JSONException, WdkUserException {
	Query query = question.getQuery();
        if (!query.getParamValuesSets().isEmpty()) {
            ParamValuesSet pvs = query.getParamValuesSets().get(0);
            Map<String, String> map = pvs.getParamValues();
            for (String paramName : map.keySet()) {
                if (!params.containsKey(paramName)) {
                    params.put(paramName, map.get(paramName));
                }
            }
        }
    }

    static Options declareOptions() {
        Options options = new Options();

        // model name
        addOption(options, "model", true,
                "The name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)", true);

        // query name
        addOption(options, "question", true,
                "The full name (set.element) of the question to run.", true);

        // reporter type
        addOption(options, "reporter", true,
                "The type of report to generate.", true);

        return options;
    }

    static void addOption(Options options, String argName,
            boolean hasArg, String desc, boolean required) {
        Option option = new Option(argName, hasArg, desc);
        option.setRequired(required);
        option.setArgName(argName);
        options.addOption(option);
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

    private static void usage(String cmdName, Options options) {
        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -model model_name"
	    + " -question full_question_name"
	    + " -reporter reporter_name";

        String header = newline
                + "Prints the properties and config options for a reporter to the console."
                + newline + newline + "Options:";

        String footer = " ";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
