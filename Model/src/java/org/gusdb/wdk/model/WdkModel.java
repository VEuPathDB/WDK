package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

public class WdkModel {

    protected RDBMSPlatformI platform;

    HashMap querySets = new HashMap();
    HashMap recordSets = new HashMap();
    HashMap referenceLists = new HashMap();
    HashMap summarySets = new HashMap();
    HashMap allModelSets = new HashMap();
    String name;
    ResultFactory resultFactory;

    public WdkModel() {
    }

    public ResultFactory getResultFactory() {
	return resultFactory;
    }

    public void setName(String name) {
	this.name = name;
    }

    //Record Sets
    public void addRecordSet(RecordSet recordSet) throws WdkModelException {
	addSet(recordSet, recordSets);
    }

    public RecordSet getRecordSet(String recordSetName) throws WdkModelException {
	
	if (!recordSets.containsKey(recordSetName)) {
	    String err = "WDK Model " + name +
		" does not contain a record set with name " + recordSetName;
	    
	    throw new WdkModelException(err);
	}
	return (RecordSet)recordSets.get(recordSetName);
    }


    //Query Sets
    public void addQuerySet(QuerySet querySet) throws WdkModelException {
	addSet(querySet, querySets);
    }
    
    private void addSet(ModelSetI set, HashMap setMap) throws WdkModelException {
	String setName = set.getName();
	if (allModelSets.containsKey(setName)) {
	   String err = "WDK Model " + name +
		" already contains a set with name " + setName;
	
	    throw new WdkModelException(err);	
	}
	setMap.put(setName, set);
	allModelSets.put(setName, set);
    }

    public QuerySet getQuerySet(String setName) throws WdkModelException {
	if (!querySets.containsKey(setName)) {
	    String err = "WDK Model " + name +
		" does not contain a query set with name " + setName;
	    throw new WdkModelException(err);
	}
	return (QuerySet)querySets.get(setName);
    }

    public boolean hasQuerySet(String setName) {
	return querySets.containsKey(setName);
    }


    //Summary Sets
    public void addSummarySet(SummarySet summarySet) throws WdkModelException {
	addSet(summarySet, summarySets);
    }

    public SummarySet getSummarySet(String setName) throws WdkModelException {
	if (!summarySets.containsKey(setName)) {
	    String err = "WDK Model " + name +
		" does not contain a Summary set with name " + setName;
	    throw new WdkModelException(err);
	}
	return (SummarySet)summarySets.get(setName);
    }

    public boolean hasSummarySet(String setName) {
	return summarySets.containsKey(setName);
    }


    //ReferenceLists
    public void addReferenceList(ReferenceList referenceList) throws WdkModelException {
	
	if (referenceLists.containsKey(referenceList.getName())){
	    String err = "WDK Model " + name + "already conatins a ReferenceList with name " +
		referenceList.getName();
	    throw new WdkModelException(err);
	}
	referenceLists.put(referenceList.getName(), referenceList);
    }
    
    public ReferenceList getReferenceList(String referenceListName) throws WdkModelException {
	
	if (!referenceLists.containsKey(referenceListName)){
	    String err = "WDK Model " + name +
		" does not contain a  query set with name " + referenceListName;
	    throw new WdkModelException(err);
	}
	return (ReferenceList)referenceLists.get(referenceListName);
    }

    public ReferenceList[] getAllReferenceLists(){

	ReferenceList lists[] = new ReferenceList[referenceLists.size()];
	Iterator keys = referenceLists.keySet().iterator();
	int counter = 0;
	while (keys.hasNext()){
	    String name = (String)keys.next();
	    ReferenceList nextReferenceList = (ReferenceList)referenceLists.get(name);
	    lists[counter] = nextReferenceList;
	    counter++;
	}
	return lists;
    }
    
    /**
     * Set whatever resources the model needs.  It will pass them to its kids
     */
    public void setResources(ResultFactory rf, RDBMSPlatformI platform) throws WdkModelException {

	this.platform = platform;
	this.resultFactory = rf;

	Iterator modelSets = allModelSets.values().iterator();
	while (modelSets.hasNext()) {
	    ModelSetI modelSet = (ModelSetI)modelSets.next();
	    modelSet.setResources(this);
	}
    }

    public RDBMSPlatformI getRDBMSPlatform() {
	return platform;
    }

    public Object resolveReference(String twoPartName, String refererName, String refererClassName, String refererAttributeName) throws WdkModelException {
	String s = "Invalid reference in " + refererClassName + " '" + refererName + "' at " + refererAttributeName + "=\"" + twoPartName + "\".";

	//ensures <code>twoPartName</code> is formatted correctly
	Reference reference = new Reference(twoPartName);

	String setName = reference.getSetName();
	String elementName = reference.getElementName();

	ModelSetI set = (ModelSetI)allModelSets.get(setName);
	if (set == null) {
	    String s3 = s + " There is no set called '" + setName + "'";
	    throw new WdkModelException(s3);
	}
	Object element = set.getElement(elementName);
	if (element == null) {
	    String s4 = s + " Set '" + setName + 
		"' does not have an element called '" + elementName + "'";
	    throw new WdkModelException(s4);
	}
	return element;
    }

    /**
     * Some elements within the set may refer to others by name.  Resolve those
     * references into real object references.
     */ 
    public void resolveReferences() throws WdkModelException {
       
       Iterator modelSets = allModelSets.values().iterator();
       while (modelSets.hasNext()) {
	   ModelSetI modelSet = (ModelSetI)modelSets.next();
	   modelSet.resolveReferences(this);
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
       buf.append(newline);

       buf.append( "--- Summary Sets---" );
       buf.append( newline );
       Iterator summarySetIterator = summarySets.values().iterator();
       while (summarySetIterator.hasNext()) {
	   buf.append( summarySetIterator.next() ).append( newline );
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

    void checkName(String setName) throws WdkModelException {
    }
}

