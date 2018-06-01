package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.filter.FilterOption;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.ParamValuesInvalidException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Charles Treatman
 * 
 */
public class Step {

  private static final Logger LOG = Logger.getLogger(Step.class);

  public static final int RESET_SIZE_FLAG = -1;

  public static final String KEY_PARAMS = "params";
  public static final String KEY_FILTERS = "filters";
  public static final String KEY_VIEW_FILTERS = "viewFilters";

  // injected during Step object creation
  private final StepFactory _stepFactory;
  // lazy loaded, but every step has a user
  private User _user;
  // in DB, owning user id
  private long _userId;
  // in DB, Primary key
  private long _stepId;
  // in DB, set during step creation
  private Date _createdTime;
  // in DB, last time Answer generated, written to DB each time
  private Date _lastRunTime;
  // in DB, set by user
  private String _customName;
  // in DB, for soft delete
  private boolean _isDeleted = false;

  // creation timestamp for this Java object
  public long _objectCreationDate;

  // nested step
  private boolean _collapsible = false;
  private String _collapsedName = null;

  private String _projectId;
  private String _projectVersion;
  private String _questionName;

  // Steps within the "main branch" strategy flow
  // Next Step must be combined step (e.g. transform or boolean/span-logic (two-answer function))
  private Step _nextStep = null;
  // Previous step could be first step (=leaf), or combined step
  private Step _previousStep = null;

  // Parent step must be a boolean
  private Step _parentStep = null;
  // Child can be a "normal" leaf, or collapsible version of leaf, boolean, or transform
  private Step _childStep = null;

  // Probably should be marked deprecated
  // Jerric says can be retrieved from param values
  private String _booleanExpression;

  // in DB, set during maintenance (true or null = valid, false = invalid)
  private boolean _valid = true;
  // set during runtime so we don't recheck validity over and over as we check the tree
  private boolean _validityChecked = false;

  /**
   * First set when step was created and answer generated, size stored in DB. So can use this to show step
   * size without pulling records from cache. Size == -1 means must rerun step (and recache results), get size
   * and store in step table. Can be out of date for releases since we don't rerun strategies on release.
   * Should be reset on step table every time we rerun step. EstimateSize is set to -1 when step is revised
   * (in place)- also all steps affected by this step are also changed to -1 so the values are set when those
   * steps are rerun (i.e. value of -1 means step is "dirty" (modified but not run))
   */
  private int _estimateSize = -1;

  // LEGACY!!  Any filtering code mods should be applied to the parameterized
  //     filter framework.  TODO: remove this code and migrade the DB
  // Name of (non-parameterized) filter instance applied to this step (if any), DB value of null = no filter
  // if any filters exist on a recordclass, model must have a "default" filter; usually this is
  // a filter that simply returns all the results. The default filter is automatically applied to a step.
  // This affects the UI- if no filter OR the default filter is applied, the filter icon does not appear
  private String _filterName;

  // stores answer values for this step object and manages reuse of those objects
  private AnswerValueCache _answerValueCache;

  // Map of param name (without set name) to stable value (always a string), which are:
  // StringParam: unquoted raw value
  // TimestampParam: millisecs since 1970 (or whatever)
  // DatasetParam: Dataset ID (PK int column in Datasets table in apicomm)
  // AbstractEnumParam: unsorted string representation of term list (comma-delimited)
  // EnumParam: (inherited)
  // FlatVocabParam: (inherited)
  // FilterParam: JSON string representing all filters applied (see FilterParam)
  // AnswerParam: Step ID
  private Map<String, String> _paramValues = new LinkedHashMap<String, String>();

  // filters applied to this step
  private FilterOptionList _filterOptions;

  // view filters applied to this step
  private FilterOptionList _viewFilterOptions;

  // only applied to leaf steps, user-defined
  // during booleans, weights of records are modified (per boolean-specific logic, see BooleanQuery)
  private int _assignedWeight;

  // in DB, for those steps unloaded (i.e. previous and child steps are lazy loaded)
  private long _previousStepId;
  private long _childStepId;

  // This value may or may not be used by the UI, but it is not changed. isRevisable always returns true
  private boolean _revisable = true;

