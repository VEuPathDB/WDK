/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class StepFactory {

    private static final String TABLE_STEP = "steps";
    private static final String TABLE_STRATEGY = "strategies";

    private static final String COLUMN_STEP_INTERNAL_ID = "step_id";
    private static final String COLUMN_DISPLAY_ID = "display_id";
    private static final String COLUMN_LEFT_CHILD_ID = "left_child_id";
    private static final String COLUMN_RIGHT_CHILD_ID = "right_child_id";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_LAST_RUN_TIME = "last_run_time";
    private static final String COLUMN_ESTIMATE_SIZE = "estimate_size";
    private static final String COLUMN_ANSWER_FILTER = "answer_filter";
    private static final String COLUMN_CUSTOM_NAME = "custom_name";
    private static final String COLUMN_IS_DELETED = "is_deleted";
    private static final String COLUMN_COLLAPSED_NAME = "collapsed_name";
    private static final String COLUMN_IS_COLLAPSIBLE = "is_collapsible";
    private static final String COLUMN_DISPLAY_PARAMS = "display_params";
    private static final String COLUMN_IS_VALID = "is_valid";
    private static final String COLUMN_SIGNATURE = "signature";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_LAST_VIEWED_TIME = "last_view_time";
    private static final String COLUMN_LAST_MODIFIED_TIME = "last_modify_time";
    private static final String COLUMN_ASSIGNED_WEIGHT = "assigned_weight";

    private static final String COLUMN_STRATEGY_INTERNAL_ID = "strategy_id";
    private static final String COLUMN_ROOT_STEP_ID = "root_step_id";
    private static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_IS_SAVED = "is_saved";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SAVED_NAME = "saved_name";
    private static final String COLUMN_VERSION = "version";

    static final int COLUMN_NAME_LIMIT = 200;

    private static final Logger logger = Logger.getLogger(StepFactory.class);

    private WdkModel wdkModel;
    private String userSchema;
    private String wdkSchema;
    private DBPlatform userPlatform;
    private DataSource dataSource;

    public StepFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.userPlatform = wdkModel.getUserPlatform();
        dataSource = userPlatform.getDataSource();

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        this.userSchema = userDB.getUserSchema();
        this.wdkSchema = userDB.getWdkEngineSchema();
    }

    // parse boolexp to pass left_child_id, right_child_id to loadAnswer
    Step createStep(User user, Question question,
            Map<String, String> dependentValues, AnswerFilterInstance filter,
            int pageStart, int pageEnd, boolean deleted, boolean validate,
            int assignedWeight) throws SQLException, WdkModelException,
            NoSuchAlgorithmException, WdkUserException, JSONException {

        // get summary list and sorting list
        String questionName = question.getFullName();
        Map<String, Boolean> sortingAttributes = user.getSortingAttributes(questionName);

        // create answer
        AnswerValue answerValue = question.makeAnswerValue(user,
                dependentValues, pageStart, pageEnd, sortingAttributes, filter,
                validate, assignedWeight);
        Answer answer = answerValue.getAnswer();

        logger.debug("id query name  :"
                + answerValue.getIdsQueryInstance().getQuery().getFullName());
        logger.debug("answer checksum:" + answerValue.getChecksum());
        logger.debug("question name:  " + question.getFullName());
        logger.debug("answer question:" + answer.getQuestionName());

        // prepare the values to be inserted.
        int userId = user.getUserId();
        int answerId = answer.getAnswerId();

        String filterName = null;
        int estimateSize;
        Exception exception = null;
        try {
            if (filter != null) {
                filterName = filter.getName();
                estimateSize = answerValue.getFilterSize(filterName);
            } else estimateSize = answerValue.getResultSize();
        }
        catch (Exception ex) {
            estimateSize = 0;
            logger.error(ex);
            exception = ex;
        }

        String displayParamContent = getParamContent(dependentValues);

        // prepare SQLs
        String userIdColumn = Utilities.COLUMN_USER_ID;
        String answerIdColumn = AnswerFactory.COLUMN_ANSWER_ID;

        StringBuffer sqlMaxId = new StringBuffer("SELECT max(");
        sqlMaxId.append(COLUMN_DISPLAY_ID).append(") AS max_id FROM ");
        sqlMaxId.append(userSchema).append(TABLE_STEP).append(" s, ");
        sqlMaxId.append(wdkSchema).append(AnswerFactory.TABLE_ANSWER).append(
                " a ");
        sqlMaxId.append("WHERE s.").append(userIdColumn).append(" = ").append(
                userId);
        sqlMaxId.append(" AND s.").append(answerIdColumn);
        sqlMaxId.append(" = a.").append(answerIdColumn);

        StringBuffer sqlInsertStep = new StringBuffer("INSERT INTO ");
        sqlInsertStep.append(userSchema).append(TABLE_STEP).append(" (");
        sqlInsertStep.append(COLUMN_STEP_INTERNAL_ID).append(", ");
        sqlInsertStep.append(COLUMN_DISPLAY_ID).append(", ");
        sqlInsertStep.append(userIdColumn).append(", ");
        sqlInsertStep.append(answerIdColumn).append(", ");
        sqlInsertStep.append(COLUMN_CREATE_TIME).append(", ");
        sqlInsertStep.append(COLUMN_LAST_RUN_TIME).append(", ");
        sqlInsertStep.append(COLUMN_ESTIMATE_SIZE).append(", ");
        sqlInsertStep.append(COLUMN_ANSWER_FILTER).append(", ");
        sqlInsertStep.append(COLUMN_IS_DELETED).append(", ");
        sqlInsertStep.append(COLUMN_ASSIGNED_WEIGHT).append(", ");
        sqlInsertStep.append(COLUMN_DISPLAY_PARAMS).append(") ");
        sqlInsertStep.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        // Now that we have the Answer, create the Step
        Date createTime = new Date();
        Date lastRunTime = new Date(createTime.getTime());

        int displayId = 0;
        int stepId = userPlatform.getNextId(userSchema, TABLE_STEP);
        Connection connection = dataSource.getConnection();

        PreparedStatement psInsertStep = null;
        ResultSet rsMax = null;
        try {
            connection.setAutoCommit(false);

            // get the current display id
            Statement statement = connection.createStatement();
            rsMax = statement.executeQuery(sqlMaxId.toString());
            if (rsMax.next()) // has old steps, get the max of it
                displayId = rsMax.getInt("max_id");
            rsMax.close();
            displayId++;

            psInsertStep = connection.prepareStatement(sqlInsertStep.toString());
            psInsertStep.setInt(1, stepId);
            psInsertStep.setInt(2, displayId);
            psInsertStep.setInt(3, userId);
            psInsertStep.setInt(4, answerId);
            psInsertStep.setTimestamp(5, new Timestamp(createTime.getTime()));
            psInsertStep.setTimestamp(6, new Timestamp(lastRunTime.getTime()));
            psInsertStep.setInt(7, estimateSize);
            psInsertStep.setString(8, filterName);
            psInsertStep.setBoolean(9, deleted);
            psInsertStep.setInt(10, assignedWeight);
            userPlatform.setClobData(psInsertStep, 11, displayParamContent,
                    false);
            psInsertStep.executeUpdate();

            connection.commit();
        }
        catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            connection.setAutoCommit(true);
            SqlUtils.closeResultSet(rsMax);
            SqlUtils.closeStatement(psInsertStep);
        }
        // create the Step
        Step step = new Step(this, user, displayId, stepId);
        step.setAnswer(answer);
        step.setCreatedTime(createTime);
        step.setLastRunTime(lastRunTime);
        step.setDeleted(deleted);
        step.setParamValues(dependentValues);
        step.setAnswerValue(answerValue);
        step.setEstimateSize(estimateSize);
        step.setAssignedWeight(assignedWeight);
        step.setException(exception);

        // update step dependencies
        updateStepTree(user, step);

        return step;
    }

    void deleteStep(User user, int displayId) throws WdkUserException,
            SQLException, WdkModelException {
        PreparedStatement psHistory = null;
        String sql;
        try {
            long start = System.currentTimeMillis();
            if (!isStepDepended(user, displayId)) {
                // remove step
                sql = "DELETE FROM " + userSchema + TABLE_STEP + " WHERE "
                        + Utilities.COLUMN_USER_ID + " = ? AND "
                        + COLUMN_DISPLAY_ID + " = ?";
                psHistory = SqlUtils.getPreparedStatement(dataSource, sql);
            } else { // hide the step
                sql = "UPDATE " + userSchema + TABLE_STEP + " SET "
                        + COLUMN_IS_DELETED + " = 1 WHERE "
                        + Utilities.COLUMN_USER_ID + " = ? " + " AND "
                        + COLUMN_DISPLAY_ID + " = ?";
                psHistory = SqlUtils.getPreparedStatement(dataSource, sql);
            }
            psHistory.setInt(1, user.getUserId());
            psHistory.setInt(2, displayId);
            int result = psHistory.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-step-factory-delete-step",
                    start);
            if (result == 0)
                throw new WdkUserException("The Step #" + displayId
                        + " of user " + user.getEmail() + " cannot be found.");
        }
        finally {
            SqlUtils.closeStatement(psHistory);
        }
    }

    boolean isStepDepended(User user, int displayId) throws SQLException,
            WdkModelException, WdkUserException {
        String answerIdColumn = AnswerFactory.COLUMN_ANSWER_ID;
        StringBuffer sql = new StringBuffer("SELECT count(*) FROM ");
        sql.append(userSchema).append(TABLE_STEP).append(" s, ");
        sql.append(wdkSchema).append(AnswerFactory.TABLE_ANSWER).append(" a ");
        sql.append(" WHERE s.").append(Utilities.COLUMN_USER_ID);
        sql.append(" = ").append(user.getUserId());
        sql.append(" AND s.").append(answerIdColumn);
        sql.append(" = a.").append(answerIdColumn);
        sql.append(" AND (").append(COLUMN_LEFT_CHILD_ID);
        sql.append(" = ").append(displayId);
        sql.append(" OR ").append(COLUMN_RIGHT_CHILD_ID);
        sql.append(" = ").append(displayId).append(")");

        Object result = SqlUtils.executeScalar(wdkModel, dataSource,
                sql.toString(), "wdk-step-factory-check-depended");
        int count = Integer.parseInt(result.toString());
        return (count > 0);
    }

    void deleteSteps(User user, boolean allProjects) throws WdkUserException,
            SQLException, WdkModelException {
        PreparedStatement psDeleteSteps = null;
        String stepTable = userSchema + TABLE_STEP;
        String answerTable = wdkSchema + AnswerFactory.TABLE_ANSWER;
        String strategyTable = userSchema + TABLE_STRATEGY;
        String userIdColumn = Utilities.COLUMN_USER_ID;
        String answerIdColumn = AnswerFactory.COLUMN_ANSWER_ID;
        String projectIdColumn = AnswerFactory.COLUMN_PROJECT_ID;
        try {
            StringBuffer sql = new StringBuffer("DELETE FROM ");
            sql.append(stepTable).append(" WHERE ");
            sql.append(userIdColumn).append(" = ? ");
            if (!allProjects) {
                sql.append(" AND ").append(answerIdColumn).append(" IN (");
                sql.append(" SELECT s.").append(answerIdColumn);
                sql.append(" FROM ").append(stepTable).append(" s, ");
                sql.append(answerTable).append(" a ");
                sql.append(" WHERE s.").append(answerIdColumn);
                sql.append(" = a.").append(answerIdColumn);
                sql.append(" AND a.").append(projectIdColumn).append(" = ?) ");
            }
            sql.append(" AND ").append(COLUMN_DISPLAY_ID);
            sql.append(" NOT IN (SELECT ").append(COLUMN_ROOT_STEP_ID);
            sql.append(" FROM ").append(strategyTable);
            if (!allProjects) {
                sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ? ");
                sql.append(" AND ").append(userIdColumn).append(" = ? ");
            }
            sql.append(") ");

            long start = System.currentTimeMillis();
            psDeleteSteps = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());
            psDeleteSteps.setInt(1, user.getUserId());
            if (!allProjects) {
                String projectId = wdkModel.getProjectId();
                psDeleteSteps.setString(2, projectId);
                psDeleteSteps.setString(3, projectId);
                psDeleteSteps.setInt(4, user.getUserId());
            }
            psDeleteSteps.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-delete-all-steps", start);
        }
        finally {
            SqlUtils.closeStatement(psDeleteSteps);
        }
    }

    public void deleteStrategy(User user, int displayId)
            throws WdkUserException, SQLException, WdkModelException {
        PreparedStatement psStrategy = null;
        String sql = "UPDATE " + userSchema + TABLE_STRATEGY + " SET "
                + COLUMN_IS_DELETED + " = ? WHERE " + Utilities.COLUMN_USER_ID
                + " = ? " + "AND " + COLUMN_PROJECT_ID + " = ? AND "
                + COLUMN_DISPLAY_ID + " = ?";
        try {
            // remove history
            /*
             * psStrategy = SqlUtils.getPreparedStatement(dataSource, "DELETE "
             * + "FROM " + userSchema + TABLE_STRATEGY + " WHERE " +
             * Utilities.COLUMN_USER_ID + " = ? " + "AND " + COLUMN_PROJECT_ID +
             * " = ? AND " + COLUMN_DISPLAY_ID + " = ?"); psStrategy.setInt(1,
             * user.getUserId()); psStrategy.setString(2,
             * wdkModel.getProjectId()); psStrategy.setInt(3, displayId); int
             * result = psStrategy.executeUpdate(); if (result == 0) throw new
             * WdkUserException("The strategy #" + displayId + " of user " +
             * user.getEmail() + " cannot be found.");
             */
            long start = System.currentTimeMillis();
            psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
            psStrategy.setBoolean(1, true);
            psStrategy.setInt(2, user.getUserId());
            psStrategy.setString(3, wdkModel.getProjectId());
            psStrategy.setInt(4, displayId);
            int result = psStrategy.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-delete-strategy", start);
            if (result == 0)
                throw new WdkUserException("The strategy #" + displayId
                        + " of user " + user.getEmail() + " cannot be found.");
        }
        finally {
            SqlUtils.closeStatement(psStrategy);
        }
    }

    void deleteStrategies(User user, boolean allProjects) throws SQLException,
            WdkUserException, WdkModelException {
        PreparedStatement psDeleteStrategies = null;
        try {
            StringBuffer sql = new StringBuffer("DELETE FROM ");
            sql.append(userSchema).append(TABLE_STRATEGY).append(" WHERE ");
            sql.append(Utilities.COLUMN_USER_ID).append(" = ?");
            if (!allProjects) {
                sql.append(" AND ").append(COLUMN_PROJECT_ID).append(" = ?");
            }
            long start = System.currentTimeMillis();
            psDeleteStrategies = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());

            psDeleteStrategies.setInt(1, user.getUserId());
            if (!allProjects)
                psDeleteStrategies.setString(2, wdkModel.getProjectId());
            psDeleteStrategies.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-delete-all-strategies", start);
        }
        finally {
            SqlUtils.closeStatement(psDeleteStrategies);
        }
    }

    void deleteInvalidSteps(User user) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        // get invalid histories
        Map<Integer, Step> invalidSteps = new LinkedHashMap<Integer, Step>();
        loadSteps(user, invalidSteps);
        for (int displayId : invalidSteps.keySet()) {
            deleteStep(user, displayId);
        }
    }

    void deleteInvalidStrategies(User user) throws WdkUserException,
            WdkModelException, SQLException, JSONException,
            NoSuchAlgorithmException {
        // get invalid histories
        Map<Integer, Strategy> invalidStrategies = new LinkedHashMap<Integer, Strategy>();
        loadStrategies(user, invalidStrategies);
        for (int displayId : invalidStrategies.keySet()) {
            deleteStep(user, displayId);
        }
    }

    int getStepCount(User user) throws WdkUserException, WdkModelException {
        String stepTable = userSchema + TABLE_STEP;
        String answerTable = wdkSchema + AnswerFactory.TABLE_ANSWER;
        String answerIdColumn = AnswerFactory.COLUMN_ANSWER_ID;
        ResultSet rsHistory = null;
        String sql = "SELECT count(h." + COLUMN_STEP_INTERNAL_ID + ") AS num"
                + " FROM " + stepTable + " h, " + answerTable + " a WHERE h."
                + answerIdColumn + " = a." + answerIdColumn + " AND h."
                + Utilities.COLUMN_USER_ID + " = ? AND a."
                + AnswerFactory.COLUMN_PROJECT_ID + " = ? "
                + " AND is_deleted = " + userPlatform.convertBoolean(false);
        try {
            long start = System.currentTimeMillis();
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, wdkModel.getProjectId());
            rsHistory = psHistory.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-step-factory-step-count",
                    start);
            rsHistory.next();
            return rsHistory.getInt("num");
        }
        catch (SQLException ex) {
            throw new WdkUserException(ex);
        }
        finally {
            SqlUtils.closeResultSet(rsHistory);
        }
    }

    Map<Integer, Step> loadSteps(User user, Map<Integer, Step> invalidSteps)
            throws SQLException, WdkModelException, JSONException,
            WdkUserException {
        Map<Integer, Step> steps = new LinkedHashMap<Integer, Step>();

        String answerIdColumn = AnswerFactory.COLUMN_ANSWER_ID;
        ResultSet rsStep = null;
        String sql = "SELECT h.*, a." + AnswerFactory.COLUMN_ANSWER_CHECKSUM
                + ", a." + AnswerFactory.COLUMN_QUESTION_NAME + " FROM "
                + userSchema + TABLE_STEP + " h, " + wdkSchema
                + AnswerFactory.TABLE_ANSWER + " a WHERE h."
                + Utilities.COLUMN_USER_ID + " = ? AND h." + answerIdColumn
                + " = a." + answerIdColumn + " AND a."
                + AnswerFactory.COLUMN_PROJECT_ID + " = ? ORDER BY h."
                + COLUMN_LAST_RUN_TIME + " DESC";
        try {
            long start = System.currentTimeMillis();
            PreparedStatement psStep = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psStep.setInt(1, user.getUserId());
            psStep.setString(2, wdkModel.getProjectId());
            rsStep = psStep.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-load-all-steps", start);

            while (rsStep.next()) {
                Step step = loadStep(user, rsStep, false);
                int stepId = step.getDisplayId();
                // if (step.isValid()) {
                steps.put(stepId, step);
                // } else {
                // invalidSteps.put(stepId, step);
                // }
            }
        }
        finally {
            SqlUtils.closeResultSet(rsStep);
        }
        logger.debug("Steps: " + steps.size());
        logger.debug("Invalid: " + invalidSteps.size());
        return steps;
    }

    // get left child id, right child id in here
    Step loadStep(User user, int displayId) throws WdkUserException,
            WdkModelException {
        String answerIdColumn = AnswerFactory.COLUMN_ANSWER_ID;
        ResultSet rsStep = null;
        String sql = "SELECT h.*, a." + AnswerFactory.COLUMN_ANSWER_CHECKSUM
                + ", a." + AnswerFactory.COLUMN_QUESTION_NAME + " FROM "
                + userSchema + TABLE_STEP + " h, " + wdkSchema
                + AnswerFactory.TABLE_ANSWER + " a WHERE h."
                + Utilities.COLUMN_USER_ID + " = ? AND h." + answerIdColumn
                + " = a." + answerIdColumn + " AND a."
                + AnswerFactory.COLUMN_PROJECT_ID + " = ? AND h."
                + COLUMN_DISPLAY_ID + " = ?";
        try {
            long start = System.currentTimeMillis();
            PreparedStatement psStep = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psStep.setInt(1, user.getUserId());
            psStep.setString(2, wdkModel.getProjectId());
            psStep.setInt(3, displayId);
            rsStep = psStep.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-step-factory-load-step",
                    start);
            if (!rsStep.next())
                throw new WdkUserException("The Step #" + displayId
                        + " of user " + user.getEmail() + " doesn't exist.");

            return loadStep(user, rsStep, false);
        }
        catch (SQLException e) {
            throw new WdkUserException("Unable to load step.", e);
        }
        catch (JSONException e) {
            throw new WdkUserException("Unable to load step.", e);
        }
        finally {
            SqlUtils.closeResultSet(rsStep);
        }
    }

    private Step loadStep(User user, ResultSet rsStep, boolean loadTree)
            throws WdkModelException, WdkUserException, SQLException,
            JSONException {
        // load Step info
        int stepId = rsStep.getInt(COLUMN_STEP_INTERNAL_ID);
        int displayId = rsStep.getInt(COLUMN_DISPLAY_ID);
        String questionName = rsStep.getString(AnswerFactory.COLUMN_QUESTION_NAME);

        Step step = new Step(this, user, displayId, stepId);
        step.setCreatedTime(rsStep.getTimestamp(COLUMN_CREATE_TIME));
        step.setLastRunTime(rsStep.getTimestamp(COLUMN_LAST_RUN_TIME));
        step.setCustomName(rsStep.getString(COLUMN_CUSTOM_NAME));
        step.setDeleted(rsStep.getBoolean(COLUMN_IS_DELETED));
        step.setCollapsible(rsStep.getBoolean(COLUMN_IS_COLLAPSIBLE));
        step.setCollapsedName(rsStep.getString(COLUMN_COLLAPSED_NAME));
        step.setEstimateSize(rsStep.getInt(COLUMN_ESTIMATE_SIZE));
        step.setFilterName(rsStep.getString(COLUMN_ANSWER_FILTER));
        if (rsStep.getObject(COLUMN_IS_VALID) != null)
            step.setValid(rsStep.getBoolean(COLUMN_IS_VALID));
        if (rsStep.getObject(COLUMN_ASSIGNED_WEIGHT) != null)
            step.setAssignedWeight(rsStep.getInt(COLUMN_ASSIGNED_WEIGHT));

        // load left and right child
        if (rsStep.getObject(COLUMN_LEFT_CHILD_ID) != null) {
            int leftStepId = rsStep.getInt(COLUMN_LEFT_CHILD_ID);
            step.setPreviousStepId(leftStepId);
        }
        if (rsStep.getObject(COLUMN_RIGHT_CHILD_ID) != null) {
            int rightStepId = rsStep.getInt(COLUMN_RIGHT_CHILD_ID);
            step.setChildStepId(rightStepId);
        }

        String dependentParamContent = userPlatform.getClobData(rsStep,
                COLUMN_DISPLAY_PARAMS);
        logger.debug("step #" + displayId + " (" + stepId + ")");
        Map<String, String> dependentValues = parseParamContent(dependentParamContent);

        String answerChecksum = rsStep.getString(AnswerFactory.COLUMN_ANSWER_CHECKSUM);

        try {
            // load Answer
            AnswerFactory answerFactory = wdkModel.getAnswerFactory();
            Answer answer = answerFactory.getAnswer(questionName,
                    answerChecksum);
            step.setAnswer(answer);
            step.setParamValues(dependentValues);
        }
        catch (Exception ex) {
            step.setValid(false);
            step.setValidationMessage(ex.getMessage());
        }
        if (!step.isValid()) setStepValidFlag(step);
        return step;
    }

    private void updateStepTree(User user, Step step) throws WdkUserException,
            WdkModelException {
        Question question = step.getQuestion();
        Map<String, String> displayParams = step.getParamValues();

        Query query = question.getQuery();
        int leftStepId = 0;
        int rightStepId = 0;
        String customName;
        if (query.isBoolean()) {
            // boolean result, set the left and right step ids accordingly, and
            // set the constructed boolean expression to custom name.
            BooleanQuery booleanQuery = (BooleanQuery) query;

            AnswerParam leftParam = booleanQuery.getLeftOperandParam();
            String leftKey = displayParams.get(leftParam.getName());
            String leftStepKey = leftKey.substring(leftKey.indexOf(":") + 1);
            leftStepId = Integer.parseInt(leftStepKey);

            AnswerParam rightParam = booleanQuery.getRightOperandParam();
            String rightKey = displayParams.get(rightParam.getName());
            String rightStepKey = rightKey.substring(rightKey.indexOf(":") + 1);
            rightStepId = Integer.parseInt(rightStepKey);

            StringParam operatorParam = booleanQuery.getOperatorParam();
            String operator = displayParams.get(operatorParam.getName());

            customName = leftStepId + " " + operator + " " + rightKey;
        } else if (query.isCombined()) {
            // transform result, set the first two params
            for (Param param : question.getParams()) {
                if (param instanceof AnswerParam) {
                    AnswerParam answerParam = (AnswerParam) param;
                    String stepId = displayParams.get(answerParam.getName());
                    // put the first child into left, the second into right
                    if (leftStepId == 0) leftStepId = Integer.valueOf(stepId);
                    else {
                        rightStepId = Integer.valueOf(stepId);
                        break;
                    }
                }
            }
            customName = step.getBaseCustomName();
        } else customName = step.getBaseCustomName();

        step.setPreviousStepId(leftStepId);
        step.setChildStepId(rightStepId);

        // construct the update sql
        StringBuffer sql = new StringBuffer("UPDATE ");
        sql.append(userSchema).append(TABLE_STEP).append(" SET ");
        sql.append(COLUMN_CUSTOM_NAME).append(" = ? ");
        if (query.isCombined()) {
            sql.append(", ").append(COLUMN_LEFT_CHILD_ID);
            sql.append(" = ").append(leftStepId);
            if (rightStepId != 0) {
                sql.append(", ").append(COLUMN_RIGHT_CHILD_ID);
                sql.append(" = ").append(rightStepId);
            }
        }
        sql.append(" WHERE ").append(COLUMN_STEP_INTERNAL_ID);
        sql.append(" = ").append(step.getInternalId());

        step.setCustomName(customName);
        PreparedStatement psUpdateStepTree = null;
        try {
            long start = System.currentTimeMillis();
            psUpdateStepTree = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());
            psUpdateStepTree.setString(1, customName);
            psUpdateStepTree.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-update-step-tree", start);
        }
        catch (SQLException e) {
            throw new WdkUserException("Could not update step tree.", e);
        }
        finally {
            SqlUtils.closeStatement(psUpdateStepTree);
        }
    }

    /**
     * This method updates the custom name, the time stamp of last running,
     * isDeleted, isCollapsible, and collapsed name
     * 
     * @param user
     * @param step
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    void updateStep(User user, Step step, boolean updateTime)
            throws WdkUserException, WdkModelException {
        logger.debug("step #" + step.getDisplayId() + " new custom name: '"
                + step.getBaseCustomName() + "'");
        // update custom name
        Date lastRunTime = (updateTime) ? new Date() : step.getLastRunTime();
        int estimateSize = step.getEstimateSize();
        PreparedStatement psStep = null;
        String sql = "UPDATE " + userSchema + TABLE_STEP + " SET "
                + COLUMN_CUSTOM_NAME + " = ?, " + COLUMN_LAST_RUN_TIME
                + " = ?, " + COLUMN_IS_DELETED + " = ?, "
                + COLUMN_IS_COLLAPSIBLE + " = ?, " + COLUMN_COLLAPSED_NAME
                + " = ?, " + COLUMN_ESTIMATE_SIZE + " = ?, " + COLUMN_IS_VALID
                + " = ?, " + COLUMN_ASSIGNED_WEIGHT + " = ? WHERE "
                + COLUMN_STEP_INTERNAL_ID + " = ?";
        try {
            long start = System.currentTimeMillis();
            psStep = SqlUtils.getPreparedStatement(dataSource, sql);
            psStep.setString(1, step.getBaseCustomName());
            psStep.setTimestamp(2, new Timestamp(lastRunTime.getTime()));
            psStep.setBoolean(3, step.isDeleted());
            psStep.setBoolean(4, step.isCollapsible());
            psStep.setString(5, step.getCollapsedName());
            psStep.setInt(6, estimateSize);
            psStep.setBoolean(7, step.isValid());
            psStep.setInt(8, step.getAssignedWeight());
            psStep.setInt(9, step.getInternalId());
            int result = psStep.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-step-factory-update-step",
                    start);
            if (result == 0)
                throw new WdkUserException("The Step #" + step.getDisplayId()
                        + " of user " + user.getEmail() + " cannot be found.");

            // update the last run stamp
            step.setLastRunTime(lastRunTime);
            step.setEstimateSize(estimateSize);

            // update dependencies
            if (step.isCombined()) updateStepTree(user, step);
        }
        catch (SQLException e) {
            throw new WdkUserException("Could not update step.", e);
        }
        finally {
            SqlUtils.closeStatement(psStep);
        }
    }

    Map<Integer, Strategy> loadStrategies(User user,
            Map<Integer, Strategy> invalidStrategies) throws WdkUserException,
            WdkModelException, JSONException, SQLException,
            NoSuchAlgorithmException {
        Map<Integer, Strategy> userStrategies = new LinkedHashMap<Integer, Strategy>();

        PreparedStatement psStrategyIds = null;
        ResultSet rsStrategyIds = null;
        String sql = "SELECT s." + COLUMN_DISPLAY_ID + " FROM " + userSchema
                + TABLE_STRATEGY + " s, " + userSchema + TABLE_STEP
                + " h WHERE s." + Utilities.COLUMN_USER_ID + " = ? AND s."
                + COLUMN_PROJECT_ID + " = ? AND s." + COLUMN_IS_DELETED
                + " = ? AND h." + Utilities.COLUMN_USER_ID + " = s."
                + Utilities.COLUMN_USER_ID + " AND h." + COLUMN_DISPLAY_ID
                + " = s." + COLUMN_ROOT_STEP_ID + " ORDER BY h."
                + COLUMN_LAST_RUN_TIME + " DESC";
        try {
            long start = System.currentTimeMillis();
            psStrategyIds = SqlUtils.getPreparedStatement(dataSource, sql);
            psStrategyIds.setInt(1, user.getUserId());
            psStrategyIds.setString(2, wdkModel.getProjectId());
            psStrategyIds.setBoolean(3, false);
            rsStrategyIds = psStrategyIds.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-load-all-strategies", start);
            Strategy strategy;
            int strategyId;
            while (rsStrategyIds.next()) {
                strategyId = rsStrategyIds.getInt(COLUMN_DISPLAY_ID);
                strategy = loadStrategy(user, strategyId, false);
                userStrategies.put(strategyId, strategy);
            }

            return userStrategies;
        }
        finally {
            SqlUtils.closeStatement(psStrategyIds);
            SqlUtils.closeResultSet(rsStrategyIds);
        }
    }

    List<Strategy> loadStrategies(User user, boolean saved, boolean recent)
            throws SQLException, WdkUserException, WdkModelException,
            JSONException, NoSuchAlgorithmException {
        String userColumn = Utilities.COLUMN_USER_ID;
        String answerColumn = AnswerFactory.COLUMN_ANSWER_ID;
        StringBuffer sql = new StringBuffer("SELECT DISTINCT sr.* ");
        sql.append(", sr." + COLUMN_LAST_VIEWED_TIME + ", sp."
                + COLUMN_ESTIMATE_SIZE + ", sp." + COLUMN_IS_VALID + ", sr."
                + COLUMN_VERSION + ", a." + AnswerFactory.COLUMN_QUESTION_NAME);
        sql.append(" FROM " + userSchema + TABLE_STRATEGY + " sr, "
                + userSchema + TABLE_STEP + " sp, " + wdkSchema
                + AnswerFactory.TABLE_ANSWER + " a");
        sql.append(" WHERE sr." + COLUMN_ROOT_STEP_ID + " = sp."
                + COLUMN_DISPLAY_ID + " AND sp." + answerColumn + " = a."
                + answerColumn + " AND sr." + userColumn + " = sp."
                + userColumn + " AND sr." + COLUMN_PROJECT_ID + " = a."
                + COLUMN_PROJECT_ID);
        sql.append(" AND sr.").append(userColumn).append(" = ?");
        sql.append(" AND sr." + userColumn + " = sp." + userColumn);
        sql.append(" AND sr.").append(COLUMN_PROJECT_ID).append(" = ?");
        sql.append(" AND sr.").append(COLUMN_IS_SAVED).append(" = ?");
        sql.append(" AND sr.").append(COLUMN_IS_DELETED).append(" = ?");
        if (recent) sql.append(" AND sr." + COLUMN_LAST_VIEWED_TIME + " >= ?");
        sql.append(" ORDER BY sr." + COLUMN_LAST_VIEWED_TIME + " DESC");

        List<Strategy> strategies = new ArrayList<Strategy>();
        ResultSet resultSet = null;
        try {
            long start = System.currentTimeMillis();
            PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());
            ps.setInt(1, user.getUserId());
            ps.setString(2, wdkModel.getProjectId());
            ps.setBoolean(3, saved);
            ps.setBoolean(4, false);
            if (recent) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1);
                Date date = new Date();
                ps.setTimestamp(5, new Timestamp(date.getTime()));
            }
            resultSet = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-load-strategies", start);
            while (resultSet.next()) {
                // ignore the invalid strategies caused by missing steps.
                // it was caused by replication failure. need to investigate
                // it further.
                try {
                    Strategy strategy = loadStrategy(user, resultSet);
                    strategies.add(strategy);
                }
                catch (WdkException ex) {
                    logger.error("ignore strategy #"
                            + resultSet.getInt("strategy_id") + ", cause: "
                            + ex);
                }
            }
        }
        finally {
            SqlUtils.closeResultSet(resultSet);
        }
        Collections.sort(strategies, new Comparator<Strategy>() {
            public int compare(Strategy o1, Strategy o2) {
                return o2.getLastRunTime().compareTo(o1.getLastRunTime());
            }
        });
        return strategies;
    }

    private Strategy loadStrategy(User user, ResultSet resultSet)
            throws SQLException, WdkUserException, WdkModelException,
            JSONException, NoSuchAlgorithmException {
        int internalId = resultSet.getInt(COLUMN_STRATEGY_INTERNAL_ID);
        int strategyId = resultSet.getInt(COLUMN_DISPLAY_ID);

        Strategy strategy = new Strategy(this, user, strategyId, internalId);
        strategy.setName(resultSet.getString(COLUMN_NAME));
        strategy.setCreatedTime(resultSet.getTimestamp(COLUMN_CREATE_TIME));
        strategy.setIsSaved(resultSet.getBoolean(COLUMN_IS_SAVED));
        strategy.setDeleted(resultSet.getBoolean(COLUMN_IS_DELETED));
        strategy.setSavedName(resultSet.getString(COLUMN_SAVED_NAME));
        strategy.setLastModifiedTime(resultSet.getTimestamp(COLUMN_LAST_MODIFIED_TIME));
        strategy.setSignature(resultSet.getString(COLUMN_SIGNATURE));
        strategy.setDescription(resultSet.getString(COLUMN_DESCRIPTION));
        strategy.setLatestStepId(resultSet.getInt(COLUMN_ROOT_STEP_ID));

        strategy.setLastRunTime(resultSet.getTimestamp(COLUMN_LAST_VIEWED_TIME));
        strategy.setEstimateSize(resultSet.getInt(COLUMN_ESTIMATE_SIZE));
        strategy.setVersion(resultSet.getString(COLUMN_VERSION));
        if (resultSet.getObject(COLUMN_IS_VALID) != null)
            strategy.setValid(resultSet.getBoolean(COLUMN_IS_VALID));

        String questionName = resultSet.getString(AnswerFactory.COLUMN_QUESTION_NAME);
        Question question = (Question) wdkModel.getQuestion(questionName);
        RecordClass recordClass = question.getRecordClass();
        strategy.setType(recordClass.getFullName());
        strategy.setDisplayType(recordClass.getDisplayName());

        String signature = strategy.getSignature();
        if (signature == null || signature.trim().length() == 0) {
            signature = getStrategySignature(user.getUserId(), internalId);
            String sql = "UPDATE " + userSchema + "strategies SET signature = "
                    + "'" + signature + "' WHERE strategy_id = " + internalId;
            SqlUtils.executeUpdate(wdkModel, dataSource, sql,
                    "wdk-step-factory-update-strategy-signature");
            strategy.setSignature(signature);
        }

        return strategy;
    }

    Strategy importStrategy(User user, Strategy oldStrategy,
            Map<Integer, Integer> stepIdsMap) throws WdkUserException,
            WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException {
        logger.debug("import strategy #" + oldStrategy.getInternalId()
                + "(internal) to user #" + user.getUserId());

        if (stepIdsMap == null)
            stepIdsMap = new LinkedHashMap<Integer, Integer>();

        Step oldRootStep = oldStrategy.getLatestStep();
        String name = getNextName(user, oldStrategy.getName(), false);

        // If user does not already have a copy of this strategy, need to
        // look up the answers recursively, construct step objects.
        Step latestStep = importStep(user, oldRootStep, stepIdsMap);
        // Need to create strategy & then load it so that all AnswerValues
        // are created properly
        // Jerric - the imported strategy should always be unsaved.
        Strategy strategy = createStrategy(user, latestStep, name, null, false,
                oldStrategy.getDescription(), false);
        return loadStrategy(user, strategy.getStrategyId(), false);
    }

    Step importStep(User newUser, Step oldStep, Map<Integer, Integer> stepIdsMap)
            throws WdkUserException, WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException {
        User oldUser = oldStep.getUser();

        // Is this answer a boolean? Import depended steps first.
        Question question = oldStep.getQuestion();
        AnswerFilterInstance filter = oldStep.getFilter();

        Map<String, Param> params = question.getParamMap();

        Map<String, String> paramValues = oldStep.getParamValues();
        for (String paramName : paramValues.keySet()) {
            Param param = params.get(paramName);
            String paramValue = paramValues.get(paramName);

            if (param instanceof AnswerParam) {
                int oldStepId = Integer.parseInt(paramValue);
                Step oldChildStep = oldUser.getStep(oldStepId);
                Step newChildStep = importStep(newUser, oldChildStep,
                        stepIdsMap);
                paramValue = Integer.toString(newChildStep.getDisplayId());
            } else if (param instanceof DatasetParam) {
                DatasetParam datasetParam = (DatasetParam) param;
                int oldUserDatasetId = Integer.parseInt(paramValue);
                Dataset oldDataset = oldUser.getDataset(oldUserDatasetId);
                oldDataset.setRecordClass(datasetParam.getRecordClass());
                Dataset newDataset = newUser.getDataset(oldDataset.getChecksum());
                newDataset.setRecordClass(datasetParam.getRecordClass());
                paramValue = Integer.toString(newDataset.getUserDatasetId());
            }
            paramValues.put(paramName, paramValue);
        }

        int startIndex = 1;
        int endIndex = oldStep.getUser().getItemsPerPage();
        boolean deleted = oldStep.isDeleted();
        int assignedWeight = oldStep.getAssignedWeight();
        Step newStep = newUser.createStep(question, paramValues, filter,
                startIndex, endIndex, deleted, false, assignedWeight);
        stepIdsMap.put(oldStep.getDisplayId(), newStep.getDisplayId());
        newStep.setCollapsedName(oldStep.getCollapsedName());
        newStep.setCollapsible(oldStep.isCollapsible());
        String customName = oldStep.getBaseCustomName();
        if (customName != null) newStep.setCustomName(customName);
        newStep.setValid(oldStep.isValid());
        newStep.update(false);
        return newStep;
    }

    Strategy loadStrategy(User user, int displayId, boolean allowDeleted)
            throws WdkUserException, WdkModelException, JSONException,
            SQLException, NoSuchAlgorithmException {
        String userColumn = Utilities.COLUMN_USER_ID;
        String answerColumn = AnswerFactory.COLUMN_ANSWER_ID;

        PreparedStatement psStrategy = null;
        ResultSet rsStrategy = null;
        try {
            StringBuffer sql = new StringBuffer("SELECT sr.*, ");
            sql.append(" sp." + COLUMN_ESTIMATE_SIZE + ", sp."
                    + COLUMN_IS_VALID + ", a."
                    + AnswerFactory.COLUMN_QUESTION_NAME);
            sql.append(" FROM " + userSchema + TABLE_STRATEGY + " sr, "
                    + userSchema + TABLE_STEP + " sp, " + wdkSchema
                    + AnswerFactory.TABLE_ANSWER + " a");
            sql.append(" WHERE sr." + COLUMN_ROOT_STEP_ID + " = sp."
                    + COLUMN_DISPLAY_ID + " AND sp." + answerColumn + " = a."
                    + answerColumn + " AND sr." + userColumn + " = sp."
                    + userColumn + " AND sr." + COLUMN_PROJECT_ID + " = a."
                    + COLUMN_PROJECT_ID);
            sql.append(" AND sr.").append(userColumn).append(" = ? ");
            sql.append(" AND sr.").append(COLUMN_DISPLAY_ID).append(" = ?");
            sql.append(" AND sr.").append(COLUMN_PROJECT_ID).append(" = ?");

            if (!allowDeleted) {
                sql.append(" AND sr." + COLUMN_IS_DELETED + " = ?");
            }
            long start = System.currentTimeMillis();
            psStrategy = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());
            psStrategy.setInt(1, user.getUserId());
            psStrategy.setInt(2, displayId);
            psStrategy.setString(3, wdkModel.getProjectId());
            if (!allowDeleted) {
                psStrategy.setBoolean(4, false);
            }
            rsStrategy = psStrategy.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-load-strategy-by-id", start);
            if (!rsStrategy.next()) {
                throw new WdkUserException("The strategy " + displayId
                        + " does not exist " + "for user " + user.getEmail());
            }

            Strategy strategy = loadStrategy(user, rsStrategy);
            // Set saved name, if any
            /*
             * if
             * (!strategy.getName().matches("^New Strategy(\\([\\d]+\\))?\\*$"))
             * { // System.out.println("Name does not match: " + //
             * strategy.getName()); // Remove any (and everything after it) from
             * name, set as // saved name
             * strategy.setSavedName(strategy.getName().replaceAll(
             * "(\\([\\d]+\\))?\\*$", "")); }
             */
            return strategy;
        }
        finally {
            SqlUtils.closeStatement(psStrategy);
            SqlUtils.closeResultSet(rsStrategy);
        }
    }

    Strategy loadStrategy(String strategySignature) throws WdkUserException,
            SQLException, WdkModelException, JSONException,
            NoSuchAlgorithmException {
        String userColumn = Utilities.COLUMN_USER_ID;
        String answerColumn = AnswerFactory.COLUMN_ANSWER_ID;
        StringBuffer sql = new StringBuffer("SELECT sr.*, ");
        sql.append(" sp." + COLUMN_ESTIMATE_SIZE + ", sp." + COLUMN_IS_VALID
                + ", a."
                + AnswerFactory.COLUMN_QUESTION_NAME);
        sql.append(" FROM " + userSchema + TABLE_STRATEGY + " sr, "
                + userSchema + TABLE_STEP + " sp, " + wdkSchema
                + AnswerFactory.TABLE_ANSWER + " a");
        sql.append(" WHERE sr." + COLUMN_ROOT_STEP_ID + " = sp."
                + COLUMN_DISPLAY_ID + " AND sp." + answerColumn + " = a."
                + answerColumn + " AND sr." + userColumn + " = sp."
                + userColumn + " AND sr." + COLUMN_PROJECT_ID + " = a."
                + COLUMN_PROJECT_ID);
        sql.append(" AND sr.").append(COLUMN_SIGNATURE).append(" = ? ");
        sql.append(" AND sr.").append(COLUMN_PROJECT_ID).append(" = ?");
        sql.append(" ORDER BY sr.").append(COLUMN_LAST_MODIFIED_TIME).append(
                " DESC");
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            long start = System.currentTimeMillis();
            ps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            ps.setString(1, strategySignature);
            ps.setString(2, wdkModel.getProjectId());
            resultSet = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-load-strategy-by-signature", start);
            if (!resultSet.next())
                throw new WdkUserException("The strategy " + strategySignature
                        + " does not exist");
            int userId = resultSet.getInt(Utilities.COLUMN_USER_ID);
            User user = wdkModel.getUserFactory().getUser(userId);
            Strategy strategy = loadStrategy(user, resultSet);
            return strategy;
        }
        finally {
            SqlUtils.closeStatement(ps);
            SqlUtils.closeResultSet(resultSet);
        }
    }

    // This function only updates the strategies table
    void updateStrategy(User user, Strategy strategy, boolean overwrite)
            throws WdkUserException, WdkModelException, SQLException,
            JSONException, NoSuchAlgorithmException {
        logger.debug("Updating strategy internal#=" + strategy.getInternalId()
                + ", overwrite=" + overwrite);

        // update strategy name, saved, step_id
        PreparedStatement psStrategy = null;
        ResultSet rsStrategy = null;

        int userId = user.getUserId();

        String userIdColumn = Utilities.COLUMN_USER_ID;
        try {
            if (overwrite) {
                String sql = "SELECT " + COLUMN_STRATEGY_INTERNAL_ID + ", "
                        + COLUMN_DISPLAY_ID + ", " + COLUMN_SIGNATURE
                        + " FROM " + userSchema + TABLE_STRATEGY + " WHERE "
                        + userIdColumn + " = ? AND " + COLUMN_PROJECT_ID
                        + " = ? AND " + COLUMN_NAME + " = ? AND "
                        + COLUMN_IS_SAVED + " = ? AND " + COLUMN_IS_DELETED
                        + " = ? "; // AND " + COLUMN_DISPLAY_ID + " <> ?";
                // If we're overwriting, need to look up saved strategy id by
                // name (only if the saved strategy is not the one we're
                // updating, i.e. the saved strategy id != this strategy id)

                // jerric - will also find the saved copy of itself, so that we
                // can keep the signature.
                long start = System.currentTimeMillis();
                PreparedStatement psCheck = SqlUtils.getPreparedStatement(
                        dataSource, sql);
                psCheck.setInt(1, userId);
                psCheck.setString(2, wdkModel.getProjectId());
                psCheck.setString(3, strategy.getName());
                psCheck.setBoolean(4, true);
                psCheck.setBoolean(5, false);
                // psCheck.setInt(6, strategy.getStrategyId());
                rsStrategy = psCheck.executeQuery();
                SqlUtils.verifyTime(wdkModel, sql,
                        "wdk-step-factory-check-strategy-name", start);

                // If there's already a saved strategy with this strategy's
                // name,
                // we need to write the new saved strategy & mark the old
                // saved strategy as deleted
                if (rsStrategy.next()) {
                    int idToDelete = rsStrategy.getInt(COLUMN_DISPLAY_ID);
                    String signature = rsStrategy.getString(COLUMN_SIGNATURE);
                    strategy.setIsSaved(true);
                    strategy.setSignature(signature);
                    strategy.setSavedName(strategy.getName());
                    // jerric - only delete the strategy if it's a different one
                    if (strategy.getStrategyId() != idToDelete)
                        user.deleteStrategy(idToDelete);
                }
            } else if (strategy.getIsSaved()) {
                // If we're not overwriting a saved strategy, then we're
                // modifying
                // it. We need to get an unsaved copy to modify. Generate
                // unsaved name
                String name = getNextName(user, strategy.getName(), false);
                Strategy newStrat = createStrategy(user,
                        strategy.getLatestStep(), name, strategy.getName(),
                        false, strategy.getDescription(), false);
                strategy.setName(newStrat.getName());
                strategy.setSavedName(newStrat.getSavedName());
                strategy.setDisplayId(newStrat.getStrategyId());
                strategy.setInternalId(newStrat.getInternalId());
                strategy.setSignature(newStrat.getSignature());
                strategy.setIsSaved(false);
            }

            Date modifiedTime = new Date();
            String sql = "UPDATE " + userSchema + TABLE_STRATEGY + " SET "
                    + COLUMN_NAME + " = ?, " + COLUMN_ROOT_STEP_ID + " = ?, "
                    + COLUMN_SAVED_NAME + " = ?, " + COLUMN_IS_SAVED + " = ?, "
                    + COLUMN_DESCRIPTION + " = ?, " + COLUMN_LAST_MODIFIED_TIME
                    + " = ?, " + COLUMN_SIGNATURE + "= ? WHERE "
                    + COLUMN_STRATEGY_INTERNAL_ID + " = ?";
            long start = System.currentTimeMillis();
            psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
            psStrategy.setString(1, strategy.getName());
            psStrategy.setInt(2, strategy.getLatestStep().getDisplayId());
            psStrategy.setString(3, strategy.getSavedName());
            psStrategy.setBoolean(4, strategy.getIsSaved());
            psStrategy.setString(5, strategy.getDescription());
            psStrategy.setTimestamp(6, new Timestamp(modifiedTime.getTime()));
            psStrategy.setString(7, strategy.getSignature());
            psStrategy.setInt(8, strategy.getInternalId());
            int result = psStrategy.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-update-strategy", start);

            strategy.setLastModifiedTime(modifiedTime);

            if (result == 0)
                throw new WdkUserException("The strategy #"
                        + strategy.getStrategyId() + " of user "
                        + user.getEmail() + " cannot be found.");
        }
        finally {
            SqlUtils.closeStatement(psStrategy);
            SqlUtils.closeResultSet(rsStrategy);
        }

    }

    // Note: this function only adds the necessary row in strategies; updating
    // of answers
    // and steps tables is handled in other functions. Once the Step
    // object exists, all of this data is already in the db.
    Strategy createStrategy(User user, Step root, String name,
            String savedName, boolean saved, String description, boolean hidden)
            throws SQLException, WdkUserException, WdkModelException,
            JSONException, NoSuchAlgorithmException {
        logger.debug("creating strategy, saved=" + saved);
        int userId = user.getUserId();

        String userIdColumn = Utilities.COLUMN_USER_ID;
        ResultSet rsCheckName = null;
        PreparedStatement psCheckName;

        String sql = "SELECT " + COLUMN_DISPLAY_ID + " FROM " + userSchema
                + TABLE_STRATEGY + " WHERE " + userIdColumn + " = ? AND "
                + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME + " = ? AND "
                + COLUMN_IS_SAVED + "= ? AND " + COLUMN_IS_DELETED + "= ?";
        try {
            // If name is not null, check if strategy exists
            if (name != null) {
                if (name.length() > COLUMN_NAME_LIMIT) {
                    name = name.substring(0, COLUMN_NAME_LIMIT - 1);
                }
                long start = System.currentTimeMillis();
                psCheckName = SqlUtils.getPreparedStatement(dataSource, sql);
                psCheckName.setInt(1, userId);
                psCheckName.setString(2, wdkModel.getProjectId());
                psCheckName.setString(3, name);
                psCheckName.setBoolean(4, saved);
                psCheckName.setBoolean(5, hidden);
                rsCheckName = psCheckName.executeQuery();
                SqlUtils.verifyTime(wdkModel, sql,
                        "wdk-step-factory-check-strategy-name", start);

                if (rsCheckName.next())
                    return loadStrategy(user,
                            rsCheckName.getInt(COLUMN_DISPLAY_ID), false);
            } else {// otherwise, generate default name
                name = getNextName(user, root.getCustomName(), saved);
            }
        }
        finally {
            SqlUtils.closeResultSet(rsCheckName);
        }

        int displayId;
        PreparedStatement psMax = null;
        PreparedStatement psStrategy = null;
        ResultSet rsMax = null;
        Connection connection = dataSource.getConnection();

        int internalId = userPlatform.getNextId(userSchema, TABLE_STRATEGY);
        String signature = getStrategySignature(user.getUserId(), internalId);
        sql = "SELECT max(" + COLUMN_DISPLAY_ID + ") max_id FROM " + userSchema
                + TABLE_STRATEGY + " WHERE " + userIdColumn + " = ? AND "
                + COLUMN_PROJECT_ID + " = ?";
        try {
            connection.setAutoCommit(false);

            // get the current max strategy id
            long start = System.currentTimeMillis();
            psMax = connection.prepareStatement(sql);
            psMax.setInt(1, userId);
            psMax.setString(2, wdkModel.getProjectId());
            rsMax = psMax.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-get-next-strategy-id", start);

            if (rsMax.next()) displayId = rsMax.getInt("max_id") + 1;
            else displayId = 1;

            // insert the row into strategies
            sql = "INSERT INTO " + userSchema + TABLE_STRATEGY + " ("
                    + COLUMN_DISPLAY_ID + ", " + COLUMN_STRATEGY_INTERNAL_ID
                    + ", " + userIdColumn + ", " + COLUMN_ROOT_STEP_ID + ", "
                    + COLUMN_IS_SAVED + ", " + COLUMN_NAME + ", "
                    + COLUMN_SAVED_NAME + ", " + COLUMN_PROJECT_ID + ", "
                    + COLUMN_IS_DELETED + ", " + COLUMN_SIGNATURE + ", "
                    + COLUMN_DESCRIPTION + ", " + COLUMN_VERSION
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            start = System.currentTimeMillis();
            psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
            psStrategy.setInt(1, displayId);
            psStrategy.setInt(2, internalId);
            psStrategy.setInt(3, userId);
            psStrategy.setInt(4, root.getDisplayId());
            psStrategy.setBoolean(5, saved);
            psStrategy.setString(6, name);
            psStrategy.setString(7, savedName);
            psStrategy.setString(8, wdkModel.getProjectId());
            psStrategy.setBoolean(9, false);
            psStrategy.setString(10, signature);
            psStrategy.setString(11, description);
            psStrategy.setString(12, wdkModel.getVersion());
            psStrategy.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-insert-strategy", start);

            logger.debug("new strategy created, internal#=" + internalId);
            connection.commit();
        }
        catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            connection.setAutoCommit(true);
            SqlUtils.closeStatement(psStrategy);
            SqlUtils.closeResultSet(rsMax);
        }

        Strategy strategy = loadStrategy(user, displayId, false);
        strategy.setLatestStep(root);
        return strategy;
    }

    int getStrategyCount(User user) throws WdkUserException, SQLException,
            WdkModelException {
        ResultSet rsStrategy = null;
        String sql = "SELECT count(*) AS num FROM " + userSchema
                + TABLE_STRATEGY + " WHERE " + Utilities.COLUMN_USER_ID
                + " = ? AND " + COLUMN_IS_DELETED + " = ? AND "
                + COLUMN_PROJECT_ID + " = ? ";
        try {
            long start = System.currentTimeMillis();
            PreparedStatement psStrategy = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psStrategy.setInt(1, user.getUserId());
            psStrategy.setBoolean(2, false);
            psStrategy.setString(3, wdkModel.getProjectId());
            rsStrategy = psStrategy.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-strategy-count", start);
            rsStrategy.next();
            return rsStrategy.getInt("num");
        }
        finally {
            SqlUtils.closeResultSet(rsStrategy);
        }
    }

    private String getParamContent(Map<String, String> params)
            throws JSONException {
        JSONObject json = new JSONObject();
        for (String paramName : params.keySet()) {
            json.put(paramName, params.get(paramName));
        }
        return json.toString();
    }

    public Map<String, String> parseParamContent(String paramContent)
            throws JSONException {
        Map<String, String> params = new LinkedHashMap<String, String>();
        if (paramContent != null && paramContent.length() > 0) {
            JSONObject json = new JSONObject(paramContent);
            String[] paramNames = JSONObject.getNames(json);
            if (paramNames != null) {
                for (String paramName : paramNames) {
                    String paramValue = json.getString(paramName);
                    logger.trace("param '" + paramName + "' = '" + paramValue
                            + "'");
                    params.put(paramName, paramValue);
                }
            }
        }
        return params;
    }

    boolean checkNameExists(Strategy strategy, String name, boolean saved)
            throws SQLException, WdkUserException, WdkModelException {
        ResultSet rsCheckName = null;
        String sql = "SELECT display_id FROM " + userSchema + TABLE_STRATEGY
                + " WHERE " + Utilities.COLUMN_USER_ID + " = ? AND "
                + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME + " = ? AND "
                + COLUMN_IS_SAVED + " = ? AND " + COLUMN_IS_DELETED
                + " = ? AND " + COLUMN_DISPLAY_ID + " <> ?";
        try {
            long start = System.currentTimeMillis();
            PreparedStatement psCheckName = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psCheckName.setInt(1, strategy.getUser().getUserId());
            psCheckName.setString(2, wdkModel.getProjectId());
            psCheckName.setString(3, name);
            psCheckName.setBoolean(4, (saved || strategy.getIsSaved()));
            psCheckName.setBoolean(5, false);
            psCheckName.setInt(6, strategy.getStrategyId());
            rsCheckName = psCheckName.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-strategy-name-exist", start);

            if (rsCheckName.next()) return true;

            return false;
        }
        finally {
            SqlUtils.closeResultSet(rsCheckName);
        }
    }

    /**
     * Copy is different from import strategy in that the copy will replicate
     * every setting of the strategy, and the new name is different with a
     * " copy" suffix.
     * 
     * @param strategy
     * @return
     * @throws JSONException
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    Strategy copyStrategy(Strategy strategy) throws SQLException,
            WdkUserException, WdkModelException, JSONException,
            NoSuchAlgorithmException {
        User user = strategy.getUser();
        Step root = strategy.getLatestStep().deepClone();
        String name = strategy.getName();
        if (!name.toLowerCase().endsWith(", copy of")) name += ", Copy of";
        name = getNextName(user, name, false);
        return createStrategy(user, root, name, null, false,
                strategy.getDescription(), false);
    }

    /**
     * copy a branch of strategy from the given step to the beginning of the
     * strategy, can make an unsaved strategy from it.
     * 
     * @param strategy
     * @param stepId
     * @return
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws JSONException
     * @throws WdkUserException
     */
    Strategy copyStrategy(Strategy strategy, int stepId) throws SQLException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            WdkUserException {
        User user = strategy.getUser();
        Step step = strategy.getStepById(stepId).deepClone();
        String name = step.getCustomName();
        if (!name.toLowerCase().endsWith(", copy of")) name += ", Copy of";
        name = getNextName(user, name, false);
        return createStrategy(user, step, name, null, false,
                strategy.getDescription(), false);
    }

    private String getNextName(User user, String oldName, boolean saved)
            throws SQLException, WdkUserException, WdkModelException {
        ResultSet rsNames = null;
        String sql = "SELECT " + COLUMN_NAME + " FROM " + userSchema
                + TABLE_STRATEGY + " WHERE " + Utilities.COLUMN_USER_ID
                + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME
                + " LIKE ? AND " + COLUMN_IS_SAVED + "= ? AND "
                + COLUMN_IS_DELETED + "= ?";
        try {
            // get the existing names
            long start = System.currentTimeMillis();
            PreparedStatement psNames = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psNames.setInt(1, user.getUserId());
            psNames.setString(2, wdkModel.getProjectId());
            psNames.setString(3, SqlUtils.escapeWildcards(oldName) + "%");
            psNames.setBoolean(4, saved);
            psNames.setBoolean(5, false);
            rsNames = psNames.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-strategy-next-name", start);

            Set<String> names = new LinkedHashSet<String>();
            while (rsNames.next())
                names.add(rsNames.getString(COLUMN_NAME));

            String name = oldName;
            Pattern pattern = Pattern.compile("(.+?)\\((\\d+)\\)");
            while (names.contains(name)) {
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches() && !name.equals(oldName)) {
                    int number = Integer.parseInt(matcher.group(2));
                    name = matcher.group(1).trim();
                    name += "(" + (++number) + ")";
                } else { // the initial name, no tailing serial number
                    name += "(2)";
                }
            }
            return name;
        }
        finally {
            SqlUtils.closeResultSet(rsNames);
        }
    }

    void updateStrategyViewTime(User user, int strategyId) throws SQLException,
            WdkUserException, WdkModelException {
        StringBuffer sql = new StringBuffer("UPDATE ");
        sql.append(userSchema).append(TABLE_STRATEGY);
        sql.append(" SET ").append(COLUMN_LAST_VIEWED_TIME + " = ? ");
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ? ");
        sql.append(" AND ").append(Utilities.COLUMN_USER_ID).append(" = ? ");
        sql.append(" AND ").append(COLUMN_DISPLAY_ID).append(" = ?");
        PreparedStatement psUpdate = null;
        try {
            long start = System.currentTimeMillis();
            psUpdate = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            psUpdate.setTimestamp(1, new Timestamp(new Date().getTime()));
            psUpdate.setString(2, wdkModel.getProjectId());
            psUpdate.setInt(3, user.getUserId());
            psUpdate.setInt(4, strategyId);
            psUpdate.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql.toString(),
                    "wdk-step-factory-update-strategy-time", start);
        }
        finally {
            SqlUtils.closeStatement(psUpdate);
        }
    }

    public String getStrategySignature(int userId, int internalId)
            throws NoSuchAlgorithmException, WdkModelException {
        String project_id = wdkModel.getProjectId();
        String content = project_id + "_" + userId + "_" + internalId
                + "_6276406938881110742";
        return Utilities.encrypt(content, true);
    }

    void setStepValidFlag(Step step) throws SQLException, WdkUserException,
            WdkModelException, JSONException {
        String sql = "UPDATE " + userSchema + TABLE_STEP + " SET "
                + COLUMN_IS_VALID + " = ? WHERE step_id = ?";
        PreparedStatement psUpdate = null;
        try {
            long start = System.currentTimeMillis();
            psUpdate = SqlUtils.getPreparedStatement(dataSource, sql);
            psUpdate.setBoolean(1, step.isValid());
            psUpdate.setInt(2, step.getInternalId());
            psUpdate.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql,
                    "wdk-step-factory-update-strategy-signature", start);
        }
        finally {
            SqlUtils.closeStatement(psUpdate);
        }
    }
}
