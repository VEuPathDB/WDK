package org.gusdb.gus.wdk.model.implementation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;

public class NullQueryInstance {

	public static final NullQueryInstance INSTANCE = new NullQueryInstance();
	
	private NullQueryInstance() {
		// Deliberately empty
	}
	
    public Collection getValues() {
    	return Collections.EMPTY_LIST;
    }

    public boolean getIsCacheable() {
    	return false;
    }

    public void setIsCacheable(boolean isCacheable) {
    	// Deliberately empty
    }

    public QueryI getQuery() {
    	return NullQuery.INSTANCE;
    }

    public ResultList getResult() throws Exception {
    	return null;
    }

    public void setValues(Map values) throws QueryParamsException {
    	// Deliberately empty
    }

}
