package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;

public class StrategyBean {

    private UserBean user;
    Strategy strategy;

    public StrategyBean(UserBean user, Strategy strategy) {
        this.user = user;
        this.strategy = strategy;
    }

    public UserBean getUser() {
        return user;
    }

    public boolean getIsDeleted() {
        return strategy.isDeleted();
    }

    public String getVersion() {
        return strategy.getVersion();
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
        return formatDate(strategy.getLastRunTime());
    }

    public Date getLastRunTime() {
        return strategy.getLastRunTime();
    }

    public String getCreatedTimeFormatted() {
        return formatDate(strategy.getCreatedTime());
    }

    public Date getCreatedTime() {
        return strategy.getCreatedTime();
    }

    public String getLastModifiedTimeFormatted() {
        return formatDate(strategy.getLastModifiedTime());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Strategy#getLastModifiedTime()
     */
    public Date getLastModifiedTime() {
        return strategy.getLastModifiedTime();
    }

    public StepBean getLatestStep() throws WdkModelException {
        return new StepBean(user, strategy.getLatestStep());
    }
    
    public int getLatestStepId() {
        return strategy.getLatestStepId();
    }

    public int getStrategyId() {
        return strategy.getStrategyId();
    }

    public StepBean getStep(int index) throws WdkModelException {
        return new StepBean(user, strategy.getStep(index));
    }

    public StepBean[] getAllSteps() throws WdkModelException {
        StepBean latestStep = new StepBean(user, strategy.getLatestStep());
        return latestStep.getAllSteps();
    }

    // public void addStep(StepBean step) throws WdkUserException,
    // WdkModelException, SQLException, JSONException {
    // strategy.addStep(step.step);
    // }

    public void setLatestStep(StepBean step) {
        strategy.setLatestStep(step.step);
    }

    public StepBean getStepById(int stepId) throws WdkModelException {
        Step target = strategy.getStepById(stepId);
        if (target != null) {
            return new StepBean(user, target);
        }
        return null;
    }

    public int getLength() throws WdkModelException {
        return getAllSteps().length;
    }

    public void update(boolean overwrite) throws WdkUserException,
            WdkModelException, SQLException {
        strategy.update(overwrite);
    }

    public Map<Integer, Integer> addStep(int targetStepId, StepBean step) throws WdkModelException, WdkUserException, SQLException {
        return strategy.addStep(targetStepId, step.step);
    }

    public Map<Integer, Integer> editOrInsertStep(int targetStepId,
            StepBean step) throws WdkModelException, WdkUserException, SQLException  {
        return strategy.editOrInsertStep(targetStepId, step.step);
    }

    public Map<Integer, Integer> moveStep(int moveFromId, int moveToId,
            String branch) throws WdkModelException, WdkUserException, SQLException  {
        return strategy.moveStep(moveFromId, moveToId, branch);
    }

    public Map<Integer, Integer> deleteStep(int stepId, boolean isBranch) throws WdkModelException, WdkUserException, SQLException
             {
        return strategy.deleteStep(stepId, isBranch);
    }

    public String getImportId() {
        return strategy.getSignature();
    }

    public StepBean getFirstStep() throws WdkModelException {
        return new StepBean(user, strategy.getFirstStep());
    }

    public String getChecksum() throws WdkModelException {
        return strategy.getChecksum();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Strategy#getDescription()
     */
    public String getDescription() {
        return strategy.getDescription();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Strategy#setDescription()
     */
    public void setDescription(String description) {
        strategy.setDescription(description);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Strategy#getSignature()
     */
    public String getSignature() {
        return strategy.getSignature();
    }

    private String formatDate(Date date) {
      if (date == null) return "-";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Strategy#isValid()
     */
    public boolean isValid() throws WdkModelException {
        return strategy.isValid();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Strategy#getEstimateSize()
     */
    public int getEstimateSize() {
        return strategy.getEstimateSize();
    }

    public RecordClassBean getRecordClass() throws WdkModelException {
        return new RecordClassBean(strategy.getRecordClass());
    }    
}