  // Set if exception occurs during step loading (but we don't want to bubble the exception up)
  // This allows the UI to show a "broken" step but not hose the whole strategy
  private Exception _exception;

  private Long _strategyId;

  // Set this if this step should not be written to /read from db.  A hack in support of
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
  public Step(StepFactory stepFactory, long userId, long stepId) throws WdkModelException {
    _stepFactory = stepFactory;
    _user = null;
    _userId = userId;
    _stepId = stepId;
    _answerValueCache = new AnswerValueCache(this);
    _isDeleted = false;
    _assignedWeight = 0;
    _objectCreationDate = System.currentTimeMillis();
  }

  /**
   * Creates a step object for the given user and step ID.
   * 
   * @param stepFactory
   *          step factory that generated this step
   * @param user
   *          owner of this step
   * @param stepId
   *          id of the step
   * @throws WdkModelException 
   * @throws NullPointerException
   *           if user is null
   */
  public Step(StepFactory stepFactory, User user, long stepId) throws WdkModelException {
    _stepFactory = stepFactory;
    _user = user;
    _userId = user.getUserId();
    _stepId = stepId;
    _answerValueCache = new AnswerValueCache(this);
    _isDeleted = false;
    _assignedWeight = 0;
    _objectCreationDate = System.currentTimeMillis();
  }

  /**
   * Constructor that takes an existing step and makes a shallow copy of the
   * existing private fields.
   * 
   * @param step Step to make a shallow copy of
   * @throws WdkModelException 
   */
  public Step(Step step) throws WdkModelException {
    _stepFactory = step._stepFactory;
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
    _questionName = step._questionName;
    _nextStep = step._nextStep;
    _previousStep = step._previousStep;
    _parentStep = step._parentStep;
    _childStep = step._childStep;
    _booleanExpression = step._booleanExpression;
    _valid = step._valid;
    _validityChecked = step._validityChecked;
    _estimateSize = step._estimateSize;
    _filterName = step._filterName;
    _paramValues = step._paramValues;
    _filterOptions = step._filterOptions;
    _viewFilterOptions = step._viewFilterOptions;
    _assignedWeight = step._assignedWeight;
    _previousStepId = step._previousStepId;
    _childStepId = step._childStepId;
    _revisable = step._revisable;
    _exception = step._exception;
    _strategyId = step._strategyId;
    _inMemoryOnly = step._inMemoryOnly;
    _objectCreationDate = step._objectCreationDate;

    // answer value cache copy is NOT shallow- if caller wants a new step, they are
    // probably going to modify it to get a different answer value
    _answerValueCache = new AnswerValueCache(this);
  }

  public Step getPreviousStep() throws WdkModelException {
    if (_previousStep == null && _previousStepId != 0)
      setPreviousStep(_stepFactory.loadStep(getUser(), _previousStepId));
    return _previousStep;
  }

  public Step getNextStep() {
    return _nextStep;
  }

  public Step getParentStep() {
    return _parentStep;
  }

  public Step getParentOrNextStep() {
    return (_nextStep != null) ? _nextStep : _parentStep;
  }

  public Step getChildStep() throws WdkModelException {
    if (_childStep == null && _childStepId != 0)
      setChildStep(_stepFactory.loadStep(getUser(), _childStepId));
    return _childStep;
  }

  public int getAnswerParamCount() {
    try {
      return getQuestion().getQuery().getAnswerParamCount();
    }
    catch (WdkModelException ex) {
      return 0;
    }
  }

  /** 
   * Get the real result size from the answerValue.  AnswerValue is
   * responsible for caching, if any
   */
  public int getResultSize() throws WdkModelException, WdkUserException {
    _estimateSize = getAnswerValue().getResultSizeFactory().getDisplayResultSize();
    return _estimateSize;
  }

  // Needs to be updated for transforms
  public String getOperation() throws WdkModelException, WdkUserException {
    if (isFirstStep()) {
      throw new WdkUserException("getOperation cannot be called on the first Step.");
    }
    BooleanQuery query = (BooleanQuery) getQuestion().getQuery();
    StringParam operator = query.getOperatorParam();
    return _paramValues.get(operator.getName());
  }

