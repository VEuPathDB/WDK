package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.DerivedColumnI;
import org.gusdb.gus.wdk.model.QueryInstance;

import java.util.HashMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class SqlResultList extends ResultList {

    ResultSet resultSet;

    public SqlResultList(QueryInstance instance,
			 String resultTableName, ResultSet resultSet) {
        super(instance, resultTableName);
        this.resultSet = resultSet;
    }

    public Object getValueFromResult(String fieldName) throws Exception {
        return resultSet.getObject(fieldName);
    }

    public boolean next() throws Exception {
        return resultSet.next();
    }

    public void write(StringBuffer buf) throws Exception {
        SqlUtils.writeResultSet(resultSet, buf);
    }

    public void print() throws Exception {
        SqlUtils.printResultSet(resultSet);
    }

    public void close() throws Exception {
        SqlUtils.closeResultSet(resultSet);
    }

    public void checkQueryColumns(Query query, boolean checkAll) throws Exception {

	boolean sqlHasIcolumn = false;
	HashMap rsCols = new HashMap();
	ResultSetMetaData metaData = resultSet.getMetaData();
	int rsColCount = metaData.getColumnCount();
	for (int i=1; i<=rsColCount; i++) {
	    String columnName = metaData.getColumnName(i).toLowerCase();

	    //check if sql is being retrieved from a result table that has an extra column named 'i' for 
	    //enumerating results in the table (this extra column will be ignored when doing column validation)
	    if (columnName.equals("i") && query.getIsCacheable().booleanValue() == true){
		sqlHasIcolumn = true;
	    }
	    rsCols.put(columnName, "");
	}
	
	HashMap alreadySeen = new HashMap();
	Column[] columns = query.getColumns();
	String queryName = query.getName();
	int colCount = 0;
	for (int i=0; i<columns.length; i++) {
	    if (columns[i] instanceof DerivedColumnI) continue;
	    colCount++;
	    String columnName = columns[i].getName();
	    if (alreadySeen.containsKey(columnName)) 
		throw new Exception("Query '" + queryName + "' declares duplicate columns named '" + columnName + "'");
	    alreadySeen.put(columnName, "");
	    if (!rsCols.containsKey(columnName)) 
		throw new Exception("Query '" + queryName + "' declares column '" + columnName + "' but it is not in the Sql");

	}

	if (checkAll) {
	    if ((rsColCount != colCount && sqlHasIcolumn == false) || (rsColCount != colCount + 1 && sqlHasIcolumn == true)) 
		throw new Exception("Query '" + queryName + "' declares a different number of columns(" + colCount + ") than are mentioned in the Sql (" + rsColCount + ")");
	} else {
	    if (rsColCount < colCount) 
		throw new Exception("Query '" + queryName + "' declares too many columns (more than are mentioned in the Sql");
	}
	
    }
    
}

