/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class StepTest {

    private WdkModel wdkModel;
    private User user;
    private Random random;
    private RecordClass recordClass;
    private List<AnswerValue> answerValues;

    public StepTest() throws NoSuchAlgorithmException, WdkModelException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, IOException, SAXException, SQLException,
            JSONException, WdkUserException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        this.wdkModel = UnitTestHelper.getModel();
        this.user = wdkModel.getUserFactory().createGuestUser();
        this.random = new Random();
        this.recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
        this.answerValues = UnitTestHelper.getAnswerPool(recordClass);
    }

    @Test
    public void testCreateHistory() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        AnswerValue answerValue = answerValues.get(random.nextInt(answerValues.size()));
        Question question = answerValue.getQuestion();
        Map<String, String> paramValues = answerValue.getIdsQueryInstance().getValues();
        Param[] params = answerValue.getQuestion().getParams();
        Step step = user.createStep(question, paramValues, (String) null);

        Assert.assertTrue(step.getDisplayId() > 0);
        Assert.assertEquals(answerValue.getChecksum(),
                step.getAnswer().getAnswerChecksum());
        Assert.assertEquals(params.length, step.getDisplayParams().size());
    }

    @Test
    public void testCreateBooleanHistory() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException {
        AnswerValue leftAnswer = answerValues.get(random.nextInt(answerValues.size()));
        Step leftStep = user.createStep(leftAnswer.getQuestion(),
                leftAnswer.getIdsQueryInstance().getValues(), (String) null);
        AnswerValue rightAnswer = answerValues.get(random.nextInt(answerValues.size()));
        Step rightStep = user.createStep(rightAnswer.getQuestion(),
                rightAnswer.getIdsQueryInstance().getValues(), (String) null);

        String expression = leftStep.getDisplayId() + " OR "
                + rightStep.getDisplayId();
        Step step = user.combineStep(expression);
        AnswerValue answerValue = step.getAnswer().getAnswerValue();
        Param[] params = answerValue.getQuestion().getParams();

        Assert.assertEquals(recordClass.getFullName(),
                answerValue.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, step.getBooleanExpression());
        Assert.assertEquals(params.length, step.getDisplayParams().size());

        // load the boolean history
        step = user.getStep(step.getDisplayId());
        answerValue = step.getAnswer().getAnswerValue();

        Assert.assertEquals(recordClass.getFullName(),
                answerValue.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, step.getBooleanExpression());
        Assert.assertEquals(params.length, step.getDisplayParams().size());
    }
}