  public void setParentStep(Step parentStep) throws WdkModelException {
    _stepFactory.verifySameOwnerAndProject(this, parentStep);
    _parentStep = parentStep;
    if (parentStep != null) {
      parentStep._childStep = this;
      parentStep._childStepId = _stepId;
    }
  }

  public void setChildStep(Step childStep) throws WdkModelException {
    _stepFactory.verifySameOwnerAndProject(this, childStep);
    _childStep = childStep;
    if (childStep != null) {
      childStep._parentStep = this;
      _childStepId = childStep.getStepId();

      // also update the param value
      String paramName = getChildStepParamName();
      _paramValues.put(paramName, Long.toString(_childStepId));
    }
    else
      _childStepId = 0;
  }

  public void setNextStep(Step nextStep) throws WdkModelException {
    _stepFactory.verifySameOwnerAndProject(this, nextStep);
    _nextStep = nextStep;
    if (nextStep != null) {
      nextStep._previousStep = this;
      nextStep._previousStepId = _stepId;
    }
  }

  public void setPreviousStep(Step previousStep) throws WdkModelException {
    _stepFactory.verifySameOwnerAndProject(this, previousStep);
    _previousStep = previousStep;
    if (previousStep != null) {
      previousStep._nextStep = this;
      _previousStepId = previousStep.getStepId();

      String paramName = getPreviousStepParamName();
      _paramValues.put(paramName, Long.toString(_previousStepId));
    }
    else
      _previousStepId = 0;
  }

  public boolean isFirstStep() throws WdkModelException {
    return (null == getPreviousStepParam());
  }

  public User getUser() throws WdkModelException {
    if (_user == null) {
      // if constructed with only the user id, lazy-load User object
      _user = _stepFactory.getWdkModel().getUserFactory().getUserById(_userId);
    }
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
    if (name == null || name.length() == 0) {
      try {
        name = getQuestion().getShortDisplayName();
      }
      catch (WdkModelException ex) {
        name = null;
      }
    }
    if (name == null)
      name = getQuestionName();
    if (name != null) {
      // remove script injections
      name = name.replaceAll("<.+?>", " ");
      name = name.replaceAll("[\"]", " ");
      name = name.trim().replaceAll("\\s+", " ");
      if (name.length() > 4000)
        name = name.substring(0, 4000);
    }
    return name;
  }

  /**
   * @return Returns the custom name, if it is set. Otherwise, returns the short display name for the
   *         underlying question.
   */
  public String getShortDisplayName() {
    /*
     * String name = customName;
     * 
     * if (name == null) name = getQuestion().getShortDisplayName(); if (name != null) { // remove script
     * injections name = name.replaceAll("<.+?>", " "); name = name.replaceAll("['\"]", " "); name =
     * name.trim().replaceAll("\\s+", " "); if (name.length() > 4000) name = name.substring(0, 4000); } return
     * name;
     */
    try {
      return getQuestion().getShortDisplayName();
    }
    catch (WdkModelException ex) {
      return getDisplayName();
    }
  }

