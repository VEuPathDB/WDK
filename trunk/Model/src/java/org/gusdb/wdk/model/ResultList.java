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


    public abstract void checkQueryColumns(Query query, boolean checkAll) throws WdkModelException;

    public Object getValue(String fieldName) throws WdkModelException {
	if (valuesInUse.containsKey(fieldName)) 
	    throw new WdkModelException("Circular attempt to access field " + fieldName);
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

    public abstract boolean next() throws WdkModelException;

    public abstract void close() throws WdkModelException;

    public abstract void write(StringBuffer buf) throws WdkModelException;

    public abstract void print() throws WdkModelException;

    public String getResultTableName() throws WdkModelException {
	if (!hasResultTable()) throw new WdkModelException("Has no result table");
	return resultTableName;
    }

    public boolean hasResultTable() {
	return resultTableName != null;
    }

    //////////////////////////////////////////////////////////////////
    //  protected
    //////////////////////////////////////////////////////////////////

    protected abstract Object getValueFromResult(String fieldName) throws WdkModelException;

    public QueryInstance getInstance() {
        return instance;
    }
}

