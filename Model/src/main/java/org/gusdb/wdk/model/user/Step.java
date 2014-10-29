package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Charles Treatman
 * 
 */
public class Step {

  private static final Logger logger = Logger.getLogger(Step.class);

  private StepFactory stepFactory;
  private User user;
  private int userId;
  private int stepId;
  private Date createdTime;
  private Date lastRunTime;
  private String customName;
  private boolean deleted = false;
  private boolean collapsible = false;
  private String collapsedName = null;

  private String projectId;
  private String projectVersion;
  private String questionName;

  private Step nextStep = null;
  private Step previousStep = null;
  private Step parentStep = null;
  private Step childStep = null;

  private String booleanExpression;

  private boolean valid = true;
  private boolean validityChecked = false;

  private int estimateSize = 0;

  private String filterName;
  private AnswerValue answerValue;

  private Map<String, String> paramValues = new LinkedHashMap<String, String>();
  private FilterOptionList filterOptions = new FilterOptionList();

  private int assignedWeight;

  private int previousStepId;
  private int childStepId;

  private boolean revisable = true;
  private Exception exception;
  

  /**
   * Creates a step object for given user and step ID.  Note that this
   * constructor lazy-loads the User object for the passed ID if one is
   * required for processing after construction.
   * 
   * @param stepFactory step factory that generated this step
   * @param userId id of the owner of this step
   * @param stepId id of the step
   */
  Step(StepFactory stepFactory, int userId, int stepId) {
    this.stepFactory = stepFactory;
    this.user = null;
    this.userId = userId;
    this.stepId = stepId;
    deleted = false;
    assignedWeight = 0;
  }
  
  /**
   * Creates a step object for the given user and step ID.
   * 
   * @param stepFactory step factory that generated this step
   * @param user owner of this step
   * @param stepId id of the step
   * @throws NullPointerException if user is null
   */
  Step(StepFactory stepFactory, User user, int stepId) {
    this.stepFactory = stepFactory;
    this.user = user;
    this.userId = user.getUserId();
    this.stepId = stepId;
    deleted = false;
    assignedWeight = 0;
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

  public int getResultSize() throws WdkModelException {
    if (!isValid()) {
      try {
        estimateSize = getAnswerValue().getResultSize();
      }
      catch (Exception ex) {
        logger.error("Exception when estimating result size.", ex);
        // FIXME (Redmine #16030): not sure if this exception means step is
        // invalid or not.  It could mean param values are invalid and step
        // should be made invalid, but it could also mean DB is down or other
        // issues.  Thus, we cannot set valid to false at all here (locally or
        // in DB), because it might impact parent steps whose valid values
        // depend on this one's.  To fix, we need to differentiate the
        // exceptions thrown by AnswerValue by the errors that mean Step is
        // invalid vs. those that don't.
      }
    }
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
    }
    else
      previousStepId = 0;
  }

