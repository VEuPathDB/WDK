/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
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
    private DBPlatform platform;

    private RecordClass recordClass;
    private AnswerValue leftAnswerValue;
    private AnswerValue rightAnswerValue;
    private String leftStepId;
    private String rightStepId;

    public BooleanQuestionTest() throws Exception {
        // load the model
        wdkModel = UnitTestHelper.getModel();
        //user = UnitTestHelper.getRegisteredUser();
        user = UnitTestHelper.getGuest();
        platform = wdkModel.getQueryPlatform();
    }

    @Before
    public void createOperands() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Step left = UnitTestHelper.createNormalStep(user);
        Step right = UnitTestHelper.createNormalStep(user);

        leftStepId = Integer.toString(left.getStepId());
        rightStepId = Integer.toString(right.getStepId());
        leftAnswerValue = left.getAnswerValue();
        rightAnswerValue = right.getAnswerValue();
        recordClass = left.getQuestion().getRecordClass();
    }

    @Test
    public void testOrOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();

        AnswerParam leftParam = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftParam.getName(), leftStepId);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightStepId);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.UNION.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(user,
                paramValues, true, 0);
        int size = answerValue.getResultSize();

        Assert.assertTrue("bigger than left",
                size >= leftAnswerValue.getResultSize());
        Assert.assertTrue("bigger than right",
                size >= rightAnswerValue.getResultSize());
    }

    @Test
    public void testAndOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();

        AnswerParam leftParam = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftParam.getName(), leftStepId);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightStepId);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.INTERSECT.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(user,
                paramValues, true, 0);
        int size = answerValue.getResultSize();

        Assert.assertTrue("smaller than left",
                size <= leftAnswerValue.getResultSize());
        Assert.assertTrue("smaller than right",
                size <= rightAnswerValue.getResultSize());
    }

    @Test
    public void testLeftMinusOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();

        AnswerParam leftParam = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftParam.getName(), leftStepId);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightStepId);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.LEFT_MINUS.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(user,
                paramValues, true, 0);
        int size = answerValue.getResultSize();

        Assert.assertTrue("smaller than left",
                size <= leftAnswerValue.getResultSize());
    }

    @Test
    public void testRightMinueOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();

        AnswerParam leftParam = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftParam.getName(), leftStepId);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightStepId);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.INTERSECT.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(user,
                paramValues, true, 0);
        int size = answerValue.getResultSize();

        Assert.assertTrue("smaller than right",
                size <= rightAnswerValue.getResultSize());
    }
}
