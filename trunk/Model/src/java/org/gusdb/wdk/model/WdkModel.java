package org.gusdb.gus.wdk.model;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;

public class WdkModel {

    protected RDBMSPlatformI platform;

    HashMap querySets = new HashMap();
    HashMap paramSets = new HashMap();
    HashMap recordSets = new HashMap();
    HashMap referenceLists = new HashMap();
    HashMap summarySets = new HashMap();
    HashMap allModelSets = new HashMap();
    String name;
    ResultFactory resultFactory;
    private Document document;

    
    /**
     * @param initRecordList
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public Summary getSummary(String initRecordList) {
        try {
            Reference r = new Reference(initRecordList);
            SummarySet ss = getSummarySet(r.getSetName());
            return ss.getSummary(r.getElementName());
        }
        catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
        catch (WdkUserException exp) {
            throw new RuntimeException(exp);
        }
    }

    
    public Record getRecord(String recordReference) {
        try {
            Reference r = new Reference(recordReference);
            RecordSet rs = getRecordSet(r.getSetName());
            return rs.getRecord(r.getElementName());
        }
        catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
        catch (WdkUserException exp) {
            throw new RuntimeException(exp);
        }
    }
    
    
    public static final WdkModel INSTANCE = new WdkModel();

    public ResultFactory getResultFactory() {
        return resultFactory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
	return name;
    }

    //Record Sets
    public void addRecordSet(RecordSet recordSet) throws WdkModelException {
        addSet(recordSet, recordSets);
    }

    public RecordSet getRecordSet(String recordSetName) throws WdkUserException {
	
        if (!recordSets.containsKey(recordSetName)) {   
            String err = "WDK Model " + name +
            " does not contain a record set with name " + recordSetName;
	    
            throw new WdkUserException(err);
        }
        return (RecordSet)recordSets.get(recordSetName);
    }

    public RecordSet[] getAllRecordSets(){
	    
        RecordSet sets[] = new RecordSet[recordSets.size()];
        Iterator keys = recordSets.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()){
            String name = (String)keys.next();
            RecordSet nextRecordSet = (RecordSet)recordSets.get(name);
            sets[counter] = nextRecordSet;
            counter++;
        }
        return sets;
    }

    //Query Sets
    public void addQuerySet(QuerySet querySet) throws WdkModelException {
        addSet(querySet, querySets);
    }

    public QuerySet getQuerySet(String setName) throws WdkUserException {
        if (!querySets.containsKey(setName)) {
            String err = "WDK Model " + name +
            " does not contain a query set with name " + setName;
            throw new WdkUserException(err);
        }
        return (QuerySet)querySets.get(setName);
    }

    public boolean hasQuerySet(String setName) {
        return querySets.containsKey(setName);
    }

    public QuerySet[] getAllQuerySets(){
	    
        QuerySet sets[] = new QuerySet[querySets.size()];
        Iterator keys = querySets.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()){
            String name = (String)keys.next();
            QuerySet nextQuerySet = (QuerySet)querySets.get(name);
            sets[counter] = nextQuerySet;
            counter++;
        }
        return sets;
    }

    //Summary Sets
    public void addSummarySet(SummarySet summarySet) throws WdkModelException {
        addSet(summarySet, summarySets);
    }

    public SummarySet getSummarySet(String setName) throws WdkUserException {
        if (!summarySets.containsKey(setName)) {
            String err = "WDK Model " + name +
            " does not contain a Summary set with name " + setName;
            throw new WdkUserException(err);
        }
        return (SummarySet)summarySets.get(setName);
    }

    public boolean hasSummarySet(String setName) {
        return summarySets.containsKey(setName);
    }

    public SummarySet[] getAllSummarySets(){
	    
        SummarySet sets[] = new SummarySet[summarySets.size()];
        Iterator keys = summarySets.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()){
            String name = (String)keys.next();
            SummarySet nextSummarySet = (SummarySet)summarySets.get(name);
            sets[counter] = nextSummarySet;
            counter++;
        }
        return sets;
    }

    //ReferenceLists
    public void addReferenceList(ReferenceList referenceList) throws WdkModelException {
        addSet(referenceList, referenceLists);
    }
    
    public ReferenceList getReferenceList(String referenceListName) throws WdkUserException {
	
        if (!referenceLists.containsKey(referenceListName)){
            String err = "WDK Model " + name +
            " does not contain a  query set with name " + referenceListName;
            throw new WdkUserException(err);
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
    
    //ModelSetI's
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

    public Object resolveReference(String twoPartName, String refererName, 
                String refererClassName, String refererAttributeName) throws WdkModelException {
        String s = "Invalid reference in " + refererClassName + " '" + refererName 
            + "' at " + refererAttributeName + "=\"" + twoPartName + "\".";

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
            "' returned null for '" + elementName + "'";
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
            ModelSetI modelSet = (ModelSetI) modelSets.next();
            modelSet.resolveReferences(this);
        }
    }
    
    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("WdkModel: name='" + name 
					   + "'");
       buf.append(showSet("Param", paramSets));
       buf.append(showSet("Query", querySets));
       buf.append(showSet("Record", recordSets));
       buf.append(showSet("Summary", summarySets));
       return buf.toString();
    }
       
    protected String showSet(String setType, HashMap setMap) {
        StringBuffer buf = new StringBuffer();
        String newline = System.getProperty("line.separator");
        buf.append( newline );
        buf.append( "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" + newline );
        buf.append( "ooooooooooooooooooooooooooooo " + setType + " Sets oooooooooooooooooooooooooo" + newline );
        buf.append( "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" + newline + newline);
        Iterator setIterator = setMap.values().iterator();
        while (setIterator.hasNext()) {
            ModelSetI set = (ModelSetI)setIterator.next();
            buf.append( "=========================== " + set.getName()+ " ===============================" + newline + newline);
            buf.append(set).append( newline );
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
 
    //Param Sets
    public void addQuerySet(ParamSet paramSet) throws WdkModelException {
        addSet(paramSet, paramSets);
    }
    
    public void addParamSet(ParamSet paramSet) throws WdkModelException {
        addSet(paramSet, paramSets);
    }

    ///////////////////////////////////////////////////////////////////
    ///////   Protected methods
    ///////////////////////////////////////////////////////////////////

    void checkName(String setName) throws WdkModelException {
        // TODO What's supposed to be here?
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
}