  public String getDisplayName() {
    try {
      return getQuestion().getDisplayName();
    }
    catch (WdkModelException ex) {
      return (_customName != null) ? _customName : _questionName;
    }
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
   * Basic getter than just returns the current value for this field without checks,
   * lazy loading, or side effects (e.g., database updates)
   * @return
   */
  public int getRawEstimateSize() {
	return _estimateSize;
  }
  
  /**
   * Calculate the estimate size
   * @return
   */
  public int calculateEstimateSize() {
    try {
      if (!isValid()) {
        return RESET_SIZE_FLAG;
      }
      if (_estimateSize == RESET_SIZE_FLAG) {
        // The flag indicates if the size has been reset, and need to be calculated again.
        try {
          _estimateSize = getResultSize();
          this.update(true);
        }
        catch (ParamValuesInvalidException e) {
          // means we have invalid param values in this Step; invalidate locally
          // TODO: experimented with writing this value to DB here but that causes errors if bad params
          //   are entered during Add Step.  Figure out why and maybe add back
          //invalidateStep();
          _valid = false;
          _validityChecked = true;
          return 0;
        }
      }
      return _estimateSize;
    }
    catch (Exception e) {
      // do not throw error in this method, just return 0, to avoid infinite
      // loop from frontend. (otherwise frontend will keep trying showStrategy.do when
      // it sees a -1;
    	  // Used by service and NOT by struts
      LOG.error("Error occurred, use the old estimate size", e);
      return -1;
    }
  }

  /**
   * @return Size estimate of this step's result
   */
  public int getEstimateSize() {
    try {
      if (!isValid()) {
        return 0;
      }
      if (_estimateSize == RESET_SIZE_FLAG) {
        // The flag indicates if the size has been reset, and need to be calculated again.
        try {
          _estimateSize = getResultSize();
        }
        catch (ParamValuesInvalidException e) {
          // means we have invalid param values in this Step; invalidate locally
          // TODO: experimented with writing this value to DB here but that causes errors if bad params
          //   are entered during Add Step.  Figure out why and maybe add back
          //invalidateStep();
          _valid = false;
          _validityChecked = true;
          return 0;
        }
      }
      return _estimateSize;
    }
    catch (Exception e) {
      // do not throw error in this method, just return 0, to avoid infinite
      // loop from frontend. (otherwise frontend will keep trying showStrategy.do when
      // it sees a -1;
      LOG.error("Error occurred, use the old estimate size", e);
      return 0;
    }
  }

  /**
   * @param estimateSize
   *          The estimateSize to set.
   */
  public void setEstimateSize(int estimateSize) {
    _estimateSize = estimateSize;
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

  /**
   * A combined step can take one or more steps as input. a Transform is a special case of combined step, and
   * a boolean is another special case.
   * 
   * @return a flag to determine if a step can take other step(s) as input.
   */
  public boolean isCombined() {
    try {
      return getQuestion().isCombined();
    }
    catch (WdkModelException ex) {
      return false;
    }
  }

  /**
   * A transform step can take exactly one step as input.
   * 
   * @return Returns whether this Step is a transform
   */
  public boolean isTransform() {
    try {
      return getQuestion().isTransform();
    }
    catch (WdkModelException ex) {
      return false;
    }
  }

  /**
   * @return Returns the booleanExpression.
   */
  public String getBooleanExpression() {
    return _booleanExpression;
  }

  /**
   * @param booleanExpression
   *          The booleanExpression to set.
   */
  public void setBooleanExpression(String booleanExpression) {
    _booleanExpression = booleanExpression;
  }

  // saves attributes of the step that do NOT impact results or parent steps
  public void update(boolean updateTime) throws WdkModelException {
    // HACK: don't update if this is an in-memory only Step
    // remove this once we refactor the world of summary views, so they don't need such Steps
    if (_inMemoryOnly) return;
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
      @Override public boolean test(Long candidateStepId) {
        // keep unless id is for this step
        return (getStepId() != candidateStepId.longValue());
      }
    });

    // alert listeners that this step has been revised and await results
    Events.triggerAndWait(new StepRevisedEvent(this, unmodifiedVersion), new WdkModelException(
        "Unable to process all StepRevised events for revised step " + getStepId()));

    // alert listeners that the step results have changed for these steps and wait for completion
    Events.triggerAndWait(new StepResultsModifiedEvent(stepIds), new WdkModelException(
        "Unable to process all StepResultsModified events for step IDs: " +
            FormatUtil.arrayToString(stepIds.toArray())));

    // refresh in-memory step here in case listeners also modified it
    refreshParamFilters();
  }

  /**
   * Refreshes some key fields of this step with the current values in the DB.  This
   * is to support outside modification of the step by event listeners.  If a listener
   * modifies the step in response to a change we made, we will want to reflect these
   * secondary changes in this current execution flow.
   * 
   * @throws WdkModelException if unable to load updated step
   */
  private void refreshParamFilters() throws WdkModelException {
    Step step = _stepFactory.getStepById(getStepId());
    _filterName = step._filterName;
    _paramValues = step._paramValues;
    _filterOptions = step._filterOptions;
    _viewFilterOptions = step._viewFilterOptions;
    _objectCreationDate = step._objectCreationDate;
  }

