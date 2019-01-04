package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.Oracle;
import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.model.MDCUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkIllegalArgumentException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 */
public class StepFactory {

  private static final String TABLE_STEP = "steps";
  private static final String TABLE_STRATEGY = "strategies";

  private static final String COLUMN_USER_ID = Utilities.COLUMN_USER_ID;
  private static final String COLUMN_STEP_ID = "step_id";
  static final String COLUMN_LEFT_CHILD_ID = "left_child_id";
  static final String COLUMN_RIGHT_CHILD_ID = "right_child_id";
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
  private static final String COLUMN_IS_ALL_STEPS_VALID = "is_all_steps_valid";
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

  private static final boolean USE_CACHE = false;

  static final int COLUMN_NAME_LIMIT = 200;

  private static final int UNKNOWN_SIZE = -1;

  private static final Logger LOG = Logger.getLogger(StepFactory.class);

  public static class NameCheckInfo {
    private boolean _nameExists;
    private boolean _isPublic;
    private String _description;

    public NameCheckInfo(boolean nameExists, boolean isPublic, String description) {
      _nameExists = nameExists;
      _isPublic = isPublic;
      _description = description;
    }

    public boolean nameExists() {
      return _nameExists;
    }

    public boolean isPublic() {
      return _isPublic;
    }

    public String getDescription() {
      return _description;
    }
  }

  private final WdkModel _wdkModel;
  // these other fields are also 'final' but are assigned in initialize()
  private String _userSchema;
  private DatabaseInstance _userDb;
  private DataSource _userDbDs;

  private StepFetcherProvider _stepFetcherProvider;

  // define SQL snippet "constants" to avoid building SQL each time
  private String modTimeSortSql;
  private String invalidStratSubquerySql;
  private String basicStratsSql;
  private String isNotDeletedCondition;
  private String byProjectCondition;
  private String byUserCondition;
  private String bySignatureCondition;
  private String isPublicCondition;
  private String isRootStepValidCondition;
  private String byStratIdCondition;
  private String isSavedCondition;
  private String byLastViewedCondition;
  private String stratsByUserSql;
  private String stratBySignatureSql;
  private String unsortedPublicStratsSql;
  private String countValidPublicStratsSql;
  private String updatePublicStratStatusSql;

