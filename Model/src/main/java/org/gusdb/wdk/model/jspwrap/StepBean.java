package org.gusdb.wdk.model.jspwrap;

import static org.gusdb.fgputil.FormatUtil.urlEncodeUtf8;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.FilterOptionList;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.json.JSONException;
import org.json.JSONObject;

public class StepBean {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(StepBean.class);

  private UserBean user;
  protected Step step;

  public StepBean(UserBean user, Step step) {
    this.user = user;
    this.step = step;
  }

  public Step getStep() {
    return step;
  }

  public StepBean getPreviousStep() {
    if (step.getPrimaryInputStep() != null) {
      return new StepBean(user, step.getPrimaryInputStep());
    }
    return null;
  }

  public StepBean getNextStep() {
    Step nextStep = step.getNextStep();
    return (nextStep == null) ? null : new StepBean(user, nextStep);
  }

  public StepBean getParentStep() {
    Step parent = step.getParentStep();
    return (parent == null) ? null : new StepBean(user, parent);
  }

  public StepBean getParentOrNextStep() {
    Step nextStep = step.getParentOrNextStep();
    return (nextStep == null) ? null : new StepBean(user, nextStep);
  }

  public StepBean getChildStep() {
    if (step.getSecondaryInputStep() != null) {
      return new StepBean(user, step.getSecondaryInputStep());
    }
    return null;
  }

  public void setParentStep(StepBean parentStep) throws WdkModelException {
    if (parentStep != null) {
      step.setParentStep(parentStep.step);
    }
    else {
      step.setParentStep(null);
    }
  }

  public void setChildStep(StepBean childStep) throws WdkModelException {
    if (childStep != null) {
      step.setChildStep(childStep.step);
    }
    else {
      step.setChildStep(null);
    }
  }

  public String getBaseCustomName() {
    return step.getBaseCustomName();
  }

  public String getCustomName() {
    return step.getCustomName();
  }

  public void setCustomName(String customName) {
    step.setCustomName(customName);
  }

  public RecordClassBean getRecordClass() {
    return new RecordClassBean(step.getRecordClass());
  }