  public String getDescription() {
    try {
      return getQuestion().getDescription();
    }
    catch (WdkModelException ex) {
      return null;
    }
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
  public boolean isValid() throws WdkModelException {
    if (!_valid || _validityChecked) {
      // check 1: return; later, revise can turn step from invalid -> valid
      // check 2: if value is memoized, do not check steps and params again
      return _valid;
    }

    // check stored param names against those in Question
    Question question = null;
    try {
      question = getQuestion();
    }
    catch (WdkModelException e) {
      LOG.warn("Found step [" + _stepId + "] with invalid question " + _questionName + ". Invalidating in DB.");
      invalidateStep();
      return false;
    }

    Map<String, Param> params = question.getParamMap();
    for (String paramName : _paramValues.keySet()) {
      if (!params.containsKey(paramName)) {
        LOG.error("Unable to find all stored param names in Question " + "(bad param name = " + paramName +
            ").  Setting valid to false.");
        invalidateStep();
        return false;
      }
    }

    // check previous and child steps if still valid
    Step myPrevStep, myChildStep;
    if (_valid &&
        (((myPrevStep = getPreviousStep()) != null && !myPrevStep.isValid()) ||
         ((myChildStep = getChildStep()) != null && !myChildStep.isValid()))) {
      invalidateStep();
      return false;
    }

    _validityChecked = true;
    return true;
  }

  /**
   * Sets valid value to false and sends change to the DB
   * 
   * @throws WdkModelException
   *           if unable to update DB
   */
  public void invalidateStep() throws WdkModelException {
    setValid(false);
    _validityChecked = true;
    if (!_inMemoryOnly) {
     _stepFactory.setStepValidFlag(this);
    }
  }

  /**
   * @param isValid
   *          the isValid to set
   */
  public void setValid(boolean valid) {
    _valid = valid;
  }

  public Map<String, String> getParamNames() throws WdkModelException {
    Map<String, Param> params = getQuestion().getQuery().getParamMap();
    Map<String, String> names = new LinkedHashMap<String, String>();
    for (Param param : params.values()) {
      names.put(param.getName(), param.getPrompt());
    }
    return names;
  }

  public String getQuestionName() {
    return _questionName;
  }

  public void setQuestionName(String questionName) {
    _questionName = questionName;
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

  /**
   * @return the paramErrors
   */
  public Map<String, String> getParamValues() {
    return new LinkedHashMap<String, String>(_paramValues);
  }

  /**
   * @param paramValues
   *          the paramValues to set
   */
  public void setParamValues(Map<String, String> paramValues) {
    if (paramValues == null)
      paramValues = new LinkedHashMap<>();
    _paramValues = new LinkedHashMap<String, String>(paramValues);
  }

  public FilterOptionList getFilterOptions() {
    if (_filterOptions == null) {
      _filterOptions = new FilterOptionList(_stepFactory.getWdkModel(), _questionName);
    }
    return _filterOptions;
  }

  public FilterOptionList getViewFilterOptions() {
    if (_viewFilterOptions == null) {
      _viewFilterOptions = new FilterOptionList(_stepFactory.getWdkModel(), _questionName);
    }
    return _viewFilterOptions;
  }

  public void setFilterOptions(FilterOptionList filterOptions) throws WdkModelException {
    validateFilterOptions(filterOptions, false);
    _filterOptions = filterOptions;
    _answerValueCache.invalidateAll();
  }

  public void setViewFilterOptions(FilterOptionList filterOptions) throws WdkModelException {
    validateFilterOptions(filterOptions, true);
    _viewFilterOptions = filterOptions;
    _answerValueCache.invalidateViewAnswers();
  }

  // we need to pass the disabled property
  public void addFilterOption(String filterName, JSONObject filterValue, boolean is_disabled) throws WdkModelException {
    getFilterOptions().addFilterOption(filterName, filterValue, is_disabled);
    validateFilterOptions(getViewFilterOptions(), false);
  }

  public void addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    getFilterOptions().addFilterOption(filterName, filterValue);
    validateFilterOptions(getViewFilterOptions(), false);
  }


  public void removeFilterOption(String filterName) {
    getFilterOptions().removeFilterOption(filterName);
  }

  public void addViewFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    getViewFilterOptions().addFilterOption(filterName, filterValue);
    validateFilterOptions(getViewFilterOptions(), true);
  }

