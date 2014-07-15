package org.gusdb.wdk.model.test.stress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;

public class StressTemplater {

    public class QuestionItem {

        private String questionName;
        private Map<String, Set<String>> params;

        public QuestionItem(String questionName) {
            this.questionName = questionName;
            this.params = new LinkedHashMap<String, Set<String>>();
        }

        public String getQuestionName() {
            return this.questionName;
        }

        public void addParamValue(String paramName, String paramValue) {
            Set<String> values = params.get(paramName);
            if (values == null) {
                values = new LinkedHashSet<String>();
                params.put(paramName, values);
            }
            if (paramValue != null && paramValue.length() > 0)
                values.add(paramValue);
        }

        public Map<String, Set<String>> getParams() {
            return this.params;
        }
    }

    public class RecordItem {

        private String recordName;
        private Set<String> recordIds;

        public RecordItem(String recordName) {
            this.recordName = recordName;
            this.recordIds = new LinkedHashSet<String>();
        }

        public String getRecordName() {
            return this.recordName;
        }

        public void addRecordId(String recordId) {
            recordIds.add(recordId);
        }

        public Set<String> getRecordIds() {
            return recordIds;
        }
    }

    private static final Logger logger = Logger.getLogger(StressTemplater.class);

    private WdkModel wdkModel;
//    private SanityModel sanityModel;

    private Map<String, QuestionItem> questionItems;
    private Map<String, RecordItem> recordItems;

    public StressTemplater(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
//        this.sanityModel = sanityModel;

        questionItems = new LinkedHashMap<String, QuestionItem>();
        recordItems = new LinkedHashMap<String, RecordItem>();
    }

    public void makeTemplate(File file) throws WdkModelException {
        // load the info from the model
        logger.info("Loading info from the model...");
        loadFromModel();

        // if the file exists, load the info from the file
        if (file.exists() && file.isFile() && file.canRead()) {
            logger.info("Loading info from the file...");
            loadFromFile(file);
        }

        // load the info from the sanity model
        logger.info("Loading info from the sanity model...");
//        loadFromSanityModel();

        // generate the template file
        logger.info("Generating template file...");
        generateTemplate(file);
    }

    private void loadFromModel() throws WdkModelException {
        // get questions
        QuestionSet[] qsets = wdkModel.getAllQuestionSets();
        for (QuestionSet qset : qsets) {
            // ignore the internal questions
            if (qset.isInternal()) {
                logger.info("Skip question set: " + qset.getName());
                continue;
            }

            Question[] questions = qset.getQuestions();
            for (Question question : questions) {
                String questionName = question.getFullName();
                QuestionItem questionItem = new QuestionItem(questionName);

                // list all params
                Param[] params = question.getParams();
                boolean unusable = false;
                for (Param param : params) {
                    if (param instanceof FlatVocabParam) {
                        // skip it
                    } else if (param instanceof AnswerParam
                            || param instanceof DatasetParam) {
                        // unusable question
                        unusable = true;
                        break;
                    } else { // add the param into the list
                        String paramName = param.getName();
                        String paramValue = param.getDefault();
                        questionItem.addParamValue(paramName, paramValue);
                    }
                }
                // add the question only if it has some params that requires
                // user's input
                if (!unusable && questionItem.getParams().size() > 0)
                    questionItems.put(questionName, questionItem);
            }
        }

        // get records
        RecordClassSet[] rcsets = wdkModel.getAllRecordClassSets();
        for (RecordClassSet rcset : rcsets) {
            RecordClass[] rcs = rcset.getRecordClasses();
            for (RecordClass recordClass : rcs) {
                String recordName = recordClass.getFullName();
                RecordItem recordItem = new RecordItem(recordName);
                recordItems.put(recordName, recordItem);
            }
        }
    }

