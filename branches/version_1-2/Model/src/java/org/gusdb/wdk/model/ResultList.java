package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
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


    AttributeFieldValue getAttributeFieldValue(String attributeName) throws WdkModelException {
	if (valuesInUse.containsKey(attributeName)) 
	    throw new WdkModelException("Circular attempt to access attribute " + attributeName);
	Object value;
	try {
	    valuesInUse.put(attributeName,attributeName);
	    Column column = query.getColumn(attributeName);
	    // the next line has the potential to be circular
	    if (column instanceof DerivedColumnI) {
		value = ((DerivedColumnI)column).getDerivedValue(this);
	    }

	    else value = getValueFromResult(attributeName);
	    AttributeField field = new AttributeField(column);
	    AttributeFieldValue fieldValue = 
		new AttributeFieldValue(field, value);
	    return fieldValue;
	} finally {
	    valuesInUse.remove(attributeName);
	}
    }
    
    public Query getQuery() {
	return query;
    }

    public Column[] getColumns() {
	return query.getColumns();
    }

    public abstract boolean next() throws WdkModelException;

    public abstract void close() throws WdkModelException;

    public void write(StringBuffer buf)  throws WdkModelException {
        String newline = System.getProperty( "line.separator" );
	Iterator rows = getRows();
	while (rows.hasNext()) {
	    Map rowMap = (Map)rows.next();
	    Iterator colNames = rowMap.keySet().iterator();
	    while (colNames.hasNext()) {
		Object colName = colNames.next();
		AttributeFieldValue fVal = 
		    (AttributeFieldValue)rowMap.get(colName);
		if (fVal.getIsInternal().booleanValue()) buf.append("[");
		buf.append(fVal.getValue());
		if (fVal.getIsInternal().booleanValue()) buf.append("]");
		buf.append("\t");
	    }
	    buf.append(newline);
	}
	close();
    }

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

    /**
     * @return Iterator of Maps as returned by getRow()
     */
    Iterator getRows() {
	return new ResultListIterator(this);
    }

    /**
     * @return Map of columnName -> AttributeFieldValue
     */
    Map getRow() {
	// return new RowMap(this);
	LinkedHashMap row = new LinkedHashMap();
	Column[] cols = getColumns();
        for (int i=0; i<cols.length; i++) {
	    String colName = cols[i].getName();
	    try {
		row.put(colName, getAttributeFieldValue(colName));
	    } catch (WdkModelException e) {
		throw new RuntimeException(e);
	    }
	}
	return row;
    }

    protected abstract Object getValueFromResult(String attributeName) throws WdkModelException;

    public QueryInstance getInstance() {
        return instance;
    }

    /////////////////////////////////////////////////////////////////////
    //  Inner classes
    /////////////////////////////////////////////////////////////////////

    public class ResultListIterator implements Iterator {

	ResultList rl;
	Map nextCache = null;
	
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
		
		// if a next thing is available, cache it
		if (hasNext) {
		    nextCache = rl.getRow();
		}
	    } catch (WdkModelException e) {
		throw new RuntimeException(e);
	    }
	    
	    return hasNext;
	}
	
	public Object next() throws NoSuchElementException {
	    // if the next thing is already in the cache, return it and clear cache
	    if (nextCache != null) {
		Map theNext = nextCache;
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

