package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SummarySet.java
 *
 * Created: Fri June 4 15:05:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */


public class SummarySet implements ModelSetI {

    HashMap summarySet;
    String name;

    public SummarySet() {
	summarySet = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public Summary getSummary(String name) {
	return (Summary)summarySet.get(name);
    }

    public Object getElement(String name) {
	return summarySet.get(name);
    }

    public Summary[] getSummarys() {
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

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("SummarySet: name='" + name 
					   + "'");

       buf.append( newline );
       buf.append( "--- Summaries ---" );
       buf.append( newline );
       Iterator summaryIterator = summarySet.values().iterator();
       while (summaryIterator.hasNext()) {
	   buf.append(summaryIterator.next()).append( newline );
       }

       return buf.toString();
	
    }

}
