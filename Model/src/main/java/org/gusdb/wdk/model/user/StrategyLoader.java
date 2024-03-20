package org.gusdb.wdk.model.user;

import static java.util.stream.Collectors.toList;
import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.db.SqlUtils.fetchNullableBoolean;
import static org.gusdb.fgputil.db.SqlUtils.fetchNullableInteger;
import static org.gusdb.fgputil.db.SqlUtils.fetchNullableLong;
import static org.gusdb.fgputil.functional.Functions.getMapFromList;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_ANSWER_FILTER;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_ASSIGNED_WEIGHT;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_CREATE_TIME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_CUSTOM_NAME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_DESCRIPTION;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_DISPLAY_PARAMS;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_DISPLAY_PREFS;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_ESTIMATE_SIZE;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_EXPANDED_NAME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_IS_DELETED;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_IS_EXPANDED;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_IS_PUBLIC;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_IS_SAVED;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_LAST_MODIFIED_TIME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_LAST_RUN_TIME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_LAST_VIEWED_TIME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_NAME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_PROJECT_ID;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_PROJECT_VERSION;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_QUESTION_NAME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_ROOT_STEP_ID;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_SAVED_NAME;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_SIGNATURE;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_STEP_ID;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_STRATEGY_ID;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_USER_ID;
import static org.gusdb.wdk.model.user.StepFactory.COLUMN_VERSION;
import static org.gusdb.wdk.model.user.StepFactory.STEP_TABLE_COLUMNS;
import static org.gusdb.wdk.model.user.StepFactory.STRATEGY_TABLE_COLUMNS;
import static org.gusdb.wdk.model.user.StepFactory.TABLE_STEP;
import static org.gusdb.wdk.model.user.StepFactory.TABLE_STRATEGY;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.Strategy.StrategyBuilder;
import org.json.JSONObject;

public class StrategyLoader {

  private static final Logger LOG = Logger.getLogger(StrategyLoader.class);

  private static final String STEP_COLUMNS = Arrays.stream(STEP_TABLE_COLUMNS)
      .map(col -> "st." + col)
      .collect(Collectors.joining(", "));

  private static final List<String> STRAT_COLUMNS = Arrays.stream(STRATEGY_TABLE_COLUMNS)
      .filter(col -> !COLUMN_STRATEGY_ID.equals(col))
      .collect(toList());

  private static final String VALUED_STRAT_COLUMNS = mappedColumnSelection(STRAT_COLUMNS, col -> "sr." + col + " as " + toStratCol(col));
  private static final String NULLED_STRAT_COLUMNS = mappedColumnSelection(STRAT_COLUMNS, col -> "NULL as " + toStratCol(col));

  // macros to fill in searches
  private static final String USER_SCHEMA_MACRO = "$$USER_SCHEMA$$";
  private static final String PROJECT_ID_MACRO = "$$PROJECT_ID$$";
  private static final String IS_DELETED_VALUE_MACRO = "$$IS_DELETED_BOOLEAN_VALUE$$";
  private static final String SEARCH_CONDITIONS_MACRO = "$$SEARCH_CONDITIONS$$";

  // to find steps
  private static final String FIND_STEPS_SQL =
      "(" +
      "  select " + STEP_COLUMNS + ", " + VALUED_STRAT_COLUMNS +
      "  from " + USER_SCHEMA_MACRO + TABLE_STRATEGY + " sr," +
      "       " + USER_SCHEMA_MACRO + TABLE_STEP + " st" +
      "  where st." + COLUMN_STRATEGY_ID + " in (" +
      "    select distinct " + COLUMN_STRATEGY_ID +
      "    from " + USER_SCHEMA_MACRO + TABLE_STEP + " st" +
      "    where st." + COLUMN_STRATEGY_ID + " is not null" +
      "    " + SEARCH_CONDITIONS_MACRO +
      "  )" +
      "  and sr." + COLUMN_STRATEGY_ID + " = st." + COLUMN_STRATEGY_ID +
      "  and sr." + COLUMN_PROJECT_ID + " = '" + PROJECT_ID_MACRO + "'" +
      "  and sr." + COLUMN_IS_DELETED + " = " + IS_DELETED_VALUE_MACRO +
      "  and st." + COLUMN_IS_DELETED + " = " + IS_DELETED_VALUE_MACRO +
      ")" +
      " union all" +
      "(" +
      "  select " + STEP_COLUMNS + ", " + NULLED_STRAT_COLUMNS +
      "  from " + USER_SCHEMA_MACRO + TABLE_STEP + " st" +
      "  where st." + COLUMN_STRATEGY_ID + " is null" +
      "  and st." + COLUMN_PROJECT_ID + " = '" + PROJECT_ID_MACRO + "'" +
      "  and st." + COLUMN_IS_DELETED + " = " + IS_DELETED_VALUE_MACRO +
      "  " + SEARCH_CONDITIONS_MACRO +
      ")" +
      " order by " + COLUMN_STRATEGY_ID;

