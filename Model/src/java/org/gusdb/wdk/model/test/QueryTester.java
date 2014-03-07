package org.gusdb.wdk.model.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.QueryInfo;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.user.User;

public class QueryTester {

  WdkModel wdkModel;
  User user;

  public QueryTester(WdkModel wdkModel) throws WdkModelException {
    this.wdkModel = wdkModel;
    user = wdkModel.getSystemUser();
  }

  private String showSql(Query query, Map<String, String> paramHash)
      throws WdkModelException {
    QueryInstance instance = query.makeInstance(user, paramHash, true, 0,
        new LinkedHashMap<String, String>());
    if (instance instanceof SqlQueryInstance) {
      return ((SqlQueryInstance) instance).getUncachedSql();
    } else return instance.getSql();
  }

  private String showResultTable(Query query, Map<String, String> paramHash)
      throws WdkModelException {
    QueryInstance instance = query.makeInstance(user, paramHash, true, 0,
        new LinkedHashMap<String, String>());
    ResultFactory resultFactory = wdkModel.getResultFactory();
    CacheFactory cacheFactory = resultFactory.getCacheFactory();
    QueryInfo queryInfo = cacheFactory.getQueryInfo(instance.getQuery());
    String cacheTable = queryInfo.getCacheTable();
    int instanceId = instance.getInstanceId();
    return cacheTable + ":" + instanceId;
  }

  // ////////////////////////////////////////////////////////////////////
  // /////////// protected methods //////////////////////////////////
  // ////////////////////////////////////////////////////////////////////

  private void displayParams(Query query) {
    String newline = System.getProperty("line.separator");
    System.out.println(newline + "Query: " + query.getFullName() + newline);

    System.out.println("Parameters");

    Param[] params = query.getParams();

    for (int i = 0; i < params.length; i++) {
      System.out.println(formatParamPrompt(params[i]));
    }
    System.out.println("");
  }

  static Map<String, String> parseParamArgs(String[] params,
      boolean useDefaults, Query query) throws WdkModelException {

    Map<String, String> h = new LinkedHashMap<String, String>();

    if (params.length % 2 != 0) {
      throw new IllegalArgumentException(
          "The -params option must be followed by key value pairs only");
    }
    for (int i = 0; i < params.length; i += 2) {
      h.put(params[i], params[i + 1]);
    }
    if (useDefaults && !query.getParamValuesSets().isEmpty()) {
      ParamValuesSet pvs = query.getParamValuesSets().get(0);
      Map<String, String> map = pvs.getParamValues();
      for (String paramName : map.keySet()) {
        if (!h.containsKey(paramName)) {
          h.put(paramName, map.get(paramName));
        }
      }
    }
    return h;
  }

  private String formatParamPrompt(Param param) {
    String newline = System.getProperty("line.separator");

    String prompt = "  " + param.getPrompt();

    if (param instanceof FlatVocabParam) {
      FlatVocabParam enumParam = (FlatVocabParam) param;
      prompt += " (choose one";
      if (enumParam.getMultiPick().booleanValue()) prompt += " or more";
      prompt += "):";
      // assume independent param
      Map<String, String> vocabs = enumParam.getVocabMap(null);
      for (String term : vocabs.keySet()) {
        String internal = vocabs.get(term);
        prompt += newline + "    " + term + " (" + internal + ")";
      }
    } else if (param instanceof StringParam) {
      StringParam stringParam = (StringParam) param;
      if (stringParam.getPrompt() != null)
        prompt += " (" + stringParam.getPrompt() + ")";
      prompt += ":";
    }

    else {
      prompt = param.getPrompt() + ":";
    }

    return prompt;
  }

  // ////////////////////////////////////////////////////////////////////
  // /////////// static methods /////////////////////////////////////
  // ////////////////////////////////////////////////////////////////////

