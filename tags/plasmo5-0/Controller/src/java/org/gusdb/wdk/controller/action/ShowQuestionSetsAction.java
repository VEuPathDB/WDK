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
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is a glue action to allow display of questionSetsFlat to be handled uniformly.
 * It forwards on the control
 */

public class ShowQuestionSetsAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	ServletContext svltCtx = getServlet().getServletContext();
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
	String customViewFile = customViewDir + File.separator
	    + CConstants.WDK_CUSTOM_QUESTIONSETS_PAGE;

	ActionForward forward = null;
	if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
	    forward = new ActionForward(customViewFile);
	} else {
	    forward = mapping.findForward(CConstants.SHOW_QUESTIONSETS_MAPKEY);
	}

	sessionStart(request, getServlet());

	return forward;
    }

    protected static void sessionStart (HttpServletRequest request, HttpServlet servlet) {
	if (request.getSession().getAttribute(CConstants.WDK_USER_KEY) != null) {
	    return;
	}
	WdkModelBean wdkModel = (WdkModelBean)servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	HttpSession session = request.getSession();
	String sessionId = session.getId();
	UserBean user = wdkModel.createUser(sessionId);
	request.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
    }
}
