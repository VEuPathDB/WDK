package org.gusdb.wdk.model.test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

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
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * SanityTester.java " [-project project_id]" +
 * 
 * 
 * Main class for running the sanity tests, which is a way to test all Queries
 * and RecordClasss in a wdk model to make sure they work as intended and their
 * results fall within an expected range, even over the course of code base
 * development. See the usage() method for parameter information, and see the
 * gusDb.org wiki page for the structure and content of the sanity test.
 * 
 * Created: Mon August 23 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-23 12:31:12 -0400 (Tue, 23 Aug
 *          2005) $Author$
 */
public class SanityTester {

    int queriesPassed = 0;
    int queriesFailed = 0;
    int recordsPassed = 0;
    int recordsFailed = 0;
    int questionsPassed = 0;
    int questionsFailed = 0;
    int testCount = 0;
    boolean[] testFilter;
    String testFilterString;
    static final String newline = System.getProperty("line.separator");

    User user;
    WdkModel wdkModel;
    boolean verbose;
    boolean failuresOnly;
    boolean indexOnly;
    boolean skipWebSvcQueries;
    String modelName;

    public static final String BANNER_LINE_top = "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv";
    public static final String BANNER_LINE_bot = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^";

    private static final Logger logger = Logger.getLogger(SanityTester.class);

