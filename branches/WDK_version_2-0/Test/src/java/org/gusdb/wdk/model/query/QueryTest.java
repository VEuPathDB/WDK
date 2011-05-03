/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class QueryTest {

    private static final Logger logger = Logger.getLogger(QueryTest.class);

    private User user;

    public QueryTest() throws Exception {
        user = UnitTestHelper.getRegisteredUser();
    }

    @Test
    public void testVocabQueries() throws Exception {
        WdkModel wdkModel = UnitTestHelper.getModel();
        Set<String> testedQueries = new HashSet<String>();
        // test queries from base flatVocabParams
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
        // test queries from customized flatVocabParams
        for (QuerySet querySet : wdkModel.getAllQuerySets()) {
            for (Query query : querySet.getQueries()) {
                for (Param param : query.getParams()) {
                    if (!(param instanceof FlatVocabParam))
                        continue;
                    Query flatQuery = ((FlatVocabParam) param).getQuery();
                    String queryName = flatQuery.getFullName();
                    if (!testedQueries.contains(queryName)) {
                        testQuery(flatQuery);
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
                if (query.isCombined() || query.getDoNotTest()
                        || (query instanceof ProcessQuery))
                    continue;

                String queryName = query.getFullName();
                if (!testedQueries.contains(queryName)) {
                    testQuery(query);
                    testedQueries.add(queryName);
                }
            }
        }
    }

    private void testQuery(Query query) throws Exception {
        // try all sample values
        int setCount = 1;
        for (ParamValuesSet valueSet : query.getParamValuesSets()) {
            logger.debug("Testing query [" + query.getFullName()
                    + "] value set #" + setCount + "...");

            int minRows = valueSet.getMinRows();
            int maxRows = valueSet.getMaxRows();
            Map<String, String> rawValues = valueSet.getParamValues();
            Map<String, String> dependentValues = query
                    .rawOrDependentValuesToDependentValues(user, rawValues);

            // try to make a query instance
            QueryInstance instance = query.makeInstance(user, dependentValues,
                    true, 0, new LinkedHashMap<String, String>());
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
