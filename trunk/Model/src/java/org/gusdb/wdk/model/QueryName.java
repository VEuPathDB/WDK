package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import org.gusdb.gus.wdk.model.QuerySet;


/**
 * QueryName.java
 *
 * Class representing the fully qualified name of a Query; knows the name of 
 * the QuerySet containing the Query and the name of the Query itself. 
 *
 * Created: Tue May 11 15:17:30 EDT 2004
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class QueryName{


    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------
    
    /**
     * Name of the QuerySet that contains this Query.
     */
    String querySetName;

    /**
     * Actual name of the Query within the QuerySet.
     */
    String queryName;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /**
     * Empty Constructor.
     */
    public QueryName(){}

    /**
     * Constructor that takes a fully qualified name of a Query (in "QuerySetName.QueryName") format to initialize
     * this QueryName.
     */
    public QueryName(String fullQueryName) throws Exception{
	
	setFullQueryName(fullQueryName);
    }
    
   
    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public String getQueryName(){
	return this.queryName;
    }

    public String getQuerySetName(){
	return this.querySetName;
    }

    /**
     * Initialize this QueryName with the data provided in <code>fullQueryName</code>.  This
     * method cannot be called on a QueryName that has data already set.
     * 
     * @param fullQueryName String with format "QuerySetName.QueryName" representing a fully 
     *                      qualified query name.
     *
     * @throws Exception    if the Query is in the incorrect format, or if this QueryName has 
     *                      already been initialized.
     */
     
    public void setFullQueryName(String fullQueryName)throws Exception{
	
	if (querySetName != null || queryName != null){
	    throw new Exception("Cannot reset initialized QueryName (query set: " + querySetName + ", query name: " + queryName + " to be " + fullQueryName);
	}
	validateAndSetQueryName(fullQueryName);
    }

    /**
     * Checks to make sure the wrapped query is of format QuerySetName.QueryName and that it exists in one of 
     * the provided querySets.
     *
     * @param simpleSets   a HashMap of SimpleQuerySets which may hold this query.
     * @param pageableSets a HashMap of PageableQuerySets which may hold this query.
     * @return true if the check passes 
     */
        public boolean checkReferences(HashMap querySets) throws Exception {


	//DTB -- this could be cleaned up if we abstract QuerySet to be the superclass of SimpleQuerySet and PageableQuerySet
	//UPTATE time to clean it up!
	QuerySet querySet = (QuerySet)querySets.get(querySetName);

	if (querySet != null){

	    Query query = querySet.getQuery(queryName);
	    if (query != null){
		return true;  //passed check
	    }
	    else {  
		String error = "QueryNameList error: '" + queryName + "' did not pass check; querySet '" + querySet.getName() + "' does not contain '" + queryName + "'";
		throw new Exception(error);
	    }
	}

	throw new Exception("QueryNameList error: '" + queryName + "' did not pass check;  there is no querySet with name '" + queryName + "' in this model");

    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    private void validateAndSetQueryName(String fullQueryName) throws Exception{
	

	if (!fullQueryName.matches("\\w+\\.\\w+")) {
	    String error = "QueryNameList format error:  QueryName '" + queryName + "' is not in the form 'querySetName.QueryName'";
	    throw new Exception(error);
	}
	    
	String[] parts = fullQueryName.split("\\.");
	this.querySetName = parts[0];
	this.queryName = parts[1];
    }

}
