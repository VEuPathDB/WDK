package org.gusdb.gus.wdk.model;

import java.util.Map;
import java.util.Hashtable;

/**
 * RecordList.java
 *
 * A class representing a binding between a Record and a Query.
 *
 * Created: Fri June 4 11:19:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class RecordList {

    private String recordTwoPartName;

    private String queryTwoPartName;

    private String name;


    //the only column in this query should be a primary key
    Query query;
    
    //QueryInstance to be shared across all RecordListInstances 
    //produced by this RecordList
    QueryInstance listIdQueryInstance;

    Record record;

    public RecordList(){
    }
    
    public RecordListInstance makeRecordListInstance(){

	if (listIdQueryInstance == null){
	    listIdQueryInstance = query.makeInstance();
	}
	return new RecordListInstance(this, listIdQueryInstance);
    }

    public Query getQuery(){
	return this.query;
    }
	
    public Record getRecord(){
	return this.record;
    }

    public void setRecordRef(String recordTwoPartName){

	this.recordTwoPartName = recordTwoPartName;
    }

    public void setQueryRef(String queryTwoPartName){

	this.queryTwoPartName = queryTwoPartName;
    }

    public void resolveReferences(Map querySetMap, Map recordSetMap)throws WdkModelException{
	
	this.query = QuerySet.resolveReference(querySetMap, queryTwoPartName, "recordList", name, "queryRef");
	this.record = RecordSet.resolveRecordReference(recordSetMap, recordTwoPartName);
    }

    public String getName(){
	return name;
    }

    public void setName(String name){
	this.name = name;
    }

    public int getTotalLength(Hashtable values) throws Exception{
	RecordListInstance rli = makeRecordListInstance();
	rli.setValues(values, 0, 0); 
	return rli.getTotalLength();
    }
    //set dummy values for start and end because they will not be used.
    //(might have to change this depending on resolution of efficiency issue)

}
