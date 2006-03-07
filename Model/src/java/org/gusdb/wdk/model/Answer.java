package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Answer.java
 *
 * Created: Fri June 4 13:01:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

/**
 * A list of RecordInstances representing one page of the answer to a Question.
 * The constructor of the Answer provides a handle (QueryInstance) on 
 * the ResultList that is the list of primary keys for the all the records (not  * just one page) that are the answer to the Question.   The ResultList also 
 * has a column that contains the row number (RESULT_TABLE_I) so that a list of
 * primary keys for a single page can be efficiently accessed.
 * 
 * The Answer is lazy in that it only constructs the set of RecordInstances
 * for the page when the first RecordInstance is requested.  
 *
 * The initial request triggers the creation of skeletal RecordInstances for
 * the page.  They contain only primary keys (these being acquired from the 
 * ResultList).
 * 
 * These skeletal RecordInstances are also lazy in that they only run an
 * attributes query when an attribute provided by that query is requested.
 * When they do run an attribute query, its QueryInstance is put into joinMode.
 * This means that the attribute query joins with the table containing
 * the primary keys, and, in one database query, generates rows containing
 * the attribute values for all the RecordInstances in the page.
 * 
 * The method <code>integrateAttributesQueryResult</> is invoked by the
 * first RecordInstance in the page upon the first request for an attribute 
 * provided by an attributes query. The query is a join with the list of 
 * primary keys, and so has a row for each RecordInstance in the page, and
 * columns that provide the attribute values (plus RESULT_TABLE_I).  The 
 * values in the rows are integrated into the corresponding RecordInstance 
 * (now no longer skeletal).  <code>integrateAttributesQueryResult</> may
 * be called a number of times, depending upon how many attribute queries
 * the record class contains.
 * 
 * Attribute queries are guaranteed to provide one row for each RecordInstance
 * in the page.  An exception is thrown otherwise.
 *
 */
public class Answer {

    private static final Logger logger = Logger.getLogger(Answer.class);
    
    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    private Question question;

    private QueryInstance idsQueryInstance;

    private QueryInstance attributesQueryInstance;

    private RecordInstance[] pageRecordInstances;

    int startRecordInstanceI;

    int endRecordInstanceI;

    private int recordInstanceCursor;

    private String recordIdColumnName;
    
    private String recordProjectColumnName;

    private boolean isBoolean = false;

