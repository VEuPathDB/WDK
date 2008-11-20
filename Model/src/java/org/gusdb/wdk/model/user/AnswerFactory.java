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

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class AnswerFactory {

    static final String TABLE_ANSWER = "answers";

    static final String COLUMN_ANSWER_ID = "answer_id";
    static final String COLUMN_ANSWER_CHECKSUM = "answer_checksum";
    static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_PROJECT_VERSION = "project_version";
    private static final String COLUMN_QUESTION_NAME = "question_name";
    private static final String COLUMN_QUERY_CHECKSUM = "query_checksum";
    private static final String COLUMN_PARAMS = "params";

    private WdkModel wdkModel;
    private DBPlatform userPlatform;
    private String wdkSchema;

    public AnswerFactory(WdkModel wdkModel) throws SQLException {
        this.wdkModel = wdkModel;
        this.userPlatform = wdkModel.getUserPlatform();
        this.wdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    }

    public Answer saveAnswerValue(AnswerValue answerValue) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // use transaction
        String answerChecksum = answerValue.getChecksum();

        // check if answer has been saved.
        Answer answer = getAnswer(answerChecksum);
        if (answer == null) {
            Question question = answerValue.getQuestion();
            // the answer hasn't been stored, create an answerInfo, and save it
            int answerId = userPlatform.getNextId(wdkSchema, TABLE_ANSWER);
            answer = new Answer(this, answerId);
            answer.setAnswerChecksum(answerValue.getChecksum());
            answer.setProjectId(wdkModel.getProjectId());
            answer.setProjectVersion(wdkModel.getVersion());
            answer.setQueryChecksum(question.getQuery().getChecksum());
            answer.setQuestionName(question.getFullName());

            String paramClob = answerValue.getIdsQueryInstance().getParamJSONObject().toString();
            saveAnswer(answer, paramClob);
        }
        answerValue.setAnswerInfo(answer);
        return answer;
    }

    AnswerValue getAnswerValue(Answer answer) throws WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException,
            SQLException {
        // get question
        Question question = (Question) wdkModel.resolveReference(answer.getQuestionName());

        // check if the query checksum matches
        Query query = question.getQuery();
        String queryChecksum = query.getChecksum();
        String savedChecksum = answer.getQueryChecksum();
        if (!queryChecksum.equals(savedChecksum)) {
            throw new WdkModelException("the query checksum in database for "
                    + savedChecksum + " does not match the one in the "
                    + "model (" + queryChecksum + "). The query may have been "
                    + "changed, and the answer is no longer usable.");
        }

        // get and parse the params
        String paramClob = getParamsClob(answer.getAnswerChecksum());
        Map<String, String> pvalues = parseParams(query.getParamMap(),
                paramClob);

        // create the answer with default page size
        AnswerValue answerValue = question.makeAnswerValue(pvalues);
        answerValue.setAnswerInfo(answer);
        return answerValue;
    }

    /**
     * @param answerChecksum
     * @return an AnswerInfo object if the answer has been saved; otherwise,
     *         return null.
     * @throws SQLException
     */
    public Answer getAnswer(String answerChecksum) throws SQLException {
        String projectId = wdkModel.getProjectId();

        // construct the query
        StringBuffer sql = new StringBuffer("SELECT * FROM ");
        sql.append(wdkSchema).append(TABLE_ANSWER);
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ? ");
        sql.append(" AND ").append(COLUMN_ANSWER_CHECKSUM).append(" = ?");

        ResultSet resultSet = null;
        PreparedStatement ps = null;
        Answer answer = null;
        try {
            DataSource dataSource = userPlatform.getDataSource();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setString(1, projectId);
            ps.setString(2, answerChecksum);
            resultSet = ps.executeQuery();

            if (resultSet.next()) {
                answer = new Answer(this, resultSet.getInt(COLUMN_ANSWER_ID));
                answer.setAnswerChecksum(answerChecksum);
                answer.setProjectId(projectId);
                answer.setProjectVersion(resultSet.getString(COLUMN_PROJECT_VERSION));
                answer.setQueryChecksum(resultSet.getString(COLUMN_QUERY_CHECKSUM));
                answer.setQuestionName(resultSet.getString(COLUMN_QUESTION_NAME));
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        return answer;
    }

    private void saveAnswer(Answer answer, String paramClob)
            throws SQLException {
        // prepare the sql
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(wdkSchema).append(TABLE_ANSWER).append(" (");
        sql.append(COLUMN_ANSWER_ID).append(", ");
        sql.append(COLUMN_ANSWER_CHECKSUM).append(", ");
        sql.append(COLUMN_PROJECT_ID).append(", ");
        sql.append(COLUMN_PROJECT_VERSION).append(", ");
        sql.append(COLUMN_QUESTION_NAME).append(", ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(", ");
        sql.append(COLUMN_PARAMS).append(") VALUES (?, ?, ?, ?, ?, ?, ?)");

        PreparedStatement ps = null;
        try {
            DataSource dataSource = userPlatform.getDataSource();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setInt(1, answer.getAnswerId());
            ps.setString(2, answer.getAnswerChecksum());
            ps.setString(3, answer.getProjectId());
            ps.setString(4, answer.getProjectVersion());
            ps.setString(5, answer.getQuestionName());
            ps.setString(6, answer.getQueryChecksum());
            userPlatform.updateClobData(ps, 7, paramClob, false);

            ps.executeUpdate();
        } finally {
            SqlUtils.closeStatement(ps);
        }
    }

    private String getParamsClob(String answerChecksum) throws SQLException {
        // construct the sql
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_PARAMS);
        sql.append(" FROM ").append(wdkSchema).append(TABLE_ANSWER);
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ?");
        sql.append(" AND ").append(COLUMN_ANSWER_CHECKSUM).append(" = ?");

        ResultSet resultSet = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement(
                    userPlatform.getDataSource(), sql.toString());
            ps.setString(1, wdkModel.getProjectId());
            ps.setString(2, answerChecksum);
            resultSet = ps.executeQuery();
            if (resultSet.next()) return userPlatform.getClobData(resultSet,
                    COLUMN_PARAMS);
            else return null;
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
    }

    private Map<String, String> parseParams(Map<String, Param> params,
            String paramClob) throws JSONException {
        JSONObject jsParams = new JSONObject(paramClob);
        Map<String, String> paramValues = new LinkedHashMap<String, String>();
        for (String param : params.keySet()) {
            String value = (jsParams.has(param)) ? jsParams.getString(param)
                    : null;
            paramValues.put(param, value);
        }
        return paramValues;
    }
}
