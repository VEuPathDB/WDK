package org.gusdb.wdk.controller.action;

import java.util.HashMap;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;


public class GrowBooleanAction extends StartBooleanAction{

    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	BooleanQuestionForm bqf = (BooleanQuestionForm)form;
	System.err.println("Grow Boolean Action: starting method");

	Object currentRoot = request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY);

	String operation = bqf.getNextBooleanOperation();
	String nextQuestionName = bqf.getNextQuestionOperand();
	System.err.println("new question to add is " + nextQuestionName);
	QuestionBean nextQuestion = getQuestionFromModel(nextQuestionName);
	BooleanQuestionLeafBean newLeaf = nextQuestion.makeBooleanQuestionLeaf();
	newLeaf.setLeafId(bqf.getNextId());
	WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	//first time user has grown something; tree is one node consisting of leaf
	if (currentRoot instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {
	    BooleanQuestionLeafBean currentLeafRoot = (BooleanQuestionLeafBean)currentRoot;		
	    currentLeafRoot.grow(newLeaf, operation, wdkModel);
	    currentRoot = currentLeafRoot.getParent();
	}
	else if (currentRoot instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean){
	    String  submitAction = request.getParameter("process_boolean_question");
	    int leafId = parseLeafId(submitAction);
	    BooleanQuestionNodeBean currentNodeRoot = (BooleanQuestionNodeBean)currentRoot;	    
	    BooleanQuestionLeafBean leafToGrow = currentNodeRoot.findLeaf(leafId);
	    leafToGrow.grow(newLeaf, operation, wdkModel);
	} else {
	    throw new RuntimeException("expect BooleanQuestion<Leaf|Node>Bean but got: " + currentRoot);
	}

	System.err.println("Current Root after grow: " + currentRoot);
	//add params from incoming leaf to form
	addNewQuestionParams(bqf, newLeaf);

	request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY, currentRoot);

	//recursive forward
	ActionForward forward = mapping.findForward(CConstants.GROW_BOOLEAN_MAPKEY);
	    
	return forward;
    }

    private int parseLeafId (String submitAction) {
	int startIdx = submitAction.indexOf('(');
	int endIdx = submitAction.indexOf(')');
	String leafIdStr = submitAction.substring(startIdx+1, endIdx);
	int leafId;
	try {
	    leafId = Integer.parseInt(leafIdStr);
	} catch (NumberFormatException e) {
	    throw new RuntimeException (e.getMessage() + " when parsing " + submitAction + " for leafId");
	} 
	return leafId;
    }

    private QuestionBean getQuestionFromModel(String fullQuestionName){
	int dotI = fullQuestionName.indexOf('.');
	String qSetName = fullQuestionName.substring(0, dotI);
	String qName = fullQuestionName.substring(dotI+1, fullQuestionName.length());
	System.err.println("getQuestionFromModel: getting qset " + qSetName + " question " + qName);
	WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	if (wdkModel == null){
	    throw new RuntimeException("couldn't find model by key: " + CConstants.WDK_MODEL_KEY);
	}
	QuestionSetBean wdkQuestionSet = (QuestionSetBean)wdkModel.getQuestionSetsMap().get(qSetName);
	if (wdkQuestionSet == null){
	    throw new RuntimeException("couldn't find qset: " + qSetName 
				       + " from sets: " + wdkModel.getQuestionSetsMap());
	}
	QuestionBean wdkQuestion = (QuestionBean)wdkQuestionSet.getQuestionsMap().get(qName);
	if (wdkQuestion == null){ 
	    throw new RuntimeException("couldn't find question: " + qName 
				       + " from questions: " + wdkQuestionSet.getQuestionsMap());
	}

	return wdkQuestion;
    }
}
