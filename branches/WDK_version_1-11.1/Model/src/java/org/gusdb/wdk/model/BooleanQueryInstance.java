package org.gusdb.wdk.model;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

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
    
    private static Logger logger =Logger.getLogger(BooleanQueryInstance.class);

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    /**
     * QueryInstance from the first Answer that is a parameter for
     * BooleanQueryInstance. May itself be a BooleanQueryInstance.
     */
    protected QueryInstance firstQueryInstance;

    /**
     * QueryInstance from the second Answer that is a parameter for
     * BooleanQueryInstance. May itself be a BooleanQueryInstance.
     */
    protected QueryInstance secondQueryInstance;

    /**
     * Operation that is to be performed to create a composition of the two
     * QueryInstances.
     */
    String operation;

    /**
     * QueryInstance IDs for the operand QueryInstances. The keys of the map are
     * the static parameter names found in BooleanQuery and the values are the
     * IDs of the respective QueryInstances. The ResultFactory uses these in its
     * logic to return a result for the BooleanQueryInstance.
     */
    protected Map operandIds = new LinkedHashMap();

    /**
     * Query that created this BooleanQueryInstance; BooleanQueryInstances and
     * BooleanQueries have a 1:1 relationship.
     */
    BooleanQuery booleanQuery;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public BooleanQueryInstance(BooleanQuery query) {

        super(query);
        this.booleanQuery = query;
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * @param values Map where the keys are the names for BooleanQuery
     *        parameters found as static variables in BooleanQuery, and the
     *        values are the expected values for those parameters (Answers for
     *        the two AnswerParam and the name of the boolean operation for the
     *        StringParam). Columns are set at this time for the BooleanQuery
     *        this instance points to.
     */
    public void setValues(Map<String, Object> values) throws WdkUserException,
            WdkModelException {

        super.setValues(values);

        Answer firstAnswer = (Answer) values.get(BooleanQuery.FIRST_ANSWER_PARAM_NAME);
        this.firstQueryInstance = firstAnswer.getIdsQueryInstance();

        Answer secondAnswer = (Answer) values.get(BooleanQuery.SECOND_ANSWER_PARAM_NAME);
        this.secondQueryInstance = secondAnswer.getIdsQueryInstance();

        validateBooleanValues(firstAnswer, secondAnswer);

        this.operation = (String) values.get(BooleanQuery.OPERATION_PARAM_NAME);

        setIsCacheable(firstQueryInstance.getIsCacheable()
                && secondQueryInstance.getIsCacheable());

        Column columns[] = secondQueryInstance.getQuery().getColumns();

        for (int i = 0; i < columns.length; i++) {

            if (booleanQuery.getColumnMap().get(columns[i].getName()) == null) {
                booleanQuery.addColumn(columns[i]);
            }
        }
    }

    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // QueryInstance
    // ------------------------------------------------------------------

    public ResultList getResult() throws WdkModelException {

        initOperandIds();
        ResultFactory resultFactory = booleanQuery.getResultFactory();
        ResultList resultList = resultFactory.getResult(this);
        return resultList;
    }

    public Collection getCacheValues() throws WdkModelException {
        initOperandIds();
        return operandIds.values();
    }

    public String getLowLevelQuery() throws WdkModelException {
        // return sth. meaningful
        return getSql();
    }

    protected ResultList getNonpersistentResult() throws WdkModelException {
        // return the result
        return getResult();
    }

    public ResultList getPersistentResultPage(int startRow, int endRow)
            throws WdkModelException {

        if (!getIsCacheable())
            throw new WdkModelException(
                    "Attempting to get persistent result page, but query instance is not cacheable");

        ResultFactory resultFactory = booleanQuery.getResultFactory();
        ResultList rl = resultFactory.getPersistentResultPage(this, startRow,
                endRow);
        return rl;

    }

    protected void writeResultToTable(String resultTableName, ResultFactory rf)
            throws WdkModelException {
        RDBMSPlatformI platform = rf.getRDBMSPlatform();

        try {
            platform.createResultTable(platform.getDataSource(),
                    resultTableName, getSql());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    public String getResultAsTableName() throws WdkModelException {
        return booleanQuery.getResultFactory().getResultAsTableName(this);

    }

    // ------------------------------------------------------------------
    // Protected Methods
    // ------------------------------------------------------------------

    /**
     * Gets sql to return a result for this BooleanQueryInstance. The sql
     * returned will be the individual sql to get the results for each of the
     * operands joined by the boolean operation.
     */
    protected String getSql() throws WdkModelException {

	String[] commonColumns = findCommonColumnNames();

        String sql = getResultFactory().getSqlForBooleanOp(firstQueryInstance, 
							   commonColumns) + 
	    " " + operation + " " + 
	    getResultFactory().getSqlForBooleanOp(secondQueryInstance,
						  commonColumns);
        
        // order by project id, and then primary key, the first item in the
        // array is primary key, and the second is project id. If the second is
        // null then only sort on primary key (lower case)
        String[] names = Answer.findPrimaryKeyColumnNames(booleanQuery);
        
        // add sorting clause
        sql = "SELECT * FROM (" + sql + ") temp ORDER BY ";
        if (names[1] != null) sql += names[1] + ", ";
	sql += "LOWER(" + names[0] +")";
        
        // TEST
        logger.debug("Boolean Id Query: " + sql);

        return sql;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    /**
     * Checks to make sure that the Queries in the Questions for the given
     * Answers have the same primary key Columns and that the Questions' 
     * RecordClasses are the same.
     */
    private void validateBooleanValues(Answer firstAnswer, Answer secondAnswer)
            throws WdkModelException {

	String[] cols1 = firstAnswer.findPrimaryKeyColumnNames();
	String[] cols2 = secondAnswer.findPrimaryKeyColumnNames();

	// compare nulls and strings
	boolean recIdMisMatch = cols1[0] != cols2[0] && !cols1[0].equals(cols2[0]);
	boolean prjIdMisMatch = cols1[1] != cols2[1] && !cols1[1].equals(cols2[1]);
	if (recIdMisMatch || prjIdMisMatch) {
	    String errMsg = 
		"Primary key columns don't match in Boolean Query for " +
		firstAnswer.getQuestion().getFullName() +
		" (" + cols1[0] + ", " + cols1[1] + ") and " +
		secondAnswer.getQuestion().getFullName() + 
		" (" + cols2[0] + ", " + cols2[1] + ")";
	    throw new WdkModelException(errMsg);
	} 

        RecordClass firstRecordClass = firstAnswer.getQuestion().getRecordClass();
        RecordClass secondRecordClass = secondAnswer.getQuestion().getRecordClass();
        if (firstRecordClass != secondRecordClass) {
            StringBuffer rc = new StringBuffer(
                    "RecordClasses in two AnswerParams in a BooleanQuery must be the same,\n");
            rc.append("but record classes in Questions "
                    + firstAnswer.getQuestion().getName() + " and "
                    + secondAnswer.getQuestion().getName() + " are not");
            throw new WdkModelException(rc.toString());
        }
    }

    private String[] findCommonColumnNames() {
	Column[] cols1 = firstQueryInstance.getQuery().getColumns();
	Column[] cols2 = secondQueryInstance.getQuery().getColumns();
	Map<String, String> cols1Map = new LinkedHashMap<String, String>();
	for (Column col : cols1) cols1Map.put(col.getName(), col.getName());
	Vector<String> answer = new Vector<String>();
	for (Column col : cols2) 
	    if (cols1Map.get(col.getName()) != null) answer.add(col.getName());
	return answer.toArray(new String[1]);
    }

    private void setOperandIds(Map values) {
        this.operandIds = new LinkedHashMap(values);
    }

    /**
     * Gets IDs for each operand QueryInstance. If it hasn't been done already,
     * this involves getting a result for each operand and inserting it into the
     * cache,
     */

    private void initOperandIds() throws WdkModelException {

        if (operandIds.isEmpty()) {
            Integer firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
            if (firstQueryInstanceId == null) {

                ResultList rl1 = firstQueryInstance.getResult();
                // assumes values have been set
                rl1.close(); // rl1 is only needed to close connection
                firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
            }

            Integer secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
            if (secondQueryInstanceId == null) {
                ResultList rl2 = secondQueryInstance.getResult();
                // assumes values have been set
                rl2.close(); // rl2 is only needed to close connection
                secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
            }
            Hashtable<String, String> h = new Hashtable<String, String>();

            h.put(BooleanQuery.FIRST_ANSWER_PARAM_NAME,
                    firstQueryInstanceId.toString());
            h.put(BooleanQuery.SECOND_ANSWER_PARAM_NAME,
                    secondQueryInstanceId.toString());

            // also set the operation so the result in the cache
            // for different operations on the same queryInstance pair can be
            // distinguished
            h.put(BooleanQuery.OPERATION_PARAM_NAME, this.operation);

            setOperandIds(h);
        }
    }

}