    private Integer resultSize;   // size of total result

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * @param question The <code>Question</code> to which this is the <code>Answer</code>.
     * @param idsQueryInstance  The <code>QueryInstance</code> that provides a handle on the ResultList containing all primary keys that are the result for the
     * question (not just one page worth).
     * @param startRecordInstanceI The index of the first <code>RecordInstance</code> in the page. (>=1)
     * @param endRecordInstanceI The index of the last <code>RecordInstance</code> in the page, inclusive.
     */
    Answer(Question question, QueryInstance idsQueryInstance, int startRecordInstanceI, int endRecordInstanceI) throws WdkUserException, WdkModelException{
	this.question = question;
	this.idsQueryInstance = idsQueryInstance;
	this.isBoolean = 
	    idsQueryInstance instanceof org.gusdb.wdk.model.BooleanQueryInstance;
	this.recordInstanceCursor = 0;
	this.startRecordInstanceI = startRecordInstanceI;
	this.endRecordInstanceI = endRecordInstanceI;   

	/*
	ResultList rl = 
	    idsQueryInstance.getPersistentResultPage(startRecordInstanceI, 
						     endRecordInstanceI);
	rl.close(); // rl only needed to close connection
	*/
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    /**
     * provide property that user's term for question
     */
    public Question getQuestion(){
	return this.question;
    }

    public int getPageSize(){
	return pageRecordInstances == null? 0 : pageRecordInstances.length;
    }
    
    public int getResultSize() throws WdkModelException{

	if (resultSize == null) {
	    ResultList rl = idsQueryInstance.getResult();
	    int counter = 0;
	    while (rl.next()){
		counter++;
	    }
	    rl.close();
	    resultSize = new Integer(counter);
	}
	return resultSize.intValue();
    }

    public boolean isDynamic() {
	return getQuestion().isDynamic();
    }

    /**
     * @return Map where key is param name and value is param value
     */
    public Map<String, Object> getParams() {
	return idsQueryInstance.getValuesMap();
    }

    /**
     * @return Map where key is param display name and value is param value
     */
    public Map<String, Object> getDisplayParams() {
	Map<String, Object> displayParamsMap = new LinkedHashMap<String, Object>();
	Map<String, Object> paramsMap = getParams();
	Param[] params = question.getParams();
	for (int i=0; i<params.length; i++) {
	    Param param = params[i];
	    displayParamsMap.put(param.getPrompt(), 
				 paramsMap.get(param.getName()));
	}
	return displayParamsMap;
    }


    public boolean getIsBoolean(){
	return this.isBoolean;
    }

    // this method is wrong.  it should be plural, and return
    // all the attributes query instances.  this returns only the last
    // one made, which is bogus.  it is used by wdkSummary --showQuery
    // which itself should be --showQueries
    public QueryInstance getAttributesQueryInstance() {
	return attributesQueryInstance;
    }

    public QueryInstance getIdsQueryInstance(){
	return idsQueryInstance;
    }

    public Map<String , AttributeField> getAttributeFields() {
	return question.getAttributeFields();
    }
    
    public Map<String, AttributeField> getReportMakerAttributeFields() {
        return question.getReportMakerAttributeFields();
    }

    public boolean isSummaryAttribute(String attName){
	return question.isSummaryAttribute(attName);
    }
    
    //Returns null if we have already returned the last instance
    public RecordInstance getNextRecordInstance() throws WdkModelException{
	initPageRecordInstances();

	RecordInstance nextInstance = null;
	if (recordInstanceCursor < pageRecordInstances.length){
	    nextInstance = pageRecordInstances[recordInstanceCursor];
	    recordInstanceCursor++;
	}
	return nextInstance;
    }
    
    public boolean hasMoreRecordInstances() throws WdkModelException 
    {
	initPageRecordInstances();

        if (pageRecordInstances == null){
            logger.warn("pageRecordInstances is still null");
        }
        return recordInstanceCursor < pageRecordInstances.length;
    }

    public void resetRecordInstanceCurser(){
	recordInstanceCursor = 0;
    }

    public Integer getDatasetId() {
	return idsQueryInstance.getQueryInstanceId();
    }

    /////////////////////////////////////////////////////////////////////
    //   print methods
    /////////////////////////////////////////////////////////////////////

    public String printAsRecords() throws WdkModelException, WdkUserException{  
	StringBuffer buf = new StringBuffer();

	initPageRecordInstances();

	for (int i = 0; i < pageRecordInstances.length; i++){
	    buf.append(pageRecordInstances[i].print());
	}
	return buf.toString();
    }
    
    /**
     * print summary attributes, one per line
     * Note: not sure why this is needed
     */
    public String printAsSummary() throws WdkModelException, WdkUserException{
	StringBuffer buf = new StringBuffer();

	initPageRecordInstances();

	for (int i = 0; i < pageRecordInstances.length; i++){
	    buf.append(pageRecordInstances[i].printSummary());
	}
	return buf.toString();
    }

    /**
     * print summary attributes in tab delimited table with header of attr. names
     */
    public String printAsTable () throws WdkModelException, WdkUserException{
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer();

	initPageRecordInstances();
		
	if (pageRecordInstances.length == 0) return buf.toString();

	for (int i = -1; i < pageRecordInstances.length; i++){

	    Iterator attributeNames = 
		question.getSummaryAttributes().keySet().iterator();

        // only print
	    while (attributeNames.hasNext()){
		String nextAttName = (String)attributeNames.next();

		// make header
		if (i == -1) buf.append(nextAttName + "\t");

		// make data row
		else {
		    AttributeField field = getAttributeFields().get(nextAttName);
		    Object value = 
			pageRecordInstances[i].getAttributeValue(field);
		    if (value == null) value = "";
            // only print part of the string
            String str = value.toString().trim();
            if (str.length()>50) str = str.substring(0, 47) + "...";
		    buf.append(str + "\t");
		}
	    }
	    buf.append(newline);
	}

	return buf.toString();
    }

    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    /**
     * Integrate into the page's RecordInstances the attribute 
     * values from a particular attributes query.  The attributes
     * query result includes only rows for this page.
     */
    void integrateAttributesQueryResult(QueryInstance attributesQueryInstance)
            throws WdkModelException {
        // TEST
//        logger.debug("Question is: " + question.hashCode());
//        logger.debug("#Summary Attributes: "
//                + question.getSummaryAttributes().size());

        this.attributesQueryInstance = attributesQueryInstance;

        boolean isDynamic = attributesQueryInstance.getQuery().getParam(
                DynamicAttributeSet.RESULT_TABLE) != null;
        
//        logger.debug("AttributeQuery is: " + attributesQueryInstance.getQuery().getFullName());
//        logger.debug("isDynamic=" + isDynamic);

        String idsTableName = idsQueryInstance.getResultAsTableName();
        attributesQueryInstance.initJoinMode(idsTableName,
                recordProjectColumnName, recordIdColumnName,
                startRecordInstanceI, endRecordInstanceI, isDynamic);

        // Initialize with nulls (handle missing attribute rows)
        Map<PrimaryKeyValue, RecordInstance> recordInstanceMap = 
            new HashMap<PrimaryKeyValue, RecordInstance>();
        for (RecordInstance recordInstance : pageRecordInstances) {
            setColumnValues(recordInstance, attributesQueryInstance, isDynamic,
                    recordIdColumnName, recordProjectColumnName, null);
            PrimaryKeyValue primaryKey = recordInstance.getPrimaryKey();
            recordInstanceMap.put(primaryKey, recordInstance);
        }

	int pageIndex = 0;
	int idsResultTableI = startRecordInstanceI;
	Set<PrimaryKeyValue> primaryKeySet = new HashSet<PrimaryKeyValue>(); 
	ResultList attrQueryResultList = attributesQueryInstance.getResult();
	while (attrQueryResultList.next()){
	    
	    String id = attrQueryResultList.getValue(recordIdColumnName).toString();
	    String project = null;
	    if (recordProjectColumnName != null) {
		project = 
		    attrQueryResultList.getValue(recordProjectColumnName).toString();
	    }

	    PrimaryKeyValue attrPrimaryKey = 
		new PrimaryKeyValue(getQuestion().getRecordClass().getPrimaryKeyField(), project, id.toString());

	    if (primaryKeySet.contains(attrPrimaryKey)) {
		String msg = 
		    "Result Table " + idsTableName + " for Attribute query " +
		    attributesQueryInstance.getQuery().getFullName() + " " +
		    " has more than one row for " + attrPrimaryKey; 
		throw new WdkModelException(msg);
	    } else {
		primaryKeySet.add(attrPrimaryKey);
	    }

	    
	    RecordInstance recordInstance = 
		recordInstanceMap.get(attrPrimaryKey);
	    setColumnValues(recordInstance, attributesQueryInstance,
			    isDynamic, recordIdColumnName,
			    recordProjectColumnName,
			    attrQueryResultList);
        }
	attrQueryResultList.close();
    }

    void setColumnValues(RecordInstance recordInstance,
			 QueryInstance attributesQueryInstance, 
			 boolean isDynamic,
			 String recordIdColumnName,
			 String recordProjectColumnName,
			 ResultList attrQueryResultList) throws WdkModelException {

	Column[] columns = attributesQueryInstance.getQuery().getColumns();

	for (int i = 0; i < columns.length; i++) {
	    String colName = columns[i].getName();
	    if (colName.equalsIgnoreCase(recordIdColumnName)) continue;
	    if (colName.equalsIgnoreCase(recordProjectColumnName)) continue;
	    Object value = null;
	    if (attrQueryResultList != null) 
		value = attrQueryResultList.getValue(colName);
		    
	    if (isDynamic) 
		recordInstance.setAttributeValue(colName, value,
						 attributesQueryInstance.getQuery());
	    else
		recordInstance.setAttributeValue(colName, value);
	}
    }

    String[] findPrimaryKeyColumnNames() {
        String[] names =findPrimaryKeyColumnNames(idsQueryInstance.getQuery());
	recordIdColumnName = names[0];
	recordProjectColumnName = names[1];	
	return names;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    /**
     * If not already initialized, initialize the page's record instances,
     * setting each with its id (either just primary key or that and
     * project, if using a federated data source).
     */
    private void initPageRecordInstances() throws WdkModelException {

	if (pageRecordInstances != null) return;

	// set instance variables projectColumnName and idsColumnName
	findPrimaryKeyColumnNames();
        
	ResultList rl = 
	    idsQueryInstance.getPersistentResultPage(startRecordInstanceI,
						     endRecordInstanceI);
   
	Vector tempRecordInstances = new Vector();

	while (rl.next()){
	    RecordInstance nextRecordInstance = 
		getQuestion().getRecordClass().makeRecordInstance();
	    nextRecordInstance.setDynamicAttributeFields(question.getDynamicAttributeFields());
	    String project = null;
	    if (recordProjectColumnName != null)
		project = rl.getValue(recordProjectColumnName).toString();
	    String id = rl.getValue(recordIdColumnName).toString();
	    nextRecordInstance.setPrimaryKey(project, id);
	    
	    nextRecordInstance.setAnswer(this);
	    tempRecordInstances.add(nextRecordInstance);
	}        
	pageRecordInstances = new RecordInstance[tempRecordInstances.size()];
	tempRecordInstances.copyInto(pageRecordInstances);
	rl.close();
    }

    /**
     * Given a set of columns, find the id and project column names
     * The project column is optional.  
     * Assumption:  the id and project columns are the first two
     * columns, but, they may be (id, project) or (project, id)
     * @return array where first element is pk col name, second is project
    */
    static String[] findPrimaryKeyColumnNames(Query query) {
	Column[] columns = query.getColumns();
	String[] names = new String[2];

	// assume id is in first column and no project column
	names[0] = columns[0].getName();
	names[1] = null;

	// having two columns, one is for Id and one for project
	if (columns.length > 1) {
	    if (columns[0].getName().toUpperCase().indexOf("PROJECT")!= -1) {
		names[0] = columns[1].getName();
		names[1] = columns[0].getName();
	    } else if (columns[1].getName().toUpperCase().indexOf("PROJECT")!= -1){
		names[1] = columns[1].getName();
	    }
	}
	return names;
    }

    /**
     * Initialize a query instance to run an attributes query.
     */
    private void initAttributesQueryInstance(QueryInstance attributesQueryInstance) throws WdkModelException{

    }

    ////////////////////// Deprecated Methods  /////////////////////////////

    /**
     * @deprecated.  See resetRecordInstanceCursor()
     */
    public void resetRecordInstanceCounter(){
	resetRecordInstanceCurser();
    }

    /*
      this method was expecting a query result to skip over certain values of i.  Keep here until we are sure we do not need it.

      
    void setQueryResult(ResultList resultList) throws WdkModelException {

        int tempCounter = 0;
        Query query = resultList.getQuery();
	Column[] columns = query.getColumns();
            
	while (resultList.next()){
            
            RecordInstance nextRecordInstance = pageRecordInstances[tempCounter];
            for (int j = 0; j < columns.length; j++){
                String nextColumnName = columns[j].getName();
		
                Object value = 
		    resultList.getAttributeFieldValue(nextColumnName).getValue();
                nextRecordInstance.setAttributeValue(nextColumnName, value);
            }
            tempCounter++;
        }
    }

    */

    public String toString() {
        StringBuffer sb = new StringBuffer(question.getDisplayName());

        Map params = getParams();

        for (Object key : params.keySet()) {
            sb.append(" " + key + ":" + params.get(key));
        }
        return sb.toString();
    }
}
