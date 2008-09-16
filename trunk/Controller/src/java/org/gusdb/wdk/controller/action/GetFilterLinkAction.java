package org.gusdb.wdk.controller.action;

import java.util.HashMap;
import java.util.Date;

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

        String description = answer.getQuestion().getRecordClass().getFilter(filterName).getDescription();

	//need to build link to summary page for specified filter
	ActionForward showSummary = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
	StringBuffer url = new StringBuffer(showSummary.getPath());
	url.append("?questionFullName=" + answer.getQuestion().getFullName());
	url.append(answer.getSummaryUrlParams());
	url.append("&filter=" + filterName);            

	String link = "<a href='" + url.toString() + "' onmouseover=displayDetails('" +
	    filterName + "') onmouseout=hideDetails('" + filterName + "')>" + size + "</a>";
	link += "<div class='hidden' id='div_" + filterName + "'>" + description + "</div>";

	System.out.println("link text sent to client: " + link);

	// check if we already have a cache of this answer
	HashMap<String, HashMap> cachedAnswers = (HashMap<String, HashMap>) request.getSession().getAttribute("answer_cache");
	HashMap<String, Date> answerTimes = (HashMap<String, Date>) request.getSession().getAttribute("answer_times");
	HashMap<String, String> cachedLinks;
	if (cachedAnswers != null && cachedAnswers.containsKey(answer.getChecksum())) {
	    // already have cache:  add this link to the cache
	    cachedLinks = cachedAnswers.get(answer.getChecksum());
	}
	else {
	    // don't have cache:  create a new cache
	    cachedLinks = new HashMap<String, String>();
	    if (cachedAnswers == null) {
		// no answer cache in session: create a new one
		cachedAnswers = new HashMap<String, HashMap>();
		answerTimes = new HashMap<String, Date>();
	    }
	    else if (cachedAnswers.size() == 3) {
		Date temp = new Date();
		String removeKey = "";
		for (String key : answerTimes.keySet()) {
		    if (temp.compareTo(answerTimes.get(key)) > 0) {
			temp = answerTimes.get(key);
			removeKey = key;
		    }
		}
		cachedAnswers.remove(removeKey);
		answerTimes.remove(removeKey);
	    }
	}

	cachedLinks.put(filterName, link);

	// now answer cache exists & has < 3 answers, add this one
	cachedAnswers.put(answer.getChecksum(), cachedLinks);
	answerTimes.put(answer.getChecksum(), new Date());

	request.getSession().setAttribute("answer_cache", cachedAnswers);
	request.getSession().setAttribute("answer_times", answerTimes);
	
	// What header, content type to send?
	ServletOutputStream out = response.getOutputStream();
	response.setContentType("text/html");

	// print link to response
	out.print(link);
	out.flush();
	out.close();

	return null;
    }
}
