package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;
import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.Oracle;
import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.BasicArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.functional.Either;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
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
import org.gusdb.wdk.model.user.StrategyLoader.MalformedStrategyList;

/**
 * Provides interface to the database to find, read, and write Step and Strategy
 * objects to DB
 *
 * @author rdoherty
 */
public class StepFactory {

  private static final Logger LOG = Logger.getLogger(StepFactory.class);

  // columns shared between steps and strategies tables
  static final String COLUMN_USER_ID = Utilities.COLUMN_USER_ID;
  static final String COLUMN_STRATEGY_ID = "strategy_id";
  static final String COLUMN_PROJECT_ID = "project_id";
  static final String COLUMN_CREATE_TIME = "create_time";
  static final String COLUMN_IS_DELETED = "is_deleted";

  // steps table and columns
  static final String TABLE_STEP = "steps";
  static final String COLUMN_STEP_ID = "step_id";
  static final String COLUMN_PREVIOUS_STEP_ID = "left_child_id";
  static final String COLUMN_CHILD_STEP_ID = "right_child_id";
  static final String COLUMN_LAST_RUN_TIME = "last_run_time";
  static final String COLUMN_ESTIMATE_SIZE = "estimate_size";
  static final String COLUMN_ANSWER_FILTER = "answer_filter";
  static final String COLUMN_CUSTOM_NAME = "custom_name";
  static final String COLUMN_IS_VALID = "is_valid";
  static final String COLUMN_COLLAPSED_NAME = "collapsed_name";
  static final String COLUMN_IS_COLLAPSIBLE = "is_collapsible";
  static final String COLUMN_ASSIGNED_WEIGHT = "assigned_weight";
  static final String COLUMN_PROJECT_VERSION = "project_version";
  static final String COLUMN_QUESTION_NAME = "question_name";
  static final String COLUMN_DISPLAY_PARAMS = "display_params";
  static final String COLUMN_DISPLAY_PREFS  = "display_prefs";

  static final String[] STEP_TABLE_COLUMNS = {
      COLUMN_USER_ID, COLUMN_STRATEGY_ID, COLUMN_PROJECT_ID, COLUMN_CREATE_TIME, COLUMN_IS_DELETED,
      COLUMN_STEP_ID, COLUMN_PREVIOUS_STEP_ID, COLUMN_CHILD_STEP_ID, COLUMN_LAST_RUN_TIME, COLUMN_ESTIMATE_SIZE,
      COLUMN_ANSWER_FILTER, COLUMN_CUSTOM_NAME, COLUMN_IS_VALID, COLUMN_COLLAPSED_NAME, COLUMN_IS_COLLAPSIBLE,
      COLUMN_ASSIGNED_WEIGHT, COLUMN_PROJECT_VERSION, COLUMN_QUESTION_NAME, COLUMN_DISPLAY_PARAMS
  };

  // strategies table and columns
  static final String TABLE_STRATEGY = "strategies";
  static final String COLUMN_ROOT_STEP_ID = "root_step_id";
  static final String COLUMN_VERSION = "version";
  static final String COLUMN_IS_SAVED = "is_saved";
  static final String COLUMN_LAST_VIEWED_TIME = "last_view_time";
  static final String COLUMN_LAST_MODIFIED_TIME = "last_modify_time";
  static final String COLUMN_DESCRIPTION = "description";
  static final String COLUMN_SIGNATURE = "signature";
  static final String COLUMN_NAME = "name";
  static final String COLUMN_SAVED_NAME = "saved_name";
  static final String COLUMN_IS_PUBLIC = "is_public";

  static final String[] STRATEGY_TABLE_COLUMNS = {
      COLUMN_USER_ID, COLUMN_STRATEGY_ID, COLUMN_PROJECT_ID, COLUMN_CREATE_TIME, COLUMN_IS_DELETED,
      COLUMN_ROOT_STEP_ID, COLUMN_VERSION, COLUMN_IS_SAVED, COLUMN_LAST_VIEWED_TIME, COLUMN_LAST_MODIFIED_TIME,
      COLUMN_DESCRIPTION, COLUMN_SIGNATURE, COLUMN_NAME, COLUMN_SAVED_NAME, COLUMN_IS_PUBLIC
  };

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

