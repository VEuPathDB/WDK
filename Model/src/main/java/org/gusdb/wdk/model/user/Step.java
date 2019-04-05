package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.functional.Functions.getNthOrNull;
import static org.gusdb.wdk.model.user.StepContainer.parentOf;
import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.ValidationUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.ParamsAndFiltersDbColumnFormat;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Charles Treatman
 * @author Ryan Doherty
 */
public class Step implements Validateable<Step> {

  public static final int RESET_SIZE_FLAG = -1;

  public static final int NAME_COLUMN_MAX_SIZE = 200; // bytes

  public static class StepBuilder {

    private final WdkModel _wdkModel;
    private long _userId;
    private long _stepId;
    private String _projectId;
    private String _projectVersion;
    private Optional<Long> _strategyId = Optional.empty();
    private Date _createdTime = new Date();
    private Date _lastRunTime = new Date();
    private String _customName = null;
    private boolean _isDeleted = false;
    private int _estimatedSize = -1;
    private boolean _isCollapsible = false;
    private String _collapsedName = null;
    private AnswerSpecBuilder _answerSpec; // cannot be null; must be set
    private boolean _inMemoryOnly = false;
    private boolean _isResultSizeDirty = false;
    private JSONObject _displayPrefs;

    private StepBuilder(WdkModel wdkModel, long userId, long stepId) {
      _wdkModel = wdkModel;
      _projectId = wdkModel.getProjectId();
      _projectVersion = wdkModel.getVersion();
      _userId = userId;
      _stepId = stepId;
      _displayPrefs = new JSONObject();
    }

    /**
     * Constructor that takes an existing step
     *
     * @param step step to make a shallow copy of
     */
    private StepBuilder(Step step) {
      _wdkModel = step._wdkModel;
      _userId = step._user.getUserId();
      _strategyId = step.getStrategyId();
      _stepId = step._stepId;
      _createdTime = step._createdTime;
      _lastRunTime = step._lastRunTime;
      _customName = step._customName;
      _isDeleted = step._isDeleted;
      _isCollapsible = step._isCollapsible;
      _collapsedName = step._collapsedName;
      _projectId = step._projectId;
      _projectVersion = step._projectVersion;
      _estimatedSize = step._estimatedSize;
      _inMemoryOnly = step._inMemoryOnly;
      _answerSpec = AnswerSpec.builder(step._answerSpec);
      _displayPrefs = new JSONObject(step.getDisplayPrefs().toString());
    }

    public long getStepId() {
      return _stepId;
    }

    public StepBuilder setStepId(long stepId) {
      _stepId = stepId;
      return this;
    }

    public StepBuilder setStrategyId(Optional<Long> strategyId) {
      _strategyId = strategyId;
      return this;
    }

    public StepBuilder setUserId(long userId) {
      _userId = userId;
      return this;
    }

    public StepBuilder setProjectId(String projectId) {
      _projectId = projectId;
      return this;
    }

    public StepBuilder setProjectVersion(String projectVersion) {
      _projectVersion = projectVersion;
      return this;
    }

    public StepBuilder setCreatedTime(Date createdTime) {
      _createdTime = createdTime;
      return this;
    }

    public StepBuilder setEstimatedSize(int estimatedSize) {
      _estimatedSize = estimatedSize;
      return this;
    }

    public StepBuilder setLastRunTime(Date lastRunTime) {
      _lastRunTime = lastRunTime;
      return this;
    }

    public StepBuilder setInMemoryOnly(boolean inMemoryOnly) {
      _inMemoryOnly = inMemoryOnly;
      return this;
    }

    public StepBuilder setDeleted(boolean isDeleted) {
      _isDeleted = isDeleted;
      return this;
    }

    public StepBuilder setCustomName(String customName) {
      _customName = customName;
      return this;
    }

    public StepBuilder setCollapsedName(String collapsedName) {
      _collapsedName = collapsedName;
      return this;
    }

    public StepBuilder setCollapsible(boolean isCollapsible) {
      _isCollapsible = isCollapsible;
      return this;
    }

    public StepBuilder setAnswerSpec(AnswerSpecBuilder answerSpec) {
      _answerSpec = answerSpec;
      return this;
    }

    public AnswerSpecBuilder getAnswerSpec() {
      return _answerSpec;
    }

    public StepBuilder setResultSizeDirty(boolean isResultSizeDirty) {
      _isResultSizeDirty = isResultSizeDirty;
      return this;
    }

    public StepBuilder setDisplayPrefs(JSONObject prefs) {
      _displayPrefs = Objects.requireNonNull(prefs);
      return this;
    }

