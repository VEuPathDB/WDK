package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * BooleanQueryInstance.java
 * 
 * Instance instantiated by a BooleanQuery. Takes Answers as values for its
 * parameters along with a boolean operation, and returns a result.
 * 
 * Created: Wed May 19 15:11:30 2004
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-09 22:24:36 -0400 (Tue, 09 Aug
 *          2005) $ $Author$
 */

public class BooleanQueryInstance extends QueryInstance {

    private static Logger logger = Logger.getLogger(BooleanQueryInstance.class);

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    private String leftQueryName;
    private int leftQueryInstanceId;
    private String rightQueryName;
    private int rightQueryInstanceId;
    private BooleanOperator operator;

    /**
     * Query that created this BooleanQueryInstance; BooleanQueryInstances and
     * BooleanQueries have a 1:1 relationship.
     */
    BooleanQuery booleanQuery;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * The instance must make a clone of the query, since the query will be
     * modified when the param values are set, and the columns will be
     * determined then.
     * 
     * @param query
     * @throws WdkModelException 
     */
    public BooleanQueryInstance(BooleanQuery query, Map<String, Object> values) throws WdkModelException {
        super(query.clone(), values);
        this.booleanQuery = (BooleanQuery) this.query;

        parseValues(values);
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * @param values
     *                Map where the keys are the names for BooleanQuery
     *                parameters found as static variables in BooleanQuery, and
     *                the values are the expected values for those parameters
     *                (Answers for the two AnswerParam and the name of the
     *                boolean operation for the StringParam). Columns are set at
     *                this time for the BooleanQuery this instance points to.
     */
    private void parseValues(Map<String, Object> values)
            throws WdkModelException {
        // get the operands and operator
        Param leftParam = booleanQuery.getLeftOperandParam();
        String leftOperandId = (String) values.get(leftParam.getName());

        Param rightParam = booleanQuery.getRightOperandParam();
        String rightOperandId = (String) values.get(rightParam.getName());

        Param operatorParam = booleanQuery.getOperatorParam();
        String operator = (String) values.get(operatorParam.getName());

        //validateValues(leftOperandId, rightOperandId, operator);

        //defineColumns();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.query.QueryInstance#appendSJONContent(org.json.JSONObject)
     */
    @Override
    protected void appendSJONContent(JSONObject jsInstance)
            throws JSONException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.query.QueryInstance#createCache(java.sql.Connection, java.lang.String, int)
     */
    @Override
    public void createCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.query.QueryInstance#getSql()
     */
    @Override
    public String getSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.query.QueryInstance#getUncachedResults(org.gusdb.wdk.model.Column[], java.lang.Integer, java.lang.Integer)
     */
    @Override
    protected ResultList getUncachedResults(Column[] columns,
            Integer startIndex, Integer endIndex) throws WdkModelException,
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.query.QueryInstance#insertToCache(java.sql.Connection, java.lang.String, int)
     */
    @Override
    public void insertToCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        // TODO Auto-generated method stub
        
    }

//    private void validateValues(String leftOperandId, String rightOperandId,
//            String operator) {
//        ResultFactory factory = booleanQuery.getWdkModel().getResultFactory();
//
//        // get the answer info of the left operand
//        String[] leftInfo = factory.getAnswerInfo(leftOperandId);
//        if (leftInfo == null)
//            throw new WdkModelException("The left answer (id=" + leftOperandId
//                    + " does not exist!");
//        leftQueryName = leftInfo[0];
//        leftQueryInstanceId = Integer.parseInt(leftInfo[1]);
//
//        // get the answer info of the right operand
//        String[] rightInfo = factory.getAnswerInfo(rightOperandId);
//        if (rightInfo == null)
//            throw new WdkModelException("The right answer (id="
//                    + rightOperandId + " does not exist!");
//        rightQueryName = rightInfo[0];
//        rightQueryInstanceId = Integer.parseInt(rightInfo[1]);
//
//        // parse the operator
//        this.operator = BooleanOperator.parse(operator);
//    }
//
//    private void defineColumns() throws WdkModelException {
//        WdkModel wdkModel = query.getWdkModel();
//        Query leftQuery = (Query) wdkModel.resolveReference(leftQueryName);
//        Map<String, Column> leftColumns = leftQuery.getColumnMap();
//
//        Query rightQuery = (Query) wdkModel.resolveReference(rightQueryName);
//        Map<String, Column> rightColumns = rightQuery.getColumnMap();
//
//        // depending on the operator, define the columns
//        List<Column> columns = new ArrayList<Column>();
//        if (operator == BooleanOperator.Union
//                || operator == BooleanOperator.Intersect) {
//            // expand the columns
//        }
//    }
//
//    // ------------------------------------------------------------------
//    // Package Methods
//    // ------------------------------------------------------------------
//
//    // ------------------------------------------------------------------
//    // QueryInstance
//    // ------------------------------------------------------------------
//
//    public ResultList getResult() throws WdkModelException {
//
//        initOperandIds();
//        ResultFactory resultFactory = booleanQuery.getResultFactory();
//        ResultList resultList = resultFactory.getResult(this);
//        return resultList;
//    }
//
//    public Collection getCacheValues() throws WdkModelException {
//        initOperandIds();
//        return operandIds.values();
//    }
//
//    public String getLowLevelQuery() throws WdkModelException {
//        // return sth. meaningful
//        return getSql();
//    }
//
//    protected ResultList getNonpersistentResult() throws WdkModelException {
//        // return the result
//        return getResult();
//    }
//
//    public ResultList getPersistentResultPage(int startRow, int endRow)
//            throws WdkModelException {
//
//        if (!getIsCacheable())
//            throw new WdkModelException(
//                    "Attempting to get persistent result page, but query instance is not cacheable");
//
//        ResultFactory resultFactory = booleanQuery.getResultFactory();
//        ResultList rl = resultFactory.getPersistentResultPage(this, startRow,
//                endRow);
//        return rl;
//
//    }
//
//    protected void writeResultToTable(String resultTableName, ResultFactory rf)
//            throws WdkModelException {
//        RDBMSPlatformI platform = rf.getRDBMSPlatform();
//
//        try {
//            platform.createResultTable(platform.getDataSource(),
//                    resultTableName, getSql());
//        } catch (SQLException e) {
//            throw new WdkModelException(e);
//        }
//    }
//
//    public String getResultAsTableName() throws WdkModelException {
//        return booleanQuery.getResultFactory().getResultAsTableName(this);
//    }
//
//    // ------------------------------------------------------------------
//    // Protected Methods
//    // ------------------------------------------------------------------
//
//    /**
//     * Gets sql to return a result for this BooleanQueryInstance. The sql
//     * returned will be the individual sql to get the results for each of the
//     * operands joined by the boolean operation.
//     */
//    public String getSql() throws WdkModelException {
//        StringBuffer buffer = new StringBuffer("SELECT * FROM (");
//
//        String[] commonColumns = findCommonColumnNames();
//        buffer.append(getSqlForBooleanOp(commonColumns, expandSubType));
//
//        // order by project id, and then primary key, the first item in the
//        // array is primary key, and the second is project id. If the second is
//        // null then only sort on primary key (lower case)
//        String[] names = Answer.findPrimaryKeyColumnNames(booleanQuery);
//        buffer.append(") temp ORDER BY ");
//        if (names[1] != null) buffer.append(names[1] + ", ");
//        buffer.append("LOWER(" + names[0] + ")");
//        String sql = buffer.toString();
//
//        // filter the result with subTypes, if presented
//        if (recordClass != null && recordClass.getSubType() != null) {
//            SubType subType = recordClass.getSubType();
//            // skip if the value equals the ignore subType value.
//            if (subTypeValue != null
//                    && !((String) subTypeValue).equals(subType.getTermToSkip())) {
//                sql = getFilterSql(sql);
//            }
//        }
//
//        // TEST
//        logger.debug("Boolean Id Query: " + sql);
//
//        return sql;
//    }
//
//    /*
//     * (non-Javadoc) override the method from parent to avoid creating a a cache
//     * table for it
//     * 
//     * @see org.gusdb.wdk.model.QueryInstance#getSqlForBooleanOp(java.lang.String[])
//     */
//    protected String getSqlForBooleanOp(String[] commonColumns) throws WdkModelException {
//        StringBuffer buffer = new StringBuffer("(");
//        buffer.append(firstQueryInstance.getSqlForBooleanOp(commonColumns,
//                expandSubType));
//        buffer.append(" ");
//        buffer.append(operation);
//        buffer.append(" ");
//        buffer.append(secondQueryInstance.getSqlForBooleanOp(commonColumns,
//                expandSubType));
//        buffer.append(")");
//        return buffer.toString();
//    }
//
//    // ------------------------------------------------------------------
//    // Private Methods
//    // ------------------------------------------------------------------
//
//    /**
//     * Checks to make sure that the Queries in the Questions for the given
//     * Answers have the same primary key Columns and that the Questions'
//     * RecordClasses are the same.
//     */
//    private void validateBooleanValues(Answer firstAnswer, Answer secondAnswer)
//            throws WdkModelException {
//
//        String[] cols1 = firstAnswer.findPrimaryKeyColumnNames();
//        String[] cols2 = secondAnswer.findPrimaryKeyColumnNames();
//
//        // compare nulls and strings
//        boolean recIdMisMatch = cols1[0] != cols2[0]
//                && !cols1[0].equals(cols2[0]);
//        boolean prjIdMisMatch = cols1[1] != cols2[1]
//                && !cols1[1].equals(cols2[1]);
//        if (recIdMisMatch || prjIdMisMatch) {
//            String errMsg = "Primary key columns don't match in Boolean Query"
//                    + " for " + firstAnswer.getQuestion().getFullName() + " ("
//                    + cols1[0] + ", " + cols1[1] + ") and "
//                    + secondAnswer.getQuestion().getFullName() + " ("
//                    + cols2[0] + ", " + cols2[1] + ")";
//            throw new WdkModelException(errMsg);
//        }
//
//        RecordClass firstRecordClass = firstAnswer.getQuestion().getRecordClass();
//        RecordClass secondRecordClass = secondAnswer.getQuestion().getRecordClass();
//        if (firstRecordClass != secondRecordClass) {
//            StringBuffer rc = new StringBuffer(
//                    "RecordClasses in two AnswerParams in a BooleanQuery must be the same,\n");
//            rc.append("but record classes in Questions "
//                    + firstAnswer.getQuestion().getName() + " and "
//                    + secondAnswer.getQuestion().getName() + " are not");
//            throw new WdkModelException(rc.toString());
//        }
//    }
//
//    private String[] findCommonColumnNames() {
//        Column[] cols1 = firstQueryInstance.getQuery().getColumns();
//        Column[] cols2 = secondQueryInstance.getQuery().getColumns();
//        Map<String, String> cols1Map = new LinkedHashMap<String, String>();
//        for (Column col : cols1)
//            cols1Map.put(col.getName(), col.getName());
//        Vector<String> answer = new Vector<String>();
//        for (Column col : cols2)
//            if (cols1Map.get(col.getName()) != null) answer.add(col.getName());
//        return answer.toArray(new String[1]);
//    }
//
//    private void setOperandIds(Map<String, String> values) {
//        this.operandIds = new LinkedHashMap<String, String>(values);
//    }
//
//    /**
//     * Gets IDs for each operand QueryInstance. If it hasn't been done already,
//     * this involves getting a result for each operand and inserting it into the
//     * cache,
//     */
//
//    private void initOperandIds() throws WdkModelException {
//
//        if (operandIds.isEmpty()) {
//            Integer firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
//            if (firstQueryInstanceId == null) {
//
//                ResultList rl1 = firstQueryInstance.getResult();
//                // assumes values have been set
//                rl1.close(); // rl1 is only needed to close connection
//                firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
//            }
//
//            Integer secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
//            if (secondQueryInstanceId == null) {
//                ResultList rl2 = secondQueryInstance.getResult();
//                // assumes values have been set
//                rl2.close(); // rl2 is only needed to close connection
//                secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
//            }
//            Hashtable<String, String> h = new Hashtable<String, String>();
//
//            h.put(BooleanQuery.FIRST_ANSWER_PARAM_NAME,
//                    firstQueryInstanceId.toString());
//            h.put(BooleanQuery.SECOND_ANSWER_PARAM_NAME,
//                    secondQueryInstanceId.toString());
//
//            // also set the operation so the result in the cache
//            // for different operations on the same queryInstance pair can be
//            // distinguished
//            h.put(BooleanQuery.OPERATION_PARAM_NAME, this.operation);
//
//            setOperandIds(h);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.gusdb.wdk.model.query.QueryInstance#appendSJONContent(org.json.JSONObject)
//     */
//    @Override
//    protected void appendSJONContent(JSONObject jsInstance)
//            throws JSONException {
//    // TODO Auto-generated method stub
//
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.gusdb.wdk.model.query.QueryInstance#createCache(java.sql.Connection,
//     *      java.lang.String, int)
//     */
//    @Override
//    public void createCache(Connection connection, String tableName,
//            int instanceId) throws WdkModelException {
//    // TODO Auto-generated method stub
//
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.gusdb.wdk.model.query.QueryInstance#getUncachedResults(org.gusdb.wdk.model.Column[],
//     *      java.lang.Integer, java.lang.Integer)
//     */
//    @Override
//    protected ResultList getUncachedResults(Column[] columns,
//            Integer startIndex, Integer endIndex) throws WdkModelException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.gusdb.wdk.model.query.QueryInstance#insertToCache(java.sql.Connection,
//     *      java.lang.String, int)
//     */
//    @Override
//    public void insertToCache(Connection connection, String tableName,
//            int instanceId) throws WdkModelException {
//    // TODO Auto-generated method stub
//
//    }
}
