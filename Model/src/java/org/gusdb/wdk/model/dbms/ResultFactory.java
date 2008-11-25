/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ProcessQueryInstance;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.json.JSONException;

/**
 * @author Jerric Gao
 * 
 */
public class ResultFactory {

    private DBPlatform platform;
    private CacheFactory cacheFactory;

    public ResultFactory(WdkModel wdkModel) throws SQLException {
        this.platform = wdkModel.getQueryPlatform();
        this.cacheFactory = new CacheFactory(wdkModel, platform);
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    public ResultList getCachedResults(QueryInstance instance)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {

        // get the cached sql
        QueryInfo queryInfo = cacheFactory.getQueryInfo(instance.getQuery());
        StringBuffer sql = new StringBuffer("SELECT ");
        boolean firstColumn = true;
        for (Column column : instance.getQuery().getColumns()) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");
            sql.append(column.getName());
        }
        sql.append(" FROM ").append(queryInfo.getCacheTable());
        sql.append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_ID);
        sql.append(" = ").append(instance.getInstanceId());

        // get the resultList
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql.toString());
        return new SqlResultList(resultSet);
    }

    public int getInstanceId(QueryInstance instance, String[] indexColumns)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        QueryInfo queryInfo = cacheFactory.getQueryInfo(instance.getQuery());

        // get the query instance id; null if not exist
        Integer instanceId = checkInstanceId(instance, queryInfo);
        if (instanceId == null) // instance cache not exist, create it
            instanceId = createCache(instance, queryInfo, indexColumns);
        instance.setInstanceId(instanceId);
        return instanceId;
    }

    public String getCacheTable(QueryInstance instance, String[] indexColumn)
            throws SQLException, WdkModelException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        // make sure the instance is cached
        getInstanceId(instance, indexColumn);
        Query query = instance.getQuery();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(query);
        return queryInfo.getCacheTable();
    }

    /**
     * look up the cache index table for the query instance; if the instance
     * doesn't exist, return null;
     * 
     * @param instance
     * @return
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    private Integer checkInstanceId(QueryInstance instance, QueryInfo queryInfo)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID);
        sql.append(" FROM ").append(CacheFactory.TABLE_INSTANCE);
        sql.append(" WHERE ").append(CacheFactory.COLUMN_QUERY_ID);
        sql.append(" = ").append(queryInfo.getQueryId());
        sql.append(" AND ").append(CacheFactory.COLUMN_INSTANCE_CHECKSUM);
        sql.append(" = '").append(instance.getChecksum()).append("'");

        DataSource dataSource = platform.getDataSource();
        try {
            Object id = SqlUtils.executeScalar(dataSource, sql.toString());
            return Integer.parseInt(id.toString());
        } catch (WdkModelException ex) {
            // the instance doesn't exist
            return null;
        }
    }

    private int createCache(QueryInstance instance, QueryInfo queryInfo,
            String[] indexColumns) throws JSONException, SQLException,
            WdkUserException, NoSuchAlgorithmException, WdkModelException {
        DataSource dataSource = platform.getDataSource();

        // start transaction
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try {
            // add instance into cache index table, and get instanceId back
            int instanceId = addCacheInstance(connection, queryInfo, instance);

            // check whether need to create the cache;
            String cacheTable = queryInfo.getCacheTable();
            if (!platform.checkTableExists(null, cacheTable)) {
                instance.createCache(connection, cacheTable, instanceId);
                createCacheTableIndex(connection, cacheTable, indexColumns);

                // the SqlQuery create & insert data at the same time; but
                // ProcessQuery does it in two steps
                if (instance instanceof ProcessQueryInstance)
                    instance.insertToCache(connection, cacheTable, instanceId);
            } else {
                // insert result into existing cache table
                instance.insertToCache(connection, cacheTable, instanceId);
            }

            connection.commit();
            return instanceId;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } catch (WdkUserException ex) {
            connection.rollback();
            throw ex;
        } catch (NoSuchAlgorithmException ex) {
            connection.rollback();
            throw ex;
        } catch (WdkModelException ex) {
            connection.rollback();
            throw ex;
        } catch (JSONException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    private void createCacheTableIndex(Connection connection,
            String cacheTable, String[] indexColumns) throws SQLException {
        // create index on query instance id
        StringBuffer sqlId = new StringBuffer("CREATE INDEX ");
        sqlId.append(cacheTable).append("_idx01 ON ").append(cacheTable);
        sqlId.append(" (").append(CacheFactory.COLUMN_INSTANCE_ID).append(")");

        // create index on other columns
        StringBuffer sqlOther = null;
        if (indexColumns != null) {
            sqlOther = new StringBuffer("CREATE INDEX ");
            sqlOther.append(cacheTable).append("_idx02 ON ").append(cacheTable);
            sqlOther.append(" (");
            boolean firstColumn = true;
            for (String column : indexColumns) {
                if (firstColumn) firstColumn = false;
                else sqlOther.append(", ");
                sqlOther.append(column);
            }
            sqlOther.append(")");
        }

        Statement stmt = null;
        try {
            stmt = connection.createStatement();

            stmt.execute(sqlId.toString());
            if (indexColumns != null) stmt.execute(sqlOther.toString());
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    private int addCacheInstance(Connection connection, QueryInfo queryInfo,
            QueryInstance instance) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // get a new id for the instance
        int instanceId = platform.getNextId(null, CacheFactory.TABLE_INSTANCE);

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(CacheFactory.TABLE_INSTANCE).append(" (");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(", ");
        sql.append(CacheFactory.COLUMN_QUERY_ID).append(", ");
        sql.append(CacheFactory.COLUMN_INSTANCE_CHECKSUM).append(", ");
        sql.append(CacheFactory.COLUMN_RESULT_MESSAGE);
        sql.append(") VALUES (?, ?, ?, ?)");

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, instanceId);
            ps.setInt(2, queryInfo.getQueryId());
            ps.setString(3, instance.getChecksum());
            platform.setClobData(ps, 4, instance.getResultMessage(), false);
            ps.executeUpdate();

            return instanceId;
        } finally {
            // close the statement manually, since we cannot close the
            // connection; it's not committed yet.
            if (ps != null) ps.close();
        }
    }
}
