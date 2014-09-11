package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.QuerySet.QueryType;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.test.sanity.tests.AttributeQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.QuestionTest;
import org.gusdb.wdk.model.test.sanity.tests.RecordClassTest;
import org.gusdb.wdk.model.test.sanity.tests.TableQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.TableTotalQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.VocabQueryTest;
import org.gusdb.wdk.model.user.User;

/**
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
    
    public String getSummaryLine(TestFilter testFilter) {
      String result = isFailedOverall() ? "FAILED" : "PASSED";
      int totalPassed = queriesPassed + recordsPassed + questionsPassed;
      int totalFailed = queriesFailed + recordsFailed + questionsFailed;
      return new StringBuilder()
          .append("TestFilter: " + testFilter.getOriginalString() + NL)
          .append("Total Passed: " + totalPassed + NL)
          .append("Total Failed: " + totalFailed + NL)
          .append("   " + queriesPassed + " queries passed, " + queriesFailed + " queries failed" + NL)
          .append("   " + recordsPassed + " records passed, " + recordsFailed + " records failed" + NL)
          .append("   " + questionsPassed + " questions passed, " + questionsFailed + " questions failed" + NL)
          .append("Sanity Test " + result + NL)
          .toString();
    }

    public boolean isFailedOverall() {
      return (queriesFailed > 0 || recordsFailed > 0 || questionsFailed > 0);
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
    addQuerySetTests(tests, wdkModel, user, QueryType.VOCAB);
    addQuerySetTests(tests, wdkModel, user, QueryType.ATTRIBUTE);
    addQuerySetTests(tests, wdkModel, user, QueryType.TABLE);
    addQuestionSetTests(tests, wdkModel, user, skipWebSvcQueries);
    addRecordSetTests(tests, wdkModel, user);
    return tests;
  }

  private static void addQuerySetTests(List<ElementTest> tests, WdkModel wdkModel, User user, QueryType forQueryType)
      throws WdkModelException {
    if (wdkModel.getProjectId().equals("EuPathDB") && forQueryType.equals(QueryType.TABLE))
      return; // do not process table queries for the portal
    for (QuerySet querySet : wdkModel.getAllQuerySets()) {
      QueryType queryType = querySet.getQueryTypeEnum();
      LOG.debug("Building tests for QuerySet " + querySet.getName() + " (type=" + queryType + "); filtering on " + forQueryType);
      if (queryType.equals(forQueryType) && !querySet.getDoNotTest()) {
        for (Query query : querySet.getQueries()) {
          if (!query.getDoNotTest()) {
            LOG.debug("   Building tests for Query " + query.getName() + " using " + query.getParamValuesSets().size() + " ParamValuesSets");
            //FIXME for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
            ParamValuesSet paramValuesSet = query.getDefaultParamValuesSet();
              switch (queryType) {
                case VOCAB:
                  tests.add(new VocabQueryTest(user, querySet, query, paramValuesSet));
                  break;
                case ATTRIBUTE:
                  tests.add(new AttributeQueryTest(user, querySet, query, paramValuesSet));
                  break;
                case TABLE:
                  tests.add(new TableQueryTest(user, querySet, query, paramValuesSet));
                  // perform additional test for table queries
                  tests.add(new TableTotalQueryTest(user, querySet, query, paramValuesSet));
                default:
                  // TABLE_TOTAL should never be a QuerySet's query type; it exists only for sanity tests.
                  // All other query types are not sanity testable
                  LOG.debug("QuerySet " + querySet.getName() + " with type " +
                      queryType + " is not sanity testable.  Skipping...");
              }
            //}
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

  public List<TestResult> runTests() throws Exception {
    List<TestResult> results = new ArrayList<>();
    for (int i = 1; i <= _tests.size(); i++) {
      ElementTest element = _tests.get(i-1);

      if (_testFilter.filterOutTest(i)) continue;

      LOG.info(" [test: " + i + "/" + _tests.size() + "] " + element.getTestName());

      if (_indexOnly) continue;

      // performing test
      TestResult result = new TestResult(element);
      try {
        result = element.test(_stats);
      }
      catch (Exception e) {
        result.setReturned("Exception thrown");
        result.setCaughtException(e);
      }

      result.setIndex(i);
      results.add(result);

      // dump test result
      if (!result.isPassed() || !_failuresOnly) {
        LOG.info(NL + result.getResultString());
      }
    }

    // check the connection usage
    if (_appDb.getConnectionsCurrentlyOpen() > 0) {
      LOG.error("Detected connections leaks after processing:");
      LOG.error(_appDb.getUnclosedConnectionInfo());
    }
    
    return results;
  }

  public String getSummaryLine() {
    return _stats.getSummaryLine(_testFilter);
  }
  
  public boolean isFailedOverall() {
    return _stats.isFailedOverall();
  }
}
