package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class PageableQuerySet {

    HashMap querySet;
    String name;

    public PageableQuerySet() {
	querySet = new HashMap();
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
	Iterator queryIterator = querySet.values().iterator();
	int i = 0;
	while (queryIterator.hasNext()) {
	    queries[i++] = (PageableQueryI)queryIterator.next();
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
       Iterator queryIterator = querySet.values().iterator();
       while (queryIterator.hasNext()) {
	   buf.append(queryIterator.next()).append( newline );
       }

       return buf.toString();
    }

    public void resolveReferences(Map querySetMap) throws Exception {
       Iterator queryIterator = querySet.values().iterator();
       while (queryIterator.hasNext()) {
	   PageableQueryI pq = (PageableQueryI)queryIterator.next();
	   pq.resolveReferences(querySetMap);
       }
    }
    
    /////////////////////////////////////////////////////////////////
    ///////  protected
    /////////////////////////////////////////////////////////////////
}
