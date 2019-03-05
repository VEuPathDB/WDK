package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.functional.Functions.getMapFromList;
import static org.gusdb.fgputil.functional.Functions.getNthOrNull;
import static org.gusdb.wdk.model.user.StepContainer.parentOf;
import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.ValidationUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.MDCUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.ParamFiltersClobFormat;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Charles Treatman
 */
public class Step implements Validateable<Step> {

  private static final Logger LOG = Logger.getLogger(Step.class);

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
      _displayPrefs = prefs;
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

  // Set if exception occurs during step loading (but we don't want to bubble the exception up)
  // This allows the UI to show a "broken" step but not hose the whole strategy
  private Exception _exception;

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
    if (_strategy == null) {
      // Confirm left and right child null if strategyId = null
      if (getSecondaryInputStepId() != 0 || getPrimaryInputStepId() != 0) {
        throw new WdkModelException("Step " + _stepId + " does not have a strategy but has answer param values.");
      }
    }
    else if ((getSecondaryInputStepParam().isPresent() && getSecondaryInputStepId() == 0) ||
             (getPrimaryInputStepParam().isPresent() && getPrimaryInputStepId() == 0)) {
      throw new WdkModelException("Step " + _stepId + " is part of a strategy but at least one answer param does not have a value.");
    }
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

  public long getPrimaryInputStepId() {
    Step step = getPrimaryInputStep();
    return step == null ? 0 : step.getStepId();
  }

  public long getSecondaryInputStepId() {
    Step step = getSecondaryInputStep();
    return step == null ? 0 : step.getStepId();
  }

  public Step getPrimaryInputStep() {
    return findAnswerParamsStep(0).orElse(null);
  }

  public Step getSecondaryInputStep() {
    return findAnswerParamsStep(1).orElse(null);
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

    if (!_answerSpec.hasValidQuestion()) {
      return Optional.empty();
    }

    AnswerParam param = getNthOrNull(_answerSpec.getQuestion().getQuery().getAnswerParams(), answerParamOrdinal);
    if (param == null) {
      return Optional.empty();
    }

    QueryInstanceSpec spec = _answerSpec.getQueryInstanceSpec();
    String stableValue = spec.get(param.getName());

    if (stableValue == null) {
      return Optional.empty();
    }

    return getContainer().findFirstStep(withId(AnswerParam.toStepId(stableValue)));
  }

  /**
   * Returns an estimate of the size of this step (number of records returned).
   * This may be the value of the estimate_size column in the steps table, or
   * if getResultSize() has been called, a refreshed value.
   *
   * @return estimate of this step's result size
   */
  public int getEstimatedSize() {
    return _answerSpec.isValid() ? _estimatedSize : 0;
  }

  /**
   * Returns the real result size of this step (number of records returned); once
   * this method is called, getEstimateSize() will also return this value.
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

  // Needs to be updated for transforms
  public String getOperation() {
    if (isFirstStep()) {
      throw new IllegalStateException("getOperation cannot be called on the first Step.");
    }
    BooleanQuery query = (BooleanQuery) _answerSpec.getQuestion().getQuery();
    StringParam operator = query.getOperatorParam();
    return _answerSpec.getQueryInstanceSpec().get(operator.getName());
  }

  public boolean isFirstStep() {
    return _strategy.isPresent() ? _strategy.get().getFirstStep().getStepId() == _stepId : false;
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

  // FIXME: is this really how we should define this method; maybe should be dependent on # of AnswerParam
  public boolean isBoolean() {
    Question question = _answerSpec.getQuestion();
    return question != null && question.isBoolean();
  }

  /**
   * A combined step can take one or more steps as input. a Transform is a special case of combined step, and
   * a boolean is another special case.
   *
   * @return a flag to determine if a step can take other step(s) as input.
   */
  // FIXME: is this really how we should define this method; maybe should be dependent on # of AnswerParam
  public boolean isCombined() {
    Question question = _answerSpec.getQuestion();
    return question != null && question.isCombined();
  }

  /**
   * A transform step can take exactly one step as input.
   *
   * @return Returns whether this Step is a transform
   */
  // FIXME: is this really how we should define this method; maybe should be dependent on # of AnswerParam
  public boolean isTransform() {
    Question question = _answerSpec.getQuestion();
    return question != null && question.isTransform();
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
    if (_isCollapsible)
      return true;
    // it is true if the step is a branch
    return (getParentStep().isPresent() && isCombined());
  }

  public String getCollapsedName() {
    if (_collapsedName == null && isCollapsible())
      return getCustomName();
    return _collapsedName;
  }

