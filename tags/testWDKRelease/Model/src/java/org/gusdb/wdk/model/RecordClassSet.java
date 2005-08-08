package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

public class RecordClassSet implements ModelSetI {

    HashMap recordClassSet;
    String name;

    public RecordClassSet() {
	recordClassSet = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }


    public RecordClass getRecordClass(String name) throws WdkUserException {

	RecordClass s = (RecordClass)recordClassSet.get(name);
	if (s == null) throw new WdkUserException("RecordClass Set " + getName() + " does not include recordClass " + name);
	return s;
    }

    public Object getElement(String name) {
	return recordClassSet.get(name);
    }

    public RecordClass[] getRecordClasses() {
	RecordClass[] recordClasses = new RecordClass[recordClassSet.size()];
	Iterator recordClassIterator = recordClassSet.values().iterator();
	int i = 0;
	while (recordClassIterator.hasNext()) {
	    recordClasses[i++] = (RecordClass)recordClassIterator.next();
	}
	return recordClasses;
    }

    boolean hasRecordClass(RecordClass recordClass){
	return recordClassSet.containsKey(recordClass.getName());
    }

    public void addRecordClass(RecordClass recordClass) throws WdkModelException {
        if (recordClassSet.get(recordClass.getName()) != null) 
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
       Iterator recordClassIterator = recordClassSet.values().iterator();
       while (recordClassIterator.hasNext()) {
	    buf.append( newline );
	    buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
	    buf.append( newline );
	   buf.append(recordClassIterator.next()).append( newline );
       }

       return buf.toString();
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
       Iterator recordClassIterator = recordClassSet.values().iterator();
       while (recordClassIterator.hasNext()) {
	   RecordClass recordClass = (RecordClass)recordClassIterator.next();
	   recordClass.resolveReferences(model);
       }
    }
    
    public void setResources(WdkModel model) throws WdkModelException {
	Iterator recordClassIterator = recordClassSet.values().iterator();
	while (recordClassIterator.hasNext()) {
	   RecordClass recordClass = (RecordClass)recordClassIterator.next();
	   recordClass.setFullName(this.getName());
	}
    }
 
	   

}
