package org.gusdb.gus.wdk.model.query;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Collection;

public class QueryInstance {

    Query query;
    boolean isCacheable;
    TreeMap values = new TreeMap();

    public SortedMap getValuesMap() {
	return new TreeMap(values);
    }

    public Collection getValues() {
	return values.values();
    }

    public boolean getIsCacheable() {
	return isCacheable;
    }

    public Query getQuery() {
	return this.query;
    }

    protected QueryInstance (Query query) {
	this.isCacheable = query.getIsCacheable().booleanValue();
	this.query = query;
    }

    protected QueryInstance (Query query, boolean isCacheable) {
	this.isCacheable = 
	    query.getIsCacheable().booleanValue() && isCacheable;
	this.query = query;
    }

    protected void setValues(Map values) throws QueryParamsException {
	this.values = new TreeMap(values);
	query.validateParamValues(values);
    }
}
