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

public class SqlQueryInstance extends QueryInstance  {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.SqlQueryInstance");
    
    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    /**
     * The unique name of the table in a database namespace which holds the cached 
     * results for this Instance.
     */
    String resultTable = null;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    
    public SqlQueryInstance (SqlQuery query) {
        super(query);
        logger.finest("I've got a new sqlQueryInstance being created");
        setIsCacheable(query.getIsCacheable().booleanValue());
    }

    protected String getSql() throws WdkModelException {

        SqlQuery q = (SqlQuery)query;
        String sql = null;
        if (inMultiMode){
            String newPkJoin = multiModeResultTableName + "." + pkToJoinWith;
            values.put("primaryKey", newPkJoin); //will this destroy the query for later use?
        }
        String initSql = 
            q.instantiateSql(query.getInternalParamValues(values));
        if (inMultiMode){
            sql = q.addMultiModeConstraints(multiModeResultTableName, pkToJoinWith,
					    startId, endId, initSql);
        } else {
            sql = initSql;
        }
	//	System.err.println("SqlQueryInstance.getSql:  returning " + sql + " to run query " + query.getName());
        return sql;

    }

    /**
     * @return Full name of table containing result
     */
    public String getResultAsTable() throws WdkModelException {
        SqlQuery q = (SqlQuery)query;
        if (resultTable == null) 
            resultTable = q.getResultFactory().getResultAsTable(this);
        return resultTable;
    }
    

    public ResultList getResult() throws WdkModelException {

	SqlQuery q = (SqlQuery)query;
        ResultList rl = q.getResultFactory().getResult(this);
        rl.checkQueryColumns(q, true);
        return rl;
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
