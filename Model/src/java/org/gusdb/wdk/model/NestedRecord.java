package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.RecordClass;

import java.util.HashMap;


public class NestedRecord {

    protected Query query;
    protected RecordClass recordClass;
    protected String queryTwoPartName;
    protected String recordClassTwoPartName;
    

    //todo:
    //special paramRefs for nested records?
    //decide if only primary key will be a join
    //validate links between nested record query and parent record instance
    //nicer print statement in record?
    //different fullname convention?
    //all this in nestedRecordList as well
    //also in nestedRecordList, maybe want to print as table?  figure out best way to do that

    public NestedRecord(){
    }

    public void setQueryRef(String queryTwoPartName){
	this.queryTwoPartName =  queryTwoPartName;
    }

    public void setRecordClassRef(String recordClassTwoPartName){
	this.recordClassTwoPartName = recordClassTwoPartName;
    }

    RecordInstance getRecordInstance(RecordInstance parentInstance) throws WdkModelException, WdkUserException{
	
	Param myQueryParams[] = query.getParams();
	HashMap queryValues = new HashMap();
	for (int i = 0; i < myQueryParams.length; i++){
	    Param nextParam = myQueryParams[i];
	    String paramName = nextParam.getName();
	    String value = parentInstance.getAttributeValue(paramName).toString();

	    queryValues.put(paramName, value);
	}
	QueryInstance qi = query.makeInstance();
	qi.setValues(queryValues);
	ResultList rl = qi.getResult();
	
	RecordInstance recordInstance = this.recordClass.makeRecordInstance();
	Column[] columns = query.getColumns();
	String primaryKeyName = columns[0].getName();
	rl.next();
	String primaryKey = rl.getAttributeFieldValue(primaryKeyName).getValue().toString();
       	recordInstance.setPrimaryKey(primaryKey);
	
	if (rl.next()){
	    throw new WdkModelException ("Query " + query.getName() + " called from nestedRecord " + getFullName() + " returned a result with more than one value");
	}

	rl.close();
	return recordInstance;
	
    }

    void resolveReferences(WdkModel model) throws WdkModelException{

	this.query = (Query)model.resolveReference(queryTwoPartName, getFullName(), "nestedRecord", "queryRef");
	this.recordClass = (RecordClass)model.resolveReference(recordClassTwoPartName, getFullName(), "nestedRecord", "recordClassRef");
    }

    String getFullName(){
	return queryTwoPartName + "." + this.recordClassTwoPartName;
    }
    
    protected RecordClass getRecordClass(){
	return this.recordClass;
    }

    protected Query getQuery(){
	return this.query;
    }

}    
