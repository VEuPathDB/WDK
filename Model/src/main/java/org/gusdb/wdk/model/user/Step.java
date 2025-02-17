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

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.ValidationUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.oauth2.client.veupathdb.UserInfo;
import org.gusdb.wdk.model.OwnedObject;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONObject;

/**
 * @author Charles Treatman
 * @author Ryan Doherty
 */
@SuppressWarnings("UseOfObsoleteDateTimeApi")
public class Step implements Validateable<Step>, OwnedObject {

  private static final Logger LOG = Logger.getLogger(Step.class);

  public static final int RESET_SIZE_FLAG = -1;

  public static final int NAME_COLUMN_MAX_SIZE = 200; // bytes

  public static class StepBuilder {

    private final WdkModel _wdkModel;
    private long _owningUserId;
    private long _stepId;
    private String _projectId;
    private String _projectVersion;
    private Optional<Long> _strategyId;
    private Date _createdTime;
    private Date _lastRunTime;
    private String _customName;
    private boolean _isDeleted;
    private int _estimatedSize = -1;
    private AnswerSpecBuilder _answerSpec; // cannot be null; must be set
    private boolean _isResultSizeDirty;
    private JSONObject _displayPrefs;
    private String _expandedName;
    private boolean _isExpanded;

    private StepBuilder(WdkModel wdkModel, long owningUserId, long stepId) {
      _wdkModel = wdkModel;
      _projectId = wdkModel.getProjectId();
      _projectVersion = wdkModel.getVersion();
      _owningUserId = owningUserId;
      _stepId = stepId;
      _displayPrefs = new JSONObject();
      _createdTime = new Date();
      _lastRunTime = new Date();
      _strategyId = Optional.empty();
    }

