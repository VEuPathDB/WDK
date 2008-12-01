/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.StringParam;
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
    private DBPlatform platform;

    private RecordClass recordClass;
    private String leftValue;
    private String rightValue;

    public BooleanQuestionTest() throws Exception {
        // load the model
        wdkModel = UnitTestHelper.getModel();
        platform = wdkModel.getQueryPlatform();
    }

    @Before
    public void createOperands() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Step left = UnitTestHelper.createNormalStep(user);
        Step right = UnitTestHelper.createNormalStep(user);

        leftValue = left.getAnswer().getAnswerChecksum();
        rightValue = right.getAnswer().getAnswerChecksum();
        recordClass = left.getAnswer().getAnswerValue().getQuestion().getRecordClass();
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
        paramValues.put(leftParam.getName(), leftValue);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightValue);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(), BooleanOperator.UNION.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(paramValues);
        int size = answerValue.getResultSize();

        AnswerValue leftAnswer = leftParam.getAnswerValue(leftValue);
        AnswerValue rightAnswer = rightParam.getAnswerValue(rightValue);

        Assert.assertTrue("bigger than left",
                size >= leftAnswer.getResultSize());
        Assert.assertTrue("bigger than right",
                size >= rightAnswer.getResultSize());
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
        paramValues.put(leftParam.getName(), leftValue);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightValue);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.INTERSECT.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(paramValues);
        int size = answerValue.getResultSize();

        AnswerValue leftAnswer = leftParam.getAnswerValue(leftValue);
        AnswerValue rightAnswer = rightParam.getAnswerValue(rightValue);

        Assert.assertTrue("smaller than left",
                size <= leftAnswer.getResultSize());
        Assert.assertTrue("smaller than right",
                size <= rightAnswer.getResultSize());
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
        paramValues.put(leftParam.getName(), leftValue);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightValue);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.LEFT_MINUS.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(paramValues);
        int size = answerValue.getResultSize();

        AnswerValue leftAnswer = leftParam.getAnswerValue(leftValue);

        Assert.assertTrue("smaller than left",
                size <= leftAnswer.getResultSize());
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
        paramValues.put(leftParam.getName(), leftValue);

        AnswerParam rightParam = booleanQuery.getRightOperandParam();
        paramValues.put(rightParam.getName(), rightValue);

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.INTERSECT.getOperator(platform));

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(paramValues);
        int size = answerValue.getResultSize();

        AnswerValue rightAnswer = rightParam.getAnswerValue(rightValue);

        Assert.assertTrue("smaller than right",
                size <= rightAnswer.getResultSize());
    }
}
