package org.gusdb.wdk.model;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created: Wed May 19 15:11:30 2004
 *
 *  *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class BooleanQueryInstance extends QueryInstance {

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    /**
     * QueryInstanceI that composes the first part of this BooleanQueryInstance.
     */
    QueryInstance firstQueryInstance;

    /**
     * QueryInstanceI that composes the second part of this BooleanQueryInstance.
     */
    QueryInstance secondQueryInstance;

    /**
     * Operation that is to be performed to create a composition of the two QueryInstances.
     * Must be taken from one of the provided operations in org.gusdb.gus.wdk.model.BooleanQueryInstanceOperation;
     */
    String operation;

    /**
     * Query that this BooleanQueryInstance can use as reference.  It can use this Query's 
     * ResultFactory and its declared columns for reference purposes, and can return it to other classes that 
     * ask for it.  This BooleanQueryInstance should not use the Query to make new Instances, however,
     * or get SQL from the Query; all concrete implementations of the Query are expressed through the 
     * QueryInstance's that the BooleanQueryInstance contains.  The Query is the same that
     * <code>firstQueryInstance</code> points to.
     */
    Query refQuery;

    
    Query booleanQuery;



    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public BooleanQueryInstance (BooleanQuery query) {

	super(query);
	this.booleanQuery = query;

    }

    

    public void init(QueryInstance firstQueryInstance, QueryInstance secondQueryInstance, 
		     String operation) throws Exception{

	if (validateParameters(firstQueryInstance, secondQueryInstance, operation) == true){
	    System.err.println("parameters match...for now");
	}
	
	this.firstQueryInstance = firstQueryInstance;
	this.secondQueryInstance = secondQueryInstance;
	this.operation = operation;

	//DTB -- we are going to get yelled at if this Query is a PageableQuery
	//       we are going to throw an exception if someone tries to run a Boolean Query
	//       that has non-boolean operand type (eg PageableQuery) but maybe we should
	//       throw the exception when the BooleanQuery is created
	this.refQuery = firstQueryInstance.getQuery();  
	
	Column columns[] = firstQueryInstance.getQuery().getColumns();
	for (int i = 0; i < columns.length; i++){
	    booleanQuery.addColumn(columns[i]);
	}

	setIsCacheable(firstQueryInstance.getIsCacheable() && secondQueryInstance.getIsCacheable());
    }



    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------

    //anything calling this and expecting to get columns from the returned query (and maybe other things, params
    //being the exception) must have first made sure that init() has been called
    //
    //    public Query getQuery(){

    //	return booleanQuery;
    // }

    // ------------------------------------------------------------------
    // QueryInstance
    // ------------------------------------------------------------------

    //dtb -- for future reference, note that a boolean query never has to be in
    //multimode or anything tricky like that since that only applies to attribute queries
    protected String getSql() throws WdkModelException{
	//should be instance.getBooleanOperandSql()
	
	String sql = firstQueryInstance.getSqlForCache() + " " + operation + " " + secondQueryInstance.getSqlForCache();
	System.err.println("BQI.getSql(): returning " + sql);
	return sql;

    }

    private boolean validateParameters(QueryInstance firstQueryInstance, QueryInstance secondQueryInstance, 
				       String operation){
	return true;
    }



    //dtb: i think we decided this will never be different than what 'get sql' returns
    //keep for now but delete once I can verify this
    //not true--this could also return sql to get result from cache, which normal getSql() never does
    //(talked with steve)--change method name to getCacheSql or something similar (getResultAsSql), basically calls the result
    //factory to get its sql for getting itself out of the cache.  If it hasn't been put in the cache yet
    //then the resultfactory will put it in.  

    public String getSqlForCache() throws WdkModelException{
	String cacheSql = refQuery.getResultFactory().getSqlForCache(this);
	System.err.println("BQI.getSqlForCache (hopefully never called): returning " + cacheSql);
	return cacheSql;
    }

    public ResultList getResult() throws WdkModelException{
	
	System.err.println("BQI: getting result");
	if (getValues().isEmpty()){
	    Integer firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
	    if (firstQueryInstanceId == null){
		System.err.println("BQI.getResult: first QI Id is null");
		firstQueryInstance.getResult();//assumes values have been set
		firstQueryInstanceId = firstQueryInstance.getQueryInstanceId();
	    }
	    else{
		System.err.println("BQI.getResult: first QI is not null " + firstQueryInstanceId);
	    }
	    Integer secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
	    if (secondQueryInstanceId == null){
		secondQueryInstance.getResult();//assumes values have been set
		secondQueryInstanceId = secondQueryInstance.getQueryInstanceId();
	    }
	    Hashtable h = new Hashtable();
	    System.err.println("BQI.getResult: got First QI Id: " + firstQueryInstanceId.toString());
	    h.put(BooleanQuery.FIRST_INSTANCE_PARAM_NAME, firstQueryInstanceId.toString());
	    h.put(BooleanQuery.SECOND_INSTANCE_PARAM_NAME, secondQueryInstanceId.toString());
	    System.err.println("BQI: first id " + firstQueryInstanceId + " second id " + secondQueryInstanceId);
	    try{ //need to throw this but ok for now
		setValues(h);
	    }
	    catch (WdkUserException ue){
		System.err.println(ue.getMessage());
		ue.printStackTrace();
	    }
	    
	}
	else{
	    System.err.println("BQI: values not empty");
	}
	
	ResultFactory resultFactory = refQuery.getResultFactory();
	ResultList resultList = resultFactory.getResult(this);
	return resultList;
    }

    public String getResultAsTable() throws WdkModelException{
	return refQuery.getResultFactory().getResultAsTable(this);

    }

	//get first query instance id
	//get second query instance id
	//put in values map if not there
	//use result factory to get result
    

    protected  ResultList getNonpersistentResult() throws WdkModelException{
	System.err.println("method needs to be written when we decide if booleans can ever not be in the cache");
	return null;
    }

    //same method as in SqlQueryInstance; can we factor them?
    protected void writeResultToTable(String resultTableName, 
				      ResultFactory rf) throws WdkModelException {
        RDBMSPlatformI platform = rf.getRDBMSPlatform();
	
	//	System.err.println("BQI.WriteResultToTable: sql = " + getSql());

        try {
            platform.createTableFromQuerySql(platform.getDataSource(),
					     resultTableName, 
					     getSql());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }
    






}