    public JSONObject getDisplayPrefs() {
      return _displayPrefs;
    }

    public Step build(UserCache userCache, ValidationLevel validationLevel, Optional<Strategy> strategy) throws WdkModelException {
      if (_strategyId.isPresent() &&
          (!strategy.isPresent() || !_strategyId.get().equals(strategy.get().getStrategyId()))) {
        throw new WdkRuntimeException("Strategy passed to build method (ID=" +
            strategy.map(Strategy::getStrategyId).orElse(null) +
            ") does not match strategy ID set on step builder (ID=" + _strategyId.get() + ").");
      }
      if (_answerSpec == null) {
        throw new WdkRuntimeException("Cannot build a step without an answer spec.");
      }
      return new Step(userCache.get(_userId), strategy, this, validationLevel);
    }

    /**
     * Builds a runnable step.  Will throw ValidObjectWrappingException if step is not runnable after validation
     *
     * @param userCache a user cache
     * @param strategy strategy containing the step
     * @return a runnable step
     * @throws WdkModelException if unable to validate step
     */
    public RunnableObj<Step> buildRunnable(UserCache userCache, Optional<Strategy> strategy) throws WdkModelException {
      return ValidObjectFactory.getRunnable(build(userCache, ValidationLevel.RUNNABLE, strategy));
    }

    public long getUserId() {
      return _userId;
    }

    public String getProjectId() {
      return _projectId;
    }

    public Optional<Long> getStrategyId() {
      return _strategyId;
    }

    public String getParamValue(String paramName) {
      return _answerSpec.getParamValue(paramName);
    }

    public StepBuilder removeStrategy() {
      _strategyId = Optional.empty();
      _answerSpec.nullifyAnswerParams();
      return this;
    }

    public boolean isResultSizeDirty() {
      return _isResultSizeDirty;
    }

    @Override
    public String toString() {
      return JsonUtil.serialize(new JSONObject()
          .put("id", _stepId)
          .put("question", _answerSpec.getQuestionName()));
    }
  }

  public static StepBuilder builder(WdkModel wdkModel, long userId, long stepId) {
    return new StepBuilder(wdkModel, userId, stepId);
  }

  public static StepBuilder builder(Step step) {
    return new StepBuilder(step);
  }

  public static RunnableObj<AnswerSpec> getRunnableAnswerSpec(RunnableObj<Step> runnableStep) {
    // we know we can simply return the answer spec because the step is runnable
    return runnableStep.get().getAnswerSpec().getRunnable().getLeft();
  }

  // set during Step object creation
  private final WdkModel _wdkModel;
  // set during build() from ID in DB
  private final User _user;
  // set during build() from ID in DB (may be empty if orphan step)
  private final Optional<Strategy> _strategy;
  // in DB, Primary key
  private final long _stepId;
  // in DB, set during step creation
  private final Date _createdTime;
  // in DB, last time Answer generated, written to DB each time
  private final Date _lastRunTime;
  // in DB, set by user
  private final String _customName;
  // in DB, for soft delete
  private final boolean _isDeleted;
  // in DB, tells if nested step
  private final boolean _isCollapsible;
  // in DB, custom name for nested "strategy"
  private final String _collapsedName;
  // in DB, project ID when step was created
  private final String _projectId;
  // in DB, project version when step was created
  private final String _projectVersion;
  // in DB, defines the parameters used to find this Step's answer
  private final AnswerSpec _answerSpec;

  // in DB, last known size of result (see _estimateSizeRefreshed below)
  private int _estimatedSize;

  // Steps within the "main branch" strategy flow
  // Next Step must be combined step (e.g. transform or boolean/span-logic (two-answer function))
  // private Step _nextStep = null;
  // Previous step could be first step (=leaf), or combined step
  // private Step _previousStep = null;

  // Child can be a "normal" leaf, or collapsible version of leaf, boolean, or transform
  // private Step _childStep = null;

  /**
   * First set when step was created and answer generated, size stored in DB. So can use this to show step
   * size without pulling records from cache. Size == -1 means must rerun step (and recache results), get size
   * and store in step table. Can be out of date for releases since we don't rerun strategies on release.
   * Should be reset on step table every time we rerun step. EstimateSize is set to -1 when step is revised
   * (in place)- also all steps affected by this step are also changed to -1 so the values are set when those
   * steps are rerun (i.e. value of -1 means step is "dirty" (modified but not run))
   */
  private boolean _estimatedSizeRefreshed = false;

  // Set this if this step should not be written to /read from db. A hack in support of
  // summary views, until they are refactored using service.
  private final boolean _inMemoryOnly;

