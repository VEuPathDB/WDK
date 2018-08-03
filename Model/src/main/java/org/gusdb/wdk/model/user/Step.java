package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.functional.Functions.defaultOnException;
import static org.gusdb.fgputil.functional.Functions.getMapFromList;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.model.MDCUtil;
import org.gusdb.wdk.model.WdkIllegalArgumentException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.ParamFiltersClobFormat;
import org.gusdb.wdk.model.answer.spec.ParamValue;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.StepFactoryHelpers.UserCache;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Charles Treatman
 * 
 */
public class Step implements StrategyElement {

  private static final Logger LOG = Logger.getLogger(Step.class);

  // TODO: decide what to do with these constants (key all errors off them somehow?)
  public static enum InvalidReason {

    // answer spec problems
    INVALID_QUESTION,
    INVALID_ANSWER_SPEC,

    // strategy integrity problems
    CHILD_STEPS_OUTSIDE_STRATEGY;
  }

  public static final int RESET_SIZE_FLAG = -1;

  public static class StepBuilder {

    private final WdkModel _wdkModel;
    private long _userId;
    private long _stepId;
    private Long _strategyId;
    private Date _createdTime;
    private Date _lastRunTime;
    private String _customName;
    private boolean _isDeleted = false;
    private boolean _isValidFlag = true;
    private int _estimateSize = -1;
    private boolean _collapsible = false;
    private String _collapsedName = null;
    private String _projectId;
    private String _projectVersion;
    private long _previousStepId = 0;
    private long _childStepId = 0;
    private AnswerSpec _answerSpec;

    // set during finish() from IDs in DB
    private User _user;
    private Strategy _strategy;

    private StepBuilder(WdkModel wdkModel) {
      
    }

