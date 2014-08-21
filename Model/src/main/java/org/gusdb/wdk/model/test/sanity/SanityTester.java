package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
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

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(SanityTester.class);

  private static final String BANNER_LINE_TOP = "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv";
  private static final String BANNER_LINE_BOT = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^";

  private static void OUT (Object... obj) { System.out.println(FormatUtil.join(obj, ", ")); }
  private static void ERR (Object... obj) { System.err.println(FormatUtil.join(obj, ", ")); }

  public interface ElementTest {
    public String getTestName();
    public TestResult test(Statistics stats) throws Exception;
    public String getCommand();
  }

  // statistics aggregator class
  public static class Statistics {

    public int testCount = 0;
    public int queriesPassed = 0;
    public int queriesFailed = 0;
    public int recordsPassed = 0;
    public int recordsFailed = 0;
    public int questionsPassed = 0;
    public int questionsFailed = 0;

    public boolean printSummaryLine(TestFilter testFilter) {

      boolean failedOverall = (queriesFailed > 0 || recordsFailed > 0 || questionsFailed > 0);
      String result = failedOverall ? "FAILED" : "PASSED";

      int totalPassed = queriesPassed + recordsPassed + questionsPassed;
      int totalFailed = queriesFailed + recordsFailed + questionsFailed;

      StringBuffer resultLine = new StringBuffer("***Sanity test summary***" + NL);
      resultLine.append("TestFilter: " + testFilter.getOriginalString() + NL);
      resultLine.append("Total Passed: " + totalPassed + NL);
      resultLine.append("Total Failed: " + totalFailed + NL);
      resultLine.append("   " + queriesPassed + " queries passed, "
          + queriesFailed + " queries failed" + NL);
      resultLine.append("   " + recordsPassed + " records passed, "
          + recordsFailed + " records failed" + NL);
      resultLine.append("   " + questionsPassed + " questions passed, "
          + questionsFailed + " questions failed" + NL);
      resultLine.append("Sanity Test " + result + NL);
      OUT(resultLine.toString());
      return failedOverall;
    }
  }

  private final DatabaseInstance _appDb;
  private final boolean _indexOnly;
  private final boolean _failuresOnly;
  private final TestFilter _testFilter;
  private final Statistics _stats;
  private final List<ElementTest> _tests;

  public SanityTester(WdkModel wdkModel, TestFilter testFilter, boolean failuresOnly,
      boolean indexOnly, boolean skipWebSvcQueries) throws WdkModelException {
    _appDb = wdkModel.getAppDb();
    _indexOnly = indexOnly;
    _failuresOnly = failuresOnly;
    _testFilter = testFilter;
    _stats = new Statistics();
    _tests = buildTestSequence(wdkModel, wdkModel.getSystemUser(), skipWebSvcQueries);
  }

  private static List<ElementTest> buildTestSequence(WdkModel wdkModel, User user, boolean skipWebSvcQueries)
      throws WdkModelException {
    List<ElementTest> tests = new ArrayList<>();
    addQuerySetTests(tests, wdkModel, user, QuerySet.TYPE_VOCAB);
    addQuerySetTests(tests, wdkModel, user, QuerySet.TYPE_ATTRIBUTE);
    addQuerySetTests(tests, wdkModel, user, QuerySet.TYPE_TABLE);
    addQuestionSetTests(tests, wdkModel, user, skipWebSvcQueries);
    addRecordSetTests(tests, wdkModel, user);
    return tests;
  }

  private static void addQuerySetTests(List<ElementTest> tests, WdkModel wdkModel, User user, String forQueryType)
      throws WdkModelException {
    if (wdkModel.getProjectId().equals("EuPathDB") && forQueryType.equals(QuerySet.TYPE_TABLE))
      return; // do not process table queries for the portal
    for (QuerySet querySet : wdkModel.getAllQuerySets()) {
      if (querySet.getQueryType().equals(forQueryType) && !querySet.getDoNotTest()) {
        for (Query query : querySet.getQueries()) {
          if (!query.getDoNotTest()) {
            for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
              tests.add(new QueryTest(user, querySet, query, paramValuesSet));
            }
            // perform additional test for table queries
            if (querySet.getQueryType().equals(QuerySet.TYPE_TABLE)) {
              tests.add(new QueryTest(user, querySet, query, null));
            }
          }
        }
      }
    }
  }

  private static void addQuestionSetTests(List<ElementTest> tests, WdkModel wdkModel,
      User user, boolean skipWebSvcQueries) throws WdkModelException {
    for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
      if (!questionSet.getDoNotTest()) {
        for (Question question : questionSet.getQuestions()) {
          Query query = question.getQuery();
          if (!(skipWebSvcQueries && query instanceof ProcessQuery) &&
              !query.getDoNotTest() && !query.getQuerySet().getDoNotTest()) {
            for (ParamValuesSet paramValuesSet : question.getQuery().getParamValuesSets()) {
              tests.add(new QuestionTest(user, question, paramValuesSet));
            }
          }
        }
      }
    }
  }

  public static void addRecordSetTests(List<ElementTest> tests, WdkModel wdkModel, User user) {
    for (RecordClassSet recordClassSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
        if (!recordClass.getDoNotTest()) {
          tests.add(new RecordClassTest(user, recordClass));
        }
      }
    }
  }

  public void runTests() throws Exception {
    for (int i = 0; i < _tests.size(); i++) {
      ElementTest element = _tests.get(i);

      if (_testFilter.filterOutTest(i)) continue;

      OUT(" [test: " + i + "] " + element.getTestName());

      if (_indexOnly) continue;

      // performing test
      TestResult result = new TestResult();
      try {
        result = element.test(_stats);
        if (result.passed) {
          result.prefix = "";
          result.status = " passed.";
        }
      }
      catch (Exception e) {
        result.returned = " It threw an exception.";
        result.caughtException = e;
      }

      // test output
      if (!result.passed) OUT(BANNER_LINE_TOP);
      if (result.caughtException != null) {
        ERR(FormatUtil.getStackTrace(result.caughtException));
      }
      else {
        String msg = result.prefix + result.getDurationSecs() +
            " [test: " + i + "]" + " " + element.getTestName() +
            result.status + result.returned + result.expected +
            " [ " + element.getCommand() + " ] " + NL;
        if (!result.passed || !_failuresOnly) OUT(msg);
      }
      if (!result.passed) OUT(BANNER_LINE_BOT + NL);
    }

    // check the connection usage
    if (_appDb.getConnectionsCurrentlyOpen() > 0) {
      ERR("Detected connections leaks after processing:");
      ERR(_appDb.getUnclosedConnectionInfo());
    }
  }

  public boolean printSummaryLine() {
    return _stats.printSummaryLine(_testFilter);
  }
}
