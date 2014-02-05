package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 */
public class StepFactory {

  private static final String TABLE_STEP = "steps";
  private static final String TABLE_STRATEGY = "strategies";

  private static final String COLUMN_STEP_ID = "step_id";
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
  private static final String COLUMN_QUESTION_NAME = "question_name";
  private static final String COLUMN_PROJECT_VERSION = "project_version";

  private static final String COLUMN_STRATEGY_ID = "strategy_id";
  private static final String COLUMN_ROOT_STEP_ID = "root_step_id";
  private static final String COLUMN_PROJECT_ID = "project_id";
  private static final String COLUMN_IS_SAVED = "is_saved";
  private static final String COLUMN_NAME = "name";
  private static final String COLUMN_SAVED_NAME = "saved_name";
  private static final String COLUMN_VERSION = "version";
  private static final String COLUMN_IS_PUBLIC = "is_public";

  static final int COLUMN_NAME_LIMIT = 200;

  private static final Logger logger = Logger.getLogger(StepFactory.class);

  private final WdkModel wdkModel;
  private final String userSchema;
  private final DatabaseInstance userDb;
  private final DataSource dataSource;

  // define SQL snippet "constants" to avoid building SQL each time
  private final String modTimeSortSql;
  private final String basicStratsSql;
  private final String isNotDeletedCondition;
  private final String byProjectCondition;
  private final String byUserCondition;
  private final String bySignatureCondition;
  private final String isPublicCondition;
  private final String byStratIdCondition;
  private final String isSavedCondition;
  private final String byLastViewedCondition;
  private final String stratsByUserSql;
  private final String stratBySignatureSql;
  private final String unsortedPublicStratsSql;
  private final String updatePublicStratStatusSql;

  public StepFactory(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
    this.userDb = wdkModel.getUserDb();
    dataSource = userDb.getDataSource();

    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    this.userSchema = userDB.getUserSchema();

    /* define "static" SQL statements dependent only on schema name */

    String userColumn = Utilities.COLUMN_USER_ID;

    // sort options
    modTimeSortSql = new StringBuilder(" ORDER BY sr.").append(
        COLUMN_LAST_MODIFIED_TIME).append(" DESC").toString();

    // basic select with required joins
    basicStratsSql = new StringBuilder().append("SELECT sr.*").append(", sp.").append(
        COLUMN_ESTIMATE_SIZE).append(", sp.").append(COLUMN_IS_VALID).append(
        ", sp.").append(COLUMN_QUESTION_NAME).append(" FROM ").append(
        userSchema).append(TABLE_STRATEGY).append(" sr, ").append(userSchema).append(
        TABLE_STEP).append(" sp").append(" WHERE sr.").append(
        COLUMN_ROOT_STEP_ID).append(" = sp.").append(COLUMN_STEP_ID).append(
        " AND sr.").append(userColumn).append(" = sp.").append(userColumn).append(
        " AND sr.").append(COLUMN_PROJECT_ID).append(" = sp.").append(
        COLUMN_PROJECT_ID).toString();

    // conditions for strategies

    // does not add any wildcards
    isNotDeletedCondition = new StringBuilder(" AND sr.").append(
        COLUMN_IS_DELETED).append(" = ").append(
        userDb.getPlatform().convertBoolean(false)).toString();
    // adds wildcard for project ID (string)
    byProjectCondition = new StringBuilder(" AND sr.").append(COLUMN_PROJECT_ID).append(
        " = ?").toString();
    // adds wildcard for user ID (integer)
    byUserCondition = new StringBuilder(" AND sr.").append(userColumn).append(
        " = ?").toString();
    // adds wildcard for strategy ID (integer)
    byStratIdCondition = new StringBuilder(" AND sr.").append(
        COLUMN_STRATEGY_ID).append(" = ?").toString();
    // adds wildcard for isSaved (boolean)
    isSavedCondition = new StringBuilder(" AND sr.").append(COLUMN_IS_SAVED).append(
        " = ?").toString();
    // adds wildcard for lastViewedTime (timestamp)
    byLastViewedCondition = new StringBuilder(" AND sr.").append(
        COLUMN_LAST_VIEWED_TIME).append(" >= ?").toString();
    // adds wildcard for signature (string)
    bySignatureCondition = new StringBuilder(" AND sr.").append(
        COLUMN_SIGNATURE).append(" = ? ").toString();
    // does not add any wildcards
    isPublicCondition = new StringBuilder(" AND sr.").append(COLUMN_IS_PUBLIC).append(
        " = ").append(userDb.getPlatform().convertBoolean(true)).toString();

    // adds wildcard for project
    String aliveByProjectSql = new StringBuilder(basicStratsSql).append(
        isNotDeletedCondition).append(byProjectCondition).toString();

    // contains wildcards for project, user ID; does not contain ordering
    stratsByUserSql = new StringBuilder(aliveByProjectSql).append(
        byUserCondition).toString();

    // contains wildcards for project, signature; contains ordering
    stratBySignatureSql = new StringBuilder(aliveByProjectSql).append(
        bySignatureCondition).append(modTimeSortSql).toString();

    // contains wildcard for project; does not contain ordering
    unsortedPublicStratsSql = new StringBuilder(aliveByProjectSql).append(
        isPublicCondition).toString();

    // contains wildcards for is_public (boolean) and strat ID (int)
    updatePublicStratStatusSql = new StringBuilder().append("UPDATE ").append(
        userSchema).append(TABLE_STRATEGY).append(" SET ").append(
        COLUMN_IS_PUBLIC).append(" = ?").append(" WHERE ").append(
        COLUMN_STRATEGY_ID).append(" = ?").toString();

    // start the purge thread for the cache
    // new Thread(stepCache).start();
  }

