package org.gusdb.gus.wdk.model;

import java.util.Map;

import org.gusdb.gus.wdk.model.implementation.SqlQuery;

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

    public void resolveReferences(Map querySetMap, Map recordSetMap)throws Exception{
	
	this.query = (SqlQuery)QuerySet.resolveReference(querySetMap, queryTwoPartName, "null", "null", "null");
	this.record = RecordSet.resolveRecordReference(recordSetMap, recordTwoPartName);
    }

    public String getName(){
	return name;
    }

    public void setName(String name){
	this.name = name;
    }

}
