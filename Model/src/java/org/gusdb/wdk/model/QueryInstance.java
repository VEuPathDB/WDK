package org.gusdb.gus.wdk.model;

import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.NotBooleanOperandException;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

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
    protected TreeMap values = new TreeMap();

    /**
     * Values that will be used when the QueryInstance is being run in multi-mode to join it with
     * a table containing a list of primary keys in a RecordListInstance.
     */
    protected String multiModeResultTableName;

    protected String pkToJoinWith;
    
    protected int startId;
    
    protected int endId;

    protected boolean inMultiMode;

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public void setValues(Map values) throws QueryParamsException {
	this.values = new TreeMap(values);
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
	

    public abstract String getBooleanOperandSql() throws NotBooleanOperandException;

    public abstract ResultList getResult() throws Exception;


    // ------------------------------------------------------------------
    // Constructor (Protected)
    // ------------------------------------------------------------------

    protected QueryInstance (Query query) {
	this.query = query;
	this.isCacheable = query.getIsCacheable().booleanValue();
	this.inMultiMode = false;
    }

}