    /**
     * Constructor that takes an existing step
     *
     * @param step step to make a shallow copy of
     */
    private StepBuilder(Step step) {
      _wdkModel = step._wdkModel;
      _owningUserId = step._owningUser.getUserId();
      _strategyId = Optional.empty();
      _strategyId = step.getStrategyId();
      _stepId = step.getStepId();
      _createdTime = step._createdTime;
      _lastRunTime = step._lastRunTime;
      _customName = step._customName;
      _isDeleted = step.isDeleted();
      _projectId = step.getProjectId();
      _projectVersion = step.getProjectVersion();
      _estimatedSize = step._estimatedSize;
      _answerSpec = AnswerSpec.builder(step._answerSpec);
      _displayPrefs = new JSONObject(step.getDisplayPrefs().toString());
      _expandedName = step.getExpandedName();
      _isExpanded = step.isExpanded();
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

    public StepBuilder setOwningUserId(long owningUserId) {
      _owningUserId = owningUserId;
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

    public StepBuilder setDeleted(boolean isDeleted) {
      _isDeleted = isDeleted;
      return this;
    }

    public StepBuilder setCustomName(String customName) {
      _customName = customName;
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
      if (isResultSizeDirty) {
        LOG.debug("Setting result size dirty on step: " + _stepId);
      }
      _isResultSizeDirty = isResultSizeDirty;
      return this;
    }

    public StepBuilder setDisplayPrefs(JSONObject prefs) {
      _displayPrefs = Objects.requireNonNull(prefs);
      return this;
    }

    @SuppressWarnings("unused") // Getter/Setter Pair
    public JSONObject getDisplayPrefs() {
      return _displayPrefs;
    }

    public Step build(UserInfoCache ownerCache, User requestingUser, ValidationLevel validationLevel, Optional<Strategy> strategy) throws WdkModelException {
      return build(ownerCache, requestingUser, validationLevel, FillStrategy.NO_FILL, strategy);
    }

    public Step build(UserInfoCache ownerCache, User requestingUser, ValidationLevel validationLevel, FillStrategy fillStrategy, Optional<Strategy> strategy) throws WdkModelException {
      if (_strategyId.isPresent() &&
          (strategy.isEmpty() || !_strategyId.get().equals(strategy.get().getStrategyId()))) {
        throw new WdkRuntimeException("Strategy passed to build method (ID=" +
            strategy.map(Strategy::getStrategyId).orElse(null) +
            ") does not match strategy ID set on step builder (ID=" + _strategyId.get() + ").");
      }
      if (_answerSpec == null) {
        throw new WdkRuntimeException("Cannot build a step without an answer spec.");
      }
      return new Step(ownerCache.get(_owningUserId), strategy, this, requestingUser, validationLevel, fillStrategy);
    }

    /**
     * Builds a runnable step.  Will throw ValidObjectWrappingException if step
     * is not runnable after validation
     *
     * @param ownerCache
     *   a user cache
     * @param strategy
     *   strategy containing the step
     *
     * @return a runnable step
     *
     * @throws WdkModelException
     *   if unable to validate step
     */
    public RunnableObj<Step> buildRunnable(UserInfoCache ownerCache, User requestingUser, Optional<Strategy> strategy) throws WdkModelException {
      return ValidObjectFactory.getRunnable(build(ownerCache, requestingUser, ValidationLevel.RUNNABLE, FillStrategy.NO_FILL, strategy));
    }

    public long getUserId() {
      return _owningUserId;
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

    public String getExpandedName() {
      return _expandedName;
    }

    public StepBuilder setExpandedName(String expandedName) {
      _expandedName = expandedName;
      return this;
    }

    public boolean isExpanded() {
      return _isExpanded;
    }

    public StepBuilder setExpanded(boolean expanded) {
      _isExpanded = expanded;
      return this;
    }

    @Override
    public String toString() {
      return JsonUtil.serialize(new JSONObject()
        .put("id", _stepId)
        .put("question", _answerSpec.getQuestionName()));
    }
  }

  public static StepBuilder builder(WdkModel wdkModel, long owningUserId, long stepId) {
    return new StepBuilder(wdkModel, owningUserId, stepId);
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
  private final UserInfo _owningUser;
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
  // in DB, project ID when step was created
  private final String _projectId;
  // in DB, project version when step was created
  private final String _projectVersion;
  // in DB, defines the parameters used to find this Step's answer
  private final AnswerSpec _answerSpec;

  // in DB, last known size of result (see _estimateSizeRefreshed below)
  private int _estimatedSize;

  private final boolean _isExpanded;

  private final String _expandedName;

  // Steps within the "main branch" strategy flow
  // Next Step must be combined step (e.g. transform or boolean/span-logic (two-answer function))
  // private Step _nextStep = null;
  // Previous step could be first step (=leaf), or combined step
  // private Step _previousStep = null;

  // Child can be a "normal" leaf, or collapsible version of leaf, boolean, or transform
  // private Step _childStep = null;

  /**
   * First set when step was created and answer generated, size stored in DB. So
   * can use this to show step size without pulling records from cache. Size ==
   * -1 means must rerun step (and recache results), get size and store in step
   * table. Can be out of date for releases since we don't rerun strategies on
   * release. Should be reset on step table every time we rerun step.
   * EstimateSize is set to -1 when step is revised (in place)- also all steps
   * affected by this step are also changed to -1 so the values are set when
   * those steps are rerun (i.e. value of -1 means step is "dirty" (modified but
   * not run))
   */
  private boolean _estimatedSizeRefreshed;

  /**
   * Set if answer spec was modified from the version stored in the database;
   * like most other attributes it is immutable and serves only to inform its
   * strategy that downstream steps' estimatedSize must be reset to -1 since
   * they are out of date.
   */
  private final boolean _isResultSizeDirty;

  private final JSONObject _displayPrefs;

  /**
   * Creates a step object for given user and step ID. Note that this
   * constructor lazy-loads the User object for the passed ID if one is required
   * for processing after construction.
   *
   * @param owner
   *   Owner of this step
   * @param strategy
   *   Strategy this step belongs to
   * @param builder
   *   Step builder containing this step's property values
   * @param validationLevel
   *   level to which this step should be validated
   * @param fillStrategy
   *   whether and when to fill in default values for parameters if not present or if not valid
   *
   * @throws WdkModelException
   *   if this step does not pass the given validation level
   */
  private Step(UserInfo owner, Optional<Strategy> strategy, StepBuilder builder,
      User requestingUser, ValidationLevel validationLevel, FillStrategy fillStrategy) throws WdkModelException {
    _owningUser = owner;
    _strategy = strategy;
    _wdkModel = builder._wdkModel;
    _stepId = builder._stepId;
    _createdTime = builder._createdTime;
    _lastRunTime = builder._lastRunTime;
    _customName = checkName("customName", builder._customName);
    _isDeleted = builder._isDeleted;
    _projectId = builder.getProjectId();
    _projectVersion = builder._projectVersion;
    _estimatedSize = builder._estimatedSize;
    _answerSpec = builder._answerSpec.build(requestingUser, getContainer(), validationLevel, fillStrategy);
    _displayPrefs = new JSONObject(builder._displayPrefs.toString());
    _isExpanded = builder.isExpanded();
    _expandedName = checkName("expandedName", builder.getExpandedName());

    // set estimated size appropriately if this step set dirty
    _isResultSizeDirty = builder._isResultSizeDirty;
    if (_isResultSizeDirty) {
      _estimatedSize = RESET_SIZE_FLAG;
    }

    // sanity checks regarding answer param values vs strategy present
    for (String answerParamName : getAnswerParamNames()) {
      String paramValue = _answerSpec.getQueryInstanceSpec().get(answerParamName);
      // Confirm left and right child null iff strategyId = null
      if (_strategy.isEmpty() && !AnswerParam.NULL_VALUE.equals(paramValue)) {
        throw new WdkModelException("Step " + _stepId + " does not have a strategy but answer param " + answerParamName + " has a value.");
      }
      if (_strategy.isPresent() && AnswerParam.NULL_VALUE.equals(paramValue)) {
        throw new WdkModelException("Step " + _stepId + " is part of a strategy but answer param " + answerParamName + " does not have a value.");
      }
    }
  }

  private List<String> getAnswerParamNames() {
    return _answerSpec.getQuestion()
        .map(question -> question.getQuery().getAnswerParams().stream()
            .map(NamedObject::getName).collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  private static String checkName(String field, String val) throws WdkModelException {
    return val == null ? null :
      ValidationUtil.maxLength(val, NAME_COLUMN_MAX_SIZE, () ->
          new WdkModelException(String.format("Step field '%s' cannot be longer" +
              " than %d", field, NAME_COLUMN_MAX_SIZE)));
  }

  public User getRequestingUser() {
    return _answerSpec.getRequestingUser();
  }

  public Optional<Step> getParentStep() {
    return getContainer().findFirstStep(parentOf(_stepId));
  }

  /**
   * @return optional containing primary input step.  If this step's question
   *   does not have a primary input or if the step is not part of a strategy
   *   (i.e. answer params have null value), an empty optional is returned.
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
   * @param answerParamOrdinal
   *   index of the answer param whose value should be used to look up step
   *
   * @return the found step, or null if not found
   */
  private Optional<Step> findAnswerParamsStep(int answerParamOrdinal) {

    Optional<AnswerParam> answerParam = findAnswerParam(answerParamOrdinal);
    if (answerParam.isEmpty()) {
      return Optional.empty();
    }

    QueryInstanceSpec spec = _answerSpec.getQueryInstanceSpec();
    String stableValue = spec.get(answerParam.get().getName());

    if (AnswerParam.NULL_VALUE.equals(stableValue)) {
      return Optional.empty();
    }

    Optional<Step> foundStep = getContainer().findFirstStep(withId(AnswerParam.toStepId(stableValue)));
    if (foundStep.isEmpty()) {
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
    return _answerSpec.getQuestion().flatMap(question ->
        Optional.ofNullable(getNthOrNull(question.getQuery().getAnswerParams(), answerParamOrdinal)));
  }

  public boolean isExpanded() {
    return _isExpanded;
  }

  public String getExpandedName() {
    return _expandedName;
  }

  /**
   * Returns an estimate of the size of this step (number of records returned).
   * This may be the value of the estimate_size column in the steps table, or if
   * getResultSize() has been called, a refreshed value.  Returns 0 if this step
   * has been found to be invalid.
   *
   * @return estimate of this step's result size
   */
  public int getEstimatedSize() {
    return _answerSpec.isValid() ? _estimatedSize : -1;
  }

  /**
   * Returns the real result size of this step (number of records returned);
   * once this method is called, getEstimateSize() will also return this value,
   * and the estimate_size column in the database will also be updated.
   *
   * @return the real result size gained by running the step
   */
  public int getResultSize() throws WdkModelException {
    return _estimatedSizeRefreshed ? _estimatedSize :
           !_answerSpec.isRunnable() ? -1 :
           recalculateResultSize(_answerSpec.getRunnable().getLeft());
  }

  // should only be called by this step's strategy
  void setRefreshedResultSize(int resultSize) {
    _estimatedSize = resultSize;
    _estimatedSizeRefreshed = true;
  }

  public Optional<Integer> recalculateResultSize() throws WdkModelException {
    return _answerSpec.isRunnable() ? Optional.of(recalculateResultSize(_answerSpec.getRunnable().getLeft())) : Optional.empty();
  }

  private int recalculateResultSize(RunnableObj<AnswerSpec> answerSpec) throws WdkModelException {
    int oldEstimatedSize = _estimatedSize;
    _estimatedSize = AnswerValueFactory.makeAnswer(answerSpec).getResultSizeFactory().getDisplayResultSize();
    _estimatedSizeRefreshed = true;
    if (oldEstimatedSize != _estimatedSize) {
      // update value in database
      new StepFactory(answerSpec.get().getRequestingUser()).updateStep(this);
    }
    return _estimatedSize;
  }

  @Override
  public UserInfo getOwningUser() {
    return _owningUser;
  }

  public Date getCreatedTime() {
    return _createdTime;
  }

  public String getBaseCustomName() {
    return _customName;
  }

  /**
   * @return Returns the customName. If no custom name set before, it will
   *   return the default name provided by the underlying AnswerValue - a
   *   combination of question's full name, parameter names and values.
   */
  public String getCustomName() {

    String name = getCustomNameOpt()
        .orElse(_answerSpec.getQuestion()
            .map(Question::getShortDisplayName)
            .orElse(_answerSpec.getQuestionName()));

    // remove script injections
    name = name.replaceAll("<.+?>", " ")
      .replaceAll("[\"]", " ")
      .trim()
      .replaceAll("\\s+", " ");

    return name.length() > 4000 ? name.substring(0, 4000) : name;
  }

  private Optional<String> getCustomNameOpt() {
    return _customName == null || _customName.isBlank()
        ? Optional.empty() : Optional.of(_customName);
  }

  /**
   * @return Returns the custom name, if it is set. Otherwise, returns the short
   *   display name for the underlying question.
   */
  public String getShortDisplayName() {
    return _answerSpec.getQuestion()
        .map(Question::getShortDisplayName)
        .orElse(getDisplayName());
  }

  /**
   * @return Returns the custom name, if it is set. Otherwise, returns the
   *   display name for the underlying question.
   */
  public String getDisplayName() {
    
    return _answerSpec.getQuestion()
        .map(Question::getDisplayName)
        .orElse(getCustomNameOpt()
            .orElse(_answerSpec.getQuestionName()));
  }

  public long getStepId() {
    return _stepId;
  }

  public Date getLastRunTime() {
    return _lastRunTime != null ? _lastRunTime : _createdTime;
  }

  public boolean hasBooleanQuestion() {
    return _answerSpec.getQuestion().map(Question::isBoolean).orElse(false);
  }

  public String getDescription() {
    return _answerSpec.getQuestion().map(Question::getDescription).orElse(null);
  }

  /**
   * @return Returns the isDeleted.
   */
  public boolean isDeleted() {
    return _isDeleted;
  }

  public String getProjectId() {
    return _projectId;
  }

  public String getProjectVersion() {
    return _projectVersion;
  }

  public Optional<RecordClass> getRecordClass() {
    return _answerSpec.getQuestion().map(Question::getRecordClass);
  }

  public boolean isFiltered() throws WdkModelException {

    // check if answer spec's question is valid
    if (!hasValidQuestion()) {
      return false;
    }

    // check if new-style filter has been applied
    if (_answerSpec.getFilterOptions().isFiltered(_answerSpec.toSimpleAnswerSpec()))
      return true;

    // check if column filter has been applied
    if (!_answerSpec.getColumnFilterConfig().isEmpty()) {
      return true;
    }

    // check if old-style filter has been applied
    Optional<AnswerFilterInstance> filter = _answerSpec.getLegacyFilter();
    Optional<AnswerFilterInstance> defaultFilter = _answerSpec.getQuestion().get().getRecordClass().getDefaultFilter();
    return filter.isPresent() &&
        (defaultFilter.isEmpty() ||
         !defaultFilter.get().getName().equals(filter.get().getName()));
  }

  /**
   * Get the answerParam that take the previousStep as input, which is the first
   * answerParam in the param list.  Returns an empty optional if this step does
   * not have any answer params.
   */
  public Optional<AnswerParam> getPrimaryInputStepParam() {
    return findAnswerParam(0);
  }

  /**
   * Get the answerParam that take the childStep as input, which is the second
   * answerParam in the param list.  Returns an empty optional if this step does
   * not have a second answer param.
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
    return _answerSpec.getQuestion().isPresent();
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

  public boolean isResultSizeDirty() {
    return _isResultSizeDirty;
  }

  public JSONObject getDisplayPrefs() {
    return _displayPrefs;
  }
}
