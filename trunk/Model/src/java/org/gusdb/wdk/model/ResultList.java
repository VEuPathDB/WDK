package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

    public Object getValue(String attributeName) throws WdkModelException {
	if (valuesInUse.containsKey(attributeName)) 
	    throw new WdkModelException("Circular attempt to access attribute " + attributeName);
	Object value;
	try {
	    valuesInUse.put(attributeName,attributeName);
	    Column column = query.getColumn(attributeName);
	    // the next line has the potential to be circular
	    if (column instanceof DerivedColumnI) 
		value = ((DerivedColumnI)column).getDerivedValue(this);
	    else value = getValueFromResult(attributeName);
	} finally {
	    valuesInUse.remove(attributeName);
	}
	return value;
    }
    
    public Query getQuery() {
	return query;
    }

    public Column[] getColumns() {
	return query.getColumns();
    }

    public Map getRow() {
	return new RowMap(this);
    }

    public Iterator getRows() {
	return new ResultListIterator(this);
    }

    public abstract boolean next() throws WdkModelException;

    public abstract boolean hasNext() throws WdkModelException;

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

    protected abstract Object getValueFromResult(String attributeName) throws WdkModelException;

    public QueryInstance getInstance() {
        return instance;
    }

   
    public class ResultListIterator implements Iterator {

	ResultList rl;
	RowMap nextCache = null;

	ResultListIterator(ResultList rl) {
	    this.rl = rl;
	}

	public boolean hasNext() {
	    // if nextCache is not consumed, return true (allow repeated calls)
	    if (nextCache != null) { return true; }

	    // ask rl if a next thing is available
	    boolean hasNext = false;
	    try {
		hasNext = rl.next();
	    } catch (WdkModelException e) {
		throw new RuntimeException(e);
	    }

	    // if a next thing is available, cache it
	    if (hasNext) {
		nextCache = (RowMap) rl.getRow();
	    }

	    return hasNext;
	}

	public Object next() throws NoSuchElementException {
	    // if the next thing is already in the cache, return it and clear cache
	    if (nextCache != null) {
		RowMap theNext = nextCache;
		nextCache = null;
		return theNext;
	    }
	    // if nothing in cache, ask hasNext for an answer
	    else {
		if (hasNext()) {
		    return nextCache;
		} else {
		    throw new NoSuchElementException("no more element left");
		}
	    }
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
}