  // to find strategies
  private static final String FIND_STRATEGIES_SQL =
      "select " + STEP_COLUMNS + ", " + VALUED_STRAT_COLUMNS +
      "  from " + USER_SCHEMA_MACRO + TABLE_STRATEGY + " sr," +
      "       " + USER_SCHEMA_MACRO + TABLE_STEP + " st" +
      "  where st." + COLUMN_STRATEGY_ID + " = sr." + COLUMN_STRATEGY_ID +
      "    and sr." + COLUMN_PROJECT_ID + " = '" + PROJECT_ID_MACRO + "'" +
      "    and sr." + COLUMN_IS_DELETED + " = " + IS_DELETED_VALUE_MACRO +
      "    and st." + COLUMN_IS_DELETED + " = " + IS_DELETED_VALUE_MACRO +
      "    " + SEARCH_CONDITIONS_MACRO +
      "  order by " + COLUMN_STRATEGY_ID;

  private static final Comparator<? super Step> STEP_COMPARATOR_LAST_RUN_TIME_DESC =
      (s1, s2) -> s2.getLastRunTime().compareTo(s1.getLastRunTime());
  private static final Comparator<? super Strategy> STRATEGY_COMPARATOR_LAST_MOD_TIME_DESC =
      (s1, s2) -> s2.getLastModifiedTime().compareTo(s1.getLastModifiedTime());

  private final WdkModel _wdkModel;
  private final DataSource _userDbDs;
  private final DBPlatform _userDbPlatform;
  private final String _userSchema;
  private final UserFactory _userFactory;
  private final ValidationLevel _validationLevel;
  private final FillStrategy _fillStrategy;

  public StrategyLoader(WdkModel wdkModel, ValidationLevel validationLevel, FillStrategy fillStrategy) {
    _wdkModel = wdkModel;
    _userDbDs = wdkModel.getUserDb().getDataSource();
    _userDbPlatform = wdkModel.getUserDb().getPlatform();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    _userFactory = wdkModel.getUserFactory();
    _validationLevel = validationLevel;
    _fillStrategy = fillStrategy;
  }

  private String sqlBoolean(boolean boolValue) {
    return _userDbPlatform.convertBoolean(boolValue).toString();
  }

  private String prepareSql(String sql) {
    return sql
        .replace(USER_SCHEMA_MACRO, _userSchema)
        .replace(PROJECT_ID_MACRO, _wdkModel.getProjectId())
        .replace(IS_DELETED_VALUE_MACRO, sqlBoolean(false));
  }

  private SearchResult doSearch(String sql) throws WdkModelException {
    return doSearch(sql, true, new Object[0], new Integer[0]);
  }

  private SearchResult doSearch(String sql, boolean propagateBuildErrors) throws WdkModelException {
    return doSearch(sql, propagateBuildErrors, new Object[0], new Integer[0]);
  }

