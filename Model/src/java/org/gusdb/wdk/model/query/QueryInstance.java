package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;

public class QueryInstance {

    Query query;
    Hashtable values = new Hashtable();

    protected QueryInstance (Query query) {
	this.query = query;
    }

    protected void setValues(Hashtable values) throws QueryParamsException {
	this.values = values;
	query.validateParamValues(values);
    }

}
