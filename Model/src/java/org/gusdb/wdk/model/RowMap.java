package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Iterator;

import java.util.logging.Logger;

public class RowMap implements Map {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.view.RowMap");
    
    private ResultList resultList;
    private Set columnNameSet;

    public RowMap(ResultList resultList) {
	this.resultList = resultList;
	columnNameSet = new LinkedHashSet();
	Column[] columns =  resultList.getColumns();
	for (int i=0; i<columns.length; i++) {
	    columnNameSet.add(columns[i].getName());
	}
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return resultList.getColumns().length;
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
	return columnNameSet.contains(key);
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
	return columnNameSet;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {

	if (!containsKey(key)) throw new IllegalArgumentException("Row does not have any value with key " + key);

	try {
	    return resultList.getValue((String)key);
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
	throw new UnsupportedOperationException("Illegal operation 'containsValue' on RecordINstance");
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
	throw new UnsupportedOperationException("Illegal operation 'put' on RecordINstance");
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
	throw new UnsupportedOperationException("Illegal operation 'remove' on RecordINstance");
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
	throw new UnsupportedOperationException("Illegal operation 'putAll' on RecordINstance");
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
	throw new UnsupportedOperationException("Illegal operation 'values' on RecordINstance");
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
	LinkedHashSet entrySet = new LinkedHashSet();
	Iterator iterator = keySet().iterator();
	while (iterator.hasNext()) {
	    Object key = iterator.next();
	    entrySet.add(new OurEntry(key, get(key)));
	}
	return entrySet;
    }
    
    public class OurEntry implements Map.Entry {
	Object key;
	Object value;

	OurEntry(Object key, Object value) {
	    this.key = key;
	    this.value = value;
	}
	public Object getKey() {
	    return key;
	}
	public Object getValue() {
	    return value;
	}
	public Object setValue(Object value) {
	    this.value = value;
	    return value;
	}
    }
 
}
