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


public class GrowBooleanAction extends StartBooleanAction{

    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	BooleanQuestionForm bqf = (BooleanQuestionForm)form;
	System.err.println("Grow Boolean Action: starting method");

	Object currentRoot = request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY);
	//first time user has grown something; tree is one node consisting of leaf
	if (currentRoot instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean) {
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
	} else {
	    throw new RuntimeException("expect BooleanQuestion<Leaf|Node>Bean but got: " + currentRoot);
	}

	//request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY, currentRoot);

	//recursive forward
	ActionForward forward = mapping.findForward(CConstants.GROW_BOOLEAN_MAPKEY);
	    
	return forward;
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
