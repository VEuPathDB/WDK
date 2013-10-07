package org.gusdb.wdk.model.test;

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
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.User;

public class RecordTester {

    // ////////////////////////////////////////////////////////////////////
    // /////////// static methods /////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws WdkModelException,
            WdkUserException {
        String cmdName = System.getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        // get arguments
        String modelName = cmdLine.getOptionValue("model");
        String recordClassFullName = cmdLine.getOptionValue("record");
        String[] primaryKeyArray = cmdLine.getOptionValues("primaryKey");

        long start = System.currentTimeMillis();
        long st = System.currentTimeMillis();

        // initialize the model
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        WdkModel wdkModel = WdkModel.construct(modelName, gusHome);

        System.out.println("Model initialization took: "
                + ((System.currentTimeMillis() - st) / 1000F) + " seconds.");
        st = System.currentTimeMillis();

        // create instance
        RecordClass recordClass = (RecordClass) wdkModel.resolveReference(recordClassFullName);
        Map<String, Object> pkValues = parsePrimaryKeyArgs(primaryKeyArray);
        User user = wdkModel.getSystemUser();
        RecordInstance recordInstance = new RecordInstance(user, recordClass,
                pkValues);

        // try to get all attributes
        recordInstance.getAttributeValueMap();

        // try to get all tables
        recordInstance.getTables();

        System.out.println("Record creation took: "
                + ((System.currentTimeMillis() - st) / 1000F) + " seconds.");
        st = System.currentTimeMillis();

        System.out.println(recordInstance.print());

        System.out.println("Fields retrieval took: "
                + ((System.currentTimeMillis() - st) / 1000F) + " seconds.");
        long end = System.currentTimeMillis();
        System.out.println("Total time spent: " + ((end - start) / 1000F)
                + " seconds.");
        // System.exit(0);
    }

    private static Map<String, Object> parsePrimaryKeyArgs(String[] array) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        if (array.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "The -primaryKey option must be followed by column key value pairs only");
        }
        for (int i = 0; i < array.length; i += 2) {
            map.put(array[i], array[i + 1]);
        }
        return map;
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
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

        // record name
        addOption(options, "record",
                "The full name (set.element) of the record to print.");

        // primary key columns
        Option primaryKey = new Option("primaryKey", true,
                "space delimited list of column_name column_value ....");
        primaryKey.setArgName("primaryKey");
        primaryKey.setArgs(Option.UNLIMITED_VALUES);
        primaryKey.setRequired(true);
        options.addOption(primaryKey);

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
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -model model_name"
                + " -record full_record_name"
                + " -primaryKey column_name column_value...";

        String header = newline
                + "Print a record found in a WDK Model xml file. Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
