package org.gusdb.wdk.model.test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

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
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.json.JSONException;
import org.xml.sax.SAXException;

public class QueryTester {

    WdkModel wdkModel;

    public QueryTester(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    // ////////////////////////////////////////////////////////////////////
    // /////////// public methods /////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////

    public ResultList getResult(String querySetName, String queryName,
            Map<String, Object> paramHash) throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        QueryInstance instance = getInstance(querySetName, queryName, paramHash);
        return instance.getResults();
    }

    public String showSql(String querySetName, String queryName,
            Map<String, Object> paramHash) throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        QueryInstance instance = getInstance(querySetName, queryName, paramHash);
        if (instance instanceof SqlQueryInstance) {
            return ((SqlQueryInstance) instance).getUncachedSql();
        } else return instance.getSql();
    }

    public String showResultTable(String querySetName, String queryName,
            Map<String, Object> paramHash) throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        QueryInstance instance = getInstance(querySetName, queryName, paramHash);
        ResultFactory factory = wdkModel.getResultFactory();
        Query query = instance.getQuery();
        String tableName = CacheFactory.normalizeTableName(query.getFullName());
        int instanceId = factory.getInstanceId(instance);
        return tableName + ":" + instanceId;
    }

    public WdkModel getWdkModel() {
        return this.wdkModel;
    }

    // ////////////////////////////////////////////////////////////////////
    // /////////// protected methods //////////////////////////////////
    // ////////////////////////////////////////////////////////////////////

    QueryInstance getInstance(String querySetName, String queryName,
            Map<String, Object> paramHash) throws WdkModelException,
            WdkUserException {
        QuerySet querySet = wdkModel.getQuerySet(querySetName);
        Query query = querySet.getQuery(queryName);
        QueryInstance instance = query.makeInstance(paramHash);
        return instance;
    }

    void displayQuery(Query query) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        String newline = System.getProperty("line.separator");
        System.out.println(newline + "Query: " + query.getFullName() + newline);

        System.out.println("Parameters");

        Param[] params = query.getParams();

        for (int i = 0; i < params.length; i++) {
            System.out.println(formatParamPrompt(params[i]));
        }
        System.out.println("");
    }

    Map<String, Object> parseParamArgs(String[] params) {

        Map<String, Object> h = new LinkedHashMap<String, Object>();

        if (params.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "The -params option must be followed by key value pairs only");
        }
        for (int i = 0; i < params.length; i += 2) {
            h.put(params[i], params[i + 1]);
        }
        return h;
    }

    String formatParamPrompt(Param param) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        String newline = System.getProperty("line.separator");

        String prompt = "  " + param.getPrompt();

        if (param instanceof FlatVocabParam) {
            FlatVocabParam enumParam = (FlatVocabParam) param;
            prompt += " (chose one";
            if (enumParam.getMultiPick().booleanValue()) prompt += " or more";
            prompt += "):";
            String[] vocab = enumParam.getVocab();
            for (int i = 0; i < vocab.length; i++) {
                String term = vocab[i];
                prompt += newline + "    " + term + " ("
                        + enumParam.getInternalValue(term) + ")";
            }
        }

        else if (param instanceof StringParam) {
            StringParam stringParam = (StringParam) param;
            if (stringParam.getSample() != null)
                prompt += " (" + stringParam.getSample() + ")";
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
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        String cmdName = System.getProperty("cmdName");
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        // process args
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        String fullQueryName = cmdLine.getOptionValue("query");
        boolean returnResultAsTable = cmdLine.hasOption("returnTable");
        boolean showQuery = cmdLine.hasOption("showQuery");
        boolean haveParams = cmdLine.hasOption("params");
        // boolean paging = cmdLine.hasOption("rows");
        String[] params = new String[0];
        if (haveParams) params = cmdLine.getOptionValues("params");

        Reference ref = new Reference(fullQueryName);
        String querySetName = ref.getSetName();
        String queryName = ref.getElementName();

        // read config info
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        WdkModel wdkModel = parser.parseModel(modelName);

        QueryTester tester = new QueryTester(wdkModel);

        Map<String, Object> paramHash = tester.parseParamArgs(params);
        if (showQuery) {
            String query = tester.showSql(querySetName, queryName, paramHash);
            String newline = System.getProperty("line.separator");
            String newlineQuery = query.replaceAll("^\\s\\s\\s", newline);
            newlineQuery = newlineQuery.replaceAll("(\\S)\\s\\s\\s", "$1"
                    + newline);
            System.out.println(newline + newlineQuery + newline);
        } else if (returnResultAsTable) {
            String table = tester.showResultTable(querySetName, queryName,
                    paramHash);
            System.out.println(table);
        } else {
            Query query = wdkModel.getQuerySet(querySetName).getQuery(queryName);

            ResultList rs = tester.getResult(querySetName, queryName, paramHash);
            print(query, rs);
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

        // query name
        addOption(options, "query",
                "The full name (set.element) of the query to run.");

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
                + " -query full_query_name"
                + " [-returnTable -rows start end | -returnSize | -showQuery]"
                + " [-params param_1_name param_1_value ...]";

        String header = newline
                + "Run a query found in a WDK Model xml file.  If run without -params, displays the parameters for the specified query"
                + newline + newline + "Options:";

        String footer = " ";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
