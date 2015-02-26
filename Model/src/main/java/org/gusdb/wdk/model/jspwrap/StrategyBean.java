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

  public boolean getIsPublic() {
    return strategy.getIsPublic();
  }

  public void setIsPublic(boolean isPublic) {
    strategy.setIsPublic(isPublic);
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
    return latestStep.getMainBranch();
  }

  // public void addStep(StepBean step) throws WdkUserException,
  // WdkModelException, SQLException, JSONException {
  // strategy.addStep(step.step);
  // }

  public void setLatestStep(StepBean step) throws WdkModelException {
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

  public void update(boolean overwrite) throws WdkUserException, WdkModelException, SQLException {
    strategy.update(overwrite);
  }

  public Map<Integer, Integer> insertStepAfter(StepBean newStep, int targetId) throws WdkModelException, WdkUserException {
    return strategy.insertStepAfter(newStep.getStep(), targetId);
  }

  public  Map<Integer, Integer> insertStepBefore(StepBean newStep, int targetId) throws WdkModelException,
      WdkUserException {
    return strategy.insertStepBefore(newStep.getStep(), targetId);
  }

  public Map<Integer, Integer> deleteStep(StepBean step) throws WdkModelException, WdkUserException {
    return strategy.deleteStep(step.getStep());
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
    if (date == null)
      return "-";
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