  private SearchResult doSearch(String sql, boolean propagateBuildErrors, Object[] paramValues, Integer[] paramTypes) throws WdkModelException {
    try {
      // execute the search and create builders for results
      TwoTuple<List<StrategyBuilder>,List<StepBuilder>> queryResults =
          loadBuilders(sql, paramValues, paramTypes);

      // all data loaded; build steps and strats at the specified validation level
      List<Strategy> builtStrategies = new ArrayList<>();
      UnbuildableStrategyList<InvalidStrategyStructureException> malstructuredStrategies = new UnbuildableStrategyList<>();
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors = new UnbuildableStrategyList<>();

      // load all the users up first (can be done in a single batch) to avoid churn
      UserCache userCache = new UserCache(_userFactory);
      userCache.loadUsersByIds(queryResults.getFirst().stream().map(StrategyBuilder::getUserId).collect(Collectors.toList()));

      // try to build each strategy and put in the appropriate list
      for (StrategyBuilder stratBuilder : queryResults.getFirst()) {
        try {
          builtStrategies.add(stratBuilder.build(userCache, _validationLevel, _fillStrategy));
        }
        catch (InvalidStrategyStructureException e) {
          malstructuredStrategies.add(new TwoTuple<>(stratBuilder, e));
        }
        catch (WdkModelException e) {
          stratsWithBuildErrors.add(new TwoTuple<>(stratBuilder, e));
        }
      }
      if (!stratsWithBuildErrors.isEmpty()) {
        String buildErrorMessages = "At least one strategy could not be built" +
          " due to WdkModelException: " + NL + stratsWithBuildErrors
            .stream()
            .map(tuple ->
              "Strategy " + tuple.getFirst().getStrategyId() +
              ", owned by " + tuple.getFirst().getUserId() + NL +
              tuple.getSecond().toString() + NL +
              FormatUtil.getStackTrace(tuple.getSecond()))
            .collect(Collectors.joining(NL));
        if (propagateBuildErrors) {
          throw new WdkModelException(buildErrorMessages);
        }
        else {
          LOG.warn(buildErrorMessages);
        }
      }

      // only build orphan steps; attached steps will be built by their strategy
      List<Step> builtOrphanSteps = new ArrayList<>();
      for (StepBuilder orphanBuilder : queryResults.getSecond()) {
        try {
          builtOrphanSteps.add(orphanBuilder.build(userCache, _validationLevel, _fillStrategy, Optional.empty()));
        }
        catch (WdkModelException e) {
          if (propagateBuildErrors) throw e;
          LOG.warn("Error occurred while building orphan step. By request, ignoring.", e);
          continue;
        }
      }

      return new SearchResult(builtStrategies, builtOrphanSteps, malstructuredStrategies, stratsWithBuildErrors);
    }
    catch (Exception e) {
      LOG.error("Unable to execute search with SQL: " + NL + sql + NL + "and params [" + FormatUtil.join(paramValues, ",") + "].", e);
      return WdkModelException.unwrap(e);
    }
  }

  private TwoTuple<List<StrategyBuilder>, List<StepBuilder>> loadBuilders(
      String sql, Object[] paramValues, Integer[] paramTypes) {
    List<StrategyBuilder> strategies = new ArrayList<>();
    List<StepBuilder> orphanSteps = new ArrayList<>();
    LOG.debug("Executing strategy search SQL:\n" + sql + "\nPARAMS: " + FormatUtil.join(paramValues, ", "));
    new SQLRunner(_userDbDs, sql, "search-steps-strategies").executeQuery(paramValues, paramTypes, rs -> {
      StrategyBuilder currentStrategy = null;
      while(rs.next()) {
        // read a row
        long nextStrategyId = rs.getLong(COLUMN_STRATEGY_ID);
        if (rs.wasNull()) {
          // this step row has no strategy ID
          if (currentStrategy != null) {
            // save off current strategy and reset
            strategies.add(currentStrategy);
            currentStrategy = null;
          }
          // read orphan step and save off
          orphanSteps.add(readStep(rs));
        }
        else {
          // this step row has a strategy ID
          if (currentStrategy != null) {
            // check to see if this row part of current strategy or beginning of next one
            if (currentStrategy.getStrategyId() == nextStrategyId) {
              // part of current; add step to current strategy
              currentStrategy.addStep(readStep(rs));
            }
            else {
              // beginning of next strategy; save off current strat then read and make next strat current
              strategies.add(currentStrategy);
              currentStrategy = readStrategy(rs); // will also read/add step
            }
          }
          else {
            // no current strategy to save off; start new one with this step row
            currentStrategy = readStrategy(rs); // will also read/add step
          }
        }
      }
      // check for leftover strategy to save
      if (currentStrategy != null) {
        strategies.add(currentStrategy);
      }
      return this;
    });
    return new TwoTuple<>(strategies, orphanSteps);
  }

