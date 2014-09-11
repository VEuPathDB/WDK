package org.gusdb.wdk.model.test;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.QuerySet.QueryType;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.User;

/**
 * SanityTester.java " [-project project_id]" +
 * 
 * Main class for running the sanity tests, which is a way to test all Queries
 * and RecordClasses in a WDK model to make sure they work as intended and their
 * results fall within an expected range, even over the course of code base
 * development. See the usage() method for parameter information, and see the
 * gusdb.org wiki page for the structure and content of the sanity test.
 * 
 * Created: Mon August 23 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-23 12:31:12 -0400 (Tue, 23 Aug
 *          2005) $Author$
 */
public class SanityTester {

  private static final Logger LOG = Logger.getLogger(SanityTester.class);

  private static void OUT (Object... obj) { System.out.println(FormatUtil.join(obj, ", ")); }
  private static void ERR (Object... obj) { System.err.println(FormatUtil.join(obj, ", ")); }

  public static final String BEGIN_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

  private static final String BANNER_LINE_top = "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv";
  private static final String BANNER_LINE_bot = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^";

  private WdkModel _wdkModel;
  private User _user;

  // input parameters
  private boolean _failuresOnly;
  private boolean _indexOnly;
  private boolean _skipWebSvcQueries;
  private String _testFilterString;
  private boolean[] _testFilter;

  // statistics aggregators
  private int _testCount = 0;
  private int _queriesPassed = 0;
  private int _queriesFailed = 0;
  private int _recordsPassed = 0;
  private int _recordsFailed = 0;
  private int _questionsPassed = 0;
  private int _questionsFailed = 0;

  public SanityTester(String modelName, String testFilterString, boolean failuresOnly,
      boolean indexOnly, boolean skipWebSvcQueries) throws WdkModelException {
    _wdkModel = WdkModel.construct(modelName, GusHome.getGusHome());
    _user = _wdkModel.getSystemUser();
    _failuresOnly = failuresOnly;
    _indexOnly = indexOnly;
    _skipWebSvcQueries = skipWebSvcQueries;
    _testFilterString = testFilterString;
    _testFilter = parseTestFilter(testFilterString);
  }

