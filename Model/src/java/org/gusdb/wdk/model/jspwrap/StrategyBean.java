package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONException;

public class StrategyBean {
    Strategy strategy;

    public StrategyBean(Strategy strategy) {
        this.strategy = strategy;
    }

    public UserBean getUser() {
        return new UserBean(strategy.getUser());
    }

    public boolean getIsDeleted() {
	return strategy.isDeleted();
    }

    public String getName() {
        return strategy.getName();
    }

    public void setName(String name) {
        strategy.setName(name);
    }

    public String getSavedName() {
        return strategy.getSavedName();
    }

    public void setSavedName(String name) {
        strategy.setSavedName(name);
    }

    public void setIsSaved(boolean saved) {
        strategy.setIsSaved(saved);
    }

    public boolean getIsSaved() {
        return strategy.getIsSaved();
    }

    public String getLastRunTimeFormatted() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(strategy.getLastRunTime());
    }

    public Date getLastRunTime() {
        return strategy.getLastRunTime();
    }

    public String getCreatedTimeFormatted() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(strategy.getCreatedTime());
    }

    public Date getCreatedTime() {
        return strategy.getCreatedTime();
    }

    public StepBean getLatestStep() {
        return new StepBean(strategy.getLatestStep());
    }

    public int getStrategyId() {
        return strategy.getDisplayId();
    }

    public int getInternalId() {
        return strategy.getInternalId();
    }

    public StepBean getStep(int index) {
        StepBean latestStep = new StepBean(strategy.getLatestStep());
        return latestStep.getStep(index);
    }

    public StepBean[] getAllSteps() {
        StepBean latestStep = new StepBean(strategy.getLatestStep());
        return latestStep.getAllSteps();
    }

    public void addStep(StepBean step) throws WdkUserException {
        strategy.addStep(step.step);
    }

    public void setLatestStep(StepBean step) throws WdkUserException {
        strategy.setLatestStep(step.step);
    }

    public StepBean getStepById(int stepId) {
        Step target = strategy.getStepById(stepId);
        if (target != null) {
            return new StepBean(target);
        }
        return null;
    }

    public int getLength() {
        return getAllSteps().length;
    }

    public void update(boolean overwrite) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        strategy.update(overwrite);
    }

    public Map<Integer,Integer> addStep(int targetStepId, StepBean step) 
	throws WdkModelException, WdkUserException, JSONException, 
	       NoSuchAlgorithmException, SQLException {
	return strategy.addStep(targetStepId, step.step);
    }

    public Map<Integer,Integer> editOrInsertStep(int targetStepId, StepBean step) 
	throws WdkModelException, WdkUserException, JSONException, 
	       NoSuchAlgorithmException, SQLException {
	return strategy.editOrInsertStep(targetStepId, step.step);
    }

    public Map<Integer,Integer> moveStep(int moveFromId, int moveToId, String branch) 
	throws WdkModelException, WdkUserException, JSONException, 
	       NoSuchAlgorithmException, SQLException {
	return strategy.moveStep(moveFromId, moveToId, branch);
    }

    public Map<Integer,Integer> deleteStep(int stepId, boolean isBranch) 
	throws WdkModelException, WdkUserException, JSONException, 
	       NoSuchAlgorithmException, SQLException {
	return strategy.deleteStep(stepId, isBranch);
    }
}
