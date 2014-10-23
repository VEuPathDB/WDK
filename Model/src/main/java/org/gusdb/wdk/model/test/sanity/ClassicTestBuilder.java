package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.QuerySet.QueryType;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.test.ParamValuesFactory;
import org.gusdb.wdk.model.test.ParamValuesFactory.ValuesSetWrapper;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.tests.QueryTest;
import org.gusdb.wdk.model.test.sanity.tests.QuestionTest;
import org.gusdb.wdk.model.test.sanity.tests.RecordClassTest;
import org.gusdb.wdk.model.test.sanity.tests.UncreateableTest;
import org.gusdb.wdk.model.user.User;

public class ClassicTestBuilder extends TestBuilder {

  private static final Logger LOG = Logger.getLogger(ClassicTestBuilder.class);

  private static final boolean SKIP_QUERY_TESTS = false;

  // statistics aggregator class
  public static class ClassicStatistics extends Statistics {

    private int _queriesPassed = 0;
    private int _queriesFailed = 0;
    private float _queriesDuration = 0;
    private int _recordsPassed = 0;
    private int _recordsFailed = 0;
    private float _recordsDuration = 0;
    private int _questionsPassed = 0;
    private int _questionsFailed = 0;
    private float _questionsDuration = 0;

    @Override
    public void processResult(ElementTest test, TestResult result) {
      @SuppressWarnings("unused") int a;
      if (test instanceof QueryTest) {
        _queriesDuration += result.getDurationSecs();
        a = (result.isPassed() ? _queriesPassed++ : _queriesFailed++);
      }
      else if (test instanceof QuestionTest) {
        _questionsDuration += result.getDurationSecs();
        a = (result.isPassed() ? _questionsPassed++ : _questionsFailed++);
      }
      else if (test instanceof RecordClassTest) {
        _recordsDuration += result.getDurationSecs();
        a = (result.isPassed() ? _recordsPassed++ : _recordsFailed++);
      }
      else if (test instanceof UncreateableTest) {
        // don't record duration; were unable to generate test
        switch (((UncreateableTest)test).getType()) {
          case QUERY: _queriesFailed++; break;
          case QUESTION: _questionsFailed++; break;
        }
      }
    }

    @Override
    public String getSummaryLine(TestFilter testFilter) {
      String result = isFailedOverall() ? "FAILED" : "PASSED";
      int totalPassed = _queriesPassed + _recordsPassed + _questionsPassed;
      int totalFailed = _queriesFailed + _recordsFailed + _questionsFailed;
      float testsDuration = _queriesDuration + _recordsDuration + _questionsDuration;
      return new StringBuilder()
          .append("Tests Created: " + _testsCreated + NL)
          .append("Test Filter: " + testFilter.getOriginalString() + NL)
          .append("Tests Run: " + _testsRun + NL)
          .append("Total Passed: " + totalPassed + NL)
          .append("Total Failed: " + totalFailed + NL)
          .append("Setup Duration: " + fmt(_setupDuration) + " seconds" + NL)
          .append("Test Duration: " + fmt(testsDuration) + " seconds" + NL)
          .append("Total Duration: " + fmt(_setupDuration + testsDuration) + " seconds" + NL)
          .append("   Over " + fmt(_queriesDuration) + " seconds, " + _queriesPassed + " queries passed, " + _queriesFailed + " queries failed" + NL)
          .append("   Over " + fmt(_questionsDuration) + " seconds, " + _questionsPassed + " questions passed, " + _questionsFailed + " questions failed" + NL)
          .append("   Over " + fmt(_recordsDuration) + " seconds, " + _recordsPassed + " records passed, " + _recordsFailed + " records failed" + NL)
          .append("Sanity Test " + result + NL)
          .toString();
    }

    @Override
    public boolean isFailedOverall() {
      return (_queriesFailed > 0 || _recordsFailed > 0 || _questionsFailed > 0);
    }
  }

  @Override
  public Statistics getNewStatisticsObj() {
    return new ClassicStatistics();
  }

  @Override
  public List<ElementTest> buildTestSequence(WdkModel wdkModel, User user, boolean skipWebSvcQueries)
      throws WdkModelException {
    List<ElementTest> tests = new ArrayList<>();
    if (!SKIP_QUERY_TESTS) {
      addQuerySetTests(tests, wdkModel, user, skipWebSvcQueries, QueryType.VOCAB);
      addQuerySetTests(tests, wdkModel, user, skipWebSvcQueries, QueryType.ATTRIBUTE);
      addQuerySetTests(tests, wdkModel, user, skipWebSvcQueries, QueryType.TABLE);
    }
    addQuestionSetTests(tests, wdkModel, user, skipWebSvcQueries);
    addRecordSetTests(tests, wdkModel, user);
    return tests;
  }

  private void addQuerySetTests(List<ElementTest> tests, WdkModel wdkModel, User user,
      boolean skipWebSvcQueries, QueryType forQueryType) {
    if (wdkModel.getProjectId().equals("EuPathDB") && forQueryType.equals(QueryType.TABLE))
      return; // do not process table queries for the portal
    for (QuerySet querySet : wdkModel.getAllQuerySets()) {
      QueryType queryType = querySet.getQueryTypeEnum();
      LOG.debug("Building tests for QuerySet " + querySet.getName() + " (type=" + queryType + "); filtering on " + forQueryType);
      if (!queryType.equals(forQueryType) || !SanityTester.isTestable(querySet)) continue;
      for (Query query : querySet.getQueries()) {
        addQueryTest(tests, querySet, queryType, query, user, skipWebSvcQueries);
      }
    }
  }

  private static void addQuestionSetTests(List<ElementTest> tests, WdkModel wdkModel,
      User user, boolean skipWebSvcQueries) {
    for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
      if (!SanityTester.isTestable(questionSet)) continue;
      for (Question question : questionSet.getQuestions()) {
        Query query = question.getQuery();
        if (!SanityTester.isTestable(query, skipWebSvcQueries)) continue;
        for (ValuesSetWrapper valuesSetWrapper : ParamValuesFactory.getValuesSetsNoError(user, question.getQuery())) {
          if (valuesSetWrapper.isCreated()) {
            try {
              tests.add(new QuestionTest(user, question, valuesSetWrapper.getValuesSet()));
            }
            catch (Exception e) {
              // error while generating param values sets
              LOG.error("Unable to generate paramValuesSets for question " + question.getName() + " (query=" + question.getQuery().getName() + ")", e);
              tests.add(new UncreateableTest(question, e));
            }
          }
          else {
            tests.add(new UncreateableTest(question, valuesSetWrapper.getException()));
          }
        }
      }
    }
  }

  public static void addRecordSetTests(List<ElementTest> tests, WdkModel wdkModel, User user) {
    for (RecordClassSet recordClassSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
        if (SanityTester.isTestable(recordClass)) {
          tests.add(new RecordClassTest(user, recordClass));
        }
      }
    }
  }
}
