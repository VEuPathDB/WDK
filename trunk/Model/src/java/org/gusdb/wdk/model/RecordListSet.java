package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * RecordListSet.java
 *
 * Created: Fri June 4 15:05:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */


public class RecordListSet {

    HashMap recordListSet;
    String name;

    public RecordListSet() {
	recordListSet = new HashMap();
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public RecordList getRecordList(String name) {
	return (RecordList)recordListSet.get(name);
    }

    public RecordList[] getRecordLists() {
	RecordList[] recordLists = new RecordList[recordListSet.size()];
	Iterator recordListIterator = recordListSet.values().iterator();
	int i = 0;
	while (recordListIterator.hasNext()) {
	    recordLists[i++] = (RecordList)recordListIterator.next();
	}
	return recordLists;
    }

    public void addRecordList(RecordList recordList) {
	if (recordListSet.get(recordList.getName()) != null) 
	    throw new IllegalArgumentException("RecordList named " 
					       + recordList.getName() 
					       + " already exists in recordList set "
					       + getName());
	
	recordListSet.put(recordList.getName(), recordList);
    }

    public void resolveReferences(Map queryMap, Map recordMap) throws WdkModelException{
	
	Iterator recordListIterator = recordListSet.values().iterator();
	while (recordListIterator.hasNext()){
	    RecordList nextList = (RecordList)recordListIterator.next();
	    nextList.resolveReferences(queryMap, recordMap);
	}
    }

}