  public static void main(String[] args) throws WdkModelException,
      WdkUserException {
    String cmdName = System.getProperty("cmdName");

    // process args
    Options options = declareOptions();
    CommandLine cmdLine = parseOptions(cmdName, options, args);

    String modelName = cmdLine.getOptionValue("model");

    String fullQueryName = cmdLine.getOptionValue("query");
    boolean returnResultAsTable = cmdLine.hasOption("returnTable");
    boolean showQuery = cmdLine.hasOption("showQuery");
    boolean showParams = cmdLine.hasOption("showParams");
    boolean useDefaults = cmdLine.hasOption("d");
    boolean haveParams = cmdLine.hasOption("params");
    // boolean paging = cmdLine.hasOption("rows");
    String[] params = new String[0];
    if (haveParams) params = cmdLine.getOptionValues("params");

    Reference ref = new Reference(fullQueryName);
    String querySetName = ref.getSetName();
    String queryName = ref.getElementName();

    // read config info
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    WdkModel wdkModel = WdkModel.construct(modelName, gusHome);

    QueryTester tester = new QueryTester(wdkModel);
    QuerySet querySet = wdkModel.getQuerySet(querySetName);
    Query query = querySet.getQuery(queryName);

    if (showParams) {
      tester.displayParams(query);
    } else {
      Map<String, String> rawValues = QueryTester.parseParamArgs(params,
          useDefaults, query);
      Map<String, String> stableValues = query.getStableValues(tester.user,
          rawValues);
      if (showQuery) {
        String querySql = tester.showSql(query, stableValues);
        String newline = System.getProperty("line.separator");
        String newlineQuery = querySql.replaceAll("^\\s\\s\\s", newline);
        newlineQuery = newlineQuery.replaceAll("(\\S)\\s\\s\\s", "$1" + newline);
        System.out.println(newline + newlineQuery + newline);
      } else if (returnResultAsTable) {
        String table = tester.showResultTable(query, stableValues);
        System.out.println(table);
      } else {
        QueryInstance instance = query.makeInstance(tester.user, stableValues,
            true, 0, new LinkedHashMap<String, String>());
        ResultList rs = instance.getResults();
        print(query, rs);
      }
    }
    System.exit(0);
  }

  private static void print(Query query, ResultList resultList)
      throws WdkModelException {
    // print out the column headers
    Column[] columns = query.getColumns();
    System.out.print("No.");
    for (Column column : columns)
      System.out.print("\t| <" + column.getName() + ">");
    System.out.println();

    // print out the rows
    int row = 1;
    while (resultList.next()) {
      System.out.print(row++);
      for (Column column : columns) {
        Object value = resultList.get(column.getName());
        System.out.print("\t| " + value);
      }
      System.out.println();
    }
  }

  private static void addOption(Options options, String argName, String desc) {
    addOption(options, argName, true, desc, true);
  }

  private static void addOption(Options options, String argName,
      boolean hasArg, String desc, boolean required) {
    Option option = new Option(argName, hasArg, desc);
    option.setRequired(required);
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

    // query name
    addOption(options, "query",
        "The full name (set.element) of the query to run.");

    // show params
    addOption(options, "showParams", false,
        "Show the names of the parameters expected by the query", false);

    // show params
    addOption(options, "d", false,
        "Use default values for unprovided parameters", false);

    OptionGroup specialOperations = new OptionGroup();

    // return only the sql
    Option showQuery = new Option("showQuery",
        "Show the query as it will be run (with parameter values in place).");
    specialOperations.addOption(showQuery);

    // return table
    Option returnTable = new Option("returnTable",
        "Place the result in a table and return the name of the table.");
    specialOperations.addOption(returnTable);

    // return result size
    Option returnSize = new Option("returnSize",
        "For pageable queries only: return the total size of the result.");
    specialOperations.addOption(returnSize);

    // rows to return
    Option rows = new Option("rows",
        "For pageable queries only: provide the start and end rows to return.");
    rows.setArgs(2);
    specialOperations.addOption(rows);

    options.addOptionGroup(specialOperations);

    // params
    Option params = new Option("params", true,
        "space delimited list of param_name param_value ....");
    params.setArgName("params");
    params.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(params);

    return options;
  }

  static CommandLine parseOptions(String cmdName, Options options, String[] args) {

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
        + " -query full_query_name" + " [-d | -showParams]"
        + " [-returnTable -rows start end | -returnSize | -showQuery]"
        + " [-params param_1_name param_1_value ...]";

    String header = newline
        + "Run a query found in a WDK Model xml file. Specify -d to use default parameter values for all unprovided parameters.  Specify -showParams to display the namew of the parameters the query takes"
        + newline + newline + "Options:";

    String footer = " ";

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(cmdlineSyntax, header, options, footer);
    System.exit(1);
  }
}
