package org.gusdb.wdk.model.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.xml.sax.SAXException;

public class RecordTester {

    // ////////////////////////////////////////////////////////////////////
    // /////////// static methods /////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////

    public static void main(String[] args)
            throws WdkModelException, WdkUserException {
        String cmdName = System.getProperty("cmdName");
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        String recordClassFullName = cmdLine.getOptionValue("record");

        String projectID = null;
        if (cmdLine.hasOption("project"))
            projectID = cmdLine.getOptionValue("project");
        String primaryKey = cmdLine.getOptionValue("primaryKey");
        boolean timeQueries = cmdLine.hasOption("timeQueries");

        long start = System.currentTimeMillis();

        try {
            long st = System.currentTimeMillis();

            Reference ref = new Reference(recordClassFullName);
            String recordClassSetName = ref.getSetName();
            String recordClassName = ref.getElementName();

            ModelXmlParser parser = new ModelXmlParser(gusHome);
            WdkModel wdkModel = parser.parseModel(modelName);

            System.out.println("Model initialization took: "
                    + ((System.currentTimeMillis() - st) / 1000F) + " seconds.");
            st = System.currentTimeMillis();

            RecordClassSet recordClassSet = wdkModel.getRecordClassSet(recordClassSetName);
            RecordClass recordClass = recordClassSet.getRecordClass(recordClassName);
            RecordInstance recordInstance = recordClass.makeRecordInstance(
                    projectID, primaryKey);

            System.out.println("Record creation took: "
                    + ((System.currentTimeMillis() - st) / 1000F) + " seconds.");
            st = System.currentTimeMillis();

            if (timeQueries) {
                recordInstance.setTimeAttributeQueries(true);
            }
            System.out.println(recordInstance.print());

            System.out.println("Fields retrieval took: "
                    + ((System.currentTimeMillis() - st) / 1000F) + " seconds.");
        } catch (SAXException ex) {
            throw new WdkModelException(ex);
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } catch (ParserConfigurationException ex) {
            throw new WdkModelException(ex);
        } catch (TransformerFactoryConfigurationError ex) {
            throw new WdkModelException(ex);
        } catch (TransformerException ex) {
            throw new WdkModelException(ex);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Total time spent: " + ((end - start) / 1000F)
                    + " seconds.");
        }
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

        // by Jerric - project ID
        // use cache
        Option project = new Option("project", true,
                "The project ID of the record, if project IDs are used.");
        options.addOption(project);

        // primary key
        addOption(options, "primaryKey",
                "The primary key of the record to find.");

        OptionGroup specialOperations = new OptionGroup();
        Option timeQueries = new Option("timeQueries",
                "print how long it takes for queries to return.");
        specialOperations.addOption(timeQueries);
        options.addOptionGroup(specialOperations);

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
                + " -record full_record_name" + " -timeQueries "
                + " -primaryKey primary_key" + " [-project project_id]";

        String header = newline
                + "Print a record found in a WDK Model xml file. Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
