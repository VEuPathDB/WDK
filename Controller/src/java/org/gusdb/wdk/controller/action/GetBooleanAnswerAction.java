package org.gusdb.wdk.controller.action;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.BooleanQuery;

import java.util.Vector;
import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class GetBooleanAnswerAction extends ShowSummaryAction {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {


	BooleanQuestionForm bqf = (BooleanQuestionForm)form;

	Object root = request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY);
	AnswerBean answer;
	if (root instanceof BooleanQuestionLeafBean) {
	    BooleanQuestionLeafBean rootLeaf = (BooleanQuestionLeafBean)root;
	    QuestionBean leafQuestion = rootLeaf.getQuestion();
	    Map params = getParamsFromForm(bqf, rootLeaf);
	    answer = summaryPaging(request, leafQuestion, params);
	} else {
	    BooleanQuestionNodeBean rootNode = (BooleanQuestionNodeBean)root;
	    Vector allNodes = new Vector();
	    allNodes = rootNode.getAllNodes(allNodes);
	
	    for (int i = 0; i < allNodes.size(); i++){
		Object nextNode = allNodes.elementAt(i);
		if (nextNode instanceof BooleanQuestionLeafBean){
		
		    BooleanQuestionLeafBean nextLeaf = (BooleanQuestionLeafBean)nextNode;
		    Hashtable values = getParamsFromForm(bqf, nextLeaf);
		    nextLeaf.setValues(values);
		}
		else { //node bean
		    BooleanQuestionNodeBean nextRealNode = (BooleanQuestionNodeBean)nextNode;
		    processNode(bqf, nextRealNode);
		}
	    }
	    rootNode.setAllValues();
	    answer = booleanAnswerPaging(request, rootNode);
	}

	request.setAttribute(CConstants.WDK_ANSWER_KEY, answer);
	
	ActionForward forward = mapping.findForward(CConstants.GET_BOOLEAN_ANSWER_MAPKEY);
	return forward;
    }


    private void processNode(BooleanQuestionForm bqf, BooleanQuestionNodeBean node){
	Hashtable values = new Hashtable();
	String opInternalName = node.getOperation();
	
	//	String value = (String)bqf.getMyProps().get(opDisplayName);
	values.put(BooleanQuery.OPERATION_PARAM_NAME, opInternalName);
	node.setValues(values);
    }

    private Hashtable getParamsFromForm(BooleanQuestionForm bqf, BooleanQuestionLeafBean leaf){

	Integer leafId = leaf.getLeafId();
	String leafPrefix = leafId.toString() + "_";
	QuestionBean question = leaf.getQuestion();
	ParamBean params[] = question.getParams();
	Hashtable values = new Hashtable();
	for (int i = 0; i < params.length; i++){
	    ParamBean nextParam = params[i];
	    String formParamName = leafPrefix + nextParam.getName();
	    Object nextValObj = bqf.getMyProps().get(formParamName);
	    String nextValue;
	    if (nextValObj instanceof String[]) {
		String[] vals = (String[])nextValObj;
		nextValue = vals[0];
		for (int j=1; j<vals.length; j++) { nextValue += "," + vals[j]; }
	    } else {
		nextValue = (String)nextValObj;
	    }
	    values.put(nextParam.getName(), nextValue);
	}
	return values;
    }
}



