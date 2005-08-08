package org.gusdb.wdk.model.implementation;


import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkLogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.Collection;

public class SqlQueryInstance extends QueryInstance  {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.SqlQueryInstance");
    
    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    /**
     * The unique name of the table in a database namespace which holds the cached 
     * results for this Instance.
     */
    String resultTableName = null;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    
    public SqlQueryInstance (SqlQuery query) {
        super(query);
        logger.finest("I've got a new sqlQueryInstance being created");
    }

    /**
     * Modified by Jerric - add project column to be joined with
     * @return
     * @throws WdkModelException
     */
    protected String getSql() throws WdkModelException {

        SqlQuery q = (SqlQuery)query;
        String sql = null;
	String newPkJoin = null;
    
    String newProjectJoin = null;   // by Jerric
    
       if (joinMode){
            newPkJoin = joinTableName + "." + primaryKeyColumnName;
            values.put("primaryKey", newPkJoin); //will this destroy the query for later use?

            // Modified by Jerric
            if (projectColumnName != null) {
                newProjectJoin = joinTableName + "." + projectColumnName;
                values.put("projectID", newProjectJoin);
            }
        }
        String initSql = 
            q.instantiateSql(query.getInternalParamValues(values));
        if (joinMode){
            sql = q.addUnionMultiModeConstraints(joinTableName, 
                    newPkJoin, startId, endId, initSql);
	} else {
            sql = initSql;
        }
        return sql;

    }

    /**
     * @return Full name of table containing result
     */
    public String getResultAsTableName() throws WdkModelException {
        if (resultTableName == null) 
            resultTableName = getResultFactory().getResultAsTableName(this);
        return resultTableName;
    }
    

    public ResultList getResult() throws WdkModelException {

	SqlQuery q = (SqlQuery)query;
        ResultList rl = getResultFactory().getResult(this);
        rl.checkQueryColumns(q, true, getIsCacheable() || joinMode);
        return rl;
    }

    public ResultList getPersistentResultPage(int startRow, int endRow) throws WdkModelException {
	
	if (!getIsCacheable()) throw new WdkModelException("Attempting to get persistent result page, but query instance is not cacheable");

	SqlQuery q = (SqlQuery)query;
        ResultList rl = getResultFactory().getPersistentResultPage(this,
								   startRow,
								   endRow);
        rl.checkQueryColumns(q, true, true);
        return rl;
	
    }

    public String getSqlForCache() throws WdkModelException{
	SqlQuery q = (SqlQuery)query;
	String cacheSql = q.getResultFactory().getSqlForCache(this);
	return cacheSql;
    }

    public Collection getCacheValues() throws WdkModelException{
	return getValues();
    }

    public String getLowLevelQuery() throws WdkModelException {
	return getSql();
    }

    protected ResultList getNonpersistentResult() throws WdkModelException {
        ResultSet resultSet = null;
        RDBMSPlatformI platform = ((SqlQuery)query).getRDBMSPlatform();

        try {
            resultSet = SqlUtils.getResultSet(platform.getDataSource(), 
                    getSql());

        } catch (SQLException e) { 
            String newline = System.getProperty( "line.separator" );
            String msg = newline + "Failed running query:" + newline +
            "\"" + getSql() + "\"" + newline;
            throw new WdkModelException(msg, e);
        }
        return new SqlResultList(this, null, resultSet);
    }

    protected void writeResultToTable(String resultTableName, 
            ResultFactory rf) throws WdkModelException {
        RDBMSPlatformI platform = ((SqlQuery)query).getRDBMSPlatform();

        try {
            platform.createTableFromQuerySql(platform.getDataSource(),
                    resultTableName, 
                    getSql());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

}
