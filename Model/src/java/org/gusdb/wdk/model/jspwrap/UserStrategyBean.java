package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;

import org.gusdb.wdk.model.user.UserStrategy;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class UserStrategyBean {
    UserStrategy strategy;
  
    public UserStrategyBean(UserStrategy strategy) {
	this.strategy = strategy;
    }
    
    public String getName() {
	return strategy.getName();
    }

    public StepBean getLatestStep() {
	return new StepBean(strategy.getLatestStep());
    }
    
    public int getStrategyId() {
	return strategy.getStrategyId();
    }

    public StepBean getStep(int index) {
	return new StepBean(strategy.getStep(index));
    }

    public StepBean[] getAllSteps() {
	ArrayList<StepBean> allSteps = new ArrayList<StepBean>();
	allSteps = buildAllStepsArray(allSteps, getLatestStep());
	return allSteps.toArray(new StepBean[allSteps.size()]);
    }

    public void addStep(StepBean step) {
	strategy.addStep(step.step);
    }

    public void setLatestStep(StepBean step) {
	strategy.setLatestStep(step.step);
    }

    public int getLength() {
	return getAllSteps().length;
    }

    public void update() throws WdkUserException {
	strategy.update();
    }

    private ArrayList<StepBean> buildAllStepsArray(ArrayList<StepBean> array, StepBean step) {
	if (step.getIsFirstStep()) {
	    array.add(step);
	}
	else {
	    array = buildAllStepsArray(array, step.getPreviousStep());
	    array.add(step);
	}
	return array;
    }
}
