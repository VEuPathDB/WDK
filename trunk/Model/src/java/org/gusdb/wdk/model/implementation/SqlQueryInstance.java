package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.NotBooleanOperandException;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.ResultList;


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
	String sql = null;
	if (inMultiMode){

	    String newPkJoin = multiModeResultTableName + "." + pkToJoinWith;
	    values.put("primaryKey", newPkJoin); //will this destroy the query for later use?
	}
	String initSql = q.instantiateSql(values);
	if (inMultiMode){
	    sql = q.addMultiModeConstraints(multiModeResultTableName, pkToJoinWith,
					    startId, endId, initSql);
	}
	else {
	    sql = initSql;
	}
	return sql;
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
	rl.checkQueryColumns(q, true);
	return rl;
    }

    public String getBooleanOperandSql() throws NotBooleanOperandException{
	return new String("method needs to be written!");
    }


}
