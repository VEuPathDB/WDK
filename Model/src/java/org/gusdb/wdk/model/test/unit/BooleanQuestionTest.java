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
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
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
    private List<Answer> answers;
    private Random random= new Random();

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
        answers = UnitTestHelper.getAnswerPool(recordClass);
    }
    
    @Test
    public void testOrOperator() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Answer left = answers.get(random.nextInt(answers.size()));
        Answer right = answers.get(random.nextInt(answers.size()));

        Question booleanQuestion = wdkModel.getBooleanQuestion(recordClass);
        BooleanQuery booleanQuery = (BooleanQuery) booleanQuestion.getQuery();
        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();

        AnswerParam leftOperand = booleanQuery.getLeftOperandParam();
        // calling answer info to make sure the answer is saved first
        paramValues.put(leftOperand.getName(),
                left.getAnswerInfo().getAnswerChecksum());

        AnswerParam rightOperand = booleanQuery.getRightOperandParam();
        paramValues.put(rightOperand.getName(),
                right.getAnswerInfo().getAnswerChecksum());

        StringParam operator = booleanQuery.getOperatorParam();
        paramValues.put(operator.getName(), BooleanOperator.Union.getOperator());

        StringParam leftFilter = booleanQuery.getLeftFilterParam();
        paramValues.put(leftFilter.getName(), null);

        StringParam rightFilter = booleanQuery.getRightFilterParam();
        paramValues.put(rightFilter.getName(), null);

        StringParam expansion = booleanQuery.getUseBooleanFilter();
        paramValues.put(expansion.getName(), "false");

        Answer answer = booleanQuestion.makeAnswer(paramValues);
        
        // try to get the summary of the answer
        logger.debug(answer.printAsTable());

        Assert.assertTrue(answer.getResultSize() >= left.getResultSize());
        Assert.assertTrue(answer.getResultSize() >= right.getResultSize());
    }
}
