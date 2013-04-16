package org.gusdb.wdk.model.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
 *         Provides a unified command line interface for writing the results of
 *         a Reporter to files.
 */
public class Dumper {

    // private final static Logger logger = Logger.getLogger(Dumper.class);
    //
    // private static final String DEFAULT_FIELD_FORMAT = "text";

    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");

        // process arguments
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        // get arguments
        String modelName = cmdLine.getOptionValue("model");
        String baseDir = cmdLine.getOptionValue("dir");
        String reporterName = cmdLine.getOptionValue("reporter");
        String questionName = cmdLine.getOptionValue("question");
        String outputFileName = cmdLine.getOptionValue("fileName");
        String[] questionParams = new String[0];
        if (cmdLine.hasOption("params"))
            questionParams = cmdLine.getOptionValues("params");
        String[] reporterConfig = new String[0];
        if (cmdLine.hasOption("config"))
            reporterConfig = cmdLine.getOptionValues("config");

        if (baseDir == null || baseDir.length() == 0) baseDir = ".";
        File dir = new File(baseDir);
        if (!dir.exists() || !dir.isDirectory()) dir.mkdirs();

        // create the file for output
        File outputFile = new File(dir, outputFileName);
        OutputStream out = new FileOutputStream(outputFile);

        // construct wdkModel
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        WdkModel wdkModel = WdkModel.construct(modelName, gusHome);
        User user = wdkModel.getSystemUser();

        // load selected question
        Question question = (Question) wdkModel.resolveReference(questionName);

        // prepare parameters
        Map<String, String> params = parseListArgs("params", questionParams);
        fillInParams(params, question);

        // load config
        Map<String, String> config = parseListArgs("config", reporterConfig);

        // Get the reporter
        AnswerValue answer = question.makeAnswerValue(user, params, true, 0);
        Reporter reporter = answer.createReport(reporterName, config);

        try {
            // initialize the reporter
            reporter.initialize();

            // write the reporter
            reporter.write(out);
        } finally {
            // complete the reporter
            reporter.complete();
            // flush the output stream
            out.flush();
            out.close();
        }
    }

    static void fillInParams(Map<String, String> params, Question question)
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

    static void addOption(Options options, String argName, boolean hasArg,
            String desc, boolean required) {
        Option option = new Option(argName, hasArg, desc);
        option.setRequired(required);
        option.setArgName(argName);
        options.addOption(option);
    }

    static void addListOption(Options options, String argName, String desc,
            boolean required) {
        Option option = new Option(argName, false, desc);
        option.setArgName(argName);
        option.setArgs(Option.UNLIMITED_VALUES);
        option.setRequired(required);
        options.addOption(option);
    }

    static Options declareOptions() {
        Options options = new Options();

        // model name
        addOption(options, "model", true, "The name of the model.  "
                + "This is used to find the "
                + "Model XML file ($GUS_HOME/config/model_name.xml) "
                + "the Model property file "
                + "($GUS_HOME/config/model_name.prop) "
                + "and the Model config file "
                + "($GUS_HOME/config/model_name-config.xml)", true);

        // query name
        addOption(options, "question", true,
                "The full name (set.element) of the question to run.", true);

        // reporter type
        addOption(options, "reporter", true, "The type of report to generate.",
                true);

        // output file name
        addOption(options, "fileName", true,
                "The name to use for the raw output file.", true);

        // base directory
        addOption(
                options,
                "dir",
                true,
                "The base directory for writing the report output.  If no directory is specified, the current directory is used.",
                false);

        // params
        addListOption(options, "params",
                "space delimited list of param_name param_value ....", false);

        // reporter config
        addListOption(options, "config",
                "space delimited list of property_name property_value ....",
                false);

        return options;
    }

    private static void usage(String cmdName, Options options) {
        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -model model_name"
                + " -question full_question_name" + " -dir base_dir"
                + " -type report_type"
                + " [-params param_1_name param_1_value ...]"
                + " [-config property_1_name property_1_value ...]";

        String header = newline
                + "Run a reporter from the command line, and dump output to files."
                + newline + newline + "Options:";

        String footer = " ";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cmdlineSyntax, header, options, footer);
        System.exit(1);
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

    static Map<String, String> parseListArgs(String argName,
            String[] inputValues) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Map<String, String> argValues = new LinkedHashMap<String, String>();

        if (inputValues.length % 2 != 0) {
            throw new IllegalArgumentException("The -" + argName
                    + " option must be followed by key value pairs only");
        }
        for (int i = 0; i < inputValues.length; i += 2) {
            argValues.put(inputValues[i], inputValues[i + 1]);
        }

        return argValues;
    }
}
