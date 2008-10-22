/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 *
 */
public class AnswerParamTest {
    
    private static final Logger logger = Logger.getLogger(AnswerParamTest.class);

    private WdkModel wdkModel;
    private List<Answer> answers;
    private RecordClass recordClass;
    private Random random = new Random();
    private List<Question> questions;
    
    public AnswerParamTest() throws NoSuchAlgorithmException, WdkUserException, WdkModelException, SQLException, JSONException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        wdkModel = UnitTestHelper.getModel();
        recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
        
        // get a list of questions that has a single answer param
        questions = loadQuestions();

        answers = UnitTestHelper.getAnswerPool(recordClass);
        
        Assert.assertTrue(questions.size() > 0);
    }
    
    private List<Question> loadQuestions() {
        Question[] questions = wdkModel.getQuestions(recordClass);
        List<Question> pool = new ArrayList<Question>();
        for (Question question : questions) {
            Param[] params = question.getParams();
            int answerParamCount = 0;
            for (Param param : params) {
                if (param instanceof AnswerParam) answerParamCount++;
            }
            if (answerParamCount > 1) pool.add(question);
        }
        return pool;
    }
    
    @Test
    public void testUseAnswerParam() throws NoSuchAlgorithmException, SQLException, WdkModelException, JSONException, WdkUserException {
        Answer operand = answers.get(random.nextInt(answers.size()));
        String checksum = operand.getAnswerInfo().getAnswerChecksum();
        
        Question question = questions.get(random.nextInt(questions.size()));
        Param[] params = question.getParams();
        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();
        for(Param param : params) {
            if (param instanceof AnswerParam) {
                paramValues.put(param.getName(), checksum);
            } else {
                paramValues.put(param.getName(), param.getDefault());
            }
        }
        Answer result = question.makeAnswer(paramValues);
        logger.debug("Answer param result: " + result.getResultMessage());
    }
}
