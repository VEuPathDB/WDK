package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

public class WdkModel {

    HashMap simpleQuerySets = new HashMap();
    HashMap pageableQuerySets = new HashMap();
    String name;
    ResultFactory resultFactory;

    public WdkModel() {
	this.resultFactory = new ResultFactory();
    }

    public ResultFactory getResultFactory() {
	return resultFactory;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void addSimpleQuerySet(SimpleQuerySet querySet) {
	String err = checkName(querySet.getName());
	if (err != null) throw new IllegalArgumentException(err);
	querySet.setResultFactory(resultFactory);
	simpleQuerySets.put(querySet.getName(), querySet);
    }

    public SimpleQuerySet getSimpleQuerySet(String setName) {
	if (!simpleQuerySets.containsKey(setName)) {
	    String err = "Query set container " + name +
		" does not contain a simple query set with name " + setName;
	    throw new IllegalArgumentException(err);
	}
	return (SimpleQuerySet)simpleQuerySets.get(setName);
    }

    public boolean hasSimpleQuerySet(String setName) {
	return simpleQuerySets.containsKey(setName);
    }

    public void addPageableQuerySet(PageableQuerySet querySet) {
	String err = checkName(querySet.getName());
	if (err != null) throw new IllegalArgumentException(err);
	pageableQuerySets.put(querySet.getName(), querySet);
    }

    public PageableQuerySet getPageableQuerySet(String setName) {
	if (!pageableQuerySets.containsKey(setName)) {
	    String err = "Query set container " + name +
		" does not contain a pageable query set with name " + setName;
	    throw new IllegalArgumentException(err);
	}
	return (PageableQuerySet)pageableQuerySets.get(setName);
    }

    public boolean hasPageableQuerySet(String setName) {
	return pageableQuerySets.containsKey(setName);
    }

    /**
     * Some elements within the set may refer to others by name.  Resolve those
     * references into real object references.
     */ 
    public void resolveReferences() throws Exception {
       Iterator querySetIterator = pageableQuerySets.values().iterator();
       while (querySetIterator.hasNext()) {
	   PageableQuerySet pqs = (PageableQuerySet)querySetIterator.next();
	   pqs.resolveReferences(simpleQuerySets);
       }
    }
    
    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("WdkModel: name='" + name 
					   + "'");

       buf.append( newline );
       buf.append( "--- Query Sets---" );
       buf.append( newline );
       Iterator querySetIterator = simpleQuerySets.values().iterator();
       while (querySetIterator.hasNext()) {
	   buf.append( querySetIterator.next() ).append( newline );
       }
       buf.append(newline);
       querySetIterator = pageableQuerySets.values().iterator();
       while (querySetIterator.hasNext()) {
	   buf.append( querySetIterator.next() ).append( newline );
       }

       return buf.toString();
    }

    /*
    public void addParamSet(ParamSet paramSet) {
    }
    */

    ///////////////////////////////////////////////////////////////////
    ///////   Protected methods
    ///////////////////////////////////////////////////////////////////
    /*
     * @return error message or null if ok
     */
    String checkName(String setName) {
	String err = null;
	if (simpleQuerySets.containsKey(setName)) {
	    err = "Query set container " + name +
		" already contains a query set with name " + setName;
	}

	if (pageableQuerySets.containsKey(setName)) {
	    err = "Query set container " + name +
		" already contains a pageable query set with name " + setName;
	}
	return err;
    }
}

