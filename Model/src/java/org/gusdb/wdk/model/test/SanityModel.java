package org.gusdb.wdk.model.test;

import java.util.Vector;

import org.w3c.dom.Document;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;


/**
 * SanityModel.java
 *
 * Model used in a sanity test.  Contains all sanity elements representing records
 * and queries that will be excercised over the course of the sanity test.  
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: sfischer $
 */

//DTB -- Took out ability to get a SanityRecord or SanityQuery by name, as so far is 
//not needed.  If this is put back in, the method will need to account for the fact
//that there can be multiple SanityRecords and SanityQueries for one wdk record or query.

public class SanityModel {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------
    
    /**
     * SanityRecords contained in this model.  
     */
    Vector sanityRecords = new Vector();

    /**
     * SanityQueries contained in this model.  
     */
    Vector sanityQueries = new Vector();

    /**
     * Document set by the xml parser that creates this model. (DTB -- not sure where
     * document is ever used!).
     */ 
    private Document document;

    /**
     * Instance object used by SanityTestXmlParser to create a SanityModel if no schema 
     * is provided.
     */
    public static final SanityModel INSTANCE = new SanityModel();
    
    // ------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------

    //SanityRecord Sets
    public void addSanityRecord(SanityRecord sanityRecord) throws WdkModelException {
	sanityRecords.add(sanityRecord);
    }

    public SanityRecord[] getAllSanityRecords(){
	    
        SanityRecord records[] = new SanityRecord[sanityRecords.size()];
        for (int i = 0; i < sanityRecords.size(); i++){
	    SanityRecord nextRecord = (SanityRecord)sanityRecords.elementAt(i);
	    records[i] = nextRecord;
	}
	return records;
    }

    /**
     * @param recordName Two-part name (recordSetName.recordName) of the record in question.
     * @param return True if the model contains one or more SanityRecords for the given recordName. 
    */
    public boolean hasSanityRecord(String recordName){

	for (int i = 0; i < sanityRecords.size(); i++){
	    SanityRecord nextRecord = (SanityRecord)sanityRecords.elementAt(i);
	    if (nextRecord.getRef().equals(recordName)){
		return true;
	    }
	}
	return false;
    }

    //SanityQuery Sets
    public void addSanityQuery(SanityQuery sanityQuery) throws WdkModelException {

	sanityQueries.add(sanityQuery);
    }

    public SanityQuery[] getAllSanityQueries(){
	
        SanityQuery queries[] = new SanityQuery[sanityQueries.size()];
        for (int i = 0; i < sanityQueries.size(); i++){
	    SanityQuery nextQuery = (SanityQuery)sanityQueries.elementAt(i);
	    queries[i] = nextQuery;
	}
	return queries;
    }

    /**
     * @param queryName Two-part name (querySetName.queryName) of the query in question.
     * @param return True if the model contains one or more SanityQueries for the given queryName. 
     */
    public boolean hasSanityQuery(String queryName){

	for (int i = 0; i < sanityQueries.size(); i++){
	    SanityQuery nextQuery = (SanityQuery)sanityQueries.elementAt(i);
	    if (nextQuery.getRef().equals(queryName)){
		return true;
	    }
	}
	return false;
    }

    public String toString() {
	StringBuffer result = new StringBuffer("SanityModel\nSanityQueries:\n");
	SanityQuery queries[] = getAllSanityQueries();
	if (queries != null){
	    for (int i = 0; i < queries.length; i++){
		SanityQuery nextSanityQuery = queries[i];
		result.append(nextSanityQuery.toString() + "\n");
	    }
	}
	result.append("SanityRecords:\n");
	SanityRecord records[] = getAllSanityRecords();
	if (records != null){
	    for (int i = 0; i < records.length; i++){
		SanityRecord nextSanityRecord = records[i];
		result.append(nextSanityRecord.toString() + "\n");
	    }
	}
	return result.toString();
    }
       
    public void validateQueries() throws WdkUserException{

	for (int i = 0; i < sanityQueries.size(); i++){
	    SanityQuery nextSanityQuery = (SanityQuery)sanityQueries.elementAt(i);
	    if (nextSanityQuery.getMinOutputLength().intValue() < 1){
		throw new WdkUserException("SanityQuery " + nextSanityQuery.getRef() + " must return at least 1 row.  Please set its minOutputLength attribute to reflect this");
	    }
	}
    }

    // ------------------------------------------------------------------
    // Protected Methods
    // ------------------------------------------------------------------
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
}

