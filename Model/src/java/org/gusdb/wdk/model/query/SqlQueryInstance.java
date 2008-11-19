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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.param.Param;
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
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     * @throws NoSuchAlgorithmException
     */
    protected SqlQueryInstance(SqlQuery query, Map<String, String> values)
            throws WdkModelException {
        super(query, values);
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
    protected ResultList getUncachedResults() throws WdkModelException,
            SQLException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        String sql = getUncachedSql();

        DBPlatform platform = query.getWdkModel().getQueryPlatform();
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        return new SqlResultList(resultSet);
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
            int instanceId) throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        Column[] columns = query.getColumns();
        StringBuffer columnList = new StringBuffer();
        columnList.append(CacheFactory.COLUMN_INSTANCE_ID);
        for (Column column : columns) {
            columnList.append(", ").append(column.getName());
        }

        // get the sql with param values applied.
        String sql = getUncachedSql();

        StringBuffer buffer = new StringBuffer("INSERT INTO ");
        buffer.append(tableName).append(" (").append(columnList).append(") ");
        buffer.append("SELECT ");
        buffer.append(instanceId).append(" AS ").append(columnList);
        buffer.append(" FROM (").append(sql).append(") f");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(buffer.toString());
        } catch (SQLException ex) {
            logger.error("Fail to run SQL:\n" + buffer);
            throw ex;
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    public String getUncachedSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        Map<String, Param> params = query.getParamMap();
        Map<String, String> paramValues = getValues();
        String sql = query.getSql();
        for (String paramName : paramValues.keySet()) {
            Param param = params.get(paramName);
            if (param == null)
                throw new WdkModelException("The param '" + paramName
                        + "' does not exist in query " + query.getFullName());

            String value = paramValues.get(paramName);
            sql = param.replaceSql(sql, value);
        }
        return sql;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.QueryInstance#getSql()
     */
    @Override
    public String getSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException {
        if (isCached()) return getCachedSql();
        else return getUncachedSql();
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
            int instanceId) throws NoSuchAlgorithmException, WdkModelException,
            SQLException, JSONException, WdkUserException {
        // get the sql with param values applied.
        String sql = getUncachedSql();

        StringBuffer buffer = new StringBuffer("CREATE TABLE ");
        buffer.append(tableName).append(" AS ").append("SELECT ");
        buffer.append(instanceId).append(" AS ").append(
                CacheFactory.COLUMN_INSTANCE_ID);
        buffer.append(", f.* FROM (").append(sql).append(") f");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(buffer.toString());
        } catch (SQLException ex) {
            logger.error("Fail to run SQL:\n" + buffer);
            throw ex;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
}
