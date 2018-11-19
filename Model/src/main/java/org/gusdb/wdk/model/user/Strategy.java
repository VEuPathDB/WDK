package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.functional.Functions.defaultOnException;
import static org.gusdb.fgputil.functional.Functions.reduce;
import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.collection.ReadOnlyHashMap.Builder;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkIllegalArgumentException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactoryHelpers.UserCache;
import org.json.JSONException;
import org.json.JSONObject;

public class Strategy implements StrategyElement, StepContainer {

  private static final Logger LOG = Logger.getLogger(Strategy.class);

  public static class StrategyBuilder {

    private final WdkModel _wdkModel;
    private final long _userId;
    private final long _strategyId;
    private String _projectId;
    private String _version;
    private String _name;
    private String _description;
    private boolean _isSaved = false;
    private String _savedName = null;
    private boolean _isDeleted = false;
    private Date _createdTime = new Date();
    private Date _lastModifiedTime;
    private Date _lastRunTime;
    private String _signature;
    private boolean _isPublic = false;
    private long _rootStepId = 0;

    private Map<Long,StepBuilder> _stepMap = new HashMap<>();

    public StrategyBuilder(WdkModel wdkModel, long userId, long strategyId) {
      _wdkModel = wdkModel;
      _projectId = wdkModel.getProjectId();
      _version = wdkModel.getVersion();
      _userId = userId;
      _strategyId = strategyId;
    }

    public StrategyBuilder(Strategy strategy) {
      _wdkModel = strategy._wdkModel;
      _userId = strategy.getUser().getUserId();
      _strategyId = strategy._strategyId;
      _projectId = strategy._projectId;
      _version = strategy._version;
      _name = strategy._name;
      _description = strategy._description;
      _isSaved = strategy._isSaved;
      _savedName = strategy._savedName;
      _isDeleted = strategy._isDeleted;
      _createdTime = strategy._createdTime;
      _lastModifiedTime = strategy._lastModifiedTime;
      _lastRunTime = strategy._lastRunTime;
      _signature = strategy._signature;
      _isPublic = strategy._isPublic;
      _rootStepId = strategy._rootStepId;
      _stepMap = Functions.getMapFromList(strategy._stepMap.values(),
          step -> new TwoTuple<>(step.getStepId(), Step.builder(step)));
    }

    public long getStrategyId() {
      return _strategyId;
    }

    public StrategyBuilder setProjectId(String projectId) {
      _projectId = projectId;
      return this;
    }

    public StrategyBuilder setVersion(String version) {
      _version = version;
      return this;
    }

    public StrategyBuilder setName(String name) {
      _name = FormatUtil.shrinkUtf8String(name, StepFactory.COLUMN_NAME_LIMIT);
      return this;
    }

    public StrategyBuilder setDescription(String description) {
      _description = description;
      return this;
    }

    public StrategyBuilder setSaved(boolean isSaved) {
      _isSaved = isSaved;
      return this;
    }

    public StrategyBuilder setSavedName(String savedName) {
      _savedName = FormatUtil.shrinkUtf8String(savedName, StepFactory.COLUMN_NAME_LIMIT);
      return this;
    }

    public StrategyBuilder setDeleted(boolean isDeleted) {
      _isDeleted = isDeleted;
      return this;
    }

    public StrategyBuilder setCreatedTime(Date createdTime) {
      _createdTime = createdTime;
      return this;
    }

    public StrategyBuilder setLastModifiedTime(Date lastModifiedTime) {
      _lastModifiedTime = lastModifiedTime;
      return this;
    }

    public StrategyBuilder setLastRunTime(Date lastRunTime) {
      _lastRunTime = lastRunTime;
      return this;
    }

    public StrategyBuilder setSignature(String signature) {
      _signature = signature;
      return this;
    }

    public StrategyBuilder setIsPublic(boolean isPublic) {
      _isPublic = isPublic;
      return this;
    }

    // note root step must be added just like any other step
    public StrategyBuilder setRootStepId(long rootStepId) {
      _rootStepId = rootStepId;
      return this;
    }

