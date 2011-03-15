package org.gusdb.wdk.model.jspwrap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FlatVocabParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONException;

public class StepBean {

    private UserBean user;
    protected Step step;

    public StepBean(UserBean user, Step step) {
        this.user = user;
        this.step = step;
    }

    public StepBean getPreviousStep() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        if (step.getPreviousStep() != null) {
            return new StepBean(user, step.getPreviousStep());
        }
        return null;
    }

    public StepBean getNextStep() {
        Step nextStep = step.getNextStep();
        return (nextStep == null) ? null : new StepBean(user, nextStep);
    }

    public void setNextStep(StepBean next) throws WdkUserException {
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

    public StepBean getChildStep() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        if (step.getChildStep() != null) {
            return new StepBean(user, step.getChildStep());
        }
        return null;
    }

    public void setParentStep(StepBean parentStep) {
        if (parentStep != null) {
            step.setParentStep(parentStep.step);
        } else {
            step.setParentStep(null);
        }
    }

    public void setChildStep(StepBean childStep) {
        if (childStep != null) {
            step.setChildStep(childStep.step);
        } else {
            step.setChildStep(null);
        }
    }

    public String getBaseCustomName() throws WdkUserException {
        return step.getBaseCustomName();
    }

    public String getCustomName() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        return step.getCustomName();
    }

    public void setCustomName(String customName) {
        step.setCustomName(customName);
    }

    public String getDataType() {
        return step.getType();
    }

    public String getShortDisplayName() throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, JSONException,
            SQLException {
        return step.getShortDisplayName();
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws JSONException
     * @throws WdkUserException
     * @throws SQLException
     * @see org.gusdb.wdk.model.user.Step#getDisplayName()
     */
    public String getDisplayName() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getDisplayName();
    }

    public int getResultSize() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        return step.getResultSize();
    }

    public String getOperation() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        return step.getOperation();
    }

    public boolean getIsFirstStep() {
        return step.isFirstStep();
    }

    public AnswerValueBean getAnswerValue() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        return new AnswerValueBean(step.getAnswerValue());
    }

    public int getAnswerId() {
        return step.getAnswerId();
    }

    public int getStepId() {
        return step.getDisplayId();
    }

    public void setAnswerValue(AnswerValueBean answer)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        step.setAnswer(answer.getAnswerValue().getAnswer());
    }

    public int getEstimateSize() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
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

    public boolean getIsBoolean() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.isBoolean();
    }

    public boolean getIsTransform() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.isTransform();
    }

    public String getBooleanExpression() {
        return step.getBooleanExpression();
    }

    public void setBooleanExpression(String booleanExpression) {
        step.setBooleanExpression(booleanExpression);
    }

    public String getQueryChecksum() throws WdkModelException {
        return step.getAnswer().getQueryChecksum();
    }

    public String getChecksum() throws WdkModelException {
        return step.getAnswer().getAnswerChecksum();
    }

    public void update(boolean updateTime) throws WdkUserException,
            NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException {
        step.update(updateTime);
    }

    public String getDescription() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
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

    public boolean getIsCollapsible() throws WdkModelException {
        return step.isCollapsible();
    }

    public void setIsCollapsible(boolean isCollapsible) {
        step.setCollapsible(isCollapsible);
    }

    public String getCollapsedName() throws WdkModelException {
        return step.getCollapsedName();
    }

    public void setCollapsedName(String collapsedName) {
        step.setCollapsedName(collapsedName);
    }

    /**
     * @return the isValid
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public boolean getIsValid() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        return step.isValid();
    }

    /**
     * @param isValid
     *            the isValid to set
     */
    public void setIsValid(boolean isValid) {
        step.setValid(isValid);
    }

    public void setParams(Map<String, String> params) throws WdkModelException {
        step.setParamValues(params);
    }

    public Map<String, String> getParams() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getParamValues();
    }

    public Map<String, String> getParamNames() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getParamNames();
    }

    public String getQuestionName() {
        return step.getQuestionName();
    }

    /* functions for navigating/manipulating step tree */
    public StepBean getStep(int index) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        return new StepBean(user, step.getStep(index));
    }

    public StepBean[] getAllSteps() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        Step[] steps = step.getAllSteps();
        StepBean[] beans = new StepBean[steps.length];
        for (int i = 0; i < steps.length; ++i) {
            beans[i] = new StepBean(user, steps[i]);
        }
        return beans;
    }

    public StepBean getStepByDisplayId(int stepId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        Step target = step.getStepByDisplayId(stepId);
        if (target != null) {
            return new StepBean(user, target);
        }
        return null;
    }

    public int getLength() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        return step.getLength();
    }

    public void addStep(StepBean next) throws WdkUserException {
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
            WdkModelException, SQLException, JSONException {
        return step.getIndexFromId(stepId);
    }

    /**
     * @param filterName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws JSONException
     * @throws WdkUserException
     * @throws SQLException
     * @see org.gusdb.wdk.model.user.Step#createStep(org.gusdb.wdk.model.AnswerFilterInstance)
     */
    public StepBean createStep(String filterName, int assignedWeight)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        return new StepBean(user, step.createStep(filterName, assignedWeight));
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws JSONException
     * @throws WdkUserException
     * @throws SQLException
     * @see org.gusdb.wdk.model.user.Step#isCombined()
     */
    public boolean isCombined() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.isCombined();
    }

    public boolean isUseBooleanFilter() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.isUseBooleanFilter();
    }

    public boolean isFiltered() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.isFiltered();
    }

    public String getFilterDisplayName() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getFilterDisplayName();
    }

    public StepBean getFirstStep() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        return new StepBean(user, step.getFirstStep());
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws JSONException
     * @throws WdkUserException
     * @throws SQLException
     * @see org.gusdb.wdk.model.user.Step#deepClone()
     */
    public StepBean deepClone() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return new StepBean(user, step.deepClone());
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
        StringBuffer sb = new StringBuffer();
        Map<String, String> paramValues = step.getParamValues();
        Map<String, Param> params = step.getQuestion().getParamMap();
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
                if (fvParam.getMultiPick()) values = paramValue.split(",");
            }
            // URL encode the values
            for (String value : values) {
                try {
                    sb.append("&" + paramName + "="
                            + URLEncoder.encode(value.trim(), "UTF-8"));
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

    public String getType() {
        return step.getType();
    }

    public String getDisplayType() {
        return step.getDisplayType();
    }

    public String getShortDisplayType() {
        return step.getShortDisplayType();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.user.Step#getValidationMessage()
     */
    public String getValidationMessage() {
        return step.getValidationMessage();
    }

    /**
     * @return
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @see org.gusdb.wdk.model.user.Step#validate()
     */
    public boolean validate() throws SQLException, WdkUserException,
            WdkModelException, JSONException {
        return step.validate();
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
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.Step#getChildrenCount()
     */
    public int getAnswerParamCount() throws WdkUserException, WdkModelException {
        return step.getAnswerParamCount();
    }

    /**
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.Step#getChildStepParam()
     */
    public String getChildStepParam() throws WdkModelException {
        return step.getChildStepParam();
    }

    /**
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.Step#getPreviousStepParam()
     */
    public String getPreviousStepParam() throws WdkModelException {
        return step.getPreviousStepParam();
    }

    /**
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.user.Step#getFrontId()
     */
    public int getFrontId() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        return step.getFrontId();
    }

    public String toString() {
        return step.toString();
    }
}
