package org.gusdb.gus.wdk.model.query;

import org.gusdb.gus.wdk.model.query.implementation.SqlResultFactory;

public class ResultFactory {
    SqlResultFactory sqlResultFactory;

    public ResultFactory() {
    }

    public void setSqlResultFactory(SqlResultFactory sqlResultFactory) {
	this.sqlResultFactory = sqlResultFactory;
    }

    public SqlResultFactory getSqlResultFactory() {
	return sqlResultFactory;
    }
}
