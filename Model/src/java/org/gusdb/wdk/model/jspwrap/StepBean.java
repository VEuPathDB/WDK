package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class StepBean {
 
    public static final String INTERNAL_AND = "INTERSECT";
    public static final String INTERNAL_OR = "UNION";
    public static final String INTERNAL_NOT = "MINUS";
    
    
    StepBean nextStep;
    StepBean previousStep;
    
    HistoryBean filterHistory;
    HistoryBean subQueryHistory;

    public StepBean() {
    }

    public StepBean(HistoryBean filterHistory, HistoryBean subQueryHistory,
		    StepBean previousStep, StepBean nextStep) {
	this.previousStep = previousStep;
	this.nextStep = nextStep;
	this.filterHistory = filterHistory;
	this.subQueryHistory = subQueryHistory;
    }

    public StepBean getPreviousStep() {

        return previousStep;
    }

    public StepBean getNextStep() {
	return nextStep;
    }

    public HistoryBean getFilterHistory() {
	return filterHistory;
    }

    public HistoryBean getSubQueryHistory() {
	return subQueryHistory;
    }

    public String getCustomName() {
	if (getIsFirstStep()) {
	    return filterHistory.getCustomName();
	}
	return subQueryHistory.getCustomName();
    }

    public int getFilterResultSize()
	throws WdkModelException, WdkUserException {
	return filterHistory.getAnswer().getResultSize();
    }

    public int getSubQueryResultSize()
	throws WdkModelException, WdkUserException {
	return subQueryHistory.getAnswer().getResultSize();
    }

    public String getOperation() throws WdkUserException {
	if (getIsFirstStep()) {
	    throw new RuntimeException("getOperation cannot be called on the first StepBean.");
	}
	return filterHistory.getAnswer().getBooleanOperation();
    }
    
    public String getDetails() {
	if (getIsFirstStep()) {
	    return filterHistory.getDescription().replace(",", "\n").replace(":", ":\n");
	}
	return subQueryHistory.getDescription().replace(",", "\n").replace(":", ":\n");
    }

    // Not sure if these mutators are safe, is there a better way to do this?
    public void setFilterHistory(HistoryBean history) {
	this.filterHistory = history;
    }
    
    public void setSubQueryHistory(HistoryBean history) {
	this.subQueryHistory = history;
    }

    protected void setNextStep(StepBean newNextStep) {
        this.nextStep = newNextStep;
    }

    protected void setPreviousStep(StepBean previousStep) {
        this.previousStep = previousStep;
    }

    public boolean getIsFirstStep() {
	return (previousStep == null);
    }
}
