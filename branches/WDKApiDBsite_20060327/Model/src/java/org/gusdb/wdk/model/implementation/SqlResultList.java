package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.Column;
//import org.gusdb.wdk.model.DerivedColumnI;
import org.gusdb.wdk.model.QueryInstance;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Clob;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlResultList extends ResultList {

    ResultSet resultSet;

    public SqlResultList(QueryInstance instance,
			 String resultTableName, ResultSet resultSet) {
        super(instance, resultTableName);
        this.resultSet = resultSet;
    }


    public Object getResultTableIndexValue() throws WdkModelException{
	Object o;
	try {
	     o = resultSet.getObject(ResultFactory.RESULT_TABLE_I);
	}
	catch (SQLException e){
	    throw new WdkModelException("Result table: " + resultTableName 
					+ " SQLState: " + e.getSQLState(), e);
	}
	return o;
    }

    public Object getValueFromResult(String attributeName) throws WdkModelException {
        Object o = null;
        try {
	    ResultSetMetaData rsmd = resultSet.getMetaData();
	    int columnIndex = resultSet.findColumn(attributeName);
	    int columnType = rsmd.getColumnType(columnIndex);
	    if (columnType == Types.CLOB){
	        Clob clob = resultSet.getClob(attributeName);
	        if (clob != null) {
	            Long length = new Long(clob.length());
	            o = clob.getSubString(1, length.intValue());
	        }
	    }
	    else{
		o = resultSet.getObject(attributeName);
	    }
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }

	if (o == null) {
	    o = "";
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

    public void checkQueryColumns(Query query, boolean checkAll, boolean has_result_table_i) throws WdkModelException {

	try {
	    // get the names of all columns in the sql, and set flag
	    // if one is RESULT_TABLE_I
	    boolean sqlHasIcolumn = false;
	    Map<String, String> rsCols = new LinkedHashMap<String, String>();
	    ResultSetMetaData metaData = resultSet.getMetaData();
	    int rsColCount = metaData.getColumnCount();
	    for (int i=1; i<=rsColCount; i++) {
		String columnName = metaData.getColumnName(i).toLowerCase();
		if (columnName.equals(ResultFactory.RESULT_TABLE_I) 
		    && has_result_table_i) {
		    sqlHasIcolumn = true;
		}
		rsCols.put(columnName.toLowerCase(), "");
	    }
	
	    // iterate through Columns declared by Query object
	    // make sure that each is found in the sql and than none are 
	    // duplicated
	    Map<String, String> alreadySeen = new LinkedHashMap<String, String>();
	    Column[] columns = getColumns();
	    String queryName = query.getFullName();
	    int colCount = 0;
	    for (int i=0; i<columns.length; i++) {
//		if (columns[i] instanceof DerivedColumnI) continue;
		colCount++;
		String columnName = columns[i].getName();
		if (alreadySeen.containsKey(columnName)) {
		    String msg = 
			"Query '" + queryName +
			"' declares duplicate columns named '" + 
			columnName + "'";
		    throw new WdkModelException(msg);
		}
		alreadySeen.put(columnName, "");
		if (!rsCols.containsKey(columnName.toLowerCase())) {
		    String msg = "Query '" + queryName + 
			"' declares column '" + columnName + 
			"' but it is not in the Sql";

		    throw new WdkModelException(msg);
		}
	    }

	    // optional more rigorous test: make sure the Query includes
	    // all columns found in sql
	    if (checkAll) {
		if ((rsColCount != colCount && !sqlHasIcolumn) 
		    || (rsColCount != colCount + 1 && sqlHasIcolumn)) {
		    String msg = "Query '" + queryName + 
			"' declares a different number of columns(" + 
			colCount + ") than are mentioned in the Sql (" + 
			rsColCount + ")";
		    throw new WdkModelException(msg);
		}
	    } else {
		if (rsColCount < colCount) {
		    String msg = "Query '" + queryName + 
			"' declares more columns than are found in the Sql";
		    throw new WdkModelException(msg);
		}
	    }
	
	} catch (SQLException e) {
	    throw new WdkModelException(e);
	}
    }
  

}

