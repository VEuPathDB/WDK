package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

public class RecordSet implements ModelSetI {

    HashMap recordSet;
    String name;

    public RecordSet() {
	recordSet = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }


    public Record getRecord(String name) throws WdkUserException {

	Record s = (Record)recordSet.get(name);
	if (s == null) throw new WdkUserException("Record Set " + getName() + " does not include record " + name);
	return s;
    }

    public Object getElement(String name) {
	return recordSet.get(name);
    }

    public Record[] getRecords() {
	Record[] records = new Record[recordSet.size()];
	Iterator recordIterator = recordSet.values().iterator();
	int i = 0;
	while (recordIterator.hasNext()) {
	    records[i++] = (Record)recordIterator.next();
	}
	return records;
    }

    public void addRecord(Record record) throws WdkModelException {
        if (recordSet.get(record.getName()) != null) 
            throw new WdkModelException("Record named " 
                    + record.getName() 
                    + " already exists in record set "
                    + getName());
        recordSet.put(record.getName(), record);
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("RecordSet: name='" + name 
					   + "'");
       buf.append( newline );
       Iterator recordIterator = recordSet.values().iterator();
       while (recordIterator.hasNext()) {
	    buf.append( newline );
	    buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
	    buf.append( newline );
	   buf.append(recordIterator.next()).append( newline );
       }

       return buf.toString();
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
       Iterator recordIterator = recordSet.values().iterator();
       while (recordIterator.hasNext()) {
	   Record record = (Record)recordIterator.next();
	   record.resolveReferences(model);
       }
    }

    public void setResources(WdkModel model) throws WdkModelException {
       Iterator recordIterator = recordSet.values().iterator();
       while (recordIterator.hasNext()) {
	   Record record = (Record)recordIterator.next();
	   record.setRecordSet(this);
       }
    }

}
