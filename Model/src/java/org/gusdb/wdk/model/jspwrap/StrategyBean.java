package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;

import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class StrategyBean {
    Strategy strategy;
  
    public StrategyBean(Strategy strategy) {
	this.strategy = strategy;
    }
        
    public UserBean getUser() {
        return new UserBean(strategy.getUser());
    }

    public String getName() {
	return strategy.getName();
    }

    public void setName(String name) {
	strategy.setName(name);
    }

    public StepBean getLatestStep() {
	return new StepBean(strategy.getLatestStep());
    }
    
    public int getStrategyId() {
	return strategy.getStrategyId();
    }

    public int getInternalId() {
	return strategy.getInternalId();
    }

    public StepBean getStep(int index) {
	StepBean latestStep = new StepBean(strategy.getLatestStep());
	return latestStep.getStep(index);
    }

    public StepBean[] getAllSteps() {
	StepBean latestStep = new StepBean(strategy.getLatestStep());
	return latestStep.getAllSteps();
    }

    public void addStep(StepBean step)
	throws WdkUserException {
	strategy.addStep(step.step);
    }

    public void setLatestStep(StepBean step) 
	throws WdkUserException {
	strategy.setLatestStep(step.step);
    }

    public int getLength() {
	return getAllSteps().length;
    }

    public void update(boolean overwrite)
	throws WdkUserException, WdkModelException {
	strategy.update(overwrite);
    }
}
