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

    public abstract String getBooleanOperandSql() throws NotBooleanOperandException;

    public abstract ResultList getResult() throws Exception;

    // ------------------------------------------------------------------
    // Constructor (Protected)
    // ------------------------------------------------------------------

    protected QueryInstance (Query query) {
	this.query = query;
	this.isCacheable = query.getIsCacheable().booleanValue();
    }

}
