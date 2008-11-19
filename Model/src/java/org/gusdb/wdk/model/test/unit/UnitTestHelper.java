/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

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
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class UnitTestHelper {

    private static final int MAX_ANSWER_POOL = 5;
    
    private static final Logger logger = Logger.getLogger(UnitTestHelper.class);

    // use a fixed random number generator in order to use cache.
    private static Random random = new Random(1);
    private static WdkModel wdkModel;
    private static Map<String, List<AnswerValue>> answerPools = new LinkedHashMap<String, List<AnswerValue>>();

    public static WdkModel getModel() throws NoSuchAlgorithmException,
            WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        if (wdkModel == null) {
            logger.info("Loading model...");
            String projectId = System.getProperty(Utilities.ARGUMENT_PROJECT_ID);
            String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
            wdkModel = WdkModel.construct(projectId, gusHome);
        }
        return wdkModel;
    }

    public static List<AnswerValue> getAnswerPool(RecordClass recordClass)
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, JSONException {
        String poolName = recordClass.getFullName();
        List<AnswerValue> answerPool = answerPools.get(poolName);
        if (answerPool == null) {
            logger.info("Preparing answer pool...");
            answerPool = new ArrayList<AnswerValue>();
            Question[] questions = wdkModel.getQuestions(recordClass);

            // create a random list that indicate the questions to be selected
            Set<Integer> indices = new LinkedHashSet<Integer>();
            int poolMax = Math.min(MAX_ANSWER_POOL, questions.length);
            while (indices.size() < poolMax) {
                int index = random.nextInt(questions.length);
                indices.add(index);
            }

            // create and store the answers
            for (int index : indices) {
                Question question = questions[index];
                if (!validateQuestion(question)) continue;
                logger.debug("Making answer....");
                AnswerValue answerValue = makeAnswer(question);
                if (answerValue.getResultSize() > 0) answerPool.add(answerValue);
            }

            answerPools.put(poolName, answerPool);
        }
        return new ArrayList<AnswerValue>(answerPool);
    }

    private static boolean validateQuestion(Question question) {
        // do not use question with ProcessQuery
        if (question.getQuery() instanceof ProcessQuery) return false;

        // do not use question with answerParam or datasetParam
        for (Param param : question.getParams()) {
            if (param instanceof AnswerParam) return false;
            if (param instanceof DatasetParam) return false;
        }
        return true;
    }

    private static AnswerValue makeAnswer(Question question)
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, JSONException {
        List<ParamValuesSet> valueSets = question.getQuery().getParamValuesSets();
        ParamValuesSet valueSet = valueSets.get(random.nextInt(valueSets.size()));
        Map<String, String> values = valueSet.getParamValues();
        return question.makeAnswerValue(values);
    }
}
