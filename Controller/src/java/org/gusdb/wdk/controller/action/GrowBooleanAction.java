package org.gusdb.wdk.controller.action;

import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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


public class GrowBooleanAction extends Action{

        public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	    BooleanQuestionForm bqf = null;
	    System.err.println("Grow Boolean Action: starting method");

	    //first time entering growBoolean page; create form and initialize tree
	    Object currentRoot = request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_MAPKEY);
	    if (currentRoot == null){
		System.err.println("GrowBooleanAction: creating new BQF");
		bqf = new BooleanQuestionForm();
		bqf.setMyProps(new HashMap());
		bqf.setMyLabels(new HashMap());
		bqf.setMyValues(new HashMap());
		ActionServlet servlet = getServlet();
		bqf.setServlet(servlet);
	
		WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
		//change this so that it is getting it from the Leaf
		EnumParamBean booleanOps = wdkModel.getBooleanOps();
		String[] booleanOpsVocab = booleanOps.getVocab();
	    
		bqf.getMyLabels().put(CConstants.BOOLEAN_OPERATIONS_PARAM_NAME, booleanOpsVocab);
		bqf.getMyValues().put(CConstants.BOOLEAN_OPERATIONS_PARAM_NAME, booleanOpsVocab);

		//get seed question, make into root of new tree
		QuestionBean wdkQuestion = (QuestionBean)request.getSession().getAttribute(CConstants.WDK_QUESTION_KEY);
		BooleanQuestionLeafBean leaf = wdkQuestion.makeBooleanQuestionLeaf();
		addNewQuestionParams(bqf, leaf);
		request.getSession().setAttribute(CConstants.BOOLEAN_SEED_QUESTION_MAPKEY, wdkQuestion);
		request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_MAPKEY, leaf);
	
	    }
	    //first time user has grown something; tree is one node consisting of leaf
	    else if (currentRoot instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {

		bqf = (BooleanQuestionForm)form;
		BooleanQuestionLeafBean currentLeafRoot = (BooleanQuestionLeafBean)currentRoot;
		
		String operation = bqf.getNextBooleanOperation();
		String nextQuestionName = bqf.getNextQuestionOperand();
		System.err.println("new question to add is " + nextQuestionName);
		QuestionBean nextQuestion = getQuestionFromModel(nextQuestionName);
		BooleanQuestionLeafBean newLeaf = nextQuestion.makeBooleanQuestionLeaf();
		
		newLeaf.setLeafId(bqf.getNextId());
		WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);

		currentLeafRoot.grow(newLeaf, operation, wdkModel);
		
		//add params from incoming leaf to form
		addNewQuestionParams(bqf, newLeaf);
	    }
	    else if (currentRoot instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean){
		//leafToGrow = currentRoot.find(nodeId);
	    }

	    
	    //necessary to do each recursive step?
	    request.getSession().setAttribute(CConstants.BOOLEAN_QUESTION_FORM_KEY, bqf);

	    //recursive forward
    	    ActionForward forward = mapping.findForward(CConstants.GROW_BOOLEAN_MAPKEY);
	    
	    return forward;
	}
    
    
    private void addNewQuestionParams(BooleanQuestionForm bqf, BooleanQuestionLeafBean leaf){

	Integer leafPrefixInt = leaf.getLeafId();
	String leafPrefix = leafPrefixInt.toString() + "_";
	
	QuestionBean leafQuestion = leaf.getQuestion();
	ParamBean[] params = leafQuestion.getParams();

	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    if (p instanceof FlatVocabParamBean) {
		//not assuming fixed order, so call once, use twice.
		String[] flatVocab = ((FlatVocabParamBean)p).getVocab();
		bqf.getMyLabels().put(leafPrefix +  p.getName(), flatVocab);
		bqf.getMyValues().put(leafPrefix + p.getName(), flatVocab);
	    }
	    bqf.getMyProps().put(leafPrefix + p.getName(), p.getDefault());
	}
    }

    private QuestionBean getQuestionFromModel(String fullQuestionName){
	int dotI = fullQuestionName.indexOf('.');
	String qSetName = fullQuestionName.substring(0, dotI);
	String qName = fullQuestionName.substring(dotI+1, fullQuestionName.length());
	System.err.println("getQuestionFromModel: getting qset " + qSetName + " question " + qName);
	WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	if (wdkModel == null){ System.err.println("couldn't find model"); }
	QuestionSetBean wdkQuestionSet = (QuestionSetBean)wdkModel.getQuestionSetsMap().get(qSetName);
	if (wdkQuestionSet == null){ System.err.println("couldn't find qset"); }
	QuestionBean wdkQuestion = (QuestionBean)wdkQuestionSet.getQuestionsMap().get(qName);
	if (wdkQuestion == null){ System.err.println("couldn't find question"); }
	return wdkQuestion;
    }
}
