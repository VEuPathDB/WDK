package org.gusdb.wdk.controller.action;

import java.util.Map;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.Summary;
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
	Summary wdkQuestion = qForm.getQuestion();

	Map params = new java.util.HashMap(qForm.getMyProps());
	System.err.println("ShowSummaryAction: num params: " + params.size() );


	java.util.Iterator paramNames = params.keySet().iterator();
	while (paramNames.hasNext()) {
	    String paramName = (String)paramNames.next();
	    Object paramVal = params.get(paramName);
	    System.err.println("*** params: (k, v) = " + paramName + ", " + paramVal);
	}
	//int s = qForm.getAnswerStartIndex();
	int s = 0;
	//int e = qForm.getAnswerEndIndex();
	int e = 50;
	SummaryInstance wdkSummary = wdkQuestion.makeSummaryInstance(params, s, e);

	request.getSession().setAttribute(CConstants.WDK_SUMMARY_KEY, wdkSummary);
	
	ActionForward forward = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
	return forward;
    }
}
