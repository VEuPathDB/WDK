package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

public class WdkModel {

    HashMap querySets = new HashMap();
    HashMap recordSets = new HashMap();
    HashMap queryNameLists = new HashMap();
    HashMap recordListSets = new HashMap();
    String name;
    ResultFactory resultFactory;

    public WdkModel() {
	this.resultFactory = new ResultFactory();
    }

    public ResultFactory getResultFactory() {
	return resultFactory;
    }

    public void setName(String name) {
	this.name = name;
    }

    //Record Sets
    public void addRecordSet(RecordSet recordSet) {
	if (recordSets.containsKey(recordSet.getName())) {
	    String err = "WDK Model " + name +
		" already contains a query set with name " + 
		recordSet.getName();
	    throw new IllegalArgumentException(err);	
	}
	recordSets.put(recordSet.getName(), recordSet);
    }

    public RecordSet getRecordSet(String recordSetName) {
	
	if (!recordSets.containsKey(recordSetName)) {
	    String err = "WDK Model " + name +
		" does not contain a record set with name " + recordSetName;
	    
	    throw new IllegalArgumentException(err);
	}
	return (RecordSet)recordSets.get(recordSetName);
    }


    //Query Sets
    public void addQuerySet(QuerySet querySet) {
	String err = checkName(querySet.getName());
	if (err != null) throw new IllegalArgumentException(err);
	querySet.setResultFactory(resultFactory);
	querySets.put(querySet.getName(), querySet);
    }

    public QuerySet getQuerySet(String setName) {
	if (!querySets.containsKey(setName)) {
	    String err = "WDK Model " + name +
		" does not contain a query set with name " + setName;
	    throw new IllegalArgumentException(err);
	}
	return (QuerySet)querySets.get(setName);
    }

    public boolean hasQuerySet(String setName) {
	return querySets.containsKey(setName);
    }


    //RecordList Sets
    public void addRecordListSet(RecordListSet recordListSet) {
	if (recordListSets.containsKey(recordListSet.getName())){
	    String err = "WDK Model " + name + "already conatins a RecordList Set with name " +
		recordListSet.getName();
	    throw new IllegalArgumentException(err);
	}
	recordListSets.put(recordListSet.getName(), recordListSet);
    }

    public RecordListSet getRecordListSet(String setName) {
	if (!recordListSets.containsKey(setName)) {
	    String err = "WDK Model " + name +
		" does not contain a RecordList set with name " + setName;
	    throw new IllegalArgumentException(err);
	}
	return (RecordListSet)recordListSets.get(setName);
    }

    public boolean hasRecordListSet(String setName) {
	return recordListSets.containsKey(setName);
    }


    //QueryNameLists
    public void addQueryNameList(QueryNameList queryNameList){
	
	if (queryNameLists.containsKey(queryNameList.getName())){
	    String err = "WDK Model " + name + "already conatins a QueryNameList with name " +
		queryNameList.getName();
	    throw new IllegalArgumentException(err);
	}
	queryNameLists.put(queryNameList.getName(), queryNameList);
    }
    
    public QueryNameList getQueryNameList(String queryNameListName){
	
	if (!queryNameLists.containsKey(queryNameListName)){
	    String err = "WDK Model " + name +
		" does not contain a  query set with name " + queryNameListName;
	    throw new IllegalArgumentException(err);
	}
	return (QueryNameList)queryNameLists.get(queryNameListName);
    }

    public QueryNameList[] getAllQueryNameLists(){

	QueryNameList lists[] = new QueryNameList[queryNameLists.size()];
	Iterator keys = queryNameLists.keySet().iterator();
	int counter = 0;
	while (keys.hasNext()){
	    String name = (String)keys.next();
	    QueryNameList nextQueryNameList = (QueryNameList)queryNameLists.get(name);
	    lists[counter] = nextQueryNameList;
	    counter++;
	}
	return lists;
    }


    /**
     * Some elements within the set may refer to others by name.  Resolve those
     * references into real object references.
     */ 
    public void resolveReferences() throws Exception {
       
       Iterator recordSetIterator = recordSets.values().iterator();
       while (recordSetIterator.hasNext()) {
	   RecordSet recordSet = (RecordSet)recordSetIterator.next();
	   recordSet.resolveReferences(querySets);
       }
       
       Iterator queryNameListIterator = queryNameLists.values().iterator();
       while (queryNameListIterator.hasNext()){
	   QueryNameList nextList = (QueryNameList)queryNameListIterator.next();
	   //	   nextList.checkReferences(querySets, pageableQuerySets);
	   //need to change checkReferences to not use pageable query sets before we can run this again
       }
       
       Iterator recordListSetIterator = recordListSets.values().iterator();
       while (recordListSetIterator.hasNext()){
	   RecordListSet nextRecordListSet = (RecordListSet)recordListSetIterator.next();
	   nextRecordListSet.resolveReferences(querySets, recordSets);
       }

    }
    
    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("WdkModel: name='" + name 
					   + "'");

       buf.append( newline );
       buf.append( "--- Query Sets---" );
       buf.append( newline );
       Iterator querySetIterator = querySets.values().iterator();
       while (querySetIterator.hasNext()) {
	   buf.append( querySetIterator.next() ).append( newline );
       }
       buf.append(newline);
       
       buf.append( "--- Record Sets---" );
       buf.append( newline );
       Iterator recordSetIterator = recordSets.values().iterator();
       while (recordSetIterator.hasNext()) {
	   buf.append( recordSetIterator.next() ).append( newline );
       }

       return buf.toString();
    }

    /*
    public void addParamSet(ParamSet paramSet) {
    }
    */

    ///////////////////////////////////////////////////////////////////
    ///////   Protected methods
    ///////////////////////////////////////////////////////////////////
    /*
     * @return error message or null if ok
     */
    String checkName(String setName) {
	String err = null;
	if (querySets.containsKey(setName)) {
	    err = "WDK Model " + name +
		" already contains a query set with name " + setName;
	}

	return err;
    }
}

