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
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.BasicArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.functional.Either;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.ParamsAndFiltersDbColumnFormat;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.Strategy.StrategyBuilder;
import org.gusdb.wdk.model.user.StrategyLoader.UnbuildableStrategyList;
import org.json.JSONObject;

/**
 * Provides interface to the database to find, read, and write Step and Strategy
 * objects to DB
 *
 * @author rdoherty
 */
public class StepFactory {

  private static final Logger LOG = Logger.getLogger(StepFactory.class);

  private static final FillStrategy DEFAULT_DB_FILL_STRATEGY = FillStrategy.FILL_PARAM_IF_MISSING;

  // columns shared between steps and strategies tables
  static final String COLUMN_USER_ID = Utilities.COLUMN_USER_ID;
  static final String COLUMN_STRATEGY_ID = "strategy_id";
  static final String COLUMN_PROJECT_ID = "project_id";
  static final String COLUMN_CREATE_TIME = "create_time";
  static final String COLUMN_IS_DELETED = "is_deleted";

  // steps table and columns
  static final String TABLE_STEP = "steps",
    COLUMN_STEP_ID          = "step_id",
    COLUMN_PREVIOUS_STEP_ID = "left_child_id",
    COLUMN_CHILD_STEP_ID    = "right_child_id",
    COLUMN_LAST_RUN_TIME    = "last_run_time",
    COLUMN_ESTIMATE_SIZE    = "estimate_size",
    COLUMN_ANSWER_FILTER    = "answer_filter",
    COLUMN_CUSTOM_NAME      = "custom_name",
    COLUMN_IS_VALID         = "is_valid",
    COLUMN_ASSIGNED_WEIGHT  = "assigned_weight",
    COLUMN_PROJECT_VERSION  = "project_version",
    COLUMN_QUESTION_NAME    = "question_name",
    COLUMN_DISPLAY_PARAMS   = "display_params",
    COLUMN_DISPLAY_PREFS    = "display_prefs",
    COLUMN_IS_EXPANDED      = "branch_is_expanded",
    COLUMN_EXPANDED_NAME    = "branch_name";


