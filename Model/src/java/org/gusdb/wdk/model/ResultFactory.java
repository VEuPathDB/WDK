package org.gusdb.gus.wdk.model;

import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;

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
