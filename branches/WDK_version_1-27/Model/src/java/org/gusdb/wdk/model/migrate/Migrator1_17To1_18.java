/**
 * 
 */
package org.gusdb.wdk.model.migrate;

import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class Migrator1_17To1_18 extends Migrator {

    private static final String OLD_USER_SCHEMA = "userlogins2.";
    private static final String NEW_USER_SCHEMA = "userlogins3.";
    private static final String NEW_WDK_SCHEMA = "wdkstorage.";

    private PreparedStatement psInsertAnswer;
    private PreparedStatement psInsertHistory;

    private Map<String, Integer> answerKeys;
    private Set<String> historyKeys;

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.migrate.Migrator#migrate()
     */
    @Override
    public void migrate() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        migrateHistories();
    }

    private void migrateHistories() throws SQLException, JSONException,
            WdkModelException, WdkUserException {
        DBPlatform userPlatform = wdkModel.getUserPlatform();
        DataSource dataSource = userPlatform.getDataSource();

        System.out.println("Loading existing histories...");
        loadHistories(dataSource);

        System.out.println("Loading existing answers...");
        loadAnswers(dataSource);

        System.out.println("Loading old histories...");
        prepareStatements(dataSource);

        ResultSet histories = getHistories(dataSource);
        int count = 0;
        System.out.println("Migrating old histories...");
        while (histories.next()) {
            int userId = histories.getInt("user_id");
            int historyId = histories.getInt("history_id");
            String projectId = histories.getString("project_id");
            String answerChecksum = histories.getString("query_instance_checksum");
            String questionName = histories.getString("question_name");
            String queryChecksum = histories.getString("query_signature");
            Date createTime = histories.getDate("create_time");
            Date lastRunTime = histories.getDate("last_run_time");
            String customName = histories.getString("custom_name");
            int estimateSize = histories.getInt("estimate_size");
            boolean isBoolean = histories.getBoolean("is_boolean");
            boolean isDeleted = histories.getBoolean("is_deleted");
            String params = userPlatform.getClobData(histories, "params");
            String convertedParams = convertParams(params, isBoolean);

            // check if history exists
            String historyKey = userId + "_" + historyId;
            if (historyKeys.contains(historyKey)) continue;

            // answer if exists
            String answerKey = projectId + "_" + answerChecksum;
            Integer answerId = answerKeys.get(answerKey);

            if (answerId == null) {
                // answer doesn't exist, save new answer
                answerId = insertAnswer(userPlatform, answerChecksum,
                        projectId, questionName, queryChecksum, convertedParams);
                answerKeys.put(answerKey, answerId);
            }

            // save history
            String displayParams = isBoolean ? params : convertedParams;
            insertHistory(userPlatform, userId, historyId, answerId,
                    createTime, lastRunTime, estimateSize, customName,
                    isBoolean, isDeleted, displayParams);
            historyKeys.add(historyKey);

            count++;

            if (count % 10 == 0)
                System.out.println("Migrated " + count + " histories...");
        }
        System.out.println("Totally migrated " + count + " histories.");

        SqlUtils.closeResultSet(histories);
        SqlUtils.closeStatement(psInsertAnswer);
        SqlUtils.closeStatement(psInsertHistory);
    }

    private void prepareStatements(DataSource dataSource) throws SQLException {
        // prepare insert answer statement
        StringBuffer sqlInsertAnswer = new StringBuffer("INSERT INTO ");
        sqlInsertAnswer.append(NEW_WDK_SCHEMA).append("answer (");
        sqlInsertAnswer.append("answer_id, answer_checksum, project_id, ");
        sqlInsertAnswer.append("project_version, question_name, ");
        sqlInsertAnswer.append("query_checksum, params) ");
        sqlInsertAnswer.append("VALUES (?, ?, ?, '1.0', ?, ?, ?)");
        psInsertAnswer = SqlUtils.getPreparedStatement(dataSource,
                sqlInsertAnswer.toString());

        // prepare insert history statement
        StringBuffer sqlInsertHistory = new StringBuffer("INSERT INTO ");
        sqlInsertHistory.append(NEW_USER_SCHEMA).append("histories (");
        sqlInsertHistory.append("history_id, user_id, answer_id, create_time, ");
        sqlInsertHistory.append("last_run_time, estimate_size, custom_name, ");
        sqlInsertHistory.append("is_boolean, is_deleted, display_params) ");
        sqlInsertHistory.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        psInsertHistory = SqlUtils.getPreparedStatement(dataSource,
                sqlInsertHistory.toString());
    }

    private ResultSet getHistories(DataSource dataSource) throws SQLException,
            WdkUserException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT u3.user_id, ");
        sql.append("h2.history_id, h2.project_id, h2.query_instance_checksum,");
        sql.append(" h2.question_name, h2.query_signature, h2.create_time, ");
        sql.append("h2.last_run_time, h2.custom_name, h2.estimate_size, ");
        sql.append("h2.is_boolean, h2.is_deleted, h2.params FROM ");
        sql.append(OLD_USER_SCHEMA).append("histories h2, ");
        sql.append(NEW_USER_SCHEMA).append("users u3 ");
        sql.append("WHERE u3.prev_user_id = h2.user_id ");
        sql.append("ORDER BY h2.create_time DESC");

        return SqlUtils.executeQuery(wdkModel, dataSource, sql.toString(),
                "wdk-migrate-select-histories");
    }

    private void loadHistories(DataSource dataSource) throws SQLException,
            WdkUserException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT user_id, history_id FROM ");
        sql.append(NEW_USER_SCHEMA).append("histories ");
        historyKeys = new LinkedHashSet<String>();
        ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource,
                sql.toString(), "wdk-migrate-select-history-ids");
        while (resultSet.next()) {
            int userId = resultSet.getInt("user_id");
            int historyId = resultSet.getInt("history_id");
            String historyKey = userId + "_" + historyId;
            historyKeys.add(historyKey);
        }
        SqlUtils.closeResultSet(resultSet);
    }

    private void loadAnswers(DataSource dataSource) throws SQLException,
            WdkUserException, WdkModelException {
        StringBuffer sql = new StringBuffer(
                "SELECT answer_id, answer_checksum,");
        sql.append(" project_id FROM ").append(NEW_WDK_SCHEMA).append("answer ");
        answerKeys = new LinkedHashMap<String, Integer>();
        ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource,
                sql.toString(), "wdk-migrate-select-answer-ids");
        while (resultSet.next()) {
            int answerId = resultSet.getInt("answer_id");
            String projectId = resultSet.getString("project_id");
            String answerChecksum = resultSet.getString("answer_checksum");
            String answerKey = projectId + "_" + answerChecksum;
            answerKeys.put(answerKey, answerId);
        }
        SqlUtils.closeResultSet(resultSet);
    }

    private String convertParams(String params, boolean isBoolean)
            throws JSONException {
        if (isBoolean) return params; // cannot convert the params

        String[] parts = params.split(Pattern.quote("--WDK_DATA_DIVIDER--"));
        JSONObject jsParams = new JSONObject();
        for (String part : parts) {
            int pos = part.indexOf('=');
            if (pos <= 0) continue;
            String param = part.substring(0, pos).trim();
            String value = part.substring(pos + 1).trim();
            jsParams.put(param, value);
        }
        return jsParams.toString();
    }

    private int insertAnswer(DBPlatform platform, String answerChecksum,
            String projectId, String questionName, String queryChecksum,
            String params) throws SQLException, WdkModelException,
            WdkUserException {
        int answerId = platform.getNextId(NEW_WDK_SCHEMA, "answer");

        psInsertAnswer.setInt(1, answerId);
        psInsertAnswer.setString(2, answerChecksum);
        psInsertAnswer.setString(3, projectId);
        psInsertAnswer.setString(4, questionName);
        psInsertAnswer.setString(5, queryChecksum);
        platform.setClobData(psInsertAnswer, 6, params, true);

        return answerId;
    }

    private void insertHistory(DBPlatform platform, int userId, int historyId,
            int answerId, Date createTime, Date lastRunTime, int estimateSize,
            String customName, boolean isBoolean, boolean isDeleted,
            String displayParams) throws SQLException {
        psInsertHistory.setInt(1, historyId);
        psInsertHistory.setInt(2, userId);
        psInsertHistory.setInt(3, answerId);
        psInsertHistory.setDate(4, createTime);
        psInsertHistory.setDate(5, lastRunTime);
        psInsertHistory.setInt(6, estimateSize);
        psInsertHistory.setString(7, customName);
        psInsertHistory.setBoolean(8, isBoolean);
        psInsertHistory.setBoolean(9, isDeleted);
        platform.setClobData(psInsertHistory, 10, displayParams, true);
    }
}