  private StrategyBuilder readStrategy(ResultSet rs) throws SQLException {

    long strategyId = rs.getLong(COLUMN_STRATEGY_ID);
    long userId = rs.getLong(toStratCol(COLUMN_USER_ID));

    StrategyBuilder strat = Strategy.builder(_wdkModel, userId, strategyId)
        .setProjectId(rs.getString(toStratCol(COLUMN_PROJECT_ID)))
        .setVersion(rs.getString(toStratCol(COLUMN_VERSION)))
        .setCreatedTime(rs.getTimestamp(toStratCol(COLUMN_CREATE_TIME)))
        .setDeleted(rs.getBoolean(toStratCol(COLUMN_IS_DELETED)))
        .setRootStepId(rs.getLong(toStratCol(COLUMN_ROOT_STEP_ID)))
        .setSaved(rs.getBoolean(toStratCol(COLUMN_IS_SAVED)))
        .setLastViewTime(rs.getTimestamp(toStratCol(COLUMN_LAST_VIEWED_TIME)))
        .setLastModifiedTime(rs.getTimestamp(toStratCol(COLUMN_LAST_MODIFIED_TIME)))
        .setDescription(rs.getString(toStratCol(COLUMN_DESCRIPTION)))
        .setSignature(rs.getString(toStratCol(COLUMN_SIGNATURE)))
        .setName(rs.getString(toStratCol(COLUMN_NAME)))
        .setSavedName(rs.getString(toStratCol(COLUMN_SAVED_NAME)))
        .setIsPublic(fetchNullableBoolean(rs, toStratCol(COLUMN_IS_PUBLIC), false)); // null = false (not public)

    return strat.addStep(readStep(rs));
  }

  private StepBuilder readStep(ResultSet rs) throws SQLException {

    long stepId = rs.getLong(COLUMN_STEP_ID);
    long userId = rs.getLong(COLUMN_USER_ID);
    String displayPrefs = rs.getString(COLUMN_DISPLAY_PREFS);

    return Step.builder(_wdkModel, userId, stepId)
      .setStrategyId(Optional.ofNullable(fetchNullableLong(rs, COLUMN_STRATEGY_ID, null)))
      .setProjectId(rs.getString(COLUMN_PROJECT_ID))
      .setProjectVersion(rs.getString(COLUMN_PROJECT_VERSION))
      .setCreatedTime(rs.getTimestamp(COLUMN_CREATE_TIME))
      .setLastRunTime(rs.getTimestamp(COLUMN_LAST_RUN_TIME))
      .setEstimatedSize(rs.getInt(COLUMN_ESTIMATE_SIZE))
      .setDeleted(rs.getBoolean(COLUMN_IS_DELETED))
      .setCustomName(rs.getString(COLUMN_CUSTOM_NAME))
      .setExpandedName(rs.getString(COLUMN_EXPANDED_NAME))
      .setExpanded(rs.getBoolean(COLUMN_IS_EXPANDED))
      .setAnswerSpec(
        AnswerSpec.builder(_wdkModel)
        .setQuestionFullName(rs.getString(COLUMN_QUESTION_NAME))
        .setLegacyFilterName(Optional.ofNullable(rs.getString(COLUMN_ANSWER_FILTER)))
        .setDbParamFiltersJson(
          new JSONObject(_userDbPlatform.getClobData(rs, COLUMN_DISPLAY_PARAMS)),
          fetchNullableInteger(rs, COLUMN_ASSIGNED_WEIGHT, 0)
        )
      )
      .setDisplayPrefs(displayPrefs == null ? new JSONObject() : new JSONObject(displayPrefs));
  }

  public Optional<Step> getStepById(long stepId) throws WdkModelException {
    String sql = prepareSql(FIND_STEPS_SQL
        .replace(SEARCH_CONDITIONS_MACRO, "and st." + COLUMN_STEP_ID + " = " + stepId));
    return doSearch(sql).findFirstOverallStep(st -> st.getStepId() == stepId);
  }

  public Optional<Strategy> getStrategyById(long strategyId) throws WdkModelException {
    String sql = prepareSql(FIND_STRATEGIES_SQL
        .replace(SEARCH_CONDITIONS_MACRO, "and sr." + COLUMN_STRATEGY_ID + " = " + strategyId));
    return doSearch(sql).getOnlyStrategy("with strategy ID = " + strategyId);
  }

