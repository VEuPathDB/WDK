package org.gusdb.gus.wdk.view;

import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



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

    private RecordInstance ri;
    private HashMap map;



    public RecordInstanceView(RecordInstance ri) {
        // TODO Fix exception handling once exceptions and logging are pinned down
        // TODO Special case handling for "overview" - Is there a better way
        this.ri = ri;
        List fieldNames = new ArrayList();
        Record record = ri.getRecord();
        addArrayContentsToList(fieldNames, record.getNonTextFieldNames());
        addArrayContentsToList(fieldNames, record.getTextFieldNames());
        map = new HashMap();
        for (Iterator it = fieldNames.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            if (!"overview".equals(key)) {
                Object value = null;
                try {
                    value = ri.getFieldValue(key);
                } catch (WdkModelException exp) {
                    exp.printStackTrace();
                }
                map.put(key, value);
            }
        }
    } 
    
    private void addArrayContentsToList(List l, String[] a) {
        for (int i = 0; i < a.length; i++) {
            l.add(a[i]);
        } 
    }

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
    
