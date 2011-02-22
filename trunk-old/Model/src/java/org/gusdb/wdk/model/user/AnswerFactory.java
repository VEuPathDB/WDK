/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
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
public class AnswerFactory {

    static final String TABLE_ANSWER = "answers";

    static final String COLUMN_ANSWER_ID = "answer_id";
    static final String COLUMN_ANSWER_CHECKSUM = "answer_checksum";
    static final String COLUMN_PROJECT_ID = "project_id";
    static final String COLUMN_PROJECT_VERSION = "project_version";
    static final String COLUMN_QUESTION_NAME = "question_name";
    private static final String COLUMN_QUERY_CHECKSUM = "query_checksum";
    // private static final String COLUMN_PARAMS = "params";

    private WdkModel wdkModel;
    private DBPlatform userPlatform;
    private String wdkSchema;

    /**
     * the answer cache. currently it will store all the answers that have been
     * loaded, assuming the number is relatively small. Might revise the design
     * later.
     */
    private Map<Answer, Answer> answers;

    public AnswerFactory(WdkModel wdkModel) throws SQLException {
        this.wdkModel = wdkModel;
        this.userPlatform = wdkModel.getUserPlatform();
        this.wdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        this.answers = new HashMap<Answer, Answer>();
    }

    public Answer saveAnswerValue(AnswerValue answerValue) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // use transaction
        String questionName = answerValue.getQuestion().getFullName();
        String answerChecksum = answerValue.getChecksum();

        // check if answer has been saved.
        Answer answer = getAnswer(questionName, answerChecksum);
        if (answer == null) {
            Question question = answerValue.getQuestion();
            // the answer hasn't been stored, create an answerInfo, and save it
            int answerId = userPlatform.getNextId(wdkSchema, TABLE_ANSWER);
            answer = new Answer(answerId);
            answer.setAnswerChecksum(answerValue.getChecksum());
            answer.setProjectId(wdkModel.getProjectId());
            answer.setProjectVersion(wdkModel.getVersion());
            answer.setQueryChecksum(question.getQuery().getChecksum(false));
            answer.setQuestionName(question.getFullName());

            JSONObject independentValues = answerValue.getIdsQueryInstance().getIndependentParamValuesJSONObject();
            String paramClob = independentValues.toString();
            saveAnswer(answer, paramClob);
        }
        answerValue.setAnswer(answer);
        return answer;
    }

    /**
     * @param answerChecksum
     * @return an AnswerInfo object if the answer has been saved; otherwise,
     *         return null.
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public Answer getAnswer(String questionName, String answerChecksum)
            throws SQLException, WdkUserException, WdkModelException {
        String projectId = wdkModel.getProjectId();

        // use the cache if exists.
        Answer answer = answers.get(new Answer(projectId, answerChecksum));
        if (answer != null) return answer;

        // construct the query
        String sql = "SELECT " + COLUMN_ANSWER_ID + ", "
                + COLUMN_PROJECT_VERSION + ", " + COLUMN_QUERY_CHECKSUM + ", "
                + COLUMN_QUESTION_NAME + " FROM " + wdkSchema + TABLE_ANSWER
                + " WHERE " + COLUMN_PROJECT_ID + " = ? AND "
                + COLUMN_QUESTION_NAME + " = ? AND " + COLUMN_ANSWER_CHECKSUM
                + " = ?";

        ResultSet resultSet = null;
        try {
            DataSource dataSource = userPlatform.getDataSource();
            long start = System.currentTimeMillis();
            PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
                    sql);
            ps.setString(1, projectId);
            ps.setString(2, questionName);
            ps.setString(3, answerChecksum);
            resultSet = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-answer-factory-answer-by-checksum", start);

            if (resultSet.next()) {
                answer = new Answer(resultSet.getInt(COLUMN_ANSWER_ID));
                answer.setAnswerChecksum(answerChecksum);
                answer.setProjectId(projectId);
                answer.setProjectVersion(resultSet.getString(COLUMN_PROJECT_VERSION));
                answer.setQueryChecksum(resultSet.getString(COLUMN_QUERY_CHECKSUM));
                answer.setQuestionName(resultSet.getString(COLUMN_QUESTION_NAME));
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        answers.put(answer, answer);
        return answer;
    }

    private void saveAnswer(Answer answer, String paramClob)
            throws SQLException, WdkUserException, WdkModelException {
        // prepare the sql
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(wdkSchema).append(TABLE_ANSWER).append(" (");
        sql.append(COLUMN_ANSWER_ID).append(", ");
        sql.append(COLUMN_ANSWER_CHECKSUM).append(", ");
        sql.append(COLUMN_PROJECT_ID).append(", ");
        sql.append(COLUMN_PROJECT_VERSION).append(", ");
        sql.append(COLUMN_QUESTION_NAME).append(", ");
        sql.append(COLUMN_QUERY_CHECKSUM).append(") VALUES (?, ?, ?, ?, ?, ?)");

        PreparedStatement ps = null;
        try {
            DataSource dataSource = userPlatform.getDataSource();
            long start = System.currentTimeMillis();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setInt(1, answer.getAnswerId());
            ps.setString(2, answer.getAnswerChecksum());
            ps.setString(3, answer.getProjectId());
            ps.setString(4, answer.getProjectVersion());
            ps.setString(5, answer.getQuestionName());
            ps.setString(6, answer.getQueryChecksum());

            ps.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-answer-factory-insert", start);
        } finally {
            SqlUtils.closeStatement(ps);
        }
    }
}