  public void testQuestionSets() throws WdkModelException {

    OUT("Sanity Test:  Checking questions" + NL);

    for (QuestionSet questionSet : _wdkModel.getAllQuestionSets()) {
      if (questionSet.getDoNotTest()) continue;

      for (Question question : questionSet.getQuestions()) {
        Query query = question.getQuery();
        if (query.getDoNotTest() || query.getQuerySet().getDoNotTest())
          continue;
        if (_skipWebSvcQueries && query instanceof ProcessQuery) continue;
        for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
          testQuestion(question, paramValuesSet);
        }
      }
    }
  }

  private void testQuestion(Question question, ParamValuesSet paramValuesSet) {
    if (!checkTestFilter(_testCount++)) return;
    if (_indexOnly) {
      OUT(" [test: " + _testCount + "]" + " QUESTION "
          + question.getFullName() + " (query "
          + question.getQuery().getFullName() + ")" + NL);
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
      AnswerValue answerValue = question.makeAnswerValue(_user,
          paramValuesSet.getParamValues(), true, 0);

      int resultSize = answerValue.getResultSize();

      // get the summary attribute list
      Map<String, AttributeField> summary = answerValue.getSummaryAttributeFieldMap();

      // iterate through the page and try every summary attribute of
      // each record
      for (RecordInstance record : answerValue.getRecordInstances()) {
        StringBuffer sb = new StringBuffer();
        for (String attrName : summary.keySet()) {
          sb.append(record.getAttributeValue(attrName));
          sb.append('\t');
        }
        LOG.debug("Record: " + sb.toString());
      }

      passed = (resultSize >= sanityMin && resultSize <= sanityMax);

      returned = " It returned " + resultSize + " rows. ";
      if (sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS)
        expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

    } catch (Exception e) {
      returned = " It threw an exception.";
      caughtException = e;
    }
    finally {
      long end = System.currentTimeMillis();
      if (passed) {
        _questionsPassed++;
        prefix = "";
        status = " passed.";
      }
      else {
        _questionsFailed++;
      }
      if (!passed) OUT(BANNER_LINE_top);
      String cmd = " [ wdkSummary -model " + _wdkModel.getProjectId()
          + " -question " + question.getFullName() + " -rows 1 100"
          + " -params " + paramValuesSet.getCmdLineString() + " ] ";

      String msg = prefix + ((end - start) / 1000F) + " [test: " + _testCount
          + "]" + " QUESTION " + question.getFullName() + " (query "
          + question.getQuery().getFullName() + ")" + status + returned
          + expected + cmd + NL;
      if (!passed || !_failuresOnly) OUT(msg);
      if (caughtException != null) caughtException.printStackTrace(System.err);
      if (!passed) OUT(BANNER_LINE_bot + NL);

      // check the connection usage
      DatabaseInstance database = _wdkModel.getAppDb();
      if (database.getActiveCount() > 0) {
        ERR("Connection leak (" + database.getActiveCount()
            + ") for question: " + question.getFullName());
      }
    }
  }

  public void testQuerySets(QueryType queryType) throws SQLException,
      WdkModelException {

    OUT("Sanity Test:  Checking " + queryType + " queries" + NL);

    for (QuerySet querySet : _wdkModel.getAllQuerySets()) {
      if (!querySet.getQueryTypeEnum().equals(queryType) || querySet.getDoNotTest())
        continue;

      int minRows = -1;
      int maxRows = -1;
      if (queryType.equals(QueryType.ATTRIBUTE)) {
        // discover number of entities expected in each attribute query
        String testRowCountSql = querySet.getTestRowCountSql();
        if (testRowCountSql != null) {
          ResultSet rs = null;
          try {
            rs = SqlUtils.executeQuery(
                _wdkModel.getAppDb().getDataSource(), testRowCountSql,
                querySet.getName() + "__sanity-test-row-count");
            if (rs.next())
              minRows = maxRows = rs.getInt(1);
            else
              throw new WdkModelException("Count query '" + testRowCountSql + "' returned zero rows.");
          }
          finally {
            SqlUtils.closeResultSetAndStatement(rs);
          }
        }
      }

      for (Query query : querySet.getQueries()) {
        if (query.getDoNotTest()) continue;
        for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
          if (!queryType.equals(QueryType.ATTRIBUTE)) {
            minRows = paramValuesSet.getMinRows();
            maxRows = paramValuesSet.getMaxRows();
          }
          testQuery(querySet, query, queryType, minRows, maxRows, paramValuesSet);
        }
        if (queryType.equals(QueryType.TABLE)) {
          testQuery(querySet, query, QueryType.TABLE_TOTAL, minRows, maxRows, null);
        }
      }
    }
  }

  private void testQuery(QuerySet querySet, Query query, QueryType queryType,
      int minRows, int maxRows, ParamValuesSet paramValuesSet) {

    if (!checkTestFilter(_testCount++)) return;
    if (_indexOnly) {
      OUT(" [test: " + _testCount + "] " + queryType
          + " QUERY " + query.getFullName() + NL);
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
      switch (queryType) {
        case ATTRIBUTE:
          count = testAttributeQuery_Count(query, paramValuesSet);
          start = System.currentTimeMillis();
          testAttributeQuery_Time(query, paramValuesSet, count);
          break;
        case TABLE_TOTAL:
          count = testTableQuery_TotalTime(query);
          break;
        case TABLE:
          query = RecordClass.prepareQuery(_wdkModel, query,
              paramValuesSet.getParamNames());
          // fall through to vocab...
        case VOCAB:
          params = " -params " + paramValuesSet.getCmdLineString();
          start = System.currentTimeMillis();
          count = testNonAttributeQuery(querySet, query, paramValuesSet);
          break;
        default:
          // do nothing for other types
      }

      passed = (count >= sanityMin && count <= sanityMax);

      returned = " It returned " + count + " rows. ";
      
      if (sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS)
        expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

    }
    catch (Exception e) {
      returned = " It threw an exception.";
      caughtException = e;
    }
    finally {
      long end = System.currentTimeMillis();
      if (passed) {
        _queriesPassed++;
        prefix = "";
        status = " passed.";
      }
      else {
        _queriesFailed++;
      }

      if (!passed) OUT(BANNER_LINE_top);

      String cmd = " [ wdkQuery -model " + _wdkModel.getProjectId() + " -query "
          + query.getFullName() + params + " ] ";

      String msg = prefix + ((end - start) / 1000F) + " [test: " + _testCount
          + "]" + " " + queryType + " QUERY " + query.getFullName()
          + status + returned + expected + cmd + NL;
      if (!passed || !_failuresOnly) OUT(msg);
      if (caughtException != null) caughtException.printStackTrace(System.err);
      if (!passed) OUT(BANNER_LINE_bot + NL);
    }
  }

  private int testNonAttributeQuery(QuerySet querySet, Query query,
      ParamValuesSet paramValuesSet) throws WdkModelException, WdkUserException {

    int count = 0;

    QueryInstance instance = query.makeInstance(_user,
        paramValuesSet.getParamValues(), true, 0,
        new LinkedHashMap<String, String>());
    ResultList rl = null;
    try {
      rl = instance.getResults();
      while (rl.next()) {
        count++;
      }
      return count;
    }
    finally {
      if (rl != null) rl.close();
    }
  }

  private int testAttributeQuery_Count(Query query,
      ParamValuesSet paramValuesSet) throws SQLException, WdkModelException, WdkUserException {
    // put user id into the param
    Map<String, String> params = new LinkedHashMap<String, String>();

    // since this attribute query is the original copy from model and it doesn't
    // params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(_user,
        params, true, 0, new LinkedHashMap<String, String>());

    // if (paramValuesSet.getParamValues().size() != 2) {
    // throw new WdkUserException(
    // "missing <defaultTestParamValues> for querySet "
    // + query.getQuerySet().getName());
    // }
    String sql = "select count(*) from " +
        "(select distinct * from (" + instance.getUncachedSql() + "))";

    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
        query.getFullName() + "__sanity-test-count");
      if (resultSet.next())
        return resultSet.getInt(1);
      else
        throw new WdkModelException("Count query '" + sql + "' returned zero rows.");
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  private void testAttributeQuery_Time(Query query,
      ParamValuesSet paramValuesSet, int count) throws SQLException, WdkModelException, WdkUserException {
    // put user id into the param
    Map<String, String> params = new LinkedHashMap<String, String>();
    // params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(_user,
        params, true, 0, new LinkedHashMap<String, String>());

    String sql = "select * from (" + instance.getUncachedSql() + ") f "
        + paramValuesSet.getWhereClause();

    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
        query.getFullName() + "__sanity-test-time");
      if (count > 0 && !resultSet.next()) {
        String msg = "no row returned for " + query.getFullName()
            + " using where clause (" + paramValuesSet.getWhereClause() + ")";
        throw new WdkModelException(msg);
      }
      while (resultSet.next()) {} // bring full result over to test speed
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  private int testTableQuery_TotalTime(Query query) throws SQLException, WdkModelException, WdkUserException {

    // put user id into the param
    Map<String, String> params = new LinkedHashMap<String, String>();
    // params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(_user,
        params, true, 0, new LinkedHashMap<String, String>());

    String sql = instance.getUncachedSql();
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
          query.getFullName() + "__sanity-test-total-time");
      int count = 0;
      while (resultSet.next())
        count++; // bring full result over to test speed
      return count;
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  public void testRecordSets() {
    OUT("Sanity Test:  Checking records" + NL);
    for (RecordClassSet recordClassSet : _wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
        if (recordClass.getDoNotTest()) continue;
        testRecordClass(recordClass, recordClass.getParamValuesSet());
      }
    }
  }

  private boolean checkTestFilter(int testIndex) {
    return _testFilter == null || _testFilter[testIndex];
  }

  private void testRecordClass(RecordClass recordClass,
      ParamValuesSet paramValuesSet) {

    if (!checkTestFilter(_testCount++)) return;
    if (_indexOnly) {
      OUT(" [test: " + _testCount + "]" + " RECORD "
          + recordClass.getFullName() + NL);
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
      RecordInstance recordInstance = new RecordInstance(_user, recordClass,
          pkValues);
      recordInstance.print();
      passed = true;

    }
    catch (Exception e) {
      caughtException = e;
    }
    finally {
      long end = System.currentTimeMillis();
      if (passed) {
        _recordsPassed++;
        prefix = "";
        status = " passed.";
      }
      else {
        _recordsFailed++;
      }

      if (!passed) OUT(BANNER_LINE_top);
      String cmd = " [ wdkRecord -model " + _wdkModel.getProjectId()
          + " -record " + recordClass.getFullName() + " -primaryKey "
          + paramValuesSet.getCmdLineString() + " ] ";

      String msg = prefix + ((end - start) / 1000F) + " [test: " + _testCount
          + "]" + " RECORD " + recordClass.getFullName() + status + cmd + NL;
      if (!passed || !_failuresOnly) OUT(msg);
      if (caughtException != null) caughtException.printStackTrace(System.err);
      if (!passed) OUT(BANNER_LINE_bot + NL);
    }
  }

  public boolean printSummaryLine() {

    boolean failedOverall = (_queriesFailed > 0 || _recordsFailed > 0 || _questionsFailed > 0);
    String result = failedOverall ? "FAILED" : "PASSED";

    int totalPassed = _queriesPassed + _recordsPassed + _questionsPassed;
    int totalFailed = _queriesFailed + _recordsFailed + _questionsFailed;

    StringBuffer resultLine = new StringBuffer("***Sanity test summary***" + NL);
    resultLine.append("TestFilter: " + _testFilterString + NL);
    resultLine.append("Total Passed: " + totalPassed + NL);
    resultLine.append("Total Failed: " + totalFailed + NL);
    resultLine.append("   " + _queriesPassed + " queries passed, "
        + _queriesFailed + " queries failed" + NL);
    resultLine.append("   " + _recordsPassed + " records passed, "
        + _recordsFailed + " records failed" + NL);
    resultLine.append("   " + _questionsPassed + " questions passed, "
        + _questionsFailed + " questions failed" + NL);
    resultLine.append("Sanity Test " + result + NL);
    OUT(resultLine.toString());
    return failedOverall;
  }

  private static void addOption(Options options, String argName, String desc) {
    Option option = new Option(argName, true, desc);
    option.setRequired(true);
    option.setArgName(argName);
    options.addOption(option);
  }

  public static Options declareOptions() {
    Options options = new Options();

    // model name
    addOption(options, "model",
        "the name of the model.  This is used to find the Model XML file " +
        "($GUS_HOME/config/model_name.xml), the Model property file " +
        "($GUS_HOME/config/model_name.prop) and the Model config file " +
        "($GUS_HOME/config/model_name-config.xml)");

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

  public static CommandLine parseOptions(String cmdName, Options options, String[] args) {

    CommandLineParser parser = new BasicParser();
    CommandLine cmdLine = null;
    try {
      // parse the command line arguments
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exp) {
      // oops, something went wrong
      OUT();
      OUT("Parsing failed.  Reason: " + exp.getMessage());
      OUT();
      usage(cmdName, options);
    }

    return cmdLine;
  }

  private static boolean[] parseTestFilter(String listStr) {
    if (listStr == null) return null;

    String[] ranges = listStr.split(",");
    boolean[] filter = new boolean[1000]; // assume no more than 1000
    // tests
    Arrays.fill(filter, false);
    for (String range : ranges) {
      String[] points = range.split("-");
      int min = Integer.parseInt(points[0]) - 1;
      int max = points.length == 1 ? min : Integer.parseInt(points[1]) - 1;
      Arrays.fill(filter, min, max + 1, true);
    }
    return filter;
  }

  public static void usage(String cmdName, Options options) {

    String newline = System.getProperty("line.separator");
    String cmdlineSyntax = cmdName
        + " -model model_name"
        + " [-verbose] [-t testfilter] [-failuresOnly | -indexOnly] [-skipWebSvcQueries]";

    String header = newline
        + "Run a test on all queries and records in a wdk model." + newline
        + newline + "Options:";

    String footer = "";

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(75, cmdlineSyntax, header, options, footer);
    System.exit(1);
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public static void main(String[] args) throws WdkModelException, SQLException {

    String cmdName = System.getProperty("cmdName");
    Options options = declareOptions();
    CommandLine cmdLine = parseOptions(cmdName, options, args);

    String modelName = cmdLine.getOptionValue("model");
    String testFilterString = cmdLine.getOptionValue("t");
    boolean failuresOnly = cmdLine.hasOption("failuresOnly");
    boolean indexOnly = cmdLine.hasOption("indexOnly");
    boolean skipWebSvcQueries = cmdLine.hasOption("skipWebSvcQueries");
    // TODO: determine if verbose mode is desired and add if so
    //boolean verbose = cmdLine.hasOption("verbose");

    SanityTester sanityTester = new SanityTester(modelName,
        testFilterString, failuresOnly, indexOnly, skipWebSvcQueries);

    String dbConnectionUrl = sanityTester.getWdkModel().getAppDb().getConfig().getConnectionUrl();

    OUT("Sanity Test: ");
    OUT(" [Model] " + modelName);
    OUT(" [Database] " + dbConnectionUrl);
    OUT(" [Time] " + new SimpleDateFormat(BEGIN_DATE_FORMAT).format(new Date()));
    OUT();

    
    sanityTester.testQuerySets(QueryType.VOCAB);
    sanityTester.testQuerySets(QueryType.ATTRIBUTE);
    if (!modelName.equals("EuPathDB")) {
      sanityTester.testQuerySets(QueryType.TABLE);
    }
    sanityTester.testQuestionSets();
    sanityTester.testRecordSets();
    
    if (!indexOnly) {
      if (sanityTester.printSummaryLine()) {
        System.exit(1);
      }
    }
  }
}
