package org.gusdb.gus.wdk.model;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * QueryNameList.java
 *
 * A simple list where each entry is of type QueryName, representing the fully 
 * qualified name of a Query.  Queries in the list can come
 * from different QuerySets and are not restricted on the different types of 
 * QuerySets they can reference (A QueryNameList could include Queries from 
 * SimpleQuerySets and PageableQuerySets, for example.)
 *
 * Created: Mon May 10 12:34:30 2004
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class QueryNameList{

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * The data structure that will hold the actual list.
     */
    private Hashtable queryNameList;

    /**
     * The name of this QueryNameList.
     */
    private String name;


    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------


    public QueryNameList(){
       	this.queryNameList = new Hashtable();
    }
    
    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * @return  an array of QueryNames, where each entry in the array represents one fully qualified Query.
     */

    public QueryName[] getQueryNames(){

	QueryName queryNames[] = new QueryName[queryNameList.size()];

	Enumeration allQueryNames = queryNameList.keys();
	int counter = 0;

	while (allQueryNames.hasMoreElements()){
	    
	    QueryName nextQueryName = (QueryName)allQueryNames.nextElement();
	    queryNames[counter] = nextQueryName;
	    counter++;
	}
	return queryNames;
    }


    /** 
     * @param fullQueryName  A QueryName wrapper for a string representing a fully qualified Query.
     */
    public void addQueryName(QueryName queryName){

	
	if (queryNameList.get(queryName) != null){
	    throw new IllegalArgumentException("Query " + queryName.getQuerySetName() + "." + queryName.getQueryName() + " already exists in QueryNameList " + getName());
	}
	
	queryNameList.put(queryName, new Integer(1));
    }
  
    /**
     * Wrapper method; checks to make sure each Query Name is of the correct format and exists in a QuerySet.
     */
    public void checkReferences(HashMap simpleSets, HashMap pageableSets)throws Exception{
	
	Enumeration queryNames = queryNameList.keys();
	while (queryNames.hasMoreElements()){
	    QueryName nextQueryName = (QueryName)queryNames.nextElement();
	    
	    //Errors will be handled in the method; we don't use <code>passedCheck</code>
	    boolean passedCheck = nextQueryName.checkReferences(simpleSets, pageableSets);
	}
    }

    public void setName(String name){
	this.name = name;
    }
    
    public String getName(){
	return this.name;
    }

} 
