package org.gusdb.gus.wdk.model;

import java.util.HashMap;

public abstract class ResultList {

    QueryInstance instance;
    Query query;
    String resultTableName;
    HashMap valuesInUse;

    public ResultList(QueryInstance instance, String resultTableName) {
	this.instance = instance;
	this.query = instance.getQuery();
	this.resultTableName = resultTableName;
	this.valuesInUse = new HashMap();
    }


    public abstract void checkQueryColumns(Query query, boolean checkAll) throws Exception;

    public Object getValue(String fieldName) throws Exception {
	if (valuesInUse.containsKey(fieldName)) 
	    throw new Exception("Circular attempt to access field " + fieldName);
	Object value;
	try {
	    valuesInUse.put(fieldName,fieldName);
	    Column column = query.getColumn(fieldName);
	    // the next line has the potential to be circular
	    if (column instanceof DerivedColumnI) 
		value = ((DerivedColumnI)column).getDerivedValue(this);
	    else value = getValueFromResult(fieldName);
	} finally {
	    valuesInUse.remove(fieldName);
	}
	return value;
    }
    
    public Query getQuery() {
	return query;
    }

    public abstract boolean next() throws Exception;

    public abstract void close() throws Exception;

    public abstract void write(StringBuffer buf) throws Exception;

    public abstract void print() throws Exception;

    public String getResultTableName() throws Exception {
	if (!hasResultTable()) throw new Exception("Has no result table");
	return resultTableName;
    }

    public boolean hasResultTable() {
	return resultTableName != null;
    }

    //////////////////////////////////////////////////////////////////
    //  protected
    //////////////////////////////////////////////////////////////////

    protected abstract Object getValueFromResult(String fieldName) throws Exception;

    public QueryInstance getInstance() {
        return instance;
    }
}

