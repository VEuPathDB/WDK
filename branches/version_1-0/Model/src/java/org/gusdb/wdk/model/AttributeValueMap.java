package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Iterator;

import java.util.logging.Logger;

public class AttributeValueMap implements Map {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.view.AttributeValueMap");
    
    private Record record;
    private RecordInstance recordInstance;
    private boolean isTableMap;

    /**
     * @param recordInstance May be null to indicate this is a map to hold
     * valueless attributes
     */
    public AttributeValueMap(Record record, RecordInstance recordInstance, boolean isTableMap) {
	this.recordInstance = recordInstance;
	this.record = record;
	this.isTableMap = isTableMap;
    }

       
    ////////////////////////////////////////////////////////////////////
    //  implementation of Map
    ////////////////////////////////////////////////////////////////////

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return keySet().size();
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
	return keySet().contains(key);
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
	return isTableMap?
	    record.getTableNames() :
	    record.getAllAttributeNames();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
	if (!containsKey(key)) throw new IllegalArgumentException("Record " + record.getFullName() + " does not have any value with key " + key);

	try {
	    String attrName = (String)key;
	    Object value =  null;
	    if (recordInstance != null) {
		value =  isTableMap?
		    recordInstance.getTableValue(attrName) :
		    recordInstance.getAttributeValue(attrName);
	    }
	    AttributeValue attrValue = 
		new AttributeValue(record, attrName,value);
	    return attrValue;
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
    
