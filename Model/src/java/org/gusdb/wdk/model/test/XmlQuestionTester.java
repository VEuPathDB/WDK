package org.gusdb.wdk.model.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.xml.XmlAnswer;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.xml.sax.SAXException;

public class XmlQuestionTester {

    static Logger logger = Logger.getRootLogger();

    public static void main(String[] args) throws WdkModelException {
        String cmdName = System.getProperty("cmdName");
        String gusHome = System.getProperty(ModelXmlParser.GUS_HOME);

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = CommandHelper.parseOptions(cmdName, options, args);

        String questionFullName = cmdLine.getOptionValue("question");
        String[] rows = cmdLine.getOptionValues("rows");
        String xmlData = null;
        if (cmdLine.hasOption("basename"))
            xmlData = cmdLine.getOptionValue("basename");

        validateRowCount(rows);

        String modelName = cmdLine.getOptionValue("model");

        Reference ref = new Reference(questionFullName);
        String questionSetName = ref.getSetName();
        String questionName = ref.getElementName();

        try {
            ModelXmlParser parser = new ModelXmlParser(gusHome);
            WdkModel wdkModel = parser.parseModel(modelName);

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
        }
    }

    private static Options declareOptions() {
        String[] names = { "model", "question", "rows", "basename" };
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
