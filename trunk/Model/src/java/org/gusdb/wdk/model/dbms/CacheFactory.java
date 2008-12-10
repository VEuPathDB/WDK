/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.json.JSONException;

/**
 * @author Jerric Gao
 * 
 */
public class CacheFactory {

    private static final String CACHE_TABLE_PREFIX = "QueryResult";

    private static final String TABLE_QUERY = "Query";

    static final String COLUMN_QUERY_ID = "query_id";
    private static final String COLUMN_QUERY_NAME = "query_name";
    private static final String COLUMN_QUERY_CHECKSUM = "query_checksum";
    private static final String COLUMN_TABLE_NAME = "table_name";

    static final String TABLE_INSTANCE = "QueryInstance";

    public static final String COLUMN_INSTANCE_ID = "instance_id";
    static final String COLUMN_INSTANCE_CHECKSUM = "instance_checksum";
    static final String COLUMN_RESULT_MESSAGE = "result_message";

    private static Logger logger = Logger.getLogger(CacheFactory.class);

    private WdkModel wdkModel;
    private DBPlatform platform;
    private DataSource dataSource;

    private Map<String, QueryInfo> queryInfoMap;

    public CacheFactory(WdkModel wdkModel, DBPlatform platform)
            throws SQLException {
        this.wdkModel = wdkModel;
        this.platform = platform;
        this.dataSource = platform.getDataSource();
        queryInfoMap = new LinkedHashMap<String, QueryInfo>();
    }

    public void createCache() {
        // create index tables for query and query instance;
        createQueryTable();
        createQueryInstanceTable();

        // create the id sequence for the query & instance index
        String sequenceName = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
        try {
            platform.createSequence(sequenceName, 1, 1);
        } catch (SQLException ex) {
            logger.error("Cannot create sequence [" + sequenceName + "]. "
                    + ex.getMessage());
        }

        sequenceName = TABLE_QUERY + DBPlatform.ID_SEQUENCE_SUFFIX;
        try {
            platform.createSequence(sequenceName, 1, 1);
        } catch (SQLException ex) {
            logger.error("Cannot create sequence [" + sequenceName + "]. "
                    + ex.getMessage());
        }
    }

    public void resetCache() {
        // drop cache tables and we are done
        dropCacheTables();
    }

    public void recreateCache() {
        // drop cache;
        dropCache();
        // create them back
        createCache();
    }

    public void dropCache() {
        // drop cache tables
        dropCacheTables();

        try {
            SqlUtils.executeUpdate(dataSource, "DROP TABLE " + TABLE_INSTANCE);
        } catch (SQLException ex) {
            logger.error("Cannot drop table [" + TABLE_INSTANCE + "]. "
                    + ex.getMessage());
        }
        String instanceSeq = TABLE_INSTANCE + DBPlatform.ID_SEQUENCE_SUFFIX;
        try {
            SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + instanceSeq);
        } catch (SQLException ex) {
            logger.error("Cannot drop sequence [" + instanceSeq + "]. "
                    + ex.getMessage());
        }

        // drop index tables and sequences
        try {
            SqlUtils.executeUpdate(dataSource, "DROP TABLE " + TABLE_QUERY);
        } catch (SQLException ex) {
            logger.error("Cannot drop table [" + TABLE_QUERY + "]. "
                    + ex.getMessage());
        }

