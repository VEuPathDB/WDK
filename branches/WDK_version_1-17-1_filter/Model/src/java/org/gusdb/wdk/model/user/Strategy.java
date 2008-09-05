package org.gusdb.wdk.model.user;

import java.util.ArrayList;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class Strategy {
    
    private UserFactory factory;
    private User user;
    private Step latestStep;
    private int strategyId;
    private int internalId;
    private boolean isSaved;
    private String name;

    Strategy( UserFactory factory, User user, int strategyId, int internalId, String name ) {
	this.factory = factory;
	this.user = user;
	this.strategyId = strategyId;
	this.internalId = internalId;
	this.name = name;
	isSaved = false;
    }

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }

    public void setIsSaved(boolean saved) {
	this.isSaved = saved;
    }
    
    public boolean getIsSaved() {
	return isSaved;
    }

    public String getDataType() {
	return latestStep.getDataType();
    }

    public Step getLatestStep() {
	return latestStep;
    }
    
    void setStrategyId(int strategyId) {
	this.strategyId = strategyId;
    }

    public int getStrategyId() {
	return strategyId;
    }

    public int getInternalId() {
	return internalId;
    }

    void setInternalId(int internalId) {
	this.internalId = internalId;
    }

    public Step getStep(int index) {
	Step[] steps = getAllSteps();
	return steps[index];
    }

    public Step[] getAllSteps() {
	ArrayList<Step> allSteps = new ArrayList<Step>();
	allSteps = buildAllStepsArray(allSteps, latestStep);
	return allSteps.toArray(new Step[allSteps.size()]);
    }

    public int getLength() {
	return getAllSteps().length;
    }

    private ArrayList<Step> buildAllStepsArray(ArrayList<Step> array, Step step) {
	if (step.getIsFirstStep()) {
	    array.add(step);
	}
	else {
	    array = buildAllStepsArray(array, step.getPreviousStep());
	    array.add(step);
	}
	return array;
    }

    public void addStep(Step step) 
	throws WdkUserException {
	if (latestStep != null) {
	    step.setPreviousStep(latestStep);
	    latestStep.setNextStep(step);
	}
	setLatestStep(step);
    }
    
    public void setLatestStep(Step step) 
	throws WdkUserException  {
	this.latestStep = step;
    }

    public void update(boolean overwrite)
	throws WdkUserException, WdkModelException {
	factory.updateStrategy(user, this, overwrite);
    }
}
