package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.FieldI;
import org.gusdb.wdk.model.SummaryInstance;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a Summary that provides simplified access for 
 * consumption by a view
 */ 
public class SummaryBean {

    SummaryInstance summary;
    

    public SummaryBean(SummaryInstance summary) {
	this.summary = summary;
    }

    public Map getParams() {
	return summary.getDisplayParams();
    }

    public int getPageSize() {
	return summary.getPageSize();
    }

    public int getResultSize() {
	try {
	    return summary.getResultSize();
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
    }

    public RecordClassBean getRecordClass() {
	return new RecordClassBean(summary.getQuestion().getRecordClass());
    }

    /**
     * @returns An iterator of RecordBeans.
     */
    public Iterator getRecords() {
	return new RecordBeanList();
    }

    class RecordBeanList implements Iterator {

	public int getSize() {
	    return summary.getPageSize();
	}
    
	public boolean hasNext() {
	    return summary.hasMoreRecordInstances();
	}
	
	public Object next() {
	    try {
		return new RecordBean(summary.getNextRecordInstance());
	    }
	    catch (WdkModelException exp) {
		throw new RuntimeException(exp);
	    }
	}
    
	public void remove() {
	    throw new UnsupportedOperationException("remove isn't allowed on this iterator");
	} 
	
    }
    

}
