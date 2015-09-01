package org.gusdb.wdk.model.jspwrap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
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

    public StepBean getPreviousStep() throws WdkModelException {
        if (step.getPreviousStep() != null) {
            return new StepBean(user, step.getPreviousStep());
        }
        return null;
    }
    
    public StepBean getNextStep() {
        Step nextStep = step.getNextStep();
        return (nextStep == null) ? null : new StepBean(user, nextStep);
    }

    public void setNextStep(StepBean next) throws WdkModelException {
        if (next != null) step.setNextStep(next.step);
        else {
            Step nextStep = null;
            step.setNextStep(nextStep);
        }
    }

    public StepBean getParentStep() {
        Step parent = step.getParentStep();
        return (parent == null) ? null : new StepBean(user, parent);
    }

    public StepBean getParentOrNextStep() {
        Step nextStep = step.getParentOrNextStep();
        return (nextStep == null) ? null : new StepBean(user, nextStep);
    }

    public StepBean getChildStep() throws WdkModelException {
        if (step.getChildStep() != null) {
            return new StepBean(user, step.getChildStep());
        }
        return null;
    }

    public void setParentStep(StepBean parentStep) throws WdkModelException {
        if (parentStep != null) {
            step.setParentStep(parentStep.step);
        } else {
            step.setParentStep(null);
        }
    }

    public void setChildStep(StepBean childStep) throws WdkModelException {
        if (childStep != null) {
            step.setChildStep(childStep.step);
        } else {
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

    public RecordClassBean getRecordClass() throws WdkModelException {
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

    public String getOperation() throws WdkUserException, WdkModelException {
        return step.getOperation();
    }

    public boolean getIsFirstStep() throws WdkModelException {
        return step.isFirstStep();
    }

    public AnswerValueBean getAnswerValue() throws WdkModelException, WdkUserException {
        return getAnswerValue(true, false);
    }

    public AnswerValueBean getViewAnswerValue() throws WdkModelException, WdkUserException {
        return getAnswerValue(true, true);
    }

    public AnswerValueBean getAnswerValue(boolean validate, boolean applyViewFilters)
            throws WdkModelException, WdkUserException {
        return new AnswerValueBean(step.getAnswerValue(validate, applyViewFilters));
    }

    public int getStepId() {
        return step.getStepId();
    }

    public void setAnswerValue(AnswerValueBean answer) {
        step.setAnswerValue(answer.getAnswerValue());
    }

    public int getEstimateSize() {
        return step.getEstimateSize();
    }

    public String getLastRunTimeFormatted() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(step.getLastRunTime());
    }

    public Date getLastRunTime() {
        return step.getLastRunTime();
    }

    public void setLastRunTime(Date lastRunTime) {
        step.setLastRunTime(lastRunTime);
    }

    public String getCreatedTimeFormatted() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT).format(step.getCreatedTime());
    }

    public Date getCreatedTime() {
        return step.getCreatedTime();
    }

    public boolean getIsBoolean() {
        return step.isBoolean();
    }

    public boolean getIsTransform() {
        return step.isTransform();
    }

    public String getBooleanExpression() {
        return step.getBooleanExpression();
    }

    public void setBooleanExpression(String booleanExpression) {
        step.setBooleanExpression(booleanExpression);
    }

    public String getQueryChecksum() throws WdkModelException, WdkUserException {
        return step.getAnswerValue().getQueryChecksum(true);
    }

    public String getChecksum() throws WdkModelException, WdkUserException {
        return step.getAnswerValue().getChecksum();
    }

    public void update(boolean updateTime) throws WdkModelException {
        step.update(updateTime);
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
     *            The isDeleted to set.
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
    public boolean getIsValid() throws WdkModelException {
        return step.isValid();
    }

    public Map<String, String> getParams() {
        return step.getParamValues();
    }

    public Map<String, String> getParamNames() throws WdkModelException {
        return step.getParamNames();
    }

    public String getQuestionName() {
        return step.getQuestionName();
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

    public StepBean getStepByDisplayId(int stepId) throws WdkModelException {
        Step target = step.getStepByDisplayId(stepId);
        if (target != null) {
            return new StepBean(user, target);
        }
        return null;
    }

    public int getLength() throws WdkModelException {
        return step.getLength();
    }

    public void addStep(StepBean next) throws WdkModelException {
        step.addStep(next.step);
    }

    /**
     * @param estimateSize
     * @see org.gusdb.wdk.model.user.Step#setEstimateSize(int)
     */
    public void setEstimateSize(int estimateSize) {
        step.setEstimateSize(estimateSize);
    }

    public int getIndexFromId(int stepId) throws WdkUserException,
            WdkModelException {
        return step.getIndexFromId(stepId);
    }

    /**
     * @param filterName
     * @return
     * @throws WdkModelException 
     * @throws SQLException 
     * @throws JSONException 
     * @throws WdkUserException 
     * @throws NoSuchAlgorithmException 
     * @see org.gusdb.wdk.model.user.Step#createStep(org.gusdb.wdk.model.AnswerFilterInstance)
     */
    public StepBean createStep(String filterName, int assignedWeight) throws WdkModelException  {
        return new StepBean(user, step.createStep(filterName, assignedWeight));
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#isCombined()
     */
    public boolean isCombined() {
        return step.isCombined();
    }

    public boolean isUseBooleanFilter() throws WdkModelException {
        return step.isUseBooleanFilter();
    }

    public boolean isFiltered() {
        return step.isFiltered();
    }

    public String getFilterDisplayName() {
        return step.getFilterDisplayName();
    }

    public StepBean getFirstStep() throws WdkModelException {
        return new StepBean(user, step.getFirstStep());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#deepClone()
     */
    public StepBean deepClone(Integer strategyId, Map<Integer, Integer> stepIdMap) throws WdkModelException {
        return new StepBean(user, step.deepClone(strategyId, stepIdMap));
    }

    public QuestionBean getQuestion() throws WdkModelException {
        return new QuestionBean(step.getQuestion());
    }

    public String getFilterName() {
        return step.getFilterName();
    }

    public String getSummaryUrlParams() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        Map<String, String> paramValues = step.getParamValues();
        Map<String, Param> params = step.getQuestion().getParamMap();
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

            try {
                paramName = URLEncoder.encode("value(" + paramName + ")",
                        "UTF-8");
                paramValue = URLEncoder.encode(paramValue, "UTF-8");
                sb.append("&" + paramName + "=" + paramValue);
            } catch (UnsupportedEncodingException ex) {
                throw new WdkModelException(ex);
            }
        }
        return sb.toString();
    }

    public String getQuestionUrlParams() throws WdkModelException {
        Question question;
        try {
            question = step.getQuestion();
        } catch (WdkModelException ex) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Map<String, String> paramValues = step.getParamValues();
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
            if (param instanceof FlatVocabParam && !(param instanceof FilterParam)) {
                FlatVocabParam fvParam = (FlatVocabParam) param;
                if (fvParam.getMultiPick()) values = paramValue.split(",");
            }
            String wrapper = (param instanceof AbstractEnumParam && !(param instanceof FilterParam)) ? "array" : "value";
            // URL encode the values
            for (String value : values) {
                
                try {
                    String pName = URLEncoder.encode(wrapper + "(" + paramName + ")", "UTF-8");
                    sb.append("&" + pName + "=" + URLEncoder.encode(value.trim(), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
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
        return step.getAssignedWeight();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#isRevisable()
     */
    public boolean isRevisable() {
        return step.isRevisable();
    }
    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#getChildrenCount()
     */
    public int getAnswerParamCount() {
        return step.getAnswerParamCount();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#getChildStepParamName()
     */
    public String getChildStepParam() throws WdkModelException {
        return step.getChildStepParamName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#getPreviousStepParamName()
     */
    public String getPreviousStepParam() throws WdkModelException {
        return step.getPreviousStepParamName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#getFrontId()
     */
    public int getFrontId() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        return step.getFrontId();
    }

    @Override
    public String toString() {
        return step.toString();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#isUncollapsible()
     */
    public boolean isUncollapsible() {
        return step.isUncollapsible();
    }
    
    public Exception getException() {
        return step.getException();
    }
    
    public Map<Integer, StepAnalysisContext> getAppliedAnalyses() throws WdkModelException {
        return user.getUser().getWdkModel().getStepAnalysisFactory().getAppliedAnalyses(step);
    }

	public void setValid(boolean valid){
		step.setValid(valid);
	}
	
	public boolean getHasCompleteAnalyses() throws WdkModelException {
	  return step.getHasCompleteAnalyses();
	}

  public void setQuestionName(String questionName) {
    step.setQuestionName(questionName);
  }

  public void setParamValues(Map<String, String> paramValues) {
    step.setParamValues(paramValues);
  }

  public void setParamValue(String paramName, String paramValue) {
    step.setParamValue(paramName, paramValue);
  }

  public void setFilterName(String filterName) {
    step.setFilterName(filterName);
  }

  public void saveParamFilters() throws WdkModelException {
    step.saveParamFilters();
  }

  public void setAssignedWeight(int assignedWeight) {
    step.setAssignedWeight(assignedWeight);
  }

  public FilterOptionList getFilterOptions() {
    return step.getFilterOptions();
  }

  public void addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    step.addFilterOption(filterName, filterValue);
  }

  public void removeFilterOption(String filterName) {
    step.removeFilterOption(filterName);
  }

  public Integer getStrategyId() {
    return step.getStrategyId();
  }

}
