package org.gusdb.wdk.controller.action;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;

/**
 * @author Charles
 */
public class GetFilterLinkAction extends Action {
    private static Logger logger = Logger.getLogger(GetFilterLinkAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response)
	throws Exception {
	
	WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	UserBean wdkUser = (UserBean) request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	if (wdkUser == null) {
	    wdkUser = wdkModel.getUserFactory().getGuestUser();
	    request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
	}

	//get the history id
	String historyId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
	String filterName = request.getParameter("filter");

	if (historyId == null || historyId.length() == 0 ||
	    filterName == null || filterName.length() == 0) {
	    throw new WdkModelException("Missing parameters for GetFilterLinkAction.");
	}

	HistoryBean history = wdkUser.getHistory(Integer.parseInt(historyId));
	AnswerBean answer = history.getAnswer();
	
	int size = answer.getFilterSize(filterName);

	//need to build link to summary page for specified filter
	ActionForward showSummary = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
	StringBuffer url = new StringBuffer(showSummary.getPath());
	url.append("?questionFullName=" + answer.getQuestion().getFullName());
	url.append(answer.getSummaryUrlParams());
	url.append("&filter=" + filterName);            
	
	// What header, content type to send?
	ServletOutputStream out = response.getOutputStream();
	response.setContentType("text/html");
	
	String link = "<a href='" + url.toString() + "'>" + size + "</a>";

	//request.setAttribute("filterLink", url.toString());
	//request.setAttribute("filterSize", size);

	out.print(link);
	out.flush();
	out.close();
	
	//ActionForward forward = mapping.findForward("filter_link");
	//return forward;

	return null;
    }
}