    public StrategyBuilder addStep(StepBuilder step) {
      _stepMap.put(step.getStepId(), step);
      return this;
    }

    public StrategyBuilder addSteps(Collection<StepBuilder> steps) {
      _stepMap.putAll(Functions.getMapFromList(steps, step -> new TwoTuple<>(step.getStepId(), step)));
      return this;
    }

    public Strategy build(UserCache userCache, ValidationLevel validationLevel) {
      return new Strategy(this, userCache.get(_userId), validationLevel);
    }
  }

  public static StrategyBuilder builder(WdkModel wdkModel, long userId, long strategyId) {
    return new StrategyBuilder(wdkModel, userId, strategyId);
  }

  private final WdkModel _wdkModel;
  private final User _user;
  private final long _strategyId;
  private final String _projectId;
  private final String _version;
  private final String _name;
  private final String _description;
  private final boolean _isSaved;
  private final String _savedName;
  private final boolean _isDeleted;
  private final Date _createdTime;
  private final Date _lastModifiedTime;
  private final Date _lastRunTime;
  private final String _signature;
  private final boolean _isPublic;
  private long _rootStepId; // <- MODIFIABLE
  private final Map<Long, Step> _stepMap;

  private Strategy(StrategyBuilder strategyBuilder, User user, ValidationLevel validationLevel) {
    _user = user;
    _wdkModel = strategyBuilder._wdkModel;
    _strategyId = strategyBuilder._strategyId;
    _projectId = strategyBuilder._projectId;
    _version = strategyBuilder._version;
    _name = strategyBuilder._name;
    _description = strategyBuilder._description;
    _isSaved = strategyBuilder._isSaved;
    _savedName = strategyBuilder._savedName;
    _isDeleted = strategyBuilder._isDeleted;
    _createdTime = strategyBuilder._createdTime;
    _lastModifiedTime = strategyBuilder._lastModifiedTime;
    _lastRunTime = strategyBuilder._lastRunTime;
    _signature = strategyBuilder._signature;
    _isPublic = strategyBuilder._isPublic;
    _rootStepId = strategyBuilder._rootStepId;
    _stepMap = createStepTree(user, _rootStepId, strategyBuilder._stepMap.values(), validationLevel);
  }

  public User getUser() {
    return _user;
  }

  public boolean isDeleted() {
    return _isDeleted;
  }

  public String getVersion() {
    return _version;
  }

  public String getName() {
    return _name;
  }

  public String getSavedName() {
    return _savedName;
  }

  public boolean getIsSaved() {
    return _isSaved;
  }

  public boolean getIsPublic() {
    return _isPublic;
  }

  public String getProjectId() {
    return _projectId;
  }

  public Step getRootStep() {
    return findFirstStep(withId(_rootStepId)).orElseThrow(
        () -> new WdkRuntimeException("Root step ID " + _rootStepId + " no longer present in strategy."));
  }

  public void setRootStep(Step step) throws WdkModelException {
    verifySameOwnerAndProject(this, step);
    // also update the cached info
    _stepMap.put(step.getStepId(), step);
    _rootStepId = step.getStepId();
  }

  @Override
  public Long getStrategyId() {
    return _strategyId;
  }

  /**
   * @return Returns the createTime.
   */
  public Date getCreatedTime() {
    return _createdTime;
  }

  public List<Step> getMainBranch() throws WdkModelException {
    return getRootStep().getMainBranch();
  }

  public int getLength() throws WdkModelException {
    return getRootStep().getLength();
  }

  public long getRootStepId() {
    return _rootStepId;
  }

