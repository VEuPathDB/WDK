package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.Column;

import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.NotBooleanOperandException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;


public class SqlQueryInstance extends QueryInstance  {


    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    /**
     * The unique name of the table in a database namespace which holds the cached 
     * results for this Instance.
     */
    String resultTable = null;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    
    public SqlQueryInstance (SqlQuery query) {
	
	super(query);
	setIsCacheable(query.getIsCacheable().booleanValue());
    }

    protected String getSql() {
	SqlQuery q = (SqlQuery)query;
	return q.instantiateSql(values);
    }

    /**
     * @return Full name of table containing result
     */
    public String getResultAsTable() throws Exception {
	SqlQuery q = (SqlQuery)query;
	if (resultTable == null) 
	    resultTable = q.getResultFactory().getSqlResultFactory().getResultAsTable(this);
	return resultTable;
    }
    

    public ResultList getResult() throws Exception {
	SqlQuery q = (SqlQuery)query;
	ResultList rl = q.getResultFactory().getSqlResultFactory().getResult(this);
	//	rl.checkQueryColumns(q, true);
	return rl;
    }

    public String getBooleanOperandSql() throws NotBooleanOperandException{
	return new String("method needs to be written!");
    }


}
