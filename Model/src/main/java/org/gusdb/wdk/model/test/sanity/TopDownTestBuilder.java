package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.test.ParamValuesFactory;
import org.gusdb.wdk.model.test.ParamValuesFactory.ValuesSetWrapper;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.tests.QuestionTest;
import org.gusdb.wdk.model.test.sanity.tests.UncreateableTest;
import org.gusdb.wdk.model.test.sanity.tests.WrappedTableQueryTest;
import org.gusdb.wdk.model.user.User;

public class TopDownTestBuilder extends TestBuilder {

  private static final Logger LOG = Logger.getLogger(TopDownTestBuilder.class);

  public static class QueryStatistics extends Statistics {

    private int _numPassed = 0;
    private float _duration = 0;

    @Override
    public void processResult(ElementTest test, TestResult result) {
      _numPassed += (result.isPassed() ? 1 : 0);
      _duration += result.getDurationSecs();
    }

    @Override
    public String getSummaryLine(TestFilter testFilter) {
      return new StringBuilder()
          .append("Tests Created: " + _testsCreated + NL)
          .append("Test Filter: " + testFilter.getOriginalString() + NL)
          .append("Tests Run: " + _testsRun + NL)
          .append("Total Passed: " + _numPassed + NL)
          .append("Total Failed: " + (_testsRun - _numPassed) + NL)
          .append("Setup Duration: " + fmt(_setupDuration) + " seconds" + NL)
          .append("Test Duration: " + fmt(_duration) + " seconds" + NL)
          .append("Total Duration: " + fmt(_setupDuration + _duration) + " seconds" + NL)
          .append("Sanity Test " + (isFailedOverall() ? "FAILED" : "PASSED") + NL)
          .toString();
    }

    @Override
    public boolean isFailedOverall() {
      return _numPassed < _testsRun;
    }
  }

  @Override
  public Statistics getNewStatisticsObj() {
    return new QueryStatistics();
  }

  @Override
  public List<ElementTest> buildTestSequence(WdkModel wdkModel, User user, boolean skipWebSvcQueries)
      throws WdkModelException {
    List<ElementTest> tests = new ArrayList<>();
    for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
      if (!SanityTester.isTestable(questionSet)) continue;
      for (Question question : questionSet.getQuestions()) {
        // Step 1: run vocab queries for any and all params associated with this question
        Param[] params = question.getParams();
        for (Param param : params) {
          if (param instanceof AbstractEnumParam) {
            AbstractEnumParam enumParam = (AbstractEnumParam)param;
            enumParam.getDependedParams();
          }
        }
        
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

    for (RecordClassSet recordClassSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
        if (!recordClass.getDoNotTest()) {
          // add tests for table queries
          for (Query query : recordClass.getTableQueries().values()) {
            for (ValuesSetWrapper valuesSetWrapper : ParamValuesFactory.getValuesSetsNoError(user, query)) {
              tests.add(new QueryTestBuilder(user, query.getQuerySet(), query, valuesSetWrapper) {
                @Override public ElementTest getTypedQueryTest(ParamValuesSet paramValuesSet) throws WdkModelException {
                  return new WrappedTableQueryTest(user, querySet, query, paramValuesSet);
                }}.getQueryTest());
            }
          }
          // add tests for attribute queries
          for (Query query : recordClass.getAttributeQueries().values()) {
            QuerySet querySet = query.getQuerySet();
            addQueryTest(tests, querySet, querySet.getQueryTypeEnum(), query, user, skipWebSvcQueries);
          }
        }
      }
    }
    return tests;
  }
}
