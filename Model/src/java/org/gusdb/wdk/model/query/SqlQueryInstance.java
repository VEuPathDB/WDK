package org.gusdb.gus.wdk.model.query;

public class SqlQueryInstance extends QueryInstance {

    protected SqlQueryInstance (SqlQuery query) {
	super(query);
    }

    protected String getSql() {
	SqlQuery q = (SqlQuery)query;
	return q.instantiateSql(values);
    }
}
