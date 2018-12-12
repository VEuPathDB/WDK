package org.gusdb.wdk.model.user;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.Oracle;
import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.BasicArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler.Status;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList;
import org.gusdb.wdk.model.answer.spec.ParamFiltersClobFormat;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactoryHelpers.*;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.Map.Entry;

import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;
import static org.gusdb.wdk.model.user.StepContainer.withId;
import static org.gusdb.wdk.model.user.StepFactoryHelpers.*;

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
   * Initialize the step factory.  Kept separate from constructor so unit tests
   * can subclass and not do any DB interaction.
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
      String customName, boolean isCollapsible, String collapsedName, Strategy strategy) throws WdkModelException {

    LOG.debug("Creating step!");

    // define creation time
    Date createTime = new Date();
    // TODO lastRunTime should be made null once the last_run_time field in DB is nullable.
    Date lastRunTime = new Date(createTime.getTime());

    // create the Step
    Step step = Step
        .builder(_wdkModel, user.getUserId(), getNewStepId())
        .setCreatedTime(createTime)
        .setLastRunTime(lastRunTime)
        .setDeleted(deleted)
        .setProjectId(_wdkModel.getProjectId())
        .setProjectVersion(_wdkModel.getVersion())
        .setCustomName(customName)
        .setCollapsible(isCollapsible)
        .setCollapsedName(collapsedName)
        .setStrategyId(strategy == null ? null : strategy.getStrategyId())
        .setAnswerSpec(AnswerSpec.builder(_wdkModel)
            .setQuestionName(question.getFullName())
            .setQueryInstanceSpec(QueryInstanceSpec.builder().putAll(dependentValues))
            .setLegacyFilterName(filter.getName())
            .setFilterOptions(FilterOptionList.builder().fromFilterOptionList(filterOptions))
            .setAssignedWeight(assignedWeight))
        .build(new UserCache(user), ValidationLevel.RUNNABLE, strategy);

    if (step.isRunnable()) {
      TwoTuple<Integer,Exception> runStatus = tryEstimateSize(step.getRunnable().get());
      step.updateEstimatedSize(runStatus.getFirst());
      step.setException(runStatus.getSecond());
    }

    // insert step into the database
    insertStep(step);

    // update step dependencies
    if (step.isCombined()) {
      updateStepTree(step);
    }

    return step;
  }

  private static TwoTuple<Integer, Exception> tryEstimateSize(RunnableObj<Step> runnableStep) {
    try {
      // is there a difference between semantically valid and runnable???  When do we want the former
      //  but not the latter?  If no difference then semantically valid must check children.  Hmmm... but
      //  want to know if we should put an 'X' on a step in the middle of a strat.  Children may or may not
      //  be valid, but if semantically valid, then no 'X' needed on a boolean.  So yes, there is a difference.
      //  TODO: need to be able to "upgrade" steps I think...  ugh
      Step step = runnableStep.getObject();
      User user = step.getUser();
      String questionName = step.getAnswerSpec().getQuestion().getFullName();
      Map<String, Boolean> sortingAttributes = user.getPreferences().getSortingAttributes(
          questionName, UserPreferences.DEFAULT_SUMMARY_VIEW_PREF_SUFFIX);
      AnswerValue answerValue = AnswerValueFactory.makeAnswer(user, step.getAnswerSpec().toRunnable(),
          0, AnswerValue.UNBOUNDED_END_PAGE_INDEX, sortingAttributes);

      QueryInstance<?> qi = answerValue.getIdsQueryInstance();
      LOG.debug("id query name  :" + (qi == null ? "<no_query_specified>" : qi.getQuery().getFullName()));
      LOG.debug("answer checksum:" + answerValue.getChecksum());
      LOG.debug("question name:  " + questionName);
      int estimateSize = answerValue.getResultSizeFactory().getDisplayResultSize();
      return new TwoTuple<>(estimateSize, null);
    }
    catch (Exception e) {
      LOG.error("Creating step failed", e);
      return new TwoTuple<>(-1, e);
    }
  }

  private long getNewStepId() throws WdkModelException {
    try {
      return _userDbPlatform.getNextId(_userDbDs, _userSchema, TABLE_STEP);
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  @Deprecated // only orphan remover should be deleting steps
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
      sql.append(" WHERE ").append(userIdColumn).append(" = ? ");
      if (!allProjects) {
        sql.append(" AND ").append(COLUMN_PROJECT_ID).append(" = ? ");
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
    String sql = "DELETE FROM " + _userSchema + TABLE_STRATEGY +
        " WHERE " + COLUMN_STRATEGY_ID + " = ?";

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

      SingleLongResultSetHandler result = new SQLRunner(_userDbDs, sql, "wdk-step-factory-step-count")
          .executeQuery(
              new Object[] { user.getUserId(), _wdkModel.getProjectId() },
              new Integer[] { Types.BIGINT, Types.VARCHAR },
              new SingleLongResultSetHandler()
          );

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

    dropDependency(leftStepId, COLUMN_PREVIOUS_STEP_ID);
    if (rightStepId != 0) {
      dropDependency(rightStepId, COLUMN_CHILD_STEP_ID);
    }

    // construct the update sql
    StringBuilder sql = new StringBuilder("UPDATE ");
    sql.append(_userSchema).append(TABLE_STEP).append(" SET ");
    sql.append(COLUMN_CUSTOM_NAME).append(" = ? ");
    if (query.isCombined()) {
      sql.append(", " + COLUMN_PREVIOUS_STEP_ID + " = " + leftStepId);
      if (rightStepId != 0) {
        sql.append(", " + COLUMN_CHILD_STEP_ID + " = " + rightStepId);
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
   * @param setLastRunTime
   * @throws WdkModelException
   */
  void updateStep(User user, Step step, boolean setLastRunTime) throws WdkModelException {
    LOG.debug("updateStep(): step #" + step.getStepId() +
        " new custom name: '" + step.getBaseCustomName() + "'");

    // update custom name
    Date lastRunTime = setLastRunTime ? new Date() : step.getLastRunTime();

    final String sql = "UPDATE " + _userSchema + TABLE_STEP + "\n" +
        "SET\n" +
        "  " + COLUMN_CUSTOM_NAME     + " = ?,\n" +
        "  " + COLUMN_LAST_RUN_TIME   + " = ?,\n" +
        "  " + COLUMN_IS_DELETED      + " = ?,\n" +
        "  " + COLUMN_IS_COLLAPSIBLE  + " = ?,\n" +
        "  " + COLUMN_COLLAPSED_NAME  + " = ?,\n" +
        "  " + COLUMN_ESTIMATE_SIZE   + " = ?,\n" +
        "  " + COLUMN_ASSIGNED_WEIGHT + " = ?\n"  +
        "WHERE\n" +
        "  " + COLUMN_STEP_ID + " = ?";

    final int boolType = _userDbPlatform.getBooleanType();

    final int result = new SQLRunner(_userDbDs, sql).executeUpdate(
        new Object[]{
            step.getBaseCustomName(),
            new Timestamp(lastRunTime.getTime()),
            _userDbPlatform.convertBoolean(step.isDeleted()),
            _userDbPlatform.convertBoolean(step.isCollapsible()),
            step.getCollapsedName(),
            step.getEstimatedSize(),
            step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight(),
            step.getStepId()
        },
        new Integer[]{
            Types.VARCHAR,   // CUSTOM_NAME
            Types.TIMESTAMP, // LAST_RUN_TIME
            boolType,        // IS_DELETED
            boolType,        // IS_COLLAPSIBLE
            Types.VARCHAR,   // COLLAPSED_NAME
            Types.BIGINT,    // ESTIMATE_SIZE
            Types.BIGINT,    // ASSIGNED_WEIGHT
            Types.BIGINT     // STEP_ID
        }
    );

    if (result == 0)
      throw new WdkModelException("The Step #" + step.getStepId() +
          " of user " + user.getEmail() + " cannot be found.");

    // update the last run stamp
    step.setLastRunTime(lastRunTime);

    // update dependencies
    if (step.isCombined())
      updateStepTree(step);

    LOG.debug("updateStep(): DONE");
  }

  void saveStepParamFilters(Step step) throws WdkModelException {
    LOG.debug("Saving params/filters of step #" + step.getStepId());
    PreparedStatement psStep = null;
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_QUESTION_NAME + " = ?, " +
        COLUMN_ANSWER_FILTER + " = ?, " + COLUMN_PREVIOUS_STEP_ID + " = ?, " + COLUMN_CHILD_STEP_ID + " = ?, " +
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

  private Strategy getStrategyByValidId(long strategyId) throws WdkModelException {
    return getStrategyById(strategyId).orElseThrow(() ->
        new WdkModelException("Could not find strategy with 'valid' ID: " + strategyId));
  }

  public Optional<Strategy> getStrategyById(long strategyId) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC).getStrategyById(strategyId);
  }

  /**
   * This method is to be used specifically for cloning a strategy for use as a
   * step in a separate, existing strategy.  Clones of the steps of the passed
   * strategy are created and written to the DB; however, the one returned by
   * this step does NOT have a saved strategy; the DB will reflect that the new
   * steps are orphans until attached to a new strategy.
   *
   * @param user owner of the new steps
   * @param strategy strategy to clone
   * @return a step representing a branch created from an existing strategy
   * @throws WdkModelException if something goes wrong
   */
  public Step copyStrategyToBranch(User user, Strategy strategy) throws WdkModelException {

    // copy the step tree
    Collection<StepBuilder> stepBuilders = copyStepTree(user, strategy.getRootStep()).toMap().values();

    // create stub strategy- will not be saved to DB; used only to create and validate steps
    Strategy stratStub = Strategy.builder(user.getWdkModel(), user.getUserId(), 0)
        .addSteps(stepBuilders)
        .build(new UserCache(user), ValidationLevel.NONE);

    // now that strategy is created (which will be returned), clean up steps for saving to DB
    List<Step> orphanSteps = new ArrayList<>();
    for (StepBuilder step : stepBuilders) {
      step.removeStrategy();
      orphanSteps.add(step.build(new UserCache(user), ValidationLevel.NONE, null));
    }

    // write orphan steps to the DB to be used by caller
    try (Connection conn = _userDbDs.getConnection()){
      SqlUtils.performInTransaction(conn, () -> insertSteps(conn, orphanSteps));
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to insert strategy or update steps.");
    }

    // return the strategy's root step- will be used to create a branch for adding to another strat
    return stratStub.getRootStep();
  }

  /**
   *
   * @param user
   * @param oldStrategy
   * @param stepIdsMap An output map of old to new step IDs. Steps recursively encountered in the copy are added by the copy
   * @return
   * @throws WdkModelException
   */
  public Strategy copyStrategy(User user, Strategy oldStrategy, Map<Long, Long> stepIdsMap)
      throws WdkModelException {

    WdkModel wdkModel = user.getWdkModel();
    long strategyId = getNewStrategyId();
    String projectId = oldStrategy.getProjectId();
    String name = addSuffixToStratNameIfNeeded(user, oldStrategy.getName(), false);
    String signature = getStrategySignature(projectId, user.getUserId(), strategyId);
    Map<Long, StepBuilder> newStepMap = copyStepTree(user, oldStrategy.getRootStep()).toMap();

    // construct the new strategy
    Strategy newStrategy = Strategy
        .builder(wdkModel, user.getUserId(), strategyId)
        .setCreatedTime(new Date())
        .setDeleted(false)
        .setDescription(oldStrategy.getDescription())
        .setIsPublic(false)
        .setLastModifiedTime(null)
        .setLastRunTime(null)
        .setName(name)
        .setProjectId(projectId)
        .setSaved(false)
        .setSavedName(null)
        .setSignature(signature)
        .setVersion(wdkModel.getVersion())
        .addSteps(newStepMap.values())
        .build(new UserCache(user), ValidationLevel.RUNNABLE);

    // persist new strategy and all steps to the DB
    try (Connection conn = _userDbDs.getConnection()){
      SqlUtils.performInTransaction(conn,
        () -> insertStrategy(conn, newStrategy),
        () -> insertSteps(conn, newStrategy.getAllSteps())
      );
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to insert strategy or update steps.");
    }

    // trigger copy events on all steps
    for (Entry<Long,Long> stepMapping : stepIdsMap.entrySet()) {
      Events.triggerAndWait(new StepCopiedEvent(
          // using get() here because we know these steps exist
          oldStrategy.findFirstStep(withId(stepMapping.getKey())).get(),
          newStrategy.findFirstStep(withId(stepMapping.getValue())).get()),
          new WdkModelException("Unable to execute all operations subsequent to step copy."));
    }

    // populate stepIdsMap with mapping from oldId -> newId
    stepIdsMap.putAll(getMapFromKeys(newStepMap.keySet(), oldId -> newStepMap.get(oldId).getStepId()));

    return newStrategy;
  }

  /**
   * Build SQL for inserting a step record.
   *
   * @return constructed SQL insert statement.
   */
  private String buildInsertStepSQL() {
    return "INSERT INTO " + _userSchema + TABLE_STEP + " (\n" +
        "  " + COLUMN_STEP_ID         + ",\n" +
        "  " + COLUMN_USER_ID         + ",\n" +
        "  " + COLUMN_CREATE_TIME     + ",\n" +
        "  " + COLUMN_LAST_RUN_TIME   + ",\n" +
        "  " + COLUMN_ESTIMATE_SIZE   + ",\n" +
        "  " + COLUMN_ANSWER_FILTER   + ",\n" +
        "  " + COLUMN_ASSIGNED_WEIGHT + ",\n" +
        "  " + COLUMN_PROJECT_ID      + ",\n" +
        "  " + COLUMN_PROJECT_VERSION + ",\n" +
        "  " + COLUMN_QUESTION_NAME   + ",\n" +
        "  " + COLUMN_CUSTOM_NAME     + ",\n" +
        "  " + COLUMN_COLLAPSED_NAME  + ",\n" +
        "  " + COLUMN_IS_DELETED      + ",\n" +
        "  " + COLUMN_IS_COLLAPSIBLE  + ",\n" +
        "  " + COLUMN_STRATEGY_ID     + ",\n" +
        "  " + COLUMN_DISPLAY_PARAMS  + ",\n" +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  }

  /**
   * Get an array of SQL Types matching the columns for both the SQL returned by
   * {@link #buildInsertStepSQL()} as well as {@link #stepToInsertParams(Step)}
   * for use with {@link SQLRunner}.
   *
   * @return SQL type array.
   */
  private Integer[] getInsertStepParamTypes() {
    final int boolType = _userDbPlatform.getBooleanType();
    return new Integer[] {
      Types.BIGINT,    // STEP_ID
      Types.BIGINT,    // USER_ID
      Types.TIMESTAMP, // CREATE_TIME
      Types.TIMESTAMP, // LAST_RUN_TIME
      Types.BIGINT,    // ESTIMATE_SIZE
      Types.VARCHAR,   // ANSWER_FILTER
      Types.BIGINT,    // ASSIGNED_WEIGHT
      Types.VARCHAR,   // PROJECT_ID
      Types.VARCHAR,   // PROJECT_VERSION
      Types.VARCHAR,   // QUESTION_NAME
      Types.VARCHAR,   // CUSTOM_NAME
      Types.VARCHAR,   // COLLAPSED_NAME
      boolType,        // IS_DELETED
      boolType,        // IS_COLLAPSIBLE
      Types.BIGINT,    // STRATEGY_ID
      Types.CLOB,      // DISPLAY_PARAMS
    };
  }

  /**
   * Constructs an array of values from the given step matching the columns and
   * types returned by {@link #buildInsertStepSQL()} and
   * {@link #getInsertStepParamTypes()} for use with {@link SQLRunner}.
   *
   * @param step The step for which a values array should be constructed.
   *
   * @return an array of values for use in an insert query run with SQLRunner.
   */
  private Object[] stepToInsertParams(Step step) {
    final AnswerSpec spec = step.getAnswerSpec();

    return new Object[] {
        step.getStepId(),
        step.getUser().getUserId(),
        new Timestamp(step.getCreatedTime().getTime()),
        new Timestamp(step.getLastRunTime().getTime()),
        step.getEstimatedSize(),
        spec.getLegacyFilterName(),
        spec.getQueryInstanceSpec().getAssignedWeight(),
        _wdkModel.getProjectId(),
        _wdkModel.getVersion(),
        spec.getQuestionName(),
        step.getCustomName(),
        step.getCollapsedName(),
        _userDbPlatform.convertBoolean(step.isDeleted()),
        _userDbPlatform.convertBoolean(step.isCollapsible()),
        step.getStrategyId(),
        new StringReader(ParamFiltersClobFormat.formatParamFilters(spec).toString())
    };
  }

  private void insertStep(Step step) {
    new SQLRunner(_userDbDs, buildInsertStepSQL())
        .executeUpdate(stepToInsertParams(step), getInsertStepParamTypes());
  }

  private void insertSteps(Connection conn, List<Step> allSteps) {
    final BasicArgumentBatch batch = new BasicArgumentBatch();

    batch.setParameterTypes(getInsertStepParamTypes());
    allSteps.stream()
        .map(this::stepToInsertParams)
        .forEach(batch::add);

    new SQLRunner(conn, buildInsertStepSQL())
        .executeStatementBatch(batch);
  }

  private void insertStrategy(Connection connection, Strategy newStrategy) {
    String sql = "INSERT INTO " + _userSchema + TABLE_STRATEGY + " (" +
        COLUMN_STRATEGY_ID + ", " +
        COLUMN_USER_ID + ", " +
        COLUMN_ROOT_STEP_ID + ", " +
        COLUMN_IS_SAVED + ", " +
        COLUMN_NAME + ", " +
        COLUMN_SAVED_NAME + ", " +
        COLUMN_PROJECT_ID + ", " +
        COLUMN_IS_DELETED + ", " +
        COLUMN_SIGNATURE + ", " +
        COLUMN_DESCRIPTION + ", " +
        COLUMN_VERSION + ", " +
        COLUMN_IS_PUBLIC +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    new SQLRunner(connection, sql, "wdk-step-factory-insert-strategy")
      .executeStatement(
        new Object[] {
          newStrategy.getStrategyId(),
          newStrategy.getUser().getUserId(),
          newStrategy.getRootStep().getStepId(),
          _userDbPlatform.convertBoolean(newStrategy.getIsSaved()),
          newStrategy.getName(),
          newStrategy.getSavedName(),
          newStrategy.getProjectId(),
          _userDbPlatform.convertBoolean(false),
          newStrategy.getSignature(),
          newStrategy.getDescription(),
          newStrategy.getVersion(),
          _userDbPlatform.convertBoolean(newStrategy.getIsPublic())
        });
  }

  public MapBuilder<Long, StepBuilder> copyStepTree(User newUser, Step oldStep) throws WdkModelException {

    StepBuilder newStep = Step.builder(oldStep)
        .setStepId(getNewStepId())
        .setUserId(newUser.getUserId())
        .setStrategyId(null);

    // recursively copy AnswerParams (aka child steps) and DatasetParams
    //   (we want a fresh copy per step because we don't track which steps are using a dataset param)
    MapBuilder<Long,StepBuilder> childSteps = copyAnswerAndDatasetParams(
        oldStep.getUser(), oldStep.getAnswerSpec(), newUser, newStep.getAnswerSpec());

    return childSteps.put(oldStep.getStepId(), newStep);
  }

  private MapBuilder<Long, StepBuilder> copyAnswerAndDatasetParams(User oldUser, AnswerSpec oldSpec, User newUser,
      AnswerSpecBuilder newSpec) throws WdkModelException {
    MapBuilder<Long,StepBuilder> newStepMap = new MapBuilder<>();
    for (Param param : oldSpec.getQuestion().getParams()) {
      String oldStableValue = oldSpec.getQueryInstanceSpec().get(param.getName());
      String replacementValue =
          param instanceof AnswerParam ?
              cloneAnswerParam(oldSpec, oldStableValue, newUser, newStepMap) :
          param instanceof DatasetParam ?
              cloneDatasetParam(oldUser, oldStableValue, newUser) :
          // otherwise use original value
              oldStableValue;
      newSpec.setParamValue(param.getName(), replacementValue);
    }
    return newStepMap;
  }

  private String cloneAnswerParam(AnswerSpec oldSpec, String oldStableValue, User newUser,
      MapBuilder<Long, StepBuilder> stepIdsMap) throws WdkModelException {
    Step oldStepValue = oldSpec.getStepContainer()
        .findFirstStep(withId(Long.parseLong(oldStableValue)))
        .orElseThrow(() -> new WdkModelException("Step container cannot find expected step."));
    stepIdsMap.putAll(copyStepTree(newUser, oldStepValue).toMap());
    return Long.toString(stepIdsMap.get(oldStepValue.getStepId()).getStepId());
  }

  private String cloneDatasetParam(User oldUser, String oldStableValue, User newUser) throws WdkModelException {
    long oldDatasetId = Long.parseLong(oldStableValue);
    DatasetFactory datasetFactory = _wdkModel.getDatasetFactory();
    Dataset oldDataset = datasetFactory.getDataset(oldUser, oldDatasetId);
    Dataset newDataset = datasetFactory.cloneDataset(oldDataset, newUser);
    return Long.toString(newDataset.getDatasetId());
  }

  public Optional<Strategy> getStrategyBySignature(String strategySignature) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC).getStrategyBySignature(strategySignature);
  }

  private Optional<TwoTuple<Long, String>> getOverwriteStrategy(long userId,
      String name) {
    final String sql = "SELECT\n" +
        "  " + COLUMN_STRATEGY_ID + ",\n" +
        "  " + COLUMN_SIGNATURE +
        "FROM "  + _userSchema + TABLE_STRATEGY +
        "WHERE " + COLUMN_USER_ID    + " = ?\n" +
        "  AND " + COLUMN_PROJECT_ID + " = ?\n" +
        "  AND " + COLUMN_NAME       + " = ?\n" +
        "  AND " + COLUMN_IS_SAVED   + " = ?\n" +
        "  AND " + COLUMN_IS_DELETED + " = ?\n";

    // If we're overwriting, need to look up saved strategy id by
    // name (only if the saved strategy is not the one we're
    // updating, i.e. the saved strategy id != this strategy id)

    // jerric - will also find the saved copy of itself, so that we
    // can keep the signature.

    final int boolType = _userDbPlatform.getBooleanType();

    final Wrapper<Optional<TwoTuple<Long, String>>> out = new Wrapper<>();

    new SQLRunner(_userDbDs, sql)
      .executeQuery(
        new Object[]{
          userId,
          _wdkModel.getProjectId(),
          name,
          _userDbPlatform.convertBoolean(true),
          _userDbPlatform.convertBoolean(false)
        },
        new Integer[] {
          Types.BIGINT,  // USER_ID
          Types.VARCHAR, // PROJECT_ID
          Types.VARCHAR, // NAME
          boolType,      // IS_SAVED
          boolType       // IS_DELETED
        },
        rs -> {
          if (rs.next()) {
            out.set(Optional.of(new TwoTuple<>(
              rs.getLong(COLUMN_STRATEGY_ID),
              rs.getString(COLUMN_SIGNATURE)
            )));
          } else {
            out.set(Optional.empty());
          }
        }
        );

    return out.get();
  }

  /**
   * Overwrite the given strategy in the strategies table.
   *
   * @param strat Strategy to overwrite
   *
   * @return Whether or not that strategy was updated in the database.  A return
   *         value of false indicates that the strategy has not been created in
   *         the database.
   */
  public boolean updateStrategy(Strategy strat) throws WdkModelException {
    try(Connection con = _userDbDs.getConnection()) {
      return updateStrategy(con, strat);
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Overwrite the given strategy in the strategies table.
   *
   * @param con   Open connection to the DB.  This can be used to run the step
   *              update queries in a controlled connection such as a
   *              transaction.
   * @param strat Strategy to overwrite
   *
   * @return Whether or not that strategy was updated in the database.  A return
   *         value of false indicates that the strategy has not been created in
   *         the database.
   */
  public boolean updateStrategy(Connection con, Strategy strat) {
    final int boolType =_userDbPlatform.getBooleanType();
    final String sql = "UPDATE " + _userSchema + TABLE_STRATEGY + "\n" +
      "SET\n" +
      "  " + COLUMN_NAME               + " = ?,\n" +
      "  " + COLUMN_ROOT_STEP_ID       + " = ?,\n" +
      "  " + COLUMN_SAVED_NAME         + " = ?,\n" +
      "  " + COLUMN_IS_SAVED           + " = ?,\n" +
      "  " + COLUMN_DESCRIPTION        + " = ?,\n" +
      "  " + COLUMN_LAST_MODIFIED_TIME + " = ?,\n" +
      "  " + COLUMN_SIGNATURE          + " = ?,\n" +
      "  " + COLUMN_IS_PUBLIC          + " = ?\n" +
      "WHERE " + COLUMN_STRATEGY_ID + " = ?";

    return 0 < new SQLRunner(con, sql).executeUpdate(
      new Object[]{
        strat.getName(),
        strat.getRootStep().getStepId(),
        strat.getSavedName(),
        _userDbPlatform.convertBoolean(strat.getIsSaved()),
        strat.getDescription(),
        new Timestamp(strat.getLastModifiedTime().getTime()),
        strat.getSignature(),
        _userDbPlatform.convertBoolean(strat.getIsPublic()),
        strat.getStrategyId()
      },
      new Integer[]{
        Types.VARCHAR,   // NAME
        Types.BIGINT,    // ROOT_STEP_ID
        Types.VARCHAR,   // SAVED_NAME
        boolType,        // IS_SAVED
        Types.VARCHAR,   // DESCRIPTION
        Types.TIMESTAMP, // LAST_MODIFY_TIME
        Types.VARCHAR,   // SIGNATURE
        boolType,        // IS_PUBLIC
        Types.BIGINT     // STRATEGY_ID
      }
    );
  }

  // This function only updates the strategies table
  Strategy updateStrategy(User user, Strategy strategy, boolean overwrite) throws WdkModelException,
      WdkUserException {
    LOG.debug("Updating strategy internal#=" + strategy.getStrategyId() + ", overwrite=" + overwrite);

    // cannot update a saved strategy if overwrite flag is false
    if (!overwrite && strategy.getIsSaved())
      throw new WdkUserException("Cannot update a saved strategy. Please " +
          "create a copy and update it, or set overwrite flag to true.");

    long userId = user.getUserId();
    Strategy.StrategyBuilder build = new Strategy.StrategyBuilder(strategy);

    if (overwrite) {
      Optional<TwoTuple<Long,String>> opSaved = getOverwriteStrategy(userId,
          strategy.getName());

      if (opSaved.isPresent()) {
        TwoTuple<Long, String> saved = opSaved.get();
        // If there's already a saved strategy with this strategy's name,
        // we need to write the new saved strategy & mark the old
        // saved strategy as deleted
        build.setSaved(true);
        build.setSignature(saved.getSecond());
        build.setSavedName(strategy.getName());
        // jerric - only delete the strategy if it's a different one
        if (!strategy.getStrategyId().equals(saved.getFirst()))
          StepUtilities.deleteStrategy(user, saved.getFirst());
      }
    }

    build.setLastModifiedTime(new Date());

    if (!updateStrategy(strategy))
      throw new WdkUserException("The strategy #" + strategy.getStrategyId() +
          " of user " + user.getEmail() + " cannot be found.");

    return build.build();
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
    return createStrategy(user, strategyId, root, name, savedName, saved, description, hidden, isPublic);
  }

  Strategy createStrategy(User user, long strategyId, Step root, String newName,
      String savedName, boolean saved, String description, boolean hidden,
      boolean isPublic) throws WdkModelException {

    final String projectId = _wdkModel.getProjectId();

    LOG.debug("creating strategy, saved=" + saved);

    long userId = user.getUserId();

    String userIdColumn = Utilities.COLUMN_USER_ID;

    final int boolType = _userDbPlatform.getBooleanType();
    final String sql =
        "SELECT " + COLUMN_STRATEGY_ID + "\n" +
        "FROM " + _userSchema + TABLE_STRATEGY + "\n" +
        "WHERE " + userIdColumn      + " = ?\n" +
        "  AND " + COLUMN_PROJECT_ID + " = ?\n" +
        "  AND " + COLUMN_NAME       + " = ?\n" +
        "  AND " + COLUMN_IS_SAVED   + "= ?\n" +
        "  AND " + COLUMN_IS_DELETED + "= ?";

    final Wrapper<Optional<Long>> stratId = new Wrapper<>();

    // If newName is not null, check if strategy exists.  if so, just load it
    // and return.  don't create a new one.
    if (newName != null) {
      if (newName.length() > COLUMN_NAME_LIMIT) {
        newName = newName.substring(0, COLUMN_NAME_LIMIT - 1);
      }
      new SQLRunner(_userDbDs, sql)
        .executeQuery(
          new Object[]{
            userId,
            projectId,
            newName,
            _userDbPlatform.convertBoolean(saved),
            _userDbPlatform.convertBoolean(hidden)
          },
          new Integer[]{
            Types.BIGINT,
            Types.VARCHAR,
            Types.VARCHAR,
            boolType,
            boolType
          },
          rs -> {
            if (rs.next())
              stratId.set(Optional.of(rs.getLong(COLUMN_STRATEGY_ID)));
            else
              stratId.set(Optional.empty());
          }
        );

      if (stratId.get().isPresent()) {
        Optional<Strategy> strategy = new StrategyLoader(_wdkModel,
            ValidationLevel.SEMANTIC).getStrategyById(stratId.get().get());
        if (strategy.isPresent())
          return strategy.get();
      }

      throw  new WdkModelException("Newly created strategy could not be found.");
    } else {
      // if newName is null, generate default name from root step (by adding/incrementing numeric suffix)
      newName = addSuffixToStratNameIfNeeded(user, root.getCustomName(), saved);
    }

    String signature = getStrategySignature(projectId, user.getUserId(), strategyId);
    try(final Connection con = _userDbDs.getConnection()) {
      insertStrategy(con, new Strategy.StrategyBuilder(_wdkModel, userId, strategyId)
          .setRootStepId(root.getStepId()).setSaved(saved).setName(newName)
          .setSavedName(savedName).setProjectId(projectId)
          .setDeleted(false).setSignature(signature).setDescription(description)
          .setVersion(_wdkModel.getVersion()).setIsPublic(isPublic).build());
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }

    // check if we need to update the strategy id on the step;
    if (root.getStrategyId() == null || root.getStrategyId() != strategyId)
      updateStrategyId(strategyId, root);

    // FIXME: once this method is refactored to create an in-memory strategy
    //        before insertion, just use that one; do not load from the DB again
    Optional<Strategy> strategy = new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC).getStrategyById(strategyId);
    return strategy.orElseThrow(() -> new WdkModelException("Newly created strategy could not be found."));
  }

  /**
   * Overwrite the details of a collection of steps in the database.
   *
   * @param steps The collection of steps that will be updated in the database.
   *
   * @throws WdkModelException if a connection to the database cannot be opened.
   */
  public void updateSteps(Collection<Step> steps) throws WdkModelException {
    try(Connection con = _userDbDs.getConnection()) {
      updateSteps(con, steps);
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Overwrite the details of a collection of steps in the database.
   *
   * @param con   Open connection to the DB.  This can be used to run the step
   *              update queries in a controlled connection such as a
   *              transaction.
   * @param steps The collection of steps that will be updated in the database.
   */
  private void updateSteps(Connection con, Collection<Step> steps) {
    final String sql = "UPDATE " + _userSchema + TABLE_STEP + "\n" +
        "SET\n" +
        "  " + COLUMN_PREVIOUS_STEP_ID + " = ?,\n" +
        "  " + COLUMN_CHILD_STEP_ID    + " = ?,\n" +
        "  " + COLUMN_LAST_RUN_TIME    + " = ?,\n" +
        "  " + COLUMN_ESTIMATE_SIZE    + " = ?,\n" +
        "  " + COLUMN_ANSWER_FILTER    + " = ?,\n" +
        "  " + COLUMN_CUSTOM_NAME      + " = ?,\n" +
        "  " + COLUMN_IS_DELETED       + " = ?,\n" +
        "  " + COLUMN_IS_VALID         + " = ?,\n" +
        "  " + COLUMN_COLLAPSED_NAME   + " = ?,\n" +
        "  " + COLUMN_IS_COLLAPSIBLE   + " = ?,\n" +
        "  " + COLUMN_ASSIGNED_WEIGHT  + " = ?,\n" +
        "  " + COLUMN_PROJECT_ID       + " = ?,\n" +
        "  " + COLUMN_PROJECT_VERSION  + " = ?,\n" +
        "  " + COLUMN_QUESTION_NAME    + " = ?,\n" +
        "  " + COLUMN_STRATEGY_ID      + " = ?,\n" +
        "  " + COLUMN_DISPLAY_PARAMS   + " = ?\n"  +
        "WHERE\n" +
        "  " + COLUMN_STEP_ID + " = ?";

    final BasicArgumentBatch batch = new BasicArgumentBatch();
    final int boolType = _userDbPlatform.getBooleanType();
    batch.setParameterTypes(new Integer[]{
        Types.BIGINT,    // LEFT_CHILD_ID
        Types.BIGINT,    // RIGHT_CHILD_ID
        Types.TIMESTAMP, // LAST_RUN_TIME
        Types.BIGINT,    // ESTIMATE_SIZE
        Types.VARCHAR,   // ANSWER_FILTER
        Types.VARCHAR,   // CUSTOM_NAME
        boolType,        // IS_DELETED
        boolType,        // IS_VALID
        Types.VARCHAR,   // COLLAPSED_NAME
        boolType,        // IS_COLLAPSIBLE
        Types.BIGINT,    // ASSIGNED_WEIGHT
        Types.VARCHAR,   // PROJECT_ID
        Types.VARCHAR,   // PROJECT_VERSION
        Types.VARCHAR,   // QUESTION_NAME
        Types.BIGINT,    // STRATEGY_ID
        Types.CLOB,      // DISPLAY_PARAMS
        Types.BIGINT     // STEP_ID
    });

    for (final Step step : steps) {
      final AnswerSpec spec = step.getAnswerSpec();

      batch.add(new Object[]{
          step.getPreviousStepId() == 0 ? null : step.getPreviousStepId(),
          step.getChildStepId() == 0 ? null : step.getChildStepId(),
          step.getLastRunTime(),
          step.getEstimatedSize(),
          spec.getLegacyFilterName(),
          step.getCustomName(),
          _userDbPlatform.convertBoolean(step.isDeleted()),
          _userDbPlatform.convertBoolean(step.isValid()),
          step.getCollapsedName(),
          _userDbPlatform.convertBoolean(step.isCollapsible()),
          spec.getQueryInstanceSpec().getAssignedWeight(),
          step.getProjectId(),
          step.getProjectVersion(),
          spec.getQuestionName(),
          step.getStrategyId(),
          ParamFiltersClobFormat.formatParamFilters(spec),
          step.getStepId()
      });
    }

    new SQLRunner(con, sql).executeUpdateBatch(batch);
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
        "SELECT count(1)" +
        " FROM " + _userSchema + TABLE_STRATEGY +
        " WHERE " + Utilities.COLUMN_USER_ID + " = ?" +
        " AND " + COLUMN_IS_DELETED + " = " + _userDbPlatform.convertBoolean(false) +
        " AND " + COLUMN_PROJECT_ID + " = ?";
      SingleLongResultSetHandler result =
        new SQLRunner(_userDbDs, sql, "wdk-step-factory-strategy-count").executeQuery(
          new Object[]{ user.getUserId(), _wdkModel.getProjectId() },
          new Integer[]{ Types.BIGINT, Types.VARCHAR },
          new SingleLongResultSetHandler());
      if (result.containsValue()) {
        return result.getRetrievedValue().intValue();
      }
      throw new WdkModelException("Failed to execute count query (status = " + result.getStatus());
    }
    catch (Exception e) {
      return WdkModelException.unwrap(e, Integer.class);
    }
  }

  public NameCheckInfo checkNameExists(Strategy strategy, String name, boolean saved) {
    final int boolType = _userDbPlatform.getBooleanType();
    final Wrapper<NameCheckInfo> out = new Wrapper<>();
    final String sql = "SELECT\n" +
      "  " + COLUMN_STRATEGY_ID + ",\n" +
      "  " + COLUMN_IS_PUBLIC   + ",\n" +
      "  " + COLUMN_DESCRIPTION + "\n" +
      "FROM " + _userSchema + TABLE_STRATEGY +
      "WHERE\n" +
      "  "     + COLUMN_USER_ID     + " = ?\n" +
      "  AND " + COLUMN_PROJECT_ID  + " = ?\n" +
      "  AND " + COLUMN_NAME        + " = ?\n" +
      "  AND " + COLUMN_IS_SAVED    + " = ?\n" +
      "  AND " + COLUMN_IS_DELETED  + " = ?\n" +
      "  AND " + COLUMN_STRATEGY_ID + " <> ?";

    new SQLRunner(_userDbDs, sql).executeQuery(
      new Object[] {
        strategy.getUser().getUserId(),
        _wdkModel.getProjectId(),
        name,
        _userDbPlatform.convertBoolean(saved || strategy.getIsSaved()),
        _userDbPlatform.convertBoolean(false),
        strategy.getStrategyId()
      },
      new Integer[] {
        Types.BIGINT,
        Types.VARCHAR,
        Types.VARCHAR,
        boolType,
        boolType,
        Types.BIGINT
      },
      rs -> {
        if (rs.next()) {
          boolean isPublic = rs.getBoolean(2);
          String description = rs.getString(3);
          out.set(new NameCheckInfo(true, isPublic, description));
        } else {
          // otherwise, no strat by this name exists
          out.set(new NameCheckInfo(false, false, null));
        }
      }
    );

    return out.get();
  }

  private String addSuffixToStratNameIfNeeded(final User user,
      final String oldName, final boolean saved) {

    final String sql = "SELECT " + COLUMN_NAME + "\n" +
        "FROM " + _userSchema + TABLE_STRATEGY + "\n" +
        "WHERE\n" +
        "  "     + COLUMN_USER_ID    + " = ?\n" +
        "  AND " + COLUMN_PROJECT_ID + " = ?\n" +
        "  AND " + COLUMN_NAME       + " LIKE ?\n" +
        "  AND " + COLUMN_IS_SAVED   + "= ?\n" +
        "  AND " + COLUMN_IS_DELETED + "= ?\n" +
        "ORDER BY COLUMN_NAME DESC";

    final int boolType = _userDbPlatform.getBooleanType();

    final Wrapper<String> wrapper = new Wrapper<>();

    new SQLRunner(_userDbDs, sql).executeQuery(
        new Object[] {
            user.getUserId(),
            _wdkModel.getProjectId(),
            SqlUtils.escapeWildcards(oldName) + "%",
            _userDbPlatform.convertBoolean(saved),
            _userDbPlatform.convertBoolean(false)
        },
        new Integer[] {
            Types.BIGINT,
            Types.VARCHAR,
            Types.VARCHAR,
            boolType,
            boolType
        },
        rs -> {
          int index = 1;
          while (rs.next()) {
            int res = parseStrategyNameIndex(rs.getString(COLUMN_NAME), oldName)
                .orElse(0);
            if (res > index)
              index = res;
          }

          wrapper.set(index > 1
              ? String.format("%s (%d)", oldName, index)
              : oldName);
        }
    );

    return wrapper.get();
  }

  /**
   * Attempt to parse an int out of parenthesis at the end of a strategy name.
   *
   * @param test    String to check for appended counter
   * @param against Original strategy name
   *
   * @return If a counter value is present, an option wrapping that int.
   *         If no counter is present, an option of none.
   */
  static Optional<Integer> parseStrategyNameIndex(String test, String against) {
    final int len = against.trim().length();
    final String trimmed = test.trim();

    try {
      return Optional.of(
          Integer.parseInt(trimmed.substring(len + 1, trimmed.length() - 2)));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  void updateStrategyViewTime(int strategyId) {
    final String sql = "UPDATE " + _userSchema + TABLE_STRATEGY + "\n" +
        "SET\n" +
        "  " + COLUMN_LAST_VIEWED_TIME + " = ?,\n" +
        "  " + COLUMN_VERSION          + " = ?\n" +
        "WHERE\n" +
        "  " + COLUMN_STRATEGY_ID + " = ?";

    new SQLRunner(_userDbDs, sql, "wdk-step-factory-update-strategy-time")
      .executeUpdate(
        new Object[]{
          new Timestamp(new Date().getTime()),
          _wdkModel.getVersion(),
          strategyId,
        },
        new Integer[]{
          Types.TIMESTAMP,
          Types.VARCHAR,
          Types.BIGINT
        }
    );
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
   * Generates an SQL that will return the step and all the steps along the path
   * back to the root.
   *
   * @param stepId ID of the step to select.
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
   * Given a step, identify it and all downstream steps and set the estimate
   * size of each to -1.
   *
   * @param step step to start from
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
