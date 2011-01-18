package org.gusdb.wdk.model.query.param;

import static org.gusdb.wdk.model.dbms.CacheFactory.COLUMN_INSTANCE_CHECKSUM;
import static org.gusdb.wdk.model.dbms.CacheFactory.COLUMN_INSTANCE_ID;
import static org.gusdb.wdk.model.dbms.CacheFactory.COLUMN_QUERY_ID;
import static org.gusdb.wdk.model.dbms.CacheFactory.TABLE_INSTANCE;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.QueryInfo;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class StrategyResultRemoteHandler implements RemoteHandler {

    private static final String PROP_ATTRIBUTES = "attributes";

    private WdkModel wdkModel;
    private String attributeList;

    public void setModel(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public void setProperties(Map<String, String> properties)
            throws WdkModelException {
        if (!properties.containsKey(PROP_ATTRIBUTES))
            throw new WdkModelException("The required property '"
                    + PROP_ATTRIBUTES + "' is not set for "
                    + "StrategyResultRemoteHandler");

        attributeList = properties.get(PROP_ATTRIBUTES);
    }

    public String getResource(User user, Map<String, String> params)
            throws JSONException, WdkModelException {
        String strategyUri = params.get(RemoteListParam.PARAM_RAW_VALUE);
        try {
            // check if cache exists
            QueryInfo cacheInfo = getCacheInfo();
            String content = cacheInfo.getQueryChecksum() + ", " + strategyUri;
            String indexChecksum = Utilities.encrypt(content);
            Integer cacheIndex = getCacheIndex(cacheInfo, indexChecksum);
            if (cacheIndex == null) {
                // cache doesn't exist, get data from remote and cache it
                Client client = Client.create();
                WebResource resource = client.resource(strategyUri);
                String response = resource.queryParam("attributes",
                        attributeList).accept(MediaType.APPLICATION_JSON_TYPE).get(
                        String.class);
                JSONObject jsAnswer = new JSONObject(response);

                cacheIndex = cacheResults(jsAnswer, cacheInfo, indexChecksum);
            }

            // return a SQL that returns the cached results
            return "SELECT * FROM " + cacheInfo.getCacheTable() + " WHERE "
                    + CacheFactory.COLUMN_INSTANCE_ID + " = " + cacheIndex;
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkModelException(ex);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    private int cacheResults(JSONObject jsAnswer, QueryInfo queryInfo,
            String indexChecksum) throws SQLException, WdkModelException,
            JSONException {
        // compose the insert sql
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(queryInfo.getCacheTable() + CacheFactory.COLUMN_INSTANCE_ID);
        JSONArray jsAttributes = jsAnswer.getJSONArray("attributes");
        for (int i = 0; i < jsAttributes.length(); i++) {
            String attribute = jsAttributes.getString(i);
            sql.append(", ").append(attribute);
        }
        sql.append(") VALUES (?");
        for (int i = 0; i < jsAttributes.length(); i++) {
            sql.append(", ?");
        }
        sql.append(")");

        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        PreparedStatement psInsert = null;
        try {
            // create cache index
            int index = createCacheIndex(connection, queryInfo, indexChecksum);

            // then cache the result
            psInsert = connection.prepareStatement(sql.toString());
            JSONArray jsRecords = jsAnswer.getJSONArray("records");
            int row = 0;
            for (; row < jsRecords.length(); row++) {
                psInsert.setInt(1, index);

                JSONObject jsRecord = jsRecords.getJSONObject(row);
                for (int i = 0; i < jsAttributes.length(); i++) {
                    String attribute = jsAttributes.getString(i);
                    String value = jsRecord.getString(attribute);
                    psInsert.setString(i + 2, value);
                }
                psInsert.addBatch();

                if (row % 1000 == 0) psInsert.executeBatch();
            }
            if (row > 0 && (row % 1000 != 0)) psInsert.executeBatch();

            connection.commit();

            return index;
        } catch (Exception ex) {
            connection.rollback();
            throw new WdkModelException(ex);
        } finally {
            connection.setAutoCommit(true);
            SqlUtils.closeStatement(psInsert);
        }
    }

    private QueryInfo getCacheInfo() throws SQLException, JSONException,
            WdkModelException, NoSuchAlgorithmException, WdkUserException {
        String[] attributes = attributeList.split(",\\s*");

        // compute checksum for finding the cache table
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        for (String attribute : attributes) {
            builder.append(",").append(attribute);
        }
        String checksum = Utilities.encrypt(builder.toString());
        String queryName = this.getClass().getName();

        // reuse the wdk query table
        DBPlatform platform = wdkModel.getQueryPlatform();
        CacheFactory cacheFactory = wdkModel.getCacheFactory();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(queryName, checksum);
        String cacheTable = queryInfo.getCacheTable();

        if (!platform.checkTableExists(null, cacheTable)) {
            // cache table doesn't exist, create it
            StringBuilder sql = new StringBuilder("CREATE TABLE ");
            sql.append(cacheTable + " AS (");
            sql.append(COLUMN_INSTANCE_ID + " "
                    + platform.getNumberDataType(12));
            for (String attribute : attributes) {
                sql.append(", " + attribute + " VARCHAR(1999)");
            }
            sql.append(")");

            DataSource dataSource = platform.getDataSource();
            SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                    "remote-handler-strategy-create-cache");

            SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX "
                    + cacheTable + "_ix01 ON " + cacheTable + " ("
                    + COLUMN_INSTANCE_ID + ")",
                    "remote-handler-strategy-create-index");
        }

        return queryInfo;
    }

    private Integer getCacheIndex(QueryInfo queryInfo, String indexChecksum)
            throws SQLException {
        // check if cache exists
        String sql = "SELECT " + COLUMN_INSTANCE_ID + " FROM " + TABLE_INSTANCE
                + " WHERE " + COLUMN_QUERY_ID + " = ? AND "
                + COLUMN_INSTANCE_CHECKSUM + " = ?";
        ResultSet resultSet = null;
        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        try {
            PreparedStatement psSelect = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psSelect.setInt(1, queryInfo.getQueryId());
            psSelect.setString(2, indexChecksum);
            resultSet = psSelect.executeQuery();
            if (resultSet.next()) { // cache index exists, use it
                return resultSet.getInt(COLUMN_INSTANCE_ID);
            } else { // cache index doesn't exists
                return null;
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
    }

    private int createCacheIndex(Connection connection, QueryInfo queryInfo,
            String indexChecksum) throws WdkModelException, WdkUserException,
            SQLException, NoSuchAlgorithmException, JSONException {
        // get a new index
        DBPlatform platform = wdkModel.getQueryPlatform();
        int index = platform.getNextId(null, CacheFactory.TABLE_INSTANCE);

        // insert a new row into the queryinstance table;
        ResultFactory resultFactory = wdkModel.getResultFactory();
        resultFactory.addCacheInstance(connection, queryInfo, index,
                indexChecksum, "");
        return index;
    }
}
