package org.gusdb.gus.wdk.view;

import org.gusdb.gus.wdk.controller.WdkLogManager;
import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.ResultList;
import org.gusdb.gus.wdk.model.SummaryInstance;
import org.gusdb.gus.wdk.model.WdkModelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;



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

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.gus.wdk.view.RIVList");
    
    private ResultList rl;
    private SummaryInstance si;
    private Map displayNames = new HashMap(); 
    private List columnNames; 
    private boolean rlHasNextCalled = false;
    
    
    public RIVList(SummaryInstance si) {
        this.si = si;
        
        columnNames = si.getSummary().getRecord().getSummaryColumnNames();
        if (columnNames != null) {
            for (Iterator it = columnNames.iterator(); it.hasNext();) {
                String name = (String) it.next();
                logger.finer("I've got a column name of "+name);
                displayNames.put(name, si.getSummary().getRecord().getDisplayName(name));
            }
        } else {
            logger.severe("No summary column information available");
        }
    }

    /**
     * @param value
     */
    public RIVList(ResultList rl) {
        this.rl = rl;
        
        columnNames = new ArrayList();
        Column[] ca = rl.getQuery().getColumns();
        if (ca != null) {
            for (int i=0; i < ca.length; i++) {
                Column c = ca[i];
                columnNames.add(c.getName());
                displayNames.put(c.getName(), c.getDisplayName());
            }
        } else {
            logger.severe("No summary column information available");
        }       
    }

    public Map getDisplayName() {
        return displayNames;
    }
    
    
    public List getColumnNames() {
        return columnNames;
    }
    
    public int getSize() {
        if (si != null) {
            return si.size();
        }
        logger.severe("Called size when si is null");
        return -1;
    }
    
    public boolean hasNext() {
        if (si != null) {
            return summaryInstanceHasNext();
        }
        return resultListHasNext();
    }

    private boolean summaryInstanceHasNext() {
        return si.hasMoreRecordInstances();
    }
    
    private boolean resultListHasNext() {
        rlHasNextCalled = true;
        try {
            return rl.next();
        }
        catch (WdkModelException exp) {
            logger.severe(exp.getMessage());
        }
        return false;
    } 
    
    public Object next() {
        if (si != null) {
            return summaryInstanceNext();
        }
        return resultListNext();
    }
    
    public Object summaryInstanceNext() {
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
    
    public Object resultListNext() {
        if (!rlHasNextCalled) {
            if (!resultListHasNext()) {
                throw new NoSuchElementException();
            }
        } else {
            rlHasNextCalled = false;
        }
        Map row = new HashMap();
        
        Column[] ca = rl.getQuery().getColumns();
        if (ca != null) {
            for (int i=0; i < ca.length; i++) {
                Column c = ca[i];
                try {
                    row.put(c.getName(), rl.getValue(c.getName()));
                }
                catch (WdkModelException exp) {
                    logger.severe(exp.getMessage());
                }
            }
        } else {
            logger.severe("No summary column information available");
        }       
        
        return new RecordInstanceView(row);
    }
    
    public void remove() {
        throw new UnsupportedOperationException("remove isn't allowed on this iterator");
    } 

    public String toString() {
        if (si != null) {
            return "I'm an RIVList: si="+si+", size="+getSize();
        }
        return "I'm an RIVList: rl="+rl;
    }
    
    
    public void close() throws WdkModelException {
//        if (si != null) {
//            si.close();
//        }
//        if (rl != null) {
//            rl.close();
//        }
    }
    
}
    
