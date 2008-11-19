/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class BooleanQuestionTest {

    private static final Logger logger = Logger.getLogger(BooleanQuestionTest.class);

    private WdkModel wdkModel;
    private RecordClass recordClass;
    private List<AnswerValue> answerValues;
    private Random random = new Random();

    public BooleanQuestionTest() throws NoSuchAlgorithmException,
            WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // load the model
        wdkModel = UnitTestHelper.getModel();
        // pick the first record class
        recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
        answerValues = UnitTestHelper.getAnswerPool(recordClass);
    }

    @Test
    public void testOrOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        AnswerValue left = answerValues.get(random.nextInt(answerValues.size()));
        AnswerValue right = answerValues.get(random.nextInt(answerValues.size()));

        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();

        AnswerParam leftOperand = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftOperand.getName(),
                left.getAnswer().getAnswerChecksum());

        AnswerParam rightOperand = booleanQuery.getRightOperandParam();
        paramValues.put(rightOperand.getName(),
                right.getAnswer().getAnswerChecksum());

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(), BooleanOperator.UNION.getOperator());

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(paramValues);

        // try to get the summary of the answer
        logger.debug(answerValue.printAsTable());

        Assert.assertTrue(answerValue.getResultSize() >= left.getResultSize());
        Assert.assertTrue(answerValue.getResultSize() >= right.getResultSize());
    }

    @Test
    public void testAndOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        AnswerValue left = answerValues.get(random.nextInt(answerValues.size()));
        AnswerValue right = answerValues.get(random.nextInt(answerValues.size()));

        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();

        AnswerParam leftOperand = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftOperand.getName(),
                left.getAnswer().getAnswerChecksum());

        AnswerParam rightOperand = booleanQuery.getRightOperandParam();
        paramValues.put(rightOperand.getName(),
                right.getAnswer().getAnswerChecksum());

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(),
                BooleanOperator.INTERSECT.getOperator());

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        AnswerValue answerValue = booleanQuestion.makeAnswerValue(paramValues);

        // try to get the summary of the answer
        logger.debug(answerValue.printAsTable());

        Assert.assertTrue(answerValue.getResultSize() <= left.getResultSize());
        Assert.assertTrue(answerValue.getResultSize() <= right.getResultSize());
    }
}
