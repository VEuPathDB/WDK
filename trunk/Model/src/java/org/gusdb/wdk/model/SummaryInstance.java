package org.gusdb.gus.wdk.model;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * SummaryInstance.java
 *
 * Created: Fri June 4 13:01:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class SummaryInstance {

    private static final Logger logger = Logger.getLogger("org.gusdb.gus.wdk.model.SummaryInstance");
    
    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    int currentRecordInstanceCounter;

    private QueryInstance listIdQueryInstance;

    private RecordInstance[] recordInstances;

    private String listPrimaryKeyName;

    private Summary summary;

    private int startRow;

    private int endRow;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public SummaryInstance(Summary summary, QueryInstance queryInstance){

	this.summary = summary;
	this.listIdQueryInstance = queryInstance;
	this.currentRecordInstanceCounter = 0;
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public void setValues(Map values, int startRow, int endRow) throws WdkUserException, WdkModelException {

	this.startRow = startRow;
	this.endRow = endRow;	
	listIdQueryInstance.setValues(values);
    initRecordInstances();
    }

    public Summary getSummary(){
	return this.summary;
    }

    public int size(){
	if (recordInstances != null){
	    return recordInstances.length;
	}
	else return 0;
    }
    
    public int getTotalLength() throws WdkModelException{

	ResultList rl = getRecordInstanceIds();
	int counter = 0;
	while (rl.next()){
	    counter++;
	}
	return counter;
	
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
        
        String resultTableName = listIdQueryInstance.getResultAsTable();
        
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
	    
	    logger.finer(recordInstances[i].print());
	}
    }


    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    
    private void initRecordInstances() throws WdkModelException {
	ResultList rl = getRecordInstanceIds();
	Query query = listIdQueryInstance.getQuery();
	Vector tempRecordInstances = new Vector();
	int counter = 0;
	while (rl.next()){
	    counter++;
	    if (counter >= startRow && counter <= endRow){
		RecordInstance nextRecordInstance = getSummary().getRecord().makeRecordInstance();
		
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

	ResultList rl = listIdQueryInstance.getResult();
	return rl;
    }
    



}
