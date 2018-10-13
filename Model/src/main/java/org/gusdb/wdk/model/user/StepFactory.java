package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.db.SqlUtils.setNullableLong;
import static org.gusdb.fgputil.db.SqlUtils.setNullableString;
import static org.gusdb.fgputil.functional.Functions.f2Swallow;
import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.getMapFromList;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_ANSWER_FILTER;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_ASSIGNED_WEIGHT;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_COLLAPSED_NAME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_CREATE_TIME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_CUSTOM_NAME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_DESCRIPTION;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_DISPLAY_PARAMS;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_ESTIMATE_SIZE;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_IS_COLLAPSIBLE;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_IS_DELETED;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_IS_PUBLIC;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_IS_SAVED;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_LAST_MODIFIED_TIME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_LAST_RUN_TIME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_LAST_VIEWED_TIME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_LEFT_CHILD_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_NAME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_PROJECT_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_PROJECT_VERSION;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_QUESTION_NAME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_RIGHT_CHILD_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_ROOT_STEP_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_SAVED_NAME;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_SIGNATURE;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_STEP_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_STRATEGY_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_USER_ID;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.COLUMN_VERSION;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.TABLE_STEP;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.TABLE_STRATEGY;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.Oracle;
import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler.Status;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.functional.FunctionalInterfaces.BinaryFunctionWithException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.FilterOptionList;
import org.gusdb.wdk.model.answer.spec.ParamFiltersClobFormat;
import org.gusdb.wdk.model.answer.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.StepFactoryHelpers.NameCheckInfo;
import org.gusdb.wdk.model.user.StepFactoryHelpers.UserCache;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides interface to the database to find, read, and write Step and Strategy objects to DB
 * 
 * @author rdoherty
 */
public class StepFactory {

  private static final Logger LOG = Logger.getLogger(StepFactory.class);

  public static final int COLUMN_NAME_LIMIT = 200;
  public static final int UNKNOWN_SIZE = -1;

  private final WdkModel _wdkModel;
  private DataSource _userDbDs;
  private DBPlatform _userDbPlatform;
  private String _userSchema;