  /**
   * @param overwrite
   *          if true, it will overwrite the strategy even if it's already saved; if false, we will create a
   *          new unsaved copy if the strategy is already saved.
   * 
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public void update(boolean overwrite) throws WdkModelException, WdkUserException {
    _wdkModel.getStepFactory().updateStrategy(_user, this, overwrite);
  }

  public RecordClass getRecordClass() {
    return getRootStep().getRecordClass();
  }

  /**
   * Insert a new step before the target. The new step will become the previous step of the target, and the
   * old previousStep of the target should become the previousStep of the new step.
   * 
   * @param newStep
   *          the newStep
   * @param targetId
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<Long, Long> insertStepBefore(Step newStep, long targetId) throws WdkModelException,
      WdkUserException {
    Step targetStep = findFirstStep(withId(targetId)).get();
    verifySameOwnerAndProject(this, targetStep);

    Map<Long, Long> rootMap = new HashMap<>();

    // if the strategy is saved, need to make a unsaved copy first
    if (getIsSaved())
      update(false);

    // make sure the previousStep of the target is now the previousStep of newStep
    if (targetStep.isFirstStep()) { // inserting before first step will cause the first step being replaced by
      // the new step, while old first step will become the child of this new step
      if (newStep.getChildStep() == null || newStep.getChildStep().getStepId() != targetId)
        throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " before step #" +
            targetId + " since it will corrupt the structure of the strategy #" + _strategyId);

      // if the first step has any step upstream, will link it to the new step
      Step nextStep = getNext(targetStep);
      if (nextStep != null) { // insert the new step in the same strategy panel
        nextStep.checkPreviousAllowed(newStep);
        nextStep.setPreviousStep(newStep);
        nextStep.saveParamFilters();
      }
      else { // the target is the only step in the strategy/nested strategy.
        Step parentStep = getParent(targetStep);
        if (parentStep != null) {
          // add the new step as the last one in a nested strategy
          // copy information from target step
          newStep.setCollapsible(targetStep.isCollapsible());
          newStep.setCollapsedName(targetStep.getCollapsedName());
          newStep.update(false); // don't need to update LastRunTime
          targetStep.setCollapsible(false);
          targetStep.update(false);

          // check and set the newStep as the child of the parent, to replace the target step
          parentStep.checkChildAllowed(newStep);
          parentStep.setChildStep(newStep);
          parentStep.saveParamFilters();
          rootMap.put(targetId, newStep.getStepId());
        }
        else { // target is at the end of the strategy, set newStep as the end of the strategy
          setRootStep(newStep);
          update(false); // don't overwrite a saved strategy.
          rootMap.put(targetId, newStep.getStepId());
        }
      }
    }
    else { // target is not the first, then the previousStep of the target will become the previous of the
      // new step.
      if (targetStep.getPreviousStepId() != newStep.getPreviousStepId())
        throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " before step #" +
            targetId + " since it will corrupt the structure of the strategy #" + _strategyId);

      // make sure the target step can take the newStep as previousStep
      targetStep.checkPreviousAllowed(newStep);

      targetStep.setPreviousStep(newStep);
      targetStep.saveParamFilters();
    }
    return rootMap;
  }

  /**
   * Inserting a step after the target. This is used when we add steps in main and nested strategy. The
   * newStep will become the next step of the target.
   * 
   * @param newStep
   *          The next step has to be a combined step, with the target as the previous step of it.
   * @param targetId
   *          a target step id that can live anywhere in the step tree.
   * @return a map of oldStepId to newStepId that are roots of the strategy or nested strategy.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<Long, Long> insertStepAfter(Step newStep, long targetId) throws WdkModelException,
      WdkUserException {
    Map<Long, Long> rootMap = new HashMap<>();

    // make sure the newStep uses target step as its previousStep
    Step previousStep = newStep.getPreviousStep();
    if (previousStep == null || previousStep.getStepId() != targetId)
      throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " after step #" + targetId +
          " since it will corrupt the structure of the strategy #" + _strategyId);

    // if the strategy is saved, need to make a unsaved copy first
    if (getIsSaved())
      update(false);

    Step targetStep = findFirstStep(withId(targetId))
        .orElseThrow(() -> new WdkModelException("Could not find target step."));

    Step nextStep = getNext(targetStep);
    if (nextStep != null) { // insert in the middle of a strategy
      // make sure the next step can take the newStep as previousStep
      nextStep.checkPreviousAllowed(newStep);
      // link new step to target step.
      targetStep.setNextStep(newStep);
      // link next step to the new step
      nextStep.setPreviousStep(newStep);
      nextStep.saveParamFilters();
    }
    else { // newStep will be the last one in main/nested strategy
      Step parentStep = getParent(targetStep);
      if (parentStep != null) {
        // make sure the parent step can take the newStep as childStep
        parentStep.checkChildAllowed(newStep);
      }

      // copy over collapsing info
      if (previousStep.isCollapsible()) {
        newStep.setCollapsible(previousStep.isCollapsible());
        newStep.setCollapsedName(previousStep.getCollapsedName());
        newStep.update(false);

        // reset the colllapsing infom
        previousStep.setCollapsible(false);
        previousStep.setCollapsedName(null);
        previousStep.update(false);
      }

      rootMap.put(previousStep.getStepId(), newStep.getStepId());

      if (parentStep != null) {
        // a step is inserted at the end of a nested strategy -- ie. add step on nested strategy.
        // the new step will become the child of the parentStep
        parentStep.setChildStep(newStep);
        parentStep.saveParamFilters();
      }
      else { // a step is inserted at the end of main strategy
        setRootStep(newStep);
        update(false);
      }
    }
    return rootMap;
  }

  /**
   * Delete the given step from the strategy. Additional step, or even the current strategy maybe deleted,
   * depending on the following cases:
   * 
   * #1 - if there is only one step in the strategy and it's deleted, then the whole strategy will be deleted.
   * 
   * #2 - if the step to be deleted is a combined one, all the steps in its child branch will also be deleted.
   * 
   * #3 - if the step has previous and next, the previous will be connected to the next;
   * 
   * #2 & #3 means that when deleting a nested step in the main strategy, we should send the id of the boolean
   * that takes the nested step, and it will delete both boolean and the whole nested strategy; however, if
   * the root of the nested strategy is passed in, the only the root will be deleted, and the remaining steps
   * in the nested strategy will become a new sub-tree.
   * 
   * @param step
   * @return a map of root id changes (both main & nested strategies) {old, new}; the information will be used
   *         to update the states of active strategies.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<Long, Long> deleteStep(Step step) throws WdkModelException, WdkUserException {
    StepFactory stepFactory = _wdkModel.getStepFactory();
    List<Step> deletes = new ArrayList<>();
    Map<Long, Long> rootMap = new HashMap<>();

    // if the strategy is saved, need to make a unsaved copy first
    if (getIsSaved())
      update(false);

    // if a step has child, delete all the steps on that branch.
    Step childStep = step.getChildStep();
    if (childStep != null)
      deletes.addAll(childStep.getNestedBranch());
    deletes.add(step); // delete the current step

    // keep a reference to previous step, in case we need to delete multiple steps. this previousStep will be
    // used to connect to the remaining steps.
    Step previousStep = step.getPreviousStep();

    // loop while the current step is marked to be deleted.
    while (step != null && deletes.contains(step)) {
      Step nextStep = getNext(step);
      if (nextStep != null) { // go to the next step in the same panel.
        step = nextStep;
        if (previousStep == null) {
          if (step.isCombined() && !step.isTransform()) {
            // a two-input combined step, since there is no previousStep, the child of it will become new
            // previousStep, and this combined step will be deleted
            previousStep = step.getChildStep();
          } // otherwise, a transform step, and since there is no previousStep, it will also be deleted
          deletes.add(step);
        }
        else { // otherwise, previous step exists, no more deletion needed. will exit loop.
          // check if the step can take the previous step
          stepFactory.dropDependency(previousStep.getStepId(), StepFactoryHelpers.COLUMN_LEFT_CHILD_ID);
          step.checkPreviousAllowed(previousStep);
          step.setPreviousStep(previousStep);
          step.saveParamFilters();
        }
      }
      else { // no next step exists, the current step must be a root of main/nested strategy.
        Step parentStep = getParent(step);
        if (parentStep != null) { // found a parent, which means we are deleting in a nested strategy.
          if (previousStep != null) { // make the previous step in the nested strategy a new root there.
            // get the collapsing info from old root of the nested strategy.
            previousStep.setCollapsible(step.isCollapsible());
            previousStep.setCollapsedName(step.getCollapsedName());
            previousStep.update(false);
            rootMap.put(step.getStepId(), previousStep.getStepId());

            // check if parent can take previous step as child
            stepFactory.dropDependency(previousStep.getStepId(), StepFactoryHelpers.COLUMN_RIGHT_CHILD_ID);
            parentStep.checkChildAllowed(previousStep);
            parentStep.setChildStep(previousStep);
            parentStep.saveParamFilters();
            break;
          }
          else { // no previousStep
            // Otherwise, the last step from nested strategy is deleted, we will also delete the parent; but
            // now the previousStep will become the previous one from the parent.
            previousStep = parentStep.getPreviousStep();
            deletes.add(parentStep);
          }
        } // otherwise, we are deleting the last step in main branch, will handle it outside of the loop
        step = parentStep;
      }
    }

    if (step == null) {
      if (previousStep != null) { // current step is null, then previous step should become new root.
        rootMap.put(getRootStepId(), previousStep.getStepId());
        setRootStep(previousStep);
        update(false);
      }
      else { // no more steps left in the strategy, delete the strategy itself.
        stepFactory.deleteStrategy(_strategyId);
        rootMap.clear();
      }
    }

    // after strategy is deleted (if needed), will now delete steps
    LOG.debug("Total " + deletes.size() + " steps deleted.");
    for (Step delete : deletes) {
      stepFactory.deleteStep(delete.getStepId());
    }

    return rootMap;
  }

  public Step getFirstStep() throws WdkModelException {
    Step step = getRootStep();
    while (step.getPreviousStep() != null) {
      step = step.getPreviousStep();
    }
    return step;
  }

  /**
   * checksum of a strategy is different from signature in that signature is stable and it will never change
   * after the strategy is created, while checksum depends on many properties of a strategy, and it will
   * change when the strategies properties are changed.
   * 
   * @return
   * @throws WdkModelException
   */
  public String getChecksum() throws WdkModelException {
    JSONObject jsStrategy = getJSONContent(true);
    String checksum = EncryptionUtil.encrypt(JsonUtil.serialize(jsStrategy));
    LOG.debug("Strategy #" + _strategyId + ", checksum=" + checksum + ", json:\n" + jsStrategy);
    return checksum;
  }

