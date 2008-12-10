/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class QueryTest {

    private static final Logger logger = Logger.getLogger(QueryTest.class);

    @Test
    public void testVocabQueries() throws Exception {
        WdkModel wdkModel = UnitTestHelper.getModel();
        Set<String> testedQueries = new HashSet<String>();
        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                if (param instanceof FlatVocabParam) {
                    Query query = ((FlatVocabParam) param).getQuery();
                    String queryName = query.getFullName();
                    if (!testedQueries.contains(queryName)) {
                        testQuery(query);
                        testedQueries.add(queryName);
                    }
                }
            }
        }
    }

    @Test
    public void testIdQueries() throws Exception {
        WdkModel wdkModel = UnitTestHelper.getModel();
        Set<String> testedQueries = new HashSet<String>();
        for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
            for (Question question : questionSet.getQuestions()) {
                Query query = question.getQuery();

                // skip any combined queries and process queries
                if (query.isCombined() || (query instanceof ProcessQuery))
                    continue;

                String queryName = query.getFullName();
                if (!testedQueries.contains(queryName)) {
                    testQuery(query);
                    testedQueries.add(queryName);
                }
            }
        }
    }

    private void testQuery(Query query) throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        // try all sample values
        int setCount = 1;
        for (ParamValuesSet valueSet : query.getParamValuesSets()) {
            logger.debug("Testing query [" + query.getFullName()
                    + "] value set #" + setCount + "...");

            int minRows = valueSet.getMinRows();
            int maxRows = valueSet.getMaxRows();
            Map<String, String> values = valueSet.getParamValues();

            // try to make a query instance
            QueryInstance instance = query.makeInstance(values);
            int rows = instance.getResultSize();

            String qName = query.getFullName();
            Assert.assertTrue(qName + " rows less than min (" + minRows + ")",
                    minRows <= rows);
            Assert.assertTrue(qName + " rows more than max (" + maxRows + ")",
                    maxRows >= rows);

            setCount++;
        }
    }
}
