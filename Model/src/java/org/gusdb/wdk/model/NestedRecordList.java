package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.RecordClass;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class NestedRecordList {

    protected Query query;
    protected RecordClass recordClass;
    protected String queryTwoPartName;
    protected String recordClassTwoPartName;
    
    public NestedRecordList(){
    }

    public void setQueryRef(String queryTwoPartName){
	this.queryTwoPartName =  queryTwoPartName;
    }

    public void setRecordClassRef(String recordClassTwoPartName){
	this.recordClassTwoPartName = recordClassTwoPartName;
    }

    RecordInstance[] getRecordInstances(RecordInstance parentInstance) throws WdkModelException, WdkUserException{
	
	Map allAtts = parentInstance.getAttributes();
	Iterator keys = allAtts.keySet().iterator();
	while (keys.hasNext()){
	    String nextKey = (String)keys.next();
	    Object value = allAtts.get(nextKey);
	}
	Param myQueryParams[] = query.getParams();
	HashMap queryValues = new HashMap();
	for (int i = 0; i < myQueryParams.length; i++){
	    Param nextParam = myQueryParams[i];
	    String paramName = nextParam.getName();
	    FieldI field = (FieldI)parentInstance.getRecordClass().getField(paramName);
	    String value;
	    if (field instanceof PrimaryKeyField){
		value = parentInstance.getPrimaryKey().toString();
	    }
	    else if (field instanceof AttributeField){
		value = parentInstance.getAttributeValue(paramName).toString();
	    }
	    else {
		throw new WdkModelException ("Illegal to link NestedRecordList " + getFullName() + " on attribute of type " + field.getClass().getName());
	    }
	    
	    queryValues.put(paramName, value);
	}
	QueryInstance qi = query.makeInstance();
	qi.setValues(queryValues);
	ResultList rl = qi.getResult();
	Column[] columns = query.getColumns();
	String primaryKeyName = columns[0].getName();
	
	Vector allRecordInstances = new Vector();

	while (rl.next()){
	    RecordInstance nextRecordInstance = this.recordClass.makeRecordInstance();
	    String primaryKeyValue = rl.getAttributeFieldValue(primaryKeyName).getValue().toString();

	    nextRecordInstance.setPrimaryKey(primaryKeyValue);
	    allRecordInstances.add(nextRecordInstance);
	}

	rl.close();
	RecordInstance finalList[] = new RecordInstance[allRecordInstances.size()];
	allRecordInstances.copyInto(finalList);

	return finalList;
	
    }

    void resolveReferences(WdkModel model) throws WdkModelException{

	this.query = (Query)model.resolveReference(queryTwoPartName, getFullName(), "nestedRecordList", "queryRef");
	this.recordClass = (RecordClass)model.resolveReference(recordClassTwoPartName, getFullName(), "nestedRecordList", "recordClassRef");
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
