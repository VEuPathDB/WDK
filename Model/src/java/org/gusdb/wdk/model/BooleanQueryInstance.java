package org.gusdb.wdk.model;

import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.RDBMSPlatformI;


import java.util.Collection;
import java.util.Map;
import java.util.HashMap;


import java.util.Hashtable;
import java.util.Enumeration;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BooleanQueryInstance.java
 *
 * Instance instantiated by a BooleanQuery.  Takes Answers as values for 
 * its parameters along with a boolean operation, and returns a result. 
 *
 * Created: Wed May 19 15:11:30 2004
 *  
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class BooleanQueryInstance extends QueryInstance {

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------
    
    /**
     * QueryInstance from the first Answer that is a parameter for BooleanQueryInstance.
     * May itself be a BooleanQueryInstance.
     */
    protected QueryInstance firstQueryInstance;

    /**
     * QueryInstance from the second Answer that is a parameter for BooleanQueryInstance.
     * May itself be a BooleanQueryInstance.
     */
    protected QueryInstance secondQueryInstance;

    /**
     * Operation that is to be performed to create a composition of the two QueryInstances.
     */
    String operation;

    /**
     * QueryInstance IDs for the operand QueryInstances.  The keys of the map are the static
     * parameter names found in BooleanQuery and the values are the IDs of the respective
     * QueryInstances.  The ResultFactory uses these in its logic to return a result for the
     * BooleanQueryInstance.
     */
    protected HashMap operandIds = new HashMap();

    /**
     * Query that created this BooleanQueryInstance; BooleanQueryInstances and BooleanQueries
     * have a 1:1 relationship.
     */
    BooleanQuery booleanQuery;



    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public BooleanQueryInstance (BooleanQuery query) {

	super(query);
	this.booleanQuery = query;
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * @param values Map where the keys are the names for BooleanQuery parameters
     * found as static variables in BooleanQuery, and the values are the expected
     * values for those parameters (Answers for the two AnswerParam and the name
     * of the boolean operation for the StringParam).  Columns are set at this time
     * for the BooleanQuery this instance points to.
     */
    public void setValues(Map values) throws WdkUserException, WdkModelException{

	super.setValues(values);
	
	Answer firstAnswer = (Answer)values.get(BooleanQuery.FIRST_ANSWER_PARAM_NAME);
	this.firstQueryInstance = firstAnswer.getQueryInstance();

	Answer secondAnswer = (Answer)values.get(BooleanQuery.SECOND_ANSWER_PARAM_NAME);
	this.secondQueryInstance = secondAnswer.getQueryInstance();

	validateBooleanValues(firstAnswer, secondAnswer);
	
	this.operation = (String)values.get(BooleanQuery.OPERATION_PARAM_NAME);
	
	setIsCacheable(firstQueryInstance.getIsCacheable() && secondQueryInstance.getIsCacheable());
	
	Column columns[] = firstQueryInstance.getQuery().getColumns();
	for (int i = 0; i < columns.length; i++){
	    booleanQuery.addColumn(columns[i]);
	}
    }


    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // QueryInstance
    // ------------------------------------------------------------------

    public ResultList getResult() throws WdkModelException{
	
	initOperandIds();
	ResultFactory resultFactory = booleanQuery.getResultFactory();
	ResultList resultList = resultFactory.getResult(this);
	return resultList;
    }

    public String getSqlForCache() throws WdkModelException{
	String cacheSql = booleanQuery.getResultFactory().getSqlForCache(this);

	return cacheSql;
    }

    public Collection getCacheValues() throws WdkModelException{
	initOperandIds();
	return operandIds.values();
    }

    protected  ResultList getNonpersistentResult() throws WdkModelException{
	return null;
    }
    
    protected void writeResultToTable(String resultTableName, 
				      ResultFactory rf) throws WdkModelException {
        RDBMSPlatformI platform = rf.getRDBMSPlatform();
	
        try {
            platform.createTableFromQuerySql(platform.getDataSource(),
					     resultTableName, 
					     getSql());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    public String getResultAsTable() throws WdkModelException{
	return booleanQuery.getResultFactory().getResultAsTable(this);

    }
    
    // ------------------------------------------------------------------
    // Protected Methods
    // ------------------------------------------------------------------

    /**
     * Gets sql to return a result for this BooleanQueryInstance.  The sql returned
     * will be the individual sql to get the results for each of the operands joined
     * by the boolean operation.
     */
    protected String getSql() throws WdkModelException{
	
	String sql = firstQueryInstance.getSqlForCache() + " " + operation + " " + secondQueryInstance.getSqlForCache();
	return sql;
    }

    // ------------------------------------------------------------------
    // Private Methods 
    // ------------------------------------------------------------------

    /**
     * Checks to make sure that the Queries in the Questions for the given Answers
     * have the same Columns and that the Questions' RecordClasses are the same.
     */
    private void validateBooleanValues(Answer firstAnswer, Answer secondAnswer) throws WdkModelException{

	

	Column firstColumns[] = firstAnswer.getQuestion().getQuery().getColumns();
	Column secondColumns[] = secondAnswer.getQuestion().getQuery().getColumns();

	int firstColumnCount = firstColumns.length;
	int secondColumnCount = secondColumns.length;

	if (firstColumnCount != secondColumnCount){
	    StringBuffer e = new StringBuffer("Must have the same number of columns when making a BooleanQuery\n");
	    e.append("ID Queries in Questions " + firstAnswer.getQuestion().getName() + " and " + secondAnswer.getQuestion().getName() + " do not");
	    throw new WdkModelException(e.toString());
	}

	for (int i = 0; i < firstColumnCount; i++){
	    Column nextColumn = firstColumns[i];
	    String nextColumnName = nextColumn.getName();
	    try {
		secondAnswer.getQuestion().getQuery().getColumn(nextColumnName);
	    }
	    catch (WdkModelException ec){
		StringBuffer sb = new StringBuffer ("Columns in Boolean Query Operands do not match\n");
		sb.append("Column " + nextColumnName + " was in Query for Question " + firstAnswer.getQuestion().getName());
		sb.append(" but not in " + secondAnswer.getQuestion().getName());
		throw new WdkModelException(sb.toString());
	    }
	}
	
    
	RecordClass firstRecordClass = firstAnswer.getQuestion().getRecordClass();
	RecordClass secondRecordClass = secondAnswer.getQuestion().getRecordClass();

	if (firstRecordClass != secondRecordClass){
	    StringBuffer rc = new StringBuffer("RecordClasses in two AnswerParams in a BooleanQuery must be the same,\n");
	    rc.append("but record classes in Questions " + firstAnswer.getQuestion().getName() + " and " + secondAnswer.getQuestion().getName() + " are not");
	    throw new WdkModelException(rc.toString());
	}
    }

    private void setOperandIds(Map values){
	this.operandIds = new HashMap(values);
    }

    /**
     * Gets IDs for each operand QueryInstance.  If it hasn't been done already, this involves
     * getting a result for each operand and inserting it into the cache, 
     */

    private void initOperandIds() throws WdkModelException{

	if (operandIds.isEmpty()){
	    Integer firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
	    if (firstQueryInstanceId == null){

		firstQueryInstance.getResult();//assumes values have been set
		firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
	    }

	    Integer secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
	    if (secondQueryInstanceId == null){
		secondQueryInstance.getResult();//assumes values have been set
		secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
	    }
	    Hashtable h = new Hashtable();

	    h.put(BooleanQuery.FIRST_ANSWER_PARAM_NAME, firstQueryInstanceId.toString());
	    h.put(BooleanQuery.SECOND_ANSWER_PARAM_NAME, secondQueryInstanceId.toString());
	    
	    setOperandIds(h); 
	}
    }

}