  List<Strategy> getPublicStrategies() throws WdkModelException {
    String sql = prepareSql(FIND_STRATEGIES_SQL
        .replace(SEARCH_CONDITIONS_MACRO, "and sr." + COLUMN_IS_PUBLIC + " = " + sqlBoolean(true)));
    return descModTimeSort(doSearch(sql, false).getStrategies());
  }

  TwoTuple<
    UnbuildableStrategyList<InvalidStrategyStructureException>,
    UnbuildableStrategyList<WdkModelException>
  > getPublicStrategyErrors() throws WdkModelException {
    String sql = prepareSql(FIND_STRATEGIES_SQL
        .replace(SEARCH_CONDITIONS_MACRO, "and sr." + COLUMN_IS_PUBLIC + " = " + sqlBoolean(true)));
    SearchResult result = doSearch(sql, false);
    return new TwoTuple<>(result.getMalformedStrategies(), result.getStratsWithBuildErrors());
  }

  Map<Long, Step> getSteps(Long userId) throws WdkModelException {
    String sql = prepareSql(FIND_STEPS_SQL
        .replace(SEARCH_CONDITIONS_MACRO, "and st." + COLUMN_USER_ID + " = " + userId));
    List<Step> steps = doSearch(sql).findAllSteps(step -> true);
    // sort steps by last run time, descending
    steps.sort(STEP_COMPARATOR_LAST_RUN_TIME_DESC);
    return toStepMap(steps);
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
  List<Strategy> getStrategies(long userId, boolean saved, boolean recent)
      throws WdkModelException {
    String baseSql = prepareSql(FIND_STRATEGIES_SQL);
    String baseConditions =
        "and sr." + COLUMN_USER_ID + " = " + userId +
        " and sr." + COLUMN_IS_SAVED + " = " + _userDbPlatform.convertBoolean(saved);
    List<Strategy> strategies = (recent ?

        // search using recently viewed condition and related statement param
        doSearch(
            baseSql.replace(SEARCH_CONDITIONS_MACRO, baseConditions +
                " and sr." + COLUMN_LAST_VIEWED_TIME + " >= ?"), false,
            new Object[] { getRecentTimestamp() }, new Integer[] { Types.TIMESTAMP }) :

        // search using only user id and is-saved conditions
        doSearch(baseSql.replace(SEARCH_CONDITIONS_MACRO, baseConditions))

    ).getStrategies();

    // sort by last modified time, descending
    return descModTimeSort(strategies);
  }

  Map<Long, Strategy> getAllStrategies(
      UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrategies,
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors) throws WdkModelException {
    return getStrategies(
        prepareSql(FIND_STRATEGIES_SQL.replace(SEARCH_CONDITIONS_MACRO, "")),
        malformedStrategies, stratsWithBuildErrors);
  }

  Map<Long, Strategy> getStrategies(long userId,
      UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrategies,
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors) throws WdkModelException {
    return getStrategies(
        prepareSql(FIND_STRATEGIES_SQL.replace(SEARCH_CONDITIONS_MACRO, "and sr." + COLUMN_USER_ID + " = " + userId)),
        malformedStrategies, stratsWithBuildErrors);
  }

  private Map<Long, Strategy> getStrategies(String searchSql,
      UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrategies,
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors) throws WdkModelException {
    SearchResult result = doSearch(searchSql);
    malformedStrategies.addAll(result.getMalformedStrategies());
    stratsWithBuildErrors.addAll(result.getStratsWithBuildErrors());
    return toStrategyMap(descModTimeSort(result.getStrategies()));
  }

  Optional<Strategy> getStrategyBySignature(String strategySignature) throws WdkModelException {
    String sql = prepareSql(FIND_STRATEGIES_SQL
        .replace(SEARCH_CONDITIONS_MACRO, "and sr." + COLUMN_SIGNATURE + " = ?"));
    return doSearch(sql, true, new Object[]{ strategySignature }, new Integer[]{ Types.VARCHAR })
        .getOnlyStrategy("with strategy signature = " + strategySignature);
  }

  @SuppressWarnings("UseOfObsoleteDateTimeApi")
  private static Timestamp getRecentTimestamp() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -1);
    return new Timestamp(calendar.getTimeInMillis());
  }

  private static Map<Long,Step> toStepMap(List<Step> steps) {
    return getMapFromList(steps, step -> new TwoTuple<>(step.getStepId(), step));
  }

  private static Map<Long,Strategy> toStrategyMap(List<Strategy> strategies) {
    return getMapFromList(strategies, strat -> new TwoTuple<>(strat.getStrategyId(), strat));
  }

  // add prefix to strategy table columns since some share names with step table columns
  private static String toStratCol(String col) {
    return "strat_" + col;
  }

  private static String mappedColumnSelection(List<String> colNames, Function<String,String> mapper) {
    return join(colNames.stream().map(mapper).collect(toList()), ", ");
  }

  private static List<Strategy> descModTimeSort(List<Strategy> strategies) {
    strategies.sort(STRATEGY_COMPARATOR_LAST_MOD_TIME_DESC);
    return strategies;
  }

  public static class UnbuildableStrategyList<T extends Exception> extends
    ArrayList<TwoTuple<StrategyBuilder, T>> {}

  private static class SearchResult {

    private final List<Strategy> _strategies;
    private final List<Step> _orphanSteps;
    private final UnbuildableStrategyList<InvalidStrategyStructureException> _malformedStrategies;
    private final UnbuildableStrategyList<WdkModelException> _stratsWithBuildErrors;

    public SearchResult(
        List<Strategy> strategies,
        List<Step> orphanSteps,
        UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrategies,
        UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors) {
      _strategies = strategies;
      _orphanSteps = orphanSteps;
      _malformedStrategies = malformedStrategies;
      _stratsWithBuildErrors = stratsWithBuildErrors;
    }

    public List<Step> findAllSteps(Predicate<Step> pred) {
      ListBuilder<Step> result = new ListBuilder<>(findOrphanSteps(pred));
      for (Strategy strat : _strategies) {
        result.addIf(pred, strat.getAllSteps());
      }
      return result.toList();
    }

    public Optional<Step> findFirstOverallStep(Predicate<Step> pred) {
      List<Step> found = findAllSteps(pred);
      return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public List<Step> findOrphanSteps(Predicate<Step> pred) {
      return _orphanSteps.stream().filter(pred).collect(toList());
    }

    public UnbuildableStrategyList<InvalidStrategyStructureException> getMalformedStrategies() {
      return _malformedStrategies;
    }

    public UnbuildableStrategyList<WdkModelException> getStratsWithBuildErrors() {
      return _stratsWithBuildErrors;
    }

    public List<Strategy> getStrategies() {
      return findStrategies(strategy -> true);
    }

    public List<Strategy> findStrategies(Predicate<Strategy> pred) {
      return _strategies.stream().filter(pred).collect(toList());
    }

    public Optional<Strategy> getOnlyStrategy(String conditionMessage) throws WdkModelException {
      switch (_strategies.size()) {
        case 0: return Optional.empty();
        case 1: return Optional.of(_strategies.get(0));
        default: throw new WdkModelException("Found >1 strategy " + conditionMessage);
      }
    }

    @Override
    public String toString() {
      return new JSONObject()
          .put("orphanSteps", _orphanSteps.stream()
              .map(Step::getStepId)
              .collect(Collectors.toList()))
          .put("strategies", _strategies.stream()
              .map(strat -> new JSONObject()
                  .put("id", strat.getStrategyId())
                  .put("stepIds", strat.getAllSteps().stream()
                      .map(Step::getStepId)
                      .collect(Collectors.toList())))
              .collect(Collectors.toList()))
          .put("malformedStrategies", _malformedStrategies.stream()
              .map(tup -> new JSONObject()
                  .put("id", tup.getFirst().getStrategyId())
                  .put("problem", tup.getSecond().getMessage()))
              .collect(Collectors.toList()))
          .put("stratsWithBuildErrors", _stratsWithBuildErrors.stream()
              .map(tup -> new JSONObject()
                  .put("id", tup.getFirst().getStrategyId())
                  .put("problem", tup.getSecond().getMessage()))
              .collect(Collectors.toList()))
          .toString(2);
    }
  }
}
