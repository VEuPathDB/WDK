package org.gusdb.wdk.model;


import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.Iterator;

/**
 * SummaryInstance.java
 *
 * Created: Fri June 4 13:01:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class SummaryInstance {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.SummaryInstance");
    
    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    int currentRecordInstanceCounter;

    private QueryInstance queryInstance;

    private RecordInstance[] recordInstances;

    private String listPrimaryKeyName;

    private Question question;

    private int startRow;

    private int endRow;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public SummaryInstance(Question question, QueryInstance queryInstance, Map paramValues, int startRow, int endRow) throws WdkUserException, WdkModelException{

	this.question = question;
	this.queryInstance = queryInstance;
	this.currentRecordInstanceCounter = 0;
    this.startRow = startRow;
    this.endRow = endRow;   
    queryInstance.setValues(paramValues);
    initRecordInstances();
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

    public Iterator getRecords() {
	return new SummaryInstanceList(this);
    }

    public Map getTables() {
	return new AttributeValueMap(question.getRecordClass(), null, true);
    }

    public Map getAttributes() {
	return new AttributeValueMap(question.getRecordClass(), null, false);
    }

    public int size(){
        if (recordInstances != null) {
            return recordInstances.length;
        }
        return 0;
    }
    
    public int getTotalSize() {
	try {
	    return getTotalLength();
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
    }
    
    public int getTotalLength() throws WdkModelException{

	ResultList rl = getRecordInstanceIds();
	int counter = 0;
	while (rl.next()){
	    counter++;
	}
	return counter;
	
    }

    /**
     * @return Map where key is param name and value is param value
     */
    public Map getParams() {
	return queryInstance.getValuesMap();
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
    
    public void setQueryResult(ResultList resultList) throws WdkModelException{
        logger.finer("In setQueryList and resultList is "+resultList);
        int tempCounter = 0;
        while (resultList.next()){
            
            RecordInstance nextRecordInstance = recordInstances[tempCounter];
            Query query = resultList.getQuery();
            Column[] columns = query.getColumns();
            for (int j = 0; j < columns.length; j++){
                String nextColumnName = columns[j].getName();
                logger.finer("Trying to get query for "+nextColumnName);
                Object value = resultList.getValue(nextColumnName);
                nextRecordInstance.setAttributeValue(nextColumnName, value);
            }
            tempCounter++;
        }

    }
    
    public void setMultiMode(QueryInstance instance) throws WdkModelException{
        
        String resultTableName = queryInstance.getResultAsTable();
        
        instance.setMultiModeValues(resultTableName, listPrimaryKeyName, startRow, endRow);
    }
    
    public boolean hasMoreRecordInstances(){
        if (recordInstances == null){
            try {
                initRecordInstances();
            }
            catch (WdkModelException exp) {
                exp.printStackTrace(System.err);
            }
        }
        if (recordInstances == null){
            logger.finer("recordInstances is still null");
        }
        if (currentRecordInstanceCounter < recordInstances.length){
            return true;
        }
        return false;
    }

    public void reset(){
	this.currentRecordInstanceCounter = 0;
    }

    public void print() throws WdkModelException{  
	
	if (recordInstances == null){
	    initRecordInstances();
	}
	for (int i = 0; i < recordInstances.length; i++){
	    System.out.println(recordInstances[i].print());
	}
    }

    public void printAsTable() throws WdkModelException{
	
	if (recordInstances == null){
	    initRecordInstances();
	}
	if (recordInstances != null){
	    RecordClass recordClass = recordInstances[0].getRecordClass();
	    Iterator attributeNames = recordClass.getNonTextAttributeNames().iterator();
	    String nameLine = "";
	    while (attributeNames.hasNext()){
		String nextAttName = (String)attributeNames.next();
		nameLine = nameLine.concat(nextAttName + "\t");
	    }
	    System.out.println(nameLine);
	    for (int i = 0; i < recordInstances.length; i++){
		String nextLine = "";
		RecordInstance nextRecordInstance = recordInstances[i];
		Iterator attributes = recordClass.getNonTextAttributeNames().iterator();
		while (attributes.hasNext()){
		    String nextAttName = (String)attributes.next();
		    Object nextAttValue = nextRecordInstance.getAttributeValue(nextAttName);
		    nextLine = nextLine.concat(nextAttValue.toString() + "\t");
		}
		System.out.println(nextLine);
	    }
	}
    }


    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    
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
		String primaryKeyName = columns[0].getName();
		this.listPrimaryKeyName = primaryKeyName;
		String primaryKey = rl.getValue(primaryKeyName).toString();
		nextRecordInstance.setPrimaryKey(primaryKey);
		
		nextRecordInstance.setSummaryInstance(this);
		tempRecordInstances.add(nextRecordInstance);
	    }
	}        
	recordInstances = new RecordInstance[tempRecordInstances.size()];
	tempRecordInstances.copyInto(recordInstances);
	rl.close();
	
    }

    private ResultList getRecordInstanceIds() throws WdkModelException{

	ResultList rl = queryInstance.getResult();
	return rl;
    }
    



}