  static final String[] STEP_TABLE_COLUMNS = {
    COLUMN_USER_ID, COLUMN_STRATEGY_ID, COLUMN_PROJECT_ID, COLUMN_CREATE_TIME,
    COLUMN_IS_DELETED, COLUMN_STEP_ID, COLUMN_PREVIOUS_STEP_ID,
    COLUMN_CHILD_STEP_ID, COLUMN_LAST_RUN_TIME, COLUMN_ESTIMATE_SIZE,
    COLUMN_ANSWER_FILTER, COLUMN_CUSTOM_NAME, COLUMN_IS_VALID,
    COLUMN_ASSIGNED_WEIGHT, COLUMN_PROJECT_VERSION, COLUMN_QUESTION_NAME,
    COLUMN_DISPLAY_PARAMS, COLUMN_DISPLAY_PREFS, COLUMN_IS_EXPANDED,
    COLUMN_EXPANDED_NAME
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
  private final DataSource _userDbDs;
  private final DBPlatform _userDbPlatform;
  private final String _userSchema;

  public StepFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    DatabaseInstance userDb = _wdkModel.getUserDb();
    _userDbDs = userDb.getDataSource();
    _userDbPlatform = userDb.getPlatform();
    _userSchema = _wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  @SuppressWarnings("UseOfObsoleteDateTimeApi")
  public SemanticallyValid<Step> createStep(User user, SemanticallyValid<AnswerSpec> validSpec, String customName,
      boolean isExpanded, String expandedName, JSONObject displayPrefs) throws WdkModelException {

    // define creation time
    Date createTime = new Date();
    // TODO lastRunTime should be made null once the last_run_time field in DB is nullable.
    Date lastRunTime = new Date(createTime.getTime());

    // create the Step
    Step step = Step
        .builder(_wdkModel, user.getUserId(), getNewStepId())
        .setCreatedTime(createTime)
        .setLastRunTime(lastRunTime)
        .setDeleted(false)
        .setProjectId(_wdkModel.getProjectId())
        .setProjectVersion(_wdkModel.getVersion())
        .setCustomName(customName)
        .setExpanded(isExpanded)
        .setExpandedName(expandedName)
        .setDisplayPrefs(displayPrefs)
        .setStrategyId(Optional.empty()) // new steps do not belong to a strategy
        .setAnswerSpec(AnswerSpec.builder(validSpec.get()))
        .build(new UserCache(user), ValidationLevel.SEMANTIC, Optional.empty());

    // insert step into the database
    insertStep(step);

    return step.getSemanticallyValid().getLeft();
  }

  // RRD 4/2019: Though not called anywhere, leaving this method here because I
  //   think once we start writing the client, we may have a use case to call it.
  private static Either<Integer, Exception> tryEstimateSize(RunnableObj<Step> runnableStep) {
    try {
      Step step = runnableStep.get();
      User user = step.getUser();
      String questionFullName = step.getAnswerSpec().getQuestion().getFullName();
      AnswerValue answerValue = AnswerValueFactory.makeAnswer(user, step.getAnswerSpec().getRunnable().getLeft());

      QueryInstance<?> qi = answerValue.getIdsQueryInstance();
      LOG.debug("id query name  :" + (qi == null ? "<no_query_specified>" : qi.getQuery().getFullName()));
      LOG.debug("answer checksum:" + answerValue.getChecksum());
      LOG.debug("question name:  " + questionFullName);
      int estimateSize = answerValue.getResultSizeFactory().getDisplayResultSize();
      return new Either<>(estimateSize, null);
    }
    catch (Exception e) {
      LOG.error("Creating step failed", e);
      return new Either<>(null, e);
    }
  }

  public long getNewStepId() throws WdkModelException {
    try {
      return _userDbPlatform.getNextId(_userDbDs, _userSchema, TABLE_STEP);
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
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

  public Map<Long, Step> getStepsByUserId(long userId, ValidationLevel level) throws WdkModelException {
    return new StrategyLoader(_wdkModel, level, DEFAULT_DB_FILL_STRATEGY).getSteps(userId);
  }

  /**
   * @param stepId
   *   step ID for which to retrieve step
   * @param validationLevel
   *   level with which to validate step
   *
   * @return step by step ID
   *
   * @throws WdkModelException
   *   if step not found or problem occurs
   */
  public Optional<Step> getStepById(long stepId, ValidationLevel validationLevel) throws WdkModelException {
    LOG.debug("Loading step#" + stepId + "....");
    return new StrategyLoader(_wdkModel, validationLevel, DEFAULT_DB_FILL_STRATEGY).getStepById(stepId);
  }

  public Step getStepByValidId(long stepId, ValidationLevel validationLevel) throws WdkModelException {
    return getStepById(stepId, validationLevel).orElseThrow(() ->
        new WdkModelException("Could not find step with 'valid' ID: " + stepId));
  }

  public Map<Long, Strategy> getStrategies(long userId, ValidationLevel validationLevel, FillStrategy fillStrategy)
      throws WdkModelException {
    return getStrategies(userId, validationLevel, fillStrategy,
        new UnbuildableStrategyList<InvalidStrategyStructureException>(),
        new UnbuildableStrategyList<WdkModelException>());
  }

  public Map<Long, Strategy> getStrategies(long userId, ValidationLevel validationLevel, FillStrategy fillStrategy,
      UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrategies,
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors) throws WdkModelException {
    return new StrategyLoader(_wdkModel, validationLevel, fillStrategy)
        .getStrategies(userId, malformedStrategies, stratsWithBuildErrors);
  }

  public Map<Long, Strategy> getAllStrategies(ValidationLevel validationLevel,
      UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrategies,
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors) throws WdkModelException {
    return new StrategyLoader(_wdkModel, validationLevel, DEFAULT_DB_FILL_STRATEGY)
        .getAllStrategies(malformedStrategies, stratsWithBuildErrors);
  }

  /**
   * Find strategies matching the given criteria.
   *
   * @param userId
   *   id of the user who owns the strategy
   * @param saved
   *   TRUE = return only saved strategies, FALSE = return only unsaved
   *   strategies.
   * @param recent
   *   TRUE = filter strategies to only those viewed within the past 24 hours.
   *
   * @return A list of Strategy instances matching the search criteria.
   */
  public List<Strategy> getStrategies(long userId, boolean saved,
      boolean recent) throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SYNTACTIC, DEFAULT_DB_FILL_STRATEGY)
        .getStrategies(userId, saved, recent);
  }

  public List<Strategy> getPublicStrategies() throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.RUNNABLE, FillStrategy.FILL_PARAM_IF_MISSING)
        .getPublicStrategies();
  }

  /**
   * @return the number of runnable public strategies (to populate count in
   *   public strat tab)
   *
   * @throws WdkModelException
   *   if unable to load and validate all public strats
   */
  public int getPublicStrategyCount() throws WdkModelException {
    return filter(new StrategyLoader(_wdkModel, ValidationLevel.RUNNABLE, FillStrategy.FILL_PARAM_IF_MISSING)
        .getPublicStrategies(), Strategy::isValid).size();
  }

  public TwoTuple<
    UnbuildableStrategyList<InvalidStrategyStructureException>,
    UnbuildableStrategyList<WdkModelException>
  > getPublicStrategyErrors() throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.RUNNABLE, FillStrategy.FILL_PARAM_IF_MISSING)
        .getPublicStrategyErrors();
  }

  public Optional<Strategy> getStrategyById(long strategyId,
      ValidationLevel validationLevel, FillStrategy fillStrategy) throws WdkModelException {
    return new StrategyLoader(_wdkModel, validationLevel, fillStrategy)
        .getStrategyById(strategyId);
  }

  /**
   * This method is to be used specifically for cloning a strategy for use as a
   * step in a separate, existing strategy.  Clones of the steps of the passed
   * strategy are created and written to the DB; however, the one returned by
   * this step does NOT have a saved strategy; the DB will reflect that the new
   * steps are orphans until attached to a new strategy.
   *
   * @param user
   *   owner of the new steps
   * @param strategy
   *   strategy to clone
   *
   * @return a step representing a branch created from an existing strategy
   *
   * @throws WdkModelException
   *   if something goes wrong
   */
  public Step copyStrategyToBranch(User user, Strategy strategy)
      throws WdkModelException {

    // copy the step tree
    Map<Long,StepBuilder> stepBuilders = copyStepTree(user, strategy.getRootStep()).toMap();

    // create stub strategy- will not be saved to DB; used only to create and
    // validate steps.  answer param values are valid (make a valid tree) in this strategy
    Strategy stratStub = Functions.mapException(
        () -> Strategy.builder(user.getWdkModel(), user.getUserId(), 0)
          .setRootStepId(stepBuilders.get(strategy.getRootStepId()).getStepId())
          .addSteps(stepBuilders.values())
          .build(new UserCache(user), ValidationLevel.NONE),
          // tree structure should already have been validated when creating the passed in strategy
          e -> new WdkModelException(e));

    // now that strategy is created (which will be returned), clean up steps for
    // saving to DB
    List<Step> orphanSteps = new ArrayList<>();
    for (StepBuilder step : stepBuilders.values()) {
      step.removeStrategy();
      orphanSteps.add(step.build(new UserCache(user), ValidationLevel.NONE, Optional.empty()));
    }

    // write orphan steps to the DB to be used by caller
    try (Connection connection = _userDbDs.getConnection()){
      SqlUtils.performInTransaction(connection,
          conn -> insertSteps(conn, orphanSteps));
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to insert strategy or update steps.");
    }

    // now that steps are in DB, trigger step copied events to copy any other
    //   step-related info (must be afterward because of some FK constraints)
    triggerCopyEvents(stepBuilders.entrySet().stream()
      .map(entry -> new TwoTuple<>(
        strategy.findFirstStep(withId(entry.getKey())).get(),
        stratStub.findFirstStep(withId(entry.getValue().getStepId())).get()))
      .collect(Collectors.toList()));
          
    
    // return the strategy's root step- will be used to create a branch for
    // adding to another strat
    return stratStub.getRootStep();
  }

  /**
   * @param stepIdsMap
   *   An output map of old to new step IDs. Steps recursively encountered in
   *   the copy are added by the copy
   */
  public Strategy copyStrategy(User user, Strategy oldStrategy, Map<Long, Long> stepIdsMap)
      throws WdkModelException {

    WdkModel wdkModel = user.getWdkModel();
    long strategyId = getNewStrategyId();
    String projectId = oldStrategy.getProjectId();
    String name = addSuffixToStratNameIfNeeded(user, oldStrategy.getName(), false);
    String signature = Strategy.createSignature(projectId, user.getUserId(), strategyId);
    Map<Long, StepBuilder> newStepMap = copyStepTree(user, oldStrategy.getRootStep()).toMap();

    // construct the new strategy
    Strategy newStrategy = Functions.mapException(() ->
      Strategy
        .builder(wdkModel, user.getUserId(), strategyId)
        .setRootStepId(newStepMap.get(oldStrategy.getRootStepId()).getStepId())
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
        conn -> insertSteps(conn, newStrategy.getAllSteps()),
        conn -> insertStrategy(conn, newStrategy)
      );
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to insert strategy or update steps.");
    }

    // trigger copy events on all steps
    triggerCopyEvents(newStepMap.entrySet().stream()
      .map(entry -> new TwoTuple<>(
        oldStrategy.findFirstStep(withId(entry.getKey())).get(),
        newStrategy.findFirstStep(withId(entry.getValue().getStepId())).get()))
      .collect(Collectors.toList()));

    // populate stepIdsMap with mapping from oldId -> newId
    stepIdsMap.putAll(getMapFromKeys(newStepMap.keySet(),
        oldId -> newStepMap.get(oldId).getStepId()));

    return newStrategy;
  }

  private void triggerCopyEvents(List<Entry<Step, Step>> stepMapping) throws WdkModelException {
    for (Entry<Step, Step> mapping : stepMapping) {
      LOG.info("Triggering step copied event with old step ID " + mapping.getKey().getStepId() + ", new step ID " + mapping.getValue().getStepId());
      Events.triggerAndWait(new StepCopiedEvent(mapping.getKey(), mapping.getValue()),
          new WdkModelException("Unable to execute all operations subsequent to step copy."));
    }
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
      "  " + COLUMN_IS_DELETED      + ",\n" +
      "  " + COLUMN_STRATEGY_ID     + ",\n" +
      "  " + COLUMN_DISPLAY_PARAMS  + ",\n" +
      "  " + COLUMN_DISPLAY_PREFS   + ",\n" +
      "  " + COLUMN_IS_EXPANDED     + ",\n" +
      "  " + COLUMN_EXPANDED_NAME   + ")\n" +
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
      boolType,        // IS_DELETED
      Types.BIGINT,    // STRATEGY_ID
      Types.CLOB,      // DISPLAY_PARAMS
      Types.CLOB,      // DISPLAY_PREFS
      boolType,        // IS_EXPANDED
      Types.VARCHAR    // EXPANDED_NAME
    };
  }

  /**
   * Constructs an array of values from the given step matching the columns and
   * types returned by {@link #buildInsertStepSQL()} and {@link
   * #getInsertStepParamTypes()} for use with {@link SQLRunner}.
   *
   * @param step
   *   The step for which a values array should be constructed.
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
      spec.getLegacyFilterName().orElse(null),
      spec.getQueryInstanceSpec().getAssignedWeight(),
      _wdkModel.getProjectId(),
      _wdkModel.getVersion(),
      spec.getQuestionName(),
      step.getCustomName(),
      _userDbPlatform.convertBoolean(step.isDeleted()),
      step.getStrategyId().orElse(null),
      new StringReader(ParamsAndFiltersDbColumnFormat.formatParamFilters(spec).toString()),
      step.getDisplayPrefs() == null ? null :
        new StringReader(step.getDisplayPrefs().toString()),
      _userDbPlatform.convertBoolean(step.isExpanded()),
      step.getExpandedName()
    };
  }

  private void insertStep(Step step) {
    new SQLRunner(_userDbDs, buildInsertStepSQL())
      .executeUpdate(stepToInsertParams(step), getInsertStepParamTypes());
  }

  /**
   * Insert a collection of steps into the database.
   *
   * @param con
   *   Open connection to the DB.  This can be used to run the step insert
   *   queries in a controlled connection such as a transaction.
   * @param steps
   *   The collection of steps that will be inserted into the database.
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
    try {
      Connection connection = _userDbDs.getConnection();
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
      .setStrategyId(Optional.empty())
      .setEstimatedSize(Step.RESET_SIZE_FLAG); // always reset on copy

    MapBuilder<Long,StepBuilder> childSteps =
      oldStep.getAnswerSpec().hasValidQuestion()
        ? assignParamValues(oldStep.getUser(), oldStep.getAnswerSpec(), newUser, newStep.getAnswerSpec())
        : new MapBuilder<>(); // no child steps can be parsed from invalid question

    return childSteps.put(oldStep.getStepId(), newStep);
  }

  private MapBuilder<Long, StepBuilder> assignParamValues(User oldUser, AnswerSpec oldSpec, User newUser,
      AnswerSpecBuilder newSpec) throws WdkModelException {
    MapBuilder<Long,StepBuilder> newStepMap = new MapBuilder<>();
    for (Param param : oldSpec.getQuestion().getParams()) {
      String oldStableValue = oldSpec.getQueryInstanceSpec().get(param.getName());
      String replacementValue =
          param instanceof AnswerParam
          ? cloneAnswerParam(oldSpec, oldStableValue, newUser, newStepMap)
          : param instanceof DatasetParam
            ? cloneDatasetParam(oldUser, oldStableValue, newUser)
            : oldStableValue; // otherwise use original stable value
      newSpec.setParamValue(param.getName(), replacementValue);
    }
    return newStepMap;
  }

  // recursively copy AnswerParams (aka child steps) as child trees
  private String cloneAnswerParam(AnswerSpec oldSpec, String oldStableValue,
      User newUser, MapBuilder<Long, StepBuilder> stepIdsMap)
      throws WdkModelException {
    Step oldStepValue = oldSpec.getStepContainer()
        .findFirstStep(withId(Long.parseLong(oldStableValue)))
        .orElseThrow(() -> new WdkModelException("Step container cannot find expected step."));
    stepIdsMap.putAll(copyStepTree(newUser, oldStepValue).toMap());
    return Long.toString(stepIdsMap.get(oldStepValue.getStepId()).getStepId());
  }

  // clone DatasetParams (we want a fresh copy per step because we don't track which steps are using a dataset param)
  private String cloneDatasetParam(User oldUser, String oldStableValue,
      User newUser) throws WdkModelException {
    try {
      long oldDatasetId = Long.parseLong(oldStableValue);
      DatasetFactory datasetFactory = _wdkModel.getDatasetFactory();
      Dataset oldDataset = datasetFactory.getDatasetWithOwner(oldDatasetId, oldUser.getUserId());
      Dataset newDataset = datasetFactory.cloneDataset(oldDataset, newUser);
      return Long.toString(newDataset.getDatasetId());
    }
    catch (WdkUserException e) {
      // dataset ID does not exist or is not owned by the user; this is DB corruption
      throw new WdkModelException("Unable to clone dataset param value", e);
    }
  }

  public Optional<Strategy> getStrategyBySignature(String strategySignature)
      throws WdkModelException {
    return new StrategyLoader(_wdkModel, ValidationLevel.SEMANTIC, DEFAULT_DB_FILL_STRATEGY)
        .getStrategyBySignature(strategySignature);
  }

  /**
   * Overwrite the given strategy in the strategies table.
   *
   * @param strat
   *   Strategy to overwrite
   *
   * @return Whether or not that strategy was updated in the database.  A return
   *   value of false indicates that the strategy has not been created in the
   *   database.
   */
  public boolean updateStrategy(Strategy strat) throws WdkModelException {
    try(Connection con = _userDbDs.getConnection()) {
      return updateStrategy(con, strat);
    }
    catch (SQLException e) {
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
   * @param connection
   *   Open connection to the DB.  This can be used to run the step update
   *   queries in a controlled connection such as a transaction.
   * @param strat
   *   Strategy to overwrite
   *
   * @return Whether or not that strategy was updated in the database.  A return
   *   value of false indicates that the strategy has not been created in the
   *   database.
   */
  public boolean updateStrategy(Connection connection, Strategy strat)
      throws WdkModelException {
    final int boolType =_userDbPlatform.getBooleanType();
    final String sql = "UPDATE " + _userSchema + TABLE_STRATEGY + "\n" +
      "SET\n" +
      "  " + COLUMN_NAME               + " = ?,\n" +
      "  " + COLUMN_USER_ID            + " = ?,\n" +
      "  " + COLUMN_ROOT_STEP_ID       + " = ?,\n" +
      "  " + COLUMN_SAVED_NAME         + " = ?,\n" +
      "  " + COLUMN_IS_SAVED           + " = ?,\n" +
      "  " + COLUMN_DESCRIPTION        + " = ?,\n" +
      "  " + COLUMN_LAST_MODIFIED_TIME + " = ?,\n" +
      "  " + COLUMN_SIGNATURE          + " = ?,\n" +
      "  " + COLUMN_IS_PUBLIC          + " = ?,\n" +
      "  " + COLUMN_IS_DELETED         + " = ?\n" +
      "WHERE " + COLUMN_STRATEGY_ID + " = ?";

    final Object[] params = new Object[] {
      strat.getName(),
      strat.getUser().getUserId(),
      strat.getRootStep().getStepId(),
      strat.getSavedName(),
      _userDbPlatform.convertBoolean(strat.isSaved()),
      strat.getDescription(),
      new Timestamp(strat.getLastModifiedTime().getTime()),
      strat.getSignature(),
      _userDbPlatform.convertBoolean(strat.isPublic()),
      _userDbPlatform.convertBoolean(strat.isDeleted()),
      strat.getStrategyId()
    };

    final Integer[] paramTypes = new Integer[] {
      Types.VARCHAR,   // NAME
      Types.BIGINT,    // USER_ID
      Types.BIGINT,    // ROOT_STEP_ID
      Types.VARCHAR,   // SAVED_NAME
      boolType,        // IS_SAVED
      Types.VARCHAR,   // DESCRIPTION
      Types.TIMESTAMP, // LAST_MODIFY_TIME
      Types.VARCHAR,   // SIGNATURE
      boolType,        // IS_PUBLIC
      boolType,        // IS_DELETED
      Types.BIGINT     // STRATEGY_ID
    };

    try {
      // update strategy and all its steps in one transaction
      Wrapper<Boolean> stratUpdated = new Wrapper<>();
      SqlUtils.performInTransaction(connection, conn -> {
        stratUpdated.set(0 < new SQLRunner(conn, sql).executeUpdate(params, paramTypes));
        updateSteps(conn, strat.getAllSteps());
      });
      return stratUpdated.get();
    }
    catch (Exception e) {
      return WdkModelException.unwrap(e);
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

  /**
   * Overwrite the details of a step in the database.
   *
   * @param step
   *   the step that will will be updated
   *
   * @throws WdkModelException
   *   if a connection to the database cannot be opened.
   */
  public void updateStep(Step step) throws WdkModelException {
    updateSteps(ListBuilder.asList(step));
  }

  /**
   * Overwrite the details of a collection of steps in the database.
   *
   * @param steps
   *   The collection of steps that will be updated in the database.
   *
   * @throws WdkModelException
   *   if a connection to the database cannot be opened.
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
   * @param con
   *   Open connection to the DB.  This can be used to run the step update
   *   queries in a controlled connection such as a transaction.
   * @param steps
   *   The collection of steps that will be updated in the database.
   */
  private void updateSteps(Connection con, Collection<Step> steps) throws WdkModelException {
    final String sql = "UPDATE " + _userSchema + TABLE_STEP + "\n" +
        "SET\n" +
        "  " + COLUMN_USER_ID          + " = ?,\n" +
        "  " + COLUMN_PREVIOUS_STEP_ID + " = ?,\n" +
        "  " + COLUMN_CHILD_STEP_ID    + " = ?,\n" +
        "  " + COLUMN_LAST_RUN_TIME    + " = ?,\n" +
        "  " + COLUMN_ESTIMATE_SIZE    + " = ?,\n" +
        "  " + COLUMN_ANSWER_FILTER    + " = ?,\n" +
        "  " + COLUMN_CUSTOM_NAME      + " = ?,\n" +
        "  " + COLUMN_IS_DELETED       + " = ?,\n" +
        "  " + COLUMN_IS_VALID         + " = ?,\n" +
        "  " + COLUMN_ASSIGNED_WEIGHT  + " = ?,\n" +
        "  " + COLUMN_PROJECT_ID       + " = ?,\n" +
        "  " + COLUMN_PROJECT_VERSION  + " = ?,\n" +
        "  " + COLUMN_QUESTION_NAME    + " = ?,\n" +
        "  " + COLUMN_STRATEGY_ID      + " = ?,\n" +
        "  " + COLUMN_DISPLAY_PARAMS   + " = ?,\n" +
        "  " + COLUMN_DISPLAY_PREFS    + " = ?,\n" +
        "  " + COLUMN_IS_EXPANDED      + " = ?,\n" +
        "  " + COLUMN_EXPANDED_NAME    + " = ?\n"  +
        "WHERE\n" +
        "  " + COLUMN_STEP_ID + " = ?";

    final BasicArgumentBatch batch = new BasicArgumentBatch();
    final int boolType = _userDbPlatform.getBooleanType();
    batch.setParameterTypes(new Integer[]{
      Types.BIGINT,    // USER_ID
      Types.BIGINT,    // LEFT_CHILD_ID
      Types.BIGINT,    // RIGHT_CHILD_ID
      Types.TIMESTAMP, // LAST_RUN_TIME
      Types.BIGINT,    // ESTIMATE_SIZE
      Types.VARCHAR,   // ANSWER_FILTER
      Types.VARCHAR,   // CUSTOM_NAME
      boolType,        // IS_DELETED
      boolType,        // IS_VALID
      Types.BIGINT,    // ASSIGNED_WEIGHT
      Types.VARCHAR,   // PROJECT_ID
      Types.VARCHAR,   // PROJECT_VERSION
      Types.VARCHAR,   // QUESTION_NAME
      Types.BIGINT,    // STRATEGY_ID
      Types.CLOB,      // DISPLAY_PARAMS
      Types.CLOB,      // DISPLAY_PREFS
      boolType,        // IS_EXPANDED
      Types.VARCHAR,   // EXPANDED_NAME
      Types.BIGINT,    // STEP_ID
    });

    for (final Step step : steps) {
      final AnswerSpec spec = step.getAnswerSpec();

      batch.add(new Object[]{
        step.getUser().getUserId(),
        step.getPrimaryInputStep().map(Step::getStepId).orElse(null),
        step.getSecondaryInputStep().map(Step::getStepId).orElse(null),
        step.getLastRunTime(),
        step.getEstimatedSize(),
        spec.getLegacyFilterName().orElse(null),
        step.getCustomName(),
        _userDbPlatform.convertBoolean(step.isDeleted()),
        _userDbPlatform.convertBoolean(step.isValid()),
        spec.getQueryInstanceSpec().getAssignedWeight(),
        step.getProjectId(),
        step.getProjectVersion(),
        spec.getQuestionName(),
        step.getStrategyId().orElse(null),
        ParamsAndFiltersDbColumnFormat.formatParamFilters(spec),
        step.getDisplayPrefs().toString(),
        _userDbPlatform.convertBoolean(step.isExpanded()),
        step.getExpandedName(),
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
        "ORDER BY " + COLUMN_NAME + " DESC";

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
          int greatestIndex = 1;
          while (rs.next()) {
            int parsedIndex = parseStrategyNameIndex(rs.getString(COLUMN_NAME), oldName)
                .orElse(0);
            if (parsedIndex > greatestIndex) {
              greatestIndex = parsedIndex;
            }
          }

          return greatestIndex > 1
              ? String.format("%s (%d)", oldName, greatestIndex)
              : oldName;
        }
    );
  }

  /**
   * Attempt to parse an int out of parenthesis at the end of a strategy name.
   *
   * @param test
   *   String to check for appended counter
   * @param against
   *   Original strategy name
   *
   * @return If a counter value is present and in the correct format, an option
   *   wrapping that int. If no counter is present or is misformatted, an option
   *   of none.
   */
  private static Optional<Integer> parseStrategyNameIndex(String test, String against) {
    test = test.trim();
    against = against.trim();
    if (!test.startsWith(against)) {
      // bad SQL above
      throw new WdkRuntimeException("Incoming name '" + test +
          "' does not start with original strategy name '" + against + "'.");
    }
    if (test.length() == against.length()) {
      // same string
      return Optional.empty();
    }
    test = test.substring(against.length()).trim();

    // see if remaining string has parens in the right place
    if (test.startsWith("(") && test.endsWith(")")) {
      test = test.substring(1, test.length() - 1).trim();
      try {
        return Optional.of(Integer.parseInt(test));
      }
      catch (NumberFormatException e) {
        return Optional.empty();
      }
    }
    // unable to parse number
    return Optional.empty();
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

  public Optional<Step> getStepByIdAndUserId(long stepId, long userId,
      ValidationLevel validationLevel) throws WdkModelException {
    Optional<Step> step = getStepById(stepId, validationLevel);
    if (step.isPresent() && step.get().getUser().getUserId() != userId) {
      return Optional.empty();
    }
    return step;
  }

  /**
   * Transfers ownership of all the strategies belonging to one user to another
   * user.
   * 
   * @param fromUser user strats will be transferred from
   * @param toUser  user strats will be transferred to
   * @throws WdkModelException
   */
  public void transferStrategyOwnership(User guestUser, User registeredUser) throws WdkModelException {
    LOG.debug("Transferring user #" + guestUser.getUserId() + "'s strategies to user #" + registeredUser.getUserId() + "...");
    UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrats = new UnbuildableStrategyList<>();
    UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors = new UnbuildableStrategyList<>();
    for (Strategy strategy : getStrategies(guestUser.getUserId(), ValidationLevel.NONE,
        FillStrategy.NO_FILL, malformedStrats, stratsWithBuildErrors).values()) {
      StrategyBuilder builder = null;
      try {
        builder = Strategy.builder(strategy).setUserId(registeredUser.getUserId());
        updateStrategy(builder.build(new UserCache(registeredUser), ValidationLevel.NONE));
      }
      catch (InvalidStrategyStructureException e) {
        logMalformedStrat(new TwoTuple<>(builder, e));
      }
    }
    malformedStrats.stream().forEach(tuple -> logMalformedStrat(tuple));
    stratsWithBuildErrors.stream().forEach(tuple -> logMalformedStrat(tuple));
  }

  private static <T extends Exception> void logMalformedStrat(TwoTuple<StrategyBuilder, T> malformedStrat) {
    LOG.warn("Unable to transfer ownership of strategy: " +
        malformedStrat.getFirst() + FormatUtil.NL +
        "For the following reason:" + malformedStrat.getSecond());
  }
}
