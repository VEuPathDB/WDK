package org.gusdb.wdk.model;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple implementation of QueryInstanceI; generally expects its subclasses
 * to do most of the real implementation.
 */
public abstract class QueryInstance {

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------
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
     * a table containing a list of primary keys in a SummaryInstance.
     */
    protected String multiModeResultTableName;

    protected String pkToJoinWith;
    
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

    public Collection getValues() {
	return values.values();
    }
    
    public Map getValuesMap() {
	return values;
    }

    public boolean getIsCacheable() {
	return isCacheable;
    }
    
    public boolean getIsPersistent() {
	//eventually this will include whether this can be put into history as well
	return getIsCacheable();
    }
    
    public void setIsCacheable(boolean isCacheable) {
	this.isCacheable =query.getIsCacheable().booleanValue() && isCacheable;
    }

    public Integer getQueryInstanceId() {
	return queryInstanceId;
    }
    
    public void setQueryInstanceId(Integer queryInstanceId) {
	this.queryInstanceId = queryInstanceId;
    }

    public Query getQuery() {
	return this.query;
    }

    public void setMultiModeValues(String resultTableName, String pkToJoinWith, int startId, int endId){

	this.multiModeResultTableName = resultTableName;
	this.pkToJoinWith = pkToJoinWith;
	this.startId = startId;
	this.endId = endId;
	this.inMultiMode = true;
    }
	

    public abstract ResultList getResult() throws WdkModelException;

    public abstract String getResultAsTable() throws WdkModelException;

    // ------------------------------------------------------------------
    // Constructor (Protected)
    // ------------------------------------------------------------------

    protected QueryInstance (Query query) {
	this.query = query;
	this.isCacheable = query.getIsCacheable().booleanValue();
	this.inMultiMode = false;
    }

    protected abstract ResultList getNonpersistentResult() throws WdkModelException;

    protected abstract void writeResultToTable(String resultTableName, 
					       ResultFactory rf) throws WdkModelException;
}
