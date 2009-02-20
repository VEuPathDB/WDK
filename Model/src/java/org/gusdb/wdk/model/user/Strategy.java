package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

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
    private String name;
    private String savedName = null;

    Strategy(StepFactory factory, User user, int displayId, int internalId,
            String name) {
        this.stepFactory = factory;
        this.user = user;
        this.displayId = displayId;
        this.internalId = internalId;
        this.name = name;
        isSaved = false;
    }

    public User getUser() {
        return user;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    private void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSavedName(String savedName) {
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

    public int getDisplayId() {
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
            WdkModelException, SQLException, JSONException {
        stepFactory.updateStrategy(user, this, overwrite);
    }

    public String getType() throws NoSuchAlgorithmException, WdkModelException,
            JSONException, WdkUserException, SQLException {
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
		while (step.getNextStep() != null && step.getNextStep().isTransform()) {
		    step = step.getNextStep();
		}
		if (step.getNextStep() == null) {
		    if (!isBranch) {
			System.out.println("Step is only non-transform step in main strategy...");
			this.setDeleted(true);
		    }
		    else {
			System.out.println("Step is only non-transform step in branch...");
			step = step.getParentStep();
			targetStepId = step.getDisplayId();
			step = step.getPreviousStep();
		    }
			
		}
		else {
		    System.out.println("Moving to second step to replace first step...");
		    targetStepId = step.getNextStep().getDisplayId();
		    step = step.getNextStep().getChildStep();
		}
	    }
	    else if (isBranch) {
                System.out.println("Step is only step in a branch...");
		step = step.getParentStep();
		targetStepId = step.getDisplayId();
		step = step.getPreviousStep();
	    }
	    else {
                System.out.println("Step is only step in main strategy...");
                this.setDeleted(true);
            }
        } else {
            System.out.println("Moving to previous step to replace non-first step...");
            step = step.getPreviousStep();
        }

        System.out.println("Updating step tree to delete target step...");
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
                    BooleanOperator operator = BooleanOperator.parse(moveFromStep.getOperation());
                    AnswerFilterInstance targetFilter = moveFromStep.getAnswer().getAnswerValue().getFilter();
                    Step rightStep = moveFromStep.getChildStep();
                    moveFromStep = user.createBooleanStep(step, rightStep,
                            operator, false, targetFilter);
                    step = moveFromStep;
                }
                // again, assuming boolean, will need to add case for
                // non-boolean
                BooleanOperator operator = BooleanOperator.parse(moveToStep.getOperation());
                AnswerFilterInstance targetFilter = moveToStep.getAnswer().getAnswerValue().getFilter();
                Step rightStep = moveToStep.getChildStep();
                moveToStep = user.createBooleanStep(step, rightStep, operator,
                        false, targetFilter);
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
                    BooleanOperator operator = BooleanOperator.parse(newStep.getOperation());
                    AnswerFilterInstance targetFilter = newStep.getAnswer().getAnswerValue().getFilter();
                    Step rightStep = newStep.getChildStep();
                    newStep = user.createBooleanStep(step, rightStep, operator,
                            false, targetFilter);
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
                newStep = updateTransform(targetStep, newStep.getDisplayId());
            } else {
                BooleanOperator operator = BooleanOperator.parse(targetStep.getOperation());
                AnswerFilterInstance targetFilter = targetStep.getAnswer().getAnswerValue().getFilter();
                Step rightStep = targetStep.getChildStep();
                newStep = user.createBooleanStep(newStep, rightStep, operator,
                        false, targetFilter);
            }
            stepIdsMap.put(new Integer(targetStep.getDisplayId()), new Integer(
                    newStep.getDisplayId()));
        }

        newStep.setParentStep(targetStep.getParentStep());
        newStep.setCollapsible(targetStep.isCollapsible());
        newStep.setCollapsedName(targetStep.getCollapsedName());
        newStep.update(false);

	// Make sure target step is made uncollapsible so that
	// we don't have incorrect references in the step tree
	targetStep.setParentStep(null);
	targetStep.setCollapsible(false);
	targetStep.setCollapsedName(null);
	targetStep.update(false);

        // if step has a parent step, need to continue
        // updating the rest of the strategy.
        while (newStep.getParentStep() != null) {
            // go to parent, update subsequent steps
            targetStep = newStep.getParentStep();

            BooleanOperator operator = BooleanOperator.parse(targetStep.getOperation());
            AnswerFilterInstance targetFilter = targetStep.getAnswer().getAnswerValue().getFilter();
            Step leftStep = targetStep.getPreviousStep();
            // update parent, then update subsequent
            newStep = user.createBooleanStep(leftStep, newStep, operator,
                    false, targetFilter);
            stepIdsMap.put(new Integer(targetStep.getDisplayId()), new Integer(
                    newStep.getDisplayId()));
            while (targetStep.getNextStep() != null) {
                targetStep = targetStep.getNextStep();
                // need to check if step is a transform (in which case there's
                // no boolean expression; we need to update history param
                if (targetStep.isTransform()) {
                    newStep = updateTransform(targetStep,
                            newStep.getDisplayId());
                } else {
                    operator = BooleanOperator.parse(targetStep.getOperation());
                    targetFilter = targetStep.getAnswer().getAnswerValue().getFilter();
                    Step rightStep = targetStep.getChildStep();
                    newStep = user.createBooleanStep(newStep, rightStep,
                            operator, false, targetFilter);
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
	    targetStep.setParentStep(null);
	    targetStep.setCollapsible(false);
	    targetStep.setCollapsedName(null);
	    targetStep.update(false);
        }

        this.setLatestStep(newStep);
        this.update(false);

        return stepIdsMap;
    }

    private Step updateTransform(Step step, int newStepId)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        /*
         * TODO: Need to convert this to use backend objects now, unless
         * transforms are filters? Question wdkQuestion; Map<String, String>
         * internalParams;
         * 
         * // Get question wdkQuestion =
         * step.getAnswer().getAnswerValue().getQuestion(); Param[] params =
         * wdkQuestion.getParams(); // Get internal params internalParams =
         * step.getAnswer().getAnswerValue().getParams(); // Change HistoryParam
         * AnswerParam answerParam = null; for (Param param : params) { if
         * (param instanceof AnswerParam) { answerParam = (AnswerParam) param; }
         * }
         * 
         * internalParams.put(answerParam.getName(), user.getSignature() + ":" +
         * newStepId);
         * 
         * AnswerFilterInstance filter = step.getAnswerValue().getFilter();
         * String filterName = (filter == null) ? null : filter.getName();
         * 
         * Step newStep = user.createStep(wdkQuestion, internalParams,
         * filterName); newStep.setCustomName(step.getBaseCustomName());
         * newStep.update(false); return newStep;
         */
        return step;
    }
}