    /**
     * Constructor that takes an existing step
     * 
     * @param step step to make a shallow copy of
     */
    private StepBuilder(Step step) {
      _wdkModel = step._wdkModel;
      _user = step._user;
      _userId = step._userId;
      _stepId = step._stepId;
      _createdTime = step._createdTime;
      _lastRunTime = step._lastRunTime;
      _customName = step._customName;
      _isDeleted = step._isDeleted;
      _collapsible = step._collapsible;
      _collapsedName = step._collapsedName;
      _projectId = step._projectId;
      _projectVersion = step._projectVersion;
      _isValidFlag = step._isValidFlag;
      _estimateSize = step._estimateSize;
      _previousStepId = step._previousStepId;
      _childStepId = step._childStepId;
      _strategyId = step._strategyId;
      _inMemoryOnly = step._inMemoryOnly;
      }
    }
  }

  // set during Step object creation
  private final WdkModel _wdkModel;
  private final StepFactory _stepFactory;

  // in DB, owning user id
  private long _userId;
  // in DB, Primary key
  private long _stepId;
  // in DB, encapsulating strategy (may be null if orphan step)
  private Long _strategyId;
  // in DB, set during step creation
  private Date _createdTime;
  // in DB, last time Answer generated, written to DB each time
  private Date _lastRunTime;
  // in DB, set by user
  private String _customName;
  // in DB, for soft delete
  private boolean _isDeleted = false;
  // in DB, valid flag may be set during maintenance (TODO: assess relevance any more?)
  private boolean _isValidFlag = true;
  // in DB, last known size of result (see _estimateSizeRefreshed below)
  private int _estimateSize = -1;
  // in DB, tells if nested step
  private boolean _collapsible = false;
  // in DB, custom name for nested "strategy"
  private String _collapsedName = null;
  // in DB, project ID when step was created
  private String _projectId;
  // in DB, project version when step was created
  private String _projectVersion;
  // in DB, IDs of child steps (i.e. left and right children)
  private long _previousStepId = 0;
  private long _childStepId = 0;

  // in DB, defines the parameters used to find this Step's answer
  private AnswerSpec _answerSpec;

  // set during finish() from IDs in DB
  private User _user;
  private Strategy _strategy;

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
  private boolean _estimateSizeRefreshed = false;

  // stores answer values for this step object and manages reuse of those objects
  private AnswerValueCache _answerValueCache;

  // Set if exception occurs during step loading (but we don't want to bubble the exception up)
  // This allows the UI to show a "broken" step but not hose the whole strategy
  private Exception _exception;

  // Set this if this step should not be written to /read from db. A hack in support of
  // summary views, until they are refactored using service.
  private boolean _inMemoryOnly = false;

  /**
   * Creates a step object for given user and step ID. Note that this constructor lazy-loads the User object
   * for the passed ID if one is required for processing after construction.
   * 
   * @param stepFactory
   *          step factory that generated this step
   * @param userId
   *          id of the owner of this step
   * @param stepId
   *          id of the step
   * @throws WdkModelException
   */
  public Step(WdkModel wdkModel, long userId, long stepId) {
    _wdkModel = wdkModel;
    _stepFactory = wdkModel.getStepFactory();
    _user = null;
    _userId = userId;
    _stepId = stepId;
    _answerValueCache = new AnswerValueCache(this);
    _isDeleted = false;
  }

  public Step getPreviousStep() {
    return _previousStepId == 0 ? null : _strategy.getStep(_previousStepId);
  }

  public Step getChildStep() {
    return _childStepId == 0 ? null : _strategy.getStep(_childStepId);
  }

  public Step getNextStep() {
    return _nextStep;
  }

  public Step getParentStep() {
    return _strategy.findParentOf(_stepId);
  }

  public Step getParentOrNextStep() {
    Step nextStep = getNextStep();
    return (_nextStep != null) ? _nextStep : getParentStep();
  }

  /**
   * Basic getter than just returns the current value for this field without checks, lazy loading, or side
   * effects (e.g., database updates)
   * 
   * @return "current" estimate size as shown in DB
   */
  @Deprecated
  public int getRawEstimateSize() {
    return getEstimateSize();
  }

  /**
   * Calculate the estimate size
   * 
   * @return newly calculated estimate size
   */
  @Deprecated
  public int getCalculatedEstimateSize() {
    return defaultOnException(() -> getResultSize(), 0);
  }

  /**
   * @return Size estimate of this step's result
   */
  public int getEstimateSize() {
    return _answerSpec.isValid() ? _estimateSize : 0;
  }

  /**
   * @param estimateSize
   *          The estimateSize to set.
   */
  void setEstimateSize(int estimateSize) {
    _estimateSize = estimateSize;
  }

  /**
   * Get the real result size from the answerValue. AnswerValue is responsible for caching, if any
   */
  public int getResultSize() throws WdkModelException {
    return _estimateSizeRefreshed ? _estimateSize : !_answerSpec.isValid() ? 0 : recalculateResultSize();
  }

  private int recalculateResultSize() throws WdkModelException {
    _estimateSize = getAnswerValue().getResultSizeFactory().getDisplayResultSize();
    _estimateSizeRefreshed = true;
    update(true);
    return _estimateSize;
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

  @Deprecated
  public void setParentStep(Step parentStep) throws WdkModelException {
    // check if this is a no-op
    if (parentStep.getStepId() == _parentStepId)
      return;
    verifySameOwnerAndProject(this, parentStep);
    _strategy.appendStep(parentStep);
    _parentStepId = parentStep.getStepId();
    if (parentStep != null) {
      parentStep._childStep = this;
      parentStep._childStepId = _stepId;
    }
  }

  public void setChildStep(Step childStep) throws WdkModelException {
    verifySameOwnerAndProject(this, childStep);
    _childStep = childStep;
    if (childStep != null) {
      childStep._parentStep = this;
      _childStepId = childStep.getStepId();
      updateAnswerParamValue(getChildStepParamName(), _childStepId);
    }
    else
      _childStepId = 0;
  }

  public void setNextStep(Step nextStep) throws WdkModelException {
    verifySameOwnerAndProject(this, nextStep);
    _nextStep = nextStep;
    if (nextStep != null) {
      nextStep._previousStepId = _stepId;
    }
  }

  public void setPreviousStep(Step previousStep) throws WdkModelException {
    verifySameOwnerAndProject(this, previousStep);
    _previousStep = previousStep;
    if (previousStep != null) {
      previousStep._nextStep = this;
      _previousStepId = previousStep.getStepId();
      updateAnswerParamValue(getPreviousStepParamName(), _previousStepId);
    }
    else
      _previousStepId = 0;
  }

  private void updateAnswerParamValue(String paramName, long stepId) {
    _answerSpec = AnswerSpec.builder(_answerSpec).setParamValue(paramName, Long.toString(stepId)).build(
        _answerSpec.getValidationBundle().getLevel());
  }

  public boolean isFirstStep() {
    return (null == getPreviousStepParam());
  }

  public User getUser() {
    return _user;
  }

  /**
   * @return Returns the createTime.
   */
  public Date getCreatedTime() {
    return _createdTime;
  }

  /**
   * @param createTime
   *          The createTime to set.
   */
  void setCreatedTime(Date createdTime) {
    _createdTime = createdTime;
  }

  public String getBaseCustomName() {
    return _customName;
  }

  /**
   * @return Returns the customName. If no custom name set before, it will return the default name provided by
   *         the underline AnswerValue - a combination of question's full name, parameter names and values.
   * @throws WdkModelException
   * @throws SQLException
   * @throws JSONException
   * @throws WdkModelException
   * @throws NoSuchAlgorithmException
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
   * @return Returns the custom name, if it is set. Otherwise, returns the short display name for the
   *         underlying question.
   */
  public String getShortDisplayName() {
    Question question = _answerSpec.getQuestion();
    return question == null ? getDisplayName() : question.getShortDisplayName();
  }

  public String getDisplayName() {
    Question question = _answerSpec.getQuestion();
    return question == null ? (_customName != null ? _customName : _answerSpec.getQuestionName())
        : question.getDisplayName();
  }

  /**
   * @param customName
   *          The customName to set.
   */
  public void setCustomName(String customName) {
    _customName = customName;
  }

  /**
   * @return Returns the stepId.
   */
  public long getStepId() {
    return _stepId;
  }

  /**
   * @return Returns the lastRunTime.
   */
  public Date getLastRunTime() {
    return _lastRunTime;
  }

  /**
   * @param lastRunTime
   *          The lastRunTime to set.
   */
  public void setLastRunTime(Date lastRunTime) {
    _lastRunTime = lastRunTime;
  }

  // FIXME: is this really how we should define this method; maybe should be dependent on # of AnswerParam
  public boolean isBoolean() {
    Question question = _answerSpec.getQuestion();
    return question == null ? false : question.isBoolean();
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
    return question == null ? false : question.isCombined();
  }

  /**
   * A transform step can take exactly one step as input.
   * 
   * @return Returns whether this Step is a transform
   */
  // FIXME: is this really how we should define this method; maybe should be dependent on # of AnswerParam
  public boolean isTransform() {
    Question question = _answerSpec.getQuestion();
    return question == null ? false : question.isTransform();
  }

  // saves attributes of the step that do NOT impact results or parent steps
  public void update(boolean updateTime) throws WdkModelException {
    // HACK: don't update if this is an in-memory only Step
    // remove this once we refactor the world of summary views, so they don't need such Steps
    if (_inMemoryOnly)
      return;
    _stepFactory.updateStep(getUser(), this, updateTime);
  }

  // saves param values AND filter values (AND step name and maybe other things)
  public void saveParamFilters() throws WdkModelException {
    // get Step as it is in the DB (FIXME: we should be tracking this in memory)
    Step dbStep = _stepFactory.getStepById(getStepId());
    saveParamFilters(dbStep);
  }

  public void saveParamFilters(Step unmodifiedVersion) throws WdkModelException {

    _stepFactory.saveStepParamFilters(this);
    _stepFactory.resetStepCounts(this);

    // Update the in-memory estimateSize with the reset flag in case other
    // step operations are called on the step object.
    _estimateSize = RESET_SIZE_FLAG;

    // get list of steps dependent on this one; all their results are now invalid
    List<Long> stepIds = _stepFactory.getStepAndParents(getStepId());
    Functions.filterInPlace(stepIds, new Predicate<Long>() {
      @Override
      public boolean test(Long candidateStepId) {
        // keep unless id is for this step
        return (getStepId() != candidateStepId.longValue());
      }
    });

    // alert listeners that this step has been revised and await results
    Events.triggerAndWait(new StepRevisedEvent(this, unmodifiedVersion),
        new WdkModelException("Unable to process all StepRevised events for revised step " + getStepId()));

    // alert listeners that the step results have changed for these steps and wait for completion
    Events.triggerAndWait(new StepResultsModifiedEvent(stepIds),
        new WdkModelException("Unable to process all StepResultsModified events for step IDs: " +
            FormatUtil.arrayToString(stepIds.toArray())));

    // refresh in-memory step here in case listeners also modified it
    refreshAnswerSpec();
  }

  /**
   * Refreshes some key fields of this step with the current values in the DB. This is to support outside
   * modification of the step by event listeners. If a listener modifies the step in response to a change we
   * made, we will want to reflect these secondary changes in this current execution flow.
   * 
   * @throws WdkModelException
   *           if unable to load updated step
   */
  private void refreshAnswerSpec() throws WdkModelException {
    Step step = _stepFactory.getStepById(getStepId());
    _answerSpec = step._answerSpec;
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

  /**
   * @param isDeleted
   *          The isDeleted to set.
   */
  public void setDeleted(boolean isDeleted) {
    _isDeleted = isDeleted;
  }

  public boolean isCollapsible() {
    if (_collapsible)
      return true;
    // it is true if the step is a branch
    return (getParentStep() != null && isCombined());
  }

  public void setCollapsible(boolean isCollapsible) {
    _collapsible = isCollapsible;
  }

  public String getCollapsedName() {
    if (_collapsedName == null && isCollapsible())
      return getCustomName();
    return _collapsedName;
  }

  public void setCollapsedName(String collapsedName) {
    _collapsedName = collapsedName;
  }

  /**
   * Checks validity of this step and all the child steps it depends on and returns result. Value is memoized
   * for efficiency. This call checks against any preset valid field value (set from the DB or during a
   * previous call to isValid()), and checks that param names are correct, but does not check param values due
   * to execution cost. Param values must be checked elsewhere; if they are found invalid, invalidateStep()
   * should be called, which updates the DB.
   * 
   * @return true if this step is valid (to the best of our knowledge), else false
   */
  public boolean isValid() {
    return _answerSpec.isValid();
  }

  public Map<String, String> getParamNames() {
    return _answerSpec.getQuestion() == null ? new LinkedHashMap<String, String>()
        : getMapFromList(_answerSpec.getQuestion().getQuery().getParamMap().values(),
            param -> new TwoTuple<>(param.getName(), param.getPrompt()));
  }

  public String getQuestionName() {
    return _answerSpec.getQuestionName();
  }

  public String getProjectId() {
    return _projectId;
  }

  public void setProjectId(String projectId) {
    _projectId = projectId;
  }

  public String getProjectVersion() {
    return _projectVersion;
  }

  public void setProjectVersion(String projectVersion) {
    _projectVersion = projectVersion;
  }

  /* functions for navigating/manipulating step tree */
  public Step getStep(int index) throws WdkModelException {
    List<Step> steps = getMainBranch();
    return steps.get(index);
  }

  /**
   * Get all the previous steps in the strategy. This doesn't include any child steps.
   * 
   * @return A list of the previous steps from the current one; the first step in the strategy will be the
   *         first one in the list, and the direct previous step of the current one will be the last in the
   *         list, in that order.
   * @throws WdkModelException
   */
  public List<Step> getMainBranch() throws WdkModelException {
    LinkedList<Step> list = new LinkedList<>();
    list.add(this);
    Step previousStep = getPreviousStep();
    while (previousStep != null) {
      list.offerFirst(previousStep);
      previousStep = previousStep.getPreviousStep();
    }
    return list;
  }

  public int getLength() throws WdkModelException {
    return getMainBranch().size();
  }

  /**
   * Get all the descendants from the current step, including both previous steps and child steps.
   * 
   * @return
   * @throws WdkModelException
   */
  public List<Step> getNestedBranch() throws WdkModelException {
    List<Step> list = new ArrayList<>(); // a list to hold all descendants.
    Stack<Step> stack = new Stack<>();
    stack.push(this);
    while (!stack.isEmpty()) {
      Step step = stack.pop();
      list.add(step);
      Step previousStep = step.getPreviousStep(), childStep = step.getChildStep();
      if (previousStep != null) {
        stack.push(previousStep);
      }
      if (childStep != null) {
        stack.push(childStep);
      }
    }
    return list;
  }

  public void addStep(Step step) throws WdkModelException {
    step.setPreviousStep(this);
    this.setNextStep(step);
  }

  public Step getStepByDisplayId(long displayId) throws WdkModelException {
    Step target;
    if (_stepId == displayId) {
      return this;
    }
    Step childStep = getChildStep();
    if (childStep != null) {
      target = childStep.getStepByDisplayId(displayId);
      if (target != null) {
        return target;
      }
    }
    Step prevStep = getPreviousStep();
    if (prevStep != null) {
      target = prevStep.getStepByDisplayId(displayId);
      if (target != null) {
        return target;
      }
    }
    return null;
  }

  public Step getStepByChildId(int childId) throws WdkModelException {
    LOG.debug("getting step by child id. current=" + this + ", input=" + childId);
    Step target;
    if (_childStepId == childId) {
      return this;
    }
    Step childStep = getChildStep();
    if (childStep != null) {
      target = childStep.getStepByChildId(childId);
      if (target != null) {
        return target;
      }
    }
    Step prevStep = getPreviousStep();
    if (prevStep != null) {
      target = prevStep.getStepByChildId(childId);
      if (target != null) {
        return target;
      }
    }
    return null;
  }

  public Step getStepByPreviousId(int previousId) throws WdkModelException {
    LOG.debug("gettting step by prev id. current=" + this + ", input=" + previousId);
    Step target;
    if (_previousStepId == previousId) {
      return this;
    }
    Step childStep = getChildStep();
    if (childStep != null) {
      target = childStep.getStepByPreviousId(previousId);
      if (target != null) {
        return target;
      }
    }
    Step prevStep = getPreviousStep();
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

  public int getIndexFromId(int stepId) throws WdkUserException, WdkModelException {
    List<Step> steps = getMainBranch();
    for (int i = 0; i < steps.size(); ++i) {
      Step step = steps.get(i);
      if (step.getStepId() == stepId ||
          (step.getChildStep() != null && step.getChildStep().getStepId() == stepId)) {
        return i;
      }
    }
    throw new WdkUserException("Id not found!");
  }

  public Step createStep(String filterName, int assignedWeight) throws WdkModelException {
    AnswerFilterInstance filter = _answerSpec.getQuestion().getRecordClass().getFilterInstance(filterName);
    return createStep(filter, assignedWeight);
  }

  public Step createStep(AnswerFilterInstance filter, int assignedWeight) throws WdkModelException {
    // make sure caller is asking for something new; if not, return this Step
    AnswerFilterInstance oldFilter = _answerSpec.getLegacyFilter();
    if (_answerSpec.getAssignedWeight() == assignedWeight && ((filter == null && oldFilter == null) ||
        (filter != null && oldFilter != null && filter.getName().equals(oldFilter.getName())))) {
      return this;
    }

    // create new steps
    Question question = _answerSpec.getQuestion();
    Map<String, String> params = _answerSpec.getQueryInstanceSpec().toMap();
    Step step = StepUtilities.createStep(_user, _strategyId, question, params, filter, _isDeleted,
        assignedWeight, _answerSpec.getFilterOptions());
    step._collapsedName = _collapsedName;
    step._customName = _customName;
    step._collapsible = _collapsible;
    step.update(false);
    return step;
  }

  /**
   * deep clone a step, the step will get a new id, and if the step contains other sub-steps, all those sub
   * steps are cloned recursively.
   * 
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public Step deepClone(Long strategyId, Map<Long, Long> stepIdMap) throws WdkModelException {
    Step step;
    if (!isCombined()) {
      step = StepUtilities.createStep(_user, strategyId, _answerSpec.getQuestion(),
          _answerSpec.getQueryInstanceSpec().toMap(), _answerSpec.getLegacyFilter(), _isDeleted,
          _answerSpec.getAssignedWeight(), _answerSpec.getFilterOptions());
    }
    else {
      Question question = _answerSpec.getQuestion();
      Map<String, String> paramValues = new LinkedHashMap<String, String>();
      Map<String, Param> params = question.getParamMap();
      for (String paramName : _answerSpec.getQueryInstanceSpec().keySet()) {
        Param param = params.get(paramName);
        String paramValue = _answerSpec.getQueryInstanceSpec().get(paramName);
        if (param instanceof AnswerParam) {
          Step child = StepUtilities.getStep(getUser(), Long.parseLong(paramValue));
          child = child.deepClone(strategyId, stepIdMap);
          paramValue = Long.toString(child.getStepId());
        }
        paramValues.put(paramName, paramValue);
      }
      step = StepUtilities.createStep(getUser(), strategyId, question, paramValues,
          _answerSpec.getLegacyFilter(), _isDeleted, _answerSpec.getAssignedWeight(),
          _answerSpec.getFilterOptions());
    }

    stepIdMap.put(getStepId(), step.getStepId());

    step._collapsedName = _collapsedName;
    step._customName = _customName;
    step._collapsible = _collapsible;
    step.update(false);

    Events.triggerAndWait(new StepCopiedEvent(this, step),
        new WdkModelException("Unable to execute all operations subsequent to step copy."));

    return step;
  }

  public boolean isFiltered() throws WdkModelException {
    // first check if new filter has been applied
    if (_answerSpec.getFilterOptions() != null &&
        _answerSpec.getFilterOptions().isFiltered(_answerSpec.toSimpleAnswerSpec()))
      return true;

    AnswerFilterInstance filter = _answerSpec.getLegacyFilter();
    Question question = _answerSpec.getQuestion();
    if (filter == null || question == null) {
      return false;
    }

    AnswerFilterInstance defaultFilter = question.getRecordClass().getDefaultFilter();
    return defaultFilter == null ? true : !defaultFilter.getName().equals(filter.getName());
  }

  public String getFilterDisplayName() {
    AnswerFilterInstance filter = _answerSpec.getLegacyFilter();
    return (filter != null) ? filter.getDisplayName() : _answerSpec.getLegacyFilterName();
  }

  public Step getFirstStep() {
    Step step = this;
    while (step.getPreviousStep() != null)
      step = step.getPreviousStep();
    return step;
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

      Step childStep = getChildStep();
      if (childStep != null) {
        jsStep.put("child", childStep.getJSONContent(strategyId, forChecksum));
      }

      Step prevStep = getPreviousStep();
      if (prevStep != null) {
        jsStep.put("previous", prevStep.getJSONContent(strategyId, forChecksum));
      }

      if (!forChecksum) {
        jsStep.put("size", _estimateSize);
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

  public AnswerValue getAnswerValue() throws WdkModelException {
    return _answerValueCache.getAnswerValue(true);
  }

  public AnswerValue getViewAnswerValue() throws WdkModelException {
    return _answerValueCache.getViewAnswerValue(true);
  }

  public AnswerValue getAnswerValue(boolean validate) throws WdkModelException {
    return _answerValueCache.getAnswerValue(validate);
  }

  public void resetAnswerValue() {
    _answerValueCache.invalidateAll();
  }

  /**
   * @return the previousStepId
   */
  public long getPreviousStepId() {
    return _previousStepId;
  }

  /**
   * @param previousStepId
   *          the previousStepId to set
   */
  public void setPreviousStepId(long previousStepId) {
    _previousStepId = previousStepId;
  }

  public void setAndVerifyPreviousStepId(long previousStepId) throws WdkModelException {
    verifySameOwnerAndProject(this, previousStepId);
    setPreviousStepId(previousStepId);
  }

  /**
   * @return the childStepId
   */
  public long getChildStepId() {
    return _childStepId;
  }

  /**
   * @param childStepId
   *          the childStepId to set
   */
  public void setChildStepId(long childStepId) {
    _childStepId = childStepId;
  }

  public void setAndVerifyChildStepId(long childStepId) throws WdkModelException {
    verifySameOwnerAndProject(this, childStepId);
    setChildStepId(childStepId);
  }

  /**
   * Get the answerParam that take the previousStep as input, which is the first answerParam in the param
   * list.
   * 
   * @return an AnswerParam
   */
  public AnswerParam getPreviousStepParam() {
    if (!hasValidQuestion())
      return null;
    Param[] params = _answerSpec.getQuestion().getParams();
    for (Param param : params) {
      if (param instanceof AnswerParam) {
        return (AnswerParam) param;
      }
    }
    return null;

  }

  /**
   * The previous step param is always the first answerParam.
   * 
   * @return
   * @throws WdkModelException
   */
  public String getPreviousStepParamName() throws WdkModelException {
    AnswerParam param = getPreviousStepParam();
    return (param == null) ? null : param.getName();
  }

  public AnswerParam getChildStepParam() {
    if (!hasValidQuestion())
      return null;
    Param[] params = _answerSpec.getQuestion().getParams();
    int index = 0;
    for (Param param : params) {
      if (param instanceof AnswerParam) {
        index++;
        if (index == 2)
          return (AnswerParam) param;
      }
    }
    return null;
  }

  /**
   * the child step param is always the second answerParam
   * 
   * @return
   * @throws WdkModelException
   */
  public String getChildStepParamName() throws WdkModelException {
    AnswerParam param = getChildStepParam();
    return (param == null) ? null : param.getName();
  }

  public int getFrontId() throws WdkUserException, WdkModelException, SQLException, JSONException {
    int frontId;
    Step previousStep = getPreviousStep();
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
    return _stepId + " (" + _previousStepId + ", " + _childStepId + ")";
  }

  public boolean isUncollapsible() {
    // if the step hasn't been collapsed, it cannot be uncollapsed.
    if (!_collapsible)
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
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public void checkPreviousAllowed(Step previousStep) throws WdkModelException, WdkUserException {
    // make sure the current step can take any previous step.
    if (!isCombined())
      throw new WdkUserException("The step #" + getStepId() + " cannot take any step as its previousStep.");

    // make sure the current step can take the newStep as previousStep
    String type = previousStep.getType();
    AnswerParam param = getPreviousStepParam();
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
   * @throws WdkModelException
   */
  public void checkChildAllowed(Step childStep) throws WdkUserException, WdkModelException {
    // check if the current step can take any child steps.
    if (!isCombined() || isTransform())
      throw new WdkUserException("The step #" + getStepId() + " cannot take any step as its childStep.");

    // make sure the current step can take the newStep as childStep
    String type = childStep.getType();
    AnswerParam param = getChildStepParam();
    if (!param.allowRecordClass(type))
      throw new WdkUserException("The new step#" + childStep.getStepId() + " of type " + type +
          " is not compatible with the parent step#" + getStepId());
  }

  @Override
  public Long getStrategyId() {
    return _strategyId;
  }

  public void setStrategyId(Long strategyId) {
    _strategyId = strategyId;
  }

  public void setAnswerValuePaging(int start, int end) {
    _answerValueCache.setPaging(start, end);

  }

  public void setInMemoryOnly(boolean inMemoryOnly) {
    _inMemoryOnly = inMemoryOnly;
  }

  public boolean hasAnswerParams() {
    if (!hasValidQuestion())
      return false;
    for (Param param : _answerSpec.getQuestion().getParams()) {
      if (param instanceof AnswerParam)
        return true;
    }
    return false;
  }

  public boolean isAnswerSpecComplete() {
    return hasAnswerParams() ? _strategyId != null : true;
  }

  public boolean finish(UserCache userCache, Strategy owner) throws WdkModelException {
    try {
      // 1. Set user and strategy for this step
      _user = userCache.get(_userId);
      _strategy = owner;

      // 2. Confirm left and right child null if strategyId = null
      if (_strategyId == null &&
          (_childStepId != 0 || _childStep != null || _previousStepId != 0 || _previousStep != null)) {
        return setInvalid(InvalidReason.CHILD_STEPS_OUTSIDE_STRATEGY);
      }

      // 3. Confirm left and right child match answer params
      // TODO: implement this

    }
    catch (JSONException e) {
      throw new WdkModelException("ParamFilters not valid JSON", e);
    }
  }

  Long getUserId() {
    return _userId;
  }

  void setUser(User user) {
    _user = user;
  }

  @Override
  public long getId() {
    return getStepId();
  }

  public void setAnswerSpec(AnswerSpec answerSpec) {
    _answerSpec = answerSpec;
  }

  private void verifySameOwnerAndProject(Step step1, Step step2) throws WdkModelException {
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

  private void verifySameOwnerAndProject(Step step1, long step2Id) throws WdkModelException {
    // some logic sets 0 for step IDs; this is valid but not eligible for this check
    if (step2Id == 0)
      return;
    Step step2;
    try {
      step2 = _stepFactory.getStepById(step2Id);
    }
    catch (Exception e) {
      throw new WdkIllegalArgumentException(getVerificationPrefix() + "Unable to load step with ID " +
          step2Id + " to compare owners with another step.", e);
    }
    verifySameOwnerAndProject(step1, step2);
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

  public void patchAnswerParams() throws WdkModelException {
    Param[] params = _answerSpec.getQuestion().getParams();
    boolean leftParamEmpty = true;
    for (Param param : params) {
      if (param instanceof AnswerParam) {
        if (leftParamEmpty) {
          updateAnswerParamValue(param.getName(), getPreviousStepId());
          leftParamEmpty = false;
        }
        else {
          updateAnswerParamValue(param.getName(), getChildStepId());
        }
      }
    }
    saveParamFilters();
  }

  public boolean isRunnable() {
    if (_answerSpec)
  }
}
