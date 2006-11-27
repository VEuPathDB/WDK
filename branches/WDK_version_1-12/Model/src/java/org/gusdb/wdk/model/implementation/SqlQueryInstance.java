package org.gusdb.wdk.model.implementation;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.gusdb.wdk.model.DynamicAttributeSet;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.WdkLogManager;
import org.gusdb.wdk.model.WdkModelException;

public class SqlQueryInstance extends QueryInstance  {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.SqlQueryInstance");
    
    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------


    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    
    public SqlQueryInstance (SqlQuery query) {
        super(query);
        logger.finest("I've got a new sqlQueryInstance being created");
    }

    public ResultList getPersistentResultPage(int startRow, int endRow)
            throws WdkModelException {

        if (!getIsCacheable())
            throw new WdkModelException(
                    "Attempting to get persistent result page, but query " +
                    "instance is not cacheable");

        SqlQuery q = (SqlQuery) query;
        ResultList rl = getResultFactory().getPersistentResultPage(this,
                startRow, endRow);
        // modified by Jerric - do not check if SQL has more columns than
        // declared in the Query. This modification is required by boolean
        // operation + dynamic attributes
        // rl.checkQueryColumns(q, true, true);
        rl.checkQueryColumns(q, false, true);
        return rl;
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
            platform.createResultTable(platform.getDataSource(),
				       resultTableName, 
				       getSql());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    /**
     * @return Sql to run.  If join mode, it is modified for joining
     * @throws WdkModelException
     */
    private String getSql() throws WdkModelException {
	return joinMode?
	    getJoinSql() :
	    ((SqlQuery)query).instantiateSql(query.getInternalParamValues(values));

    }


    private String getJoinSql()  throws WdkModelException {
	String sql = ((SqlQuery)query).getSql();

        RDBMSPlatformI platform = ((SqlQuery)query).getRDBMSPlatform();
	SqlClause clause = new SqlClause(sql, joinTableName, 
					 startIndex, endIndex, platform);

	return instantiateSqlWithJoin(clause.getModifiedSql());

    }

    private String instantiateSqlWithJoin(String sql) throws WdkModelException { 
	String primaryKeyJoin = joinTableName + "." + primaryKeyColumnName;

    checksum = null;
	values.put(RecordClass.PRIMARY_KEY_NAME, primaryKeyJoin); 
	
	if (projectColumnName != null) {
	    String projectJoin = joinTableName + "." + projectColumnName;
	    values.put(RecordClass.PROJECT_ID_NAME, projectJoin);
	}

	if (isDynamic) {
	    values.put(DynamicAttributeSet.RESULT_TABLE, joinTableName);
	}

	return ((SqlQuery)query).instantiateSql(query.getInternalParamValues(values), sql);
    }

}
