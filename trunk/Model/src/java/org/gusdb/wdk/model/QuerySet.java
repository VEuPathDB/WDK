package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.gusdb.gus.wdk.model.QueryName;

public class QuerySet {

    HashMap querySet;
    String name;
    ResultFactory resultFactory;

    public QuerySet() {
	querySet = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public Query getQuery(String name) {
	return (Query)querySet.get(name);
    }

    public Query[] getQueries() {
	Query[] queries = new Query[querySet.size()];
	Iterator queryIterator = querySet.values().iterator();
	int i = 0;
	while (queryIterator.hasNext()) {
	    queries[i++] = (Query)queryIterator.next();
	}
	return queries;
    }

    public void addQuery(Query query) {
	if (querySet.get(query.getName()) != null) 
	    throw new IllegalArgumentException("Query named " 
					       + query.getName() 
					       + " already exists in query set "
					       + getName());
	query.setResultFactory(resultFactory);
	querySet.put(query.getName(), query);
    }

    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer("QuerySet: name='" + name 
					   + "'");

	buf.append( newline );
	buf.append( "--- Queries ---" );
	buf.append( newline );
	Iterator queryIterator = querySet.values().iterator();
	while (queryIterator.hasNext()) {
	    buf.append( queryIterator.next() ).append( newline );	
	}
	return buf.toString();
    }

    /////////////////////////////////////////////////////////////////
    ///////  protected
    /////////////////////////////////////////////////////////////////
    void setResultFactory(ResultFactory resultFactory) {
	this.resultFactory = resultFactory;
	Iterator queryIterator = querySet.values().iterator();
	while (queryIterator.hasNext()) {
	    Query query = (Query)queryIterator.next();
	    query.setResultFactory(resultFactory);
	}
    }

    /////////////////////////////////////////////////////////////////
    ///////  static
    /////////////////////////////////////////////////////////////////

    public static Query resolveReference(Map querySetMap, String twoPartName, 
					 String callerType, String callerName, 
					 String callerAttribute) throws Exception {
	String s = callerType + " '" + callerName + "' has a " + callerAttribute;

	//ensures <code>twoPartName</code> is formatted correctly
	QueryName fullQueryName = new QueryName(twoPartName);

	String querySetName = fullQueryName.getQuerySetName();
	String queryName = fullQueryName.getQueryName();

	QuerySet sqs = (QuerySet)querySetMap.get(querySetName);
	if (sqs == null) {
	    String s3 = s + " which contains an unrecognized querySet '" 
		+ querySetName + "'";
	    throw new Exception(s3);
	}
	Query sq = sqs.getQuery(queryName);
	if (sq == null) {

	    String s4 = s + " which contains an unrecognized query '" 
		+ queryName + "'";
	    throw new Exception(s4);
	}
	return sq;
    }
}
