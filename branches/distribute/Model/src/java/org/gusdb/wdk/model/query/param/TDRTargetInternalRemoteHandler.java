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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
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
import org.json.JSONException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class TDRTargetInternalRemoteHandler implements RemoteHandler {

    private static final String ATTRIBUTE_SOURCE_ID = "source_id";

    private static final Logger logger = Logger.getLogger(TDRTargetInternalRemoteHandler.class);

    private WdkModel wdkModel;

    public void setModel(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public void setProperties(Map<String, String> properties) {
    // do nothing
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
                String response = resource.accept(MediaType.TEXT_HTML_TYPE).get(
                        String.class);

                logger.debug("TDR remote query: " + strategyUri);

                List<String> genes = parseContent(response);

                cacheIndex = cacheResults(genes, cacheInfo, indexChecksum);
            }

            // return a SQL that returns the cached results
            return "(SELECT * FROM " + cacheInfo.getCacheTable() + " WHERE "
                    + CacheFactory.COLUMN_INSTANCE_ID + " = " + cacheIndex
                    + ")";
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkModelException(ex);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    private List<String> parseContent(String response) {
        // find the begin & end of the data section
        int begin = response.indexOf("<table class=\"list\">");
        int end = response.indexOf("</table>", begin);
        response = response.substring(begin, end);

        List<String> genes = new ArrayList<String>();

        Pattern pattern = Pattern.compile("</td>.*?<a [^>]*>(.+?)</a>");
        begin = 0;
        while (true) {
            begin = response.indexOf("<tr>", begin);
            if (begin < 0) break;

            end = response.indexOf("</tr>", begin);
            String content = response.substring(begin, end);
            content = content.replaceAll("\\s+", " ");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String sourceId = matcher.group(1).trim();
                genes.add(sourceId);
            }

            begin = end;
        }
        return genes;
    }

    private int cacheResults(List<String> genes, QueryInfo queryInfo,
            String indexChecksum) throws SQLException, WdkModelException,
            JSONException {
        // compose the insert sql
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(queryInfo.getCacheTable() + " (");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID + ", ");
        sql.append(ATTRIBUTE_SOURCE_ID + ") VALUES (?, ?)");

        logger.debug("INSERT SQL:\n" + sql);

        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        PreparedStatement psInsert = null;
        try {
            // create cache index
            int index = createCacheIndex(connection, queryInfo, indexChecksum);

            // then cache the result
            psInsert = connection.prepareStatement(sql.toString());
            int row = 0;
            for (; row < genes.size(); row++) {
                psInsert.setInt(1, index);
                psInsert.setString(2, genes.get(row));
                psInsert.addBatch();

                if (row % 1000 == 0) psInsert.executeBatch();
            }
            if (row > 0 && (row % 1000 != 0)) psInsert.executeBatch();

            logger.debug(row + " records inserted.");

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
        // compute checksum for finding the cache table
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
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
            sql.append(cacheTable + " (");
            sql.append(COLUMN_INSTANCE_ID + " "
                    + platform.getNumberDataType(12));
            sql.append(", " + ATTRIBUTE_SOURCE_ID + " VARCHAR(1999))");

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