  public String getShortDisplayName() {
    return step.getShortDisplayName();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.Step#getDisplayName()
   */
  public String getDisplayName() {
    return step.getDisplayName();
  }

  public int getResultSize() throws WdkModelException {
    return step.getResultSize();
  }

  public String getOperation() {
    return step.getOperation();
  }

  public boolean getIsFirstStep() {
    return step.isFirstStep();
  }

  public AnswerValueBean getAnswerValue() throws WdkModelException {
    return new AnswerValueBean(step.getAnswerValue());
  }

  public AnswerValueBean getAnswerValue(boolean validate) throws WdkModelException {
    return new AnswerValueBean(step.getAnswerValue(validate));
  }

  public AnswerValueBean getViewAnswerValue() throws WdkModelException {
    return new AnswerValueBean(step.getViewAnswerValue());
  }

  public long getStepId() {
    return step.getStepId();
  }

  public int getEstimateSize() {
    return step.getEstimatedSize();
  }

  public String getLastRunTimeFormatted() {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(step.getLastRunTime());
  }

  public Date getLastRunTime() {
    return step.getLastRunTime();
  }

  public void setLastRunTime(Date lastRunTime) {
    step.setLastRunTime(lastRunTime);
  }

  public String getCreatedTimeFormatted() {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(step.getCreatedTime());
  }

  public Date getCreatedTime() {
    return step.getCreatedTime();
  }

  public boolean getIsBoolean() {
    return step.isBoolean();
  }

  public boolean getIsCombined() {
    return step.isCombined();
  }

  public boolean getIsTransform() {
    return step.isTransform();
  }

  public String getQueryChecksum() throws WdkModelException {
    return step.getAnswerValue().getIdsQueryInstance().getQuery().getChecksum(true);
  }

  public String getChecksum() throws WdkModelException {
    return step.getAnswerValue().getChecksum();
  }

  public void update(boolean updateTime) throws WdkModelException {
    step.writeMetadataToDb(updateTime);
  }

  public String getDescription() {
    return step.getDescription();
  }

  /**
   * @return Returns the isDeleted.
   */
  public boolean getIsDeleted() {
    return step.isDeleted();
  }

  /**
   * @param isDeleted
   *          The isDeleted to set.
   */
  public void setIsDeleted(boolean isDeleted) {
    step.setDeleted(isDeleted);
  }

  public boolean getIsCollapsible() {
    return step.isCollapsible();
  }

  public void setIsCollapsible(boolean isCollapsible) {
    step.setCollapsible(isCollapsible);
  }

  public String getCollapsedName() {
    return step.getCollapsedName();
  }

  public void setCollapsedName(String collapsedName) {
    step.setCollapsedName(collapsedName);
  }

  /**
   * @return the isValid
   */
  public boolean getIsValid() {
    return step.isValid();
  }

  public Map<String, String> getParams() {
    return step.getAnswerSpec().getQueryInstanceSpec().toMap();
  }

  public Map<String, String> getParamNames() {
    return step.getParamNames();
  }

  public String getQuestionName() {
    return step.getAnswerSpec().getQuestionName();
  }

  /* functions for navigating/manipulating step tree */
  public StepBean getStep(int index) throws WdkModelException {
    return new StepBean(user, step.getStep(index));
  }

  public StepBean[] getMainBranch() throws WdkModelException {
    List<Step> steps = step.getMainBranch();
    StepBean[] beans = new StepBean[steps.size()];
    for (int i = 0; i < steps.size(); ++i) {
      beans[i] = new StepBean(user, steps.get(i));
    }
    return beans;
  }

  public int getLength() throws WdkModelException {
    return step.getLength();
  }

  public int getIndexFromId(int stepId) throws WdkUserException, WdkModelException {
    return step.getIndexFromId(stepId);
  }

  public boolean isCombined() {
    return step.isCombined();
  }

  public boolean isFiltered() throws WdkModelException {
    return step.isFiltered();
  }

  public String getFilterDisplayName() {
    return step.getFilterDisplayName();
  }

  public StepBean getFirstStep() throws WdkModelException {
    return new StepBean(user, step.getMainBranch().get(0));
  }

  public StepBean deepClone() throws WdkModelException {
    StepFactory factory = step.getStepFactory();
    return new StepBean(user, factory.copyStrategyToBranch(user.getUser(), getStep().getStrategy()));
  }

  public StepFactory getStepFactory() {
    return step.getStepFactory();
  }

  public QuestionBean getQuestion() throws WdkModelException {
    return new QuestionBean(step.getAnswerSpec().getQuestion());
  }

  public String getFilterName() {
    return step.getAnswerSpec().getLegacyFilterName();
  }

  public String getSummaryUrlParams() {
    StringBuffer sb = new StringBuffer();
    Map<String, String> paramValues = step.getAnswerSpec().getQueryInstanceSpec().toMap();
    Map<String, Param> params = step.getAnswerSpec().getQuestion().getParamMap();
    for (String paramName : paramValues.keySet()) {
      Object value = paramValues.get(paramName);
      String paramValue = (value == null) ? "" : value.toString();

      // check if it's dataset param, if so remove user signature
      Param param = params.get(paramName);
      if (param instanceof DatasetParam) {
        int pos = paramValue.indexOf(":");
        if (pos >= 0)
          paramValue = paramValue.substring(pos + 1).trim();
      }

      paramName = urlEncodeUtf8("value(" + paramName + ")");
      paramValue = urlEncodeUtf8(paramValue);
      sb.append("&" + paramName + "=" + paramValue);
    }
    return sb.toString();
  }

  public String getQuestionUrlParams() {
    Question question = step.getAnswerSpec().getQuestion();
    if (question == null)
      return "";
    StringBuffer sb = new StringBuffer();
    Map<String, String> paramValues = step.getAnswerSpec().getQueryInstanceSpec().toMap();
    Map<String, Param> params = question.getParamMap();
    for (String paramName : paramValues.keySet()) {
      String paramValue = paramValues.get(paramName).toString();

      // check if the parameter is multipick param
      Param param = params.get(paramName);

      // check if it's dataset param, if so remove user signature
      if (param instanceof DatasetParam) {
        int pos = paramValue.indexOf(":");
        if (pos >= 0)
          paramValue = paramValue.substring(pos + 1).trim();
      }
      String[] values = { paramValue };
      if (param instanceof FlatVocabParam) {
        FlatVocabParam fvParam = (FlatVocabParam) param;
        if (fvParam.getMultiPick())
          values = paramValue.split(",");
      }
      String wrapper = (param instanceof AbstractEnumParam) ? "array" : "value";
      // URL encode the values
      for (String value : values) {
        String pName = urlEncodeUtf8(wrapper + "(" + paramName + ")");
        sb.append("&" + pName + "=" + urlEncodeUtf8(value.trim()));
      }
    }
    return sb.toString();
  }

  /**
   * 
   * @see org.gusdb.wdk.model.user.Step#resetAnswerValue()
   */
  public void resetAnswerValue() {
    step.resetAnswerValue();
  }

  public UserBean getUser() {
    return user;
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.Step#getAssignedWeight()
   */
  public int getAssignedWeight() {
    return step.getAnswerSpec().getQueryInstanceSpec().getAssignedWeight();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.Step#isRevisable()
   */
  public boolean isRevisable() {
    return step.isMutable();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.Step#getChildrenCount()
   */
  public int getAnswerParamCount() {
    return step.getAnswerSpec().getQuestion().getQuery().getAnswerParamCount();
  }

  /**
   * @return
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.user.Step#getSecondaryInputStepParamName()
   */
  public String getChildStepParam() {
    return step.getSecondaryInputStepParamName().orElse(null);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.Step#getPrimaryInputStepParamName()
   */
  public String getPreviousStepParam() {
    return step.getPrimaryInputStepParamName().orElse(null);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.Step#getFrontId()
   */
  public int getFrontId() throws WdkUserException, WdkModelException, SQLException, JSONException {
    return step.getFrontId();
  }

  @Override
  public String toString() {
    return step.toString();
  }

  public boolean isUncollapsible() {
    return step.isUncollapsible();
  }

  public Exception getException() {
    return step.getException();
  }

  public Map<Long, StepAnalysisContext> getAppliedAnalyses() throws WdkModelException {
    return user.getUser().getWdkModel().getStepAnalysisFactory().getAppliedAnalyses(step);
  }

  public boolean getHasCompleteAnalyses() throws WdkModelException {
    return step.getHasCompleteAnalyses();
  }

  public void saveParamFilters() throws WdkModelException {
    step.writeParamFiltersToDb();
  }

  public FilterOptionList getFilterOptions() {
    return step.getAnswerSpec().getFilterOptions();
  }

  public Long getStrategyId() {
    return step.getStrategyId();
  }

  public void setAnswerValuePaging(int start, int end) {
    step.setAnswerValuePaging(start, end);
  }

}