  // Set if answer spec was modified from the version stored in the database;
  // like most other attributes it is immutable and serves only to inform its
  // strategy that downstream steps' estimatedSize must be reset to -1 since
  // they are out of date.
  private final boolean _isResultSizeDirty;

  private final JSONObject _displayPrefs;

  /**
   * Creates a step object for given user and step ID. Note that this
   * constructor lazy-loads the User object for the passed ID if one is required
   * for processing after construction.
   *
   * @param user            Owner of this step
   * @param strategy        Strategy this step belongs to
   * @param builder         Step builder containing this step's property values
   * @param validationLevel level to which this step should be validated
   *
   * @throws WdkModelException if this step does not pass the given validation
   * level
   */
  private Step(User user, Optional<Strategy> strategy, StepBuilder builder, ValidationLevel validationLevel) throws WdkModelException {
    _user = user;
    _strategy = strategy;
    _wdkModel = builder._wdkModel;
    _stepId = builder._stepId;
    _createdTime = builder._createdTime;
    _lastRunTime = builder._lastRunTime;
    _customName = checkName("customName", builder._customName);
    _isDeleted = builder._isDeleted;
    _isCollapsible = builder._isCollapsible;
    _collapsedName = checkName("collapsedName", builder._collapsedName);
    _projectId = builder._projectId;
    _projectVersion = builder._projectVersion;
    _estimatedSize = builder._estimatedSize;
    _inMemoryOnly = builder._inMemoryOnly;
    _answerSpec = builder._answerSpec.build(user, getContainer(), validationLevel);
    _displayPrefs = new JSONObject(builder._displayPrefs.toString());

    // set estimated size appropriately if this step set dirty
    _isResultSizeDirty = builder._isResultSizeDirty;
    if (_isResultSizeDirty) {
      _estimatedSize = RESET_SIZE_FLAG;
    }

    // sanity checks regarding answer param values vs strategy present
    for (String answerParamName : getAnswerParamNames()) {
      String paramValue = _answerSpec.getQueryInstanceSpec().get(answerParamName);
      // Confirm left and right child null if strategyId = null
      if (!_strategy.isPresent() && !AnswerParam.NULL_VALUE.equals(paramValue)) {
        throw new WdkModelException("Step " + _stepId + " does not have a strategy but answer param " + answerParamName + " has a value.");
      }
      if (_strategy.isPresent() && AnswerParam.NULL_VALUE.equals(paramValue)) {
        throw new WdkModelException("Step " + _stepId + " is part of a strategy but answer param " + answerParamName + " does not have a value.");
      }
    }
  }

  private List<String> getAnswerParamNames() {
    return !_answerSpec.hasValidQuestion() ? Collections.emptyList() :
      _answerSpec.getQuestion().getQuery().getAnswerParams().stream()
        .map(NamedObject::getName).collect(Collectors.toList());
  }

  private static String checkName(String field, String val) throws WdkModelException {
    return val == null ? null :
      ValidationUtil.maxLength(val, NAME_COLUMN_MAX_SIZE, () ->
          new WdkModelException(String.format("Step field '%s' cannot be longer" +
              " than %d", field, NAME_COLUMN_MAX_SIZE)));
  }

  public Optional<Step> getParentStep() {
    return getContainer().findFirstStep(parentOf(_stepId));
  }

  /**
   * @return optional containing primary input step.  If this step's question
   * does not have a primary input or if the step is not part of a strategy
   * (i.e. answer params have null value), an empty optional is returned.
   */
  public Optional<Step> getPrimaryInputStep() {
    return findAnswerParamsStep(0);
  }

  public Optional<Step> getSecondaryInputStep() {
    return findAnswerParamsStep(1);
  }

  /**
   * Finds this step's question's answer param at the passed ordinal (0 or 1
   * since a maximum of 2 answer params are supported), then finds that param's
   * value in the answer spec and asks this step's step container (typically a
   * strategy) to find that step by ID.  Returns null if step cannot be found.
   *
   * @param answerParamOrdinal index of the answer param whose value should be
   *                           used to look up step
   *
   * @return the found step, or null if not found
   */
  private Optional<Step> findAnswerParamsStep(int answerParamOrdinal) {

    Optional<AnswerParam> answerParam = findAnswerParam(answerParamOrdinal);
    if (!answerParam.isPresent()) {
      return Optional.empty();
    }

    QueryInstanceSpec spec = _answerSpec.getQueryInstanceSpec();
    String stableValue = spec.get(answerParam.get().getName());

    if (AnswerParam.NULL_VALUE.equals(stableValue)) {
      return Optional.empty();
    }

    Optional<Step> foundStep = getContainer().findFirstStep(withId(AnswerParam.toStepId(stableValue)));
    if (!foundStep.isPresent()) {
      throw new WdkRuntimeException("Value of AnswerParam " + answerParam.get().getName() +
          " in step " + getStepId() + " is " + stableValue + ", which does not" +
          " refer to a step in this step's strategy (id=" + getStrategyId() + ").");
    }
    return foundStep;
  }

