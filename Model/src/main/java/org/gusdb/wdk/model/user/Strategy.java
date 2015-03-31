package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONException;
import org.json.JSONObject;

public class Strategy {

  private static final Logger LOG = Logger.getLogger(Strategy.class);

  private StepFactory stepFactory;
  private User user;
  private Step latestStep;
  private int strategyId;
  private boolean isSaved;
  private boolean isDeleted = false;
  private Date createdTime;
  private Date lastModifiedTime;
  private String projectId;
  private String signature;
  private String description;
  private String name;
  private String savedName = null;
  private boolean isPublic = false;

  private int latestStepId = 0;
  private int estimateSize;
  private String version;
  private boolean valid = true;
  private Date lastRunTime;
  private RecordClass recordClass;

  public Strategy(StepFactory factory, User user, int strategyId) {
    this.stepFactory = factory;
    this.user = user;
    this.strategyId = strategyId;
    isSaved = false;
  }

  public User getUser() {
    return user;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public String getVersion() {
    // if (latestStep != null)
    // version = latestStep.getAnswer().getProjectVersion();
    return version;
  }

  void setVersion(String version) {
    this.version = version;
  }

  public void setName(String name) {
    if (name != null && name.length() > StepFactory.COLUMN_NAME_LIMIT) {
      name = name.substring(0, StepFactory.COLUMN_NAME_LIMIT - 1);
    }
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setSavedName(String savedName) {
    if (savedName != null && savedName.length() > StepFactory.COLUMN_NAME_LIMIT) {
      savedName = savedName.substring(0, StepFactory.COLUMN_NAME_LIMIT - 1);
    }
    this.savedName = savedName;
  }

  public String getSavedName() {
    return savedName;
  }

  public void setIsSaved(boolean saved) {
    this.isSaved = saved;
  }

  public boolean getIsSaved() {
    return isSaved;
  }

  public boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public Step getLatestStep() throws WdkModelException {
    if (latestStep == null && latestStepId != 0)
      setLatestStep(stepFactory.loadStep(user, latestStepId));
    return latestStep;
  }

  public void setLatestStep(Step step) throws WdkModelException {
    stepFactory.verifySameOwnerAndProject(this, step);
    this.latestStep = step;
    // also update the cached info
    latestStepId = step.getStepId();
  }

  void setStrategyId(int strategyId) {
    this.strategyId = strategyId;
  }

  public int getStrategyId() {
    return strategyId;
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

  public Step getStep(int index) throws WdkModelException {
    return getLatestStep().getStep(index);
  }

  public List<Step> getMainBranch() throws WdkModelException {
    return getLatestStep().getMainBranch();
  }

  public int getLength() throws WdkModelException {
    return getLatestStep().getLength();
  }

  public void setLatestStepId(int stepId) {
    this.latestStepId = stepId;
    this.latestStep = null; // root step is now out of date
  }

  public int getLatestStepId() {
    return latestStepId;
  }

  public Step getStepById(int id) throws WdkModelException {
    return getLatestStep().getStepByDisplayId(id);
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
    stepFactory.updateStrategy(user, this, overwrite);
  }

  public RecordClass getRecordClass() throws WdkModelException {
    if (latestStep == null && recordClass != null)
      return recordClass;
    return getLatestStep().getRecordClass();
  }

  /**
   * Insert a new step before the target. The new step will become the previous step of the target, and the
   * old previousStep of the target should become the previousStep of the new step.
   * 
   * @param newStep the newStep
   * @param targetId
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<Integer, Integer> insertStepBefore(Step newStep, int targetId) throws WdkModelException,
      WdkUserException {
    Step targetStep = getStepById(targetId);
    stepFactory.verifySameOwnerAndProject(this, targetStep);

    Map<Integer, Integer> rootMap = new HashMap<>();

    // make sure the previousStep of the target is now the previousStep of newStep
    if (targetStep.isFirstStep()) { // inserting before first step will cause the first step being replaced by
      // the new step, while old first step will become the child of this new step
      if (newStep.getChildStep() == null || newStep.getChildStep().getStepId() != targetId)
        throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " before step #" +
            targetId + " since it will corrupt the structure of the strategy #" + strategyId);

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
          newStep.update(false);    // don't need to update LastRunTime
          targetStep.setCollapsible(false);
          targetStep.update(false);

          // check and set the newStep as the child of the parent, to replace the target step
          parentStep.checkChildAllowed(newStep);
          parentStep.setChildStep(newStep);
          parentStep.saveParamFilters();
          rootMap.put(targetId, newStep.getStepId());
        }
        else { // target is at the end of the strategy, set newStep as the end of the strategy
          setLatestStep(newStep);
          update(false);    // don't overwrite a saved strategy.
          rootMap.put(targetId, newStep.getStepId());
        }
      }
    }
    else { // target is not the first, then the previousStep of the target will become the previous of the
      // new step.
      if (targetStep.getPreviousStepId() != newStep.getPreviousStepId())
        throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " before step #" +
            targetId + " since it will corrupt the structure of the strategy #" + strategyId);

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
  public Map<Integer, Integer> insertStepAfter(Step newStep, int targetId) throws WdkModelException,
      WdkUserException {
    Map<Integer, Integer> rootMap = new HashMap<>();

    // make sure the newStep uses target step as its previousStep
    Step previousStep = newStep.getPreviousStep();
    if (previousStep == null || previousStep.getStepId() != targetId)
      throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " after step #" + targetId +
          " since it will corrupt the structure of the strategy #" + strategyId);

    Step targetStep = getStepById(targetId);

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
        setLatestStep(newStep);
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
   * 
   * 
   * @param step
   * @return a map of root id changes (both main & nested strategies) {old, new}; the information will be used
   *         to update the states of active strategies.
   * 
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<Integer, Integer> deleteStep(Step step) throws WdkModelException, WdkUserException {
    List<Step> deletes = new ArrayList<>();
    Map<Integer, Integer> rootMap = new HashMap<>();

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
            deletes.add(step);
          }
          else { // a transform step, and since there is no previousStep, it will also be deleted
            deletes.add(step);
          }
        }
        else { // otherwise, previous step exists, no more deletion needed. will exit loop.
          // check if the step can take the previous step
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
        rootMap.put(getLatestStepId(), previousStep.getStepId());
        setLatestStep(previousStep);
        update(true);
      }
      else if (step == null) { // no more steps left in the strategy, delete the strategy itself.
        stepFactory.deleteStrategy(strategyId);
        rootMap.clear();
      }
    }

    // after strategy is deleted (if needed), will now delete steps
    LOG.debug("Total " + deletes.size() + " steps deleted.");
    for (Step delete : deletes) {
      stepFactory.deleteStep(delete.getStepId());
    }

    return rootMap;

    // // are we deleting the first step?
    // if (step.isFirstStep()) {
    // if (step.getNextStep() != null) {
    // // if there are at least two steps, we need to turn the child
    // // step of step 2 into a first step (no operation)
    // while (step.getNextStep() != null && step.getNextStep().isTransform()) {
    // step = step.getNextStep();
    // }
    // if (step.getNextStep() == null) {// found the last step in a strategy, or a nested strategy
    // if (!isBranch) {
    // logger.debug("Step is only non-transform step in main strategy...");
    // this.setDeleted(true);
    // }
    // else {
    // logger.debug("Step is only non-transform step in branch...");
    // step = step.getParentStep();
    // targetStepId = step.getStepId();
    // step = step.getPreviousStep();
    // }
    //
    // }
    // else {
    // logger.debug("Moving to second step to replace first step...");
    // targetStepId = step.getNextStep().getStepId();
    // step = step.getNextStep().getChildStep();
    // }
    // }
    // else if (isBranch) {
    // logger.debug("Step is only step in a branch...");
    // step = step.getParentStep();
    // targetStepId = step.getStepId();
    // step = step.getPreviousStep();
    // }
    // else {
    // logger.debug("Deleting the single step in the strategy will cause the strategy to be deleted");
    // stepFactory.deleteStrategy(strategyId);
    // }
    // }
    // else {
    // logger.debug("Moving to previous step to replace non-first step...");
    // step = step.getPreviousStep();
    // }
    //
    // logger.debug("Updating step tree to delete target step...");
    // return updateStepTree(targetStepId, step);
  }

  // public Map<Integer, Integer> moveStep(int moveFromId, int moveToId, String branch)
  // throws WdkModelException, WdkUserException, SQLException {
  // Step targetStep;
  // if (branch == null) {
  // targetStep = getLatestStep();
  // }
  // else {
  // targetStep = getLatestStep().getStepByDisplayId(Integer.parseInt(branch));
  // }
  //
  // int moveFromIx = targetStep.getIndexFromId(moveFromId);
  // int moveToIx = targetStep.getIndexFromId(moveToId);
  //
  // Step moveFromStep = targetStep.getStep(moveFromIx);
  // Step moveToStep = targetStep.getStep(moveToIx);
  // Step step, newStep;
  //
  // int stubIx = Math.min(moveFromIx, moveToIx) - 1;
  // int targetStepId = targetStep.getStepId();
  // int length = targetStep.getLength();
  //
  // if (stubIx < 0) {
  // step = null;
  // }
  // else {
  // step = targetStep.getStep(stubIx);
  // }
  //
  // for (int i = stubIx + 1; i < length; ++i) {
  // if (i == moveToIx) {
  // if (step == null) {
  // step = moveFromStep.getChildStep();
  // }
  // else {
  // // assuming boolean, will need to add case for
  // // non-boolean op
  // Step rightStep = moveFromStep.getChildStep();
  // moveFromStep = user.createBooleanStep(step, rightStep, moveFromStep.getOperation(), false,
  // moveFromStep.getFilterName());
  // step = moveFromStep;
  // }
  // // again, assuming boolean, will need to add case for
  // // non-boolean
  // Step rightStep = moveToStep.getChildStep();
  // moveToStep = user.createBooleanStep(step, rightStep, moveToStep.getOperation(), false,
  // moveToStep.getFilterName());
  // step = moveToStep;
  // }
  // else if (i == moveFromIx) {
  // // do nothing; this step was moved, so we just ignore it.
  // }
  // else {
  // newStep = targetStep.getStep(i);
  // if (step == null) {
  // step = newStep.getChildStep();
  // }
  // else {
  // // again, assuming boolean, will need to add case for
  // // non-boolean
  // Step rightStep = newStep.getChildStep();
  // newStep = user.createBooleanStep(step, rightStep, newStep.getOperation(), false,
  // newStep.getFilterName());
  // step = moveToStep;
  // }
  // }
  // }
  //
  // return updateStepTree(targetStepId, step);
  // }

  // private void resetStepCounts(Step fromStep) throws WdkModelException {
  // stepFactory.resetStepCounts(fromStep);
  // }

  // private Map<Integer, Integer> updateStepTree(int targetStepId, Step newStep) throws WdkModelException,
  // WdkUserException, SQLException {
  // logger.debug("update step tree - target=" + targetStepId + ", newStep=" + newStep.getStepId());
  // Map<Integer, Integer> stepIdsMap = new HashMap<Integer, Integer>();
  // Step root = getLatestStep();
  // Step targetStep = root.getStepByDisplayId(targetStepId);
  //
  // int newStepId = newStep.getStepId();
  // stepIdsMap.put(targetStepId, newStepId);
  //
  // newStep.setCollapsible(targetStep.isCollapsible());
  // newStep.setCollapsedName(targetStep.getCollapsedName());
  // newStep.update(false);
  //
  // // update the parents/nexts
  // while (targetStep.getStepId() != root.getStepId()) {
  // Step parentStep = root.getStepByChildId(targetStepId);
  // Step nextStep = root.getStepByPreviousId(targetStepId);
  //
  // logger.debug("target: " + targetStep.getStepId() + ", parent: " + parentStep + ", next: " + nextStep);
  // if (parentStep == null && nextStep == null)
  // break;
  //
  // targetStep = (parentStep != null) ? parentStep : nextStep;
  //
  // // create a new step by replacing only the target step id in the
  // // params.
  // Question question = targetStep.getQuestion();
  // Map<String, String> values = targetStep.getParamValues();
  //
  // // filter out invalid params for the new step to use
  // Set<String> invalidParams = new LinkedHashSet<>();
  // Map<String, Param> params = question.getParamMap();
  // for (String paramName : values.keySet()) {
  // if (!params.containsKey(paramName))
  // invalidParams.add(paramName);
  // }
  // for (String paramName : invalidParams) {
  // values.remove(paramName);
  // }
  //
  // String paramName;
  // if (parentStep != null) {
  // paramName = targetStep.getChildStepParam();
  // }
  // else {
  // paramName = targetStep.getPreviousStepParam();
  // }
  // values.put(paramName, Integer.toString(newStepId));
  //
  // // replace newStep with new pnStep, and iterate to the parent/next
  // // node
  // newStep = user.createStep(question, values, targetStep.getFilter(), false, false,
  // targetStep.getAssignedWeight());
  // newStep.setCustomName(targetStep.getBaseCustomName());
  // newStep.setCollapsible(targetStep.isCollapsible());
  // newStep.setCollapsedName(targetStep.getCollapsedName());
  // newStep.update(false);
  //
  // Events.triggerAndWait(new StepCopiedEvent(targetStep, newStep), new WdkModelException(
  // "Unable to execute all operations subsequent to step copy."));
  //
  // newStepId = newStep.getStepId();
  // targetStepId = targetStep.getStepId();
  // stepIdsMap.put(targetStepId, newStepId);
  // }
  // // done with iteration, the target will be the original root, while the
  // // newStep will be the new root.
  //
  // this.setLatestStep(newStep);
  // this.update(false);
  //
  // return stepIdsMap;
  // }

  public Step getFirstStep() throws WdkModelException {
    return getLatestStep().getFirstStep();
  }

  /**
   * checksum of a strategy is different from signature in that signature is stable and it will never change
   * after the strategy is created, while checksum depends on many properties of a strategy, and it will
   * change when the strategies properties are changed.
   * 
   * @return
   * @throws JSONException
   * @throws NoSuchAlgorithmException
   * @throws WdkModelException
   * @throws SQLException
   * @throws WdkUserException
   */
  public String getChecksum() throws WdkModelException {
    JSONObject jsStrategy = getJSONContent(true);

    return Utilities.encrypt(jsStrategy.toString());
  }

  public JSONObject getJSONContent() throws WdkModelException {
    return getJSONContent(false);
  }

  public JSONObject getJSONContent(boolean forChecksum) throws WdkModelException {
    JSONObject jsStrategy = new JSONObject();

    try {
      jsStrategy.put("id", this.strategyId);
      jsStrategy.put("name", this.name);
      jsStrategy.put("savedName", this.savedName);
      jsStrategy.put("description", this.description);
      jsStrategy.put("saved", this.isSaved);
      jsStrategy.put("deleted", this.isDeleted);
      jsStrategy.put("type", getRecordClass().getFullName());

      if (!forChecksum) {
        jsStrategy.put("valid", isValid());
        jsStrategy.put("version", getVersion());
        jsStrategy.put("resultSize", getEstimateSize());
      }

      JSONObject stepContent = getLatestStep().getJSONContent(this.strategyId, forChecksum);
      jsStrategy.put("latestStep", stepContent);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsStrategy;
  }

  /**
   * @return the valid
   * @throws JSONException
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public boolean isValid() throws WdkModelException {
    if (latestStep == null)
      getLatestStep();
    if (latestStep != null)
      valid = latestStep.isValid();
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  /**
   * @return the lastRunTime
   */
  public Date getLastRunTime() {
    if (latestStep != null)
      lastRunTime = latestStep.getLastRunTime();
    return lastRunTime;
  }

  /**
   * @param lastRunTime
   *          the lastRunTime to set
   */
  public void setLastRunTime(Date lastRunTime) {
    this.lastRunTime = lastRunTime;
  }

  /**
   * @return the lastModifiedTime
   */
  public Date getLastModifiedTime() {
    return lastModifiedTime;
  }

  /**
   * @param lastModifiedTime
   *          the lastModifiedTime to set
   */
  public void setLastModifiedTime(Date lastModifiedTime) {
    this.lastModifiedTime = lastModifiedTime;
  }

  /**
   * checksum of a strategy is different from signature in that signature is stable and it will never change
   * after the strategy is created, while checksum depends on many properties of a strategy, and it will
   * change when the strategies properties are changed.
   * 
   * @return the signature
   */
  public String getSignature() {
    return signature;
  }

  /**
   * @param signature
   *          the signature to set
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  public int getEstimateSize() {
    if (latestStep != null)
      estimateSize = latestStep.getEstimateSize();
    return estimateSize;
  }

  void setEstimateSize(int estimateSize) {
    this.estimateSize = estimateSize;
  }

  public void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
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
    stack.push(getLatestStep());
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
    stack.push(getLatestStep());
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
}
