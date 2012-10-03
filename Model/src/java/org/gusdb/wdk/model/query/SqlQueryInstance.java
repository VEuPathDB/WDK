/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class SqlQueryInstance extends QueryInstance {

    private static Logger logger = Logger.getLogger(SqlQueryInstance.class);

    private SqlQuery query;

    /**
     * @param query
     * @param values
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     * @throws NoSuchAlgorithmException
     */
    protected SqlQueryInstance(User user, SqlQuery query,
            Map<String, String> values, boolean validate, int assignedWeight,
            Map<String, String> context) throws WdkModelException {
        super(user, query, values, validate, assignedWeight, context);
        this.query = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#appendSJONContent(org.json.JSONObject
     * )
     */
    @Override
    protected void appendSJONContent(JSONObject jsInstance) {
        // nothing to add to;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#getUncachedResults(java.lang.
     * Integer, java.lang.Integer)
     */
    @Override
    protected ResultList getUncachedResults() throws WdkModelException {
    	try {
	        String sql = getUncachedSql();
	        DBPlatform platform = query.getWdkModel().getQueryPlatform();
	        DataSource dataSource = platform.getDataSource();
	        ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
	                query.getFullName() + "-uncached-result");
	        return new SqlResultList(resultSet);
    	}
    	catch (SQLException e) {
    		throw new WdkModelException("Could not get uncached results from DB.", e);
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#insertToCache(java.sql.Connection
     * , java.lang.String)
     */
    @Override
    public void insertToCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException {
        String idColumn = CacheFactory.COLUMN_INSTANCE_ID;
        Map<String, Column> columns = query.getColumnMap();
        StringBuffer columnList = new StringBuffer();
        for (Column column : columns.values()) {
            columnList.append(", " + column.getName());
        }
        if (query.isHasWeight()) {
            String weightColumn = Utilities.COLUMN_WEIGHT;
            if (!columns.containsKey(weightColumn))
                columnList.append(", " + weightColumn);
        }

        // get the sql with param values applied. The last column has to be the
        // weight.
        String sql = getUncachedSql();

        StringBuffer buffer = new StringBuffer("INSERT INTO " + tableName);
        buffer.append(" (" + idColumn + columnList + ") ");
        buffer.append("SELECT ");
        buffer.append(instanceId + " AS " + idColumn + columnList);
        buffer.append(" FROM (").append(sql).append(") f");

        Statement stmt = null;
        try {
            long start = System.currentTimeMillis();

            stmt = connection.createStatement();
            stmt.execute(buffer.toString());

            SqlUtils.verifyTime(wdkModel, buffer.toString(),
                    query.getFullName() + "-insert-cache", start);
        }
        catch (SQLException ex) {
            logger.error("Fail to run SQL:\n" + buffer);
            throw new WdkModelException("Could not insert values into cache.", ex);
        }
        finally {
        	if (stmt != null)
                try { stmt.close(); }
                catch (SQLException e) { logger.error("Could not close Statement!", e); }
        }
    }

    public String getUncachedSql() throws WdkModelException {
        Map<String, String> internalValues = getInternalParamValues();
        Map<String, Param> params = query.getParamMap();
        String sql = query.getSql();
        for (String paramName : params.keySet()) {
            Param param = params.get(paramName);
            String value = internalValues.get(paramName);
            sql = param.replaceSql(sql, value);
        }
        if (query.isHasWeight()) {
            // add weight to the last column if it doesn't exist, it has to be
            // the last column.
            Map<String, Column> columns = query.getColumnMap();
            String weightColumn = Utilities.COLUMN_WEIGHT;
            if (!columns.containsKey(weightColumn)) {
                sql = "SELECT o.*, " + assignedWeight + " AS " + weightColumn
                        + " FROM (" + sql + ") o";
            } else { // has weight column defined, add assigned weight to it
                StringBuilder builder = new StringBuilder();
                for (String column : columns.keySet()) {
                    if (column.equals(weightColumn))
                        continue;
                    if (builder.length() == 0)
                        builder.append("SELECT ");
                    else
                        builder.append(", o.");
                    builder.append(column);
                }
                builder.append(", (o." + weightColumn + " + " + assignedWeight);
                builder.append(") AS " + weightColumn);
                builder.append(" FROM (" + sql + ") o");
            }
        }
        return sql;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#getSql()
     */
    @Override
    public String getSql() throws WdkModelException {
        if (isCached())
            return getCachedSql();
        else
            return getUncachedSql();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.QueryInstance#createCache(java.sql.Connection,
     * java.lang.String, int)
     */
    @Override
    public void createCache(Connection connection, String tableName,
            int instanceId, String[] indexColumns)
            throws WdkModelException {
        // get the sql with param values applied.
        String sql = getUncachedSql();

        StringBuffer buffer = new StringBuffer("CREATE TABLE " + tableName);
        buffer.append(" AS SELECT ");
        buffer.append(instanceId + " AS " + CacheFactory.COLUMN_INSTANCE_ID);
        buffer.append(", f.* FROM (").append(sql).append(") f");

        Statement stmt = null;
        try {
            long start = System.currentTimeMillis();
            stmt = connection.createStatement();
            stmt.execute(buffer.toString());

            SqlUtils.verifyTime(wdkModel, buffer.toString(),
                    "wdk-create-table", start);

            ResultFactory resultFactory = wdkModel.getResultFactory();
            resultFactory.createCacheTableIndex(connection, tableName,
                    indexColumns);
        }
        catch (SQLException ex) {
        	logger.error("Fail to run SQL:\n" + buffer);
            throw new WdkModelException("Unable to create cache.", ex);
        } finally {
        	SqlUtils.closeQuietly(stmt);
        }
    }
}
