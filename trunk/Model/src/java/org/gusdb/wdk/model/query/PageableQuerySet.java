package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;
import java.util.Enumeration;

public class PageableQuerySet {

    Hashtable querySet;
    String name;

    public PageableQuerySet() {
	querySet = new Hashtable();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public PageableQueryI getQuery(String name) {
	return (PageableQueryI)querySet.get(name);
    }

    public PageableQueryI[] getQueries() {
	PageableQueryI[] queries = new PageableQueryI[querySet.size()];
	Enumeration e = querySet.elements();
	for (int i=0; i<querySet.size(); i++) {
	    queries[i] = (PageableQueryI)e.nextElement();
	}
	return queries;
    }

    public void addQuery(PageableQueryI query) {
	if (querySet.get(query.getName()) != null) 
	    throw new IllegalArgumentException("Query named " 
					       + query.getName() 
					       + " already exists in query set "
					       + getName());
	querySet.put(query.getName(), query);
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("PageableQuerySet: name='" + name 
					   + "'");

       buf.append( newline );
       buf.append( "--- Queries ---" );
       buf.append( newline );
       Enumeration e = querySet.elements();
       for (int i=0; i<querySet.size(); i++) {
	   buf.append( e.nextElement() ).append( newline );
       }

       return buf.toString();
    }

    /////////////////////////////////////////////////////////////////
    ///////  protected
    /////////////////////////////////////////////////////////////////
}
