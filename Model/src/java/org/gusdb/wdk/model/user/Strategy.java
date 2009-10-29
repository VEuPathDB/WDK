package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.json.JSONException;
import org.json.JSONObject;

public class Strategy {

    private static final Logger logger = Logger.getLogger(Strategy.class);

    private StepFactory stepFactory;
    private User user;
    private Step latestStep;
    private int displayId;
    private int internalId;
    private boolean isSaved;
    private boolean isDeleted = false;
    private Date createdTime;
    private Date lastViewedTime;
    private Date lastModifiedTime;
    private String signature;
    private String description;
    private String name;
    private String savedName = null;

    Strategy(StepFactory factory, User user, int displayId, int internalId) {
        this.stepFactory = factory;
        this.user = user;
        this.displayId = displayId;
        this.internalId = internalId;
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
        if (savedName != null
                && savedName.length() > StepFactory.COLUMN_NAME_LIMIT) {
            savedName = savedName.substring(0,
                    StepFactory.COLUMN_NAME_LIMIT - 1);
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

    public Step getLatestStep() {
        return latestStep;
    }

    void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public int getStrategyId() {
        return displayId;
    }

    public int getInternalId() {
        return internalId;
    }

    void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    /**
     * @return Returns the createTime.
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createTime
     *            The createTime to set.
     */
    void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return Returns the lastRunTime.
     */
    public Date getLastRunTime() {
        return latestStep.getLastRunTime();
    }

    public Step getStep(int index) {
        return latestStep.getStep(index);
    }

    public Step[] getAllSteps() {
        return latestStep.getAllSteps();
    }

    public int getLength() {
        return latestStep.getLength();
    }

    public void addStep(Step step) throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        if (latestStep != null) {
            latestStep.addStep(step);
        }
        setLatestStep(step);
    }

    public void setLatestStep(Step step) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        this.latestStep = step;
    }

    public Step getStepById(int id) {
        return latestStep.getStepByDisplayId(id);
    }

    public void update(boolean overwrite) throws WdkUserException,
            WdkModelException, SQLException, JSONException,
            NoSuchAlgorithmException {
        stepFactory.updateStrategy(user, this, overwrite);
    }

    public String getType() {
        return latestStep.getType();
    }

    public Map<Integer, Integer> addStep(int targetStepId, Step step)
            throws WdkModelException, WdkUserException, JSONException,
            NoSuchAlgorithmException, SQLException {
        return updateStepTree(targetStepId, step);
    }

    public Map<Integer, Integer> editOrInsertStep(int targetStepId, Step step)
            throws WdkModelException, WdkUserException, JSONException,
            NoSuchAlgorithmException, SQLException {
        logger.debug("Edit/Insert - target: " + targetStepId + ", new step: "
                + step.getDisplayId());

        return updateStepTree(targetStepId, step);
    }

    public Map<Integer, Integer> deleteStep(int stepId, boolean isBranch)
            throws WdkModelException, WdkUserException, JSONException,
            NoSuchAlgorithmException, SQLException {
        Step step = latestStep.getStepByDisplayId(stepId);
        int targetStepId = step.getDisplayId();

        // are we deleting the first step?
        if (step.isFirstStep()) {
            if (step.getNextStep() != null) {
                // if there are at least two steps, we need to turn the child
                // step
                // of step 2 into a first step (no operation)
                while (step.getNextStep() != null
                        && step.getNextStep().isTransform()) {
                    step = step.getNextStep();
                }
                if (step.getNextStep() == null) {
                    if (!isBranch) {
                        logger.debug("Step is only non-transform step in main strategy...");
                        this.setDeleted(true);
                    } else {
                        logger.debug("Step is only non-transform step in branch...");
                        step = step.getParentStep();
                        targetStepId = step.getDisplayId();
                        step = step.getPreviousStep();
                    }

                } else {
                    logger.debug("Moving to second step to replace first step...");
                    targetStepId = step.getNextStep().getDisplayId();
                    step = step.getNextStep().getChildStep();
                }
            } else if (isBranch) {
                logger.debug("Step is only step in a branch...");
                step = step.getParentStep();
                targetStepId = step.getDisplayId();
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
            String branch) throws WdkModelException, WdkUserException,
            JSONException, NoSuchAlgorithmException, SQLException {
        Step targetStep;
        if (branch == null) {
            targetStep = latestStep;
        } else {
            targetStep = latestStep.getStepByDisplayId(Integer.parseInt(branch));
        }

        int moveFromIx = targetStep.getIndexFromId(moveFromId);
        int moveToIx = targetStep.getIndexFromId(moveToId);

        Step moveFromStep = targetStep.getStep(moveFromIx);
        Step moveToStep = targetStep.getStep(moveToIx);
        Step step, newStep;

        int stubIx = Math.min(moveFromIx, moveToIx) - 1;
        int targetStepId = targetStep.getDisplayId();
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
                moveToStep = user.createBooleanStep(step, rightStep, moveToStep.getOperation(),
                        false, moveToStep.getFilterName());
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
                    newStep = user.createBooleanStep(step, rightStep, newStep.getOperation(),
                            false, newStep.getFilterName());
                    step = moveToStep;
                }
            }
        }

