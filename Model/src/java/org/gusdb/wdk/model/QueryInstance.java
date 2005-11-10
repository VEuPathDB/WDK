package org.gusdb.wdk.model;
 
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple implementation of QueryInstanceI; generally expects its subclasses
 * to do most of the real implementation.
 */
public abstract class QueryInstance {

    protected boolean isCacheable;

    /**
     * Query of which this QueryInstance is an invocation.
     */
    protected Query query;

    /**
     * ID of this QueryInstance as defined in the QueryInstance table.
     */
    protected Integer queryInstanceId;
    
    /**
     * Values that define this QueryInstance relative to others pointing to <code>query</code>
     */
    protected HashMap values = new HashMap();

    /**
     * Name of the table to join with when in join mode
     */
    protected String joinTableName;

    /**
     * In the join table.
     */
    protected String primaryKeyColumnName;
    
    /**
     * In the join table
     */
    protected String projectColumnName;
    
    protected int startIndex;
    
    protected int endIndex;

    protected boolean joinMode;

    /**
     * the attributes query has an additional parameter, which is the
     * name of the result table
     */
    protected boolean isDynamic;


    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public void setValues(Map values) throws WdkUserException, WdkModelException {
	this.values = new HashMap(values);
	query.applyDefaults(values);
	query.validateParamValues(values);
    }

    public boolean getIsCacheable() {
	return isCacheable;
    }
    
    public boolean getJoinMode() {
	return joinMode;
    }

    public boolean getIsPersistent() {

	//eventually this will include whether this can be put into history as well
	return getIsCacheable();
    }
    
    public void setIsCacheable(boolean isCacheable) throws WdkModelException {
	if (isCacheable && !query.getIsCacheable().booleanValue()) {
	    throw new WdkModelException(query.getFullName() + " is not cacheable, but a query instance is, which is illegal");
	}
	this.isCacheable = isCacheable;
    }

    public Query getQuery() {
	return this.query;
    }

    public abstract ResultList getResult() throws WdkModelException;

    public abstract ResultList getPersistentResultPage(int startRow, int endRow) throws WdkModelException;

    public abstract String getResultAsTableName() throws WdkModelException;

    // ------------------------------------------------------------------
    // Package methods
    // ------------------------------------------------------------------

    public Collection getValues() {
	return values.values();
    }
    
    Map getValuesMap() {
	return values;
    }

    Integer getQueryInstanceId() {
	return queryInstanceId;
    }
    
    void setQueryInstanceId(Integer queryInstanceId) {
	this.queryInstanceId = queryInstanceId;
    }

    /**
     * @param resultTableName
     * @param projectColumnName
     * @param primaryKeyColumnName
     * @param startIndex
     * @param endIndex
     */
    void initJoinMode(String resultTableName,String projectColumnName, String primaryKeyColumnName, int startIndex, int endIndex, boolean isDynamic){

	this.joinTableName = resultTableName;
	this.projectColumnName = projectColumnName;
	this.primaryKeyColumnName = primaryKeyColumnName;
	this.startIndex = startIndex;
	this.endIndex = endIndex;
	this.joinMode = true;
	this.isDynamic = isDynamic;
    }

    public abstract Collection getCacheValues() throws WdkModelException;
    

    public abstract String getLowLevelQuery() throws WdkModelException;

    // ------------------------------------------------------------------
    // Protected methods
    // ------------------------------------------------------------------

    protected abstract String getSqlForCache() throws WdkModelException;

    protected QueryInstance (Query query) {
	this.query = query;
	this.isCacheable = query.getIsCacheable().booleanValue();
	this.joinMode = false;
    }

    protected ResultFactory getResultFactory() {
	return query.getResultFactory();
    }

    protected abstract ResultList getNonpersistentResult() throws WdkModelException;

    protected abstract void writeResultToTable(String resultTableName, 
					       ResultFactory rf) throws WdkModelException;
}
