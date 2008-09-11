/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class HistoryTest {

    private WdkModel wdkModel;
    private User user;
    private Random random;
    private RecordClass recordClass;
    private List<Answer> answers;

    public HistoryTest() throws NoSuchAlgorithmException, WdkModelException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, IOException, SAXException, SQLException,
            JSONException, WdkUserException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        this.wdkModel = UnitTestHelper.getModel();
        this.user = wdkModel.getUserFactory().createGuestUser();
        this.random = new Random();
        this.recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
        this.answers = UnitTestHelper.getAnswerPool(recordClass);
    }

    @Test
    public void testCreateHistory() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        Answer answer = answers.get(random.nextInt(answers.size()));
        Param[] params = answer.getQuestion().getParams();
        History history = user.createHistory(answer);

        Assert.assertTrue(history.getHistoryId() > 0);
        Assert.assertEquals(answer.getChecksum(),
                history.getAnswer().getChecksum());
        Assert.assertEquals(params.length, history.getDisplayParams().size());
    }

    @Test
    public void testCreateBooleanHistory() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException {
        Answer leftAnswer = answers.get(random.nextInt(answers.size()));
        History leftHistory = user.createHistory(leftAnswer);
        Answer rightAnswer = answers.get(random.nextInt(answers.size()));
        History rightHistory = user.createHistory(rightAnswer);

        String expression = leftHistory.getHistoryId() + " OR "
                + rightHistory.getHistoryId();
        History history = user.combineHistory(expression, false);
        Answer answer = history.getAnswer();
        Param[] params = answer.getQuestion().getParams();
        
        Assert.assertEquals(recordClass.getFullName(),
                answer.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, history.getBooleanExpression());
        Assert.assertEquals(params.length, history.getDisplayParams().size());
        
        // load the boolean history
        history = user.getHistory(history.getHistoryId());
        answer = history.getAnswer();
        
        Assert.assertEquals(recordClass.getFullName(),
                answer.getQuestion().getRecordClass().getFullName());
        Assert.assertEquals(expression, history.getBooleanExpression());
        Assert.assertEquals(params.length, history.getDisplayParams().size());
    }
}
