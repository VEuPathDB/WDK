/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.Query;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class AnswerFactory {

    static final String TABLE_ANSWER = "answer";

    static final String COLUMN_ANSWER_ID = "answer_id";
    static final String COLUMN_ANSWER_CHECKSUM = "answer_checksum";
    static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_PROJECT_VERSION = "project_version";
    private static final String COLUMN_QUESTION_NAME = "question_name";
    private static final String COLUMN_QUERY_CHECKSUM = "query_checksum";
    private static final String COLUMN_ESTIMATED_SIZE = "estimated_size";
    private static final String COLUMN_PARAMS = "params";
    private static final String COLUMN_RESULT_MESSAGE = "result_message";

    private WdkModel wdkModel;
    private DBPlatform loginPlatform;
    private String answerSchema;

    public AnswerFactory(WdkModel wdkModel) throws SQLException {
        this.wdkModel = wdkModel;
        this.loginPlatform = wdkModel.getAuthenticationPlatform();
        this.answerSchema = wdkModel.getModelConfig().getAnswerSchema();

        // create the answer table if needed
        createTables();
    }

    private void createTables() throws SQLException {
        // check if answer table exists
        if (!loginPlatform.checkTableExists(answerSchema, TABLE_ANSWER))
            createAnswerTable();
    }

    private void createAnswerTable() throws SQLException {
        // construct sql to create the table
        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(answerSchema).append(TABLE_ANSWER).append(" (");
        sql.append(COLUMN_ANSWER_ID).append(" ");
        sql.append(loginPlatform.getNumberDataType(12)).append(" NOT NULL, ");
        sql.append(COLUMN_ANSWER_CHECKSUM).append(" ");
        sql.append(loginPlatform.getStringDataType(40)).append(" NOT NULL, ");
        sql.append(COLUMN_PROJECT_ID).append(" ");
        sql.append(loginPlatform.getStringDataType(50)).append(" NOT NULL, ");
        sql.append(COLUMN_PROJECT_VERSION).append(" ");
        sql.append(loginPlatform.getStringDataType(50)).append(" NOT NULL, ");
        sql.append(COLUMN_QUESTION_NAME).append(" ");
        sql.append(loginPlatform.getStringDataType(200)).append(" NOT NULL, ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(" ");
        sql.append(loginPlatform.getStringDataType(40)).append(" NOT NULL, ");
        sql.append(COLUMN_ESTIMATED_SIZE).append(" ");
        sql.append(loginPlatform.getNumberDataType(12)).append(", ");
        sql.append(COLUMN_PARAMS).append(" ");
        sql.append(loginPlatform.getClobDataType()).append(", ");
        sql.append(COLUMN_RESULT_MESSAGE).append(" ");
        sql.append(loginPlatform.getClobDataType()).append(", ");
        sql.append("CONSTRAINT \"").append(TABLE_ANSWER).append("_PK\" ");
        sql.append("PRIMARY KEY (").append(COLUMN_ANSWER_ID).append("), ");
        sql.append("CONSTRAINT \"").append(TABLE_ANSWER).append("_UQ1\" ");
        sql.append("UNIQUE (").append(COLUMN_PROJECT_ID).append(", ");
        sql.append(COLUMN_ANSWER_CHECKSUM).append(") )");

        // execute the DDL
        DataSource dataSource = loginPlatform.getDataSource();
        SqlUtils.executeUpdate(dataSource, sql.toString());

        // create the sequence for the answer table
        String sequenceName = answerSchema + TABLE_ANSWER
                + DBPlatform.ID_SEQUENCE_SUFFIX;
        loginPlatform.createSequence(sequenceName, 1, 1);
    }

    public AnswerInfo saveAnswer(Answer answer) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // use transaction
        String answerChecksum = answer.getChecksum();

        // check if answer has been saved.
        AnswerInfo answerInfo = getAnswerInfo(answerChecksum);
        if (answerInfo == null) {

            // the answer hasn't been stored, create an answerInfo, and save it
            int answerId = loginPlatform.getNextId(answerSchema, TABLE_ANSWER);
            answerInfo = new AnswerInfo(answerId);
            answerInfo.setAnswerChecksum(answer.getChecksum());
            answerInfo.setEstimatedSize(answer.getResultSize());
            answerInfo.setProjectId(wdkModel.getProjectId());
            answerInfo.setProjectVersion(wdkModel.getVersion());
            answerInfo.setQueryChecksum(answer.getQuestion().getQuery().getChecksum());
            answerInfo.setQuestionName(answer.getQuestion().getFullName());
            answerInfo.setResultMessage(answer.getResultMessage());

            String paramClob = answer.getIdsQueryInstance().getParamJSONObject().toString();
            saveAnswerInfo(answerInfo, paramClob);
        }
        answer.setAnswerInfo(answerInfo);
        return answerInfo;
    }

    public Answer getAnswer(AnswerInfo answerInfo) throws WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException,
            SQLException {
        // get question
        Question question = (Question) wdkModel.resolveReference(answerInfo.getQuestionName());

        // check if the query checksum matches
        Query query = question.getQuery();
        String queryChecksum = query.getChecksum();
        if (!queryChecksum.equals(answerInfo.getQueryChecksum())) {
            throw new WdkModelException("the query checksum in database for "
                    + query.getChecksum() + " does not match the one in the "
                    + "model. The query may have been changed, and the answer "
                    + "is no longer usable.");
        }

        // get and parse the params
        String paramClob = getParamsClob(answerInfo.getAnswerChecksum());
        Map<String, Object> pvalues = parseParams(query.getParamMap(),
                paramClob);

        // create the answer with default page size
        Answer answer = question.makeAnswer(pvalues);
        answer.setAnswerInfo(answerInfo);
        return answer;
    }

    /**
     * @param answerChecksum
     * @return an AnswerInfo object if the answer has been saved; otherwise,
     *         return null.
     * @throws SQLException
     */
    public AnswerInfo getAnswerInfo(String answerChecksum) throws SQLException {
        String projectId = wdkModel.getProjectId();

        // construct the query
        StringBuffer sql = new StringBuffer("SELECT * FROM ");
        sql.append(answerSchema).append(TABLE_ANSWER);
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ? ");
        sql.append(" AND ").append(COLUMN_ANSWER_CHECKSUM).append(" = ?");

        ResultSet resultSet = null;
        PreparedStatement ps = null;
        AnswerInfo answerInfo = null;
        try {
            DataSource dataSource = loginPlatform.getDataSource();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setString(1, projectId);
            ps.setString(2, answerChecksum);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                answerInfo = new AnswerInfo(resultSet.getInt(COLUMN_ANSWER_ID));
                answerInfo.setAnswerChecksum(answerChecksum);
                answerInfo.setEstimatedSize(resultSet.getInt(COLUMN_ESTIMATED_SIZE));
                answerInfo.setProjectId(projectId);
                answerInfo.setProjectVersion(resultSet.getString(COLUMN_PROJECT_VERSION));
                answerInfo.setQueryChecksum(resultSet.getString(COLUMN_QUERY_CHECKSUM));
                answerInfo.setQuestionName(resultSet.getString(COLUMN_QUESTION_NAME));
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        return answerInfo;
    }

    private void saveAnswerInfo(AnswerInfo answerInfo, String paramClob)
            throws SQLException {
        // prepare the sql
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(answerSchema).append(TABLE_ANSWER).append(" (");
        sql.append(COLUMN_ANSWER_ID).append(", ");
        sql.append(COLUMN_ANSWER_CHECKSUM).append(", ");
        sql.append(COLUMN_ESTIMATED_SIZE).append(", ");
        sql.append(COLUMN_PROJECT_ID).append(", ");
        sql.append(COLUMN_PROJECT_VERSION).append(", ");
        sql.append(COLUMN_QUESTION_NAME).append(", ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(", ");
        sql.append(COLUMN_PARAMS).append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        PreparedStatement ps = null;
        try {
            DataSource dataSource = loginPlatform.getDataSource();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setInt(1, answerInfo.getAnswerId());
            ps.setString(2, answerInfo.getAnswerChecksum());
            ps.setInt(3, answerInfo.getEstimatedSize());
            ps.setString(4, answerInfo.getProjectId());
            ps.setString(5, answerInfo.getProjectVersion());
            ps.setString(6, answerInfo.getQuestionName());
            ps.setString(7, answerInfo.getQueryChecksum());
            loginPlatform.updateClobData(ps, 8, paramClob, false);

            ps.executeUpdate();
        } finally {
            SqlUtils.closeStatement(ps);
        }
    }

    private String getParamsClob(String answerChecksum) throws SQLException {
        // construct the sql
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_PARAMS);
        sql.append(" FROM ").append(answerSchema).append(TABLE_ANSWER);
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ?");
        sql.append(" AND ").append(COLUMN_ANSWER_CHECKSUM).append(" = ?");

        ResultSet resultSet = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement(
                    loginPlatform.getDataSource(), sql.toString());
            ps.setString(1, wdkModel.getProjectId());
            ps.setString(2, answerChecksum);
            resultSet = ps.executeQuery();
            if (resultSet.next()) return loginPlatform.getClobData(resultSet,
                    COLUMN_PARAMS);
            else return null;
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
    }

    private Map<String, Object> parseParams(Map<String, Param> params,
            String paramClob) throws JSONException {
        JSONObject jsParams = new JSONObject(paramClob);
        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();
        for (String param : params.keySet()) {
            String value = (jsParams.has(param)) ? jsParams.getString(param)
                    : null;
            paramValues.put(param, value);
        }
        return paramValues;
    }

    String getAnswerTable() {
        return answerSchema + TABLE_ANSWER;
    }
}
