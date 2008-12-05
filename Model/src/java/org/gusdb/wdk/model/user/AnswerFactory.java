/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Utilities;
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
    static final String COLUMN_QUESTION_NAME = "question_name";
    private static final String COLUMN_PROJECT_VERSION = "project_version";
    private static final String COLUMN_QUERY_CHECKSUM = "query_checksum";
    private static final String COLUMN_PARAMS = "params";
    private static final String COLUMN_RESULT_MESSAGE = "result_message";

    private WdkModel wdkModel;
    private DBPlatform loginPlatform;
    private String answerSchema;

    public AnswerFactory(WdkModel wdkModel) throws SQLException {
        this.wdkModel = wdkModel;
        this.loginPlatform = wdkModel.getAuthenticationPlatform();
        this.answerSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    }

    public AnswerInfo saveAnswer(Answer answer) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // use transaction
        String answerChecksum = answer.getChecksum();

        // check if answer has been saved.
        AnswerInfo answerInfo = getAnswerInfo(answerChecksum);
        if (answerInfo == null) {
            Question question = answer.getQuestion();
            // the answer hasn't been stored, create an answerInfo, and save it
            int answerId = loginPlatform.getNextId(answerSchema, TABLE_ANSWER);
            answerInfo = new AnswerInfo(answerId);
            answerInfo.setAnswerChecksum(answer.getChecksum());
            answerInfo.setProjectId(wdkModel.getProjectId());
            answerInfo.setProjectVersion(wdkModel.getVersion());
            answerInfo.setQueryChecksum(question.getQuery().getChecksum());
            answerInfo.setQuestionName(question.getFullName());
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
        String savedChecksum = answerInfo.getQueryChecksum();
        if (!queryChecksum.equals(savedChecksum)) {
            throw new WdkModelException("the query checksum in database for "
                    + savedChecksum + " does not match the one in the "
                    + "model (" + queryChecksum + "). The query may have been "
                    + "changed, and the answer is no longer usable.");
        }

        // get and parse the params
        Map<String, Object> pvalues = getParams(answerInfo.getAnswerChecksum());

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
        sql.append(COLUMN_PROJECT_ID).append(", ");
        sql.append(COLUMN_PROJECT_VERSION).append(", ");
        sql.append(COLUMN_QUESTION_NAME).append(", ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(", ");
        sql.append(COLUMN_PARAMS).append(") VALUES (?, ?, ?, ?, ?, ?, ?)");

        PreparedStatement ps = null;
        try {
            DataSource dataSource = loginPlatform.getDataSource();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setInt(1, answerInfo.getAnswerId());
            ps.setString(2, answerInfo.getAnswerChecksum());
            ps.setString(3, answerInfo.getProjectId());
            ps.setString(4, answerInfo.getProjectVersion());
            ps.setString(5, answerInfo.getQuestionName());
            ps.setString(6, answerInfo.getQueryChecksum());
            loginPlatform.updateClobData(ps, 7, paramClob, false);

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

    Map<String, Object> getParams(String answerChecksum) throws JSONException,
            SQLException {
        String paramClob = getParamsClob(answerChecksum);

        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();
        if (paramClob == null) {
            // nothing to parse with
        } else if (!paramClob.startsWith("{")) {
            // boolean answer of the old form, the last part should be the expression
            String[] parts = paramClob.split(Utilities.DATA_DIVIDER);
            String expression = parts[parts.length - 1].trim();
            paramValues.put(BooleanExpression.BOOLEAN_EXPRESSION, expression);
        } else {
            JSONObject jsParams = new JSONObject(paramClob);
            Iterator<?> keys = jsParams.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = jsParams.get(key).toString();
                paramValues.put(key, value);
            }
        }
        return paramValues;
    }

    String getAnswerTable() {
        return answerSchema + TABLE_ANSWER;
    }
}
