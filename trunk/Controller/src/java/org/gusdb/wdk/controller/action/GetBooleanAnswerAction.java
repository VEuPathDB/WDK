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

public class GetBooleanAnswerAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	//todo: create AnswerBean
	System.err.println("GetBooleanAnswerAction: started");

	BooleanQuestionForm bqf = (BooleanQuestionForm)form;
	System.err.println("BQF params: properties are " + bqf.getMyProps().toString());
	BooleanQuestionNodeBean root = (BooleanQuestionNodeBean)request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY);
	Vector allNodes = new Vector();
        allNodes = root.getAllNodes(allNodes);
	
	for (int i = 0; i < allNodes.size(); i++){
	    Object nextNode = allNodes.elementAt(i);
	    if (nextNode instanceof BooleanQuestionLeafBean){
		
		BooleanQuestionLeafBean nextLeaf = (BooleanQuestionLeafBean)nextNode;
		processLeaf(bqf, nextLeaf);
	    }
	    else { //node bean
		BooleanQuestionNodeBean nextRealNode = (BooleanQuestionNodeBean)nextNode;
		processNode(nextRealNode);
	    }
	}
	//offending line of code
	root.setAllValues();
	
	//5. set values using hashtable
	//6. setAllValues on root (calls set all values on root bqn)
	//7. call make answer on root, pass in param values for root

	//Show Summary stuff
	int start = 1;
	if (request.getParameter("pager.offset") != null) {
	    start = Integer.parseInt(request.getParameter("pager.offset"));
	    start++;  //following Adrian's lead on this. (find out why it is necessary)
	}
	int pageSize = 20;
	if (request.getParameter("pageSize") != null) {
	    start = Integer.parseInt(request.getParameter("pageSize"));
	}
	if (start <1) { start = 1; } 
	int end = start + pageSize-1;

	AnswerBean answer = root.makeAnswer(1, 20);
	
	int totalSize = answer.getResultSize();
	
	if (end > totalSize) { end = totalSize; }
	
	String uriString = request.getRequestURI();
	List editedParamNames = new ArrayList();
	for (Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
	    String key = (String) en.nextElement();
	    if (!"pageSize".equals(key) && !"start".equals(key) &&!"pager.offset".equals(key)) {
		editedParamNames.add(key);
	    }
	}
	
	request.setAttribute("wdk_paging_total", new Integer(totalSize));
	request.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
	request.setAttribute("wdk_paging_start", new Integer(start));
	request.setAttribute("wdk_paging_end", new Integer(end));
	request.setAttribute("wdk_paging_url", uriString);
	request.setAttribute("wdk_paging_params", editedParamNames);
	
	request.setAttribute(CConstants.WDK_ANSWER_KEY, answer);
	
	ActionForward forward = mapping.findForward(CConstants.GET_BOOLEAN_ANSWER_MAPKEY);
	return forward;
    }


    private void processNode(BooleanQuestionNodeBean node){
	Hashtable values = new Hashtable();
	values.put(BooleanQuery.OPERATION_PARAM_NAME, node.getOperation());
	System.err.println("process node: created values: " + values.toString());
	node.setValues(values);
    }

    private void processLeaf(BooleanQuestionForm bqf, BooleanQuestionLeafBean leaf){

	Integer leafId = leaf.getLeafId();
	String leafPrefix = leafId.toString() + "_";
	QuestionBean question = leaf.getQuestion();
	ParamBean params[] = question.getParams();
	Hashtable values = new Hashtable();
	for (int i = 0; i < params.length; i++){
	    ParamBean nextParam = params[i];
	    String formParamName = leafPrefix + nextParam.getName();
	    String nextValue = (String)bqf.getMyProps().get(formParamName);
	    values.put(nextParam.getName(), nextValue);
	}
	System.err.println("process leaf, leafId " + leafId + ": here is the hashtable: " + values.toString());
	leaf.setValues(values);
    }

}