    public SanityTester(String modelName, boolean verbose,
            String testFilterString, boolean failuresOnly, boolean indexOnly,
            boolean skipWebSvcQueries) throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException,
            ParserConfigurationException, TransformerException, IOException,
            SAXException, SQLException, JSONException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        this.wdkModel = WdkModel.construct(modelName, gusHome);
        this.verbose = verbose;
        this.failuresOnly = failuresOnly;
        this.indexOnly = indexOnly;
        this.skipWebSvcQueries = skipWebSvcQueries;
        this.modelName = modelName;
        this.testFilterString = testFilterString;
        this.user = wdkModel.getSystemUser();
        testFilter = parseTestFilter(testFilterString);
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private void testQuestionSets() throws SQLException, WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException {

        System.out.println("Sanity Test:  Checking questions" + newline);

        for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
            if (questionSet.getDoNotTest())
                continue;

            for (Question question : questionSet.getQuestions()) {
                Query query = question.getQuery();
                if (query.getDoNotTest() || query.getQuerySet().getDoNotTest())
                    continue;
                if (skipWebSvcQueries && query instanceof ProcessQuery)
                    continue;
                for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
                    testQuestion(question, paramValuesSet);
                }
            }
        }
    }

    private void testQuestion(Question question, ParamValuesSet paramValuesSet) {
        if (!checkTestFilter(testCount++))
            return;
        if (indexOnly) {
            System.out.println(" [test: " + testCount + "]" + " QUESTION "
                    + question.getFullName() + " (query "
                    + question.getQuery().getFullName() + ")" + newline);
            return;
        }
        long start = System.currentTimeMillis();
        int sanityMin = paramValuesSet.getMinRows();
        int sanityMax = paramValuesSet.getMaxRows();
        boolean passed = false;
        String status = " FAILED!";
        String prefix = "***";
        String returned = "";
        String expected = "";
        Exception caughtException = null;

        try {
            question.getQuery().setIsCacheable(false);
            AnswerValue answerValue = question.makeAnswerValue(user,
                    paramValuesSet.getParamValues(), true, 0);

            int resultSize = answerValue.getResultSize();

            // get the summary attribute list
            Map<String, AttributeField> summary = answerValue
                    .getSummaryAttributeFieldMap();

            // iterate through the page and try every summary attribute of
            // each record
            for (RecordInstance record : answerValue.getRecordInstances()) {
                StringBuffer sb = new StringBuffer();
                for (String attrName : summary.keySet()) {
                    sb.append(record.getAttributeValue(attrName));
                    sb.append('\t');
                }
                logger.debug("Record: " + sb.toString());
            }

            passed = (resultSize >= sanityMin && resultSize <= sanityMax);

            returned = " It returned " + resultSize + " rows. ";
            if (sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS)
                expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

        } catch (Exception e) {
            returned = " It threw an exception.";
            caughtException = e;
        } finally {
            long end = System.currentTimeMillis();
            if (passed) {
                questionsPassed++;
                prefix = "";
                status = " passed.";
            } else {
                questionsFailed++;
            }
            if (!passed)
                System.out.println(BANNER_LINE_top);
            String cmd = " [ wdkSummary -model " + wdkModel.getProjectId()
                    + " -question " + question.getFullName() + " -rows 1 100"
                    + " -params " + paramValuesSet.getCmdLineString() + " ] ";

            String msg = prefix + ((end - start) / 1000F) + " [test: "
                    + testCount + "]" + " QUESTION " + question.getFullName()
                    + " (query " + question.getQuery().getFullName() + ")"
                    + status + returned + expected + cmd + newline;
            if (!passed || !failuresOnly)
                System.out.println(msg);
            if (caughtException != null)
                caughtException.printStackTrace(System.err);
            if (!passed)
                System.out.println(BANNER_LINE_bot + newline);

            // check the connection usage
            DBPlatform platform = wdkModel.getQueryPlatform();
            if (platform.getActiveCount() > 0) {
                System.err.println("Connection leak ("
                        + platform.getActiveCount() + ") for question: "
                        + question.getFullName());
            }
        }
    }

    private void testQuerySets(String queryType) throws SQLException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            WdkUserException {

        System.out.println("Sanity Test:  Checking " + queryType + " queries"
                + newline);

        for (QuerySet querySet : wdkModel.getAllQuerySets()) {
            if (!querySet.getQueryType().equals(queryType)
                    || querySet.getDoNotTest())
                continue;

            int minRows = -1;
            int maxRows = -1;
            if (queryType.equals(QuerySet.TYPE_ATTRIBUTE)) {
                // discover number of entities expected in each attribute query
                String testRowCountSql = querySet.getTestRowCountSql();
                if (testRowCountSql != null) {
                    ResultSet rs = SqlUtils.executeQuery(wdkModel, wdkModel
                            .getQueryPlatform().getDataSource(),
                            testRowCountSql, querySet.getName()
                                    + "__sanity-test-row-count");
                    rs.next();
                    minRows = maxRows = rs.getInt(1);
                    SqlUtils.closeResultSetAndStatement(rs);
                }
            }

            for (Query query : querySet.getQueries()) {
                if (query.getDoNotTest())
                    continue;
                for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
                    if (!queryType.equals(QuerySet.TYPE_ATTRIBUTE)) {
                        minRows = paramValuesSet.getMinRows();
                        maxRows = paramValuesSet.getMaxRows();
                    }
                    testQuery(querySet, query, queryType, minRows, maxRows,
                            paramValuesSet);
                }
                if (queryType.equals(QuerySet.TYPE_TABLE)) {
                    testQuery(querySet, query, queryType + "TOTAL", minRows,
                            maxRows, null);
                }
            }
        }
    }

    private void testQuery(QuerySet querySet, Query query, String queryType,
            int minRows, int maxRows, ParamValuesSet paramValuesSet) {

        String typeUpperCase = queryType.toUpperCase();

        if (!checkTestFilter(testCount++))
            return;
        if (indexOnly) {
            System.out.println(" [test: " + testCount + "] " + typeUpperCase
                    + " QUERY " + query.getFullName() + newline);
            return;
        }

        boolean passed = false;
        String prefix = "***";
        String status = " FAILED!";
        int sanityMin = minRows;
        int sanityMax = maxRows;
        int count = 0;
        long start = System.currentTimeMillis();
        String returned = "";
        String expected = "";
        String params = "";
        Exception caughtException = null;

        try {
            if (queryType.equals(QuerySet.TYPE_ATTRIBUTE)) {
                count = testAttributeQuery_Count(query, paramValuesSet);
                start = System.currentTimeMillis();
                testAttributeQuery_Time(query, paramValuesSet, count);
            } else if (queryType.equals(QuerySet.TYPE_TABLE + "TOTAL")) {
                count = testTableQuery_TotalTime(query);
            } else {
                if (queryType.equals(QuerySet.TYPE_TABLE)) {
                    query = RecordClass.prepareQuery(wdkModel, query,
                            paramValuesSet.getParamNames());
                }
                params = " -params " + paramValuesSet.getCmdLineString();
                start = System.currentTimeMillis();
                count = testNonAttributeQuery(querySet, query, paramValuesSet);
            }

            passed = (count >= sanityMin && count <= sanityMax);

            returned = " It returned " + count + " rows. ";
            if (sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS)
                expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

        } catch (Exception e) {
            returned = " It threw an exception.";
            caughtException = e;
        } finally {
            long end = System.currentTimeMillis();
            if (passed) {
                queriesPassed++;
                prefix = "";
                status = " passed.";
            } else {
                queriesFailed++;
            }
            if (!passed)
                System.out.println(BANNER_LINE_top);

            String cmd = " [ wdkQuery -model " + wdkModel.getProjectId()
                    + " -query " + query.getFullName() + params + " ] ";

            String msg = prefix + ((end - start) / 1000F) + " [test: "
                    + testCount + "]" + " " + typeUpperCase + " QUERY "
                    + query.getFullName() + status + returned + expected + cmd
                    + newline;
            if (!passed || !failuresOnly)
                System.out.println(msg);
            if (caughtException != null)
                caughtException.printStackTrace(System.err);
            if (!passed)
                System.out.println(BANNER_LINE_bot + newline);
        }
    }

    private int testNonAttributeQuery(QuerySet querySet, Query query,
            ParamValuesSet paramValuesSet) throws SQLException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            WdkUserException {

        int count = 0;

        QueryInstance instance = query.makeInstance(user,
                paramValuesSet.getParamValues(), true, 0,
                new LinkedHashMap<String, String>());
        ResultList rl = instance.getResults();

        while (rl.next()) {
            count++;
        }
        rl.close();
        return count;
    }

    private int testAttributeQuery_Count(Query query,
            ParamValuesSet paramValuesSet) throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        // put user id into the param
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

        SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
                params, true, 0, new LinkedHashMap<String, String>());

        if (paramValuesSet.getParamValues().size() != 2) {
            throw new WdkUserException(
                    "missing <defaultTestParamValues> for querySet "
                            + query.getQuerySet().getName());
        }
        String sql = "select count (*) from (select distinct "
                + paramValuesSet.getNamesAsString() + " from ("
                + instance.getUncachedSql() + "))";

        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                query.getFullName() + "__sanity-test-count");
        resultSet.next();
        int count = resultSet.getInt(1);
        SqlUtils.closeResultSetAndStatement(resultSet);
        return count;
    }

    private void testAttributeQuery_Time(Query query,
            ParamValuesSet paramValuesSet, int count)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        // put user id into the param
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

        SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
                params, true, 0, new LinkedHashMap<String, String>());

        String sql = "select * from (" + instance.getUncachedSql() + ") "
                + paramValuesSet.getWhereClause();

        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                query.getFullName() + "__sanity-test-time");
        if (count > 0 && !resultSet.next()) {
            String msg = "no row returned for " + query.getFullName()
                    + " using where clause (" + paramValuesSet.getWhereClause()
                    + ")";
            throw new WdkModelException(msg);
        }
        while (resultSet.next())
            ; // bring full result over to test speed
        SqlUtils.closeResultSetAndStatement(resultSet);
    }

    private int testTableQuery_TotalTime(Query query)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        // put user id into the param
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

        SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
                params, true, 0, new LinkedHashMap<String, String>());

        String sql = instance.getUncachedSql();

        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                query.getFullName() + "__sanity-test-total-time");
        int count = 0;
        while (resultSet.next())
            count++; // bring full result over to test speed
        SqlUtils.closeResultSetAndStatement(resultSet);
        return count;
    }

    private void testRecordSets() throws SQLException, WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException {

        System.out.println("Sanity Test:  Checking records" + newline);

        for (RecordClassSet recordClassSet : wdkModel.getAllRecordClassSets()) {
            for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
                if (recordClass.getDoNotTest())
                    continue;
                testRecordClass(recordClass, recordClass.getParamValuesSet());
            }
        }
    }

    private boolean checkTestFilter(int testIndex) {
        return testFilter == null || testFilter[testIndex];
    }

    private void testRecordClass(RecordClass recordClass,
            ParamValuesSet paramValuesSet) {

        if (!checkTestFilter(testCount++))
            return;
        if (indexOnly) {
            System.out.println(" [test: " + testCount + "]" + " RECORD "
                    + recordClass.getFullName() + newline);
            return;
        }
        long start = System.currentTimeMillis();
        boolean passed = false;
        String status = " FAILED!";
        String prefix = "***";
        Exception caughtException = null;

        try {
            Map<String, String> paramValues = paramValuesSet.getParamValues();
            Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
            for (String key : paramValues.keySet()) {
                pkValues.put(key, paramValues.get(key));
            }
            RecordInstance recordInstance = new RecordInstance(user,
                    recordClass, pkValues);
            recordInstance.print();
            passed = true;

        } catch (Exception e) {
            caughtException = e;
        } finally {
            long end = System.currentTimeMillis();
            if (passed) {
                recordsPassed++;
                prefix = "";
                status = " passed.";
            } else {
                recordsFailed++;
            }

            if (!passed)
                System.out.println(BANNER_LINE_top);
            String cmd = " [ wdkRecord -model " + wdkModel.getProjectId()
                    + " -record " + recordClass.getFullName() + " -primaryKey "
                    + paramValuesSet.getCmdLineString() + " ] ";

            String msg = prefix + ((end - start) / 1000F) + " [test: "
                    + testCount + "]" + " RECORD " + recordClass.getFullName()
                    + status + cmd + newline;
            if (!passed || !failuresOnly)
                System.out.println(msg);
            if (caughtException != null)
                caughtException.printStackTrace(System.err);
            if (!passed)
                System.out.println(BANNER_LINE_bot + newline);
        }
    }

    /**
     * @param queryResult
     *            a two-value array where the first entry is the number of
     *            queries that passed the test and the second is the number of
     *            queries that failed.
     * 
     * @param recordResult
     *            a two-value array where the first entry is the number of
     *            records that passed the test and the second is the number of
     *            records that failed.
     * 
     * @param return true if one or more tests failed; false otherwise.
     */

    private boolean printSummaryLine() {

        boolean failedOverall = (queriesFailed > 0 || recordsFailed > 0 || questionsFailed > 0);
        String result = failedOverall ? "FAILED" : "PASSED";

        int totalPassed = queriesPassed + recordsPassed + questionsPassed;
        int totalFailed = queriesFailed + recordsFailed + questionsFailed;

        StringBuffer resultLine = new StringBuffer("***Sanity test summary***"
                + newline);
        resultLine.append("TestFilter: " + testFilterString + newline);
        resultLine.append("Total Passed: " + totalPassed + newline);
        resultLine.append("Total Failed: " + totalFailed + newline);
        resultLine.append("   " + queriesPassed + " queries passed, "
                + queriesFailed + " queries failed" + newline);
        resultLine.append("   " + recordsPassed + " records passed, "
                + recordsFailed + " records failed" + newline);
        resultLine.append("   " + questionsPassed + " questions passed, "
                + questionsFailed + " questions failed" + newline);
        resultLine.append("Sanity Test " + result + newline);
        System.out.println(resultLine.toString());
        return failedOverall;
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
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml), the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

        // verbose
        Option verbose = new Option("verbose",
                "Print out more information while running test.");
        options.addOption(verbose);

        // verbose
        Option filter = new Option("t", true,
                "Optional list of tests to run (default=all).  E.g., 1,4-17,62");
        options.addOption(filter);

        Option failuresOnly = new Option("failuresOnly",
                "Only print failures only.");
        options.addOption(failuresOnly);

        Option indexOnly = new Option("indexOnly",
                "Only print an index of the tests.");
        options.addOption(indexOnly);

        Option skipWebSvcQueries = new Option("skipWebSvcQueries",
                "Skip all questions and queries that use web service queries.");
        options.addOption(skipWebSvcQueries);

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
            System.out.println("");
            System.out.println("Parsing failed.  Reason: " + exp.getMessage());
            System.out.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static boolean[] parseTestFilter(String listStr) {
        if (listStr == null)
            return null;

        String[] ranges = listStr.split(",");
        boolean[] filter = new boolean[1000]; // assume no more than 1000
        // tests
        Arrays.fill(filter, false);
        for (String range : ranges) {
            String[] points = range.split("-");
            int min = Integer.parseInt(points[0]) - 1;
            int max = points.length == 1 ? min
                    : Integer.parseInt(points[1]) - 1;
            Arrays.fill(filter, min, max + 1, true);
        }
        return filter;
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName
                + " -model model_name"
                + " [-verbose] [-t testfilter] [-failuresOnly | -indexOnly] [-skipWebSvcQueries]";

        String header = newline
                + "Run a test on all queries and records in a wdk model."
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

    // private static Logger logger = Logger.getLogger(SanityTester.class);

    public static void main(String[] args) throws WdkModelException,
            SAXException, IOException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            NoSuchAlgorithmException, SQLException, JSONException,
            InstantiationException, IllegalAccessException, WdkUserException,
            ClassNotFoundException {
        String cmdName = System.getProperty("cmdName");

        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String modelName = cmdLine.getOptionValue("model");

        String testFilterString = cmdLine.getOptionValue("t");

        boolean verbose = cmdLine.hasOption("verbose");

        boolean failuresOnly = cmdLine.hasOption("failuresOnly");

        boolean indexOnly = cmdLine.hasOption("indexOnly");

        boolean skipWebSvcQueries = cmdLine.hasOption("skipWebSvcQueries");

        SanityTester sanityTester = new SanityTester(modelName, verbose,
                testFilterString, failuresOnly, indexOnly, skipWebSvcQueries);

        String dbConnectionUrl = sanityTester.wdkModel
                .getQueryPlatform().getDbConfig().getConnectionUrl();                

        System.out.println("Sanity Test: ");
        System.out.println(" [Database] " + dbConnectionUrl);
        SimpleDateFormat sdFormat =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        System.out.println(" [Time] " + sdFormat.format(new Date()));
        System.out.println();

	//System.out.println(" MODEL: " + modelName + "\n\n");

        sanityTester.testQuerySets(QuerySet.TYPE_VOCAB);
        sanityTester.testQuerySets(QuerySet.TYPE_ATTRIBUTE);
        if ( !modelName.equals("EuPathDB") ) { sanityTester.testQuerySets(QuerySet.TYPE_TABLE); }
        sanityTester.testQuestionSets();
        sanityTester.testRecordSets();
        if (!indexOnly) {
            if (sanityTester.printSummaryLine()) {
                System.exit(1);
            }
        }


    }
}
