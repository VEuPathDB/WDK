package org.gusdb.gus.wdk.view;

import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.util.FixableSmallMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class RecordInstanceView implements Map {

    private static final Logger logger = Logger.getLogger("org.gusdb.gus.wdk.view.RecordInstanceView");
    
    private RecordInstance ri;
    private Map map;
    private List attributeNames;

    public RecordInstanceView(RecordInstance ri) {
        // TODO Fix exception handling once exceptions and logging are pinned down
        // TODO Special case handling for "overview" - Is there a better way
        this.ri = ri;
        
        generateAttributeNames();
 
        map = new FixableSmallMap();
        for (Iterator it = attributeNames.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            if (!"overview".equals(key)) {
                Object value = null;
                try {
                    value = ri.getAttributeValue(key);
                }
                catch (WdkModelException exp) {
                    exp.printStackTrace();
                }
                if ("primary_key".equals(key)) {
                    value = new PrimaryKey(value);
                }
                map.put(key, value);
            }
        }
    }
    

    
    private void generateAttributeNames() {
//        if (summary) {
//            //attributeNames = ri.getSummaryAttributes();
//        } else {
            List tempNames = new ArrayList();
            Record record = ri.getRecord();
            logger.severe("Going off to fetch record names");
            tempNames.addAll(record.getNonTextAttributeNames());
            // FIXME Disabling text attributes for now
            //tempNames.addAll(record.getTextAttributeNames());
            tempNames.addAll(record.getTableNames());
            attributeNames = tempNames;

            
//        }
//        
//        // FIXME Just waiting for method on model
//        if (summary) {
//            List tempNames = new ArrayList();
//            Record record = ri.getRecord();
//            addArrayContentsToList(tempNames, record.getNonTextAttributeNames());
//            addArrayContentsToList(tempNames, record.getTextAttributeNames());
//            attributeNames = tempNames;
//        }
        
        attributeNames = Collections.unmodifiableList(attributeNames);  
    }
    
    public List getAttributeNames() {
        return attributeNames;
    }
    
    
//    private void addListContentsToList(List l, List a) {
//        l.addAll(a);
//    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return map.get(key);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        return map.remove(key);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        map.putAll(t);
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        map.clear();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return map.keySet();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        return map.values();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return map.entrySet();
    }
 
}
    
