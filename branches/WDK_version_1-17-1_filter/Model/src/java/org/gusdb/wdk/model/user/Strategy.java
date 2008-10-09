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
    private String savedName = null;

    Strategy( UserFactory factory, User user, int strategyId, int internalId, String name ) {
	this.factory = factory;
	this.user = user;
	this.strategyId = strategyId;
	this.internalId = internalId;
	this.name = name;
	isSaved = false;
    }
    
    public User getUser() {
        return user;
    }

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }

    public void setSavedName(String savedName) {
	this.savedName = savedName;
    }
    
    public String getSavedName() {
	return savedName;
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
	return latestStep.getStep(index);
    }

    public Step[] getAllSteps() {
	return latestStep.getAllSteps();
    }

    public int getLength() {
	return latestStep.getLength();
    }

    public void addStep(Step step) 
	throws WdkUserException {
	if (latestStep != null) {
	    latestStep.addStep(step);
	}
	setLatestStep(step);
    }
    
    public void setLatestStep(Step step) 
	throws WdkUserException  {
	this.latestStep = step;
    }

    public Step getStepById(int id) {
	return latestStep.getStepById(id);
    }

    public void update(boolean overwrite)
	throws WdkUserException, WdkModelException {
	factory.updateStrategy(user, this, overwrite);
    }
}
