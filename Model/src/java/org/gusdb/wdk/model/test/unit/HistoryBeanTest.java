/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class HistoryBeanTest {

    private WdkModel wdkModel;
    private UserBean user;
    private Random random;
    private RecordClass recordClass;
    private List<AnswerValueBean> answers;

    public HistoryBeanTest() throws NoSuchAlgorithmException,
            WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        this.wdkModel = UnitTestHelper.getModel();
        WdkModelBean modelBean = new WdkModelBean(wdkModel);
        this.user = modelBean.getUserFactory().getGuestUser();
        this.random = new Random();
        this.recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
        List<AnswerValue> answerValues = UnitTestHelper.getAnswerPool(recordClass);
        this.answers = new ArrayList<AnswerValueBean>();
        for (AnswerValue answerValue : answerValues) {
            this.answers.add(new AnswerValueBean(answerValue));
        }
    }

    @Test
    public void testCreateBooleanHistory() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        AnswerValueBean leftAnswer = answers.get(random.nextInt(answers.size()));
        StepBean leftStep = user.createStep(leftAnswer.getQuestion(),
                leftAnswer.getParams(), (String) null);
        AnswerValueBean rightAnswer = answers.get(random.nextInt(answers.size()));
        StepBean rightStep = user.createStep(rightAnswer.getQuestion(),
                rightAnswer.getParams(), (String) null);

        String expression = leftStep.getStepId() + " OR "
                + rightStep.getStepId();
        StepBean step = user.combineStep(expression, false);
        AnswerValueBean answerValue = step.getAnswerValue();
        Map<String, String> params = answerValue.getParams();

        Assert.assertEquals(recordClass.getFullName(),
                answerValue.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, step.getBooleanExpression());
        Assert.assertEquals(params.size(), step.getParams().size());
        Assert.assertEquals(leftAnswer.getChecksum(),
                answerValue.getFirstChildAnswer().getChecksum());
        Assert.assertEquals(rightAnswer.getChecksum(),
                answerValue.getSecondChildAnswer().getChecksum());

        // load the boolean history
        step = user.getStep(step.getStepId());
        answerValue = step.getAnswerValue();

        Assert.assertEquals(recordClass.getFullName(),
                answerValue.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, step.getBooleanExpression());
        Assert.assertEquals(params.size(), step.getParams().size());
        Assert.assertEquals(leftAnswer.getChecksum(),
                answerValue.getFirstChildAnswer().getChecksum());
        Assert.assertEquals(rightAnswer.getChecksum(),
                answerValue.getSecondChildAnswer().getChecksum());
    }
}
