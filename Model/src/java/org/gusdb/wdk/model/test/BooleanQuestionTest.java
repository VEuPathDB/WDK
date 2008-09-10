/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class BooleanQuestionTest {

    private static final int MAX_ANSWER_POOL = 5;

    private static final Logger logger = Logger.getLogger(BooleanQuestionTest.class);

    private WdkModel wdkModel;
    private RecordClass recordClass;
    private List<Answer> answers;
    private Random random;

    public BooleanQuestionTest() throws NoSuchAlgorithmException,
            WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        random = new Random();
        // load the model
        String projectId = System.getProperty(Utilities.ARGUMENT_PROJECT_ID);
        wdkModel = WdkModel.construct(projectId);
        // pick the first record class
        recordClass = wdkModel.getAllRecordClassSets()[0].getRecordClasses()[0];
    }

    @Before
    public void prepareOperands() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        answers = new ArrayList<Answer>();

        Question[] questions = wdkModel.getQuestions(recordClass);

        // create a random list that indicate the questions to be selected
        Set<Integer> indices = new LinkedHashSet<Integer>();
        int poolMax = Math.min(MAX_ANSWER_POOL, questions.length);
        while (indices.size() < poolMax) {
            int index = random.nextInt(questions.length);
            indices.add(index);
        }

        // create a store the answers
        for (int index : indices) {
            Question question = questions[index];
            if (!validateQuestion(question)) continue;
            Answer answer = makeAnswer(question);
            answers.add(answer);
        }

        logger.info("answer pool size: " + answers.size());
    }

    private boolean validateQuestion(Question question) {
        // do not use question with ProcessQuery
        if (question.getQuery() instanceof ProcessQuery) return false;

        // do not use question with answerParam or datasetParam
        for (Param param : question.getParams()) {
            if (param instanceof AnswerParam) return false;
            if (param instanceof DatasetParam) return false;
        }
        return true;
    }

    private Answer makeAnswer(Question question)
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, JSONException {
        Param[] params = question.getParams();
        // use default param values
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        for (Param param : params) {
            values.put(param.getName(), param.getDefault());
        }
        return question.makeAnswer(values);
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

        Assert.assertTrue(answer.getResultSize() >= left.getResultSize());
        Assert.assertTrue(answer.getResultSize() >= right.getResultSize());
    }
}
