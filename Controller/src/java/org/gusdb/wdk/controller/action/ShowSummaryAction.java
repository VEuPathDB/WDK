package org.gusdb.wdk.controller.action;

import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.SummaryInstance;

/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the summary
 *    3) forwards control to a jsp page that displays a summary
 */

public class ShowSummaryAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	//why I am not able to get back my question from the session? use the form for  now
	//Summary wdkQuestion = (Summary)request.getSession().getAttribute(CConstants.WDK_QUESTION_KEY);

	QuestionForm qForm = (QuestionForm)form;
	Question wdkQuestion = qForm.getQuestion();

	Map params = new java.util.HashMap(qForm.getMyProps());
	/*
	java.util.Iterator paramNames = params.keySet().iterator();
	while (paramNames.hasNext()) {
	    String paramName = (String)paramNames.next();
	    Object paramVal = params.get(paramName);
	    System.err.println("*** params: (k, v) = " + paramName + ", " + paramVal);
	}
	*/

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

	SummaryInstance wdkSummary = wdkQuestion.makeSummaryInstance(params, start, end);

	int totalSize = wdkSummary.getResultSize();
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

	request.getSession().setAttribute(CConstants.WDK_SUMMARY_KEY, wdkSummary);
	
	ActionForward forward = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
	return forward;
    }
}