  public StepFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    initialize();
  }

  /**
   * Initialize the step factory.  Kept separate from constructor so unit tests can subclass and not do
   * any DB interaction.
   */
  protected void initialize() {
    _userDb = _wdkModel.getUserDb();
    _userDbDs = _userDb.getDataSource();

    ModelConfigUserDB userDbConfig = _wdkModel.getModelConfig().getUserDB();
    _userSchema = userDbConfig.getUserSchema();

    _stepFetcherProvider = new StepFetcherProvider(this);

    /* define "static" SQL statements dependent only on schema name */

    // sort options
    modTimeSortSql = new StringBuilder(" ORDER BY sr.").append(COLUMN_LAST_MODIFIED_TIME).append(" DESC").toString();

    // estimate validity of strategies by executing subquery against steps in the strategy
    invalidStratSubquerySql = "( SELECT sr." + COLUMN_STRATEGY_ID + ", min(CAST(" + 
    	_userDb.getPlatform().getNvlFunctionName() + "(" + COLUMN_IS_VALID +
        ", " + _userDb.getPlatform().convertBoolean(true) + ") AS INTEGER)) AS " + COLUMN_IS_VALID + " FROM " + _userSchema + TABLE_STRATEGY + " sr, " + _userSchema +
        TABLE_STEP + " sp WHERE sp." + COLUMN_STRATEGY_ID + " = sr." + COLUMN_STRATEGY_ID + " GROUP BY sr." +
        COLUMN_STRATEGY_ID + " )";
    
    // basic select with required joins
    basicStratsSql = "SELECT sr.*, sp." + COLUMN_ESTIMATE_SIZE + ", sp." + COLUMN_IS_VALID + ", sp." +
        COLUMN_QUESTION_NAME + ", sv." + COLUMN_IS_VALID + " AS " + COLUMN_IS_ALL_STEPS_VALID + " FROM " + 
        _userSchema + TABLE_STRATEGY + " sr, " + _userSchema + TABLE_STEP + " sp, " + invalidStratSubquerySql +
        " sv WHERE sr." + COLUMN_ROOT_STEP_ID + " = sp." + COLUMN_STEP_ID + " AND sr." + COLUMN_USER_ID +
        " = sp." + COLUMN_USER_ID + " AND sr." + COLUMN_PROJECT_ID + " = sp." + COLUMN_PROJECT_ID + " AND sr." +
        COLUMN_STRATEGY_ID + " = sv." + COLUMN_STRATEGY_ID;

    // conditions for strategies

    // does not add any wildcards
    isNotDeletedCondition = new StringBuilder(" AND sr.").append(COLUMN_IS_DELETED).append(" = ").append(
        _userDb.getPlatform().convertBoolean(false)).toString();
    // adds wildcard for project ID (string)
    byProjectCondition = new StringBuilder(" AND sr.").append(COLUMN_PROJECT_ID).append(" = ?").toString();
    // adds wildcard for user ID (integer)
    byUserCondition = new StringBuilder(" AND sr.").append(COLUMN_USER_ID).append(" = ?").toString();
    // adds wildcard for strategy ID (integer)
    byStratIdCondition = new StringBuilder(" AND sr.").append(COLUMN_STRATEGY_ID).append(" = ?").toString();
    // adds wildcard for isSaved (boolean)
    isSavedCondition = new StringBuilder(" AND sr.").append(COLUMN_IS_SAVED).append(" = ?").toString();
    // adds wildcard for lastViewedTime (timestamp)
    byLastViewedCondition = new StringBuilder(" AND sr.").append(COLUMN_LAST_VIEWED_TIME).append(" >= ?").toString();
    // adds wildcard for signature (string)
    bySignatureCondition = new StringBuilder(" AND sr.").append(COLUMN_SIGNATURE).append(" = ? ").toString();
    // does not add any wildcards
    isPublicCondition = new StringBuilder(" AND sr.").append(COLUMN_IS_PUBLIC).append(" = ").append(
        _userDb.getPlatform().convertBoolean(true)).toString();
    // does not add any wildcards
    // NOTE: need to include null is_valid values, which should evaluate to 'true'
    // because of the way EuPathDb maintains the valid step values between releases
    isRootStepValidCondition = new StringBuilder(" AND (sp.").append(COLUMN_IS_VALID).append(
        " is null OR sp.").append(COLUMN_IS_VALID).append(" = ").append(
        _userDb.getPlatform().convertBoolean(true)).append(")").toString();

    // adds wildcard for project
    String aliveByProjectSql = new StringBuilder(basicStratsSql).append(isNotDeletedCondition).append(
        byProjectCondition).toString();

    // contains wildcards for project, user ID; does not contain ordering
    stratsByUserSql = new StringBuilder(aliveByProjectSql).append(byUserCondition).toString();

    // contains wildcards for project, signature; contains ordering
    stratBySignatureSql = new StringBuilder(aliveByProjectSql).append(bySignatureCondition).append(
        modTimeSortSql).toString();

    // contains wildcard for project; does not contain ordering
    unsortedPublicStratsSql = new StringBuilder(aliveByProjectSql).append(isPublicCondition).toString();

    // contains wildcard for project; does not contain ordering
    countValidPublicStratsSql = new StringBuilder("select count(1) from ( ").append(unsortedPublicStratsSql).append(
        isRootStepValidCondition).append(" ) cps ").toString();

    // contains wildcards for is_public (boolean) and strat ID (int)
    updatePublicStratStatusSql = new StringBuilder().append("UPDATE ").append(_userSchema).append(
        TABLE_STRATEGY).append(" SET ").append(COLUMN_IS_PUBLIC).append(" = ?").append(" WHERE ").append(
        COLUMN_STRATEGY_ID).append(" = ?").toString();

    // start the purge thread for the cache
    // new Thread(stepCache).start();
  }

  WdkModel getWdkModel() {
    return _wdkModel;
  }
  
  /**
   * Creates a step using new step service concept.  
   * @param user
   * @param question
   * @param dependentValues
   * @param filter
   * @param pageStart
   * @param pageEnd
   * @param deleted
   * @param validate
   * @param assignedWeight
   * @param filterOptions
   * @param customName
   * @param isCollapsible
   * @param collapsedName
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Step createStep(User user, Question question, Map<String, String> dependentValues,
      AnswerFilterInstance filter, int pageStart, int pageEnd, boolean deleted, boolean validate,
      int assignedWeight, FilterOptionList filterOptions, String customName, boolean isCollapsible,
      String collapsedName) throws WdkModelException, WdkUserException {
    LOG.debug("Creating step!");

    String questionName = question.getFullName();
   
    // prepare the values to be inserted.
    long userId = user.getUserId();

    String filterName = null;
   
    Exception exception = null;
   

    // prepare SQLs
    String userIdColumn = Utilities.COLUMN_USER_ID;

    StringBuffer sqlInsertStep = new StringBuffer("INSERT INTO ");
    sqlInsertStep.append(_userSchema).append(TABLE_STEP).append(" (");
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
    sqlInsertStep.append(COLUMN_STRATEGY_ID).append(", ");
    sqlInsertStep.append(COLUMN_DISPLAY_PARAMS).append(", ");
    sqlInsertStep.append(COLUMN_CUSTOM_NAME).append(", ");
    sqlInsertStep.append(COLUMN_IS_COLLAPSIBLE).append(", ");
    sqlInsertStep.append(COLUMN_COLLAPSED_NAME).append(") ");
    
    sqlInsertStep.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    // Create the Step sans Answer
    Date createTime = new Date();
    //TODO lastRunTime should be made null once the last_run_time field in db is nullable.
    Date lastRunTime = new Date(createTime.getTime());

    long stepId;

    try {
      stepId = _userDb.getPlatform().getNextId(_userDbDs, _userSchema, TABLE_STEP);
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }

    // create the Step
    Step step = new Step(this, user, stepId);
    step.setQuestionName(questionName);
    step.setCreatedTime(createTime);
    step.setLastRunTime(lastRunTime);
    step.setDeleted(deleted);
    step.setParamValues(dependentValues);
    LOG.debug("Creating step: to set Filter Options and to add Default Filters");
    step.setFilterOptions(filterOptions);
    applyAlwaysOnFiltersToNewStep(step);
    step.setEstimateSize(-1);
    step.setAssignedWeight(assignedWeight);
    step.setException(exception);
    step.setProjectId(_wdkModel.getProjectId());
    step.setProjectVersion(_wdkModel.getVersion());
    step.setCustomName(customName);
    step.setCollapsible(isCollapsible);
    step.setCollapsedName(collapsedName);

    PreparedStatement psInsertStep = null;
    try {
      JSONObject jsParamFilters = step.getParamFilterJSON();

      psInsertStep = SqlUtils.getPreparedStatement(_userDbDs, sqlInsertStep.toString());
      psInsertStep.setLong(1, stepId);
      psInsertStep.setLong(2, userId);
      psInsertStep.setTimestamp(3, new Timestamp(createTime.getTime()));
      psInsertStep.setTimestamp(4, new Timestamp(lastRunTime.getTime()));
      psInsertStep.setInt(5, 0);
      psInsertStep.setString(6, filterName);
      psInsertStep.setBoolean(7, deleted);
      psInsertStep.setInt(8, assignedWeight);
      psInsertStep.setString(9, _wdkModel.getProjectId());
      psInsertStep.setString(10, _wdkModel.getVersion());
      psInsertStep.setString(11, questionName);
      psInsertStep.setObject(12, null);
      _userDb.getPlatform().setClobData(psInsertStep, 13, JsonUtil.serialize(jsParamFilters), false);
      psInsertStep.setString(14,  customName);
      psInsertStep.setBoolean(15, isCollapsible);
      psInsertStep.setString(16, collapsedName);
      psInsertStep.executeUpdate();
    }
    catch (SQLException | JSONException ex) {
      throw new WdkModelException("Error while creating step: " + ex.getMessage(), ex);
    }
    finally {
      SqlUtils.closeStatement(psInsertStep);
    }
    LOG.debug("Step created!!: " + stepId + "\n\n");
    return step;
  }

  // parse boolexp to pass left_child_id, right_child_id to loadAnswer
  public Step createStep(User user, Long strategyId, Question question, Map<String, String> dependentValues,
      AnswerFilterInstance filter, boolean deleted, boolean validate,
      int assignedWeight, FilterOptionList filterOptions) throws WdkModelException {
    LOG.debug("Creating step!");

    // get summary list and sorting list
    String questionName = question.getFullName();
    Map<String, Boolean> sortingAttributes = user.getPreferences().getSortingAttributes(
        questionName, UserPreferences.DEFAULT_SUMMARY_VIEW_PREF_SUFFIX);

    // prepare the values to be inserted.
    long userId = user.getUserId();

    String filterName = null;
    int estimateSize;
    Exception exception = null;
    try {
      // create answer
      AnswerValue answerValue = question.makeAnswerValue(user, dependentValues, 0, -1,
          sortingAttributes, filter, validate, assignedWeight);
      answerValue.setFilterOptions(filterOptions);

      QueryInstance<?> queryInstance = answerValue.getIdsQueryInstance();
      LOG.debug("id query name  :" + (queryInstance == null ? "<no_query_specified>" : queryInstance.getQuery().getFullName()));
      LOG.debug("answer checksum:" + answerValue.getChecksum());
      LOG.debug("question name:  " + question.getFullName());
      estimateSize = answerValue.getResultSizeFactory().getDisplayResultSize();
    }
    catch (Exception ex) {
      estimateSize = 0;
      LOG.error("creating step failed", ex);
      exception = ex;
    }

    // prepare SQLs
    String userIdColumn = Utilities.COLUMN_USER_ID;

    StringBuffer sqlInsertStep = new StringBuffer("INSERT INTO ");
    sqlInsertStep.append(_userSchema).append(TABLE_STEP).append(" (");
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
    sqlInsertStep.append(COLUMN_STRATEGY_ID).append(", ");
    sqlInsertStep.append(COLUMN_DISPLAY_PARAMS).append(") ");
    sqlInsertStep.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    // Now that we have the Answer, create the Step
    Date createTime = new Date();
    Date lastRunTime = new Date(createTime.getTime());

    long stepId;

    try {
      stepId = _userDb.getPlatform().getNextId(_userDbDs, _userSchema, TABLE_STEP);
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }

    // create the Step
    Step step = new Step(this, user, stepId);
    step.setQuestionName(questionName);
    step.setCreatedTime(createTime);
    step.setLastRunTime(lastRunTime);
    step.setDeleted(deleted);
    step.setParamValues(dependentValues);
    LOG.debug("Creating step: to set Filter Options and to add Default Filters");
    step.setFilterOptions(filterOptions);
    applyAlwaysOnFiltersToNewStep(step);
    step.setEstimateSize(estimateSize);
    step.setAssignedWeight(assignedWeight);
    step.setException(exception);
    step.setProjectId(_wdkModel.getProjectId());
    step.setProjectVersion(_wdkModel.getVersion());

    PreparedStatement psInsertStep = null;
    try {
      JSONObject jsParamFilters = step.getParamFilterJSON();

      psInsertStep = SqlUtils.getPreparedStatement(_userDbDs, sqlInsertStep.toString());
      psInsertStep.setLong(1, stepId);
      psInsertStep.setLong(2, userId);
      psInsertStep.setTimestamp(3, new Timestamp(createTime.getTime()));
      psInsertStep.setTimestamp(4, new Timestamp(lastRunTime.getTime()));
      psInsertStep.setInt(5, estimateSize);
      psInsertStep.setString(6, filterName);
      psInsertStep.setBoolean(7, deleted);
      psInsertStep.setInt(8, assignedWeight);
      psInsertStep.setString(9, _wdkModel.getProjectId());
      psInsertStep.setString(10, _wdkModel.getVersion());
      psInsertStep.setString(11, questionName);
      psInsertStep.setObject(12, strategyId);
      _userDb.getPlatform().setClobData(psInsertStep, 13, JsonUtil.serialize(jsParamFilters), false);
      psInsertStep.executeUpdate();
    }
    catch (SQLException | JSONException ex) {
      throw new WdkModelException("Error while creating step: " + ex.getMessage(), ex);
    }
    finally {
      SqlUtils.closeStatement(psInsertStep);
    }

    // update step dependencies
    if (step.isCombined())
      updateStepTree(step);

    LOG.debug("Step created!!: " + stepId + "\n\n");
    return step;
  }

  void deleteStepAndChildren(int stepId) throws WdkModelException, SQLException {
    String selectSql = selectStepAndChildren(stepId);
    int count = SqlUtils.executeUpdate(_userDbDs, "DELETE FROM " + _userSchema + "steps WHERE step_id IN (" +
        selectSql + ")", "wdk-step-delete-children");
    LOG.debug(count + " steps deleted from id: " + stepId);
  }

  public void deleteStep(long stepId) throws WdkModelException {
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_IS_DELETED + " = " +
        _userDb.getPlatform().convertBoolean(true) + " WHERE " + COLUMN_STEP_ID + " = ?";
    try {
      int result = new SQLRunner(_userDbDs, sql, "wdk-step-factory-delete-step")
          .executeUpdate(new Object[]{ stepId }, new Integer[]{ Types.BIGINT });
      if (result == 0) {
        throw new WdkModelException("The Step #" + stepId + " cannot be found.");
      }
      // stepCache.removeStep(user.getUserId(), displayId);
    }
    catch (Exception e) {
      WdkModelException.unwrap(e, "Could not delete step " + stepId);
    }
  }

  public boolean isStepReferenced(long stepId) throws WdkModelException {
    try {
      String sql = "SELECT count(*) FROM (                                      " +
          "   SELECT step_id FROM " + _userSchema + "steps                       " +
          "   WHERE left_child_id = " + stepId + " OR right_child_id = " + stepId +
          " UNION                                                               " +
          "   SELECT root_step_id FROM " + _userSchema + "strategies WHERE root_step_id = " + stepId + ")";

      Object result = SqlUtils.executeScalar(_userDbDs, sql, "wdk-step-factory-check-depended");
      int count = Integer.parseInt(result.toString());
      return (count > 0);
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  void deleteSteps(User user, boolean allProjects) throws WdkModelException {
    PreparedStatement psDeleteSteps = null;
    String stepTable = _userSchema + TABLE_STEP;
    String strategyTable = _userSchema + TABLE_STRATEGY;
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

      // stepCache.removeSteps(user.getUserId());
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
      // remove history
      /*
       * psStrategy = SqlUtils.getPreparedStatement(dataSource, "DELETE " + "FROM " + userSchema +
       * TABLE_STRATEGY + " WHERE " + Utilities.COLUMN_USER_ID + " = ? " + "AND " + COLUMN_PROJECT_ID +
       * " = ? AND " + COLUMN_DISPLAY_ID + " = ?"); psStrategy.setInt(1, user.getUserId());
       * psStrategy.setString(2, wdkModel.getProjectId()); psStrategy.setInt(3, displayId); int result =
       * psStrategy.executeUpdate(); if (result == 0) throw new WdkUserException("The strategy #" + displayId
       * + " of user " + user.getEmail() + " cannot be found.");
       */
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

  void deleteStrategies(User user, boolean allProjects) throws WdkModelException {
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

  public void deleteInvalidSteps(User user) throws WdkModelException {
    // get invalid histories
    Map<Long, Step> invalidSteps = new LinkedHashMap<>();
    loadSteps(user, invalidSteps);
    for (long stepId : invalidSteps.keySet()) {
      deleteStep(stepId);
    }
  }

  public void deleteInvalidStrategies(User user) throws WdkModelException {
    // get invalid histories
    Map<Long, Strategy> invalidStrategies = new LinkedHashMap<>();
    loadStrategies(user, invalidStrategies);
    for (long strategyId : invalidStrategies.keySet()) {
      deleteStrategy(strategyId);
    }
  }

  public int getStepCount(User user) throws WdkModelException {
    String stepTable = _userSchema + TABLE_STEP;
    PreparedStatement psHistory = null;
    ResultSet rsStep = null;
    String sql = "SELECT count(*) AS num FROM " + stepTable + " WHERE " + Utilities.COLUMN_USER_ID +
        " = ? AND " + COLUMN_PROJECT_ID + " = ? " + " AND is_deleted = ?";
    try {
      long start = System.currentTimeMillis();
      psHistory = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psHistory.setLong(1, user.getUserId());
      psHistory.setString(2, _wdkModel.getProjectId());
      psHistory.setBoolean(3, false);
      rsStep = psHistory.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-step-count", start);
      rsStep.next();
      return rsStep.getInt("num");
    }
    catch (SQLException ex) {
      throw new WdkModelException("Could not get step count for user " + user.getEmail(), ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsStep, psHistory);
    }
  }

  Map<Long, Step> loadSteps(User user, Map<Long, Step> invalidSteps) throws WdkModelException {

    Map<Long, Step> steps = new LinkedHashMap<>();
    PreparedStatement psStep = null;
    ResultSet rsStep = null;
    String sql = "SELECT * FROM " + _userSchema + TABLE_STEP + " WHERE " + Utilities.COLUMN_USER_ID +
        " = ? AND " + COLUMN_PROJECT_ID + " = ? " + " ORDER BY " + COLUMN_LAST_RUN_TIME + " DESC";
    try {
      long start = System.currentTimeMillis();
      psStep = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStep.setLong(1, user.getUserId());
      psStep.setString(2, _wdkModel.getProjectId());
      rsStep = psStep.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, "wdk-step-factory-load-all-steps", start, rsStep);

      while (rsStep.next()) {
        Step step = loadStep(user, rsStep);
        long stepId = step.getStepId();
        steps.put(stepId, step);
        if (!step.isValid())
          invalidSteps.put(stepId, step);
      }
    }
    catch (SQLException | JSONException ex) {
      throw new WdkModelException("Could not load steps for user", ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsStep, psStep);
    }
    LOG.debug("Steps: " + steps.size());
    LOG.debug("Invalid: " + invalidSteps.size());
    return steps;
  }

  /**
   * @param stepId step ID for which to retrieve step
   * @return step by step ID
   * @throws WdkModelException if step not found or problem occurs
   */
  public Step getStepById(long stepId) throws WdkModelException {
    return loadStep(null, stepId);
  }

  // get left child id, right child id in here
  Step loadStep(User user, long stepId) throws WdkModelException {
    if (USE_CACHE) {
      try {
        return CacheMgr.get().getStepCache().getValue(stepId, _stepFetcherProvider.getFetcher(user));
      }
      catch (ValueProductionException e) {
        throw (WdkModelException)e.getCause();
      }
    }
    return loadStepNoCache(user, stepId);
  }

  Step loadStepNoCache(User user, long stepId) throws WdkModelException {
    LOG.debug("Loading step#" + stepId + "....");
    PreparedStatement psStep = null;
    ResultSet rsStep = null;
    String sql = "SELECT * FROM " + _userSchema + TABLE_STEP + " WHERE " + COLUMN_STEP_ID + " = ?";
    try {
      long start = System.currentTimeMillis();
      psStep = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStep.setLong(1, stepId);
      rsStep = psStep.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-load-step", start);

      LOG.debug("SELECT step#" + stepId + " SQL finished execution, now creating step object...");
      if (!rsStep.next())
        throw new WdkModelException("The Step #" + stepId + " of user " +
            (user == null ? "unspecified" : user.getEmail()) + " doesn't exist.");

      Step step = loadStep(user, rsStep);
      LOG.debug("Step#" + stepId + " object loaded.");
      return step;
    }
    catch (SQLException | JSONException ex) {
      throw new WdkModelException("Unable to load step.", ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsStep, psStep);
    }
  }

  private Step loadStep(User user, ResultSet rsStep) throws WdkModelException, SQLException, JSONException {
    LOG.debug("\nStepFactory: loadStep()\n");

    // load Step info
    long stepId = rsStep.getLong(COLUMN_STEP_ID);
    long userId = rsStep.getLong(Utilities.COLUMN_USER_ID);

    Step step = (user == null ? new Step(this, userId, stepId) : new Step(this, user, stepId));
    step.setQuestionName(rsStep.getString(COLUMN_QUESTION_NAME));
    step.setCreatedTime(rsStep.getTimestamp(COLUMN_CREATE_TIME));
    step.setLastRunTime(rsStep.getTimestamp(COLUMN_LAST_RUN_TIME));
    step.setCustomName(rsStep.getString(COLUMN_CUSTOM_NAME));
    step.setDeleted(rsStep.getBoolean(COLUMN_IS_DELETED));
    step.setCollapsible(rsStep.getBoolean(COLUMN_IS_COLLAPSIBLE));
    step.setCollapsedName(rsStep.getString(COLUMN_COLLAPSED_NAME));
    step.setEstimateSize(rsStep.getInt(COLUMN_ESTIMATE_SIZE));
    step.setFilterName(rsStep.getString(COLUMN_ANSWER_FILTER));
    step.setProjectId(rsStep.getString(COLUMN_PROJECT_ID));
    step.setProjectVersion(rsStep.getString(COLUMN_PROJECT_VERSION));
    if (rsStep.getObject(COLUMN_IS_VALID) != null)
      step.setValid(rsStep.getBoolean(COLUMN_IS_VALID));
    if (rsStep.getObject(COLUMN_ASSIGNED_WEIGHT) != null)
      step.setAssignedWeight(rsStep.getInt(COLUMN_ASSIGNED_WEIGHT));
    if (rsStep.getObject(COLUMN_STRATEGY_ID) != null)
      step.setStrategyId(rsStep.getLong(COLUMN_STRATEGY_ID));

    // load left and right child
    if (rsStep.getObject(COLUMN_LEFT_CHILD_ID) != null) {
      long leftStepId = rsStep.getLong(COLUMN_LEFT_CHILD_ID);
      step.setPreviousStepId(leftStepId);
    }
    if (rsStep.getObject(COLUMN_RIGHT_CHILD_ID) != null) {
      long rightStepId = rsStep.getLong(COLUMN_RIGHT_CHILD_ID);
      step.setChildStepId(rightStepId);
    }

    String paramFilters = _userDb.getPlatform().getClobData(rsStep, COLUMN_DISPLAY_PARAMS);
    if (step.hasValidQuestion() && paramFilters != null && paramFilters.length() > 0) {
      // parse the param & filter values
      step.setParamFilterJSON(new JSONObject(paramFilters));
    }

    // New for GUS4: apply any default filter values to this step that are automatically applied to
    //   new steps, but may not have been applied to steps already in the DB.  This allows model XML
    //   authors to add default filters to the model without worrying about existing steps in the DB (as
    //   long as they override and correctly implement the applyDefaultIfApplicable() method in their Filter.
    applyAlwaysOnFiltersToExistingStep(step);

    LOG.debug("loaded step #" + stepId);
    return step;
  }

  private void updateStepTree(Step step) throws WdkModelException {
    Question question = step.getQuestion();
    Map<String, String> displayParams = step.getParamValues();

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
    Date lastRunTime = (updateTime) ? new Date() : step.getLastRunTime();
    int estimateSize = step.getEstimateSize();
    PreparedStatement psStep = null;
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_CUSTOM_NAME + " = ?, " +
        COLUMN_LAST_RUN_TIME + " = ?, " + COLUMN_IS_DELETED + " = ?, " + COLUMN_IS_COLLAPSIBLE + " = ?, " +
        COLUMN_COLLAPSED_NAME + " = ?, " + COLUMN_ESTIMATE_SIZE + " = ?, " + COLUMN_IS_VALID + " = ?, " +
        COLUMN_ASSIGNED_WEIGHT + " = ? WHERE " + COLUMN_STEP_ID + " = ?";
    try {
      long start = System.currentTimeMillis();
      psStep = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStep.setString(1, step.getBaseCustomName());
      psStep.setTimestamp(2, new Timestamp(lastRunTime.getTime()));
      psStep.setBoolean(3, step.isDeleted());
      psStep.setBoolean(4, step.isCollapsible());
      psStep.setString(5, step.getCollapsedName());
      psStep.setInt(6, estimateSize);
      psStep.setBoolean(7, step.isValid());
      psStep.setInt(8, step.getAssignedWeight());
      psStep.setLong(9, step.getStepId());
      int result = psStep.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-update-step", start);
      if (result == 0)
        throw new WdkModelException("The Step #" + step.getStepId() + " of user " + user.getEmail() +
            " cannot be found.");

      // update the last run stamp
      step.setLastRunTime(lastRunTime);
      step.setEstimateSize(estimateSize);

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
        COLUMN_ASSIGNED_WEIGHT + " = ?, " + COLUMN_DISPLAY_PARAMS + " = ?, " + COLUMN_IS_VALID + " = ? " +
        "    WHERE " + COLUMN_STEP_ID + " = ?";

    DBPlatform platform = _wdkModel.getUserDb().getPlatform();
    JSONObject jsContent = step.getParamFilterJSON();
    long leftId = step.getPreviousStepId();
    long childId = step.getChildStepId();
    try {
      long start = System.currentTimeMillis();
      psStep = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStep.setString(1, step.getQuestionName());
      psStep.setString(2, step.getFilterName());
      if (leftId != 0)
        psStep.setLong(3, leftId);
      else
        psStep.setObject(3, null);
      if (childId != 0)
        psStep.setLong(4, childId);
      else
        psStep.setObject(4, null);
      psStep.setInt(5, step.getAssignedWeight());
      platform.setClobData(psStep, 6, JsonUtil.serialize(jsContent), false);
      psStep.setBoolean(7, true);
      psStep.setLong(8, step.getStepId());
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

  Map<Long, Strategy> loadStrategies(User user, Map<Long, Strategy> invalidStrategies)
      throws WdkModelException {
    Map<Long, Strategy> userStrategies = new LinkedHashMap<>();
    String sql = stratsByUserSql + modTimeSortSql;
    PreparedStatement psStrategyIds = null;
    ResultSet rsStrategyIds = null;
    try {
      long start = System.currentTimeMillis();
      psStrategyIds = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStrategyIds.setString(1, _wdkModel.getProjectId());
      psStrategyIds.setLong(2, user.getUserId());
      rsStrategyIds = psStrategyIds.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, "wdk-step-factory-load-all-strategies", start, rsStrategyIds);
      List<Strategy> strategies = loadStrategies(user, rsStrategyIds);
      for (Strategy strategy : strategies) {
        userStrategies.put(strategy.getStrategyId(), strategy);
        if (!strategy.isValid())
          invalidStrategies.put(strategy.getStrategyId(), strategy);
      }
      return userStrategies;
    }
    catch (SQLException sqle) {
      throw new WdkModelException("Could not load strategies for user " + user.getEmail(), sqle);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsStrategyIds, psStrategyIds);
    }
  }

  public List<Strategy> loadStrategies(User user, boolean saved, boolean recent) throws WdkModelException {
    StringBuilder sql = new StringBuilder(stratsByUserSql).append(isSavedCondition);
    if (recent)
      sql.append(byLastViewedCondition);
    sql.append(modTimeSortSql);

    List<Strategy> strategies;
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(_userDbDs, sql.toString());
      ps.setString(1, _wdkModel.getProjectId());
      ps.setLong(2, user.getUserId());
      ps.setBoolean(3, saved);
      if (recent) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date date = new Date();
        ps.setTimestamp(4, new Timestamp(date.getTime()));
      }
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(sql.toString(), "wdk-step-factory-load-strategies", start,
          resultSet);
      strategies = loadStrategies(user, resultSet);
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not load strategies for user " + user.getEmail(), e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
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
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      String publicStratsSql = unsortedPublicStratsSql + modTimeSortSql;
      LOG.debug("Executing SQL with one param ('" + _wdkModel.getProjectId() + "'): " + publicStratsSql);
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(_userDbDs, publicStratsSql);
      ps.setString(1, _wdkModel.getProjectId());
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(publicStratsSql, "wdk-step-factory-load-public-strategies",
          start, resultSet);
      return loadStrategies(null, resultSet);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to load public strategies", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }
  }

  public void setStrategyPublicStatus(int stratId, boolean isPublic) throws WdkModelException {
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
      throw new WdkModelException("Unable to update public strategy status" + " (" + stratId + "," +
          isPublic + ")", e);
    }
    finally {
      QueryLogger.logEndStatementExecution(updatePublicStratStatusSql,
          "wdk-step-factory-update-public-strat-status", startTime);
      SqlUtils.closeStatement(ps);
    }
  }

  public int getPublicStrategyCount() throws WdkModelException {
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      LOG.debug("Executing SQL with one param ('" + _wdkModel.getProjectId() + "'): " +
          countValidPublicStratsSql);
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(_userDbDs, countValidPublicStratsSql);
      ps.setString(1, _wdkModel.getProjectId());
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(countValidPublicStratsSql,
          "wdk-step-factory-count-valid-public-strategies", start, resultSet);
      if (resultSet.next()) {
        return resultSet.getInt(1);
      }
      throw new WdkModelException("Count query returned no rows.");
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to count public strategies", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }
  }

  private List<Strategy> loadStrategies(User user, ResultSet resultSet) throws WdkModelException,
      SQLException {
    List<Strategy> strategies = new ArrayList<>();
    boolean loadUserPerStrat = (user == null);
    Map<Long, User> userMap = new HashMap<>();

    while (resultSet.next()) {

      // load user if needed
      if (loadUserPerStrat) {
        Long userId = resultSet.getLong(Utilities.COLUMN_USER_ID);
        user = userMap.get(userId);
        if (user == null) {
          user = _wdkModel.getUserFactory().getUserById(userId);
          userMap.put(userId, user);
        }
      }

      long strategyId = resultSet.getLong(COLUMN_STRATEGY_ID);

      Strategy strategy = new Strategy(this, user, strategyId);
      strategy.setName(resultSet.getString(COLUMN_NAME));
      strategy.setCreatedTime(resultSet.getTimestamp(COLUMN_CREATE_TIME));
      strategy.setIsSaved(resultSet.getBoolean(COLUMN_IS_SAVED));
      strategy.setDeleted(resultSet.getBoolean(COLUMN_IS_DELETED));
      strategy.setSavedName(resultSet.getString(COLUMN_SAVED_NAME));
      strategy.setLastModifiedTime(resultSet.getTimestamp(COLUMN_LAST_MODIFIED_TIME));
      strategy.setSignature(resultSet.getString(COLUMN_SIGNATURE));
      strategy.setDescription(resultSet.getString(COLUMN_DESCRIPTION));
      strategy.setLatestStepId(resultSet.getLong(COLUMN_ROOT_STEP_ID));
      strategy.setProjectId(resultSet.getString(COLUMN_PROJECT_ID));

      strategy.setLastRunTime(resultSet.getTimestamp(COLUMN_LAST_VIEWED_TIME));
      strategy.setEstimateSize(resultSet.getInt(COLUMN_ESTIMATE_SIZE));
      strategy.setVersion(resultSet.getString(COLUMN_VERSION));
      strategy.setValidBasedOnStepFlags(resultSet.getBoolean(COLUMN_IS_ALL_STEPS_VALID));
      if (resultSet.getObject(COLUMN_IS_VALID) != null)
        strategy.setValid(resultSet.getBoolean(COLUMN_IS_VALID));
      if (resultSet.getObject(COLUMN_IS_PUBLIC) != null)
        strategy.setIsPublic(resultSet.getBoolean(COLUMN_IS_PUBLIC));

      // load recordClass for the strategy
      String questionName = resultSet.getString(COLUMN_QUESTION_NAME);
      try {
        Question question = _wdkModel.getQuestion(questionName);
        strategy.setRecordClass(question.getRecordClass());
      }
      catch (WdkModelException ex) { // the question doesn't exist; this is a root step and so we cannot get
                                     // the strategy recordclass;
        // skip such strategies for now, since we dont have an "unknown" type tab in All Tab in front end
        continue;
        // strategy.setValid(false);
      }

      String signature = strategy.getSignature();
      if (signature == null || signature.trim().length() == 0) {
        signature = getStrategySignature(user.getUserId(), strategyId);
        String sql = "UPDATE " + _userSchema + TABLE_STRATEGY + " SET signature = " + "'" + signature +
            "' WHERE strategy_id = " + strategyId;
        SqlUtils.executeUpdate(_userDbDs, sql, "wdk-step-factory-update-strategy-signature");
        strategy.setSignature(signature);
      }

      strategies.add(strategy);
    }

    return strategies;
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
  public Strategy copyStrategy_old(Strategy strategy, Map<Long, Long> stepIdMap) throws WdkModelException,
      WdkUserException {
    String name = strategy.getName();
    if (!name.toLowerCase().endsWith(", copy of"))
      name += ", Copy of";
    return copyStrategy(strategy.getUser(), strategy, stepIdMap, name);
  }

  /**
   * 
   * @param user
   * @param oldStrategy
   * @param stepIdsMap An output map of old to new step IDs. Steps recursively encountered in the copy are added by the copy
   * @param baseName The name to use as a basis for the new name.  If the user does not already have this name, 
   * then use it.  Otherwise, add a numeric suffix to it.  If it already has a suffix, increment it
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Strategy copyStrategy(User user, Strategy oldStrategy, Map<Long, Long> stepIdsMap, String baseName)
      throws WdkModelException, WdkUserException {

    // get a new strategy id
    long newStrategyId = getNewStrategyId();

    Step latestStep = copyStepTree(user, newStrategyId, oldStrategy.getLatestStep(), stepIdsMap);

    String name = addSuffixToStratNameIfNeeded(user, baseName, false);

    return createStrategy(user, newStrategyId, latestStep, name, null, false, oldStrategy.getDescription(),
        false, false);
  }

  public Step copyStepTree(User newUser, long newStrategyId, Step oldStep, Map<Long, Long> stepIdsMap)
      throws WdkModelException {

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
    newStep.setValid(oldStep.isValid());
    
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

  Strategy loadStrategy(User user, long strategyId, boolean allowDeleted) throws WdkModelException,
      WdkUserException {
    PreparedStatement psStrategy = null;
    ResultSet rsStrategy = null;
    try {
      String sql = basicStratsSql + byStratIdCondition;
      if (!allowDeleted) {
        sql += isNotDeletedCondition;
      }
      long start = System.currentTimeMillis();
      psStrategy = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psStrategy.setLong(1, strategyId);
      rsStrategy = psStrategy.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-load-strategy-by-id", start);
      List<Strategy> strategies = loadStrategies(user, rsStrategy);

      if (strategies.size() == 0) {
        throw new WdkUserException("The strategy " + strategyId + " does not exist " + "for user " +
            (user == null ? "null" : user.getEmail()));
      }
      else if (strategies.size() > 1) {
        throw new WdkModelException("More than one strategy of id " + strategyId + " exists.");
      }

      Strategy strategy = strategies.get(0);
      // Set saved name, if any
      /*
       * if (!strategy.getName().matches("^New Strategy(\\([\\d]+\\))?\\*$")) { //
       * System.out.println("Name does not match: " + // strategy.getName()); // Remove any (and everything
       * after it) from name, set as // saved name strategy.setSavedName(strategy.getName().replaceAll(
       * "(\\([\\d]+\\))?\\*$", "")); }
       */
      return strategy;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to load strategies for user " + user.getEmail(), e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsStrategy, psStrategy);
    }
  }

  public Strategy loadStrategy(String strategySignature) throws WdkModelException, WdkUserException {
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(_userDbDs, stratBySignatureSql);
      ps.setString(1, _wdkModel.getProjectId());
      ps.setString(2, strategySignature);
      resultSet = ps.executeQuery();
      QueryLogger.logEndStatementExecution(stratBySignatureSql,
          "wdk-step-factory-load-strategy-by-signature", start);
      List<Strategy> strategies = loadStrategies(null, resultSet);
      if (strategies.size() == 0) {
        throw new WdkUserException("The strategy of signature " + strategySignature + " doesn't exist.");
      }
      else if (strategies.size() > 1) {
        throw new WdkModelException("More than one strategy of signature " + strategySignature + " exists.");
      }
      return strategies.get(0);
    }
    catch (SQLException e) {
      throw new WdkModelException("Cannot load strategy with signature " + strategySignature, e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }
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
      return _userDb.getPlatform().getNextId(_userDbDs, _userSchema, TABLE_STRATEGY);
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
      String description, boolean hidden, boolean isPublic) throws WdkModelException, WdkUserException {
    long strategyId = (root.getStrategyId() == null) ? getNewStrategyId() : root.getStrategyId();
    return createStrategy(user, strategyId, root, name, savedName, saved, description, hidden, isPublic);
  }

  Strategy createStrategy(User user, long strategyId, Step root, String newName, String savedName, boolean saved,
      String description, boolean hidden, boolean isPublic) throws WdkModelException, WdkUserException {
    LOG.debug("creating strategy, saved=" + saved);

    long userId = user.getUserId();

    String userIdColumn = Utilities.COLUMN_USER_ID;
    PreparedStatement psCheckName = null;
    ResultSet rsCheckName = null;

    String sql = "SELECT " + COLUMN_STRATEGY_ID + " FROM " + _userSchema + TABLE_STRATEGY + " WHERE " +
        userIdColumn + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_NAME + " = ? AND " +
        COLUMN_IS_SAVED + "= ? AND " + COLUMN_IS_DELETED + "= ?";
    try {
      // If newName is not null, check if strategy exists.  if so, just load it and return.  don't create a new one.
      if (newName != null) {
        if (newName.length() > COLUMN_NAME_LIMIT) {
          newName = newName.substring(0, COLUMN_NAME_LIMIT - 1);
        }
        long start = System.currentTimeMillis();
        psCheckName = SqlUtils.getPreparedStatement(_userDbDs, sql);
        psCheckName.setLong(1, userId);
        psCheckName.setString(2, _wdkModel.getProjectId());
        psCheckName.setString(3, newName);
        psCheckName.setBoolean(4, saved);
        psCheckName.setBoolean(5, hidden);
        rsCheckName = psCheckName.executeQuery();
        QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-check-strategy-name", start);

        if (rsCheckName.next())
          return loadStrategy(user, rsCheckName.getInt(COLUMN_STRATEGY_ID), false);
      }
      
      // if newName is null, generate default name from root step (by adding/incrementing numeric suffix)
      else {
        newName = addSuffixToStratNameIfNeeded(user, root.getCustomName(), saved);
      }
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not create strategy", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsCheckName, psCheckName);
    }

    PreparedStatement psStrategy = null;
    String signature = getStrategySignature(user.getUserId(), strategyId);
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
      psStrategy.setString(5, newName);
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

    Strategy strategy = loadStrategy(user, strategyId, false);
    strategy.setLatestStep(root);
    return strategy;
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

  public int getStrategyCount(long userId) throws WdkModelException {
    try {
      String sql =
        "SELECT st.question_name" +
        " FROM " + _userSchema + TABLE_STEP + " st, " + _userSchema + TABLE_STRATEGY + " sr" +
        " WHERE sr." + Utilities.COLUMN_USER_ID + " = ?" +
        " AND sr." + COLUMN_IS_DELETED + " = " + _userDb.getPlatform().convertBoolean(false) +
        " AND sr." + COLUMN_PROJECT_ID + " = ?" +
        " AND st." + COLUMN_STEP_ID + " = sr.root_step_id";
      Wrapper<Integer> result = new Wrapper<>();
      new SQLRunner(_userDbDs, sql, "wdk-step-factory-strategy-count").executeQuery(
        new Object[]{ userId, _wdkModel.getProjectId() },
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

  private String addSuffixToStratNameIfNeeded(User user, String oldName, boolean saved) throws WdkModelException {
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

      // randomly find the first name that matches oldName (\d+).  
      // increment that numeric suffix, and continue looping until the incremented guys is not found.
      // that's our new name.
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

  public String getStrategySignature(long userId, long internalId) {
    String project_id = _wdkModel.getProjectId();
    String content = project_id + "_" + userId + "_" + internalId + "_6276406938881110742";
    return EncryptionUtil.encrypt(content, true);
  }

  void setStepValidFlag(Step step) throws WdkModelException {
    String sql = "UPDATE " + _userSchema + TABLE_STEP + " SET " + COLUMN_IS_VALID + " = ? WHERE " +
        COLUMN_STEP_ID + " = ?";
    PreparedStatement psUpdate = null;
    try {
      long start = System.currentTimeMillis();
      psUpdate = SqlUtils.getPreparedStatement(_userDbDs, sql);
      psUpdate.setBoolean(1, step.isValid());
      psUpdate.setLong(2, step.getStepId());
      psUpdate.executeUpdate();
      QueryLogger.logEndStatementExecution(sql, "wdk-step-factory-update-strategy-signature", start);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to set valid step flag on step " + step.getStepId(), e);
    }
    finally {
      SqlUtils.closeStatement(psUpdate);
    }
  }

  public void verifySameOwnerAndProject(Step step1, Step step2) throws WdkModelException {
    // check that users match
    if (step1.getUser().getUserId() != step2.getUser().getUserId()) {
      throw new WdkIllegalArgumentException(getVerificationPrefix() +
          "Cannot align two steps with different " + "owners.  Existing step " + step1.getStepId() +
          " has owner " + step1.getUser().getUserId() + " (" + step1.getUser().getEmail() +
          ")\n  Call made to align the following step (see stack below for " +
          "how):\n  Newly aligned step " + step2.getStepId() + " has owner " + step2.getUser().getUserId() +
          " (" + step2.getUser().getEmail() + ")");
    }

    // check that projects both match current project
    String projectId = _wdkModel.getProjectId();
    if (!step1.getProjectId().equals(projectId) || !step2.getProjectId().equals(projectId)) {
      throw new WdkIllegalArgumentException(getVerificationPrefix() +
          "Cannot align two steps with different " +
          "projects.  Project IDs don't match during alignment of two " +
          "steps!!\n  Currently loaded model has project " + projectId + ".\n  Existing step " +
          step1.getStepId() + " has project " + step1.getProjectId() +
          "\n  Call made to align the following " + "step (see stack below for how):\n  Newly aligned step " +
          step2.getStepId() + " has project " + step2.getProjectId());
    }
  }

  public void verifySameOwnerAndProject(Step step1, long step2Id) throws WdkModelException {
    // some logic sets 0 for step IDs; this is valid but not eligible for this check
    if (step2Id == 0)
      return;
    Step step2;
    try {
      step2 = getStepById(step2Id);
    }
    catch (Exception e) {
      throw new WdkIllegalArgumentException(getVerificationPrefix() + "Unable to load step with ID " +
          step2Id + " to compare owners with another step.", e);
    }
    verifySameOwnerAndProject(step1, step2);
  }

  public void verifySameOwnerAndProject(Strategy strategy, Step step) throws WdkModelException {
    // check that users match
    if (strategy.getUser().getUserId() != step.getUser().getUserId()) {
      throw new WdkIllegalArgumentException(getVerificationPrefix() +
          "Cannot assign a root step to a strategy unless they have the same" + "owner.  Existing strategy " +
          strategy.getStrategyId() + " has" + "owner " + strategy.getUser().getUserId() + " (" +
          strategy.getUser().getEmail() + ")\n  Call made to assign the " +
          "following root step (see stack below for how):\n  Newly assigned" + "step " + step.getStepId() +
          " has owner " + step.getUser().getUserId() + " (" + step.getUser().getEmail() + ")");
    }

    // check that projects both match current project
    String projectId = _wdkModel.getProjectId();
    if (!strategy.getProjectId().equals(projectId) || !step.getProjectId().equals(projectId)) {
      throw new WdkIllegalArgumentException(getVerificationPrefix() +
          "Cannot assign a root step to a strategy " +
          "unless they have the same project.  Project IDs don't match " +
          "during assignment of root step to strategy!!\n  Currently loaded " + "model has project " +
          projectId + ".\n  Root step to be assigned (" + step.getStepId() + ") has project " +
          step.getProjectId() + ".\n  " + "Strategy being assigned step (" + strategy.getStrategyId() +
          ") has project " + strategy.getProjectId());
    }
  }

  private String getVerificationPrefix() {
    return "[IP " + MDCUtil.getIpAddress() + " requested page from " + MDCUtil.getRequestedDomain() + "] ";
  }

  /**
   * This method will reset the estimate size of the step and all other steps that depends on it.
   * 
   * @param fromStep
   * @return
   * @throws WdkModelException
   */
  int resetStepCounts(Step fromStep) throws WdkModelException {
    DBPlatform platform = _userDb.getPlatform();
    String selectSql = selectStepAndParents(fromStep.getStepId());
    String sql = "UPDATE " + _userSchema + "steps SET estimate_size = " + UNKNOWN_SIZE +
        ", is_valid = " + platform.convertBoolean(true) + " WHERE step_id IN (" + selectSql + ")";
    try {
      return SqlUtils.executeUpdate(_userDb.getDataSource(), sql, "wdk-step-reset-count-recursive");
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
  public String selectStepAndParents(long stepId) throws WdkModelException {
    DBPlatform platform = _userDb.getPlatform();
    String sql;
    String stepTable = _userSchema + "steps";
    if (platform instanceof Oracle) {
      sql = "SELECT step_id FROM " + stepTable + " START WITH step_id = " + stepId +
          "  CONNECT BY (PRIOR step_id = left_child_id OR PRIOR step_id = right_child_id)";
    }
    else if (platform instanceof PostgreSQL) {
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
      throw new WdkModelException("Unsupported platform type: " + platform.getClass().getName());
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
  public String selectStepAndChildren(long stepId) throws WdkModelException {
    DBPlatform platform = _userDb.getPlatform();
    String sql;
    String stepTable = _userSchema + "steps";
    if (platform instanceof Oracle) {
      sql = "SELECT step_id FROM " + stepTable + " START WITH step_id = " + stepId +
          "  CONNECT BY (step_id = PRIOR left_child_id OR step_id = PRIOR right_child_id)";
    }
    else if (platform instanceof PostgreSQL) {
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
      throw new WdkModelException("Unsupported platform type: " + platform.getClass().getName());
    }
    return sql;
  }

  public List<Long> getStepAndParents(final long stepId) throws WdkModelException {
    final List<Long> ids = new ArrayList<>();
    new SQLRunner(_userDb.getDataSource(), selectStepAndParents(stepId), "select-step-and-parent-ids")
        .executeQuery(new ResultSetHandler() {
      @Override
      public void handleResult(ResultSet rs) throws SQLException {
        while (rs.next()) {
          ids.add(rs.getLong(1));
        }
      }
    });
    return ids;
  }

  private void applyAlwaysOnFiltersToExistingStep(Step step) throws WdkModelException {
    if (!step.hasValidQuestion()) {
      // if not valid, user can only delete step so don't apply filters
      return;
    }

    // create a copy of the step representing the pre-modified version
    Step unmodifiedVersion = new Step(step);
    unmodifiedVersion.setFilterOptions(new FilterOptionList(step.getFilterOptions()));
    unmodifiedVersion.setViewFilterOptions(new FilterOptionList(step.getViewFilterOptions()));

    boolean modified = false;
    Set<String> appliedFilterKeys = step.getFilterOptions().getFilterOptions().keySet();
    Set<String> appliedViewFilterKeys = step.getViewFilterOptions().getFilterOptions().keySet();
    Question question = step.getQuestion();
    @SuppressWarnings("unchecked") // cannot create array of specific map
    Map<String, Filter>[] filterMaps = new Map[] { question.getFilters(), question.getViewFilters() };
    for (Map<String, Filter> filterMap : filterMaps) {
      for (Filter filter : filterMap.values()) {
        if (!filter.getIsAlwaysApplied()) {
          // only need to apply always-applied filters
          continue;
        }
        if ((filter.getIsViewOnly() && !appliedViewFilterKeys.contains(filter.getKey())) ||
            (!filter.getIsViewOnly() && !appliedFilterKeys.contains(filter.getKey()))) {
          // RRD - decided to NOT automatically apply always-on filter values but to throw exception instead
          //throw new WdkModelException("Always-on filter '" + filter.getKey() + "' not found on step " + step.getStepId());
          // always-on filter is not yet on; rectify the situation
          modified = addFilterDefault(step, filter) || modified;
        }
      }
    }
    if (modified) {
      step.saveParamFilters(unmodifiedVersion);
    }
  }

  // we need to add/pass the disabled property
  private void applyAlwaysOnFiltersToNewStep(Step step) throws WdkModelException {
    for (Filter filter : step.getQuestion().getFilters().values()) {
      if (filter.getIsAlwaysApplied()) {
        LOG.debug("Adding filter '" + filter.getKey() + "' default value to step.");
        addFilterDefault(step, filter);
      }
    }
  }

  private boolean addFilterDefault(Step step, Filter filter) throws WdkModelException {
    JSONObject defaultValue = filter.getDefaultValue(step);
    if (defaultValue != null) {
      if (filter.getIsViewOnly()) {
        step.addViewFilterOption(filter.getKey(), defaultValue);
      }
      else {
        step.addFilterOption(filter.getKey(), defaultValue, false);
      }
      return true;
    }
    return false;
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

  public void patchAnswerParams(Step step) throws WdkModelException {
    Param[] params = step.getQuestion().getParams();
    boolean leftParamEmpty = true;
    for(Param param : params) {
      if(param instanceof AnswerParam) {
        if(leftParamEmpty) {
          step.setParamValue(param.getName(), Long.toString(step.getPreviousStepId()));
          leftParamEmpty = false;
        }
        else {
          step.setParamValue(param.getName(), Long.toString(step.getChildStepId()));
        }
      }
    }
    step.saveParamFilters();
  }
}