        return updateStepTree(targetStepId, step);
    }

    private Map<Integer, Integer> updateStepTree(int targetStepId, Step newStep)
            throws WdkModelException, WdkUserException, JSONException,
            NoSuchAlgorithmException, SQLException {
        Map<Integer, Integer> stepIdsMap = new HashMap<Integer, Integer>();
        Step targetStep = latestStep.getStepByDisplayId(targetStepId);

        stepIdsMap.put(new Integer(targetStep.getDisplayId()), new Integer(
                newStep.getDisplayId()));

        while (targetStep.getNextStep() != null) {
            targetStep = targetStep.getNextStep();
            if (targetStep.isTransform()) {
                newStep = updateTransform(targetStep,
                        newStep.getQuestion().getRecordClass(),
                        newStep.getDisplayId());
            } else {
                Step rightStep = targetStep.getChildStep();
                newStep = user.createBooleanStep(newStep, rightStep, targetStep.getOperation(),
                        false, targetStep.getFilterName());
            }
            stepIdsMap.put(new Integer(targetStep.getDisplayId()), new Integer(
                    newStep.getDisplayId()));
        }

        newStep.setParentStep(targetStep.getParentStep());
        newStep.setCollapsible(targetStep.isCollapsible());
        newStep.setCollapsedName(targetStep.getCollapsedName());
        newStep.update(false);

        if (targetStep.isCollapsible()
                && newStep.getPreviousStep() != null
                && newStep.getPreviousStep().getDisplayId() == targetStep.getDisplayId()) {
            if (getIsSaved()) {
                // If the strategy is saved, clone the target step so we don't
                // wipe out
                // collapsed name for the saved strategy
                targetStep = targetStep.deepClone();
                Step parent = newStep.getParentStep();
                if (newStep.isTransform()) {
                    newStep = updateTransform(newStep,
                            targetStep.getQuestion().getRecordClass(),
                            targetStep.getDisplayId());
                } else {
                    Step rightStep = newStep.getChildStep();
                    newStep = user.createBooleanStep(targetStep, rightStep,
                            newStep.getOperation(), false, newStep.getFilterName());
                }
                newStep.setParentStep(parent);
                newStep.setCollapsible(targetStep.isCollapsible());
                newStep.setCollapsedName(targetStep.getCollapsedName());
                newStep.update(false);
            }
            // Make sure target step is made uncollapsible so that
            // we don't have incorrect references in the step tree
            targetStep.setParentStep(null);
            targetStep.setCollapsible(false);
            targetStep.setCollapsedName(null);
            targetStep.update(false);
        }

        // if step has a parent step, need to continue
        // updating the rest of the strategy.
        while (newStep.getParentStep() != null) {
            // go to parent, update subsequent steps
            targetStep = newStep.getParentStep();

            Step leftStep = targetStep.getPreviousStep();
            // update parent, then update subsequent
            newStep = user.createBooleanStep(leftStep, newStep, targetStep.getOperation(),
                    false, targetStep.getFilterName());
            stepIdsMap.put(new Integer(targetStep.getDisplayId()), new Integer(
                    newStep.getDisplayId()));
            while (targetStep.getNextStep() != null) {
                targetStep = targetStep.getNextStep();
                // need to check if step is a transform (in which case there's
                // no boolean expression; we need to update history param
                if (targetStep.isTransform()) {
                    newStep = updateTransform(targetStep,
                            newStep.getQuestion().getRecordClass(),
                            newStep.getDisplayId());
                } else {
                    Step rightStep = targetStep.getChildStep();
                    newStep = user.createBooleanStep(newStep, rightStep,
                            targetStep.getOperation(), false, targetStep.getFilterName());
                }
                stepIdsMap.put(new Integer(targetStep.getDisplayId()),
                        new Integer(newStep.getDisplayId()));
            }
            newStep.setParentStep(targetStep.getParentStep());
            newStep.setCollapsible(targetStep.isCollapsible());
            newStep.setCollapsedName(targetStep.getCollapsedName());
            newStep.update(false);

            // Make sure target step is made uncollapsible so that
            // we don't have incorrect references in the step tree
            /*
             * targetStep.setParentStep(null); targetStep.setCollapsible(false);
             * targetStep.setCollapsedName(null); targetStep.update(false);
             */
        }

        this.setLatestStep(newStep);
        this.update(false);

        return stepIdsMap;
    }

    private Step updateTransform(Step step, RecordClass recordClass,
            int newStepId) throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        // Get question
        Question wdkQuestion = step.getQuestion();
        // Get internal params
        Map<String, String> paramValues = step.getParamValues();
        // Change HistoryParam
        AnswerParam[] answerParams = wdkQuestion.getTransformParams(recordClass);
        for (AnswerParam p : answerParams) {
            paramValues.put(p.getName(), Integer.toString(newStepId));
        }
        AnswerFilterInstance filter = step.getFilter();
        String filterName = (filter == null) ? null : filter.getName();

        Step newStep = user.createStep(wdkQuestion, paramValues, filterName,
                step.isDeleted(), false);
        newStep.setCustomName(step.getBaseCustomName());
        newStep.update(false);
        return newStep;
    }

    public Step getFirstStep() {
        return latestStep.getFirstStep();
    }

    /**
     * checksum of a strategy is different from signature in that signature is
     * stable and it will never change after the strategy is created, while
     * checksum depends on many properties of a strategy, and it will change
     * when the strategies properties are changed.
     * 
     * @return
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     */
    public String getChecksum() throws JSONException, NoSuchAlgorithmException,
            WdkModelException {
        JSONObject jsStrategy = getJSONContent();
        return Utilities.encrypt(jsStrategy.toString());
    }

    public JSONObject getJSONContent() throws JSONException {
        JSONObject jsStrategy = new JSONObject();
        jsStrategy.put("id", this.displayId);
        jsStrategy.put("name", this.name);
        jsStrategy.put("savedName", this.savedName);
        jsStrategy.put("saved", this.isSaved);
        jsStrategy.put("deleted", this.isDeleted);
        jsStrategy.put("valid", this.isValid());
        jsStrategy.put("latestStep", latestStep.getJSONContent(this.displayId));

        return jsStrategy;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return latestStep.isValid();
    }

    /**
     * @return the lastViewedTime
     */
    public Date getLastViewedTime() {
        return lastViewedTime;
    }

    /**
     * @param lastViewedTime
     *            the lastViewedTime to set
     */
    public void setLastViewedTime(Date lastViewedTime) {
        this.lastViewedTime = lastViewedTime;
    }

    /**
     * @return the lastModifiedTime
     */
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * @param lastModifiedTime
     *            the lastModifiedTime to set
     */
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * checksum of a strategy is different from signature in that signature is
     * stable and it will never change after the strategy is created, while
     * checksum depends on many properties of a strategy, and it will change
     * when the strategies properties are changed.
     * 
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature
     *            the signature to set
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
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
