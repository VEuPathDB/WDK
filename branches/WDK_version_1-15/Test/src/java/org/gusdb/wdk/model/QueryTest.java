/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.implementation.SqlQuery;
import org.gusdb.wdk.model.implementation.WSQuery;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class QueryTest extends WdkModelTestBase {

    public static final String SAMPLE_QUERY_SET = "SampleQueries";
    public static final String SAMPLE_SQL_QUERY = "SampleIdQuery";
    public static final String SAMPLE_WS_QUERY = "SampleWsQuery";

    /**
     * test reading the queries from the model
     */
    @org.junit.Test
    public void testGetQueries() {
        // validate the references to the queries
        QuerySet[] querySets = wdkModel.getAllQuerySets();
        Assert.assertTrue("There must be at least one query set",
                querySets.length > 0);
        for (QuerySet querySet : querySets) {
            Query[] queries = querySet.getQueries();
            Assert.assertTrue("There must be at least one query in each query "
                    + "set", queries.length > 0);
            for (Query query : queries) {
                // the query must have at least one column
                Assert.assertTrue("The query must define at least one column",
                        query.getColumns().length > 0);
            }
        }
    }

    /**
     * test getting queries by name
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetQuery() throws WdkModelException {
        // get query by reference
        Query query1 = (Query) wdkModel.resolveReference(SAMPLE_QUERY_SET + "."
                + SAMPLE_SQL_QUERY);
        Assert.assertNotNull(query1);

        // get query from query set
        QuerySet querySet = wdkModel.getQuerySet(SAMPLE_QUERY_SET);
        Query query2 = querySet.getQuery(SAMPLE_SQL_QUERY);
        Assert.assertSame(query1, query2);

        // there are more than one param
        Param[] params = query2.getParams();
        Assert.assertTrue(params.length > 1);
        Assert.assertFalse(params[0].isAllowEmpty());

        // there must be only two columns
        Column[] columns = query2.getColumns();
        Assert.assertTrue(columns.length == 2);
    }

    /**
     * Test getting a sql query
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetSqlQuery() throws WdkModelException {
        // get query by reference
        SqlQuery query = (SqlQuery) wdkModel.resolveReference(SAMPLE_QUERY_SET
                + "." + SAMPLE_SQL_QUERY);

        // get sql
        Assert.assertTrue(query.getSql().trim().length() > 0);
    }

    /**
     * Test getting a ws query
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetWsQuery() throws WdkModelException {
        // get query by reference
        WSQuery query = (WSQuery) wdkModel.resolveReference(SAMPLE_QUERY_SET
                + "." + SAMPLE_WS_QUERY);

        // get process name and url
        Assert.assertTrue(query.getProcessName().trim().length() > 0);
        Assert.assertTrue(query.getWebServiceUrl().trim().length() > 0);
    }

    /**
     * get a customized enumParam; by default, the useTermOnly is false for this
     * param; however, it is configured to be true in this query.
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetCustomEnumParam() throws WdkModelException {
        // get query by reference
        SqlQuery query = (SqlQuery) wdkModel.resolveReference(SAMPLE_QUERY_SET
                + "." + SAMPLE_SQL_QUERY);
        EnumParam param = (EnumParam) query.getParamMap().get("booleanParam");
        String[] terms = param.getVocab();
        String[] internals = param.getVocabInternal();
        // terms and internals are equal, since the useTermOnly is true
        Assert.assertArrayEquals(terms, internals);
    }

    /**
     * get a customized flatVocabParam; by default, the useTermOnly is true for
     * this param; however, it is configured to be false in this query
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetCustomVocabParam() throws WdkModelException {
        // get query by reference
        SqlQuery query = (SqlQuery) wdkModel.resolveReference(SAMPLE_QUERY_SET
                + "." + SAMPLE_SQL_QUERY);
        FlatVocabParam param = (FlatVocabParam) query.getParamMap().get(
                "flatVocabParam");
        String[] terms = param.getVocab();
        String[] internals = param.getVocabInternal();
        Assert.assertTrue(terms.length == 3 && terms.length == internals.length);

        // terms and internals are not equal, since the useTermOnly is false
        for (int i = 0; i < terms.length; i++) {
            Assert.assertFalse(terms[i].equals(internals[i]));
        }
    }
}
