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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;

/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class ShowSummaryAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	//why I am not able to get back my question from the session? use the form for  now
	//QuestionBean wdkQuestion = (QuestionBean)request.getSession().getAttribute(CConstants.WDK_QUESTION_KEY);
	
	QuestionForm qForm = (QuestionForm)form;
	QuestionBean wdkQuestion = qForm.getQuestion();
		    
	Map params = handleMultiPickParams(new java.util.HashMap(qForm.getMyProps()));
		
	AnswerBean wdkAnswer = summaryPaging(request, wdkQuestion, params);

	request.getSession().setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswer);
	
	ActionForward forward = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
	return forward;
    }

    protected Map handleMultiPickParams (Map params) 
    {
	java.util.Iterator newParamNames = params.keySet().iterator();
	while (newParamNames.hasNext()) {
	    String paramName = (String)newParamNames.next();
	    Object paramVal = params.get(paramName);
	    String paramValStr = null;
	    if (paramVal instanceof String[]) {
		String[] pVals = (String[])paramVal;
		paramValStr = pVals[0];
		for (int i=1; i<pVals.length; i++) { paramValStr += "," + pVals[i]; }
		params.put(paramName, paramValStr);
	    } else {
		paramValStr = (paramVal == null ? null : paramVal.toString());
	    }
	    //System.err.println("*** debug params: (k, v) = " + paramName + ", " + paramValStr);
	}
	return params;
    }
    
    protected AnswerBean booleanAnswerPaging(HttpServletRequest request, Object answerMaker)
	throws WdkModelException, WdkUserException
    {
	return summaryPaging(request, answerMaker, null);
    }

    protected AnswerBean summaryPaging (HttpServletRequest request, Object answerMaker, Map params)
	throws WdkModelException, WdkUserException
    {
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

	AnswerBean wdkAnswer = null;
	if (answerMaker instanceof org.gusdb.wdk.model.jspwrap.QuestionBean) {
	    wdkAnswer = ((QuestionBean)answerMaker).makeAnswer(params, start, end);
	} else if (answerMaker instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean) {
	    wdkAnswer = ((BooleanQuestionNodeBean)answerMaker).makeAnswer(start, end);
	} else {
	    throw new RuntimeException("unexpected answerMaker: " + answerMaker);
	}
	int totalSize = wdkAnswer.getResultSize();

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
	return wdkAnswer;
    }
}



