package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.NotBooleanOperandException;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.ResultFactory;


import java.sql.SQLException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class NullQueryInstance extends QueryInstance {

    public static final NullQueryInstance INSTANCE = new NullQueryInstance();
    
    private NullQueryInstance() {
	super(NullQuery.INSTANCE);
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

    public Query getQuery() {
    	return NullQuery.INSTANCE;
    }

    public ResultList getResult() throws Exception {
    	return null;
    }

    public void setValues(Map values) throws QueryParamsException {
    	// Deliberately empty
    }

    public String getBooleanOperandSql() throws NotBooleanOperandException {
        // TODO Auto-generated method stub
        return new String("method needs to be written!");
    }

    protected ResultList getNonpersistentResult() throws Exception {
	return null;
    }

    protected void writeResultToTable(String resultTableName, 
				      ResultFactory rf) throws SQLException{
	System.err.println("need to write method");
    }

	

}