  public Step createStep(User user, SemanticallyValid<AnswerSpec> validSpec, String customName,
      boolean isCollapsible, String collapsedName) throws WdkModelException {
    AnswerSpec answerSpec = validSpec.getObject();
    // TODO: merge/refactor relevant logic from this createStep and the deprecated one
    //   so that only deprecated logic lives in the deprecated method
    return createStep(user,
        answerSpec.getQuestion(),
        answerSpec.getQueryInstanceSpec().toMap(),
        answerSpec.getLegacyFilter(),
        answerSpec.getFilterOptions(),
        answerSpec.getQueryInstanceSpec().getAssignedWeight(),
        false, // not deleted
        customName,
        isCollapsible,
        collapsedName,
        null); // new steps from service do not have a strategy
  }

  /**
   * Creates a step and adds to database
   */
  @Deprecated
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
      TwoTuple<Integer,Exception> runStatus = tryEstimateSize(step.getRunnable().getLeft());
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
      AnswerValue answerValue = AnswerValueFactory.makeAnswer(user, step.getAnswerSpec().getRunnable().getLeft(),
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
      StringBuilder sql = new StringBuilder("DELETE FROM " + stepTable)
          .append(" WHERE ").append(userIdColumn).append(" = ? ");
      if (!allProjects) {
        sql.append(" AND ").append(COLUMN_PROJECT_ID).append(" = ? ");
      }
      sql.append(" AND ").append(COLUMN_STEP_ID)
          .append(" NOT IN (SELECT ").append(COLUMN_ROOT_STEP_ID)
          .append(" FROM ").append(strategyTable);
      if (!allProjects) {
        sql.append(" WHERE ").append(COLUMN_PROJECT_ID).append(" = ? ")
            .append(" AND ").append(userIdColumn).append(" = ? ");
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
    String sql = "DELETE FROM " + _userSchema + TABLE_STRATEGY +
        " WHERE " + COLUMN_STRATEGY_ID + " = ?";

    final int result = new SQLRunner(_userDbDs, sql, "wdk-step-factory-delete-strategy")
        .executeUpdate(new Object[]{strategyId}, new Integer[]{Types.BIGINT});

    if (result == 0)
      throw new WdkModelException("The strategy #" + strategyId + " cannot be found.");
  }

  public void deleteStrategies(User user, boolean allProjects) {
    final Object[] values;
    final Integer[] types;
    final StringBuilder sql = new StringBuilder("DELETE FROM ")
        .append(_userSchema).append(TABLE_STRATEGY)
        .append(" WHERE ").append(Utilities.COLUMN_USER_ID).append(" = ?");

    if (!allProjects) {
      values = new Object[] { user.getUserId(), _wdkModel.getProjectId() };
      types  = new Integer[] { Types.BIGINT, Types.VARCHAR };
      sql.append(" AND ").append(COLUMN_PROJECT_ID).append(" = ?");
    } else {
      values = new Object[] { user.getUserId() };
      types  = new Integer[] { Types.BIGINT };
    }

    new SQLRunner(_userDbDs, sql.toString(), "wdk-step-factory-delete-all-strategies")
        .executeStatement(values, types);
  }

  public int getStepCount(User user) throws WdkModelException {
    try {
      String sql =
          "SELECT count(*) AS num" +
          " FROM " + _userSchema + TABLE_STEP +
          " WHERE " + COLUMN_USER_ID + " = ?" +
          "   AND " + COLUMN_PROJECT_ID + " = ? " +
          "   AND is_deleted = " + _userDbPlatform.convertBoolean(false);

      return new SQLRunner(_userDbDs, sql, "wdk-step-factory-step-count")
          .executeQuery(
              new Object[] { user.getUserId(), _wdkModel.getProjectId() },
              new Integer[] { Types.BIGINT, Types.VARCHAR },
              new SingleLongResultSetHandler()
          )
          .orElseThrow(() -> new WdkModelException(
              "Could not get step count for user " + user.getEmail()))
          .intValue();
    }
    catch (Exception e) {
      return WdkModelException.unwrap(e);
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
  @Deprecated
  public Optional<Step> getStepById(long stepId) throws WdkModelException {
    LOG.debug("Loading step#" + stepId + "....");
    return getStepById(stepId, ValidationLevel.SEMANTIC);
  }

  /**
   * @param stepId step ID for which to retrieve step
   * @param validationLevel level with which to validate step
   * @return step by step ID
   * @throws WdkModelException if step not found or problem occurs
   */
  public Optional<Step> getStepById(long stepId, ValidationLevel validationLevel) throws WdkModelException {
    LOG.debug("Loading step#" + stepId + "....");
    return new StrategyLoader(_wdkModel, validationLevel).getStepById(stepId);
  }

  public Step getStepByValidId(long stepId) throws WdkModelException {
    return getStepById(stepId).orElseThrow(() ->
        new WdkModelException("Could not find step with 'valid' ID: " + stepId));
  }

  @Deprecated
  private void updateStepTree(Step step) throws WdkModelException {
    Question question = step.getAnswerSpec().getQuestion();
    Map<String, String> displayParams = step.getAnswerSpec()
        .getQueryInstanceSpec()
        .toMap();

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
    StringBuilder sql = new StringBuilder("UPDATE ")
        .append(_userSchema).append(TABLE_STEP).append(" SET ")
        .append(COLUMN_CUSTOM_NAME).append(" = ? ");
    if (query.isCombined()) {
      sql.append(", " + COLUMN_PREVIOUS_STEP_ID + " = " + leftStepId);
      if (rightStepId != 0) {
        sql.append(", " + COLUMN_CHILD_STEP_ID + " = " + rightStepId);
      }
    }
    sql.append(" WHERE " + COLUMN_STEP_ID + " = " + step.getStepId());

    //step.setCustomName(customName);

    new SQLRunner(_userDbDs, sql.toString(), "wdk-step-factory-update-step-tree")
        .executeUpdate(new Object[]{ customName }, new Integer[]{ Types.VARCHAR });
  }

  @Deprecated
  int dropDependency(long stepId, String column) throws WdkModelException {
    String sql = "UPDATE " + _userSchema + TABLE_STEP +"\n" +
        "SET " + column + " = null\n" +
        "WHERE " + column + " = " + stepId;
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
   * This method updates the custom name, the time stamp of last running,
   * isDeleted, isCollapsible, and collapsed name
   *
   * @param user
   * @param step
   * @param setLastRunTime
   * @throws WdkModelException
   */
  @Deprecated
  void updateStep(User user, Step step, boolean setLastRunTime)
      throws WdkModelException {
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
        "  " + COLUMN_ASSIGNED_WEIGHT + " = ?,\n" +
        "  " + COLUMN_DISPLAY_PREFS   + " = ?\n"  +
        "WHERE\n" +
        "  " + COLUMN_STEP_ID + " = ?";

    final int boolType = _userDbPlatform.getBooleanType();
    final Either<Exception, String> displayPrefs = JsonUtil.toJsonString(step.getDisplayPrefs());

    final int result = new SQLRunner(_userDbDs, sql).executeUpdate(
        new Object[]{
            step.getBaseCustomName(),
            new Timestamp(lastRunTime.getTime()),
            _userDbPlatform.convertBoolean(step.isDeleted()),
            _userDbPlatform.convertBoolean(step.isCollapsible()),
            step.getCollapsedName(),
            step.getEstimatedSize(),
            step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight(),
            JsonUtil.toJsonString(step.getDisplayPrefs())
                .valueOrElseThrow(e -> new WdkModelException(displayPrefs.getLeft())),
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
            Types.CLOB,      // DISPLAY_PREFS
            Types.BIGINT     // STEP_ID
        }
    );

    if (result == 0)
      throw new WdkModelException("The Step #" + step.getStepId() +
          " of user " + user.getEmail() + " cannot be found.");

    // update the last run stamp
    //step.setLastRunTime(lastRunTime);

    // update dependencies
    if (step.isCombined())
      updateStepTree(step);

    LOG.debug("updateStep(): DONE");
  }

  void saveStepParamFilters(Step step) throws WdkModelException {
    LOG.debug("Saving params/filters of step #" + step.getStepId());
    final String sql = "UPDATE " + _userSchema + TABLE_STEP + "\n" +
        "SET\n" +
        "  " + COLUMN_QUESTION_NAME    + " = ?,\n" +
        "  " + COLUMN_ANSWER_FILTER    + " = ?,\n" +
        "  " + COLUMN_PREVIOUS_STEP_ID + " = ?,\n" +
        "  " + COLUMN_CHILD_STEP_ID    + " = ?,\n" +
        "  " + COLUMN_ASSIGNED_WEIGHT  + " = ?,\n" +
        "  " + COLUMN_DISPLAY_PARAMS   + " = ?\n" +
        "WHERE " + COLUMN_STEP_ID + " = ?";

    final AnswerSpec spec = step.getAnswerSpec();
    final long leftId = step.getPrimaryInputStepId();
    final long childId = step.getSecondaryInputStepId();

    final Integer[] types = {
        Types.VARCHAR,
        Types.VARCHAR,
        Types.BIGINT,
        Types.BIGINT,
        Types.BIGINT,
        Types.CLOB,
        Types.BIGINT
    };

    final Object[] values = {
        spec.getQuestionName(),
        spec.getLegacyFilterName(),
        leftId == 0 ? null : leftId,
        childId == 0 ? null : childId,
        spec.getQueryInstanceSpec().getAssignedWeight(),
        new StringReader(ParamFiltersClobFormat.formatParamFilters(
            step.getAnswerSpec()).toString()),
        step.getStepId()
    };

    final int result = new SQLRunner(_userDbDs, sql, "wdk-step-factory-save-step-params")
      .executeUpdate(values, types);

    if (result == 0)
      throw new WdkModelException(String.format("The Step #%d cannot be found.",
          step.getStepId()));
  }

  public Map<Long, Strategy> getStrategies(long userId) throws WdkModelException {
    return getStrategies(userId, ValidationLevel.SYNTACTIC, new MalformedStrategyList());
  }

  public Map<Long, Strategy> getStrategies(long userId, ValidationLevel validationLevel,
      MalformedStrategyList malformedStrategies) throws WdkModelException {
    return new StrategyLoader(_wdkModel, validationLevel)
        .getStrategies(userId, malformedStrategies);
  }

  public Map<Long, Strategy> getAllStrategies(ValidationLevel validationLevel,
      MalformedStrategyList malformedStrategies) throws WdkModelException {
    return new StrategyLoader(_wdkModel, validationLevel)
        .getAllStrategies(malformedStrategies);
  }
  /**
   * Find strategies matching the given criteria.
   *
   * @param userId id of the user who owns the strategy
   * @param saved  TRUE = return only saved strategies, FALSE = return only
   *               unsaved strategies.
   * @param recent TRUE = filter strategies to only those viewed within the past
   *               24 hours.
   *
   * @return A list of Strategy instances matching the search criteria.
   */
  public List<Strategy> getStrategies(long userId, boolean saved,
      boolean recent) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC)
        .getStrategies(userId, saved, recent);
  }

  public List<Strategy> getPublicStrategies() throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.RUNNABLE)
        .getPublicStrategies();
  }

