package org.gusdb.gus.wdk.model;

import java.util.Map;

/**
 * Summary.java
 *
 * A class representing a binding between a Record and a Query.
 *
 * Created: Fri June 4 11:19:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class Summary {

    private String recordTwoPartName;

    private String queryTwoPartName;

    private String name;


    //the only column in this query should be a primary key
    Query query;
    
    //QueryInstance to be shared across all SummaryInstances 
    //produced by this Summary
    QueryInstance listIdQueryInstance;

    Record record;

    public Summary(){
    }
    
    public SummaryInstance makeSummaryInstance(){

	if (listIdQueryInstance == null){
	    listIdQueryInstance = query.makeInstance();
	}
	return new SummaryInstance(this, listIdQueryInstance);
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

    public void resolveReferences(WdkModel model)throws WdkModelException{
	
	this.query = (Query)model.resolveReference(queryTwoPartName, name, "summary", "queryRef");
	this.record = (Record)model.resolveReference(recordTwoPartName, name, "summary", "recordRef");
    }

    public String getName(){
	return name;
    }

    public void setName(String name){
	this.name = name;
    }

    public int getTotalLength(Map values) throws WdkModelException, WdkUserException{
	SummaryInstance rli = makeSummaryInstance();
	rli.setValues(values, 0, 0); 
	return rli.getTotalLength();
    }
    //set dummy values for start and end because they will not be used.
    //(might have to change this depending on resolution of efficiency issue)

    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf =
	    new StringBuffer("Summary: name='" + name + "'" + newline  +
			     "  record='" + recordTwoPartName + "'" + newline +
			     "  query='" + queryTwoPartName + "'" + newline
			     );	    
	return buf.toString();
    }
    
}
