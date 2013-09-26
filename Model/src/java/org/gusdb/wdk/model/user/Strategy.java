package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.json.JSONException;
import org.json.JSONObject;

public class Strategy {

  private static final Logger logger = Logger.getLogger(Strategy.class);

  private StepFactory stepFactory;
  private User user;
  private Step latestStep;
  private int strategyId;
  private boolean isSaved;
  private boolean isDeleted = false;
  private Date createdTime;
  private Date lastModifiedTime;
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

  Strategy(StepFactory factory, User user, int strategyId) {
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
    
  public Step getLatestStep() throws WdkModelException {
    if (latestStep == null && latestStepId != 0)
      setLatestStep(stepFactory.loadStep(user, latestStepId));
    return latestStep;
  }

  public void setLatestStep(Step step) {
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

  public Step[] getAllSteps() throws WdkModelException {
    return getLatestStep().getAllSteps();
  }

  public int getLength() throws WdkModelException {
    return getLatestStep().getLength();
  }

  public void setLatestStepId(int displayId) {
    this.latestStepId = displayId;
  }

  public int getLatestStepId() {
    return latestStepId;
  }

  public Step getStepById(int id) throws WdkModelException {
    return getLatestStep().getStepByDisplayId(id);
  }

  public void update(boolean overwrite) throws WdkModelException,
      WdkUserException, SQLException {
    stepFactory.updateStrategy(user, this, overwrite);
  }

  public RecordClass getRecordClass() throws WdkModelException {
    if (latestStep == null && recordClass != null) return recordClass;
    return getLatestStep().getRecordClass();
  }

  public Map<Integer, Integer> addStep(int targetStepId, Step step)
      throws WdkModelException, WdkUserException, SQLException {
    return updateStepTree(targetStepId, step);
  }

  public Map<Integer, Integer> editOrInsertStep(int targetStepId, Step step)
      throws WdkModelException, WdkUserException, SQLException {
    logger.debug("Edit/Insert - target: " + targetStepId + ", new step: "
        + step.getStepId());

    return updateStepTree(targetStepId, step);
  }

  public Map<Integer, Integer> deleteStep(int stepId, boolean isBranch)
      throws WdkModelException, WdkUserException, SQLException {
    Step step = getStepById(stepId);
    int targetStepId = step.getStepId();

    // are we deleting the first step?
    if (step.isFirstStep()) {
      if (step.getNextStep() != null) {
        // if there are at least two steps, we need to turn the child
        // step
        // of step 2 into a first step (no operation)
        while (step.getNextStep() != null && step.getNextStep().isTransform()) {
          step = step.getNextStep();
        }
        if (step.getNextStep() == null) {
          if (!isBranch) {
            logger.debug("Step is only non-transform step in main strategy...");
            this.setDeleted(true);
          } else {
            logger.debug("Step is only non-transform step in branch...");
            step = step.getParentStep();
            targetStepId = step.getStepId();
            step = step.getPreviousStep();
          }

        } else {
          logger.debug("Moving to second step to replace first step...");
          targetStepId = step.getNextStep().getStepId();
          step = step.getNextStep().getChildStep();
        }
      } else if (isBranch) {
        logger.debug("Step is only step in a branch...");
        step = step.getParentStep();
        targetStepId = step.getStepId();
        step = step.getPreviousStep();
      } else {
        logger.debug("Step is only step in main strategy...");
        this.setDeleted(true);
      }
    } else {
      logger.debug("Moving to previous step to replace non-first step...");
      step = step.getPreviousStep();
    }

    logger.debug("Updating step tree to delete target step...");
    return updateStepTree(targetStepId, step);
  }

  public Map<Integer, Integer> moveStep(int moveFromId, int moveToId,
      String branch) throws WdkModelException, WdkUserException, SQLException {
    Step targetStep;
    if (branch == null) {
      targetStep = getLatestStep();
    } else {
      targetStep = getLatestStep().getStepByDisplayId(Integer.parseInt(branch));
    }

    int moveFromIx = targetStep.getIndexFromId(moveFromId);
    int moveToIx = targetStep.getIndexFromId(moveToId);

    Step moveFromStep = targetStep.getStep(moveFromIx);
    Step moveToStep = targetStep.getStep(moveToIx);
    Step step, newStep;

    int stubIx = Math.min(moveFromIx, moveToIx) - 1;
    int targetStepId = targetStep.getStepId();
    int length = targetStep.getLength();

    if (stubIx < 0) {
      step = null;
    } else {
      step = targetStep.getStep(stubIx);
    }

    for (int i = stubIx + 1; i < length; ++i) {
      if (i == moveToIx) {
        if (step == null) {
          step = moveFromStep.getChildStep();
        } else {
          // assuming boolean, will need to add case for
          // non-boolean op
          Step rightStep = moveFromStep.getChildStep();
          moveFromStep = user.createBooleanStep(step, rightStep,
              moveFromStep.getOperation(), false, moveFromStep.getFilterName());
          step = moveFromStep;
        }
        // again, assuming boolean, will need to add case for
        // non-boolean
        Step rightStep = moveToStep.getChildStep();
        moveToStep = user.createBooleanStep(step, rightStep,
            moveToStep.getOperation(), false, moveToStep.getFilterName());
        step = moveToStep;
      } else if (i == moveFromIx) {
        // do nothing; this step was moved, so we just ignore it.
      } else {
        newStep = targetStep.getStep(i);
        if (step == null) {
          step = newStep.getChildStep();
        } else {
          // again, assuming boolean, will need to add case for
          // non-boolean
          Step rightStep = newStep.getChildStep();
          newStep = user.createBooleanStep(step, rightStep,
              newStep.getOperation(), false, newStep.getFilterName());
          step = moveToStep;
        }
      }
    }

    return updateStepTree(targetStepId, step);
  }

  private Map<Integer, Integer> updateStepTree(int targetStepId, Step newStep)
      throws WdkModelException, WdkUserException, SQLException {
    logger.debug("update step tree - target=" + targetStepId + ", newStep="
        + newStep.getStepId());
    Map<Integer, Integer> stepIdsMap = new HashMap<Integer, Integer>();
    Step root = getLatestStep();
    Step targetStep = root.getStepByDisplayId(targetStepId);

    int newStepId = newStep.getStepId();
    stepIdsMap.put(targetStepId, newStepId);

    newStep.setCollapsible(targetStep.isCollapsible());
    newStep.setCollapsedName(targetStep.getCollapsedName());
    newStep.update(false);

    // update the parents/nexts
    while (targetStep.getStepId() != root.getStepId()) {
      Step parentStep = root.getStepByChildId(targetStepId);
      Step nextStep = root.getStepByPreviousId(targetStepId);

      logger.debug("target: " + targetStep.getStepId() + ", parent: "
          + parentStep + ", next: " + nextStep);
      if (parentStep == null && nextStep == null) break;

      targetStep = (parentStep != null) ? parentStep : nextStep;

      // create a new step by replacing only the target step id in the
      // params.
      Question question = targetStep.getQuestion();
      Map<String, String> values = targetStep.getParamValues();

      // filter out invalid params for the new step to use
      Set<String> invalidParams = new LinkedHashSet<>();
      Map<String, Param> params = question.getParamMap();
      for (String paramName : values.keySet()) {
        if (!params.containsKey(paramName)) invalidParams.add(paramName);
      }
      for (String paramName : invalidParams) {
        values.remove(paramName);
      }

      String paramName;
      if (parentStep != null) {
        paramName = targetStep.getChildStepParam();
      } else {
        paramName = targetStep.getPreviousStepParam();
      }
      values.put(paramName, Integer.toString(newStepId));

      // replace newStep with new pnStep, and iterate to the parent/next
      // node
      newStep = user.createStep(question, values, targetStep.getFilter(),
          false, false, targetStep.getAssignedWeight());
      newStep.setCustomName(targetStep.getBaseCustomName());
      newStep.setCollapsible(targetStep.isCollapsible());
      newStep.setCollapsedName(targetStep.getCollapsedName());
      newStep.update(false);

      newStepId = newStep.getStepId();
      targetStepId = targetStep.getStepId();
      stepIdsMap.put(targetStepId, newStepId);
    }
    // done with iteration, the target will be the original root, while the
    // newStep will be the new root.

    this.setLatestStep(newStep);
    this.update(false);

    return stepIdsMap;
  }

  public Step getFirstStep() throws WdkModelException {
    return getLatestStep().getFirstStep();
  }

  /**
   * checksum of a strategy is different from signature in that signature is
   * stable and it will never change after the strategy is created, while
   * checksum depends on many properties of a strategy, and it will change when
   * the strategies properties are changed.
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
    } catch (JSONException ex) {
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
    if (latestStep != null) valid = latestStep.isValid();
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  /**
   * @return the lastRunTime
   */
  public Date getLastRunTime() {
    if (latestStep != null) lastRunTime = latestStep.getLastRunTime();
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
   * checksum of a strategy is different from signature in that signature is
   * stable and it will never change after the strategy is created, while
   * checksum depends on many properties of a strategy, and it will change when
   * the strategies properties are changed.
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
    if (latestStep != null) estimateSize = latestStep.getEstimateSize();
    return estimateSize;
  }

  void setEstimateSize(int estimateSize) {
    this.estimateSize = estimateSize;
  }

  public void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
  }

}
