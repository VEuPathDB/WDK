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
     * Values that will be used when the QueryInstance is being run in multi-mode to join it with
     * a table containing a list of primary keys in a Answer.
     */
    protected String multiModeResultTableName;

    protected String pkToJoinWith;
    
    /**
     * Added by Jerric - project column name to be joined with
     */
    protected String projectToJoinWith;
    
    protected int startId;
    
    protected int endId;

    protected boolean inMultiMode;


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
    
    public boolean getMultiMode() {
	return inMultiMode;
    }

    public boolean getIsPersistent() {
	//eventually this will include whether this can be put into history as well
	return getIsCacheable();
    }
    
    public void setIsCacheable(boolean isCacheable) {
	this.isCacheable =query.getIsCacheable().booleanValue() && isCacheable;
    }

    public Query getQuery() {
	return this.query;
    }

    public abstract ResultList getResult() throws WdkModelException;

    public abstract String getResultAsTable() throws WdkModelException;

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
     * Modified by Jerric - add project column to join with
     * @param resultTableName
     * @param projectToJoinWith
     * @param pkToJoinWith
     * @param startId
     * @param endId
     */
    void setMultiModeValues(String resultTableName,String projectToJoinWith, String pkToJoinWith, int startId, int endId){

    this.multiModeResultTableName = resultTableName;
    this.projectToJoinWith = projectToJoinWith;
    this.pkToJoinWith = pkToJoinWith;
    this.startId = startId;
    this.endId = endId;
    this.inMultiMode = true;
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
	this.inMultiMode = false;
    }

    protected ResultFactory getResultFactory() {
	return query.getResultFactory();
    }

    protected abstract ResultList getNonpersistentResult() throws WdkModelException;

    protected abstract void writeResultToTable(String resultTableName, 
					       ResultFactory rf) throws WdkModelException;
}
