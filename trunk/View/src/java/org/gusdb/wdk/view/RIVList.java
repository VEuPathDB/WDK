package org.gusdb.gus.wdk.view;

import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.SummaryInstance;
import org.gusdb.gus.wdk.model.WdkModelException;

import java.util.Iterator;
import java.util.NoSuchElementException;



/**
 * A view on a RecordInstance as a Map. Currently only handles the text and non-text fields 
 * ie not nested tables. If this works it should probably be refactored into the model 
 * to avoid the extra wrapper instance.
 * 
 * Most of the work is done in the constructor to prepare a private map. Most of the other 
 * methods delegate to this map. 
 * 
 * @author art
 */
public class RIVList implements Iterator {

    private SummaryInstance si;

    public RIVList(SummaryInstance si) {
        this.si = si;
    }

    public int getSize() {
        return si.size();
    }
    
    public boolean hasNext() {
        return si.hasMoreRecordInstances();
    }

    public Object next() {
        RecordInstance ri = null;
        try {
            ri = si.getNextRecordInstance();
        }
        catch (WdkModelException exp) {
            // Deliberately ignore. Hope we're not hiding a bug
        }
        if ( ri == null ) {
            return new NoSuchElementException();  
        }
        return new RecordInstanceView(ri);
    }

    public void remove() {
        throw new UnsupportedOperationException("remove isn't allowed on this iterator");
    } 

    public String toString() {
        return "I'm an RIVList: si="+si+", size="+getSize();
    }
    
    
}
    