  public void setStrategyPublicStatus(int stratId, boolean isPublic)
      throws WdkModelException {
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
        throw new WdkModelException(String.format(
          "Non-singular (%d) row updated during public strat status update.",
          rowsUpdated
        ));
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
        .getPublicStrategies(), Strategy::isValid).size();
  }

  public Optional<Strategy> getStrategyById(long strategyId,
      ValidationLevel validationLevel) throws WdkModelException {
    return new StrategyLoader(_wdkModel, validationLevel)
        .getStrategyById(strategyId);
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
  public Step copyStrategyToBranch(User user, Strategy strategy)
      throws WdkModelException {

    // copy the step tree
    Collection<StepBuilder> stepBuilders = copyStepTree(user,
        strategy.getRootStep()).toMap().values();

    // create stub strategy- will not be saved to DB; used only to create and
    // validate steps
    Strategy stratStub = Functions.mapException(
        () -> Strategy.builder(user.getWdkModel(), user.getUserId(), 0)
          .addSteps(stepBuilders)
          .build(new UserCache(user), ValidationLevel.NONE),
          // tree structure should already have been validated when creating the passed in strategy
          e -> new WdkModelException(e));

    // now that strategy is created (which will be returned), clean up steps for
    // saving to DB
    List<Step> orphanSteps = new ArrayList<>();
    for (StepBuilder step : stepBuilders) {
      step.removeStrategy();
      orphanSteps.add(step.build(new UserCache(user), ValidationLevel.NONE, null));
    }

    // write orphan steps to the DB to be used by caller
    try (Connection connection = _userDbDs.getConnection()){
      SqlUtils.performInTransaction(connection,
          conn -> insertSteps(conn, orphanSteps));
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to insert strategy or update steps.");
    }

    // return the strategy's root step- will be used to create a branch for
    // adding to another strat
    return stratStub.getRootStep();
  }

  /**
   *
   * @param user
   * @param oldStrategy
   * @param stepIdsMap An output map of old to new step IDs. Steps recursively
   *                   encountered in the copy are added by the copy
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
    Strategy newStrategy = Functions.mapException(() ->
      Strategy
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
        .build(new UserCache(user), ValidationLevel.RUNNABLE),
      // tree structure should already have been validated when creating the passed in strategy
        WdkModelException::new);

    // persist new strategy and all steps to the DB
    try (Connection connection = _userDbDs.getConnection()){
      SqlUtils.performInTransaction(connection,
        conn -> insertStrategy(conn, newStrategy),
        conn -> insertSteps(conn, newStrategy.getAllSteps())
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
          new WdkModelException(
              "Unable to execute all operations subsequent to step copy."));
    }

    // populate stepIdsMap with mapping from oldId -> newId
    stepIdsMap.putAll(getMapFromKeys(newStepMap.keySet(),
        oldId -> newStepMap.get(oldId).getStepId()));

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
      "  " + COLUMN_DISPLAY_PREFS   + ",\n" +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
      Types.CLOB       // DISPLAY_PREFS
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
      new StringReader(ParamFiltersClobFormat.formatParamFilters(spec).toString()),
      new StringReader(JsonUtil.toJsonString(step.getDisplayPrefs()).getValue())
    };
  }

  private void insertStep(Step step) {
    new SQLRunner(_userDbDs, buildInsertStepSQL())
      .executeUpdate(stepToInsertParams(step), getInsertStepParamTypes());
  }

  /**
   * Insert a collection of steps into the database.
   *
   * @param con   Open connection to the DB.  This can be used to run the step
   *              insert queries in a controlled connection such as a
   *              transaction.
   * @param steps The collection of steps that will be inserted into the
   *              database.
   */
  private void insertSteps(Connection con, Collection<Step> steps) {
    final BasicArgumentBatch batch = new BasicArgumentBatch();

    batch.setParameterTypes(getInsertStepParamTypes());
    steps.stream()
      .map(this::stepToInsertParams)
      .forEach(batch::add);

    new SQLRunner(con, buildInsertStepSQL()).executeStatementBatch(batch);
  }

  public void insertStrategy(Strategy newStrategy) throws WdkModelException {
    try(Connection connection = _userDbDs.getConnection()) {
      SqlUtils.performInTransaction(connection, conn -> {
        insertStrategy(conn, newStrategy);
        updateSteps(conn, newStrategy.getAllSteps());
      });
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
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
          _userDbPlatform.convertBoolean(newStrategy.isSaved()),
          newStrategy.getName(),
          newStrategy.getSavedName(),
          newStrategy.getProjectId(),
          _userDbPlatform.convertBoolean(false),
          newStrategy.getSignature(),
          newStrategy.getDescription(),
          newStrategy.getVersion(),
          _userDbPlatform.convertBoolean(newStrategy.isPublic())
        });
  }

  private MapBuilder<Long, StepBuilder> copyStepTree(User newUser, Step oldStep) throws WdkModelException {

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
      String replacementValue = param instanceof AnswerParam
          ? cloneAnswerParam(oldSpec, oldStableValue, newUser, newStepMap)
          : param instanceof DatasetParam
              ? cloneDatasetParam(oldUser, oldStableValue, newUser)
              : oldStableValue; // otherwise use original value
      newSpec.setParamValue(param.getName(), replacementValue);
    }
    return newStepMap;
  }

  private String cloneAnswerParam(AnswerSpec oldSpec, String oldStableValue,
      User newUser, MapBuilder<Long, StepBuilder> stepIdsMap)
      throws WdkModelException {
    Step oldStepValue = oldSpec.getStepContainer()
        .findFirstStep(withId(Long.parseLong(oldStableValue)))
        .orElseThrow(() -> new WdkModelException("Step container cannot find expected step."));
    stepIdsMap.putAll(copyStepTree(newUser, oldStepValue).toMap());
    return Long.toString(stepIdsMap.get(oldStepValue.getStepId()).getStepId());
  }

  private String cloneDatasetParam(User oldUser, String oldStableValue,
      User newUser) throws WdkModelException {
    long oldDatasetId = Long.parseLong(oldStableValue);
    DatasetFactory datasetFactory = _wdkModel.getDatasetFactory();
    Dataset oldDataset = datasetFactory.getDataset(oldUser, oldDatasetId);
    Dataset newDataset = datasetFactory.cloneDataset(oldDataset, newUser);
    return Long.toString(newDataset.getDatasetId());
  }

  public Optional<Strategy> getStrategyBySignature(String strategySignature)
      throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC)
        .getStrategyBySignature(strategySignature);
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

    return new SQLRunner(_userDbDs, sql)
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
        rs -> rs.next()
            ? Optional.of(new TwoTuple<>(
                rs.getLong(COLUMN_STRATEGY_ID),
                rs.getString(COLUMN_SIGNATURE)
              ))
            : Optional.empty()
      );
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

  public void updateStrategies(Collection<Strategy> toUpdate) throws WdkModelException {
    try(Connection con = _userDbDs.getConnection()) {
      con.setAutoCommit(false);
      for(Strategy strat : toUpdate)
        updateStrategy(con, strat);
      con.commit();
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
  public boolean updateStrategy(Connection con, Strategy strat)
      throws WdkModelException {
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

    final boolean stratUpdated = 0 < new SQLRunner(con, sql).executeUpdate(
      new Object[]{
        strat.getName(),
        strat.getRootStep().getStepId(),
        strat.getSavedName(),
        _userDbPlatform.convertBoolean(strat.isSaved()),
        strat.getDescription(),
        new Timestamp(strat.getLastModifiedTime().getTime()),
        strat.getSignature(),
        _userDbPlatform.convertBoolean(strat.isPublic()),
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

    updateSteps(strat.getAllSteps());

    return stratUpdated;
  }

  // This function only updates the strategies table
  Strategy updateStrategy(Strategy strategy, boolean overwrite)
      throws WdkModelException,
      WdkUserException {
    LOG.debug("Updating strategy internal#=" + strategy.getStrategyId() +
        ", overwrite=" + overwrite);

    // cannot update a saved strategy if overwrite flag is false
    if (!overwrite && strategy.isSaved())
      throw new WdkUserException("Cannot update a saved strategy. Please " +
          "create a copy and update it, or set overwrite flag to true.");

    User user = strategy.getUser();
    Strategy.StrategyBuilder builder = Strategy.builder(strategy);

    if (overwrite) {
      Optional<TwoTuple<Long,String>> opSaved = getOverwriteStrategy(
          user.getUserId(), strategy.getName());

      if (opSaved.isPresent()) {
        TwoTuple<Long, String> saved = opSaved.get();
        // If there's already a saved strategy with this strategy's name,
        // we need to write the new saved strategy & mark the old
        // saved strategy as deleted
        builder.setSaved(true);
        builder.setSignature(saved.getSecond());
        builder.setSavedName(strategy.getName());
        // only delete the strategy if it's a different one
        if (!strategy.getStrategyId().equals(saved.getFirst()))
          StepUtilities.deleteStrategy(user, saved.getFirst());
      }
    }

    builder.setLastModifiedTime(new Date());

    if (!updateStrategy(strategy))
      throw new WdkUserException("The strategy #" + strategy.getStrategyId() +
          " of user " + user.getEmail() + " cannot be found.");

    return Functions.mapException(() ->
      builder.build(new UserCache(user), strategy.getValidationBundle().getLevel()),
      // tree structure should already have been validated when creating the passed in strategy
        WdkModelException::new);

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
  @Deprecated
  public Strategy createStrategy(User user, Step root, String name,
      String savedName, boolean saved, String description, boolean hidden,
      boolean isPublic) throws WdkModelException, InvalidStrategyStructureException {
    long strategyId = (root.getStrategyId() == null)
        ? getNewStrategyId()
        : root.getStrategyId();
    return createStrategy(user, strategyId, root, name, savedName, saved,
        description, hidden, isPublic);
  }

  @Deprecated
  Strategy createStrategy(User user, long strategyId, Step root, String newName,
      String savedName, boolean saved, String description, boolean hidden,
      boolean isPublic) throws WdkModelException, InvalidStrategyStructureException {

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

    // If newName is not null, check if strategy exists.  if so, just load it
    // and return.  don't create a new one.
    if (newName != null) {
      if (newName.length() > COLUMN_NAME_LIMIT) {
        newName = newName.substring(0, COLUMN_NAME_LIMIT - 1);
      }
      Optional<Long> stratId = new SQLRunner(_userDbDs, sql)
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
          rs -> rs.next() ?
            Optional.of(rs.getLong(COLUMN_STRATEGY_ID)) :
            Optional.empty()
        );

      if (stratId.isPresent()) {
        Optional<Strategy> strategy = new StrategyLoader(_wdkModel,
            ValidationLevel.SEMANTIC).getStrategyById(stratId.get());
        if (strategy.isPresent())
          return strategy.get();
      }

      throw  new WdkModelException("Newly created strategy could not be found.");
    } else {
      // if newName is null, generate default name from root step (by
      // adding/incrementing numeric suffix)
      newName = addSuffixToStratNameIfNeeded(user, root.getCustomName(), saved);
    }

    String signature = getStrategySignature(projectId, user.getUserId(),
        strategyId);

    Strategy outStrat = new Strategy.StrategyBuilder(_wdkModel, userId, strategyId)
      .setRootStepId(root.getStepId())
      .setSaved(saved)
      .setName(newName)
      .setSavedName(savedName)
      .setProjectId(projectId)
      .setDeleted(false)
      .setSignature(signature)
      .setDescription(description)
      .setVersion(_wdkModel.getVersion())
      .setIsPublic(isPublic)
      .addStep(Step.builder(root))
      .build(new UserCache(root.getUser()), root.getValidationBundle().getLevel());

    try(final Connection con = _userDbDs.getConnection()) {
      insertStrategy(con, outStrat);
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
   * Overwrite the details of a step in the database.
   *
   * @param step the step that will will be updated
   *
   * @throws WdkModelException if a connection to the database cannot be opened.
   */
  public void updateStep(Step step) throws WdkModelException {
    updateSteps(ListBuilder.asList(step));
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
   * @throws WdkModelException
   */
  private void updateSteps(Connection con, Collection<Step> steps) throws WdkModelException {
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
        "  " + COLUMN_DISPLAY_PARAMS   + " = ?,\n" +
        "  " + COLUMN_DISPLAY_PREFS    + " = ?\n"  +
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
        Types.CLOB,      // DISPLAY_PREFS
        Types.BIGINT     // STEP_ID
    });

    for (final Step step : steps) {
      final AnswerSpec spec = step.getAnswerSpec();

      batch.add(new Object[]{
          step.getPrimaryInputStepId() == 0 ? null : step.getPrimaryInputStepId(),
          step.getSecondaryInputStepId() == 0 ? null : step.getSecondaryInputStepId(),
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
          JsonUtil.toJsonString(step.getDisplayPrefs()).getValue(),
          step.getStepId()
      });
    }

    try {
      new SQLRunner(con, sql).executeUpdateBatch(batch);
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }

    // get list of dirty steps; all their results are now invalid
    List<Long> dirtyStepIds = steps.stream()
        .filter(step -> step.isResultSizeDirty())
        .map(step -> step.getStepId())
        .collect(Collectors.toList());

    // alert listeners that the step results have changed for these steps
    Events.triggerAndWait(new StepResultsModifiedEvent(dirtyStepIds),
        new WdkModelException("Unable to process all StepResultsModified events for step IDs: " +
            FormatUtil.arrayToString(dirtyStepIds.toArray())));

  }

  private void updateStrategyId(long strategyId, Step rootStep) throws WdkModelException {
    String stepIdSql = selectStepAndChildren(rootStep.getStepId());
    String sql = "UPDATE " + _userSchema + TABLE_STEP + "\n" +
        "SET " + COLUMN_STRATEGY_ID + " = " + strategyId + "\n" +
        "WHERE step_id IN (" + stepIdSql + ")";
    new SQLRunner(_userDbDs, sql, "wdk-update-strategy-on-steps").executeUpdate();
  }

  public int getStrategyCount(long userId) throws WdkModelException {
    try {
      String sql =
        "SELECT count(1)" +
        " FROM " + _userSchema + TABLE_STRATEGY +
        " WHERE " + Utilities.COLUMN_USER_ID + " = ?" +
        " AND " + COLUMN_IS_DELETED + " = " + _userDbPlatform.convertBoolean(false) +
        " AND " + COLUMN_PROJECT_ID + " = ?";
      return new SQLRunner(_userDbDs, sql, "wdk-step-factory-strategy-count")
        .executeQuery(
          new Object[]{ userId, _wdkModel.getProjectId() },
          new Integer[]{ Types.BIGINT, Types.VARCHAR },
          new SingleLongResultSetHandler())
        .orElseThrow(() -> new WdkModelException("Failed to execute strategy count for user: " + userId))
        .intValue();
    }
    catch (Exception e) {
      return WdkModelException.unwrap(e);
    }
  }

  public NameCheckInfo checkNameExists(Strategy strategy, String name, boolean saved) {
    final int boolType = _userDbPlatform.getBooleanType();
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

    return new SQLRunner(_userDbDs, sql).executeQuery(
      new Object[] {
        strategy.getUser().getUserId(),
        _wdkModel.getProjectId(),
        name,
        _userDbPlatform.convertBoolean(saved || strategy.isSaved()),
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
          return new NameCheckInfo(true, isPublic, description);
        }
        // otherwise, no strat by this name exists
        return new NameCheckInfo(false, false, null);
      }
    );
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

    return new SQLRunner(_userDbDs, sql).executeQuery(
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

          return index > 1
              ? String.format("%s (%d)", oldName, index)
              : oldName;
        }
    );
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
    final String clean = test.trim();

    try {
      return Optional.of(Integer.parseInt(clean.substring(len + 1,
          clean.length() - 2)));
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

  public static String getStrategySignature(String projectId, long userId,
      long strategyId) {
    String content = projectId + "_" + userId + "_" + strategyId +
        "_6276406938881110742";

    return EncryptionUtil.encrypt(content, true);
  }

  /**
   * Generates an SQL that will return the step and all the steps along the path
   * back to the root.
   *
   * @param stepId ID of the step to select.
   *
   * @return an SQL that returns a step_id column.
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
    return new SQLRunner(_userDbDs, selectStepAndParents(stepId), "select-step-and-parent-ids")
      .executeQuery(rs -> {
        final List<Long> ids = new ArrayList<>();
        while (rs.next()) {
          ids.add(rs.getLong(1));
        }
        return ids;
      });
  }

  /**
   * Given a step, identify it and all downstream steps and set the estimate
   * size of each to -1.
   *
   * @param step step to start from
   */
  public int resetEstimateSizeForThisAndDownstreamSteps(Step step)
      throws WdkModelException {
    String sql = "UPDATE " + _userSchema + TABLE_STEP + "\n" +
        "SET " + COLUMN_ESTIMATE_SIZE + " = " + UNKNOWN_SIZE + "\n" +
        "WHERE step_id IN (" + selectStepAndParents(step.getStepId()) + ")";

    try {
      return SqlUtils.executeUpdate(_userDbDs, sql, "wdk-update-estimate-size-on-steps");
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  public void setStrategyIdForThisAndUpstreamSteps(Step step, Long strategyId)
      throws WdkModelException {
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

  /**
   * Updates the step's state in the DB (and any dependents within its strategy).
   *
   * @param previous the previous version of the step (pre-modification)
   * @param stepToSave the modified version of the step (in builder form)
   * @param isDirtyChange whether the changes made change the step's result
   * @param level validation level at which step and/or strategy should be built
   * @throws WdkModelException if error occurs during validation
   * @throws InvalidStrategyStructureException if answer value changes cause the
   *         structure of the strategy to become invalid
   * TODO: if validation fails, what to do?  Maybe throw exception?  Do we even
   *       want to validate here?  In what cases should we validate? etc.
   */
  public void updateStepAndDependents(Step previous, StepBuilder stepToSave, boolean isDirtyChange, ValidationLevel level)
      throws WdkModelException, InvalidStrategyStructureException {
    UserCache userCache = new UserCache(previous.getUser());
    stepToSave.setResultSizeDirty(isDirtyChange);
    if (previous.hasStrategy()) {
      Strategy newStrategy = Strategy.builder(previous.getStrategy())
          .addStep(stepToSave)
          .build(userCache, level);
      updateStrategy(newStrategy);
    }
    else {
      updateStep(stepToSave.build(userCache, level, null));
    }
  }

  public void updateStrategyAndOtherSteps(Strategy newStrat, List<Step> orphanedSteps) throws WdkModelException {
    try(Connection connection = _userDbDs.getConnection()) {
      SqlUtils.performInTransaction(connection, conn -> {
        updateStrategy(conn, newStrat);
        updateSteps(conn, orphanedSteps);
      });
    }
    catch (Exception e) {
      throw new WdkModelException("Could not update strategy and steps", e);
    }
  }
}
