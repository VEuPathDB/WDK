package org.gusdb.gus.wdk.model.implementation;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.gusdb.gus.wdk.model.QueryI;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.SimpleQueryInstanceI;

public class NullQueryInstance implements SimpleQueryInstanceI {

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

    public ResultSet getResult() throws Exception {
    	return null;
    }

    public void setValues(Map values) throws QueryParamsException {
    	// Deliberately empty
    }

	public void checkColumns(ResultSet rs, boolean checkAll) throws Exception {
		// TODO Auto-generated method stub		
	}
}
