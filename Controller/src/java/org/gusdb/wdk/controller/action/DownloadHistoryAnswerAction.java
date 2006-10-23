package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is process the download of Answers on queryHistory.jsp page.
 *
 */

public class DownloadHistoryAnswerAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	getAnswerBean(request);
	ActionForward forward = mapping.findForward(CConstants.DOWNLOAD_HISTORY_ANSWER_MAPKEY);

	return forward;
    }

    protected AnswerBean getAnswerBean(HttpServletRequest request) throws Exception {
	String histIdstr = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
    if (histIdstr == null) {
        histIdstr = (String) request.getAttribute(CConstants.WDK_HISTORY_ID_KEY);
    }
	if (histIdstr != null) {
	    int histId = Integer.parseInt(histIdstr);
        UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);

        HistoryBean history = wdkUser.getHistory(histId);
	    AnswerBean wdkAnswer = history.getAnswer();
//	    if (userAnswer.isCombinedAnswer()) {
//		wdkAnswer.setIsCombinedAnswer(true);
//		wdkAnswer.setUserAnswerName(userAnswer.getName());
//	    }

	    request.setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswer);
	    request.setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY, wdkAnswer.getInternalParams());
	    request.setAttribute(CConstants.WDK_HISTORY_ID_KEY, histId);

	    return wdkAnswer;
	} else {
	    throw new Exception ("no history id is given for which to download the result");
	}
    }
}
