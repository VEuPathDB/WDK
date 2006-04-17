package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action shows the queryHistory page.
 *
 */

public class ShowQueryHistoryAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	request.getSession().removeAttribute(CConstants.QUESTIONFORM_KEY);
	request.getSession().removeAttribute(CConstants.WDK_QUESTION_PARAMS_KEY);

	UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	if (wdkUser == null) { 
	    ShowQuestionSetsAction.sessionStart(request, getServlet());
	}

	return getForward(mapping);
    }

    private ActionForward getForward (ActionMapping mapping) {
	ServletContext svltCtx = getServlet().getServletContext();
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
	String customViewFile = customViewDir + File.separator + CConstants.WDK_CUSTOM_HISTORY_PAGE;
	ActionForward forward = null;
	if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
	    forward = new ActionForward(customViewFile);
	} else {
	    forward = mapping.findForward(CConstants.SHOW_QUERY_HISTORY_MAPKEY);
	}
	return forward;
    }

}
