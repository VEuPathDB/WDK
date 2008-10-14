package org.gusdb.wdk.model.jspwrap;

import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

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

    public void setNextStep(StepBean next)
	throws WdkUserException {
	if (next != null)
	    step.setNextStep(next.step);
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
	}
	else {
	    step.setParentStep(null);
	}
    }

    public void setChildStep(StepBean childStep) {
	if (childStep != null) {
	    step.setChildStep(childStep.step);
	}
	else {
	    step.setChildStep(null);
	}
    }

    public String getCustomName() 
	throws WdkUserException {
	return step.getCustomName();
    }

    public void setCustomName(String customName) {
	step.setCustomName(customName);
    }

    public String getDataType() {
	return step.getDataType();
    }

    public String getShortDisplayName() 
	throws WdkModelException, WdkUserException {
	return step.getShortDisplayName();
    }

    public int getResultSize()
	throws WdkModelException, WdkUserException {
	return step.getResultSize();
    }

    public String getOperation() throws WdkUserException {
	return step.getOperation();
    }

    public boolean getIsFirstStep() {
	return step.getIsFirstStep();
    }

    public AnswerValueBean getAnswerValue()
	throws WdkUserException {
	return new AnswerValueBean(step.getAnswerValue());
    }

    public int getStepId() {
	return step.getStepId();
    }

    public void setAnswerValue( AnswerValueBean answer ) {
        step.setAnswerValue(answer.answer);
    }

    public int getEstimateSize() {
        return step.getEstimateSize();
    }

    public void setEstimateSize( int estimateSize ) {
        step.setEstimateSize(estimateSize);
    }

    public Date getLastRunTime() {
	return step.getLastRunTime();
    }

    public void setLastRunTime( Date lastRunTime ) {
        step.setLastRunTime(lastRunTime);
    }

    public Date getCreatedTime() {
	return step.getLastRunTime();
    }

    public void setCreatedTime( Date lastRunTime ) {
        step.setLastRunTime(lastRunTime);
    }

    public boolean getIsBoolean() {
        return step.isBoolean();
    }

    public boolean isTransform() {
	return step.isTransform();
    }

    public void setIsBoolean( boolean isBoolean ) {
        step.setBoolean(isBoolean);
    }
    public String getBooleanExpression() {
        return step.getBooleanExpression();
    }
    
    public void setBooleanExpression( String booleanExpression ) {
        step.setBooleanExpression(booleanExpression);
    }
    
    public String getSignature() throws WdkModelException {
        return step.getSignature();
    }
    
    public String getChecksum() throws WdkModelException {
        return step.getChecksum();
    }

    public void update()
	throws WdkUserException {
        step.update();
    }

    public void update(boolean updateTime)
	throws WdkUserException {
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
    public void setIsDeleted( boolean isDeleted ) {
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
    
    public String getCacheFullTable() throws WdkModelException {
        return step.getCacheFullTable();
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
    public void setIsValid( boolean isValid ) {
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
    public void setVersion( String version ) {
       step.setVersion(version);
    }
    
    public void setParams( Map< String, Object > params ) {
        step.setParams(params);
    }
    
    public Map< String, Object > getParams() {
        return step.getParams();
    }
    
    public Map< String, String > getParamNames() {
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

    public StepBean getStepById(int stepId) {
	Step target = step.getStepById(stepId);
	if (target != null) {
	    return new StepBean(target);
	}
	return null;
    }

    public int getLength() {
	return step.getLength();
    }

    public void addStep(StepBean next) 
	throws WdkUserException {
	step.addStep(next.step);
    }
}
