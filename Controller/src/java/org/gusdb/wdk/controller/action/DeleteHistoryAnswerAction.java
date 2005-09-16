package org.gusdb.wdk.controller.action;

import java.io.File;

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

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is process the download of Answers on queryHistory.jsp page.
 *
 */

public class DeleteHistoryAnswerAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	String ua_id_str = request.getParameter(CConstants.USER_ANSWER_ID);
	if (ua_id_str != null) {
	    int ua_id = Integer.parseInt(ua_id_str);
	    UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	    wdkUser.deleteAnswer(ua_id);
	} else {
	    throw new Exception ("no user answer id is given for deletion");
	}

	ActionForward forward = mapping.findForward(CConstants.DELETE_HISTORY_ANSWER_MAPKEY);

	return forward;
    }
}
