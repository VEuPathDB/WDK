package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

public class SimpleSqlQueryInstance extends QueryInstance implements SimpleQueryInstanceI {

    String resultTable = null;

    public SimpleSqlQueryInstance (SimpleSqlQuery query) {
	super(query);
    }

    protected String getSql() {
	SimpleSqlQuery q = (SimpleSqlQuery)query;
	return q.instantiateSql(values);
    }

    public ResultSet getResult() throws Exception {
	SimpleSqlQuery q = (SimpleSqlQuery)query;
	return q.getSqlResultFactory().getResult(this);
    }

    /**
     * @return Full name of table containing result
     */
    public String getResultAsTable() throws Exception {
	SimpleSqlQuery q = (SimpleSqlQuery)query;
	if (resultTable == null) 
	    resultTable = q.getSqlResultFactory().getResultAsTable(this);
	return resultTable;
    }

    public void checkColumns(ResultSet rs, boolean checkAll) throws Exception {
	Column[] columns = query.getColumns();
	ResultSetMetaData metaData = rs.getMetaData();
	int rsColCount = metaData.getColumnCount();
	String queryName = query.getName();

	if (checkAll) {
	    if (rsColCount != columns.length) 
		throw new Exception("Query '" + queryName + "' declares a different number of columns than are mentioned in the Sql");
	} else {
	    if (rsColCount < columns.length) 
		throw new Exception("Query '" + queryName + "' declares too many columns (more than are mentioned in the Sql");
	}

	HashMap rsCols = new HashMap();
	for (int i=1; i<=rsColCount; i++) {
 	    rsCols.put(metaData.getColumnName(i).toLowerCase(), "");
	}
	
	HashMap alreadySeen = new HashMap();
	for (int i=0; i<columns.length; i++) {
	    String columnName = columns[i].getName();
	    if (alreadySeen.containsKey(columnName)) 
		throw new Exception("Query '" + queryName + "' declares duplicate columns named '" + columnName + "'");
	    alreadySeen.put(columnName, "");
	    if (!rsCols.containsKey(columnName)) 
		throw new Exception("Query '" + queryName + "' declares column '" + columnName + "' but it is not in the Sql");

	}
    }
}
