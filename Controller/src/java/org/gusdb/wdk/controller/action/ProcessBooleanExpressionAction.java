package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;


/**
 * This Action is process boolean expression on queryHistory.jsp page.
 *
 */

public class ProcessBooleanExpressionAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	BooleanExpressionForm beForm = (BooleanExpressionForm)form;
	booleanExpressionPaging(request, beForm);

	ActionForward forward = mapping.findForward(CConstants.PROCESS_BOOLEAN_EXPRESSION_MAPKEY);

	return forward;
    }

    private void booleanExpressionPaging (HttpServletRequest request, BooleanExpressionForm beForm)
	throws WdkModelException, WdkUserException
    {
	int start = 1;
	if (request.getParameter("pager.offset") != null) {
	    start = Integer.parseInt(request.getParameter("pager.offset"));
	    start++;
	}
	int pageSize = 20;
	if (request.getParameter("pageSize") != null) {
	    pageSize = Integer.parseInt(request.getParameter("pageSize"));
	}

	if (start <1) { start = 1; } 
	int end = start + pageSize-1;	

	UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	UserAnswerBean userAnswer = wdkUser.combineAnswers(beForm.getBooleanExpression(), start, end);
	AnswerBean wdkAnswer = userAnswer.getAnswer();
	int aid = userAnswer.getAnswerID();

	request.setAttribute(CConstants.USER_ANSWER_ID, new Integer(aid).toString());
	wdkUser.addAnswerFuzzy(wdkAnswer);

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
    }
}