  public boolean isFirstStep() {
    return (previousStepId == 0);
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
   * @return Returns the isBoolean.
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

  public void update(boolean updateTime) throws WdkModelException {
    stepFactory.updateStep(getUser(), this, updateTime);
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
   * Checks validity of this step and all the child steps it depends on and
   * returns result.  Value is memoized for efficiency.  This call checks
   * against any preset valid value, and checks that param names are correct,
   * but does not check param values due to execution cost.  Param values must
   * be checked elsewhere; if they are found invalid, invalidateStep() should
   * be called, which updates the DB.
   * 
   * @return true if this step is valid (to the best of our knowledge), else false
   */
  public boolean isValid() throws WdkModelException {
    if (!valid || validityChecked) {
      // check 1: nothing but revise can turn step from invalid -> valid
      // check 2: if value is memoized, do not check steps and params again
      return valid;
    }
    
    // check stored param names against those in Question
    Map<String, Param> params = getQuestion().getParamMap();
    for (String paramName : paramValues.keySet()) {
      if (!params.containsKey(paramName)) {
        logger.error("Unable to find all stored param names in Question " +
            "(bad param name = " + paramName + ").  Setting valid to false.");
        invalidateStep();
      }
    }
    
    // check previous and child steps if still valid
    Step prevStep, childStep;
    if (valid &&
        (((prevStep = getPreviousStep()) != null && !prevStep.isValid()) ||
         ((childStep = getChildStep()) != null && !childStep.isValid()))) {
      invalidateStep();
    }

    // mark validity checked so we don't do the work above again
    validityChecked = true;
    return valid;
  }

  /**
   * Sets valid value to false and sends change to the DB
   * @throws WdkModelException if unable to update DB
   */
  public void invalidateStep() throws WdkModelException {
    setValid(false);
    stepFactory.setStepValidFlag(this);
  }
  
  /**
   * @param isValid the isValid to set
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
    Step[] steps = getAllSteps();
    return steps[index];
  }

  public Step[] getAllSteps() throws WdkModelException {
    ArrayList<Step> allSteps = new ArrayList<Step>();
    allSteps = buildAllStepsArray(allSteps, this);
    return allSteps.toArray(new Step[allSteps.size()]);
  }

  public int getLength() throws WdkModelException {
    return getAllSteps().length;
  }

  private ArrayList<Step> buildAllStepsArray(ArrayList<Step> array, Step step) throws WdkModelException {
    if (step.isFirstStep()) {
      array.add(step);
    }
    else {
      array = buildAllStepsArray(array, step.getPreviousStep());
      array.add(step);
    }
    return array;
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
   * @param paramErrors
   *          the paramErrors to set
   */
  public void setParamValues(Map<String, String> paramValues) {
    if (paramValues == null)
      paramValues = new LinkedHashMap<>();
    this.paramValues = new LinkedHashMap<String, String>(paramValues);
  }
  
  public FilterOptionList getFilterOptions() {
    return filterOptions;
  }
  
  public void setFilterOptions(FilterOptionList filterOptions) {
    this.filterOptions = filterOptions;
  }
  
  public void addFilterOption(String filterName, JSONObject filterValue) {
    this.filterOptions.addFilterOption(filterName, filterValue);
  }
  
  public void removeFilterOption(String filterName) {
    this.filterOptions.removeFilterOption(filterName);
  }

  public RecordClass getRecordClass() throws WdkModelException {
    return getQuestion().getRecordClass();
  }

  public int getIndexFromId(int stepId) throws WdkUserException, WdkModelException {
    Step[] steps = getAllSteps();
    for (int i = 0; i < steps.length; ++i) {
      if (steps[i].getStepId() == stepId ||
          (steps[i].getChildStep() != null && steps[i].getChildStep().getStepId() == stepId)) {
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
      Step step = getUser().createStep(question, params, filter, startIndex, endIndex, deleted, false,
          assignedWeight, filterOptions);
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
   * @throws SQLException
   * @throws WdkUserException
   * @throws JSONException
   * @throws WdkModelException
   * @throws NoSuchAlgorithmException
   * 
   */
  public Step deepClone() throws WdkModelException {
    Step step;
    AnswerValue answerValue;
    try {
      answerValue = getAnswerValue();
      if (!isCombined()) {
        step = getUser().createStep(answerValue, deleted, assignedWeight);
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
            child = child.deepClone();
            paramValue = Integer.toString(child.getStepId());
          }
          paramValues.put(paramName, paramValue);
        }
        AnswerFilterInstance filter = getFilter();
        int pageStart = answerValue.getStartIndex();
        int pageEnd = answerValue.getEndIndex();
        step = getUser().createStep(question, paramValues, filter, pageStart, pageEnd, deleted, false,
            assignedWeight, filterOptions);
      }
    }
    catch (WdkUserException ex) {
      throw new WdkModelException(ex);
    }

    step.collapsedName = collapsedName;
    step.customName = customName;
    step.collapsible = collapsible;
    step.update(false);
    
    try {
      stepFactory.getWdkModel().getStepAnalysisFactory().copyAnalysisInstances(this, step);
    }
    catch (WdkUserException e) {
      // means copied answer is no longer valid for this type of analysis; this should probably not happen
      throw new WdkModelException("Cannot copy analysis instances during step copy", e);
    }
    return step;
  }

  public boolean isFiltered() {
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

      Step childStep = getChildStep();
      if (childStep != null) {
        jsStep.put("child", childStep.getJSONContent(strategyId));
      }

      Step prevStep = getPreviousStep();
      if (prevStep != null) {
        jsStep.put("previous", prevStep.getJSONContent(strategyId));
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
    return getAnswerValue(true);
  }

  public AnswerValue getAnswerValue(boolean validate) throws WdkModelException, WdkUserException {
    // even if a step is invalid, still allow user to create answerValue
    // if (!valid)
    // throw new WdkUserException("Step #" + internalId
    // + "(i) is invalid, cannot create answerValue.");
    if (answerValue == null) {
      Question question = getQuestion();
      User user = getUser();
      Map<String, Boolean> sortingMap = user.getSortingAttributes(question.getFullName());
      int endIndex = user.getItemsPerPage();
      try {
        answerValue = question.makeAnswerValue(user, paramValues, 1, endIndex, sortingMap, getFilter(),
            validate, assignedWeight);
        answerValue.setFilterOptions(filterOptions);
      }
      catch (WdkUserException ex) {
        throw new WdkModelException(ex);
      }
      try {
        this.estimateSize = answerValue.getResultSize();
      }
      catch (WdkModelException | WdkUserException ex) {
        // if validate is false, the error will be ignored to allow the process to continue.
        if (validate)
          throw ex;
        else
          logger.warn(ex);
      }
      update(false);
    }
    return answerValue;
  }

  public boolean isUseBooleanFilter() throws WdkModelException {
    if (!isBoolean())
      return false;
    BooleanQuery query = (BooleanQuery) getQuestion().getQuery();
    String paramName = query.getUseBooleanFilter().getName();
    String strBooleanFlag = paramValues.get(paramName);
    return Boolean.parseBoolean(strBooleanFlag);
  }

  public void setAnswerValue(AnswerValue answerValue) {
    this.answerValue = answerValue;
  }

  public void resetAnswerValue() {
    this.answerValue = null;
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
   * The previous step param is always the first answerParam.
   * 
   * @return
   * @throws WdkModelException
   */
  public String getPreviousStepParam() throws WdkModelException {
    Param[] params = getQuestion().getParams();
    for (Param param : params) {
      if (param instanceof AnswerParam) {
        return param.getName();
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
  public String getChildStepParam() throws WdkModelException {
    Param[] params = getQuestion().getParams();
    int index = 0;
    for (Param param : params) {
      if (param instanceof AnswerParam) {
        index++;
        if (index == 2)
          return param.getName();
      }
    }
    return null;
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
}
