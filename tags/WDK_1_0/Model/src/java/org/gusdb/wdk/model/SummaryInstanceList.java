package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class SummaryInstanceList implements Iterator {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.view.RIVList");
    
    private SummaryInstance summaryInstance;
    
    public SummaryInstanceList(SummaryInstance summaryInstance) {
	    this.summaryInstance = summaryInstance; 
    }

    public int getSize() {
	return summaryInstance.size();
    }
    
    public boolean hasNext() {
	return summaryInstance.hasMoreRecordInstances();
   }

    public Object next() {
        try {
            return summaryInstance.getNextRecordInstance();
        }
        catch (WdkModelException exp) {
	    throw new RuntimeException(exp);
	}
    }
    
    public void remove() {
        throw new UnsupportedOperationException("remove isn't allowed on this iterator");
    } 

}
    
