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

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
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
    private List<AnswerBean> answers;

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
        List<Answer> answers = UnitTestHelper.getAnswerPool(recordClass);
        this.answers = new ArrayList<AnswerBean>();
        for (Answer answer : answers) {
            this.answers.add(new AnswerBean(answer));
        }
    }

    @Test
    public void testCreateBooleanHistory() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        AnswerBean leftAnswer = answers.get(random.nextInt(answers.size()));
        HistoryBean leftHistory = user.createHistory(leftAnswer);
        AnswerBean rightAnswer = answers.get(random.nextInt(answers.size()));
        HistoryBean rightHistory = user.createHistory(rightAnswer);

        String expression = leftHistory.getHistoryId() + " OR "
                + rightHistory.getHistoryId();
        HistoryBean history = user.combineHistory(expression, false);
        AnswerBean answer = history.getAnswer();
        Map<String, Object> params = answer.getParams();

        Assert.assertEquals(recordClass.getFullName(),
                answer.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, history.getBooleanExpression());
        Assert.assertEquals(params.size(), history.getParams().size());
        Assert.assertEquals(leftAnswer.getChecksum(),
                answer.getFirstChildAnswer().getChecksum());
        Assert.assertEquals(rightAnswer.getChecksum(),
                answer.getSecondChildAnswer().getChecksum());

        // load the boolean history
        history = user.getHistory(history.getHistoryId());
        answer = history.getAnswer();

        Assert.assertEquals(recordClass.getFullName(),
                answer.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, history.getBooleanExpression());
        Assert.assertEquals(params.size(), history.getParams().size());
        Assert.assertEquals(leftAnswer.getChecksum(),
                answer.getFirstChildAnswer().getChecksum());
        Assert.assertEquals(rightAnswer.getChecksum(),
                answer.getSecondChildAnswer().getChecksum());
    }
}
