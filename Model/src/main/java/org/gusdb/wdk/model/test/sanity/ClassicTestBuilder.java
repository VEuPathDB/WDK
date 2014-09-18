package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.SanityTester.TestBuilder;
import org.gusdb.wdk.model.test.sanity.tests.AttributeQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.QueryTest;
import org.gusdb.wdk.model.test.sanity.tests.QuestionTest;
import org.gusdb.wdk.model.test.sanity.tests.RecordClassTest;
import org.gusdb.wdk.model.test.sanity.tests.TableQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.TableTotalQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.UncreateableTest;
import org.gusdb.wdk.model.test.sanity.tests.VocabQueryTest;
import org.gusdb.wdk.model.user.User;

public class ClassicTestBuilder implements TestBuilder {

  private static final Logger LOG = Logger.getLogger(ClassicTestBuilder.class);

  private static final boolean SKIP_QUERY_TESTS = true;

  // statistics aggregator class
  public static class OriginalStatistics extends Statistics {

    public int queriesPassed = 0;
    public int queriesFailed = 0;
    public float queriesDuration = 0;
    public int recordsPassed = 0;
    public int recordsFailed = 0;
    public float recordsDuration = 0;
    public int questionsPassed = 0;
    public int questionsFailed = 0;
    public float questionsDuration = 0;

    @Override
    public void processResult(ElementTest test, TestResult result) {
      @SuppressWarnings("unused") int a;
      if (test instanceof QueryTest) {
        queriesDuration += result.getDurationSecs();
        a = (result.isPassed() ? queriesPassed++ : queriesFailed++);
      }
      else if (test instanceof QuestionTest) {
        questionsDuration += result.getDurationSecs();
        a = (result.isPassed() ? questionsPassed++ : questionsFailed++);
      }
      else if (test instanceof RecordClassTest) {
        recordsDuration += result.getDurationSecs();
        a = (result.isPassed() ? recordsPassed++ : recordsFailed++);
      }
      else if (test instanceof UncreateableTest) {
        // don't record duration; were unable to generate test
        switch (((UncreateableTest)test).getType()) {
          case QUERY: queriesFailed++; break;
          case QUESTION: questionsFailed++; break;
        }
      }
    }

    @Override
    public String getSummaryLine(TestFilter testFilter) {
      String result = isFailedOverall() ? "FAILED" : "PASSED";
      int totalPassed = queriesPassed + recordsPassed + questionsPassed;
      int totalFailed = queriesFailed + recordsFailed + questionsFailed;
      float testsDuration = queriesDuration + recordsDuration + questionsDuration;
      return new StringBuilder()
          .append("Tests Created: " + _testsCreated + NL)
          .append("Test Filter: " + testFilter.getOriginalString() + NL)
          .append("Tests Run: " + _testsRun + NL)
          .append("Total Passed: " + totalPassed + NL)
          .append("Total Failed: " + totalFailed + NL)
          .append("Setup Duration: " + fmt(_setupDuration) + " seconds" + NL)
          .append("Test Duration: " + fmt(testsDuration) + " seconds" + NL)
          .append("Total Duration: " + fmt(_setupDuration + testsDuration) + " seconds" + NL)
          .append("   Over " + fmt(queriesDuration) + " seconds, " + queriesPassed + " queries passed, " + queriesFailed + " queries failed" + NL)
          .append("   Over " + fmt(questionsDuration) + " seconds, " + questionsPassed + " questions passed, " + questionsFailed + " questions failed" + NL)
          .append("   Over " + fmt(recordsDuration) + " seconds, " + recordsPassed + " records passed, " + recordsFailed + " records failed" + NL)
          .append("Sanity Test " + result + NL)
          .toString();
    }

    @Override
    public boolean isFailedOverall() {
      return (queriesFailed > 0 || recordsFailed > 0 || questionsFailed > 0);
    }
  }

  @Override
  public Statistics getNewStatisticsObj() {
    return new OriginalStatistics();
  }

  @Override
  public List<ElementTest> buildTestSequence(WdkModel wdkModel, User user, boolean skipWebSvcQueries)
      throws WdkModelException {
    List<ElementTest> tests = new ArrayList<>();
    if (!SKIP_QUERY_TESTS) {
      addQuerySetTests(tests, wdkModel, user, QueryType.VOCAB);
      addQuerySetTests(tests, wdkModel, user, QueryType.ATTRIBUTE);
      addQuerySetTests(tests, wdkModel, user, QueryType.TABLE);
    }
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
            int numParamValuesSets = query.getNumParamValuesSets();
            try {
              for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
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
              }
            }
            catch (Exception e) {
              // error while generating param values sets
              LOG.error("Unable to generate paramValuesSets for query " + query.getName(), e);
              // to keep the index correct, add already failed tests for each of the param values sets we expected
              for (int i = 0; i < numParamValuesSets; i++) {
                tests.add(new UncreateableTest(querySet, query, e));
              }
            }
          }
        }
      }
    }
  }

  private static void addQuestionSetTests(List<ElementTest> tests, WdkModel wdkModel,
      User user, boolean skipWebSvcQueries) {
    for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
      if (!questionSet.getDoNotTest()) {
        for (Question question : questionSet.getQuestions()) {
          Query query = question.getQuery();
          if (!(skipWebSvcQueries && query instanceof ProcessQuery) &&
              !query.getDoNotTest() && !query.getQuerySet().getDoNotTest()) {
            int numParamValuesSets = question.getQuery().getNumParamValuesSets();
            try {
              for (ParamValuesSet paramValuesSet : question.getQuery().getParamValuesSets()) {
                tests.add(new QuestionTest(user, question, paramValuesSet));
              }
            }
            catch (Exception e) {
              // error while generating param values sets
              LOG.error("Unable to generate paramValuesSets for question " + question.getName() + " (query=" + question.getQuery().getName() + ")", e);
              // to keep the index correct, add already failed tests for each of the param values sets we expected
              for (int i = 0; i < numParamValuesSets; i++) {
                tests.add(new UncreateableTest(question, e));
              }
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
}
