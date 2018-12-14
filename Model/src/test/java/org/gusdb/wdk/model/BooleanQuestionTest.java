package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.StepContainer.ListStepContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class BooleanQuestionTest {

    private WdkModel wdkModel;
    private User user;
    private DatabaseInstance appDb;

    private RecordClass recordClass;
    private AnswerValue leftAnswerValue;
    private AnswerValue rightAnswerValue;
    private Step leftStep;
    private Step rightStep;
    private StepContainer stepContainer;

    public BooleanQuestionTest() throws Exception {
        // load the model
        wdkModel = UnitTestHelper.getModel();
        //user = UnitTestHelper.getRegisteredUser();
        user = UnitTestHelper.getGuest();
        appDb = wdkModel.getAppDb();
    }

    @Before
    public void createOperands() throws Exception {
        User regUser = UnitTestHelper.getRegisteredUser();
        leftStep = UnitTestHelper.createNormalStep(regUser);
        rightStep = UnitTestHelper.createNormalStep(regUser);

        leftAnswerValue = leftStep.getAnswerValue();
        rightAnswerValue = rightStep.getAnswerValue();

        recordClass = leftStep.getAnswerSpec().getQuestion().getRecordClass();

        ListStepContainer container = new ListStepContainer();
        container.add(leftStep);
        container.add(rightStep);
        stepContainer = container;
    }

    private int getBooleanResultSizeWithOperator(BooleanOperator operator) throws WdkModelException {

      Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
      BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
      Map<String, String> paramValues = new LinkedHashMap<String, String>();

      AnswerParam leftParam = booleanQuery.getLeftOperandParam();
      // calling answer info to make sure the answer is saved first
      paramValues.put(leftParam.getName(), String.valueOf(leftStep.getStepId()));

      AnswerParam rightParam = booleanQuery.getRightOperandParam();
      paramValues.put(rightParam.getName(), String.valueOf(rightStep.getStepId()));

      StringParam operatorParam = booleanQuery.getOperatorParam();
      paramValues.put(operatorParam.getName(), operator.getOperator(appDb.getPlatform()));

      AnswerValue answerValue = AnswerValueFactory.makeAnswer(user, AnswerSpec
          .builder(wdkModel)
          .setQuestionName(booleanQuestion.getFullName())
          .setParamValues(paramValues)
          .buildRunnable(user, stepContainer));

      return answerValue.getResultSizeFactory().getResultSize();
    }

    @Test
    public void testOrOperator() throws WdkModelException {
      int size = getBooleanResultSizeWithOperator(BooleanOperator.UNION);
      Assert.assertTrue("bigger than left",
          size >= leftAnswerValue.getResultSizeFactory().getResultSize());
      Assert.assertTrue("bigger than right",
          size >= rightAnswerValue.getResultSizeFactory().getResultSize());
    }

    @Test
    public void testAndOperator() throws WdkModelException {
      int size = getBooleanResultSizeWithOperator(BooleanOperator.INTERSECT);
      Assert.assertTrue("smaller than left",
          size <= leftAnswerValue.getResultSizeFactory().getResultSize());
      Assert.assertTrue("smaller than right",
          size <= rightAnswerValue.getResultSizeFactory().getResultSize());
    }

    @Test
    public void testLeftMinusOperator() throws WdkModelException {
      int size = getBooleanResultSizeWithOperator(BooleanOperator.LEFT_MINUS);
      Assert.assertTrue("smaller than left",
          size <= leftAnswerValue.getResultSizeFactory().getResultSize());
    }

    @Test
    public void testRightMinusOperator() throws WdkModelException {
      int size = getBooleanResultSizeWithOperator(BooleanOperator.RIGHT_MINUS);
      Assert.assertTrue("smaller than right",
          size <= rightAnswerValue.getResultSizeFactory().getResultSize());
    }

    @Test
    public void testLeftOnlyOperator() throws WdkModelException {
      int size = getBooleanResultSizeWithOperator(BooleanOperator.LEFT_ONLY);
      Assert.assertTrue("equal to left",
          size == leftAnswerValue.getResultSizeFactory().getResultSize());
    }

    @Test
    public void testRightOnlyOperator() throws WdkModelException {
      int size = getBooleanResultSizeWithOperator(BooleanOperator.RIGHT_ONLY);
      Assert.assertTrue("equal to right",
          size == rightAnswerValue.getResultSizeFactory().getResultSize());
    }
}
