package org.gusdb.wdk.model.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.ResultSizeFactory;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.RecordInstanceFormatter;
import org.gusdb.wdk.model.record.ResultProperty;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.util.ReporterFactory;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

public class SummaryTester {

  private static final String ARG_FILTER = "answer_filter";

  public static void main(String[] args) throws Exception {

    String cmdName = System.getProperty("cmdName");

    // process args
    Options options = declareOptions();
    CommandLine cmdLine = parseOptions(cmdName, options, args);

    String questionFullName = cmdLine.getOptionValue("question");

    String[] params = null;
    boolean haveParams = cmdLine.hasOption("params");
    if (haveParams)
      params = cmdLine.getOptionValues("params");
    boolean useDefaults = cmdLine.hasOption("d");

    boolean toXml = cmdLine.hasOption("toXml");
    String xmlFileName = cmdLine.getOptionValue("toXml");
    String[] rows = cmdLine.getOptionValues("rows");

    boolean hasFormat = cmdLine.hasOption("format");
    String format = cmdLine.getOptionValue("format");

    boolean hasFormatConfig = cmdLine.hasOption("config");
    String configFile = cmdLine.getOptionValue("config");

    if (!hasFormat && hasFormatConfig) {
      throw new IllegalArgumentException(
          "Please specify the output format before providing the configuration file for that format.");
    }

    if (toXml) {
      if (xmlFileName == null || xmlFileName.equals(""))
        usage(cmdName, options);
    }
    else {
      if (rows == null || rows.length == 0)
        usage(cmdName, options);
      validateRowCount(rows);
    }

    // variable never used
    String modelName = cmdLine.getOptionValue("model");
    String gusHome = GusHome.getGusHome();
    try (WdkModel wdkModel = WdkModel.construct(modelName, gusHome)) {

      User user = wdkModel.getSystemUser();

      Question question = wdkModel.getQuestionByFullName(questionFullName)
          .orElseThrow(() -> new WdkModelException("Question " + questionFullName + " does not exist."));
      Query query = question.getQuery();

      Map<String, String> paramValues = new LinkedHashMap<String, String>();
      if (haveParams) {
        paramValues = QueryTester.parseParamArgs(user, params, useDefaults, query);
      }

      // get filter
      Optional<AnswerFilterInstance> filter = Optional.empty();
      if (cmdLine.hasOption(ARG_FILTER)) {
        String filterName = cmdLine.getOptionValue(ARG_FILTER);
        filter = Optional.of(question.getRecordClass().getFilterInstance(filterName)
          .orElseThrow(() -> new WdkUserException("Given filter name doesn't exist: " + filterName)));
      }

      // this is suspicious
      // Query query = question.getQuery();
      // query.setIsCacheable(new Boolean(true));
      int pageCount = 1;

      if (toXml) {
        writeSummaryAsXml(user, question, paramValues, xmlFileName, filter);
        return;
      }

      Map<String, Boolean> sortingMap = question.getSortingAttributeMap();

      for (int i = 0; i < rows.length; i += 2) {
        int nextStartRowIndex = Integer.parseInt(rows[i]);
        int nextEndRowIndex = Integer.parseInt(rows[i + 1]);

        RunnableObj<AnswerSpec> validSpec = AnswerSpec.builder(wdkModel)
            .setQuestionFullName(question.getFullName())
            .setParamValues(paramValues)
            .setLegacyFilterName(filter.map(NamedObject::getName))
            .buildRunnable(user, StepContainer.emptyContainer());

        AnswerValue answerValue = AnswerValueFactory.makeAnswer(
            validSpec, nextStartRowIndex, nextEndRowIndex, sortingMap, false);

        // this is wrong. it only shows one attribute query, not
        // all. Fix this in Answer by saving a list of attribute
        // queries, not just one.
        if (cmdLine.hasOption("showQuery")) {
          System.out.println(getLowLevelQuery(answerValue));
          System.exit(0);
        }

        if (rows.length != 2)
          System.out.println("page " + pageCount);

        // print the size of the answer
        ResultSizeFactory resultSizes = answerValue.getResultSizeFactory();
        System.out.println("Total # of records: " + resultSizes.getResultSize());
        System.out.println("Display Size: " + resultSizes.getDisplayResultSize());
        ResultProperty rp = answerValue.getQuestion().getRecordClass().getResultPropertyPlugin();
        /* temp for debugging */
        if (rp != null) {
          Integer missingTrans = rp.getPropertyValue(answerValue, "genesMissingTranscriptsCount");

          System.out.println("Result Property : " + missingTrans);
        }

        System.out.println("Display Size: " + resultSizes.getDisplayResultSize());
        System.out.println("Answer Checksum: " + answerValue.getChecksum());

        answerValue.getResultSizeFactory().getResultSizesByProject();

        // load configuration for output format
        if (!hasFormat)
          format = "tabular";
        Map<String, String> config = loadConfiguration(configFile);

        Reporter reporter = ReporterFactory.getReporter(answerValue, format, config);
        reporter.report(System.out);
        System.out.println();

        pageCount++;
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  private static Map<String, String> loadConfiguration(String configFileName) throws IOException {
    Map<String, String> config = new LinkedHashMap<String, String>();

    if (configFileName == null || configFileName.length() == 0)
      return config;

    InputStream stream = new FileInputStream(configFileName);
    Properties properties = new Properties();
    if (configFileName.toLowerCase().endsWith(".xml")) {
      properties.loadFromXML(stream);
    }
    else
      properties.load(stream);
    stream.close();

    for (Object obj : properties.keySet()) {
      String key = (String) obj;
      config.put(key, properties.getProperty(key));
    }
    return config;
  }

  private static void writeSummaryAsXml(User user, Question question, Map<String, String> paramValues,
      String xmlFile, Optional<AnswerFilterInstance> filter) throws WdkModelException, WdkUserException, IOException, JSONException {

    RunnableObj<AnswerSpec> answerSpec = AnswerSpec.builder(question.getWdkModel())
        .setQuestionFullName(question.getFullName())
        .setParamValues(paramValues)
        .setLegacyFilterName(filter.map(NamedObject::getName))
        .buildRunnable(user, StepContainer.emptyContainer());
    Map<String, Boolean> sortingMap = question.getSortingAttributeMap();
    AnswerValue answerValue = AnswerValueFactory.makeAnswer(answerSpec, 1, 2, sortingMap, false);
    int resultSize = answerValue.getResultSizeFactory().getResultSize();
    answerValue = AnswerValueFactory.makeAnswer(answerSpec, 1, resultSize, sortingMap, false);
    FileWriter fw = new FileWriter(new File(xmlFile), false);

    String newline = System.getProperty("line.separator");
    String ident = "    ";

    fw.write("<" + question.getFullName() + ">" + newline);
    fw.close();
    fw = new FileWriter(new File(xmlFile), true);
    for (RecordInstance ri : answerValue.getRecordInstances()) {
      fw.write(RecordInstanceFormatter.toXML(ri,ident) + newline);
    }
    fw.write("</" + question.getFullName() + ">" + newline);
    fw.close();
  }

  private static String getLowLevelQuery(AnswerValue answerValue) throws WdkModelException {
    // QueryInstance instance = answer.getAttributesQueryInstance();
    QueryInstance<?> instance = answerValue.getIdsQueryInstance();
    String query = (instance instanceof SqlQueryInstance) ? ((SqlQueryInstance) instance).getUncachedSql()
        : instance.getSql();
    String newline = System.getProperty("line.separator");
    String newlineQuery = query.replaceAll("^\\s\\s\\s", newline);
    newlineQuery = newlineQuery.replaceAll("(\\S)\\s\\s\\s", "$1" + newline);
    return newline + newlineQuery + newline;
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
    addOption(options, "model",
        "the name of the model.  This is used to " +
            "find the Model XML file ($GUS_HOME/config/model_name.xml) " +
            "the Model property file ($GUS_HOME/config/model_name.prop) " + "and the Model config file " +
            "($GUS_HOME/config/model_name-config.xml)");

    // question name
    addOption(options, "question", "The full name (set.element) of the " + "question to run.");

    // rows to return
    Option rows = new Option("rows", "The start and end pairs of the " +
        "summary rows to return. Ignored when toXml is turned on, " + "but required otherwise.");
    rows.setArgs(2);
    options.addOption(rows);

    // use defaults
    Option useDefaults = new Option("d", "Use default values for unprovided parameters");
    options.addOption(useDefaults);

    // show query
    Option showQuery = new Option("showQuery",
        "Show the query as it will " + "be run (with parameter values in place).");
    options.addOption(showQuery);

    // output XML
    Option toXml = new Option("toXml", true, "output summary in XML format" + " to given file");
    options.addOption(toXml);

    // output XML
    Option fullRecords = new Option("fullRecords", "output full records");
    options.addOption(fullRecords);

    // output format
    Option format = new Option("format", true,
        "the output format, which " + "is record type specific (defined in the model file)");
    options.addOption(format);

    // the config file for output format
    Option config = new Option("config", true, "The configuration file " + "for the output format");
    options.addOption(config);

    // the sub type input
    Option filter = new Option(ARG_FILTER, true, "The filter to be used " + "to filter out the result");
    options.addOption(filter);

    // params
    Option params = new Option("params", true, "Space delimited list of param_name param_value ....");
    params.setArgName("params");
    params.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(params);
    return options;
  }

  static CommandLine parseOptions(String cmdName, Options options, String[] args) {

    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine = null;
    try {
      // parse the command line arguments
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exp) {
      // oops, something went wrong
      System.err.println("");
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      System.err.println("");
      usage(cmdName, options);
    }

    return cmdLine;
  }

  static void validateRowCount(String[] rows) {
    if (rows.length % 2 != 0) {
      throw new IllegalArgumentException(
          "The -rows option must be followed by pairs of row numbers (each pair representing the start and end of a page");
    }
  }

  static Map<String, Object> parseParamArgs(String[] params) {

    Map<String, Object> h = new LinkedHashMap<String, Object>();
    if (params[0].equals("NONE")) {
      return h;
    }
    else {
      if (params.length % 2 != 0) {
        throw new IllegalArgumentException("The -params option must be followed by key value pairs only");
      }
      for (int i = 0; i < params.length; i += 2) {
        h.put(params[i], params[i + 1]);
      }
      return h;
    }
  }

  static void usage(String cmdName, Options options) {

    String newline = System.getProperty("line.separator");
    String cmdlineSyntax = cmdName + " -model model_name\n" + " -question full_question_name\n" +
        " -rows start end\n" + " [-showQuery]\n" + " [-toXml <xmlFile>|-fullRecords]\n" +
        " [-format tabular | gff3 | fullRecords [-config <config_file>]]\n" + " [-" + ARG_FILTER +
        " <filter_name>]\n" + " -params param_1_name param_1_value ...\n";

    String header = newline + "Print a summary found in a WDK Model xml file. Options:";

    String footer = "";

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(75, cmdlineSyntax, header, options, footer);
    System.exit(1);
  }

}