  // parse boolexp to pass left_child_id, right_child_id to loadAnswer
  Step createStep(User user, Question question,
      Map<String, String> dependentValues, AnswerFilterInstance filter,
      int pageStart, int pageEnd, boolean deleted, boolean validate,
      int assignedWeight) throws WdkModelException {

    // get summary list and sorting list
    String questionName = question.getFullName();
    Map<String, Boolean> sortingAttributes = user.getSortingAttributes(questionName);

    // create answer
    AnswerValue answerValue = question.makeAnswerValue(user, dependentValues,
        pageStart, pageEnd, sortingAttributes, filter, validate, assignedWeight);

    logger.debug("id query name  :"
        + answerValue.getIdsQueryInstance().getQuery().getFullName());
    logger.debug("answer checksum:" + answerValue.getChecksum());
    logger.debug("question name:  " + question.getFullName());

    // prepare the values to be inserted.
    int userId = user.getUserId();

    String filterName = null;
    int estimateSize;
    Exception exception = null;
    try {
      if (filter != null) {
        filterName = filter.getName();
        estimateSize = answerValue.getFilterSize(filterName);
      } else
        estimateSize = answerValue.getResultSize();
    } catch (Exception ex) {
      estimateSize = 0;
      logger.error("creating step failed", ex);
      exception = ex;
    }

    // prepare SQLs
    String userIdColumn = Utilities.COLUMN_USER_ID;

    StringBuffer sqlInsertStep = new StringBuffer("INSERT INTO ");
    sqlInsertStep.append(userSchema).append(TABLE_STEP).append(" (");
    sqlInsertStep.append(COLUMN_STEP_ID).append(", ");
    sqlInsertStep.append(userIdColumn).append(", ");
    sqlInsertStep.append(COLUMN_CREATE_TIME).append(", ");
    sqlInsertStep.append(COLUMN_LAST_RUN_TIME).append(", ");
    sqlInsertStep.append(COLUMN_ESTIMATE_SIZE).append(", ");
    sqlInsertStep.append(COLUMN_ANSWER_FILTER).append(", ");
    sqlInsertStep.append(COLUMN_IS_DELETED).append(", ");
    sqlInsertStep.append(COLUMN_ASSIGNED_WEIGHT).append(", ");
    sqlInsertStep.append(COLUMN_PROJECT_ID).append(", ");
    sqlInsertStep.append(COLUMN_PROJECT_VERSION).append(", ");
    sqlInsertStep.append(COLUMN_QUESTION_NAME).append(", ");
    sqlInsertStep.append(COLUMN_DISPLAY_PARAMS).append(") ");
    sqlInsertStep.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    // Now that we have the Answer, create the Step
    Date createTime = new Date();
    Date lastRunTime = new Date(createTime.getTime());

    int stepId;

    try {
      stepId = userDb.getPlatform().getNextId(dataSource, userSchema,
          TABLE_STEP);
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }

    PreparedStatement psInsertStep = null;
    try {
      String displayParamContent = getParamContent(dependentValues);

      psInsertStep = SqlUtils.getPreparedStatement(dataSource,
          sqlInsertStep.toString());
      psInsertStep.setInt(1, stepId);
      psInsertStep.setInt(2, userId);
      psInsertStep.setTimestamp(3, new Timestamp(createTime.getTime()));
      psInsertStep.setTimestamp(4, new Timestamp(lastRunTime.getTime()));
      psInsertStep.setInt(5, estimateSize);
      psInsertStep.setString(6, filterName);
      psInsertStep.setBoolean(7, deleted);
      psInsertStep.setInt(8, assignedWeight);
      psInsertStep.setString(9, wdkModel.getProjectId());
      psInsertStep.setString(10, wdkModel.getVersion());
      psInsertStep.setString(11, questionName);
      userDb.getPlatform().setClobData(psInsertStep, 12, displayParamContent,
          false);
      psInsertStep.executeUpdate();
    } catch (SQLException | JSONException ex) {
      throw new WdkModelException("Error while creating step", ex);
    } finally {
      SqlUtils.closeStatement(psInsertStep);
    }
    // create the Step
    Step step = new Step(this, user, stepId);
    step.setQuestionName(questionName);
    step.setCreatedTime(createTime);
    step.setLastRunTime(lastRunTime);
    step.setDeleted(deleted);
    step.setParamValues(dependentValues);
    step.setAnswerValue(answerValue);
    step.setEstimateSize(estimateSize);
    step.setAssignedWeight(assignedWeight);
    step.setException(exception);
    step.setProjectVersion(wdkModel.getVersion());

    // update step dependencies
    updateStepTree(user, step);

    return step;
  }

