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
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepsModifiedEvent;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.filter.FilterOption;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.BooleanQuery;
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

  public static final int RESET_SIZE_FLAG = -1;
  
  private static final String KEY_PARAMS = "params";
  private static final String KEY_FILTERS = "filters";
  private static final String KEY_VIEW_FILTERS = "viewFilters";

  private static final Logger logger = Logger.getLogger(Step.class);

  // injected during Step object creation
  private final StepFactory stepFactory;
  // lazy loaded, but every step has a user
  private User user;
  // in DB, owning user id
  private int userId;
  // in DB, Primary key
  private int stepId;
  // in DB, set during step creation
  private Date createdTime;
  // in DB, last time Answer generated, written to DB each time
  private Date lastRunTime;
  // in DB, set by user
  private String customName;
  // in DB, for soft delete
  private boolean deleted = false;

  // nested step
  private boolean collapsible = false;
  private String collapsedName = null;

  private String projectId;
  private String projectVersion;
  private String questionName;

  // Steps within the "main branch" strategy flow
  // Next Step must be combined step (e.g. transform or boolean/span-logic (two-answer function))
  private Step nextStep = null;
  // Previous step could be first step (=leaf), or combined step
  private Step previousStep = null;

  // Parent step must be a boolean
  private Step parentStep = null;
  // Child can be a "normal" leaf, or collapsible version of leaf, boolean, or transform
  private Step childStep = null;

  // Probably should be marked deprecated
  // Jerric says can be retrieved from param values
  private String booleanExpression;

  // in DB, set during maintenance (true or null = valid, false = invalid)
  private boolean valid = true;
  // set during runtime so we don't recheck validity over and over as we check the tree
  private boolean validityChecked = false;

  /**
   * First set when step was created and answer generated, size stored in DB. So can use this to show step
   * size without pulling records from cache. Size == -1 means must rerun step (and recache results), get size
   * and store in step table. Can be out of date for releases since we don't rerun strategies on release.
   * Should be reset on step table every time we rerun step. EstimateSize is set to -1 when step is revised
   * (in place)- also all steps affected by this step are also changed to -1 so the values are set when those
   * steps are rerun (i.e. value of -1 means step is "dirty" (modified but not run))
   */
  private int estimateSize = 0;

  // LEGACY!!  Any filtering code mods should be applied to the parameterized
  //     filter framework.  TODO: remove this code and migrade the DB
  // Name of (non-parameterized) filter instance applied to this step (if any), DB value of null = no filter
  // if any filters exist on a recordclass, model must have a "default" filter; usually this is
  // a filter that simply returns all the results. The default filter is automatically applied to a step.
  // This affects the UI- if no filter OR the default filter is applied, the filter icon does not appear
  private String filterName;

  // stores answer values for this step object and manages reuse of those objects
  private AnswerValueCache answerValueCache;

  // Map of param name (without set name) to stable value (always a string), which are:
  // StringParam: unquoted raw value
  // TimestampParam: millisecs since 1970 (or whatever)
  // DatasetParam: Dataset ID (PK int column in Datasets table in apicomm)
  // AbstractEnumParam: unsorted string representation of term list (comma-delimited)
  // EnumParam: (inherited)
  // FlatVocabParam: (inherited)
  // FilterParam: JSON string representing all filters applied (see FilterParam)
  // AnswerParam: Step ID
  private Map<String, String> paramValues = new LinkedHashMap<String, String>();

  // filters applied to this step
  private FilterOptionList filterOptions;

  // view filters applied to this step
  private FilterOptionList viewFilterOptions;

  // only applied to leaf steps, user-defined
  // during booleans, weights of records are modified (per boolean-specific logic, see BooleanQuery)
  private int assignedWeight;

  // in DB, for those steps unloaded (i.e. previous and child steps are lazy loaded)
  private int previousStepId;
  private int childStepId;

  // This value may or may not be used by the UI, but it is not changed. isRevisable always returns true
  private boolean revisable = true;

  // Set if exception occurs during step loading (but we don't want to bubble the exception up)
  // This allows the UI to show a "broken" step but not hose the whole strategy
  private Exception exception;

  private Integer strategyId;

  // Set this if this step should not be written to /read from db.  A hack in support of
  // summary views, until they are refactored using service.
  private boolean inMemoryOnly = false;

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
  public Step(StepFactory stepFactory, int userId, int stepId) throws WdkModelException {
    this.stepFactory = stepFactory;
    this.user = null;
    this.userId = userId;
    this.stepId = stepId;
    this.answerValueCache = new AnswerValueCache(this);
    deleted = false;
    assignedWeight = 0;
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
  public Step(StepFactory stepFactory, User user, int stepId) throws WdkModelException {
    this.stepFactory = stepFactory;
    this.user = user;
    this.userId = user.getUserId();
    this.stepId = stepId;
    this.answerValueCache = new AnswerValueCache(this);
    deleted = false;
    assignedWeight = 0;
  }

  /**
   * Constructor that takes an existing step and makes a shallow copy of the
   * existing private fields.
   * 
   * @param step Step to make a shallow copy of
   * @throws WdkModelException 
   */
  public Step(Step step) throws WdkModelException {
    stepFactory = step.stepFactory;
    user = step.user;
    userId = step.userId;
    stepId = step.stepId;
    createdTime = step.createdTime;
    lastRunTime = step.lastRunTime;
    customName = step.customName;
    deleted = step.deleted;
    collapsible = step.collapsible;
    collapsedName = step.collapsedName;
    projectId = step.projectId;
    projectVersion = step.projectVersion;
    questionName = step.questionName;
    nextStep = step.nextStep;
    previousStep = step.previousStep;
    parentStep = step.parentStep;
    childStep = step.childStep;
    booleanExpression = step.booleanExpression;
    valid = step.valid;
    validityChecked = step.validityChecked;
    estimateSize = step.estimateSize;
    filterName = step.filterName;
    paramValues = step.paramValues;
    filterOptions = step.filterOptions;
    viewFilterOptions = step.viewFilterOptions;
    assignedWeight = step.assignedWeight;
    previousStepId = step.previousStepId;
    childStepId = step.childStepId;
    revisable = step.revisable;
    exception = step.exception;
    strategyId = step.strategyId;
    inMemoryOnly = step.inMemoryOnly;

    // answer value cache copy is NOT shallow- if caller wants a new step, they are
    // probably going to modify it to get a different answer value
    this.answerValueCache = new AnswerValueCache(this);
  }

  public Step getPreviousStep() throws WdkModelException {
    if (previousStep == null && previousStepId != 0)
      setPreviousStep(stepFactory.loadStep(getUser(), previousStepId));
    return previousStep;
  }

  public Step getNextStep() {
    return nextStep;
  }

  public Step getParentStep() {
    return parentStep;
  }

  public Step getParentOrNextStep() {
    return (nextStep != null) ? nextStep : parentStep;
  }

  public Step getChildStep() throws WdkModelException {
    if (childStep == null && childStepId != 0)
      setChildStep(stepFactory.loadStep(getUser(), childStepId));
    return childStep;
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
	estimateSize = getAnswerValue().getDisplayResultSize();
	return estimateSize;
    }

  // Needs to be updated for transforms
  public String getOperation() throws WdkModelException, WdkUserException {
    if (isFirstStep()) {
      throw new WdkUserException("getOperation cannot be called on the first Step.");
    }
    BooleanQuery query = (BooleanQuery) getQuestion().getQuery();
    StringParam operator = query.getOperatorParam();
    return this.paramValues.get(operator.getName());
  }

  public void setParentStep(Step parentStep) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, parentStep);
    this.parentStep = parentStep;
    if (parentStep != null) {
      parentStep.childStep = this;
      parentStep.childStepId = stepId;
    }
  }

  public void setChildStep(Step childStep) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, childStep);
    this.childStep = childStep;
    if (childStep != null) {
      childStep.parentStep = this;
      childStepId = childStep.getStepId();

      // also update the param value
      String paramName = getChildStepParamName();
      paramValues.put(paramName, Integer.toString(childStepId));
    }
    else
      childStepId = 0;
  }

  public void setNextStep(Step nextStep) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, nextStep);
    this.nextStep = nextStep;
    if (nextStep != null) {
      nextStep.previousStep = this;
      nextStep.previousStepId = stepId;
    }
  }

  public void setPreviousStep(Step previousStep) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, previousStep);
    this.previousStep = previousStep;
    if (previousStep != null) {
      previousStep.nextStep = this;
      previousStepId = previousStep.getStepId();

      String paramName = getPreviousStepParamName();
      paramValues.put(paramName, Integer.toString(previousStepId));
    }
    else
      previousStepId = 0;
  }

  public boolean isFirstStep() throws WdkModelException {
    return (null == getPreviousStepParam());
  }

  public User getUser() throws WdkModelException {
    if (user == null) {
      // if constructed with only the user id, lazy-load User object
      user = stepFactory.getWdkModel().getUserFactory().getUser(userId);
    }
    return user;
  }

  /**
   * @return Returns the createTime.
   */
  public Date getCreatedTime() {
    return createdTime;
  }

  /**
   * @param createTime
   *          The createTime to set.
   */
  void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public String getBaseCustomName() {
    return customName;
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
    String name = customName;
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
      return (customName != null) ? customName : questionName;
    }
  }

  /**
   * @param customName
   *          The customName to set.
   */
  public void setCustomName(String customName) {
    this.customName = customName;
  }

  /**
   * @return Returns the stepId.
   */
  public int getStepId() {
    return stepId;
  }

  /**
   * @return Size estimate of this step's result
   */
  public int getEstimateSize() {
    if (estimateSize == RESET_SIZE_FLAG) {
      // The flag indicates if the size has been reset, and need to be calculated again.
      try {
	estimateSize = getResultSize();
      }
      catch (Exception ex) {
        // do not throw error in this method, just return 0, to avoid infinite
        // loop from frontend. (otherwise frontend will keep trying showStrategy.do when
        // it sees a -1;
        logger.error("Error occurred, use the old estimate size", ex);
        return 0;
      }
    }
    return estimateSize;
  }

  /**
   * @param estimateSize
   *          The estimateSize to set.
   */
  public void setEstimateSize(int estimateSize) {
    this.estimateSize = estimateSize;
  }

  /**
   * @return Returns the lastRunTime.
   */
  public Date getLastRunTime() {
    return lastRunTime;
  }

  /**
   * @param lastRunTime
   *          The lastRunTime to set.
   */
  public void setLastRunTime(Date lastRunTime) {
    this.lastRunTime = lastRunTime;
  }

  /**
   * A combined step can take one or more steps as input. a Transform is a special case of combined step, and
   * a boolean is another special case.
   * 
   * @return a flag to determine if a step can take other step(s) as input.
   */
  public boolean isCombined() {
    try {
      return getQuestion().getQuery().isCombined();
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
      return getQuestion().getQuery().isTransform();
    }
    catch (WdkModelException ex) {
      return false;
    }
  }

  /**
   * @return Returns the booleanExpression.
   */
  public String getBooleanExpression() {
    return booleanExpression;
  }

  /**
   * @param booleanExpression
   *          The booleanExpression to set.
   */
  public void setBooleanExpression(String booleanExpression) {
    this.booleanExpression = booleanExpression;
  }

  // saves attributes of the step that do NOT impact results or parent steps
  public void update(boolean updateTime) throws WdkModelException {
    // HACK: don't update if this is an in-memory only Step
    // remove this once we refactor the world of summary views, so they don't need such Steps
    if (inMemoryOnly) return;
    stepFactory.updateStep(getUser(), this, updateTime);
  }

  // saves param values AND filter values (AND step name and maybe other things)
  public void saveParamFilters() throws WdkModelException {

    stepFactory.saveStepParamFilters(this);
    stepFactory.resetStepCounts(this);

    // get list of steps dependent on this one; all their results are now invalid
    List<Integer> stepIds = stepFactory.getStepAndParents(getStepId());
    // invalidate step analysis tabs for this step and wait for completion
    Events.triggerAndWait(new StepsModifiedEvent(stepIds), new WdkModelException(
        "Unable to invalidate step IDs: " + FormatUtil.arrayToString(stepIds.toArray())));
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
    return deleted;
  }

  /**
   * @param isDeleted
   *          The isDeleted to set.
   */
  public void setDeleted(boolean isDeleted) {
    this.deleted = isDeleted;
  }

  public boolean isCollapsible() {
    if (collapsible)
      return true;
    // it is true if the step is a branch
    return (getParentStep() != null && isCombined());
  }

  public void setCollapsible(boolean isCollapsible) {
    this.collapsible = isCollapsible;
  }

  public String getCollapsedName() {
    if (collapsedName == null && isCollapsible())
      return getCustomName();
    return collapsedName;
  }

  public void setCollapsedName(String collapsedName) {
    this.collapsedName = collapsedName;
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
    if (!valid || validityChecked) {
      // check 1: return; later, revise can turn step from invalid -> valid
      // check 2: if value is memoized, do not check steps and params again
      return valid;
    }

    // check stored param names against those in Question
    Map<String, Param> params = getQuestion().getParamMap();
    for (String paramName : paramValues.keySet()) {
      if (!params.containsKey(paramName)) {
        logger.error("Unable to find all stored param names in Question " + "(bad param name = " + paramName +
            ").  Setting valid to false.");
        invalidateStep();
      }
    }

    // check previous and child steps if still valid
    Step prevStep, childStep;
    if (valid &&
        (((prevStep = getPreviousStep()) != null && !prevStep.isValid()) || ((childStep = getChildStep()) != null && !childStep.isValid()))) {
      invalidateStep();
    }

    // mark validity checked so we don't do the work above again
    validityChecked = true;
    return valid;
  }

  /**
   * Sets valid value to false and sends change to the DB
   * 
   * @throws WdkModelException
   *           if unable to update DB
   */
  public void invalidateStep() throws WdkModelException {
    setValid(false);
    stepFactory.setStepValidFlag(this);
  }

  /**
   * @param isValid
   *          the isValid to set
   */
  public void setValid(boolean valid) {
    this.valid = valid;
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
    return questionName;
  }

  public void setQuestionName(String questionName) {
    this.questionName = questionName;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getProjectVersion() {
    return projectVersion;
  }

  public void setProjectVersion(String projectVersion) {
    this.projectVersion = projectVersion;
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

  public Step getStepByDisplayId(int displayId) throws WdkModelException {
    Step target;
    if (this.stepId == displayId) {
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
    logger.debug("gettting step by child id. current=" + this + ", input=" + childId);
    Step target;
    if (this.childStepId == childId) {
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
    logger.debug("gettting step by prev id. current=" + this + ", input=" + previousId);
    Step target;
    if (this.previousStepId == previousId) {
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
    return new LinkedHashMap<String, String>(paramValues);
  }

  /**
   * @param paramValues
   *          the paramValues to set
   */
  public void setParamValues(Map<String, String> paramValues) {
    if (paramValues == null)
      paramValues = new LinkedHashMap<>();
    this.paramValues = new LinkedHashMap<String, String>(paramValues);
  }

  public FilterOptionList getFilterOptions() throws WdkModelException {
    if (filterOptions == null) {
      filterOptions = new FilterOptionList(getQuestion());
    }
    return filterOptions;
  }

  public FilterOptionList getViewFilterOptions() throws WdkModelException {
    if (viewFilterOptions == null) {
      viewFilterOptions = new FilterOptionList(getQuestion());
    }
    return viewFilterOptions;
  }

  public void setFilterOptions(FilterOptionList filterOptions) throws WdkModelException {
    validateFilterOptions(filterOptions, false);
    this.filterOptions = filterOptions;
    answerValueCache.invalidateAll();
  }

  public void setViewFilterOptions(FilterOptionList filterOptions) throws WdkModelException {
    validateFilterOptions(filterOptions, true);
    this.viewFilterOptions = filterOptions;
    answerValueCache.invalidateViewAnswers();
  }

  public void addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    getFilterOptions().addFilterOption(filterName, filterValue);
    validateFilterOptions(getViewFilterOptions(), false);
  }

  public void removeFilterOption(String filterName) throws WdkModelException {
    getFilterOptions().removeFilterOption(filterName);
  }

  public void addViewFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    getViewFilterOptions().addFilterOption(filterName, filterValue);
    validateFilterOptions(getViewFilterOptions(), true);
  }

  public void removeViewFilterOption(String filterName) throws WdkModelException {
    getViewFilterOptions().removeFilterOption(filterName);
  }

  public void setParamValue(String paramName, String paramValue) {
    paramValues.put(paramName, paramValue);
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
    if (filter == null && oldFilter == null && this.assignedWeight == assignedWeight)
      return this;
    if (filter != null && oldFilter != null && filter.getName().equals(oldFilter.getName()) &&
        this.assignedWeight == assignedWeight)
      return this;

    // create new steps
    Question question = getQuestion();
    Map<String, String> params = getParamValues();
    try {
      int startIndex = getAnswerValue().getStartIndex();
      int endIndex = getAnswerValue().getEndIndex();
      Step step = getUser().createStep(strategyId, question, params, filter, startIndex, endIndex, deleted, false,
          assignedWeight, getFilterOptions());
      step.collapsedName = collapsedName;
      step.customName = customName;
      step.collapsible = collapsible;
      step.update(false);
      return step;
    }
    catch (WdkUserException ex) {
      throw new WdkModelException(ex);
    }
  }

  /**
   * deep clone a step, the step will get a new id, and if the step contains other sub-steps, all those sub
   * steps are cloned recursively.
   * 
   * @throws WdkUserException
   * @throws WdkModelException
   */
  public Step deepClone(Integer strategyId, Map<Integer, Integer> stepIdMap) throws WdkModelException {
    Step step;
    try {
      if (!isCombined()) {
        AnswerValue answerValue = answerValueCache.getAnswerValue(false);
        step = getUser().createStep(strategyId, answerValue, deleted, assignedWeight);
      }
      else {
        Question question = getQuestion();
        Map<String, String> paramValues = new LinkedHashMap<String, String>();
        Map<String, Param> params = question.getParamMap();
        for (String paramName : this.paramValues.keySet()) {
          Param param = params.get(paramName);
          String paramValue = this.paramValues.get(paramName);
          if (param instanceof AnswerParam) {
            Step child = getUser().getStep(Integer.parseInt(paramValue));
            child = child.deepClone(strategyId, stepIdMap);
            paramValue = Integer.toString(child.getStepId());
          }
          paramValues.put(paramName, paramValue);
        }
        AnswerFilterInstance filter = getFilter();
        step = getUser().createStep(strategyId, question, paramValues, filter, deleted, false,
            assignedWeight, getFilterOptions());
      }
    }
    catch (WdkUserException ex) {
      throw new WdkModelException(ex);
    }

    stepIdMap.put(getStepId(), step.getStepId());
    
    step.collapsedName = collapsedName;
    step.customName = customName;
    step.collapsible = collapsible;
    step.update(false);

    Events.triggerAndWait(new StepCopiedEvent(this, step), new WdkModelException(
        "Unable to execute all operations subsequent to step copy."));

    return step;
  }

  public boolean isFiltered() throws WdkModelException {
    // first check if new filter has been applied
    if (filterOptions != null && filterOptions.isFiltered())
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
    return (filter != null) ? filter.getDisplayName() : filterName;
  }

  public Step getFirstStep() throws WdkModelException {
    Step step = this;
    while (step.getPreviousStep() != null)
      step = step.getPreviousStep();
    return step;
  }

  public boolean isBoolean() {
    try {
      return getQuestion().getQuery().isBoolean();
    }
    catch (WdkModelException ex) {
      return false;
    }
  }

  public JSONObject getJSONContent(int strategyId) throws WdkModelException {
    return getJSONContent(strategyId, false);
  }

  public JSONObject getJSONContent(int strategyId, boolean forChecksum) throws WdkModelException {

    JSONObject jsStep = new JSONObject();

    try {
      jsStep.put("id", this.stepId);
      jsStep.put("customName", this.customName);
      jsStep.put("question", this.questionName);
      jsStep.put("projectVersion", this.projectVersion);
      jsStep.put("filter", this.filterName);
      jsStep.put("collapsed", this.isCollapsible());
      jsStep.put("collapsedName", this.getCollapsedName());
      jsStep.put("deleted", deleted);
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
        jsStep.put("size", this.estimateSize);
      }

      if (this.isCollapsible()) { // a sub-strategy, needs to get order number
        String subStratId = strategyId + "_" + this.stepId;
        Integer order = getUser().getStrategyOrder(subStratId);
        if (order == null)
          order = 0; // the sub-strategy is not displayed
        jsStep.put("order", order);
      }
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsStep;
  }

  public Question getQuestion() throws WdkModelException {
    WdkModel wdkModel = stepFactory.getWdkModel();
    return (Question) wdkModel.resolveReference(questionName);
  }

  public AnswerFilterInstance getFilter() {
    try {
      return getQuestion().getRecordClass().getFilterInstance(filterName);
    }
    catch (WdkModelException ex) {
      return null;
    }
  }

  public String getFilterName() {
    return filterName;
  }

  public void setFilterName(String filterName) {
    this.filterName = filterName;
  }

  public AnswerValue getAnswerValue() throws WdkModelException, WdkUserException {
    return answerValueCache.getAnswerValue(true);
  }

  public AnswerValue getViewAnswerValue() throws WdkModelException, WdkUserException {
    return answerValueCache.getViewAnswerValue(true);
  }

  public AnswerValue getAnswerValue(boolean validate) throws WdkModelException, WdkUserException {
    return answerValueCache.getAnswerValue(validate);
  }

  public void resetAnswerValue() {
    answerValueCache.invalidateAll();
  }

  /**
   * @return the assignedWeight
   */
  public int getAssignedWeight() {
    return assignedWeight;
  }

  /**
   * @param assignedWeight
   *          the assignedWeight to set
   */
  public void setAssignedWeight(int assignedWeight) {
    this.assignedWeight = assignedWeight;
  }

  /**
   * @return the previousStepId
   */
  public int getPreviousStepId() {
    return previousStepId;
  }

  /**
   * @param previousStepId
   *          the previousStepId to set
   */
  public void setPreviousStepId(int previousStepId) {
    this.previousStepId = previousStepId;
  }

  public void setAndVerifyPreviousStepId(int previousStepId) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, previousStepId);
    setPreviousStepId(previousStepId);
  }

  /**
   * @return the childStepId
   */
  public int getChildStepId() {
    return childStepId;
  }

  /**
   * @param childStepId
   *          the childStepId to set
   */
  public void setChildStepId(int childStepId) {
    this.childStepId = childStepId;
  }

  public void setAndVerifyChildStepId(int childStepId) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, childStepId);
    setChildStepId(childStepId);
  }

  public boolean isRevisable() {
    return revisable;
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
    return stepId + " (" + previousStepId + ", " + childStepId + ")";
  }

  public boolean isUncollapsible() {
    // if the step hasn't been collapsed, it cannot be uncollapsed.
    if (!collapsible)
      return false;

    // if the step is a combined step, it cannot be uncollapsed
    if (isCombined())
      return false;

    return true;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception ex) {
    this.exception = ex;
  }

  public boolean getHasCompleteAnalyses() throws WdkModelException {
    return stepFactory.getWdkModel().getStepAnalysisFactory().hasCompleteAnalyses(this);
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

    logger.debug("Parsing json:\n" + jsContent.toString(2));

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
    setFilterOptionsJSON(getFilterArrayOrNull(jsContent, KEY_FILTERS));
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
    for (String paramName : paramValues.keySet()) {
      jsParams.put(paramName, paramValues.get(paramName));
    }
    return jsParams;
  }

  void setParamsJSON(JSONObject jsParams) throws WdkModelException {
    paramValues = new LinkedHashMap<String, String>();
    if (jsParams != null) {
      try {
        // read params;
        String[] paramNames = JSONObject.getNames(jsParams);
        if (paramNames != null) {
          for (String paramName : paramNames) {
            String paramValue = jsParams.getString(paramName);
            logger.trace("param '" + paramName + "' = '" + paramValue + "'");
            paramValues.put(paramName, paramValue);
          }
        }
      }
      catch (JSONException ex) {
        throw new WdkModelException(ex);
      }
    }
  }

  public JSONArray getFilterOptionsJSON() {
    return (filterOptions == null) ? null : filterOptions.getJSON();
  }

  public void setFilterOptionsJSON(JSONArray jsOptions) throws WdkModelException {
    if (jsOptions == null) {
      this.filterOptions = null;
    }
    else {
      FilterOptionList newList = new FilterOptionList(getQuestion(), jsOptions);
      validateFilterOptions(newList, false);
      this.filterOptions = newList;
    }
  }

  public JSONArray getViewFilterOptionsJSON() {
    return (viewFilterOptions == null) ? null : viewFilterOptions.getJSON();
  }

  public void setViewFilterOptionsJSON(JSONArray jsOptions) throws WdkModelException {
    if (jsOptions == null) {
      this.viewFilterOptions = null;
    }
    else {
      FilterOptionList newList = new FilterOptionList(getQuestion(), jsOptions);
      validateFilterOptions(newList, true);
      this.viewFilterOptions = newList;
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

  public Integer getStrategyId() {
    return strategyId;
  }

  public void setStrategyId(Integer strategyId) {
    this.strategyId = strategyId;
  }

  public void setAnswerValuePaging(int start, int end) {
    answerValueCache.setPaging(start, end);
    
  }

  public void setInMemoryOnly(boolean flag) {
    inMemoryOnly = true;
  }
}
