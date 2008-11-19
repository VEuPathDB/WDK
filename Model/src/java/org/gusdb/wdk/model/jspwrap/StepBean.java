package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class StepBean {
    Step step;

    public StepBean(Step step) {
        this.step = step;
    }

    public StepBean getPreviousStep() {
        if (step.getPreviousStep() != null) {
            return new StepBean(step.getPreviousStep());
        }
        return null;
    }

    public StepBean getNextStep() {
        if (step.getNextStep() != null) {
            return new StepBean(step.getNextStep());
        }
        return null;
    }

    public void setNextStep(StepBean next) throws WdkUserException {
        if (next != null) step.setNextStep(next.step);
        else {
            Step nextStep = null;
            step.setNextStep(nextStep);
        }
    }

    public StepBean getParentStep() {
        if (step.getParentStep() != null) {
            return new StepBean(step.getParentStep());
        }
        return null;
    }

    public StepBean getChildStep() {
        if (step.getChildStep() != null) {
            return new StepBean(step.getChildStep());
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

    public String getDataType() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getType();
    }

    public String getShortDisplayName() throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, JSONException,
            SQLException {
        return step.getShortDisplayName();
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
        return step.getIsFirstStep();
    }

    public AnswerValueBean getAnswerValue() throws WdkUserException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            SQLException {
        return new AnswerValueBean(step.getAnswer().getAnswerValue());
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
        step.setAnswer(answer.answerValue.getAnswer());
    }

    public int getEstimateSize() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return step.getEstimateSize();
    }

    public Date getLastRunTime() {
        return step.getLastRunTime();
    }

    public void setLastRunTime(Date lastRunTime) {
        step.setLastRunTime(lastRunTime);
    }

    public Date getCreatedTime() {
        return step.getLastRunTime();
    }

    public void setCreatedTime(Date lastRunTime) {
        step.setLastRunTime(lastRunTime);
    }

    public boolean getIsBoolean() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getAnswer().getAnswerValue().isBoolean();
    }

    public boolean getIsTransform() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getAnswer().getAnswerValue().isTransform();
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

    public void update() throws WdkUserException, NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException {
        step.update();
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

    /**
     * @param isValid
     *            the isValid to set
     */
    public void setIsValid(boolean isValid) {
        step.setValid(isValid);
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return step.getVersion();
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        step.setVersion(version);
    }

    public void setParams(Map<String, String> params) {
        step.setDisplayParams(params);
    }

    public Map<String, String> getParams() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getDisplayParams();
    }

    public Map<String, String> getParamNames() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        return step.getParamNames();
    }

    public String getQuestionName() {
        return step.getQuestionName();
    }

    /* functions for navigating/manipulating step tree */
    public StepBean getStep(int index) {
        return new StepBean(step.getStep(index));
    }

    public StepBean[] getAllSteps() {
        Step[] steps = step.getAllSteps();
        StepBean[] beans = new StepBean[steps.length];
        for (int i = 0; i < steps.length; ++i) {
            beans[i] = new StepBean(steps[i]);
        }
        return beans;
    }

    public StepBean getStepByDisplayId(int stepId) {
        Step target = step.getStepByDisplayId(stepId);
        if (target != null) {
            return new StepBean(target);
        }
        return null;
    }

    public int getLength() {
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

}