  public void removeViewFilterOption(String filterName) {
    getViewFilterOptions().removeFilterOption(filterName);
  }

  public void setParamValue(String paramName, String paramValue) {
    _paramValues.put(paramName, paramValue);
  }

  public RecordClass getRecordClass() throws WdkModelException {
    return getQuestion().getRecordClass();
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
    RecordClass recordClass = getQuestion().getRecordClass();
    AnswerFilterInstance filter = recordClass.getFilterInstance(filterName);
    return createStep(filter, assignedWeight);
  }

  public Step createStep(AnswerFilterInstance filter, int assignedWeight) throws WdkModelException {
    AnswerFilterInstance oldFilter = getFilter();
    if (filter == null && oldFilter == null && _assignedWeight == assignedWeight)
      return this;
    if (filter != null && oldFilter != null && filter.getName().equals(oldFilter.getName()) &&
        _assignedWeight == assignedWeight)
      return this;

    // create new steps
    Question question = getQuestion();
    Map<String, String> params = getParamValues();
    Step step = StepUtilities.createStep(_user, _strategyId, question, params, filter, _isDeleted, false,
        assignedWeight, getFilterOptions());
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
      step = StepUtilities.createStep(_user, strategyId, getQuestion(), _paramValues,
          getFilter(), _isDeleted, false, _assignedWeight, _filterOptions);
    }
    else {
      Question question = getQuestion();
      Map<String, String> paramValues = new LinkedHashMap<String, String>();
      Map<String, Param> params = question.getParamMap();
      for (String paramName : _paramValues.keySet()) {
        Param param = params.get(paramName);
        String paramValue = _paramValues.get(paramName);
        if (param instanceof AnswerParam) {
          Step child = StepUtilities.getStep(getUser(), Integer.parseInt(paramValue));
          child = child.deepClone(strategyId, stepIdMap);
          paramValue = Long.toString(child.getStepId());
        }
        paramValues.put(paramName, paramValue);
      }
      step = StepUtilities.createStep(getUser(), strategyId, question, paramValues,
          getFilter(), _isDeleted, false, _assignedWeight, getFilterOptions());
    }

    stepIdMap.put(getStepId(), step.getStepId());

    step._collapsedName = _collapsedName;
    step._customName = _customName;
    step._collapsible = _collapsible;
    step.update(false);

    Events.triggerAndWait(new StepCopiedEvent(this, step), new WdkModelException(
        "Unable to execute all operations subsequent to step copy."));

