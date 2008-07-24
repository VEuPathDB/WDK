package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class Step {
 
    public static final String INTERNAL_AND = "INTERSECT";
    public static final String INTERNAL_OR = "UNION";
    public static final String INTERNAL_NOT = "MINUS";
    
    Step nextStep = null;
    Step previousStep = null;
    
    UserAnswer filterUserAnswer;
    Step childStep = null;

    /*public Step() {
      }*/

    public Step(UserAnswer filterUserAnswer) {
	this.filterUserAnswer = filterUserAnswer;
    }

    public Step(UserAnswer filterUserAnswer, Step childStep,
		    Step previousStep, Step nextStep) {
	this.previousStep = previousStep;
	this.nextStep = nextStep;
	this.filterUserAnswer = filterUserAnswer;
	this.childStep = childStep;
    }

    public Step getPreviousStep() {
        return previousStep;
    }

    public Step getNextStep() {
	return nextStep;
    }

    public UserAnswer getFilterUserAnswer() {
	return filterUserAnswer;
    }

    public Step getChildStep() {
	return childStep;
    }

    public UserAnswer getChildStepUserAnswer() {
	return childStep.getFilterUserAnswer();
    }

    public String getCustomName() {
	if (getIsFirstStep()) {
	    return filterUserAnswer.getCustomName();
	}
	return childStep.getFilterUserAnswer().getCustomName();
    }

    public String getShortName() 
	throws WdkModelException, WdkUserException {
	if (getIsFirstStep()) {
	    return filterUserAnswer.getRecordPage().getQuestion().getShortDisplayName();
	}
	else {
	    return childStep.getFilterUserAnswer().getRecordPage().getQuestion().getShortDisplayName();
	}
    }

    public String getDataType() {
	return filterUserAnswer.getDataType();
    }

    public int getFilterResultSize()
	throws WdkModelException, WdkUserException {
	return filterUserAnswer.getRecordPage().getResultSize();
    }

    // Need to check whether subquery is a Step or a UserAnswer
    public int getSubQueryResultSize()
	throws WdkModelException, WdkUserException {
	return ((Step)childStep).getFilterResultSize();
    }

    // Needs to be updated for transforms
    public String getOperation() throws WdkUserException {
	if (getIsFirstStep()) {
	    throw new RuntimeException("getOperation cannot be called on the first Step.");
	}
	return filterUserAnswer.getRecordPage().getBooleanOperation();
    }
    
    public void setFilterUserAnswer(UserAnswer userAnswer) {
	this.filterUserAnswer = userAnswer;
    }
    
    public void setChildStep(Step childStep) {
	this.childStep = childStep;
    }

    protected void setNextStep(Step newNextStep) {
        this.nextStep = newNextStep;
    }

    protected void setPreviousStep(Step previousStep) {
        this.previousStep = previousStep;
    }

    public boolean getIsFirstStep() {
	return (previousStep == null);
    }
}
