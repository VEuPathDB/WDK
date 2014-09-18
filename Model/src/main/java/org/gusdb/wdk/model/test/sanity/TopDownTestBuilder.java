package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.SanityTester.TestBuilder;
import org.gusdb.wdk.model.test.sanity.tests.QuestionTest;
import org.gusdb.wdk.model.test.sanity.tests.UncreateableTest;
import org.gusdb.wdk.model.user.User;

public class TopDownTestBuilder implements TestBuilder {

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
    /*
    for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
      if (!questionSet.getDoNotTest()) {
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
    */
    return tests;
  }

}
