package org.gusdb.gus.wdk.model.query.implementation;

import java.sql.SQLException;
import java.sql.ResultSet;

import org.gusdb.gus.wdk.model.query.SimpleQueryInstanceI;

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
}
