package org.gusdb.wdk.model.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.DynamicAttributeSet;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.WdkModelException;

public class SqlQueryInstance extends QueryInstance {

    private static final Logger logger = Logger
            .getLogger(SqlQueryInstance.class);

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    public SqlQueryInstance(SqlQuery query) {
        super(query);
        // logger.debug("I've got a new sqlQueryInstance being created");
    }

    public ResultList getPersistentResultPage(int startRow, int endRow)
            throws WdkModelException {

        if (!getIsCacheable())
            throw new WdkModelException("Attempting to get persistent result "
                    + "page, but query instance is not cacheable");

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
        RDBMSPlatformI platform = ((SqlQuery) query).getRDBMSPlatform();

        try {
            resultSet = SqlUtils.getResultSet(platform.getDataSource(),
                    getSql());

        } catch (SQLException e) {
            String newline = System.getProperty("line.separator");
            String msg = newline + "Failed running query:" + newline + "\""
                    + getSql() + "\"" + newline;
            throw new WdkModelException(msg, e);
        }
        return new SqlResultList(this, null, resultSet);
    }

    protected void writeResultToTable(String resultTableName, ResultFactory rf)
            throws WdkModelException {
        RDBMSPlatformI platform = ((SqlQuery) query).getRDBMSPlatform();

        try {
            platform.createResultTable(platform.getDataSource(),
                    resultTableName, getSql());
        } catch (SQLException e) {
            throw new WdkModelException(e);
        }
    }

    /**
     * @return Sql to run. If join mode, it is modified for joining
     * @throws WdkModelException
     */
    private String getSql() throws WdkModelException {
        return joinMode ? getJoinSql() : ((SqlQuery) query)
                .instantiateSql(query.getInternalParamValues(values));

    }

    private String getJoinSql() throws WdkModelException {
        String sql = ((SqlQuery) query).getSql();

        // resolve the references to sorting columns
        RDBMSPlatformI platform = ((SqlQuery) query).getRDBMSPlatform();
        String joinTableName = cacheTable.getCacheTableFullName();
        int sortingIndex = getSortingIndex();
        SqlClause clause = new SqlClause(sql, joinTableName, projectColumnName,
                primaryKeyColumnName, sortingIndex, startIndex, endIndex,
                platform);

        return instantiateSqlWithJoin(clause.getModifiedSql());
    }

    private String instantiateSqlWithJoin(String sql) throws WdkModelException {
        String joinTableName = cacheTable.getCacheTableFullName();
        String primaryKeyJoin = joinTableName + "." + primaryKeyColumnName;

        // in the join mode, we have to force the "use quote" of primaryKey and
        // project_id to be false, since the values of them are column names.
        
        // backup the quote flag of primary key
        StringParam primaryKey = (StringParam)query.getParam(RecordClass.PRIMARY_KEY_NAME);
        boolean primaryKeyQuote = primaryKey.isQuote();
        primaryKey.setQuote(false);
        
        values.put(RecordClass.PRIMARY_KEY_NAME, primaryKeyJoin);

        StringParam projectId = null;
        boolean projectIdQuote = true;;
        if (projectColumnName != null) {
            String projectJoin = joinTableName + "." + projectColumnName;

            // backup the quote flag of primary key
            projectId = (StringParam)query.getParam(RecordClass.PROJECT_ID_NAME);
            projectIdQuote = projectId.isQuote();
            projectId.setQuote(false);
            
            values.put(RecordClass.PROJECT_ID_NAME, projectJoin);
        }

        if (isDynamic) {
            values.put(DynamicAttributeSet.RESULT_TABLE, joinTableName);
        }

        sql = ((SqlQuery) query).instantiateSql(query
                .getInternalParamValues(values), sql);
        
        // restore the quote flags
        primaryKey.setQuote(primaryKeyQuote);
        if (projectId != null) projectId.setQuote(projectIdQuote);
        
        return sql;
    }

}
