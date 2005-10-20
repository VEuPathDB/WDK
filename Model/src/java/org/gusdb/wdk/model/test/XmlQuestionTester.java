package org.gusdb.wdk.model.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.xml.XmlAnswer;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;

public class XmlQuestionTester {

    public static void main(String[] args) {

        String cmdName = System.getProperties().getProperty("cmdName");

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper.parseOptions(cmdName, options, args);

        String questionFullName = cmdLine.getOptionValue("question");
        String[] rows = cmdLine.getOptionValues("rows");
        String xmlData = null;
        if (cmdLine.hasOption("xmlData"))
            xmlData = cmdLine.getOptionValue("xmlData");

        validateRowCount(rows);

        try {
            String modelName = cmdLine.getOptionValue("model");
            File configDir = new File(System.getProperties().getProperty(
                    "configDir"));

            File modelConfigXmlFile = new File(configDir, modelName
                    + "-config.xml");
            File modelXmlFile = new File(configDir, modelName + ".xml");
            File modelPropFile = new File(configDir, modelName + ".prop");

            File schemaFile = new File(System.getProperty("schemaFile"));
            File xmlSchemaFile = new File(System.getProperty("xmlSchemaFile"));

            Reference ref = new Reference(questionFullName);
            String questionSetName = ref.getSetName();
            String questionName = ref.getElementName();

            WdkModel wdkModel = ModelXmlParser.parseXmlFile(
                    modelXmlFile.toURL(), modelPropFile.toURL(),
                    schemaFile.toURL(), xmlSchemaFile.toURL(),
                    modelConfigXmlFile.toURL());
            
            // use the config dir as Xml data path
            File xmlDataPath = new File(System.getProperty("xmlDataDir"));
            wdkModel.setXmlDataPath(xmlDataPath);

            XmlQuestionSet questionSet = wdkModel.getXmlQuestionSet(questionSetName);
            XmlQuestion question = questionSet.getQuestion(questionName);
            
            // use external data source
            if (xmlData != null) question.setXmlDataURL(xmlData);
            
            int pageCount = 1;

            for (int i = 0; i < rows.length; i += 2) {
                int nextStartRow = Integer.parseInt(rows[i]);
                int nextEndRow = Integer.parseInt(rows[i + 1]);

                XmlAnswer answer = question.makeAnswer(null, nextStartRow,
                        nextEndRow);

                System.out.println("Printing Record Instances on page "
                        + pageCount);
                System.out.println(answer.print());
                pageCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Options declareOptions() {
        String[] names = { "model", "question", "rows", "xmlData" };
        String[] descs = {
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)",
                "The full name (set.element) of the question to run.",
                "The start and end pairs of the summary rows to return",
                "(Optional) The URL to the xml data source" };
        boolean[] required = { true, true, true, false };
        int[] args = { 0, 0, Option.UNLIMITED_VALUES, 0 };

        return CommandHelper.declareOptions(names, descs, required, args);
    }

    static void validateRowCount(String[] rows) {
        if (rows.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "The -rows option must be followed by pairs of row numbers (each pair representing the start and end of a page");
        }
    }
}