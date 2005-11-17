package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.DerivedColumnI;
import org.gusdb.wdk.model.QueryInstance;

import java.util.HashMap;

public class WSResultList extends ResultList {

    String[][] resultArray;
    int currentRow = 0;
    HashMap <String, Integer> columnName2Index;
    boolean closed = false;

    public WSResultList(QueryInstance instance, String[][] resultArray) {
        super(instance, null);
        this.resultArray = resultArray;
	makeColumnsHash();
    }


    public Object getResultTableIndexValue() throws WdkModelException{
	throw new WdkModelException("Unsupported method");
    }

    public Object getValueFromResult(String attributeName) throws WdkModelException {
	checkIfClosed();
	Integer index = columnName2Index.get(attributeName);
	if (index == null) {
	    throw new WdkModelException("Invalid attribute name " + 
					attributeName + " requested for " +
					instance.getQuery().getFullName());
	}
	return resultArray[currentRow][index.intValue()];
    }

    public boolean next() throws WdkModelException {
	checkIfClosed();
	if (currentRow == resultArray.length) {
	    return false;
	} else {
	    currentRow++;
	    return true;
	}
    }

    public void print() throws WdkModelException {
	checkIfClosed();
	for (String[] row : resultArray) {
	    for (String col : row) System.out.print(col + "\t");
	}	    
	System.out.println("");
	close();
    }

    public void close() throws WdkModelException {
	closed = true;
    }

    public void checkQueryColumns(Query query, boolean checkAll, boolean has_result_table_i) throws WdkModelException {

	Column[] columns = query.getColumns();
	int i=0;
	for (String[] row : resultArray) {
	    if (columns.length != row.length) {
		String msg = 
		    "Web service result for " +
		    query.getFullName() + 
		    " has wrong number of columns in row " +
		    i + ".  Expected " + columns.length + " found " +
		    row.length + ".";
		throw new WdkModelException(msg);
	    }
	    i++;
	}
    }

    ////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////
    private void makeColumnsHash() {
	columnName2Index = new HashMap();
	Column[] columns = query.getColumns();
	int i=0;
	for (Column column : columns) {
	    columnName2Index.put(column.getName(), new Integer(i++));
	}
    }

    private void checkIfClosed() throws WdkModelException {
	if (closed) throw new WdkModelException("Web Service result list is already closed");
    }

}