  private Optional<AnswerParam> findAnswerParam(int answerParamOrdinal) {
    if (!hasValidQuestion()) {
      return Optional.empty();
    }
    return Optional.ofNullable(getNthOrNull(
        _answerSpec.getQuestion().getQuery().getAnswerParams(), answerParamOrdinal));
  }

  /**
   * Returns an estimate of the size of this step (number of records returned).
   * This may be the value of the estimate_size column in the steps table, or
   * if getResultSize() has been called, a refreshed value.  Returns 0 if this
   * step has been found to be invalid.
   *
   * @return estimate of this step's result size
   */
  public int getEstimatedSize() {
    return _answerSpec.isValid() ? _estimatedSize : 0;
  }

  /**
   * Returns the real result size of this step (number of records returned); once
   * this method is called, getEstimateSize() will also return this value, and
   * the estimate_size column in the database will also be updated.
   *
   * @return the real result size gained by running the step
   */
  public int getResultSize() throws WdkModelException {
    return _estimatedSizeRefreshed ? _estimatedSize :
           !_answerSpec.isRunnable() ? 0 :
           recalculateResultSize(_answerSpec.getRunnable().getLeft());
  }

  private int recalculateResultSize(RunnableObj<AnswerSpec> answerSpec) throws WdkModelException {
    int oldEstimatedSize = _estimatedSize;
    _estimatedSize = AnswerValueFactory.makeAnswer(_user, answerSpec).getResultSizeFactory().getDisplayResultSize();
    _estimatedSizeRefreshed = true;
    if (oldEstimatedSize != _estimatedSize) {
      // update value in database
      _wdkModel.getStepFactory().updateStep(this);
    }
    return _estimatedSize;
  }

  public User getUser() {
    return _user;
  }

  public Date getCreatedTime() {
    return _createdTime;
  }

  public String getBaseCustomName() {
    return _customName;
  }

  /**
   * @return Returns the customName. If no custom name set before, it will
   *         return the default name provided by the underline AnswerValue - a
   *         combination of question's full name, parameter names and values.
   */
  public String getCustomName() {
    String name = _customName;
    Question question = _answerSpec.getQuestion();
    if (name == null || name.isEmpty()) {
      name = question == null ? _answerSpec.getQuestionName() : question.getShortDisplayName();
    }
    // remove script injections
    name = name.replaceAll("<.+?>", " ");
    name = name.replaceAll("[\"]", " ");
    name = name.trim().replaceAll("\\s+", " ");
    if (name.length() > 4000) {
      name = name.substring(0, 4000);
    }
    return name;
  }

  /**
   * @return Returns the custom name, if it is set. Otherwise, returns the short
   *         display name for the underlying question.
   */
  public String getShortDisplayName() {
    Question question = _answerSpec.getQuestion();
    return question == null ? getDisplayName() : question.getShortDisplayName();
  }

  public String getDisplayName() {
    Question question = _answerSpec.getQuestion();
    return question == null
        ? (_customName != null ? _customName : _answerSpec.getQuestionName())
        : question.getDisplayName();
  }

  public long getStepId() {
    return _stepId;
  }

  public Date getLastRunTime() {
    return _lastRunTime != null ? _lastRunTime : _createdTime;
  }

  public boolean hasBooleanQuestion() {
    Question question = _answerSpec.getQuestion();
    return question != null && question.isBoolean();
  }

  public String getDescription() {
    Question question = _answerSpec.getQuestion();
    return question == null ? null : question.getDescription();
  }

  /**
   * @return Returns the isDeleted.
   */
  public boolean isDeleted() {
    return _isDeleted;
  }

  public boolean isCollapsible() {
    return _isCollapsible;
  }

  public String getCollapsedName() {
    if (_collapsedName == null && isCollapsible())
      return getCustomName();
    return _collapsedName;
  }

  public String getProjectId() {
    return _projectId;
  }

  public String getProjectVersion() {
    return _projectVersion;
  }

  public Optional<RecordClass> getRecordClass() {
    return hasValidQuestion() ?
        Optional.of(_answerSpec.getQuestion().getRecordClass()) :
        Optional.empty();
  }