  public Map<String, String> getParamNames() {
    return _answerSpec.getQuestion() == null
        ? new LinkedHashMap<>()
        : getMapFromList(
            _answerSpec.getQuestion().getQuery().getParamMap().values(),
            param -> new TwoTuple<>(param.getName(), param.getPrompt()));
  }

  public String getProjectId() {
    return _projectId;
  }

  public String getProjectVersion() {
    return _projectVersion;
  }

  /* functions for navigating/manipulating step tree */
  public Step getStep(int index) {
    List<Step> steps = getMainBranch();
    return steps.get(index);
  }

  /**
   * Get all the previous steps in the strategy. This doesn't include any child
   * steps.
   *
   * @return A list of the previous steps from the current one; the first step
   *         in the strategy will be the first one in the list, and the direct
   *         previous step of the current one will be the last in the list, in
   *         that order.
   */
  public List<Step> getMainBranch() {
    LinkedList<Step> list = new LinkedList<>();
    list.add(this);
    Step previousStep = getPrimaryInputStep();
    while (previousStep != null) {
      list.offerFirst(previousStep);
      previousStep = previousStep.getPrimaryInputStep();
    }
    return list;
  }

  public int getLength() {
    return getMainBranch().size();
  }

  /**
   * Get all the descendants from the current step, including both previous
   * steps and child steps.
   *
   * @return
   */
  public List<Step> getNestedBranch() {
    List<Step> list = new ArrayList<>(); // a list to hold all descendants.
    Stack<Step> stack = new Stack<>();
    stack.push(this);
    while (!stack.isEmpty()) {
      Step step = stack.pop();
      list.add(step);
      Step previousStep = step.getPrimaryInputStep(), childStep = step.getSecondaryInputStep();
      if (previousStep != null) {
        stack.push(previousStep);
      }
      if (childStep != null) {
        stack.push(childStep);
      }
    }
    return list;
  }

  public Step getStepByPreviousId(int previousId) {
    LOG.debug("gettting step by prev id. current=" + this + ", input=" + previousId);
    Step target;
    if (getPrimaryInputStepId() == previousId) {
      return this;
    }
    Step childStep = getSecondaryInputStep();
    if (childStep != null) {
      target = childStep.getStepByPreviousId(previousId);
      if (target != null) {
        return target;
      }
    }
    Step prevStep = getPrimaryInputStep();
    if (prevStep != null) {
      target = prevStep.getStepByPreviousId(previousId);
      if (target != null) {
        return target;
      }
    }
    return null;
  }

  public RecordClass getRecordClass() {
    return _answerSpec.hasValidQuestion() ? null : _answerSpec.getQuestion().getRecordClass();
  }

  public int getIndexFromId(int stepId) throws WdkUserException {
    List<Step> steps = getMainBranch();
    for (int i = 0; i < steps.size(); ++i) {
      Step step = steps.get(i);
      if (step.getStepId() == stepId ||
          (step.getSecondaryInputStep() != null && step.getSecondaryInputStep().getStepId() == stepId)) {
        return i;
      }
    }
    throw new WdkUserException("Id not found!");
  }

