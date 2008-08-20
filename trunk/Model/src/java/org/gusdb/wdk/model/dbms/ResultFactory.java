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
import java.util.Date;

import javax.sql.DataSource;

import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.QueryInstance;
import org.json.JSONException;

/**
 * @author Jerric Gao
 * 
 */
public class ResultFactory {

    private DBPlatform platform;
    private CacheFactory cacheFactory;

    public ResultFactory(WdkModel wdkModel) {
        this.platform = wdkModel.getQueryPlatform();
        this.cacheFactory = new CacheFactory(platform);
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    public ResultList getCachedResults(QueryInstance instance,
            Column[] displayColumns, Integer startIndex, Integer endIndex)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException {
        int instanceId = getInstanceId(instance);

        // get the composed sql
        String sql = composeSql(instance, displayColumns, instanceId,
                startIndex, endIndex);

        // get the resultList
        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        return new SqlResultList(resultSet);
    }

    public int getInstanceId(QueryInstance instance) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // get the query instance id; null if not exist
        Integer instanceId = queryInstanceId(instance);
        if (instanceId == null) { // instance cache not exist, create it
            instanceId = createCache(instance);
        } else { // instance has been cached
            updateAccessTime(instance, instanceId);
        }
        instance.setInstanceId(instanceId);
        return instanceId;
    }

    public String getCacheTable(QueryInstance instance) throws SQLException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        // make sure the instance is cached
        getInstanceId(instance);
        String queryName = instance.getQuery().getFullName();
        return CacheFactory.normalizeTableName(queryName);
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
     */
    private Integer queryInstanceId(QueryInstance instance)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID);
        sql.append(" FROM ").append(CacheFactory.TABLE_CACHE_INDEX);
        sql.append(" WHERE ").append(CacheFactory.COLUMN_QUERY_NAME);
        sql.append(" = '").append(instance.getQuery().getFullName());
        sql.append("' AND ").append(CacheFactory.COLUMN_INSTANCE_CHECKSUM);
        sql.append(" = '").append(instance.getChecksum()).append("'");

        DataSource dataSource = platform.dataSource;
        return (Integer) SqlUtils.executeScalar(dataSource, sql.toString());
    }

    private int createCache(QueryInstance instance) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        DataSource dataSource = platform.getDataSource();

        // start transaction
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try {
            // create cache index
            int instanceId = addCacheIndex(connection, instance);

            // check whether to create the cache or insert into existing cache
            String queryName = instance.getQuery().getFullName();
            String tableName = CacheFactory.normalizeTableName(queryName);
            if (!platform.checkTableExists(null, tableName)) {
                instance.createCache(connection, tableName, instanceId);
                createIndexOnCache(connection, tableName, instanceId);
            } else instance.insertToCache(connection, tableName, instanceId);

            connection.commit();
            return instanceId;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } catch (WdkUserException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    private void createIndexOnCache(Connection connection, String tableName,
            int instanceId) throws SQLException {

        // create index
        StringBuffer sqlIndex = new StringBuffer("CREATE INDEX ");
        sqlIndex.append(tableName).append("_id ON ").append(tableName);
        sqlIndex.append(" (").append(CacheFactory.COLUMN_INSTANCE_ID);
        sqlIndex.append(")");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(sqlIndex.toString());
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    private String composeSql(QueryInstance instance, Column[] columns,
            int instanceId, Integer startIndex, Integer endIndex) {
        String queryName = instance.getQuery().getFullName();
        String tableName = CacheFactory.normalizeTableName(queryName);
        StringBuffer sql = new StringBuffer("SELECT ");
        boolean firstColumn = true;
        for (Column column : instance.getQuery().getColumns()) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");
            sql.append(column.getName());
        }
        sql.append(" FROM ").append(tableName);

        // check if we need to do paging
        if (startIndex != null && endIndex != null) {
            return platform.getPagedSql(sql.toString(), startIndex, endIndex);
        } else return sql.toString();
    }

    private int addCacheIndex(Connection connection, QueryInstance instance)
            throws SQLException, NoSuchAlgorithmException, WdkModelException,
            JSONException {
        // get a new id for the instance
        int instanceId = platform.getNextId(null,
                CacheFactory.TABLE_CACHE_INDEX);

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(CacheFactory.TABLE_CACHE_INDEX).append(" (");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(", ");
        sql.append(CacheFactory.COLUMN_QUERY_NAME).append(", ");
        sql.append(CacheFactory.COLUMN_INSTANCE_CHECKSUM).append(", ");
        sql.append(CacheFactory.COLUMN_LAST_ACCESS).append(", ");
        sql.append(CacheFactory.COLUMN_RESULT_MESSAGE);
        sql.append(") VALUES (?, ?, ?, ?, ?)");

        Date lastAccess = new Date();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, instanceId);
            ps.setString(2, instance.getQuery().getFullName());
            ps.setString(3, instance.getChecksum());
            ps.setDate(4, new java.sql.Date(lastAccess.getTime()));
            platform.updateClobData(ps, 5, instance.getResultMessage(), false);

            instance.setLastAccessTime(lastAccess);
            return instanceId;
        } finally {
            // close the statement manually, since we cannot close the
            // connection; it's not committed yet.
            if (ps != null) ps.close();
        }
    }

    private void updateAccessTime(QueryInstance instance, int instanceId)
            throws SQLException {
        Date lastAccess = new Date();

        StringBuffer sql = new StringBuffer("UPDATE ");
        sql.append(CacheFactory.TABLE_CACHE_INDEX).append(" SET ");
        sql.append(CacheFactory.COLUMN_LAST_ACCESS).append(" = ? WHERE ");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(" = ?");

        PreparedStatement ps = null;
        DataSource dataSource = platform.getDataSource();
        try {
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setDate(1, new java.sql.Date(lastAccess.getTime()));
            ps.setInt(2, instanceId);
            ps.execute();
            instance.setLastAccessTime(lastAccess);
        } finally {
            SqlUtils.closeStatement(ps);
        }
    }
}