  public boolean isFiltered() throws WdkModelException {

    // check if answer spec's question is valid
    if (!hasValidQuestion()) {
      return false;
    }

    // check if new-style filter has been applied
    if (_answerSpec.getFilterOptions().isFiltered(_answerSpec.toSimpleAnswerSpec()))
      return true;

    // check if old-style filter has been applied
    Optional<AnswerFilterInstance> filter = _answerSpec.getLegacyFilter();
    Optional<AnswerFilterInstance> defaultFilter = _answerSpec.getQuestion().getRecordClass().getDefaultFilter();
    return filter.isPresent() &&
        (!defaultFilter.isPresent() ||
         !defaultFilter.get().getName().equals(filter.get().getName()));
  }

  public void updateEstimatedSize(int checkedSize) {
    _estimatedSize = checkedSize;
    _estimatedSizeRefreshed = true;
  }

  public void resetEstimatedSize() {
    _estimatedSize = RESET_SIZE_FLAG;
    _estimatedSizeRefreshed = false;
  }

  public JSONObject getJSONContent(int strategyId) throws WdkModelException {
    return getJSONContent(strategyId, false);
  }

  public JSONObject getJSONContent(long strategyId, boolean forChecksum) throws WdkModelException {

    JSONObject jsStep = new JSONObject();

    try {
      jsStep.put("id", _stepId);
      jsStep.put("customName", _customName);
      jsStep.put("question", _answerSpec.getQuestion());
      jsStep.put("projectVersion", _projectVersion);
      jsStep.put("filter", _answerSpec.getLegacyFilterName().orElse(null));
      jsStep.put("collapsed", this.isCollapsible());
      jsStep.put("collapsedName", this.getCollapsedName());
      jsStep.put("deleted", _isDeleted);
      jsStep.put(ParamsAndFiltersDbColumnFormat.KEY_PARAMS,
          ParamsAndFiltersDbColumnFormat.formatParams(_answerSpec.getQueryInstanceSpec()));
      jsStep.put(ParamsAndFiltersDbColumnFormat.KEY_FILTERS,
          ParamsAndFiltersDbColumnFormat.formatFilters(_answerSpec.getFilterOptions()));

      Optional<Step> childStep = getSecondaryInputStep();
      if (childStep.isPresent()) {
        jsStep.put("child", childStep.get().getJSONContent(strategyId, forChecksum));
      }

      Optional<Step> prevStep = getPrimaryInputStep();
      if (prevStep.isPresent()) {
        jsStep.put("previous", prevStep.get().getJSONContent(strategyId, forChecksum));
      }

      if (!forChecksum) {
        jsStep.put("size", _estimatedSize);
      }

      if (this.isCollapsible()) { // a sub-strategy, needs to get order number
        String subStratId = strategyId + "_" + _stepId;
        int order = getUser().getSession().getStrategyOrder(subStratId);
        jsStep.put("order", order);
      }
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsStep;
  }

  /**
   * Get the answerParam that take the previousStep as input, which is the first
   * answerParam in the param list.  Returns an empty optional if this step
   * does not have any answer params.
   */
  public Optional<AnswerParam> getPrimaryInputStepParam() {
    return findAnswerParam(0);
  }

  /**
   * Get the answerParam that take the childStep as input, which is the second
   * answerParam in the param list.  Returns an empty optional if this step
   * does not have a second answer param.
   */
  public Optional<AnswerParam> getSecondaryInputStepParam() {
    return findAnswerParam(1);
  }

  public boolean getHasCompleteAnalyses() throws WdkModelException {
    return _wdkModel.getStepAnalysisFactory().hasCompleteAnalyses(this);
  }

  public Optional<Long> getStrategyId() {
    return _strategy.map(Strategy::getStrategyId);
  }

  public Optional<Strategy> getStrategy() {
    return _strategy;
  }

  public boolean hasValidQuestion() {
    return _answerSpec.hasValidQuestion();
  }

  public AnswerSpec getAnswerSpec() {
    return _answerSpec;
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _answerSpec.getValidationBundle();
  }

  public StepContainer getContainer() {
    return _strategy
        // must cast to send empty container as default
        .map(str -> (StepContainer)str)
        .orElse(StepContainer.emptyContainer());
  }

  public boolean isMutable() {
    return !getStrategy().isPresent() || !getStrategy().get().isSaved();
  }

  public boolean isResultSizeDirty() {
    return _isResultSizeDirty;
  }

  public JSONObject getDisplayPrefs() {
    return _displayPrefs;
  }
}
