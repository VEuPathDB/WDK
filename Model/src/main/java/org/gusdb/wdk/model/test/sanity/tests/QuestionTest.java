package org.gusdb.wdk.model.test.sanity.tests;

import java.util.Map;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.RangeCountTestUtil;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class QuestionTest implements ElementTest {

  private final User _user;
  private final Question _question;
  private final ParamValuesSet _paramValuesSet;

  public QuestionTest(User user, Question question, ParamValuesSet paramValuesSet) {
    _user = user;
    _question = question;
    _paramValuesSet = paramValuesSet;
  }

  @Override
  public String getTestName() {
    return getTestName(_question);
  }
  
  public static String getTestName(Question question) {
    return "QUESTION " + question.getFullName() +
        " (query " + question.getQuery().getFullName() + ")";
  }

  @Override
  public String getCommand() {
    return "wdkSummary -model " + _question.getWdkModel().getProjectId() +
        " -question " + _question.getFullName() + " -rows 1 100" +
        " -params " + _paramValuesSet.getCmdLineString();
  }

  @Override
  public TestResult test(Statistics stats) throws Exception {
    int sanityMin = _paramValuesSet.getMinRows();
    int sanityMax = _paramValuesSet.getMaxRows();
    TestResult result = new TestResult(this);
    result.setExpected("Expect [" + sanityMin + " - " + sanityMax + "] rows" +
        ((sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS) ? "" : " (default)"));
    if (_question.getQuery() instanceof SqlQuery) {
      ((SqlQuery)_question.getQuery()).setIsCacheable(false);
    }
    AnswerValue answerValue = AnswerValueFactory.makeAnswer(_user,
        AnswerSpec.builder(_question.getWdkModel())
        .setQuestionFullName(_question.getFullName())
        .setParamValues(_paramValuesSet.getParamValues())
        .buildRunnable(_user, StepContainer.emptyContainer()));
    int resultSize = answerValue.getResultSizeFactory().getResultSize();

    // get the summary attribute list
    Map<String, AttributeField> summary = _question.getSummaryAttributeFieldMap();

    // iterate through the page and try every summary attribute of each record
    for (RecordInstance record : answerValue.getRecordInstances()) {
      StringBuffer sb = new StringBuffer();
      for (String attrName : summary.keySet()) {
        sb.append(record.getAttributeValue(attrName));
        sb.append('\t');
      }
    }

    result.setReturned(resultSize + " rows returned");
    RangeCountTestUtil.applyCountAssessment(resultSize, sanityMin, sanityMax,
        false, result);
    return result;
  }
}
