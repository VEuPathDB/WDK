package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class StepBean {
    Step step;

    public StepBean(UserAnswerBean userAnswer) {
	this.step = new Step(userAnswer.userAnswer);
    }

    public StepBean(Step step) {
	this.step = step;
    }

    public void setStrategy(UserStrategyBean strategy)
	throws WdkUserException {
	step.setStrategy(strategy.strategy);
    }
	
    public UserStrategyBean getStrategy() {
	return new UserStrategyBean(step.getStrategy());
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

    public UserAnswerBean getChildStepUserAnswer() {
	if (step.getChildStep() != null) {
	    return new UserAnswerBean(step.getChildStepUserAnswer());
	}
	return null;
    }

    public StepBean getChildStep() {
	if (step.getChildStep() != null) {
	    return new StepBean(step.getChildStep());
	}
	return null;
    }

    public UserAnswerBean getFilterUserAnswer() {
	if (step.getFilterUserAnswer() != null) {
	    return new UserAnswerBean(step.getFilterUserAnswer());
	}
	return null;
    }

    public void setFilterUserAnswer(UserAnswerBean answer) {
	step.setFilterUserAnswer(answer.userAnswer);
    }

    public void setChildStep(StepBean childStep) {
	step.setChildStep(childStep.step);
    }

    public String getCustomName() 
	throws WdkUserException {
	return step.getCustomName();
    }

    public String getDataType() {
	return step.getDataType();
    }

    public String getShortName() 
	throws WdkModelException, WdkUserException {
	return step.getShortName();
    }

    public int getFilterResultSize()
	throws WdkModelException, WdkUserException {
	return step.getFilterResultSize();
    }

    public int getSubQueryResultSize()
	throws WdkModelException, WdkUserException {
	return step.getSubQueryResultSize();
    }

    public String getOperation() throws WdkUserException {
	return step.getOperation();
    }

    public boolean getIsFirstStep() {
	return step.getIsFirstStep();
    }
}
