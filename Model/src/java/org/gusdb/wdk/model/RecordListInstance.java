package org.gusdb.gus.wdk.model;
import java.util.Hashtable;
import java.util.Vector;

/**
 * RecordListInstance.java
 *
 * Created: Fri June 4 13:01:30 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class RecordListInstance {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    int currentRecordInstanceCounter;

    private Hashtable values;

    private QueryInstance listIdQueryInstance;

    private RecordInstance[] recordInstances;

    private String listPrimaryKeyName;

    private RecordList recordList;

    private int startRow;

    private int endRow;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public RecordListInstance(RecordList recordList, QueryInstance queryInstance){

	this.recordList = recordList;
	this.listIdQueryInstance = queryInstance;
	this.currentRecordInstanceCounter = 0;
    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    public void setValues(Hashtable values, int startRow, int endRow){

	this.startRow = startRow;
	this.endRow = endRow;	
	this.values = values;
    }

    public RecordList getRecordList(){
	return this.recordList;
    }
    
    //Returns null if we have already returned the last instance
    public RecordInstance getNextRecordInstance() throws Exception{
	
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
    
    public void setQueryResult(ResultList resultList) throws Exception{
	
	int tempCounter = 0;
	while (resultList.next()){
	    	    
	    RecordInstance nextRecordInstance = recordInstances[tempCounter];
	    Query query = resultList.getQuery();
	    Column[] columns = query.getColumns();
	    for (int j = 0; j < columns.length; j++){
		String nextColumnName = columns[j].getName();
		Object value = resultList.getValue(nextColumnName);
		nextRecordInstance.setFieldValue(nextColumnName, value);
	    }
	    tempCounter++;
	}
    }
    
    public void setMultiMode(QueryInstance instance) throws Exception{

	String resultTableName = listIdQueryInstance.getResult().getResultTableName();

	instance.setMultiModeValues(resultTableName, listPrimaryKeyName, startRow, endRow);
    }

    public boolean hasMoreRecordInstances(){

	if (currentRecordInstanceCounter < recordInstances.length){
	    return true;
	}
	else { return false; }
    }

    public void reset(){
	this.currentRecordInstanceCounter = 0;
    }

    public void print() throws Exception{  //change this so it works if startrow and endrow are not set
	
	if (recordInstances == null){
	    initRecordInstances();
	}
	int rownum = startRow;
	for (int i = startRow - 1; i < endRow; i++){
	    
	    System.err.println("Data For Record Instance number " + rownum + ":\n");
	    System.err.println(recordInstances[i].print());
	    rownum++;
	}
    }


    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    
    private void initRecordInstances() throws Exception{
	ResultList rl = getRecordInstanceIds();
	//	recordInstances = new RecordInstance[rl.size()];
	Query query = listIdQueryInstance.getQuery();
	Vector tempRecordInstances = new Vector();

	while (rl.next()){

	    RecordInstance nextRecordInstance = getRecordList().getRecord().makeInstance();

	    Column[] columns = query.getColumns();
	    String primaryKeyName = columns[0].getName();
	    this.listPrimaryKeyName = primaryKeyName;
	    String primaryKey = rl.getValue(primaryKeyName).toString();
	    nextRecordInstance.setPrimaryKey(primaryKey);

	    nextRecordInstance.setRecordListInstance(this);
	    tempRecordInstances.add(nextRecordInstance);
	}        
	recordInstances = new RecordInstance[tempRecordInstances.size()];
	tempRecordInstances.copyInto(recordInstances);
    }

    private ResultList getRecordInstanceIds() throws Exception{
	
	listIdQueryInstance.setValues(values);
	ResultList rl = listIdQueryInstance.getResult();
	return rl;
    }
    



}