        String querySeq = TABLE_QUERY + DBPlatform.ID_SEQUENCE_SUFFIX;
        try {
            SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + querySeq);
        } catch (SQLException ex) {
            logger.error("Cannot drop sequence [" + querySeq + "]. "
                    + ex.getMessage());
        }
    }

    public void dropCache(int instanceId) {
        String whereClause = " WHERE " + COLUMN_INSTANCE_ID + " = "
                + instanceId;

        // get the query name from the id
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_QUERY_NAME);
        sql.append(" FROM ");
        sql.append(TABLE_INSTANCE);
        sql.append(whereClause);

        Query query;
        try {
            String queryName = (String) SqlUtils.executeScalar(dataSource,
                    sql.toString());
            query = (Query) wdkModel.resolveReference(queryName);
        } catch (Exception ex) {
            // cannot get query name or resolve query, cancel remaining steps.
            logger.warn(ex);
            return;
        }

        // get cache table name
        String cacheTable;
        try {
            QueryInfo queryInfo = getQueryInfo(query);
            cacheTable = queryInfo.getCacheTable();
        } catch (Exception ex) {
            logger.error("Cannot get cache table for query ["
                    + query.getFullName() + "]. " + ex.getMessage());
            return;
        }

        sql = new StringBuffer("DELETE FROM ");
        sql.append(cacheTable);
        sql.append(whereClause);
        try {
            SqlUtils.executeUpdate(dataSource, sql.toString());
        } catch (SQLException ex) {
            logger.error("Cannot delete rows from [" + cacheTable + "]. "
                    + ex.getMessage());
        }

        // delete the instance index
        sql = new StringBuffer("DELETE FROM ");
        sql.append(TABLE_INSTANCE);
        sql.append(whereClause);
        try {
            SqlUtils.executeUpdate(dataSource, sql.toString());
        } catch (SQLException ex) {
            logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. "
                    + ex.getMessage());
        }
    }

    public void dropCache(String queryName) {
        String cacheTable;
        try {
            Query query = (Query) wdkModel.resolveReference(queryName);
            QueryInfo queryInfo = getQueryInfo(query);
            cacheTable = queryInfo.getCacheTable();
        } catch (Exception ex) {
            logger.error("Cannot get cache table for query [" + queryName
                    + "]. " + ex.getMessage());
            return;
        }

        // drop the cacheTable
        try {
            SqlUtils.executeUpdate(dataSource, "DROP TABLE " + cacheTable);
        } catch (SQLException ex) {
            logger.error("Cannot drop table [" + cacheTable + "]. "
                    + ex.getMessage());
        }

        // delete instance index
        StringBuffer sqlInstance = new StringBuffer("DELETE FROM ");
        sqlInstance.append(TABLE_INSTANCE);
        sqlInstance.append(" WHERE ").append(COLUMN_QUERY_ID).append(" IN ");
        sqlInstance.append("(SELECT ").append(COLUMN_INSTANCE_ID);
        sqlInstance.append(" FROM ").append(TABLE_QUERY);
        sqlInstance.append(" WHERE ").append(COLUMN_TABLE_NAME).append(" = ?)");

        PreparedStatement stInstance = null;
        try {
            stInstance = SqlUtils.getPreparedStatement(dataSource,
                    sqlInstance.toString());
            stInstance.setString(1, cacheTable);
            stInstance.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. "
                    + ex.getMessage());
        } finally {
            try {
                SqlUtils.closeStatement(stInstance);
            } catch (SQLException ex) {
                // do nothing
            }
        }

        // delete query index
        StringBuffer sqlQuery = new StringBuffer("DELETE FROM ");
        sqlQuery.append(TABLE_QUERY);
        sqlQuery.append(" WHERE ").append(COLUMN_TABLE_NAME);
        sqlQuery.append(" = '").append(cacheTable).append("'");

        try {
            SqlUtils.executeUpdate(dataSource, sqlQuery.toString());
        } catch (SQLException ex) {
            logger.error("Cannot delete rows from [" + TABLE_QUERY + "]. "
                    + ex.getMessage());
        } finally {}
    }

    private void createQueryTable() {
        // create the cache index table
        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(TABLE_QUERY).append(" ( ");
        sql.append(COLUMN_QUERY_ID).append(" ");
        sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
        sql.append(COLUMN_QUERY_NAME).append(" ");
        sql.append(platform.getStringDataType(200)).append(" NOT NULL, ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(" ");
        sql.append(platform.getStringDataType(40)).append(" NOT NULL, ");
        sql.append(COLUMN_TABLE_NAME).append(" ");
        sql.append(platform.getStringDataType(30)).append(" NOT NULL, ");
        sql.append(" CONSTRAINT PK_").append(COLUMN_QUERY_ID);
        sql.append(" PRIMARY KEY (").append(COLUMN_QUERY_ID).append("), ");
        sql.append(" CONSTRAINT UK_").append(COLUMN_QUERY_NAME);
        sql.append(" UNIQUE (").append(COLUMN_QUERY_NAME).append(", ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(") )");
        try {
            SqlUtils.executeUpdate(dataSource, sql.toString());
        } catch (SQLException ex) {
            logger.error("Cannot create table [" + TABLE_QUERY + "]. "
                    + ex.getMessage());
        }
    }

    private void createQueryInstanceTable() {
        // create the cache index table
        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(TABLE_INSTANCE).append(" ( ");

        // define columns
        sql.append(COLUMN_INSTANCE_ID).append(" ");
        sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
        sql.append(COLUMN_QUERY_ID).append(" ");
        sql.append(platform.getNumberDataType(12)).append(" NOT NULL, ");
        sql.append(COLUMN_INSTANCE_CHECKSUM).append(" ");
        sql.append(platform.getStringDataType(40)).append(" NOT NULL, ");
        sql.append(COLUMN_RESULT_MESSAGE).append(" ");
        sql.append(platform.getClobDataType());

        // define primary key
        sql.append(", CONSTRAINT PK_").append(COLUMN_INSTANCE_ID);
        sql.append(" PRIMARY KEY (").append(COLUMN_INSTANCE_ID).append("), ");

        // define foreign key to Query table
        sql.append(" CONSTRAINT FK_").append(COLUMN_QUERY_ID);
        sql.append(" FOREIGN KEY (").append(COLUMN_QUERY_ID).append(")");
        sql.append(" REFERENCES ").append(TABLE_QUERY);
        sql.append("(").append(COLUMN_QUERY_ID).append("), ");

        // define unique constraint to (query_id, instance_checksum) tuple
        sql.append(" CONSTRAINT UK_").append(COLUMN_QUERY_ID);
        sql.append(" UNIQUE (").append(COLUMN_QUERY_ID).append(", ");
        sql.append(COLUMN_INSTANCE_CHECKSUM).append(") )");
        try {
            SqlUtils.executeUpdate(dataSource, sql.toString());
        } catch (SQLException ex) {
            logger.error("Cannot create table [" + TABLE_INSTANCE + "]. "
                    + ex.getMessage());
        }
    }

    private void dropCacheTables() {
        queryInfoMap.clear();
        
        // get a list of cache tables
        StringBuffer sql = new StringBuffer("SELECT DISTINCT ");
        sql.append(COLUMN_TABLE_NAME).append(" FROM ").append(TABLE_QUERY);

        ResultSet resultSet = null;
        Set<String> cacheTables = new LinkedHashSet<String>();
        try {
            resultSet = SqlUtils.executeQuery(dataSource, sql.toString());
            while (resultSet.next()) {
                cacheTables.add(resultSet.getString(COLUMN_TABLE_NAME));
            }
        } catch (SQLException ex) {
            logger.error("Cannot query on table [" + TABLE_QUERY + "]. "
                    + ex.getMessage());
        } finally {
            try {
                SqlUtils.closeResultSet(resultSet);
            } catch (SQLException ex) {
                // do nothing
            }
        }

        // drop the cache tables
        for (String cacheTable : cacheTables) {
            try {
                SqlUtils.executeUpdate(dataSource, "DROP TABLE " + cacheTable);
            } catch (SQLException ex) {
                logger.error("Cannot drop table [" + cacheTable + "]. "
                        + ex.getMessage());
            }
        }

        // delete rows from cache index table
        try {
            SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TABLE_INSTANCE);
        } catch (SQLException ex) {
            logger.error("Cannot delete rows from [" + TABLE_INSTANCE + "]. "
                    + ex.getMessage());
        }
        try {
            SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TABLE_QUERY);
        } catch (SQLException ex) {
            logger.error("Cannot delete rows from [" + TABLE_QUERY + "]. "
                    + ex.getMessage());
        }
    }

    public synchronized QueryInfo getQueryInfo(Query query)
            throws SQLException, NoSuchAlgorithmException, JSONException,
            WdkModelException {
        QueryInfo queryInfo = checkQueryInfo(query);
        if (queryInfo != null) return queryInfo;

        // cache table doesn't exist, create one
        queryInfo = new QueryInfo();
        queryInfo.setQueryId(platform.getNextId(null, TABLE_QUERY));
        queryInfo.setCacheTable(CACHE_TABLE_PREFIX + queryInfo.getQueryId());
        queryInfo.setQueryName(query.getFullName());
        queryInfo.setQueryChecksum(query.getChecksum());

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(TABLE_QUERY).append(" (");
        sql.append(COLUMN_QUERY_ID).append(", ");
        sql.append(COLUMN_QUERY_NAME).append(", ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(", ");
        sql.append(COLUMN_TABLE_NAME).append(") ");
        sql.append("VALUES (?, ?, ?, ?)");

        PreparedStatement psInsert = null;
        try {
            psInsert = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            psInsert.setInt(1, queryInfo.getQueryId());
            psInsert.setString(2, queryInfo.getQueryName());
            psInsert.setString(3, queryInfo.getQueryChecksum());
            psInsert.setString(4, queryInfo.getCacheTable());
            psInsert.executeUpdate();
        } finally {
            SqlUtils.closeStatement(psInsert);
        }
        String queryKey = getQueryKey(query.getFullName(), query.getChecksum());
        queryInfoMap.put(queryKey, queryInfo);
        return queryInfo;
    }

    private QueryInfo checkQueryInfo(Query query)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            SQLException {
        String queryName = query.getFullName();
        String queryChecksum = query.getChecksum();

        // check if the query table has been seen before
        String queryKey = getQueryKey(queryName, queryChecksum);
        QueryInfo queryInfo = queryInfoMap.get(queryKey);
        if (queryInfo != null) return queryInfo;

        StringBuffer sql = new StringBuffer("SELECT * FROM ");
        sql.append(TABLE_QUERY);
        sql.append(" WHERE ").append(COLUMN_QUERY_NAME).append(" = ?");
        sql.append(" AND ").append(COLUMN_QUERY_CHECKSUM).append(" = ?");

        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setString(1, queryName);
            ps.setString(2, queryChecksum);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                queryInfo = new QueryInfo();
                queryInfo.setQueryName(queryName);
                queryInfo.setQueryChecksum(queryChecksum);
                queryInfo.setQueryId(resultSet.getInt(COLUMN_QUERY_ID));
                queryInfo.setCacheTable(resultSet.getString(COLUMN_TABLE_NAME));

                queryInfoMap.put(queryKey, queryInfo);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
            if (resultSet == null) SqlUtils.closeStatement(ps);
        }
        return queryInfo;
    }

    private String getQueryKey(String queryName, String queryChecksum) {
        return queryName + queryChecksum;
    }
}
