package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

public class QuerySet implements ModelSetI {

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

   public Query getQuery(String name) throws WdkUserException {
	Query q = (Query)querySet.get(name);
	if (q == null) throw new WdkUserException("Query Set " + getName() + " does not include query " + name);
	return q;
    }

    public Object getElement(String name) {
	return querySet.get(name);
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

    public void addQuery(Query query) throws WdkModelException {
	if (querySet.get(query.getName()) != null) 
	    throw new WdkModelException("Query named " 
					+ query.getName() 
					+ " already exists in query set "
					+ getName());
	querySet.put(query.getName(), query);
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
	Iterator queryIterator = querySet.values().iterator();
	while (queryIterator.hasNext()) {
	    Query query = (Query)queryIterator.next();
	    query.resolveReferences(model);
	}
    }

    public void setResources(WdkModel model) throws WdkModelException {
	Iterator queryIterator = querySet.values().iterator();
	while (queryIterator.hasNext()) {
	    Query query = (Query)queryIterator.next();
	    query.setResources(model);
	    query.setFullName(this.getName());
	}
    }

    public String toString() {
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer("QuerySet: name='" + name 
					   + "'");
	buf.append( newline );
	Iterator queryIterator = querySet.values().iterator();
	while (queryIterator.hasNext()) {
	    buf.append( newline );
	    buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
	    buf.append( newline );
	    buf.append( queryIterator.next() ).append( newline );	
	}
	return buf.toString();
    }

    /////////////////////////////////////////////////////////////////
    ///////  protected
    /////////////////////////////////////////////////////////////////

}
