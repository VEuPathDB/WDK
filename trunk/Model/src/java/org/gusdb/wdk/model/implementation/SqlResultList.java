package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.DerivedColumnI;
import org.gusdb.gus.wdk.model.QueryInstance;

import java.util.HashMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SqlResultList extends ResultList {

    ResultSet resultSet;

    public SqlResultList(QueryInstance instance,
			 String resultTableName, ResultSet resultSet) {
        super(instance, resultTableName);
        this.resultSet = resultSet;
    }

    public Object getValueFromResult(String attributeName) throws WdkModelException {
        Object o = null;
        try {
            o = resultSet.getObject(attributeName);
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
        return o;
    }

    public boolean next() throws WdkModelException {
        boolean b = false;
        try {
            b = resultSet.next();
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
        return b;
    }

    public void write(StringBuffer buf) throws WdkModelException {
	try {
	    SqlUtils.writeResultSet(resultSet, buf);
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }

    public void print() throws WdkModelException {
	try {
	    SqlUtils.printResultSet(resultSet);
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }

    public void close() throws WdkModelException {
	try {
	    SqlUtils.closeResultSet(resultSet);
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }

    public void checkQueryColumns(Query query, boolean checkAll) throws WdkModelException {

	try {
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
	    String queryName = query.getFullName();
	    int colCount = 0;
	    for (int i=0; i<columns.length; i++) {
		if (columns[i] instanceof DerivedColumnI) continue;
		colCount++;
		String columnName = columns[i].getName();
		if (alreadySeen.containsKey(columnName)) 
		    throw new WdkModelException("Query '" + queryName + "' declares duplicate columns named '" + columnName + "'");
		alreadySeen.put(columnName, "");
		if (!rsCols.containsKey(columnName)) 
		    throw new WdkModelException("Query '" + queryName + "' declares column '" + columnName + "' but it is not in the Sql");

	    }

	    if (checkAll) {
		if ((rsColCount != colCount && sqlHasIcolumn == false) || (rsColCount != colCount + 1 && sqlHasIcolumn == true)) 
		    throw new WdkModelException("Query '" + queryName + "' declares a different number of columns(" + colCount + ") than are mentioned in the Sql (" + rsColCount + ")");
	    } else {
		if (rsColCount < colCount) 
		    throw new WdkModelException("Query '" + queryName + "' declares too many columns (more than are mentioned in the Sql");
	    }
	
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }
    
}