    return step;
  }

  public boolean isFiltered() throws WdkModelException {
    // first check if new filter has been applied
    if (_filterOptions != null && _filterOptions.isFiltered(this))
      return true;

    AnswerFilterInstance filter = getFilter();
    if (filter == null)
      return false;

    Question question;
    try {
      question = getQuestion();
    }
    catch (WdkModelException ex) {
      return false;
    }
    RecordClass recordClass = question.getRecordClass();
    AnswerFilterInstance defaultFilter = recordClass.getDefaultFilter();
    if (defaultFilter == null)
      return true;

    return (!defaultFilter.getName().equals(filter.getName()));
  }

  public String getFilterDisplayName() {
    AnswerFilterInstance filter = getFilter();
    return (filter != null) ? filter.getDisplayName() : _filterName;
  }

  public Step getFirstStep() throws WdkModelException {
    Step step = this;
    while (step.getPreviousStep() != null)
      step = step.getPreviousStep();
    return step;
  }

  public boolean isBoolean() {
    try {
      return getQuestion().isBoolean();
    }
    catch (WdkModelException ex) {
      return false;
    }
  }

  public JSONObject getJSONContent(int strategyId) throws WdkModelException {
    return getJSONContent(strategyId, false);
  }

  public JSONObject getJSONContent(long strategyId, boolean forChecksum) throws WdkModelException {

    JSONObject jsStep = new JSONObject();

    try {
      jsStep.put("id", _stepId);
      jsStep.put("customName", _customName);
      jsStep.put("question", _questionName);
      jsStep.put("projectVersion", _projectVersion);
      jsStep.put("filter", _filterName);
      jsStep.put("collapsed", this.isCollapsible());
      jsStep.put("collapsedName", this.getCollapsedName());
      jsStep.put("deleted", _isDeleted);
      jsStep.put(KEY_PARAMS, getParamsJSON());
      jsStep.put(KEY_FILTERS, getFilterOptionsJSON());

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

  public Question getQuestion() throws WdkModelException {
    return _stepFactory.getWdkModel().getQuestion(_questionName);
  }

  public AnswerFilterInstance getFilter() {
    try {
      return getQuestion().getRecordClass().getFilterInstance(_filterName);
    }
    catch (WdkModelException ex) {
      return null;
    }
  }

  public String getFilterName() {
    return _filterName;
  }

  public void setFilterName(String filterName) {
    _filterName = filterName;
  }

  public AnswerValue getAnswerValue() throws WdkModelException, WdkUserException {
    return _answerValueCache.getAnswerValue(true);
  }

  public AnswerValue getViewAnswerValue() throws WdkModelException, WdkUserException {
    return _answerValueCache.getViewAnswerValue(true);
  }

  public AnswerValue getAnswerValue(boolean validate) throws WdkModelException, WdkUserException {
    return _answerValueCache.getAnswerValue(validate);
  }

  public void resetAnswerValue() {
    _answerValueCache.invalidateAll();
  }

  /**
   * @return the assignedWeight
   */
  public int getAssignedWeight() {
    return _assignedWeight;
  }

  /**
   * @param assignedWeight
   *          the assignedWeight to set
   */
  public void setAssignedWeight(int assignedWeight) {
    _assignedWeight = assignedWeight;
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
    _stepFactory.verifySameOwnerAndProject(this, previousStepId);
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
    _stepFactory.verifySameOwnerAndProject(this, childStepId);
    setChildStepId(childStepId);
  }

  public boolean isRevisable() {
    return _revisable;
  }

  /**
   * Get the answerParam that take the previousStep as input, which is the first answerParam in the param
   * list.
   * 
   * @return an AnswerParam
   * @throws WdkModelException
   */
  public AnswerParam getPreviousStepParam() throws WdkModelException {
    Param[] params = getQuestion().getParams();
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

  public AnswerParam getChildStepParam() throws WdkModelException {
    Param[] params = getQuestion().getParams();
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
    return _stepFactory.getWdkModel().getStepAnalysisFactory().hasCompleteAnalyses(this);
  }

  public JSONObject getParamFilterJSON() {
    JSONObject jsContent = new JSONObject();
    jsContent.put(KEY_PARAMS, getParamsJSON());
    jsContent.put(KEY_FILTERS, getFilterOptionsJSON());
    jsContent.put(KEY_VIEW_FILTERS, getViewFilterOptionsJSON());
    return jsContent;
  }

  public void setParamFilterJSON(JSONObject jsContent) throws WdkModelException {

    // handle no params, no filters
    if (jsContent == null) {
      setParamsJSON(null);
      setFilterOptionsJSON(null);
      setViewFilterOptionsJSON(null);
      return;
    }

    //logger.debug("Parsing json:\n" + jsContent.toString(2));

    // legacy records: if no "params" property, assume JSON represents only params
    if (!jsContent.has(KEY_PARAMS)) {
      setParamsJSON(jsContent);
      setFilterOptionsJSON(null);
      setViewFilterOptionsJSON(null);
      return;
    }

    // assume up-to-date layout
    try {
      setParamsJSON(jsContent.getJSONObject(KEY_PARAMS));
    }
    catch (JSONException e) {
      throw new WdkModelException("Params property value is not a JSON Object", e);
    }

    //logger.debug("**********setting filters for step:");
    setFilterOptionsJSON(getFilterArrayOrNull(jsContent, KEY_FILTERS));
    //logger.debug("**********setting VIEW filters for step:");
    setViewFilterOptionsJSON(getFilterArrayOrNull(jsContent, KEY_VIEW_FILTERS));
  }

  private static JSONArray getFilterArrayOrNull(JSONObject jsContent, String propKey) throws WdkModelException {
    if (jsContent.has(propKey)) {
      // FIXME: Some steps in the DB have a filters property with a JSON object (usually empty);
      //    Ignore these for now to avoid errors; they will eventually be overwritten or die quietly
      try {
        jsContent.getJSONObject(propKey);
        // if successful, ignore!
        return null;
      }
      catch (JSONException e) {
        // not an object; hopefully it is an array (parsed below)
      }

      // proper value is an array of filter objects
      try {
        return jsContent.getJSONArray(propKey);
      }
      catch (JSONException e) {
        throw new WdkModelException(propKey + " property value is not a JSON Array", e);
      }
    }
    return null;
  }

  public JSONObject getParamsJSON() {
    // convert params
    JSONObject jsParams = new JSONObject();
    for (String paramName : _paramValues.keySet()) {
      jsParams.put(paramName, _paramValues.get(paramName));
    }
    return jsParams;
  }

  void setParamsJSON(JSONObject jsParams) throws WdkModelException {
    _paramValues = new LinkedHashMap<String, String>();
    if (jsParams != null) {
      try {
        // read params;
        String[] paramNames = JSONObject.getNames(jsParams);
        if (paramNames != null) {
          for (String paramName : paramNames) {
            String paramValue = jsParams.getString(paramName);
            LOG.trace("param '" + paramName + "' = '" + paramValue + "'");
            _paramValues.put(paramName, paramValue);
          }
        }
      }
      catch (JSONException ex) {
        throw new WdkModelException(ex);
      }
    }
  }

  public JSONArray getFilterOptionsJSON() {
    return (_filterOptions == null) ? new JSONArray() : _filterOptions.getJSON();
  }

  public void setFilterOptionsJSON(JSONArray jsOptions) throws WdkModelException {
    // getQuestion() is null when we come from a newly created step from StepExpander in apicomm maint
    if (jsOptions == null || _questionName == null ) {
      _filterOptions = null;
    }
    else {
      FilterOptionList newList = new FilterOptionList(_stepFactory.getWdkModel(), _questionName, jsOptions);
      validateFilterOptions(newList, false);
      _filterOptions = newList;
    }
  }

  public JSONArray getViewFilterOptionsJSON() {
    return (_viewFilterOptions == null) ? new JSONArray() : _viewFilterOptions.getJSON();
  }

  public void setViewFilterOptionsJSON(JSONArray jsOptions) throws WdkModelException {
    if (jsOptions == null || _questionName == null) {
      _viewFilterOptions = null;
    }
    else {
      FilterOptionList newList = new FilterOptionList(_stepFactory.getWdkModel(), _questionName, jsOptions);
      validateFilterOptions(newList, true);
      _viewFilterOptions = newList;
    }
  }

  private void validateFilterOptions(FilterOptionList optionList, boolean desiredViewOnlyFlag) throws WdkModelException {
    if (optionList == null) return;
    for (FilterOption filter : optionList) {
      if (filter.getFilter().getIsViewOnly() != desiredViewOnlyFlag) {
        String viewOnlyString = (desiredViewOnlyFlag ? "view-only" : "regular (non-view-only)");
        throw new WdkModelException("Cannot set Filter '" + filter.getFilter().getKey() + "' as a " + viewOnlyString + " filter.");
      }
    }
  }

  public String getType() throws WdkModelException {
    return getRecordClass().getFullName();
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
  
  public boolean hasAnswerParams() throws WdkModelException {
    for(Param param : getQuestion().getParams()) {
    	  if(param instanceof AnswerParam) return true;
    }
    return false;
  }
  
  public boolean isAnswerSpecComplete() throws WdkModelException {
    return hasAnswerParams() ? _strategyId != null : true;
  }

  public boolean hasValidQuestion() {
    try {
      getQuestion();
      return true;
    }
    catch (WdkModelException e) {
      return false;
    }
  }
}