  public StepFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    initialize();
  }

  /**
   * Initialize the step factory.  Kept separate from constructor so unit tests can subclass and not do
   * any DB interaction.
   */
  protected void initialize() {
    DatabaseInstance userDb = _wdkModel.getUserDb();
    _userDbDs = userDb.getDataSource();
    _userDbPlatform = userDb.getPlatform();
    _userSchema = _wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  /**
   * Creates a step and adds to database
   */
  public Step createStep(User user, Question question, Map<String, String> dependentValues,
      AnswerFilterInstance filter, FilterOptionList filterOptions, int assignedWeight, boolean deleted,
      String customName, boolean isCollapsible, String collapsedName, Long strategyId) throws WdkModelException, WdkUserException {
  
    LOG.debug("Creating step!");

    // define creation time
    Date createTime = new Date();
    // TODO lastRunTime should be made null once the last_run_time field in DB is nullable.
    Date lastRunTime = new Date(createTime.getTime());

    // create the Step
    Step step = new Step(_wdkModel, user.getUserId(), getNextStepId());
    step.setCreatedTime(createTime);
    step.setLastRunTime(lastRunTime);
    step.setDeleted(deleted);
    step.setProjectId(_wdkModel.getProjectId());
    step.setProjectVersion(_wdkModel.getVersion());
    step.setCustomName(customName);
    step.setCollapsible(isCollapsible);
    step.setCollapsedName(collapsedName);
    step.setStrategyId(strategyId);
    step.setAnswerSpec(AnswerSpec.builder(_wdkModel)
      .setQuestionName(question.getFullName())
      .setQueryInstanceSpec(QueryInstanceSpec.builder().putAll(dependentValues))
      .setLegacyFilterName(filter.getName())
      .setFilterOptions(FilterOptionList.builder().fromFilterOptionList(filterOptions))
      .setAssignedWeight(assignedWeight)
      .build(ValidationLevel.SEMANTIC)
    );
    step.finish(new UserCache(user), strategyId == null ? null : getStrategyByValidId(strategyId));

    TwoTuple<Integer,Exception> runStatus = tryEstimateSize(step);
    step.setEstimateSize(runStatus.getFirst());
    step.setException(runStatus.getSecond());

    // insert step into the database
    insertStep(step);

    // update step dependencies
    if (step.isCombined()) {
      updateStepTree(step);
    }

    return step;
  }

  
  private void insertStep(Step step) throws WdkModelException {
    // prepare SQL
    String sql = new StringBuilder("INSERT INTO ")
        .append(_userSchema).append(TABLE_STEP).append(" (")
        .append(COLUMN_STEP_ID).append(", ")
        .append(COLUMN_USER_ID).append(", ")
        .append(COLUMN_CREATE_TIME).append(", ")
        .append(COLUMN_LAST_RUN_TIME).append(", ")
        .append(COLUMN_ESTIMATE_SIZE).append(", ")
        .append(COLUMN_ANSWER_FILTER).append(", ")
        .append(COLUMN_ASSIGNED_WEIGHT).append(", ")
        .append(COLUMN_PROJECT_ID).append(", ")
        .append(COLUMN_PROJECT_VERSION).append(", ")
        .append(COLUMN_QUESTION_NAME).append(", ")
        .append(COLUMN_CUSTOM_NAME).append(", ")
        .append(COLUMN_COLLAPSED_NAME).append(", ")
        .append(COLUMN_IS_DELETED).append(", ")
        .append(COLUMN_IS_COLLAPSIBLE).append(", ")
        .append(COLUMN_STRATEGY_ID).append(", ")
        .append(COLUMN_DISPLAY_PARAMS).append(", ")
        .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
        .append(", " + _userDbPlatform.convertBoolean(step.isDeleted()))
        .append(", " + _userDbPlatform.convertBoolean(step.isCollapsible()))
        .append(", ?, ?)") // save custom cases til the end
        .toString();

    PreparedStatement ps = null;
    try {
      ps = SqlUtils.getPreparedStatement(_userDbDs, sql);
      ps.setLong(1, step.getStepId());
      ps.setLong(2, step.getUserId());
      ps.setTimestamp(3, new Timestamp(step.getCreatedTime().getTime()));
      ps.setTimestamp(4, new Timestamp(step.getLastRunTime().getTime()));
      ps.setInt(5, step.getEstimateSize());
      ps.setString(6, step.getAnswerSpec().getLegacyFilterName());
      ps.setInt(7, step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight());
      ps.setString(8, _wdkModel.getProjectId());
      ps.setString(9, _wdkModel.getVersion());
      ps.setString(10, step.getQuestionName());
      setNullableString(ps, 11, step.getCustomName());
      setNullableString(ps, 12, step.getCollapsedName());
      setNullableLong(ps, 13, step.getStrategyId());
      _userDbPlatform.setClobData(ps, 14,
          JsonUtil.serialize(ParamFiltersClobFormat.formatParamFilters(step.getAnswerSpec())), false);
      ps.executeUpdate();
    }
    catch (SQLException ex) {
      throw new WdkModelException("Error while inserting step", ex);
    }
    finally {
      SqlUtils.closeStatement(ps);
    }
  }

  private static TwoTuple<Integer, Exception> tryEstimateSize(Step step) {
    try {
      // create answer
      if (!step.isRunnable()) {
        return new TwoTuple<>(-1, new WdkModelException("Passed step is not runnable."));
      }
      // is there a difference between semantically valid and runnable???  When do we want the former
      //  but not the latter?  If no difference then semantically valid must check children.  Hmmm... but
      //  want to know if we should put an 'X' on a step in the middle of a strat.  Children may or may not
      //  be valid, but if semantically valid, then no 'X' needed on a boolean.  So yes, there is a difference.
      //  TODO: need to be able to "upgrade" steps I think...  ugh
      SemanticallyValid
      Map<String, Boolean> sortingAttributes = step.getUser().getPreferences().getSortingAttributes(
          step.getQuestionName(), UserPreferences.DEFAULT_SUMMARY_VIEW_PREF_SUFFIX);
      AnswerValue answerValue = AnswerValueFactory.makeAnswer(step.getUser(), , startIndex, endIndex, sortingMap)
      AnswerValue answerValue = step.getAnswerSpec().getQuestion().makeAnswerValue(
          step.getUser(), step.getAnswerSpec().getQueryInstanceSpec().toMap(), 0, -1, sortingAttributes,
          step.getAnswerSpec().getLegacyFilter(), true, step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight());
      answerValue.setFilterOptions(step.getAnswerSpec().getFilterOptions());

      QueryInstance<?> qi = answerValue.getIdsQueryInstance();
      LOG.debug("id query name  :" + (qi == null ? "<no_query_specified>" : qi.getQuery().getFullName()));
      LOG.debug("answer checksum:" + answerValue.getChecksum());
      LOG.debug("question name:  " + step.getQuestionName());
      int estimateSize = answerValue.getResultSizeFactory().getDisplayResultSize();
      return new TwoTuple<>(estimateSize, null);
    }
    catch (Exception e) {
      LOG.error("Creating step failed", e);
      return new TwoTuple<>(-1, e);
    }
  }

  private long getNextStepId() throws WdkModelException {
    try {
      return _userDbPlatform.getNextId(_userDbDs, _userSchema, TABLE_STEP);
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  public void deleteStep(long stepId) throws WdkModelException {
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_IS_DELETED + " = " +
        _userDbPlatform.convertBoolean(true) + " WHERE " + COLUMN_STEP_ID + " = ?";
    try {
      int result = new SQLRunner(_userDbDs, sql, "wdk-step-factory-delete-step")
          .executeUpdate(new Object[]{ stepId }, new Integer[]{ Types.BIGINT });
      if (result == 0) {
        throw new WdkModelException("The Step #" + stepId + " cannot be found.");
      }
    }
    catch (Exception e) {
      WdkModelException.unwrap(e, "Could not delete step " + stepId);
    }
  }

  public void deleteSteps(User user, boolean allProjects) throws WdkModelException {
    PreparedStatement psDeleteSteps = null;
    String stepTable = _userSchema + TABLE_STEP;
    String strategyTable = _userSchema + TABLE_STRATEGY;
    String userIdColumn = Utilities.COLUMN_USER_ID;
    try {
      StringBuilder sql = new StringBuilder("DELETE FROM " + stepTable);
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
      psDeleteSteps = SqlUtils.getPreparedStatement(_userDbDs, sql.toString());
      psDeleteSteps.setLong(1, user.getUserId());
      if (!allProjects) {
        String projectId = _wdkModel.getProjectId();
        psDeleteSteps.setString(2, projectId);
        psDeleteSteps.setString(3, projectId);
        psDeleteSteps.setLong(4, user.getUserId());
      }
      psDeleteSteps.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(), "wdk-step-factory-delete-all-steps", start);

    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to delete steps", e);
    }
    finally {
      SqlUtils.closeStatement(psDeleteSteps);
    }
  }

  public void deleteStrategy(long strategyId) throws WdkModelException {
    PreparedStatement psStrategy = null;
    String sql = "DELETE FROM " + _userSchema + TABLE_STRATEGY + " WHERE " + COLUMN_STRATEGY_ID + " = ?";
    try {
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStrategy.setLong(1, strategyId);
      int result = psStrategy.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-delete-strategy", start);
      if (result == 0)
        throw new WdkModelException("The strategy #" + strategyId + " cannot be found.");
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not delete strategy", e);
    }
    finally {
      SqlUtils.closeStatement(psStrategy);
    }
  }

  public void deleteStrategies(User user, boolean allProjects) throws WdkModelException {
    PreparedStatement psDeleteStrategies = null;
    try {
      StringBuffer sql = new StringBuffer("DELETE FROM ");
      sql.append(_userSchema).append(TABLE_STRATEGY).append(" WHERE ");
      sql.append(Utilities.COLUMN_USER_ID).append(" = ?");
      if (!allProjects) {
        sql.append(" AND ").append(COLUMN_PROJECT_ID).append(" = ?");
      }
      long start = System.currentTimeMillis();
      psDeleteStrategies = SqlUtils.getPreparedStatement(_userDbDs, sql.toString());

      psDeleteStrategies.setLong(1, user.getUserId());
      if (!allProjects)
        psDeleteStrategies.setString(2, _wdkModel.getProjectId());
      psDeleteStrategies.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(), "wdk-step-factory-delete-all-strategies", start);
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not delete strategies for user " + user.getEmail() +
          ", allProjects = " + allProjects, e);
    }
    finally {
      SqlUtils.closeStatement(psDeleteStrategies);
    }
  }

  public int getStepCount(User user) throws WdkModelException {
    try {
      String sql =
          "SELECT count(*) AS num" +
          " FROM " + _userSchema + TABLE_STEP +
          " WHERE " + COLUMN_USER_ID + " = ?" +
          "   AND " + COLUMN_PROJECT_ID + " = ? " +
          "   AND is_deleted = " + _userDbPlatform.convertBoolean(false);
      SingleLongResultSetHandler result = new SQLRunner(_userDbDs, sql, "wdk-step-factory-step-count").executeQuery(
              new Object[] { user.getUserId(), _wdkModel.getProjectId() },
              new Integer[] { Types.BIGINT, Types.VARCHAR },
              new SingleLongResultSetHandler());
      if (Status.NON_NULL_VALUE.equals(result.getStatus())) {
        return result.getRetrievedValue().intValue();
      }
      throw new WdkModelException("Could not get step count for user " +
          user.getEmail() + "[status=" + result.getStatus() + "]");
    }
    catch (Exception e) {
      return WdkModelException.unwrap(e, Integer.class);
    }
  }

  public Map<Long, Step> getSteps(long userId) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC).getSteps(userId);
  }

  /**
   * @param stepId step ID for which to retrieve step
   * @return step by step ID
   * @throws WdkModelException if step not found or problem occurs
   */
  public Optional<Step> getStepById(long stepId) throws WdkModelException {
    LOG.debug("Loading step#" + stepId + "....");
    return new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC).getStepById(stepId);
  }

  public Step getStepByValidId(long stepId) throws WdkModelException {
    return getStepById(stepId).orElseThrow(() ->
        new WdkModelException("Could not find step with 'valid' ID: " + stepId));
  }

  private void updateStepTree(Step step) throws WdkModelException {
    Question question = step.getAnswerSpec().getQuestion();
    Map<String, String> displayParams = step.getAnswerSpec().getQueryInstanceSpec().toMap();

    Query query = question.getQuery();
    long leftStepId = 0;
    long rightStepId = 0;
    String customName = step.getBaseCustomName();
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
    }
    else if (query.isCombined()) {
      // transform result, set the first two params
      for (Param param : question.getParams()) {
        if (param instanceof AnswerParam) {
          AnswerParam answerParam = (AnswerParam) param;
          String stepId = displayParams.get(answerParam.getName());
          // put the first child into left, the second into right
          if (leftStepId == 0)
            leftStepId = Long.valueOf(stepId);
          else {
            rightStepId = Long.valueOf(stepId);
            break;
          }
        }
      }
    }

    dropDependency(leftStepId, COLUMN_LEFT_CHILD_ID);
    if (rightStepId != 0) {
      dropDependency(rightStepId, COLUMN_RIGHT_CHILD_ID);
    }

    step.setAndVerifyPreviousStepId(leftStepId);
    step.setAndVerifyChildStepId(rightStepId);

    // construct the update sql
    StringBuffer sql = new StringBuffer("UPDATE ");
    sql.append(_userSchema).append(TABLE_STEP).append(" SET ");
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
      psUpdateStepTree = SqlUtils.getPreparedStatement(_userDbDs, sql.toString());
      psUpdateStepTree.setString(1, customName);
      psUpdateStepTree.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(), "wdk-step-factory-update-step-tree", start);
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not update step tree.", e);
    }
    finally {
      SqlUtils.closeStatement(psUpdateStepTree);
    }
  }

  int dropDependency(long stepId, String column) throws WdkModelException {
    String sql = "UPDATE " + _userSchema + "steps SET " + column + " = null WHERE " + column + " = " + stepId;
    try {
      int count = SqlUtils.executeUpdate(_userDbDs, sql, "wdk-steps-drop-dependecy");
      if (count != 0)
        LOG.debug(count + " dependencies on step " + stepId + " is removed.");
      return count;
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  /**
   * This method updates the custom name, the time stamp of last running, isDeleted, isCollapsible, and
   * collapsed name
   * 
   * @param user
   * @param step
   * @throws WdkUserException
   * @throws SQLException
   * @throws JSONException
   * @throws WdkModelException
   * @throws NoSuchAlgorithmException
   */
  void updateStep(User user, Step step, boolean updateTime) throws WdkModelException {
    LOG.debug("updateStep(): step #" + step.getStepId() + " new custom name: '" + step.getBaseCustomName() + "'");
    // update custom name
    Date lastRunTime = updateTime ? new Date() : step.getLastRunTime();
    PreparedStatement psStep = null;
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_CUSTOM_NAME + " = ?, " +
        COLUMN_LAST_RUN_TIME + " = ?, " + COLUMN_IS_DELETED + " = ?, " + COLUMN_IS_COLLAPSIBLE + " = ?, " +
        COLUMN_COLLAPSED_NAME + " = ?, " + COLUMN_ESTIMATE_SIZE + " = ?, " +
        COLUMN_ASSIGNED_WEIGHT + " = ? WHERE " + COLUMN_STEP_ID + " = ?";
    try {
      long start = System.currentTimeMillis();
      psStep = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStep.setString(1, step.getBaseCustomName());
      psStep.setTimestamp(2, new Timestamp(lastRunTime.getTime()));
      psStep.setBoolean(3, step.isDeleted());
      psStep.setBoolean(4, step.isCollapsible());
      psStep.setString(5, step.getCollapsedName());
      psStep.setInt(6, step.getEstimateSize());
      psStep.setInt(7, step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight());
      psStep.setLong(8, step.getStepId());
      int result = psStep.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-update-step", start);
      if (result == 0)
        throw new WdkModelException("The Step #" + step.getStepId() + " of user " + user.getEmail() +
            " cannot be found.");

      // update the last run stamp
      step.setLastRunTime(lastRunTime);

      // update dependencies
      if (step.isCombined())
        updateStepTree(step);
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not update step.", e);
    }
    finally {
      SqlUtils.closeStatement(psStep);
    }
    LOG.debug("updateStep(): DONE");
  }

  void saveStepParamFilters(Step step) throws WdkModelException {
    LOG.debug("Saving params/filters of step #" + step.getStepId());
    PreparedStatement psStep = null;
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_QUESTION_NAME + " = ?, " +
        COLUMN_ANSWER_FILTER + " = ?, " + COLUMN_LEFT_CHILD_ID + " = ?, " + COLUMN_RIGHT_CHILD_ID + " = ?, " +
        COLUMN_ASSIGNED_WEIGHT + " = ?, " + COLUMN_DISPLAY_PARAMS + " = ? " +
        "    WHERE " + COLUMN_STEP_ID + " = ?";

    DBPlatform platform = _wdkModel.getUserDb().getPlatform();
    JSONObject jsContent = ParamFiltersClobFormat.formatParamFilters(step.getAnswerSpec());
    long leftId = step.getPreviousStepId();
    long childId = step.getChildStepId();
    try {
      long start = System.currentTimeMillis();
      psStep = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStep.setString(1, step.getAnswerSpec().getQuestionName());
      psStep.setString(2, step.getAnswerSpec().getLegacyFilterName());
      if (leftId != 0)
        psStep.setLong(3, leftId);
      else
        psStep.setObject(3, null);
      if (childId != 0)
        psStep.setLong(4, childId);
      else
        psStep.setObject(4, null);
      psStep.setInt(5, step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight());
      platform.setClobData(psStep, 6, JsonUtil.serialize(jsContent), false);
      psStep.setLong(7, step.getStepId());
      int result = psStep.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-save-step-params", start);
      if (result == 0)
        throw new WdkModelException("The Step #" + step.getStepId() + " cannot be found.");
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not update step.", e);
    }
    finally {
      SqlUtils.closeStatement(psStep);
    }

  }

  public Map<Long, Strategy> getStrategies(long userId, Map<Long, Strategy> invalidStrategies) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC).getStrategies(userId, invalidStrategies);
  }

  public List<Strategy> getStrategies(long userId, boolean saved, boolean recent) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC).getStrategies(userId, saved, recent);
  }

  public List<Strategy> getPublicStrategies() throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC).getPublicStrategies();
  }

  public void setStrategyPublicStatus(int stratId, boolean isPublic) throws WdkModelException {
    // contains wildcards for is_public (boolean) and strat ID (int)
    String updatePublicStratStatusSql = new StringBuilder()
        .append("UPDATE ").append(_userSchema).append(TABLE_STRATEGY)
        .append(" SET ").append(COLUMN_IS_PUBLIC).append(" = ?")
        .append(" WHERE ").append(COLUMN_STRATEGY_ID).append(" = ?").toString();
    long startTime = System.currentTimeMillis();
    PreparedStatement ps = null;
    try {
      ps = SqlUtils.getPreparedStatement(_userDbDs, updatePublicStratStatusSql);
      ps.setBoolean(1, isPublic);
      ps.setInt(2, stratId);
      int rowsUpdated = ps.executeUpdate();
      if (rowsUpdated != 1) {
        throw new WdkModelException("Non-singular (" + rowsUpdated +
            ") row updated during public strat status update.");
      }
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to update public strategy status (" + stratId + "," + isPublic + ")", e);
    }
    finally {
      QueryLogger.logEndStatementExecution(updatePublicStratStatusSql,
          "wdk-step-factory-update-public-strat-status", startTime);
      SqlUtils.closeStatement(ps);
    }
  }

  public int getPublicStrategyCount() throws WdkModelException {
    return filter(new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC)
        .getPublicStrategies(), strat -> strat.isValid()).size();
  }

  /**
   * Make a copy of the strategy, and if the original strategy's name is not ended with ", Copy of", then that
   * suffix will be appended to it. The copy will be unsaved.
   * 
   * The steps of the strategy will be cloned, and an id map will be filled during the cloning.
   * 
   * @param strategy
   * @param stepIdMap
   *          the mapping from ids of old steps to those of newly cloned ones will be put into this provided
   *          map.
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Strategy copyStrategy(Strategy strategy, Map<Long, Long> stepIdMap) throws WdkModelException,
      WdkUserException {
    String name = strategy.getName();
    if (!name.toLowerCase().endsWith(", copy of"))
      name += ", Copy of";
    return copyStrategy(strategy.getUser(), strategy, stepIdMap, name);
  }

  public Strategy copyStrategy(User user, Strategy oldStrategy, String name)
      throws WdkModelException, WdkUserException {
    Map<Long, Long> stepIdsMap = new LinkedHashMap<Long, Long>();
    return copyStrategy(user, oldStrategy, stepIdsMap, name);
  }

  public Strategy copyStrategy(User user, Strategy oldStrategy, Map<Long, Long> stepIdsMap, String name)
      throws WdkModelException, WdkUserException {

    // get a new strategy id
    long newStrategyId = getNewStrategyId();

    Step latestStep = copyStepTree(user, newStrategyId, oldStrategy.getLatestStep(), stepIdsMap);

    // String name = getNextName(user, oldStrategy.getName(), false);

    return createStrategy(user, newStrategyId, latestStep, name, null, false, oldStrategy.getDescription(),
        false, false);
  }

  public Step copyStepTree(User newUser, long newStrategyId, Step oldStep, Map<Long, Long> stepIdsMap)
      throws WdkModelException, WdkUserException {

    Map<String, String> paramValues = new HashMap<String, String>(oldStep.getParamValues());
    
    // recursively copy AnswerParams (aka child steps)
    // also copy Datasetparams (we want a fresh copy per step because we don't track what steps are using a dataset param.  A 1-1 is easiest to manage)
    copyAnswerAndDatasetParams(paramValues, newUser, newStrategyId, oldStep, stepIdsMap);

    Step newStep = StepUtilities.createStep(newUser, newStrategyId, oldStep.getQuestion(), paramValues, oldStep.getFilter(),
        oldStep.isDeleted(), false, oldStep.getAssignedWeight(), oldStep.getFilterOptions());

    // copy step properties
    newStep.setCollapsedName(oldStep.getCollapsedName());
    newStep.setCollapsible(oldStep.isCollapsible());
    newStep.setCustomName(oldStep.getBaseCustomName());
    
    // update properties on disk
    newStep.update(false);
    
    stepIdsMap.put(oldStep.getStepId(), newStep.getStepId());
    
    Events.triggerAndWait(new StepCopiedEvent(oldStep, newStep), new WdkModelException(
        "Unable to execute all operations subsequent to step copy."));

    return newStep;
  }
  
  private void copyAnswerAndDatasetParams(Map<String, String> paramValues, User newUser, long newStrategyId,
      Step oldStep, Map<Long, Long> stepIdsMap) throws WdkModelException {
    for (String paramName : paramValues.keySet()) {
      Param param = oldStep.getQuestion().getParamMap().get(paramName);
      String paramValue = paramValues.get(paramName);

      if (param instanceof AnswerParam)
        paramValues.put(paramName,
            copyAnswerParam(newUser, newStrategyId, paramValue, oldStep.getUser(), stepIdsMap));

      else if (param instanceof DatasetParam)
        paramValues.put(paramName, copyDatasetParam(newUser, paramValue, oldStep.getUser()));
    }
  }

  private String copyAnswerParam(User newUser, long newStrategyId, String paramValue, User oldUser, Map<Long, Long> stepIdsMap) throws WdkModelException {
    int oldStepId = Integer.parseInt(paramValue);
    Step oldChildStep = StepUtilities.getStep(oldUser, oldStepId);
    Step newChildStep = copyStepTree(newUser, newStrategyId, oldChildStep, stepIdsMap);
    return Long.toString(newChildStep.getStepId());
  }
  
  private String copyDatasetParam(User newUser, String paramValue, User oldUser) throws WdkModelException {
    DatasetFactory datasetFactory = _wdkModel.getDatasetFactory();
    int oldUserDatasetId = Integer.parseInt(paramValue);
    Dataset oldDataset = datasetFactory.getDataset(oldUser, oldUserDatasetId);
    Dataset newDataset = datasetFactory.cloneDataset(oldDataset, newUser);
    return Long.toString(newDataset.getDatasetId());
  }

  public Strategy getStrategyById(User user, long strategyId) throws WdkModelException, WdkUserException {
    return loadStrategy(user, strategyId, false);
  }

  public Optional<Strategy> getStrategyBySignature(String strategySignature) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC).getStrategyBySignature(strategySignature);
  }

  // This function only updates the strategies table
  void updateStrategy(User user, Strategy strategy, boolean overwrite) throws WdkModelException,
      WdkUserException {
    LOG.debug("Updating strategy internal#=" + strategy.getStrategyId() + ", overwrite=" + overwrite);

    // cannot update a saved strategy if overwrite flag is false
    if (!overwrite && strategy.getIsSaved())
      throw new WdkUserException("Cannot update a saved strategy. Please create a copy and update it, "
          + "or set overwrite flag to true.");

    // update strategy name, saved, step_id
    PreparedStatement psStrategy = null;
    PreparedStatement psCheck = null;
    ResultSet rsStrategy = null;

    long userId = user.getUserId();

    String userIdColumn = Utilities.COLUMN_USER_ID;
    try {
      if (overwrite) {
        String sql = "SELECT " + COLUMN_STRATEGY_ID + ", " + COLUMN_SIGNATURE + " FROM " + _userSchema +
            TABLE_STRATEGY + " WHERE " + userIdColumn + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " +
            COLUMN_NAME + " = ? AND " + COLUMN_IS_SAVED + " = ? AND " + COLUMN_IS_DELETED + " = ? ";
        // AND " + COLUMN_DISPLAY_ID + " <> ?";

        // If we're overwriting, need to look up saved strategy id by
        // name (only if the saved strategy is not the one we're
        // updating, i.e. the saved strategy id != this strategy id)

        // jerric - will also find the saved copy of itself, so that we
        // can keep the signature.
        long start = System.currentTimeMillis();
        psCheck = SqlUtils.getPreparedStatement(_userDbDs, sql);
        psCheck.setLong(1, userId);
        psCheck.setString(2, _wdkModel.getProjectId());
        psCheck.setString(3, strategy.getName());
        psCheck.setBoolean(4, true);
        psCheck.setBoolean(5, false);
        // psCheck.setInt(6, strategy.getStrategyId());
        rsStrategy = psCheck.executeQuery();
        QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-check-strategy-name", start);

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
            StepUtilities.deleteStrategy(user, idToDelete);
        }
      }

      Date modifiedTime = new Date();
      String sql = "UPDATE " + _userSchema + TABLE_STRATEGY + " SET " + COLUMN_NAME + " = ?, " +
          COLUMN_ROOT_STEP_ID + " = ?, " + COLUMN_SAVED_NAME + " = ?, " + COLUMN_IS_SAVED + " = ?, " +
          COLUMN_DESCRIPTION + " = ?, " + COLUMN_LAST_MODIFIED_TIME + " = ?, " + COLUMN_SIGNATURE + "= ?, " +
          COLUMN_IS_PUBLIC + " = ? " + "WHERE " + COLUMN_STRATEGY_ID + " = ?";
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStrategy.setString(1, strategy.getName());
      psStrategy.setLong(2, strategy.getLatestStep().getStepId());
      psStrategy.setString(3, strategy.getSavedName());
      psStrategy.setBoolean(4, strategy.getIsSaved());
      psStrategy.setString(5, strategy.getDescription());
      psStrategy.setTimestamp(6, new Timestamp(modifiedTime.getTime()));
      psStrategy.setString(7, strategy.getSignature());
      psStrategy.setBoolean(8, strategy.getIsPublic());
      psStrategy.setLong(9, strategy.getStrategyId());
      int result = psStrategy.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-update-strategy", start);

      strategy.setLastModifiedTime(modifiedTime);

      if (result == 0)
        throw new WdkUserException("The strategy #" + strategy.getStrategyId() + " of user " +
            user.getEmail() + " cannot be found.");
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeStatement(psStrategy);
      SqlUtils.closeResultSetAndStatement(rsStrategy, psCheck);
    }

  }

  public long getNewStrategyId() throws WdkModelException {
    try {
      return _userDbPlatform.getNextId(_userDbDs, _userSchema, TABLE_STRATEGY);
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  // Note: this function only adds the necessary row in strategies; updating
  // of answers
  // and steps tables is handled in other functions. Once the Step
  // object exists, all of this data is already in the db.
  public Strategy createStrategy(User user, Step root, String name, String savedName, boolean saved,
      String description, boolean hidden, boolean isPublic) throws WdkModelException {
    long strategyId = (root.getStrategyId() == null) ? getNextStrategyId() : root.getStrategyId();
    return createStrategy(user, root, name, savedName, saved, description, hidden, isPublic, strategyId);
  }

  public Strategy createStrategy(User user, Step root, String name, String savedName, boolean saved,
      String description, boolean hidden, boolean isPublic, long strategyId) throws WdkModelException {

    LOG.debug("creating strategy, saved=" + saved);

    long userId = user.getUserId();

    String userIdColumn = Utilities.COLUMN_USER_ID;
    PreparedStatement psCheckName = null;
    ResultSet rsCheckName = null;

    String sql =
        "SELECT " + COLUMN_STRATEGY_ID +
        " FROM " + _userSchema + TABLE_STRATEGY +
        " WHERE " + userIdColumn + " = ?" +
        "   AND " + COLUMN_PROJECT_ID + " = ?" +
        "   AND " + COLUMN_NAME + " = ?" +
        "   AND " + COLUMN_IS_SAVED + "= ?" +
        "   AND " + COLUMN_IS_DELETED + "= ?";
    try {
      // If name is not null, check if strategy exists
      if (name != null) {
        if (name.length() > COLUMN_NAME_LIMIT) {
          name = name.substring(0, COLUMN_NAME_LIMIT - 1);
        }
        long start = System.currentTimeMillis();
        psCheckName = SqlUtils.getPreparedStatement(_userDbDs, sql);
        psCheckName.setLong(1, userId);
        psCheckName.setString(2, _wdkModel.getProjectId());
        psCheckName.setString(3, name);
        psCheckName.setBoolean(4, saved);
        psCheckName.setBoolean(5, hidden);
        rsCheckName = psCheckName.executeQuery();
        QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-check-strategy-name", start);

        if (rsCheckName.next()) {
          Optional<Strategy> strategy = new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC)
              .getStrategyById(rsCheckName.getLong(COLUMN_STRATEGY_ID));
          return strategy.orElseThrow(() -> new WdkModelException("Newly created strategy could not be found."));
        }
      }
      else {// otherwise, generate default name
        name = getNextName(user, root.getCustomName(), saved);
      }
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not create strategy", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsCheckName, psCheckName);
    }

    PreparedStatement psStrategy = null;
    String signature = getStrategySignature(_wdkModel.getProjectId(), user.getUserId(), strategyId);
    try {
      // insert the row into strategies
      sql = "INSERT INTO " + _userSchema + TABLE_STRATEGY + " (" + COLUMN_STRATEGY_ID + ", " + userIdColumn +
          ", " + COLUMN_ROOT_STEP_ID + ", " + COLUMN_IS_SAVED + ", " + COLUMN_NAME + ", " +
          COLUMN_SAVED_NAME + ", " + COLUMN_PROJECT_ID + ", " + COLUMN_IS_DELETED + ", " + COLUMN_SIGNATURE +
          ", " + COLUMN_DESCRIPTION + ", " + COLUMN_VERSION + ", " + COLUMN_IS_PUBLIC +
          ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStrategy.setLong(1, strategyId);
      psStrategy.setLong(2, userId);
      psStrategy.setLong(3, root.getStepId());
      psStrategy.setBoolean(4, saved);
      psStrategy.setString(5, name);
      psStrategy.setString(6, savedName);
      psStrategy.setString(7, _wdkModel.getProjectId());
      psStrategy.setBoolean(8, false);
      psStrategy.setString(9, signature);
      psStrategy.setString(10, description);
      psStrategy.setString(11, _wdkModel.getVersion());
      psStrategy.setBoolean(12, isPublic);
      psStrategy.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-insert-strategy", start);

      LOG.debug("new strategy created, id=" + strategyId);

      // check if we need to update the strategy id on the step;
      if (root.getStrategyId() == null || root.getStrategyId() != strategyId)
        updateStrategyId(strategyId, root);
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeStatement(psStrategy);
    }

    Optional<Strategy> strategy = new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC).getStrategyById(strategyId);
    if (strategy.isPresent()) {
      strategy.get().setLatestStep(root);
    }
    return strategy.orElseThrow(() -> new WdkModelException("Newly created strategy could not be found."));
  }

  private void updateStrategyId(long strategyId, Step rootStep) throws WdkModelException {
    String stepIdSql = selectStepAndChildren(rootStep.getStepId());
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_STRATEGY_ID + " = " + strategyId +
        " WHERE step_id IN (" + stepIdSql + ")";
    try {
      SqlUtils.executeUpdate(_userDbDs, sql, "wdk-update-strategy-on-steps");
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  public int getStrategyCount(User user) throws WdkModelException {
    try {
      String sql =
        "SELECT st.question_name" +
        " FROM " + _userSchema + TABLE_STEP + " st, " + _userSchema + TABLE_STRATEGY + " sr" +
        " WHERE sr." + Utilities.COLUMN_USER_ID + " = ?" +
        " AND sr." + COLUMN_IS_DELETED + " = " + _userDbPlatform.convertBoolean(false) +
        " AND sr." + COLUMN_PROJECT_ID + " = ?" +
        " AND st." + COLUMN_STEP_ID + " = sr.root_step_id";
      Wrapper<Integer> result = new Wrapper<>();
      new SQLRunner(_userDbDs, sql, "wdk-step-factory-strategy-count").executeQuery(
        new Object[]{ user.getUserId(), _wdkModel.getProjectId() },
        new Integer[]{ Types.BIGINT, Types.VARCHAR },
        rs -> {
          int count = 0;
          while (rs.next()) {
            try {
              _wdkModel.getQuestion(rs.getString(1));
              count++;
            }
            catch (WdkModelException e) { /* invalid question; ignore */ }
          }
          result.set(count);
        });
      return result.get();
    }
    catch (Exception e) {
      WdkModelException.unwrap(e);
      return 0;
    }
  }

  public NameCheckInfo checkNameExists(Strategy strategy, String name, boolean saved) throws WdkModelException {
    PreparedStatement psCheckName = null;
    ResultSet rsCheckName = null;
    String sql = "SELECT strategy_id, is_public, description FROM " + _userSchema + TABLE_STRATEGY +
        " WHERE " + Utilities.COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME +
        " = ? AND " + COLUMN_IS_SAVED + " = ? AND " + COLUMN_IS_DELETED + " = ? AND " + COLUMN_STRATEGY_ID +
        " <> ?";
    try {
      long start = System.currentTimeMillis();
      psCheckName = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psCheckName.setLong(1, strategy.getUser().getUserId());
      psCheckName.setString(2, _wdkModel.getProjectId());
      psCheckName.setString(3, name);
      psCheckName.setBoolean(4, (saved || strategy.getIsSaved()));
      psCheckName.setBoolean(5, false);
      psCheckName.setLong(6, strategy.getStrategyId());
      rsCheckName = psCheckName.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-strategy-name-exist", start);

      if (rsCheckName.next()) {
        boolean isPublic = rsCheckName.getBoolean(2);
        String description = rsCheckName.getString(3);
        return new NameCheckInfo(true, isPublic, description);
      }
      // otherwise, no strat by this name exists
      return new NameCheckInfo(false, false, null);

    }
    catch (SQLException e) {
      throw new WdkModelException(
          "Error checking name for strategy " + strategy.getStrategyId() + ":" + name, e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsCheckName, psCheckName);
    }
  }

  private String getNextName(User user, String oldName, boolean saved) throws WdkModelException {
    PreparedStatement psNames = null;
    ResultSet rsNames = null;
    String sql = "SELECT " + COLUMN_NAME + " FROM " + _userSchema + TABLE_STRATEGY + " WHERE " +
        Utilities.COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME +
        " LIKE ? AND " + COLUMN_IS_SAVED + "= ? AND " + COLUMN_IS_DELETED + "= ?";
    try {
      // get the existing names
      long start = System.currentTimeMillis();
      psNames = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psNames.setLong(1, user.getUserId());
      psNames.setString(2, _wdkModel.getProjectId());
      psNames.setString(3, SqlUtils.escapeWildcards(oldName) + "%");
      psNames.setBoolean(4, saved);
      psNames.setBoolean(5, false);
      rsNames = psNames.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, "wdk-step-factory-strategy-next-name", start, rsNames);

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
        }
        else { // the initial name, no tailing serial number
          name += "(2)";
        }
      }
      return name;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to get next name", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsNames, psNames);
    }
  }

  void updateStrategyViewTime(int strategyId) throws WdkModelException {
    StringBuffer sql = new StringBuffer("UPDATE ");
    sql.append(_userSchema).append(TABLE_STRATEGY);
    sql.append(" SET ").append(COLUMN_LAST_VIEWED_TIME + " = ?, ");
    sql.append(COLUMN_VERSION + " = ? ");
    sql.append(" WHERE ").append(COLUMN_STRATEGY_ID).append(" = ?");
    PreparedStatement psUpdate = null;
    try {
      long start = System.currentTimeMillis();
      psUpdate = SqlUtils.getPreparedStatement(_userDbDs, sql.toString());
      psUpdate.setTimestamp(1, new Timestamp(new Date().getTime()));
      psUpdate.setString(2, _wdkModel.getVersion());
      psUpdate.setInt(3, strategyId);
      psUpdate.executeUpdate();
      QueryLogger.logEndStatementExecution(sql.toString(), "wdk-step-factory-update-strategy-time", start);
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not update strategy view time for strat with id " + strategyId, e);
    }
    finally {
      SqlUtils.closeStatement(psUpdate);
    }
  }

  public static String getStrategySignature(String projectId, long userId, long strategyId) {
    String content = projectId + "_" + userId + "_" + strategyId + "_6276406938881110742";
    return EncryptionUtil.encrypt(content, true);
  }

  /**
   * This method will reset the estimate size of the step and all other steps that depends on it.
   * 
   * @param fromStep
   * @return
   * @throws WdkModelException
   */
  int resetStepCounts(Step fromStep) throws WdkModelException {
    String selectSql = selectStepAndParents(fromStep.getStepId());
    String sql = "UPDATE " + _userSchema + "steps SET estimate_size = " + UNKNOWN_SIZE + " WHERE step_id IN (" + selectSql + ")";
    try {
      return SqlUtils.executeUpdate(_userDbDs, sql, "wdk-step-reset-count-recursive");
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  /**
   * Generates an SQL that will return the step and all the steps along the path back to the root.
   * 
   * @param stepId
   * @return an SQL that returns a step_id column.
   * @throws WdkModelException
   */
  private String selectStepAndParents(long stepId) throws WdkModelException {
    String sql;
    String stepTable = _userSchema + "steps";
    if (_userDbPlatform instanceof Oracle) {
      sql = "SELECT step_id FROM " + stepTable + " START WITH step_id = " + stepId +
          "  CONNECT BY (PRIOR step_id = left_child_id OR PRIOR step_id = right_child_id)";
    }
    else if (_userDbPlatform instanceof PostgreSQL) {
      sql = "WITH RECURSIVE parent_steps (step_id, left_child_id, right_child_id) AS (" +
          "      SELECT step_id, left_child_id, right_child_id FROM   " + stepTable +
          "      WHERE step_id = " + stepId +
          "    UNION ALL                                                                    " +
          "      SELECT s.step_id, s.left_child_id, s.right_child_id                        " +
          "      FROM " + stepTable + " s, parent_steps ps " +
          "      WHERE s.left_child_id = ps.step_id OR s.right_child_id = ps.step_id)" +
          "  SELECT step_id FROM parent_steps";
    }
    else {
      throw new WdkModelException("Unsupported platform type: " + _userDbPlatform.getClass().getName());
    }
    return sql;
  }

  /**
   * TODO - consider refactor this code into platform.
   * 
   * @param stepId
   * @return
   * @throws WdkModelException
   */
  private String selectStepAndChildren(long stepId) throws WdkModelException {
    String sql;
    String stepTable = _userSchema + "steps";
    if (_userDbPlatform instanceof Oracle) {
      sql = "SELECT step_id FROM " + stepTable + " START WITH step_id = " + stepId +
          "  CONNECT BY (step_id = PRIOR left_child_id OR step_id = PRIOR right_child_id)";
    }
    else if (_userDbPlatform instanceof PostgreSQL) {
      sql = "WITH RECURSIVE parent_steps (step_id, left_child_id, right_child_id) AS (" +
          "      SELECT step_id, left_child_id, right_child_id FROM   " + stepTable +
          "      WHERE step_id = " + stepId +
          "    UNION ALL                                                                    " +
          "      SELECT s.step_id, s.left_child_id, s.right_child_id                        " +
          "      FROM " + stepTable + " s, parent_steps ps " +
          "      WHERE ps.left_child_id = s.step_id OR ps.right_child_id = s.step_id)" +
          "  SELECT step_id FROM parent_steps";
    }
    else {
      throw new WdkModelException("Unsupported platform type: " + _userDbPlatform.getClass().getName());
    }
    return sql;
  }

  public List<Long> getStepAndParents(final long stepId) throws WdkModelException {
    final List<Long> ids = new ArrayList<>();
    new SQLRunner(_userDbDs, selectStepAndParents(stepId), "select-step-and-parent-ids")
      .executeQuery(rs -> {
        while (rs.next()) {
          ids.add(rs.getLong(1));
        }
      });
    return ids;
  }

  /**
   * Given a step, identify it and all downstream steps and set the estimate size of each to -1.
   * @param step - step to start from
   * @throws WdkModelException
   */
  public void resetEstimateSizeForThisAndDownstreamSteps(Step step) throws WdkModelException {
    try {
      String stepIdSql = selectStepAndParents(step.getStepId());
      String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_ESTIMATE_SIZE + " = -1 " +
                   " WHERE step_id IN (" + stepIdSql + ")";
      SqlUtils.executeUpdate(_userDbDs, sql, "wdk-update-estimate-size-on-steps");
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  public void setStrategyIdForThisAndUpstreamSteps(Step step, Long strategyId) throws WdkModelException {
    try {
      String stepIdSql = selectStepAndChildren(step.getStepId());
      String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_STRATEGY_ID + " = " + strategyId +
                   " WHERE step_id IN (" + stepIdSql + ")";
      SqlUtils.executeUpdate(_userDbDs, sql, "wdk-set-strategy-id-on-steps");
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

}