  public boolean isFiltered() throws WdkModelException {

    // check if answer spec's question is valid
    if (!_answerSpec.hasValidQuestion()) {
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

  public String getFilterDisplayName() {
    return _answerSpec.getLegacyFilter()
        .map(filter -> filter.getDisplayName())
        .orElse(_answerSpec.getLegacyFilterName()
        .orElse("None"));
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
      jsStep.put("filter", _answerSpec.getLegacyFilterName());
      jsStep.put("collapsed", this.isCollapsible());
      jsStep.put("collapsedName", this.getCollapsedName());
      jsStep.put("deleted", _isDeleted);
      jsStep.put(ParamFiltersClobFormat.KEY_PARAMS,
          ParamFiltersClobFormat.formatParams(_answerSpec.getQueryInstanceSpec()));
      jsStep.put(ParamFiltersClobFormat.KEY_FILTERS,
          ParamFiltersClobFormat.formatFilters(_answerSpec.getFilterOptions()));

      Step childStep = getSecondaryInputStep();
      if (childStep != null) {
        jsStep.put("child", childStep.getJSONContent(strategyId, forChecksum));
      }

      Step prevStep = getPrimaryInputStep();
      if (prevStep != null) {
        jsStep.put("previous", prevStep.getJSONContent(strategyId, forChecksum));
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
   * Get the answerParam that take the previousStep as input, which is the first answerParam in the param
   * list.
   *
   * @return an AnswerParam
   */
  public Optional<AnswerParam> getPrimaryInputStepParam() {
    if (!hasValidQuestion())
      return Optional.empty();

    return Arrays.stream(_answerSpec.getQuestion().getParams())
        .filter(AnswerParam.class::isInstance)
        .findFirst()
        .map(AnswerParam.class::cast);
  }

  /**
   * The previous step param is always the first answerParam.
   */
  public Optional<String> getPrimaryInputStepParamName() {
    return getPrimaryInputStepParam().map(NamedObject::getName);
  }

  public Optional<AnswerParam> getSecondaryInputStepParam() {
    if (!hasValidQuestion())
      return Optional.empty();
    Param[] params = _answerSpec.getQuestion().getParams();
    int index = 0;
    for (Param param : params) {
      if (param instanceof AnswerParam) {
        index++;
        if (index == 2)
          return Optional.of((AnswerParam) param);
      }
    }
    return Optional.empty();
  }

  /**
   * the child step param is always the second answerParam
   */
  public Optional<String> getSecondaryInputStepParamName() {
    return getSecondaryInputStepParam().map(NamedObject::getName);
  }

  public int getFrontId() {
    int frontId;
    Step previousStep = getPrimaryInputStep();
    if (previousStep == null)
      frontId = 1;
    else {
      frontId = previousStep.getFrontId();
      frontId++;
    }
    return frontId;
  }

  @Override
  public String toString() {
    return _stepId + " (" + getPrimaryInputStepId() + ", " + getSecondaryInputStepId() + ")";
  }

  public boolean isUncollapsible() {
    // if the step hasn't been collapsed, it cannot be uncollapsed.
    if (!_isCollapsible)
      return false;

    // if the step is a combined step, it cannot be uncollapsed
    if (isCombined())
      return false;

    return true;
  }

  public Exception getException() {
    return _exception;
  }

  public void setException(Exception ex) {
    _exception = ex;
  }

  public boolean getHasCompleteAnalyses() throws WdkModelException {
    return _wdkModel.getStepAnalysisFactory().hasCompleteAnalyses(this);
  }

  public String getType() {
    RecordClass recordClass = getRecordClass();
    return recordClass == null ? null : getRecordClass().getFullName();
  }

  /**
   * Check id the given step can be assigned as the previous step of the current one. If it's not allowed, a
   * WdkUserException will be thrown out
   *
   * @param previousStep
   * @throws WdkUserException
   */
  public void checkIfPassedStepAllowedAsPrimaryInput(Step previousStep) throws
      WdkUserException {
    // make sure the current step can take any previous step.
    if (!isCombined())
      throw new WdkUserException("The step #" + getStepId() + " cannot take any step as its previousStep.");

    // make sure the current step can take the newStep as previousStep
    String type = previousStep.getType();
    AnswerParam param = getPrimaryInputStepParam()
        .orElseThrow(() -> new WdkUserException("Cannot assign a previous step to step " + getStepId()));
    if (!param.allowRecordClass(type))
      throw new WdkUserException("The new step#" + previousStep.getStepId() + " of type " + type +
          " is not compatible with the next step#" + getStepId());
  }

  /**
   * Check id the given step can be assigned as the child step of the current one. If it's not allowed, a
   * WdkUserException will be thrown out.
   *
   * @param childStep
   * @throws WdkUserException
   */
  public void checkIfPassedStepAllowedAsSecondaryInput(Step childStep) throws WdkUserException {
    // check if the current step can take any child steps.
    if (!isCombined() || isTransform())
      throw new WdkUserException("The step #" + getStepId() + " cannot take any step as its childStep.");

    // make sure the current step can take the newStep as childStep
    String type = childStep.getType();
    AnswerParam param = getSecondaryInputStepParam()
        .orElseThrow(() -> new WdkUserException("Cannot assign a child step to step " + getStepId()));
    if (!param.allowRecordClass(type))
      throw new WdkUserException("The new step#" + childStep.getStepId() + " of type " + type +
          " is not compatible with the parent step#" + getStepId());
  }

  public Optional<Long> getStrategyId() {
    return _strategy.map(Strategy::getStrategyId);
  }

  public Optional<Strategy> getStrategy() {
    return _strategy;
  }

  public boolean hasAnswerParams() {
    return hasValidQuestion() &&
        _answerSpec.getQuestion().getQuery().getAnswerParamCount() > 0;
  }

  static String getVerificationPrefix() {
    return "[IP " + MDCUtil.getIpAddress() + " requested page from " + MDCUtil.getRequestedDomain() + "] ";
  }

  public boolean hasValidQuestion() {
    return _answerSpec.getQuestion() != null;
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
