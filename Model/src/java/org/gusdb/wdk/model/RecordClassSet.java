package org.gusdb.wdk.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecordClassSet implements ModelSetI {

    Map<String, RecordClass> recordClassSet;
    String name;

    public RecordClassSet() {
	recordClassSet = new LinkedHashMap<String, RecordClass>();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }


    public RecordClass getRecordClass(String name) throws WdkModelException {
        RecordClass s = recordClassSet.get(name);
        if (s == null)
            throw new WdkModelException("RecordClass Set " + getName()
                    + " does not include recordClass " + name);
        return s;
    }

    public Object getElement(String name) {
	return recordClassSet.get(name);
    }

    public RecordClass[] getRecordClasses() {
        RecordClass[] recordClasses = new RecordClass[recordClassSet.size()];
        recordClassSet.values().toArray(recordClasses);
        return recordClasses;
    }

    boolean hasRecordClass(RecordClass recordClass){
	return recordClassSet.containsKey(recordClass.getName());
    }

    public void addRecordClass(RecordClass recordClass) throws WdkModelException {
        if (recordClassSet.containsKey(recordClass.getName())) 
            throw new WdkModelException("RecordClass named " 
                    + recordClass.getName() 
                    + " already exists in recordClass set "
                    + getName());
        recordClassSet.put(recordClass.getName(), recordClass);
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("RecordClassSet: name='" + name 
					   + "'");
       buf.append( newline );
       Iterator<RecordClass> recordClassIterator = recordClassSet.values().iterator();
       while (recordClassIterator.hasNext()) {
	    buf.append( newline );
	    buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
	    buf.append( newline );
	   buf.append(recordClassIterator.next()).append( newline );
       }

       return buf.toString();
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
        Iterator<RecordClass> recordClassIterator = recordClassSet.values().iterator();
        while (recordClassIterator.hasNext()) {
            RecordClass recordClass = recordClassIterator.next();
            recordClass.resolveReferences(model);
        }
    }

    public void setResources(WdkModel model) throws WdkModelException {
        Iterator<RecordClass> recordClassIterator = recordClassSet.values().iterator();
        while (recordClassIterator.hasNext()) {
            RecordClass recordClass = recordClassIterator.next();
            recordClass.setFullName(this.getName());
        }
    }
}