    private void loadFromFile(File file) throws WdkModelException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = null;
            QuestionItem questionItem = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String lowerCase = line.toLowerCase();
                if (lowerCase.startsWith("question")) {
                    questionItem = null; // clear the old question
                    int pos = line.indexOf(":");
                    if (pos > 0) {
                        String questionName = line.substring(pos + 1).trim();
                        questionItem = questionItems.get(questionName);
                    }
                } else if (lowerCase.startsWith("param")) {
                    if (questionItem != null) {
                        int pos = line.indexOf(":");
                        if (pos > 0) {
                            line = line.substring(pos + 1).trim();
                            pos = line.indexOf("=");
                            if (pos > 0) {
                                String paramName = line.substring(0, pos).trim();
                                if (questionItem.getParams().containsKey(
                                        paramName)) {
                                    String valueString = line.substring(pos + 1).trim();
                                    String[] values = valueString.split(",");
                                    for (String value : values) {
                                        questionItem.addParamValue(paramName,
                                                value.trim());
                                    }
                                }
                            }
                        }
                    }
                } else if (lowerCase.startsWith("record")) {
                    int pos = line.indexOf(":");
                    if (pos > 0) {
                        line = line.substring(pos + 1).trim();
                        pos = line.indexOf("=");
                        if (pos > 0) {
                            String recordName = line.substring(0, pos).trim();
                            RecordItem recordItem = recordItems.get(recordName);

                            if (recordItem != null) {
                                String idString = line.substring(pos + 1).trim();
                                String[] ids = idString.split(",");
                                for (String id : ids) {
                                    recordItem.addRecordId(id);
                                }
                            }
                        }
                    }
                } // other line types are ignored
            }

            reader.close();
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        }
    }

//    private void loadFromSanityModel() {
//        // get sanity questions
//        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();
//        for (SanityQuestion question : questions) {
//            String questioName = question.getName();
//            QuestionItem questionItem = questionItems.get(questioName);
//            if (questionItem != null) {
//                Map<String, Object> params = question.getParamHash();
//                for (String paramName : params.keySet()) {
//                    if (questionItem.getParams().containsKey(paramName)) {
//                        String paramValue = (String) params.get(paramName);
//                        questionItem.addParamValue(paramName, paramValue);
//                    }
//                }
//            }
//        }
//
//        // get records
//        SanityRecord[] records = sanityModel.getAllSanityRecords();
//        for (SanityRecord record : records) {
//            String recordName = record.getName();
//            RecordItem recordItem = recordItems.get(recordName);
//            if (recordItem != null) {
//                String recordId = record.getPrimaryKey();
//                recordItem.addRecordId(recordId);
//            }
//        }
//    }

    private void generateTemplate(File file) throws WdkModelException {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));

            // output the questions
            for (String questionName : questionItems.keySet()) {
                writer.println("Question: " + questionName);
                QuestionItem questionItem = questionItems.get(questionName);
                Map<String, Set<String>> params = questionItem.getParams();
                for (String paramName : params.keySet()) {
                    Set<String> values = params.get(paramName);
                    StringBuffer sb = new StringBuffer();
                    for (String value : values) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(value);
                    }
                    writer.println("    Param: " + paramName + " = "
                            + sb.toString());
                }
                writer.println("//");
            }
            writer.flush();

            // output the records
            for (String recordName : recordItems.keySet()) {
                RecordItem recordItem = recordItems.get(recordName);
                Set<String> recordIds = recordItem.getRecordIds();
                StringBuffer sb = new StringBuffer();
                for (String recordId : recordIds) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(recordId);
                }
                writer.println("Record: " + recordName + " = " + sb.toString());
                writer.println("//");
            }
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // /////////// static methods /////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws WdkModelException {

        String cmdName = System.getProperty("cmdName");
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        WdkModel wdkModel = WdkModel.construct(modelName, gusHome);
//        SanityTestXmlParser sanityParser = new SanityTestXmlParser(gusHome);
//        SanityModel sanityModel = sanityParser.parseModel(modelName, wdkModel);

        StressTemplater tester = new StressTemplater(wdkModel);

        // open the input/output file
        File outFile = new File(gusHome, "/config/" + modelName
                + "/stress.template");
        tester.makeTemplate(outFile);

        System.out.println("The template file for " + modelName
                + " has been saved at " + outFile.getAbsolutePath());
        System.exit(0);
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

        String header = newline + "Preparing the template file for stress test"
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
