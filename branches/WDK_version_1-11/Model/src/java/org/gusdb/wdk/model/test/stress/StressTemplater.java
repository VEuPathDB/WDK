package org.gusdb.wdk.model.test.stress;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;

public class StressTemplater {

    WdkModel wdkModel;

    public StressTemplater(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public WdkModel getWdkModel() {
        return this.wdkModel;
    }
    
    public void createTemplate(File outFile) throws IOException, WdkModelException {
        PrintWriter out = new PrintWriter(new FileWriter(outFile));
        // save question template
        QuestionSet[] qsets = wdkModel.getAllQuestionSets();
        for (QuestionSet qset : qsets) {
            Question[] questions = qset.getQuestions();
            for (Question q : questions) {
                // save question full name
                out.println("Question: " + q.getFullName());
                // save parameters
                Param[] params = q.getParams();
                for (Param param : params) {
                    out.print("Param: " + param.getName() + " = ");
                    if (param instanceof FlatVocabParam) {
                        FlatVocabParam fparam = (FlatVocabParam) param;
                        String[] vocab = fparam.getVocab();
                        if (vocab.length > 0)
                        out.print(vocab[0]);
                        for (int i = 1; i < vocab.length; i++) {
                            out.print(", " + vocab[i]);
                        }
                    } else if (param.getDefault() != null) {
                        out.print(param.getDefault());
                    }
                    out.println();
                }
                out.println("//");
            }
        }
        
        // save records
        RecordClassSet[] rsets = wdkModel.getAllRecordClassSets();
        for (RecordClassSet rset : rsets) {
            RecordClass[] rclasses = rset.getRecordClasses();
            for (RecordClass rclass : rclasses) {
                // save record class full name
                out.println("Record: " + rclass.getFullName() + " = ");
                out.println("//");
            }
        }
        
        // save xml questions
        XmlQuestionSet[] xmlqsets = wdkModel.getXmlQuestionSets();
        for (XmlQuestionSet xmlqset : xmlqsets) {
            XmlQuestion[] xmlquestions = xmlqset.getQuestions();
            for (XmlQuestion xmlq : xmlquestions) {
                // save xml question full name
                out.println("XmlQuestion: " + xmlq.getFullName());
                out.println("//");
            }
        }
        out.flush();
        out.close();
    }

    // ////////////////////////////////////////////////////////////////////
    // /////////// static methods /////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        String cmdName = System.getProperties().getProperty("cmdName");
        File configDir = new File(System.getProperties().getProperty(
                "configDir"));

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        File modelConfigXmlFile = new File(configDir, modelName + "-config.xml");
        File modelXmlFile = new File(configDir, modelName + ".xml");
        File modelPropFile = new File(configDir, modelName + ".prop");

        try {
            // read config info
            File schemaFile = new File(System.getProperty("schemaFile"));
            File xmlSchemaFile = new File(System.getProperty("xmlSchemaFile"));
            WdkModel wdkModel = ModelXmlParser.parseXmlFile(
                    modelXmlFile.toURL(), modelPropFile.toURL(),
                    schemaFile.toURL(), xmlSchemaFile.toURL(),
                    modelConfigXmlFile.toURL());

            StressTemplater tester = new StressTemplater(wdkModel);
            
            // open the output file
            File outFile = new File(configDir, modelName + "-stress.template");
            tester.createTemplate(outFile);
            
         } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (WdkModelException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
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
        String cmdlineSyntax = cmdName + " -model model_name";
 
        String header = newline
                + "Preparing the template file for stress test"
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
