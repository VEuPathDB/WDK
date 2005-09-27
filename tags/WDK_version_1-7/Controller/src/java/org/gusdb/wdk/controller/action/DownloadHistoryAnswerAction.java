package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
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

	String ua_id_str = request.getParameter(CConstants.USER_ANSWER_ID);
	if (ua_id_str != null) {
	    int ua_id = Integer.parseInt(ua_id_str);
	    UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	    AnswerBean wdkAnswer = wdkUser.getUserAnswerByID(ua_id).getAnswer();
	    request.getSession().setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswer);
	    request.getSession().setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY, wdkAnswer.getInternalParams());
	} else {
	    throw new Exception ("no user answer id is given for which to download the result");
	}

	ActionForward forward = mapping.findForward(CConstants.DOWNLOAD_HISTORY_ANSWER_MAPKEY);

	return forward;
    }
}
