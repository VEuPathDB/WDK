package org.gusdb.wdk.model.jspwrap;

import java.util.List;
import java.util.Map;

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
	if (isFirstStep()) {
	    throw new RuntimeException("getOperation cannot be called on the first StepBean.");
	}
	return filterHistory.getAnswer().getBooleanOperation();
    }
    
    public String getDetails() {
	if (isFirstStep()) {
	    return filterHistory.getDescription();
	}
	return subQueryHistory.getDescription();
    }

    protected void setNextStep(StepBean newNextStep) {
        this.nextStep = newNextStep;
    }

    protected void setPreviousStep(StepBean previousStep) {
        this.previousStep = previousStep;
    }

    public boolean isFirstStep() {
	return (previousStep == null);
    }
}
