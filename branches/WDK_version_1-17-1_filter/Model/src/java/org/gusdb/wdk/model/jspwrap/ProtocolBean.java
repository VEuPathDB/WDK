package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class ProtocolBean {
    
    StepBean latestStep;
    String name;

    public ProtocolBean(StepBean step, String name) {
	this.latestStep = step;
	this.name = name;
    }

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }

    public StepBean getLatestStep() {
	return latestStep;
    }
    
    public int getProtocolId() {
	return latestStep.getFilterHistory().getHistoryId();
    }

    public StepBean getStep(int index) {
	StepBean[] steps = getAllSteps();
	return steps[index];
    }

    public StepBean[] getAllSteps() {
	ArrayList<StepBean> allSteps = new ArrayList<StepBean>();
	allSteps = buildAllStepsArray(allSteps, latestStep);
	return allSteps.toArray(new StepBean[allSteps.size()]);
    }

    private ArrayList<StepBean> buildAllStepsArray(ArrayList<StepBean> array, StepBean step) {
	if (step.isFirstStep()) {
	    array.add(step);
	}
	else {
	    array = buildAllStepsArray(array, step.getPreviousStep());
	    array.add(step);
	}
	return array;
    }

    public void addStep(StepBean step) {
	if (latestStep != null) {
	    step.setPreviousStep(latestStep);
	    latestStep.setNextStep(step);
	}
	//this.latestStep = step;
	setLatestStep(step);
    }
    
    public void setLatestStep(StepBean step) {
	this.latestStep = step;
    }

    /*
     *
     * Static methods
     *
     */

    public static ProtocolBean getProtocol(String protocolId, ProtocolBean protocol, UserBean wdkUser) 
	throws WdkModelException, WdkUserException {
	HistoryBean filterHistory = wdkUser.getHistory(Integer.parseInt(protocolId));
	
	StepBean step = new StepBean();
	step.setFilterHistory(filterHistory);

	if (filterHistory.isBoolean()) {
	    String[] expParts = filterHistory.getBooleanExpression().split("\\s+");
	    if (expParts.length != 3) {
		throw new WdkModelException("Protocol boolean expression must have two arguments and one operation.  Received: " + filterHistory.getBooleanExpression());
	    }
	    HistoryBean subQueryHistory = wdkUser.getHistory(Integer.parseInt(expParts[2]));
	    step.setSubQueryHistory(subQueryHistory);
	    if (protocol == null) {
		protocol = getProtocol(expParts[0], protocol, wdkUser);
	    }
	    protocol.addStep(step);
	}
	else if (protocol == null) {
	    protocol = new ProtocolBean(step, "Load name here.");
	}

	return protocol;
    }
}
