package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

import org.gusdb.wdk.model.jspwrap.ParamBean;

/**
 *  form bean for setting up a boolean question
 */

public class BooleanQuestionForm extends ActionForm {

    private Map myProps = new HashMap();
    private Map myLabels = new HashMap();
    private Map myValues = new HashMap();

    private BooleanQuestionNodeBean rootNode = null;
    private BooleanQuestionLeafBean seedLeaf = null;
    private int currentNodeId = 0;

    private QuestionBean newQuestion;
    private String operation;
    private String nextBooleanOperation;
    private String nextQuestionOperand;

    public void reset() {
	//DTB -- differs from question form -- what should be reset?  Maybe parameters of all
	// Questions on page

	/*BooleanQuestionNodeBean bqn = getBooleanQuestionNode();
	ParamBean[] params = booleanQuestionNode.getParams();
	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    setMyProp(p.getName(), null);
	    }*/
    }

    /**
     * validate the properties that have been sent from the HTTP request,
     * and return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
	ActionErrors errors = new ActionErrors();

	//DTB -- need to figure out what to validate
	
	BooleanQuestionNodeBean booleanQuestionNode = getBooleanQuestionNode();
	//	ParamBean[] params = booleanQuestionNode.getParams();
	/*for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    try {
		Object pVal = getMyProp(p.getName());
		String errMsg = p.validateValue(pVal);
		if (errMsg != null) {
		    errors.add(ActionErrors.GLOBAL_ERROR,
			       new ActionError("mapped.properties", p.getPrompt(), errMsg));
		}
	    } catch (WdkModelException exp) {
		throw new RuntimeException(exp);
	    }
	    }*/
	return errors;
    }

    public int getNextId(){
	currentNodeId++;
	return currentNodeId;
    }

    public void setMyProp(String key, Object val)
    {
	myProps.put(key, val);
    }

    public Object getMyProp(String key)  throws WdkModelException
    {
	Object res = myProps.get(key);
	return res;
    }

    /* returns a list of labels for a select box */
    public String[] getLabels(String key) throws WdkModelException
    {
	return (String[])myLabels.get(key);
    }

    /* returns a list of values for a select box */
    public String[] getValues(String key) throws WdkModelException
    {
	return (String[])myValues.get(key);
    }

    void setMyProps(Map props) { myProps = props; }
    public Map getMyProps() { return myProps; }

    void setMyLabels (Map lbls) { myLabels = lbls; }
    Map getMyLabels () { return myLabels; }

    void setMyValues (Map vals) { myValues = vals; }
    Map getMyValues () { return myValues; }

    public String getOperation(){
	return operation;
    }

    public void setOperation(String op){
	this.operation = op;
    }

    public void setBooleanQuestionNode(BooleanQuestionNodeBean bqn) { rootNode = bqn; }
    public BooleanQuestionNodeBean getBooleanQuestionNode() { return rootNode; }

    public void setBooleanQuestionLeaf(BooleanQuestionLeafBean bqf) { seedLeaf = bqf; }
    public BooleanQuestionLeafBean getBooleanQuestionLeaf() { return seedLeaf;}

    public void setNextQuestionOperand(String nextQuestionOperand) {
	this.nextQuestionOperand = nextQuestionOperand;
    }

    public void setNextBooleanOperation(String nextBooleanOperation){
	this.nextBooleanOperation = nextBooleanOperation;
    }

    public String getNextQuestionOperand(){
	return nextQuestionOperand;
    }
    public String getNextBooleanOperation(){
	return nextBooleanOperation;
    }
}
