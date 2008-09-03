/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

/**
 * @author Jerric Gao
 * 
 */
public class CacheFactory {

    static final String TABLE_CACHE_INDEX = "QueryInstance";
    public static final String COLUMN_INSTANCE_ID = "instance_id";
    static final String COLUMN_QUERY_NAME = "query_name";
    static final String COLUMN_INSTANCE_CHECKSUM = "instance_checksum";
    static final String COLUMN_RESULT_MESSAGE = "result_message";
    
    public static String normalizeTableName(String tableName) {
        return tableName.trim().toLowerCase().replaceAll("\\W", "_");
    }

    private DBPlatform platform;
    private DataSource dataSource;

    public CacheFactory(DBPlatform platform) {
        this.platform = platform;
        this.dataSource = platform.getDataSource();
    }

    public void createCache() throws SQLException {
        // create the index table;
        createCacheIndexTable();

        // create the id sequence for the cache index
        String sequenceName = TABLE_CACHE_INDEX + DBPlatform.ID_SEQUENCE_SUFFIX;
        platform.createSequence(sequenceName, 1, 1);
    }

    public void resetCache() throws SQLException {
        // drop cache tables and we are done
        dropCacheTables();
    }

    public void recreateCache() throws SQLException {
        // drop cache;
        dropCache();
        // create them back
        createCache();
    }

    public void dropCache() throws SQLException {
        // drop cache tables
        dropCacheTables();

        // drop index table and sequence
        SqlUtils.executeUpdate(dataSource, "DROP TABLE " + TABLE_CACHE_INDEX);
        String sequenceName = TABLE_CACHE_INDEX + DBPlatform.ID_SEQUENCE_SUFFIX;
        SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + sequenceName);
    }

    public void dropCache(int instanceId) throws SQLException {
        String whereClause = " WHERE " + COLUMN_INSTANCE_ID + " = "
                + instanceId;

        // get the query name from the id
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_QUERY_NAME);
        sql.append(" FROM ");
        sql.append(TABLE_CACHE_INDEX);
        sql.append(whereClause);
        String queryName = (String) SqlUtils.executeScalar(dataSource,
                sql.toString());

        // delete the specific query result from the cache
        String cacheTable = normalizeTableName(queryName);
        sql = new StringBuffer("DELETE FROM ");
        sql.append(cacheTable);
        sql.append(whereClause);
        SqlUtils.executeUpdate(dataSource, sql.toString());

        // delete the query index
        sql = new StringBuffer("DELETE FROM ");
        sql.append(TABLE_CACHE_INDEX);
        sql.append(whereClause);
        SqlUtils.executeUpdate(dataSource, sql.toString());
    }

    public void dropCache(String queryName) throws SQLException {
        String cacheTable = normalizeTableName(queryName);

        // drop the cacheTable
        StringBuffer sql = new StringBuffer("DROP TABLE ");
        sql.append(cacheTable);
        SqlUtils.executeUpdate(dataSource, sql.toString());

        // delete the rows in the index
        sql = new StringBuffer("DELETE FROM ");
        sql.append(TABLE_CACHE_INDEX);
        sql.append(" WHERE ").append(COLUMN_QUERY_NAME).append(" = ?");

        PreparedStatement stDelete = null;
        try {
            stDelete = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            stDelete.setString(1, queryName);
            stDelete.executeUpdate();
        } finally {
            SqlUtils.closeStatement(stDelete);
        }
    }

    private void createCacheIndexTable() throws SQLException {
        // create the cache index table
        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(TABLE_CACHE_INDEX).append(" ( ");
        sql.append(COLUMN_INSTANCE_ID).append(" ");
        sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
        sql.append(COLUMN_QUERY_NAME).append(" ");
        sql.append(platform.getStringDataType(100)).append(" NOT NULL, ");
        sql.append(COLUMN_INSTANCE_CHECKSUM).append(" ");
        sql.append(platform.getStringDataType(40)).append(" NOT NULL, ");
        sql.append(COLUMN_RESULT_MESSAGE).append(" ");
        sql.append(platform.getClobDataType());
        sql.append(", CONSTRAINT PK_").append(COLUMN_INSTANCE_ID);
        sql.append(" PRIMARY KEY (").append(COLUMN_INSTANCE_ID).append(") )");
        SqlUtils.executeUpdate(dataSource, sql.toString());

        // add proper index
        sql = new StringBuffer("CREATE INDEX ");
        sql.append("idx_").append(TABLE_CACHE_INDEX).append("_query ON ");
        sql.append(TABLE_CACHE_INDEX).append(" (");
        sql.append(COLUMN_QUERY_NAME).append(", ");
        sql.append(COLUMN_INSTANCE_CHECKSUM).append(")");
        SqlUtils.executeUpdate(dataSource, sql.toString());
    }

    private void dropCacheTables() throws SQLException {
        // get a list of cache tables
        StringBuffer sql = new StringBuffer("SELECT DISTINCT ");
        sql.append(COLUMN_QUERY_NAME).append(" FROM ").append(TABLE_CACHE_INDEX);

        ResultSet resultSet = null;
        Set<String> queryNames = new LinkedHashSet<String>();
        try {
            resultSet = SqlUtils.executeQuery(dataSource, sql.toString());
            while (resultSet.next()) {
                queryNames.add(resultSet.getString(COLUMN_QUERY_NAME));
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }

        // drop the cache tables
        for (String queryName : queryNames) {
            String cacheTable = normalizeTableName(queryName);
            SqlUtils.executeUpdate(dataSource, "DROP TABLE " + cacheTable);
        }

        // delete rows from cache index table
        SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TABLE_CACHE_INDEX);
    }
}
