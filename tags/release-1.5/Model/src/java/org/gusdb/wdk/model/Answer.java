package org.gusdb.wdk.model;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.Iterator;

/**
 * Answer.java
 *
 * Created: Fri June 4 13:01:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class Answer {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.Answer");
    
    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    int currentRecordInstanceCounter;

    private QueryInstance queryInstance;

    private QueryInstance multiModeQueryInstance;

    private RecordInstance[] recordInstances;

    private String listPrimaryKeyName;
    
    /**
     * Added by Jerric - the column name of project 
     */
    private String listProjectName;

    private Question question;

    private int startRow;

    private int endRow;

    private boolean isBoolean = false;

    private Integer resultSize;   // size of total result

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    /**
     * Assumes the values of <code>queryInstance</code> have been set already.
     */
    Answer(Question question, QueryInstance queryInstance, int startRow, int endRow) throws WdkUserException, WdkModelException{

	this.question = question;
	this.queryInstance = queryInstance;
	if (queryInstance instanceof org.gusdb.wdk.model.BooleanQueryInstance){
	    this.isBoolean = true;
	}
	this.currentRecordInstanceCounter = 0;
	this.startRow = startRow;
	this.endRow = endRow;   
	ResultList rl = getRecordInstanceIds();
	rl.close(); // rl only needed to close connection
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
        if (recordInstances != null) {
            return recordInstances.length;
        }
        return 0;
    }
    
    public int getResultSize() throws WdkModelException{

	if (resultSize == null) {
	    ResultList rl = getRecordInstanceIds();
	    int counter = 0;
	    while (rl.next()){
		counter++;
	    }
	    rl.close();
	    resultSize = new Integer(counter);
	}
	return resultSize.intValue();
    }

    /**
     * @return Map where key is param name and value is param value
     */
    public Map getParams() {
	return queryInstance.getValuesMap();
    }

    /**
     * @return Map where key is param display name and value is param value
     */
    public Map getDisplayParams() {
	LinkedHashMap displayParamsMap = new LinkedHashMap();
	Map paramsMap = getParams();
	Param[] params = question.getParams();
	for (int i=0; i<params.length; i++) {
	    Param param = params[i];
	    displayParamsMap.put(param.getPrompt(), 
				 paramsMap.get(param.getName()));
	}
	return displayParamsMap;
    }


    //Returns null if we have already returned the last instance
    public RecordInstance getNextRecordInstance() throws WdkModelException{
	
	if (recordInstances == null){
	    initRecordInstances();
	}
	RecordInstance nextInstance = null;
	if (currentRecordInstanceCounter < recordInstances.length){
	    nextInstance = recordInstances[currentRecordInstanceCounter];
	    currentRecordInstanceCounter++;
	}
	return nextInstance;
    }
    
    public boolean hasMoreRecordInstances() throws WdkModelException 
    {
        if (recordInstances == null){
	    initRecordInstances();
        }
        if (recordInstances == null){
            logger.finer("recordInstances is still null");
        }
        if (currentRecordInstanceCounter < recordInstances.length){
            return true;
        }
        return false;
    }

    public void resetRecordInstanceCounter(){
	currentRecordInstanceCounter = 0;

    }

    public String print() throws WdkModelException, WdkUserException{  
	StringBuffer buf = new StringBuffer();
	if (recordInstances == null){
	    initRecordInstances();
	}
	for (int i = 0; i < recordInstances.length; i++){
	    buf.append(recordInstances[i].print());
	}
	return buf.toString();
    }
    
    /**
     * print summary attributes, one per line
     */
    public String printAsSummary() throws WdkModelException, WdkUserException{
	StringBuffer buf = new StringBuffer();
	if (recordInstances == null){
	    initRecordInstances();
	}
	for (int i = 0; i < recordInstances.length; i++){
	    buf.append(recordInstances[i].printSummary());
	}
	return buf.toString();
    }

    /**
     * print summary attributes in tab delimited table with header of attr. names
     */
    public String printAsTable () throws WdkModelException, WdkUserException{
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer();
	if (recordInstances == null){
	    initRecordInstances();
	}
		
	if (recordInstances != null && recordInstances.length > 0){
	    RecordClass recordClass = recordInstances[0].getRecordClass();
	    Iterator attributeNames = 
		question.getSummaryAttributes().keySet().iterator();

	    while (attributeNames.hasNext()){
		buf.append((String)attributeNames.next() + "\t");
	    }
	    buf.append(newline);

	    for (int i = 0; i < recordInstances.length; i++){

		RecordInstance recordInstance = recordInstances[i];
		attributeNames = 
		    question.getSummaryAttributes().keySet().iterator();

		while (attributeNames.hasNext()){
		    String nextAttName = (String)attributeNames.next();

		    Object value = 
			recordInstance.getAttributeValue(nextAttName);
		    if (value != null){
			buf.append(value.toString() + "\t");
		    }
		}
		buf.append(newline);
		
	    }
	}

	return buf.toString();
    }


    private void initRecordInstances() throws WdkModelException {
	ResultList rl = getRecordInstanceIds();
	Query query = queryInstance.getQuery();
	Vector tempRecordInstances = new Vector();
	int counter = 0;
	while (rl.next()){
	    counter++;
	    if (counter >= startRow && counter <= endRow){
		RecordInstance nextRecordInstance = getQuestion().getRecordClass().makeRecordInstance();
		Column[] columns = query.getColumns();
        
        // Modified by Jerric

//		String primaryKeyName = columns[0].getName();
//		this.listPrimaryKeyName = primaryKeyName;
//		String primaryKey = 
//		    rl.getAttributeFieldValue(primaryKeyName).getValue().toString();
//		nextRecordInstance.setPrimaryKey(primaryKey);
        // check columns for project id and primary key
        String projectName, localPKName;
        
        if (columns.length == 1) {  // only present primary key
            projectName = null;
            localPKName = columns[0].getName();
        } else {    
            // having two columns, one is for primary key and one for project ID
            projectName = columns[0].getName();
            if (projectName.toUpperCase().indexOf("PROJECT")!= -1) {
                localPKName = columns[1].getName();
            } else {
                localPKName = projectName;
                projectName = columns[1].getName();
            }
        }
        this.listPrimaryKeyName = localPKName;
        this.listProjectName = projectName;
        
        String projectID = null;
        if (projectName != null)
            projectID = rl.getAttributeFieldValue(projectName).getValue().toString();
        String localPK = rl.getAttributeFieldValue(localPKName).getValue().toString();
        nextRecordInstance.setPrimaryKey(projectID, localPK);
		
		nextRecordInstance.setAnswer(this);
		tempRecordInstances.add(nextRecordInstance);
	    }
	}        
	recordInstances = new RecordInstance[tempRecordInstances.size()];
	tempRecordInstances.copyInto(recordInstances);
	rl.close();
    }

    public boolean getIsBoolean(){
	return this.isBoolean;
    }

    public QueryInstance getMultiModeQueryInstance() {
	return multiModeQueryInstance;
    }

    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    void setMultiMode(QueryInstance instance) throws WdkModelException{

        String resultTableName = queryInstance.getResultAsTable();
        // Modified by Jerric
//        instance.setMultiModeValues(resultTableName, listPrimaryKeyName, startRow, endRow);
        instance.setMultiModeValues(resultTableName, listProjectName, listPrimaryKeyName, startRow, endRow);
	multiModeQueryInstance = instance;
    }

    void setQueryResult(ResultList resultList) throws WdkModelException {
    
	Query query = resultList.getQuery();
	Column[] columns = query.getColumns();
        int counter = 0;
	int oldI = startRow;
        int i = 0;
	while (resultList.next()){

            Integer iInt = new Integer(resultList.getAttributeFieldValue(ResultFactory.MULTI_MODE_I).getValue().toString());
	    i = iInt.intValue();
	    int counterStart = counter;

	    //for all values of i that are skipped, set attributes for that record instance to null
	    while (counter < counterStart + (i - oldI)){
		RecordInstance nextRecordInstance = recordInstances[counter];
		counter++;
		for (int k = 0; k < columns.length; k++){
		    String nextColumnName = columns[k].getName();
		    nextRecordInstance.setAttributeValue(nextColumnName, "null");
		}
	    }
	    oldI = i;
	    //process first value of i in this loop that is not skipped
	    RecordInstance nextRecordInstance = recordInstances[counter];
            for (int k = 0; k < columns.length; k++){
                String nextColumnName = columns[k].getName();

                Object value = 
		    resultList.getAttributeFieldValue(nextColumnName).getValue();
                nextRecordInstance.setAttributeValue(nextColumnName, value);
            }
	    oldI++;
	    counter++;
        }
	counter = i;
	//process values of i at end of result set that did not return rows
	//could factor this out with similar logic above
	while (counter < recordInstances.length){
	    RecordInstance nextRecordInstance = recordInstances[counter];
	    counter++;
	    for (int k = 0; k < columns.length; k++){
		String nextColumnName = columns[k].getName();
		nextRecordInstance.setAttributeValue(nextColumnName, "null");
	    }
	}
    }
	
	

    public boolean isSummaryAttribute(String attName){
	return question.isSummaryAttribute(attName);
    }
    
    public QueryInstance getQueryInstance(){
	return queryInstance;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    private ResultList getRecordInstanceIds() throws WdkModelException{

	ResultList rl = queryInstance.getResult();
	return rl;
    }
    
    /*
      this method was expecting a query result to skip over certain values of i.  Keep here until we are sure we do not need it.

      
    void setQueryResult(ResultList resultList) throws WdkModelException {

        int tempCounter = 0;
        Query query = resultList.getQuery();
	Column[] columns = query.getColumns();
            
	while (resultList.next()){
            
            RecordInstance nextRecordInstance = recordInstances[tempCounter];
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


}