  public JSONObject getJSONContent() throws WdkModelException {
    return getJSONContent(false);
  }

  public JSONObject getJSONContent(boolean forChecksum) throws WdkModelException {
    JSONObject jsStrategy = new JSONObject();

    try {
      jsStrategy.put("id", _strategyId);
      jsStrategy.put("name", _name);
      jsStrategy.put("savedName", _savedName);
      jsStrategy.put("description", _description);
      jsStrategy.put("saved", _isSaved);
      jsStrategy.put("deleted", _isDeleted);
      jsStrategy.put("type", getRecordClass().getFullName());

      if (!forChecksum) {
        jsStrategy.put("valid", isValid());
        jsStrategy.put("version", getVersion());
        jsStrategy.put("resultSize", getEstimateSize());
      }

      JSONObject stepContent = getRootStep().getJSONContent(_strategyId, forChecksum);
      jsStrategy.put("latestStep", stepContent);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsStrategy;
  }

  public boolean isValid() {
    // if any steps are invalid, the strategy is invalid
    return reduce(_stepMap.values(), (acc, next) -> (!next.isValid() ? false : acc), true);
  }

  /**
   * @return the lastRunTime
   */
  public Date getLastRunTime() {
    return getRootStep().getLastRunTime();
  }

  /**
   * @return the lastModifiedTime
   */
  public Date getLastModifiedTime() {
    return _lastModifiedTime;
  }

  /**
   * checksum of a strategy is different from signature in that signature is stable and it will never change
   * after the strategy is created, while checksum depends on many properties of a strategy, and it will
   * change when the strategies properties are changed.
   * 
   * @return the signature
   */
  public String getSignature() {
    return _signature;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }


  public int getEstimateSize() {
    // FIXME: could root step really be null?  I think if root step is deleted, strategy id deleted?
    return (getRootStep() == null ? 0 : defaultOnException(() -> getRootStep().getResultSize(), 0));
  }

  public String getEstimateSizeNoCalculate() {
    // FIXME: could root step really be null?  I think if root step is deleted, strategy id deleted?
    int latestStepEstimateSize = getRootStep() == null ? 0 : getRootStep().getEstimateSize();
    return (latestStepEstimateSize == Step.RESET_SIZE_FLAG ? "Unknown" : String.valueOf(latestStepEstimateSize));
  }

  /**
   * Get the parent of the given step in the context of the strategy, which maybe different from the parent
   * set in the step itself (in the case that the step is updated, but the strategy tree hasn't been updated).
   * 
   * @param step
   * @return the parent of the step, or null if the step doesn't have parent, or doesn't belong to the
   *         strategy.
   * @throws WdkModelException
   */
  private Step getParent(Step step) throws WdkModelException {
    // use a stack to store the previous steps to be examined.
    Stack<Step> stack = new Stack<>();
    stack.push(getRootStep());
    while (!stack.isEmpty()) {
      Step s = stack.pop();
      Step parent = null;
      while (s != null) {
        if (s.getPreviousStep() != null)
          stack.push(s.getPreviousStep());
        if (s.getStepId() == step.getStepId())
          return parent;
        parent = s;
        s = s.getChildStep();
      }
    }
    return null;
  }

  /**
   * Get the next of the given step, which could be different from the next stored in the step itself (in the
   * case that step is updated, but strategy hasn't been updated).
   * 
   * @param step
   * @return return the next of the given step, or null if the step doesn't have next, or if the step doesn't
   *         belong the the strategy.
   * @throws WdkModelException
   */
  private Step getNext(Step step) throws WdkModelException {
    // use a stack to store the child steps to be examined.
    Stack<Step> stack = new Stack<>();
    stack.push(getRootStep());
    while (!stack.isEmpty()) {
      Step s = stack.pop();
      Step next = null;
      while (s != null) {
        if (s.getChildStep() != null)
          stack.push(s.getChildStep());
        if (s.getStepId() == step.getStepId())
          return next;
        next = s;
        s = s.getPreviousStep();
      }
    }
    return null;
  }

  private static Map<Long, Step> createStepTree(User user, long rootStepId, Collection<StepBuilder> steps,
      ValidationLevel validationLevel) {

    // 1. Confirm project and user id match across strat and all steps, and that steps were properly assigned
    for (StepBuilder step : steps) {
      String identifier = "Step " + step.getStepId();
      if (_userId != step.getUserId().longValue()) {
        throw new WdkModelException(identifier + " does not have the same owner as its strategy, " + _strategyId);
      }
      if (!_projectId.equals(step.getProjectId())) {
        throw new WdkModelException(identifier + " does not have the same project as its strategy, " + _strategyId);
      }
      if (_strategyId != step.getStrategyId().longValue()) {
        throw new WdkModelException(identifier + " was given to strategy " + _strategyId + " but belongs to strategy " + step.getStrategyId() + " (i.e. SQL is broken).");
      }
    }

    // temporary map to keep track of which steps have been referenced so far
    Map<Long, Boolean> stepRefMap = Functions.getMapFromList(_stepMap.values(),
        step -> new TwoTuple<Long,Boolean>(step.getId(), false));

    // 2. Make sure all referenced steps are present and used only once
    _latestStep = getReferencedStep(stepRefMap, this, getRootStepId(), false);
    for (Step step : _stepMap.values()) {
      step.setPreviousStep(getReferencedStep(stepRefMap, step, step.getPreviousStepId(), true));
      step.setChildStep(getReferencedStep(stepRefMap, step, step.getChildStepId(), true));
    }

    // 3. Throw exception if steps are assigned to this strat that are not in the tree
    for (Entry<Long,Boolean> ref : stepRefMap.entrySet()) {
      if (!ref.getValue()) {
        throw new WdkModelException("Step " + ref.getKey() + " has strategy ID " + _strategyId +
            " but is not referenced as the root step or by any steps in the strategy.");
      }
    }

    Functions.getMapFromList(strategyBuilder._stepMap.values(),
        stepBuilder -> new TwoTuple<>(stepBuilder.getStepId(),
            stepBuilder.build(new UserCache(user), validationLevel, this)));

    // 4. Validate each step in the strategy; regardless of validity, strategy can be sent out and hopefully displayed
    for (Step step : _stepMap.values()) {
      step.finish(userCache, this);
    }
  }

  private Step getReferencedStep(Map<Long,Boolean> stepRefMap, StrategyElement parent, long stepId, boolean allowZero) throws WdkModelException {
    String parentIdentifier = parent.getClass().getSimpleName() + " " + parent.getId();
    if (stepId == 0) {
      if (allowZero) {
        return null;
      }
      else {
        throw new WdkModelException(parentIdentifier + " " + " must refer to a valid step ID.");
      }
    }
    Boolean isReferenced = stepRefMap.get(stepId);
    if (isReferenced == null) {
      throw new WdkModelException(parentIdentifier + " refers to step " + stepId + " which is not part of its strategy.");
    }
    if (isReferenced) {
      throw new WdkModelException("Step " + stepId + " is referenced by >1 steps in strategy " + parent.getStrategyId());
    }
    stepRefMap.put(stepId, true);
    return _stepMap.get(stepId);
  }

  @Override
  public long getId() {
    return getStrategyId();
  }

  public int getNumSteps() {
    return _stepMap.size();
  }

  @Deprecated // used???
  public int getNumStepsUi() {
    return _stepMap.values().stream()
      // FIXME: the following line has issues if getQuestion() returns null (i.e. invalid question name)
      .filter(step -> step.getAnswerSpec().getQuestion().getQuery().getAnswerParamCount() < 2)
      .collect(Collectors.counting()).intValue();
  }

  private void verifySameOwnerAndProject(Strategy strategy, Step step) throws WdkModelException {
    // check that users match
    if (strategy.getUser().getUserId() != step.getUser().getUserId()) {
      throw new WdkIllegalArgumentException(Step.getVerificationPrefix() +
          "Cannot assign a root step to a strategy unless they have the same" + "owner.  Existing strategy " +
          strategy.getStrategyId() + " has" + "owner " + strategy.getUser().getUserId() + " (" +
          strategy.getUser().getEmail() + ")\n  Call made to assign the " +
          "following root step (see stack below for how):\n  Newly assigned" + "step " + step.getStepId() +
          " has owner " + step.getUser().getUserId() + " (" + step.getUser().getEmail() + ")");
    }

    // check that projects both match current project
    String projectId = _wdkModel.getProjectId();
    if (!strategy.getProjectId().equals(projectId) || !step.getProjectId().equals(projectId)) {
      throw new WdkIllegalArgumentException(Step.getVerificationPrefix() +
          "Cannot assign a root step to a strategy " +
          "unless they have the same project.  Project IDs don't match " +
          "during assignment of root step to strategy!!\n  Currently loaded " + "model has project " +
          projectId + ".\n  Root step to be assigned (" + step.getStepId() + ") has project " +
          step.getProjectId() + ".\n  " + "Strategy being assigned step (" + strategy.getStrategyId() +
          ") has project " + strategy.getProjectId());
    }
  }

  /**
   * Returns the first step in this strategy that passes the search predicate.  If none exists,
   * throws IllegalArgumentException with a custom message.
   * 
   * @param search step search
   * @return first step found that passes the predicate
   * @throws IllegalArgumentException if strategy does not contain a step that matches the search criteria
   */
  @Override
  public Optional<Step> findFirstStep(StepSearch search) {
    return _stepMap.values().stream().filter(search.getPredicate()).findFirst();
  }

}