  void deleteStep(int stepId) throws WdkModelException {
    PreparedStatement psHistory = null;
    String sql;
    try {
      long start = System.currentTimeMillis();
      if (!isStepDepended(stepId)) {
        // remove step
        sql = "DELETE FROM " + userSchema + TABLE_STEP + " WHERE "
            + COLUMN_STEP_ID + " = ?";
        psHistory = SqlUtils.getPreparedStatement(dataSource, sql);
      } else { // hide the step
        sql = "UPDATE " + userSchema + TABLE_STEP + " SET " + COLUMN_IS_DELETED
            + " = 1 WHERE " + COLUMN_STEP_ID + " = ?";
        psHistory = SqlUtils.getPreparedStatement(dataSource, sql);
      }
      psHistory.setInt(1, stepId);
      int result = psHistory.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-delete-step",
          start);
      if (result == 0)
        throw new WdkModelException("The Step #" + stepId + " cannot be found.");

      // stepCache.removeStep(user.getUserId(), displayId);
    } catch (SQLException e) {
      throw new WdkModelException("Could not delete step " + stepId, e);
    } finally {
      SqlUtils.closeStatement(psHistory);
    }
  }

  boolean isStepDepended(int stepId) throws WdkModelException {
    try {
      StringBuffer sql = new StringBuffer("SELECT count(*) FROM ");
      sql.append(userSchema).append(TABLE_STEP);
      sql.append(" WHERE ").append(COLUMN_LEFT_CHILD_ID + " = " + stepId);
      sql.append(" OR ").append(COLUMN_RIGHT_CHILD_ID + " = " + stepId);

      Object result = SqlUtils.executeScalar(dataSource, sql.toString(),
          "wdk-step-factory-check-depended");
      int count = Integer.parseInt(result.toString());
      return (count > 0);
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  void deleteSteps(User user, boolean allProjects) throws WdkModelException {
    PreparedStatement psDeleteSteps = null;
    String stepTable = userSchema + TABLE_STEP;
    String strategyTable = userSchema + TABLE_STRATEGY;
    String userIdColumn = Utilities.COLUMN_USER_ID;
    try {
      StringBuffer sql = new StringBuffer("DELETE FROM " + stepTable);
      sql.append(" WHERE " + userIdColumn + " = ? ");
      if (!allProjects) {
        sql.append(" AND " + COLUMN_PROJECT_ID + " = ? ");
      }
      sql.append(" AND ").append(COLUMN_STEP_ID);
      sql.append(" NOT IN (SELECT ").append(COLUMN_ROOT_STEP_ID);
      sql.append(" FROM ").append(strategyTable);
      if (!allProjects) {
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ? ");
        sql.append(" AND ").append(userIdColumn).append(" = ? ");
      }
      sql.append(") ");

      long start = System.currentTimeMillis();
      psDeleteSteps = SqlUtils.getPreparedStatement(dataSource, sql.toString());
      psDeleteSteps.setInt(1, user.getUserId());
      if (!allProjects) {
        String projectId = wdkModel.getProjectId();
        psDeleteSteps.setString(2, projectId);
        psDeleteSteps.setString(3, projectId);
        psDeleteSteps.setInt(4, user.getUserId());
      }
      psDeleteSteps.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(),
          "wdk-step-factory-delete-all-steps", start);

      // stepCache.removeSteps(user.getUserId());
    } catch (SQLException e) {
      throw new WdkModelException("Unable to delete steps", e);
    } finally {
      SqlUtils.closeStatement(psDeleteSteps);
    }
  }

  public void deleteStrategy(int strategyId) throws WdkModelException {
    PreparedStatement psStrategy = null;
    String sql = "UPDATE " + userSchema + TABLE_STRATEGY + " SET "
        + COLUMN_IS_DELETED + " = ? WHERE " + COLUMN_STRATEGY_ID + " = ?";
    try {
      // remove history
      /*
       * psStrategy = SqlUtils.getPreparedStatement(dataSource, "DELETE " +
       * "FROM " + userSchema + TABLE_STRATEGY + " WHERE " +
       * Utilities.COLUMN_USER_ID + " = ? " + "AND " + COLUMN_PROJECT_ID +
       * " = ? AND " + COLUMN_DISPLAY_ID + " = ?"); psStrategy.setInt(1,
       * user.getUserId()); psStrategy.setString(2, wdkModel.getProjectId());
       * psStrategy.setInt(3, displayId); int result =
       * psStrategy.executeUpdate(); if (result == 0) throw new
       * WdkUserException("The strategy #" + displayId + " of user " +
       * user.getEmail() + " cannot be found.");
       */
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
      psStrategy.setBoolean(1, true);
      psStrategy.setInt(2, strategyId);
      int result = psStrategy.executeUpdate();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-delete-strategy", start);
      if (result == 0)
        throw new WdkModelException("The strategy #" + strategyId
            + " cannot be found.");
    } catch (SQLException e) {
      throw new WdkModelException("Could not delete strategy", e);
    } finally {
      SqlUtils.closeStatement(psStrategy);
    }
  }

  void deleteStrategies(User user, boolean allProjects)
      throws WdkModelException {
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
      QueryLogger.logEndStatementExecution(sql.toString(),
          "wdk-step-factory-delete-all-strategies", start);
    } catch (SQLException e) {
      throw new WdkModelException("Could not delete strategies for user "
          + user.getEmail() + ", allProjects = " + allProjects, e);
    } finally {
      SqlUtils.closeStatement(psDeleteStrategies);
    }
  }

  void deleteInvalidSteps(User user) throws WdkModelException {
    // get invalid histories
    Map<Integer, Step> invalidSteps = new LinkedHashMap<Integer, Step>();
    loadSteps(user, invalidSteps);
    for (int stepId : invalidSteps.keySet()) {
      deleteStep(stepId);
    }
  }

  void deleteInvalidStrategies(User user) throws WdkModelException {
    // get invalid histories
    Map<Integer, Strategy> invalidStrategies = new LinkedHashMap<Integer, Strategy>();
    loadStrategies(user, invalidStrategies);
    for (int strategyId : invalidStrategies.keySet()) {
      deleteStrategy(strategyId);
    }
  }

  int getStepCount(User user) throws WdkModelException {
    String stepTable = userSchema + TABLE_STEP;
    ResultSet rsStep = null;
    String sql = "SELECT count(*) AS num FROM " + stepTable + " WHERE "
        + Utilities.COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ? "
        + " AND is_deleted = ?";
    try {
      long start = System.currentTimeMillis();
      PreparedStatement psHistory = SqlUtils.getPreparedStatement(dataSource,
          sql);
      psHistory.setInt(1, user.getUserId());
      psHistory.setString(2, wdkModel.getProjectId());
      psHistory.setBoolean(3, false);
      rsStep = psHistory.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-step-count",
          start);
      rsStep.next();
      return rsStep.getInt("num");
    } catch (SQLException ex) {
      throw new WdkModelException("Could not get step count for user "
          + user.getEmail(), ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsStep);
    }
  }

  Map<Integer, Step> loadSteps(User user, Map<Integer, Step> invalidSteps)
      throws WdkModelException {
    Map<Integer, Step> steps = new LinkedHashMap<Integer, Step>();

    ResultSet rsStep = null;
    String sql = "SELECT * FROM " + userSchema + TABLE_STEP + " WHERE "
        + Utilities.COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ? "
        + " ORDER BY " + COLUMN_LAST_RUN_TIME + " DESC";
    try {
      long start = System.currentTimeMillis();
      PreparedStatement psStep = SqlUtils.getPreparedStatement(dataSource, sql);
      psStep.setInt(1, user.getUserId());
      psStep.setString(2, wdkModel.getProjectId());
      rsStep = psStep.executeQuery();
      QueryLogger.logStartResultsProcessing(sql,
          "wdk-step-factory-load-all-steps", start, rsStep);

      while (rsStep.next()) {
        Step step = loadStep(user, rsStep);
        int stepId = step.getStepId();
        steps.put(stepId, step);
        if (!step.isValid())
          invalidSteps.put(stepId, step);
      }
    } catch (SQLException ex) {
      throw new WdkModelException("Could not load steps for user", ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsStep);
    }
    logger.debug("Steps: " + steps.size());
    logger.debug("Invalid: " + invalidSteps.size());
    return steps;
  }

  // get left child id, right child id in here
  Step loadStep(User user, int stepId) throws WdkModelException {
    ResultSet rsStep = null;
    String sql = "SELECT * FROM " + userSchema + TABLE_STEP + " WHERE "
        + COLUMN_STEP_ID + " = ?";
    try {
      long start = System.currentTimeMillis();
      PreparedStatement psStep = SqlUtils.getPreparedStatement(dataSource, sql);
      psStep.setInt(1, stepId);
      rsStep = psStep.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-load-step",
          start);
      if (!rsStep.next())
        throw new WdkModelException("The Step #" + stepId + " of user "
            + user.getEmail() + " doesn't exist.");

      return loadStep(user, rsStep);
    } catch (SQLException ex) {
      throw new WdkModelException("Unable to load step.", ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsStep);
    }
  }

  private Step loadStep(User user, ResultSet rsStep) throws WdkModelException,
      SQLException {
    logger.debug("\nStepFactory: loadStep()\n");

    // load Step info
    int stepId = rsStep.getInt(COLUMN_STEP_ID);

    Step step = new Step(this, user, stepId);
    step.setQuestionName(rsStep.getString(COLUMN_QUESTION_NAME));
    step.setCreatedTime(rsStep.getTimestamp(COLUMN_CREATE_TIME));
    step.setLastRunTime(rsStep.getTimestamp(COLUMN_LAST_RUN_TIME));
    step.setCustomName(rsStep.getString(COLUMN_CUSTOM_NAME));
    step.setDeleted(rsStep.getBoolean(COLUMN_IS_DELETED));
    step.setCollapsible(rsStep.getBoolean(COLUMN_IS_COLLAPSIBLE));
    step.setCollapsedName(rsStep.getString(COLUMN_COLLAPSED_NAME));
    step.setEstimateSize(rsStep.getInt(COLUMN_ESTIMATE_SIZE));
    step.setFilterName(rsStep.getString(COLUMN_ANSWER_FILTER));
    step.setProjectVersion(rsStep.getString(COLUMN_PROJECT_VERSION));
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

    String dependentParamContent = userDb.getPlatform().getClobData(rsStep,
        COLUMN_DISPLAY_PARAMS);
    step.setParamValues(parseParamContent(dependentParamContent));

    logger.debug("loaded step #" + stepId);
    return step;
  }

  private void updateStepTree(User user, Step step) throws WdkModelException {
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
          if (leftStepId == 0)
            leftStepId = Integer.valueOf(stepId);
          else {
            rightStepId = Integer.valueOf(stepId);
            break;
          }
        }
      }
      customName = step.getBaseCustomName();
    } else
      customName = step.getBaseCustomName();

    step.setPreviousStepId(leftStepId);
    step.setChildStepId(rightStepId);

    // construct the update sql
    StringBuffer sql = new StringBuffer("UPDATE ");
    sql.append(userSchema).append(TABLE_STEP).append(" SET ");
    sql.append(COLUMN_CUSTOM_NAME).append(" = ? ");
    if (query.isCombined()) {
      sql.append(", " + COLUMN_LEFT_CHILD_ID + " = " + leftStepId);
      if (rightStepId != 0) {
        sql.append(", " + COLUMN_RIGHT_CHILD_ID + " = " + rightStepId);
      }
    }
    sql.append(" WHERE " + COLUMN_STEP_ID + " = " + step.getStepId());

    step.setCustomName(customName);
    PreparedStatement psUpdateStepTree = null;
    try {
      long start = System.currentTimeMillis();
      psUpdateStepTree = SqlUtils.getPreparedStatement(dataSource,
          sql.toString());
      psUpdateStepTree.setString(1, customName);
      psUpdateStepTree.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(),
          "wdk-step-factory-update-step-tree", start);
    } catch (SQLException e) {
      throw new WdkModelException("Could not update step tree.", e);
    } finally {
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
      throws WdkModelException {
    logger.debug("step #" + step.getStepId() + " new custom name: '"
        + step.getBaseCustomName() + "'");
    // update custom name
    Date lastRunTime = (updateTime) ? new Date() : step.getLastRunTime();
    int estimateSize = step.getEstimateSize();
    PreparedStatement psStep = null;
    String sql = "UPDATE " + userSchema + TABLE_STEP + " SET "
        + COLUMN_CUSTOM_NAME + " = ?, " + COLUMN_LAST_RUN_TIME + " = ?, "
        + COLUMN_IS_DELETED + " = ?, " + COLUMN_IS_COLLAPSIBLE + " = ?, "
        + COLUMN_COLLAPSED_NAME + " = ?, " + COLUMN_ESTIMATE_SIZE + " = ?, "
        + COLUMN_IS_VALID + " = ?, " + COLUMN_ASSIGNED_WEIGHT + " = ? WHERE "
        + COLUMN_STEP_ID + " = ?";
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
      psStep.setInt(9, step.getStepId());
      int result = psStep.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-update-step",
          start);
      if (result == 0)
        throw new WdkModelException("The Step #" + step.getStepId()
            + " of user " + user.getEmail() + " cannot be found.");

      // update the last run stamp
      step.setLastRunTime(lastRunTime);
      step.setEstimateSize(estimateSize);

      // update dependencies
      if (step.isCombined())
        updateStepTree(user, step);
    } catch (SQLException e) {
      throw new WdkModelException("Could not update step.", e);
    } finally {
      SqlUtils.closeStatement(psStep);
    }
  }

  Map<Integer, Strategy> loadStrategies(User user,
      Map<Integer, Strategy> invalidStrategies) throws WdkModelException {
    Map<Integer, Strategy> userStrategies = new LinkedHashMap<Integer, Strategy>();
    String sql = stratsByUserSql + modTimeSortSql;
    PreparedStatement psStrategyIds = null;
    ResultSet rsStrategyIds = null;
    try {
      long start = System.currentTimeMillis();
      psStrategyIds = SqlUtils.getPreparedStatement(dataSource, sql);
      psStrategyIds.setString(1, wdkModel.getProjectId());
      psStrategyIds.setInt(2, user.getUserId());
      rsStrategyIds = psStrategyIds.executeQuery();
      QueryLogger.logStartResultsProcessing(sql,
          "wdk-step-factory-load-all-strategies", start, rsStrategyIds);
      List<Strategy> strategies = loadStrategies(user, rsStrategyIds);
      for (Strategy strategy : strategies) {
        userStrategies.put(strategy.getStrategyId(), strategy);
        if (!strategy.isValid())
          invalidStrategies.put(strategy.getStrategyId(), strategy);
      }
      return userStrategies;
    } catch (SQLException sqle) {
      throw new WdkModelException("Could not load strategies for user "
          + user.getEmail(), sqle);
    } finally {
      SqlUtils.closeStatement(psStrategyIds);
      SqlUtils.closeResultSetAndStatement(rsStrategyIds);
    }
  }

  List<Strategy> loadStrategies(User user, boolean saved, boolean recent)
      throws WdkModelException {
    StringBuilder sql = new StringBuilder(stratsByUserSql).append(isSavedCondition);
    if (recent)
      sql.append(byLastViewedCondition);
    sql.append(modTimeSortSql);

    List<Strategy> strategies;
    ResultSet resultSet = null;
    try {
      long start = System.currentTimeMillis();
      PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
          sql.toString());
      ps.setString(1, wdkModel.getProjectId());
      ps.setInt(2, user.getUserId());
      ps.setBoolean(3, saved);
      if (recent) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date date = new Date();
        ps.setTimestamp(4, new Timestamp(date.getTime()));
      }
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(sql.toString(),
          "wdk-step-factory-load-strategies", start, resultSet);
      strategies = loadStrategies(user, resultSet);
    } catch (SQLException e) {
      throw new WdkModelException("Could not load strategies for user "
          + user.getEmail(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    Collections.sort(strategies, new Comparator<Strategy>() {
      @Override
      public int compare(Strategy o1, Strategy o2) {
        return o2.getLastRunTime().compareTo(o1.getLastRunTime());
      }
    });
    return strategies;
  }

  public List<Strategy> loadPublicStrategies() throws WdkModelException {
    ResultSet resultSet = null;
    try {
      String publicStratsSql = unsortedPublicStratsSql + modTimeSortSql;
      long start = System.currentTimeMillis();
      PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
          publicStratsSql);
      ps.setString(1, wdkModel.getProjectId());
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(publicStratsSql,
          "wdk-step-factory-load-public-strategies", start, resultSet);
      return loadStrategies(null, resultSet);
    } catch (SQLException e) {
      throw new WdkModelException("Unable to load public strategies", e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  public void setStrategyPublicStatus(int stratId, boolean isPublic)
      throws WdkModelException {
    long startTime = System.currentTimeMillis();
    PreparedStatement ps = null;
    try {
      ps = SqlUtils.getPreparedStatement(dataSource, updatePublicStratStatusSql);
      ps.setBoolean(1, isPublic);
      ps.setInt(2, stratId);
      int rowsUpdated = ps.executeUpdate();
      if (rowsUpdated != 1) {
        throw new WdkModelException("Non-singular (" + rowsUpdated
            + ") row updated during public strat status update.");
      }
    } catch (SQLException e) {
      throw new WdkModelException("Unable to update public strategy status"
          + " (" + stratId + "," + isPublic + ")", e);
    } finally {
      QueryLogger.logEndStatementExecution(updatePublicStratStatusSql,
          "wdk-step-factory-update-public-strat-status", startTime);
      SqlUtils.closeStatement(ps);
    }
  }

  public int getPublicStrategyCount() throws WdkModelException {
    String countSql = "SELECT COUNT(1) FROM (" + unsortedPublicStratsSql + ") public_strats";
    ResultSet resultSet = null;
    try {
      long start = System.currentTimeMillis();
      PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource, countSql);
      ps.setString(1, wdkModel.getProjectId());
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(countSql,
          "wdk-step-factory-count-public-strategies", start, resultSet);
      if (resultSet.next()) {
        return resultSet.getInt(1);
      }
      throw new WdkModelException("Count query returned no rows.");
    } catch (SQLException e) {
      throw new WdkModelException("Unable to count public strategies", e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  private List<Strategy> loadStrategies(User user, ResultSet resultSet)
      throws WdkModelException, SQLException {
    List<Strategy> strategies = new ArrayList<>();
    boolean loadUserPerStrat = (user == null);
    Map<Integer, User> userMap = new HashMap<Integer, User>();

    while (resultSet.next()) {

      // load user if needed
      if (loadUserPerStrat) {
        Integer userId = resultSet.getInt(Utilities.COLUMN_USER_ID);
        user = userMap.get(userId);
        if (user == null) {
          user = wdkModel.getUserFactory().getUser(userId);
          userMap.put(userId, user);
        }
      }

      int strategyId = resultSet.getInt(COLUMN_STRATEGY_ID);

      Strategy strategy = new Strategy(this, user, strategyId);
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
      if (resultSet.getObject(COLUMN_IS_PUBLIC) != null)
        strategy.setIsPublic(resultSet.getBoolean(COLUMN_IS_PUBLIC));

      // load recordClass for the strategy
      String questionName = resultSet.getString(COLUMN_QUESTION_NAME);
      try {
        Question question = wdkModel.getQuestion(questionName);
        strategy.setRecordClass(question.getRecordClass());
      } catch (WdkModelException ex) { // the question doesn't exist
        // skip such strategies for now
        continue;
        // strategy.setValid(false);
      }

      String signature = strategy.getSignature();
      if (signature == null || signature.trim().length() == 0) {
        signature = getStrategySignature(user.getUserId(), strategyId);
        String sql = "UPDATE " + userSchema + TABLE_STRATEGY
            + " SET signature = " + "'" + signature + "' WHERE strategy_id = "
            + strategyId;
        SqlUtils.executeUpdate(dataSource, sql,
            "wdk-step-factory-update-strategy-signature");
        strategy.setSignature(signature);
      }

      strategies.add(strategy);
    }

    return strategies;
  }

  Strategy importStrategy(User user, Strategy oldStrategy,
      Map<Integer, Integer> stepIdsMap) throws WdkModelException,
      WdkUserException {
    logger.debug("import strategy #" + oldStrategy.getStrategyId()
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
        oldStrategy.getDescription(), false, false);
    return loadStrategy(user, strategy.getStrategyId(), false);
  }

  Step importStep(User newUser, Step oldStep, Map<Integer, Integer> stepIdsMap)
      throws WdkModelException {
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
        Step newChildStep = importStep(newUser, oldChildStep, stepIdsMap);
        paramValue = Integer.toString(newChildStep.getStepId());
      } else if (param instanceof DatasetParam) {
        DatasetFactory datasetFactory = wdkModel.getDatasetFactory();
        int oldUserDatasetId = Integer.parseInt(paramValue);
        Dataset oldDataset = oldUser.getDataset(oldUserDatasetId);
        Dataset newDataset = datasetFactory.cloneDataset(oldDataset, newUser);
        paramValue = Integer.toString(newDataset.getDatasetId());
      }
      paramValues.put(paramName, paramValue);
    }

    int startIndex = 1;
    int endIndex = oldStep.getUser().getItemsPerPage();
    boolean deleted = oldStep.isDeleted();
    int assignedWeight = oldStep.getAssignedWeight();
    Step newStep = newUser.createStep(question, paramValues, filter,
        startIndex, endIndex, deleted, false, assignedWeight);
    stepIdsMap.put(oldStep.getStepId(), newStep.getStepId());
    newStep.setCollapsedName(oldStep.getCollapsedName());
    newStep.setCollapsible(oldStep.isCollapsible());
    String customName = oldStep.getBaseCustomName();
    if (customName != null)
      newStep.setCustomName(customName);
    newStep.setValid(oldStep.isValid());
    newStep.update(false);
    return newStep;
  }

  public Strategy getStrategyById(int strategyId) throws WdkModelException,
      WdkUserException {
    return loadStrategy(null, strategyId, false);
  }

  Strategy loadStrategy(User user, int strategyId, boolean allowDeleted)
      throws WdkModelException, WdkUserException {
    PreparedStatement psStrategy = null;
    ResultSet rsStrategy = null;
    try {
      String sql = basicStratsSql + byStratIdCondition;
      if (!allowDeleted) {
        sql += isNotDeletedCondition;
      }
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
      psStrategy.setInt(1, strategyId);
      rsStrategy = psStrategy.executeQuery();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-load-strategy-by-id", start);
      List<Strategy> strategies = loadStrategies(user, rsStrategy);

      if (strategies.size() == 0) {
        throw new WdkUserException("The strategy " + strategyId
            + " does not exist " + "for user "
            + (user == null ? "null" : user.getEmail()));
      } else if (strategies.size() > 1) {
        throw new WdkModelException("More than one strategy of id "
            + strategyId + " exists.");
      }

      Strategy strategy = strategies.get(0);
      // Set saved name, if any
      /*
       * if (!strategy.getName().matches("^New Strategy(\\([\\d]+\\))?\\*$")) {
       * // System.out.println("Name does not match: " + // strategy.getName());
       * // Remove any (and everything after it) from name, set as // saved name
       * strategy.setSavedName(strategy.getName().replaceAll(
       * "(\\([\\d]+\\))?\\*$", "")); }
       */
      return strategy;
    } catch (SQLException e) {
      throw new WdkModelException("Unable to load strategies for user "
          + user.getEmail(), e);
    } finally {
      SqlUtils.closeStatement(psStrategy);
      SqlUtils.closeResultSetAndStatement(rsStrategy);
    }
  }

  Strategy loadStrategy(String strategySignature) throws WdkModelException,
      WdkUserException {
    ResultSet resultSet = null;
    PreparedStatement ps = null;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(dataSource, stratBySignatureSql);
      ps.setString(1, wdkModel.getProjectId());
      ps.setString(2, strategySignature);
      resultSet = ps.executeQuery();
      QueryLogger.logEndStatementExecution(stratBySignatureSql,
          "wdk-step-factory-load-strategy-by-signature", start);
      List<Strategy> strategies = loadStrategies(null, resultSet);
      if (strategies.size() == 0) {
        throw new WdkUserException("The strategy of signature "
            + strategySignature + " doesn't exist.");
      } else if (strategies.size() > 1) {
        throw new WdkModelException("More than one strategy of signature "
            + strategySignature + " exists.");
      }
      return strategies.get(0);
    } catch (SQLException e) {
      throw new WdkModelException("Cannot load strategy with signature "
          + strategySignature, e);
    } finally {
      SqlUtils.closeStatement(ps);
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  // This function only updates the strategies table
  void updateStrategy(User user, Strategy strategy, boolean overwrite)
      throws SQLException, WdkModelException, WdkUserException {
    logger.debug("Updating strategy internal#=" + strategy.getStrategyId()
        + ", overwrite=" + overwrite);

    // update strategy name, saved, step_id
    PreparedStatement psStrategy = null;
    ResultSet rsStrategy = null;

    int userId = user.getUserId();

    String userIdColumn = Utilities.COLUMN_USER_ID;
    try {
      if (overwrite) {
        String sql = "SELECT " + COLUMN_STRATEGY_ID + ", " + COLUMN_SIGNATURE
            + " FROM " + userSchema + TABLE_STRATEGY + " WHERE " + userIdColumn
            + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME
            + " = ? AND " + COLUMN_IS_SAVED + " = ? AND " + COLUMN_IS_DELETED
            + " = ? "; // AND " + COLUMN_DISPLAY_ID + " <> ?";
        // If we're overwriting, need to look up saved strategy id by
        // name (only if the saved strategy is not the one we're
        // updating, i.e. the saved strategy id != this strategy id)

        // jerric - will also find the saved copy of itself, so that we
        // can keep the signature.
        long start = System.currentTimeMillis();
        PreparedStatement psCheck = SqlUtils.getPreparedStatement(dataSource,
            sql);
        psCheck.setInt(1, userId);
        psCheck.setString(2, wdkModel.getProjectId());
        psCheck.setString(3, strategy.getName());
        psCheck.setBoolean(4, true);
        psCheck.setBoolean(5, false);
        // psCheck.setInt(6, strategy.getStrategyId());
        rsStrategy = psCheck.executeQuery();
        QueryLogger.logEndStatementExecution(sql,
            "wdk-step-factory-check-strategy-name", start);

        // If there's already a saved strategy with this strategy's name,
        // we need to write the new saved strategy & mark the old
        // saved strategy as deleted
        if (rsStrategy.next()) {
          int idToDelete = rsStrategy.getInt(COLUMN_STRATEGY_ID);
          String signature = rsStrategy.getString(COLUMN_SIGNATURE);
          strategy.setIsSaved(true);
          strategy.setSignature(signature);
          strategy.setSavedName(strategy.getName());
          // jerric - only delete the strategy if it's a different one
          if (strategy.getStrategyId() != idToDelete)
            user.deleteStrategy(idToDelete);
        }
      } else if (strategy.getIsSaved()) {
        // If we're not overwriting a saved strategy, then we're modifying
        // it. We need to get an unsaved copy to modify. Generate unsaved name.
        // Note all new unsaved strats are private; they do not inherit public.
        String name = getNextName(user, strategy.getName(), false);
        Strategy newStrat = createStrategy(user, strategy.getLatestStep(),
            name, strategy.getName(), false, strategy.getDescription(), false, false);
        strategy.setName(newStrat.getName());
        strategy.setSavedName(newStrat.getSavedName());
        strategy.setStrategyId(newStrat.getStrategyId());
        strategy.setSignature(newStrat.getSignature());
        strategy.setIsSaved(false);
        strategy.setIsPublic(false);
      }

      Date modifiedTime = new Date();
      String sql = "UPDATE " + userSchema + TABLE_STRATEGY + " SET "
          + COLUMN_NAME + " = ?, " + COLUMN_ROOT_STEP_ID + " = ?, "
          + COLUMN_SAVED_NAME + " = ?, " + COLUMN_IS_SAVED + " = ?, "
          + COLUMN_DESCRIPTION + " = ?, " + COLUMN_LAST_MODIFIED_TIME
          + " = ?, " + COLUMN_SIGNATURE + "= ?, " + COLUMN_IS_PUBLIC + " = ? "
          + "WHERE " + COLUMN_STRATEGY_ID + " = ?";
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
      psStrategy.setString(1, strategy.getName());
      psStrategy.setInt(2, strategy.getLatestStep().getStepId());
      psStrategy.setString(3, strategy.getSavedName());
      psStrategy.setBoolean(4, strategy.getIsSaved());
      psStrategy.setString(5, strategy.getDescription());
      psStrategy.setTimestamp(6, new Timestamp(modifiedTime.getTime()));
      psStrategy.setString(7, strategy.getSignature());
      psStrategy.setBoolean(8, strategy.getIsPublic());
      psStrategy.setInt(9, strategy.getStrategyId());
      int result = psStrategy.executeUpdate();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-update-strategy", start);

      strategy.setLastModifiedTime(modifiedTime);

      if (result == 0)
        throw new WdkUserException("The strategy #" + strategy.getStrategyId()
            + " of user " + user.getEmail() + " cannot be found.");
    } finally {
      SqlUtils.closeStatement(psStrategy);
      SqlUtils.closeResultSetAndStatement(rsStrategy);
    }

  }

  // Note: this function only adds the necessary row in strategies; updating
  // of answers
  // and steps tables is handled in other functions. Once the Step
  // object exists, all of this data is already in the db.
  Strategy createStrategy(User user, Step root, String name, String savedName,
      boolean saved, String description, boolean hidden, boolean isPublic)
      throws WdkModelException, WdkUserException {
    logger.debug("creating strategy, saved=" + saved);
    int userId = user.getUserId();

    String userIdColumn = Utilities.COLUMN_USER_ID;
    ResultSet rsCheckName = null;
    PreparedStatement psCheckName;

    String sql = "SELECT " + COLUMN_STRATEGY_ID + " FROM " + userSchema
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
        QueryLogger.logEndStatementExecution(sql,
            "wdk-step-factory-check-strategy-name", start);

        if (rsCheckName.next())
          return loadStrategy(user, rsCheckName.getInt(COLUMN_STRATEGY_ID),
              false);
      } else {// otherwise, generate default name
        name = getNextName(user, root.getCustomName(), saved);
      }
    } catch (SQLException e) {
      throw new WdkModelException("Could not create strategy", e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsCheckName);
    }

    PreparedStatement psStrategy = null;
    int strategyId;
    try {
      strategyId = userDb.getPlatform().getNextId(dataSource, userSchema,
          TABLE_STRATEGY);
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }

    String signature = getStrategySignature(user.getUserId(), strategyId);
    try {
      // insert the row into strategies
      sql = "INSERT INTO " + userSchema + TABLE_STRATEGY + " ("
          + COLUMN_STRATEGY_ID + ", " + userIdColumn + ", "
          + COLUMN_ROOT_STEP_ID + ", " + COLUMN_IS_SAVED + ", " + COLUMN_NAME
          + ", " + COLUMN_SAVED_NAME + ", " + COLUMN_PROJECT_ID + ", "
          + COLUMN_IS_DELETED + ", " + COLUMN_SIGNATURE + ", "
          + COLUMN_DESCRIPTION + ", " + COLUMN_VERSION + ", "
          + COLUMN_IS_PUBLIC + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(dataSource, sql);
      psStrategy.setInt(1, strategyId);
      psStrategy.setInt(2, userId);
      psStrategy.setInt(3, root.getStepId());
      psStrategy.setBoolean(4, saved);
      psStrategy.setString(5, name);
      psStrategy.setString(6, savedName);
      psStrategy.setString(7, wdkModel.getProjectId());
      psStrategy.setBoolean(8, false);
      psStrategy.setString(9, signature);
      psStrategy.setString(10, description);
      psStrategy.setString(11, wdkModel.getVersion());
      psStrategy.setBoolean(12, isPublic);
      psStrategy.executeUpdate();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-insert-strategy", start);

      logger.debug("new strategy created, id=" + strategyId);
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    } finally {
      SqlUtils.closeStatement(psStrategy);
    }

    Strategy strategy = loadStrategy(user, strategyId, false);
    strategy.setLatestStep(root);
    return strategy;
  }

  int getStrategyCount(User user) throws WdkModelException {
    ResultSet rsStrategy = null;
    String sql = "SELECT count(*) AS num FROM " + userSchema + TABLE_STRATEGY
        + " WHERE " + Utilities.COLUMN_USER_ID + " = ? AND "
        + COLUMN_IS_DELETED + " = ? AND " + COLUMN_PROJECT_ID + " = ? ";
    try {
      long start = System.currentTimeMillis();
      PreparedStatement psStrategy = SqlUtils.getPreparedStatement(dataSource,
          sql);
      psStrategy.setInt(1, user.getUserId());
      psStrategy.setBoolean(2, false);
      psStrategy.setString(3, wdkModel.getProjectId());
      rsStrategy = psStrategy.executeQuery();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-strategy-count", start);
      rsStrategy.next();
      return rsStrategy.getInt("num");
    } catch (SQLException e) {
      throw new WdkModelException("Could not get strategy count for user "
          + user.getEmail(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsStrategy);
    }
  }

  public String getParamContent(Map<String, String> params)
      throws JSONException {
    JSONObject json = new JSONObject();
    for (String paramName : params.keySet()) {
      json.put(paramName, params.get(paramName));
    }
    return json.toString();
  }

  public Map<String, String> parseParamContent(String paramContent)
      throws WdkModelException {
    Map<String, String> params = new LinkedHashMap<String, String>();
    if (paramContent != null && paramContent.length() > 0) {
      try {
        JSONObject json = new JSONObject(paramContent);
        String[] paramNames = JSONObject.getNames(json);
        if (paramNames != null) {
          for (String paramName : paramNames) {
            String paramValue = json.getString(paramName);
            logger.trace("param '" + paramName + "' = '" + paramValue + "'");
            params.put(paramName, paramValue);
          }
        }
      } catch (JSONException ex) {
        throw new WdkModelException(ex);
      }
    }
    return params;
  }

  boolean[] checkNameExists(Strategy strategy, String name, boolean saved)
      throws WdkModelException {
    ResultSet rsCheckName = null;
    String sql = "SELECT strategy_id, is_public FROM " + userSchema + TABLE_STRATEGY
        + " WHERE " + Utilities.COLUMN_USER_ID + " = ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME + " = ? AND "
        + COLUMN_IS_SAVED + " = ? AND " + COLUMN_IS_DELETED + " = ? AND "
        + COLUMN_STRATEGY_ID + " <> ?";
    try {
      long start = System.currentTimeMillis();
      PreparedStatement psCheckName = SqlUtils.getPreparedStatement(dataSource,
          sql);
      psCheckName.setInt(1, strategy.getUser().getUserId());
      psCheckName.setString(2, wdkModel.getProjectId());
      psCheckName.setString(3, name);
      psCheckName.setBoolean(4, (saved || strategy.getIsSaved()));
      psCheckName.setBoolean(5, false);
      psCheckName.setInt(6, strategy.getStrategyId());
      rsCheckName = psCheckName.executeQuery();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-strategy-name-exist", start);

      if (rsCheckName.next()) {
    	  boolean isPublic = rsCheckName.getBoolean(2);
    	  return new boolean[] { true, isPublic };
      }
      // otherwise, no strat by this name exists
      return new boolean[] { false, false };
      
    } catch (SQLException e) {
      throw new WdkModelException("Error checking name for strategy "
          + strategy.getStrategyId() + ":" + name, e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsCheckName);
    }
  }

  /**
   * Copy is different from import strategy in that the copy will replicate
   * every setting of the strategy, and the new name is different with a " copy"
   * suffix.
   * 
   * @param strategy
   * @return
   * @throws JSONException
   * @throws WdkModelException
   * @throws WdkUserException
   * @throws SQLException
   * @throws NoSuchAlgorithmException
   */
  Strategy copyStrategy(Strategy strategy) throws WdkModelException,
      WdkUserException {
    User user = strategy.getUser();
    Step root = strategy.getLatestStep().deepClone();
    String name = strategy.getName();
    if (!name.toLowerCase().endsWith(", copy of"))
      name += ", Copy of";
    name = getNextName(user, name, false);
    return createStrategy(user, root, name, null, false, null, false, false);
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
  Strategy copyStrategy(Strategy strategy, int stepId)
      throws WdkModelException, WdkUserException {
    User user = strategy.getUser();
    Step step = strategy.getStepById(stepId).deepClone();
    String name = step.getCustomName();
    if (!name.toLowerCase().endsWith(", copy of"))
      name += ", Copy of";
    name = getNextName(user, name, false);
    return createStrategy(user, step, name, null, false, null, false, false);
  }

  private String getNextName(User user, String oldName, boolean saved)
      throws WdkModelException {
    ResultSet rsNames = null;
    String sql = "SELECT " + COLUMN_NAME + " FROM " + userSchema
        + TABLE_STRATEGY + " WHERE " + Utilities.COLUMN_USER_ID + " = ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME + " LIKE ? AND "
        + COLUMN_IS_SAVED + "= ? AND " + COLUMN_IS_DELETED + "= ?";
    try {
      // get the existing names
      long start = System.currentTimeMillis();
      PreparedStatement psNames = SqlUtils.getPreparedStatement(dataSource, sql);
      psNames.setInt(1, user.getUserId());
      psNames.setString(2, wdkModel.getProjectId());
      psNames.setString(3, SqlUtils.escapeWildcards(oldName) + "%");
      psNames.setBoolean(4, saved);
      psNames.setBoolean(5, false);
      rsNames = psNames.executeQuery();
      QueryLogger.logStartResultsProcessing(sql,
          "wdk-step-factory-strategy-next-name", start, rsNames);

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
    } catch (SQLException e) {
      throw new WdkModelException("Unable to get next name", e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rsNames);
    }
  }

  void updateStrategyViewTime(int strategyId) throws WdkModelException {
    StringBuffer sql = new StringBuffer("UPDATE ");
    sql.append(userSchema).append(TABLE_STRATEGY);
    sql.append(" SET ").append(COLUMN_LAST_VIEWED_TIME + " = ?, ");
    sql.append(COLUMN_VERSION + " = ? ");
    sql.append(" WHERE ").append(COLUMN_STRATEGY_ID).append(" = ?");
    PreparedStatement psUpdate = null;
    try {
      long start = System.currentTimeMillis();
      psUpdate = SqlUtils.getPreparedStatement(dataSource, sql.toString());
      psUpdate.setTimestamp(1, new Timestamp(new Date().getTime()));
      psUpdate.setString(2, wdkModel.getVersion());
      psUpdate.setInt(3, strategyId);
      psUpdate.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(),
          "wdk-step-factory-update-strategy-time", start);
    } catch (SQLException e) {
      throw new WdkModelException(
          "Could not update strategy view time for strat with id " + strategyId,
          e);
    } finally {
      SqlUtils.closeStatement(psUpdate);
    }
  }

  public String getStrategySignature(int userId, int internalId)
      throws WdkModelException {
    String project_id = wdkModel.getProjectId();
    String content = project_id + "_" + userId + "_" + internalId
        + "_6276406938881110742";
    return Utilities.encrypt(content, true);
  }

  void setStepValidFlag(Step step) throws SQLException, WdkModelException {
    String sql = "UPDATE " + userSchema + TABLE_STEP + " SET "
        + COLUMN_IS_VALID + " = ? WHERE " + COLUMN_STEP_ID + " = ?";
    PreparedStatement psUpdate = null;
    try {
      long start = System.currentTimeMillis();
      psUpdate = SqlUtils.getPreparedStatement(dataSource, sql);
      psUpdate.setBoolean(1, step.isValid());
      psUpdate.setInt(2, step.getStepId());
      psUpdate.executeUpdate();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-step-factory-update-strategy-signature", start);
    } finally {
      SqlUtils.closeStatement(psUpdate);
    }
  }
}
