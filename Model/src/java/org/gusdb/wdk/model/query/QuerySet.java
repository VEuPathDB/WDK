package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;
import java.util.Enumeration;

public class QuerySet {

    Hashtable querySet;
    String name;

    public QuerySet() {
	querySet = new Hashtable();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public Query getQuery(String name) {
	return (SqlQuery)querySet.get(name);
    }

    public Query[] getQueries() {
	Query[] queries = new Query[querySet.size()];
	Enumeration e = querySet.elements();
	for (int i=0; i<querySet.size(); i++) {
	    queries[i] = (Query)e.nextElement();
	}
	return queries;
    }

    public void addQuery(Query query) {
	querySet.put(query.getName(), query);
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("QuerySet: name='" + name 
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
	
}
