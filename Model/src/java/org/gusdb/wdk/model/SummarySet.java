package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Iterator;

/**
 * SummarySet.java
 *
 * Created: Fri June 4 15:05:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */


public class SummarySet implements ModelSetI {

    LinkedHashMap summarySet;
    String name;
    String displayName;
    String description;

    public SummarySet() {
	summarySet = new LinkedHashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

    public String getDisplayName() {
	return (displayName != null)? displayName : name;
    }

     public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

    public Summary getSummary(String name) throws WdkUserException {

	Summary s = (Summary)summarySet.get(name);
	if (s == null) throw new WdkUserException("Summary Set " + getName() + " does not include summary " + name);
	return s;
    }

    public Object getElement(String name) {
	return summarySet.get(name);
    }

    public Summary[] getSummaries() {
	Summary[] summarys = new Summary[summarySet.size()];
	Iterator summaryIterator = summarySet.values().iterator();
	int i = 0;
	while (summaryIterator.hasNext()) {
	    summarys[i++] = (Summary)summaryIterator.next();
	}
	return summarys;
    }

    public void addSummary(Summary summary) throws WdkModelException {
	if (summarySet.get(summary.getName()) != null) 
	    throw new WdkModelException("Summary named " 
					+ summary.getName() 
					+ " already exists in summary set "
					+ getName());
	
	summarySet.put(summary.getName(), summary);
    }

    public void resolveReferences(WdkModel model) throws WdkModelException{
	Iterator summaryIterator = summarySet.values().iterator();
	while (summaryIterator.hasNext()){
	    Summary summary = (Summary)summaryIterator.next();
	    summary.resolveReferences(model);
	}
    }

    public void setResources(WdkModel model) throws WdkModelException {
	Iterator summaryIterator = summarySet.values().iterator();
	while (summaryIterator.hasNext()){
	    Summary summary = (Summary)summaryIterator.next();
	    summary.setSummarySet(this);
	}
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer("QuestionSet: name='" + getName() + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  description='" + getDescription() + "'" + newline);
       buf.append( newline );

       Iterator summaryIterator = summarySet.values().iterator();
       while (summaryIterator.hasNext()) {
	   buf.append( newline );
	   buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
	   buf.append( newline );
	   buf.append(summaryIterator.next()).append( newline );
       }

       return buf.toString();
	
    }

}
