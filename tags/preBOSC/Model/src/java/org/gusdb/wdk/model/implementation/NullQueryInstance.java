package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.NotBooleanOperandException;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.QueryInstance;
import org.gusdb.gus.wdk.model.WdkUserException;
import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.ResultFactory;

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

    public ResultList getResult() throws WdkModelException {
    	return null;
    }

    public void setValues(Map values) throws WdkUserException {
    	// Deliberately empty
    }

    public String getBooleanOperandSql() throws NotBooleanOperandException {
        // TODO Auto-generated method stub
        return new String("method needs to be written!");
    }

    protected ResultList getNonpersistentResult() throws WdkModelException {
	return null;
    }

    protected void writeResultToTable(String resultTableName, 
				      ResultFactory rf) throws WdkModelException{
        //TODO Need to write
	System.err.println("need to write method");
    }

    public String getResultAsTable() throws WdkModelException {
        // TODO Auto-generated method stub
        return null;
    }

	

}
