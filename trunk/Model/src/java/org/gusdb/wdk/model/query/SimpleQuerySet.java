package org.gusdb.gus.wdk.model.query;

import java.util.HashMap;
import java.util.Iterator;

public class SimpleQuerySet {

    HashMap querySet;
    String name;
    ResultFactory resultFactory;

    public SimpleQuerySet() {
	querySet = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public SimpleQueryI getQuery(String name) {
	return (SimpleQueryI)querySet.get(name);
    }

    public SimpleQueryI[] getQueries() {
	SimpleQueryI[] queries = new SimpleQueryI[querySet.size()];
	Iterator queryIterator = querySet.values().iterator();
	int i = 0;
	while (queryIterator.hasNext()) {
	    queries[i++] = (SimpleQueryI)queryIterator.next();
	}
	return queries;
    }

    public void addQuery(SimpleQueryI query) {
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
	StringBuffer buf = new StringBuffer("SimpleQuerySet: name='" + name 
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
	    SimpleQueryI query = (SimpleQueryI)queryIterator.next();
	    query.setResultFactory(resultFactory);
	}
    }
}
