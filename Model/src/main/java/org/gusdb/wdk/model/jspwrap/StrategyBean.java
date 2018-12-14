package org.gusdb.wdk.model.jspwrap;

import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gusdb.fgputil.FormatUtil;
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

  public String getSavedName() {
    return strategy.getSavedName();
  }

  public boolean getIsSaved() {
    return strategy.isSaved();
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
    return strategy.isPublic();
  }

  public StepBean getLatestStep() {
    return new StepBean(user, strategy.getRootStep());
  }

  public long getLatestStepId() {
    return strategy.getRootStepId();
  }

  public long getStrategyId() {
    return strategy.getStrategyId();
  }

  public StepBean[] getAllSteps() throws WdkModelException {
    StepBean latestStep = new StepBean(user, strategy.getRootStep());
    return latestStep.getMainBranch();
  }

  public StepBean getStepById(long stepId) throws WdkModelException {
    Step target = strategy.findFirstStep(withId(stepId)).orElseThrow(
        () -> new WdkModelException("Step " + stepId + " is not in strategy " + strategy.getStrategyId()));
    if (target != null) {
      return new StepBean(user, target);
    }
    return null;
  }

  public int getLength() throws WdkModelException {
    return getAllSteps().length;
  }

  public void update(boolean overwrite) throws WdkUserException, WdkModelException {
    strategy.update(overwrite);
  }

  public String getImportId() {
    return strategy.getSignature();
  }

  public StepBean getFirstStep() {
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
   * @see org.gusdb.wdk.model.user.Strategy#getSignature()
   */
  public String getSignature() {
    return strategy.getSignature();
  }

  /**
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.user.Strategy#getEstimatedSize()
   */
  public int getEstimateSize() throws WdkModelException {
    return strategy.getResultSize();
  }

  public String getEstimateSizeNoCalculate() {
    return strategy.getEstimatedSize();
  }

  private String formatDate(Date date) {
    if (date == null)
      return "-";
    DateFormat formatter = new SimpleDateFormat(FormatUtil.STANDARD_DATETIME_FORMAT_DASH);
    return formatter.format(date);
  }

  public boolean isValid() {
    return strategy.isValid();
  }

  public RecordClassBean getRecordClass() {
    return new RecordClassBean(strategy.getRecordClass());
  }

  public Strategy getStrategy() {
    return strategy;
  }
}
