package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class RecordSet {

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

    public Record getRecord(String name) {
	return (Record)recordSet.get(name);
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
       buf.append( "--- Records ---" );
       buf.append( newline );
       Iterator recordIterator = recordSet.values().iterator();
       while (recordIterator.hasNext()) {
	   buf.append(recordIterator.next()).append( newline );
       }

       return buf.toString();
    }

    public void resolveReferences(Map querySetMap) throws WdkModelException {
       Iterator recordIterator = recordSet.values().iterator();
       while (recordIterator.hasNext()) {
	   Record record = (Record)recordIterator.next();
	   record.resolveReferences(querySetMap);
       }
    }
    
    public static Record resolveRecordReference(Map recordSetMap, String twoPartRecordName) throws WdkModelException {

	//change to RecordName eventually
	
	String[] parts = twoPartRecordName.split("\\.");
	String recordSetName = parts[0];
	String recordName = parts[1];

	RecordSet rs = (RecordSet)recordSetMap.get(recordSetName);
	if (rs == null) {
	    //maybe change to mirror SimpleQuerySet error messaging?
	    throw new WdkModelException ("Could not find RecordSet " + recordSetName);
	}
	Record record = (Record)rs.getRecord(recordName);

	if (record == null) {

	    throw new WdkModelException ("Could not find Record " + recordName);
	}
	return record;
    }
}
