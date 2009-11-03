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

/**
 * This Action shows the queryHistory page.
 * 
 */

public class ShowQueryHistoryAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ServletContext svltCtx = getServlet().getServletContext();
        String historyType = request.getParameter(CConstants.WDK_HISTORY_TYPE_PARAM);
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        ActionForward forward = null;

	String stratHistoryFile = CConstants.WDK_DEFAULT_VIEW_DIR
	    + File.separator + CConstants.WDK_PAGES_DIR
	    + File.separator + "strategyHistory.jsp";
	
	String stepHistoryFile = CConstants.WDK_DEFAULT_VIEW_DIR
	    + File.separator + CConstants.WDK_PAGES_DIR
	    + File.separator + "stepHistory.jsp";

        if (historyType != null && historyType.equalsIgnoreCase("step")) {
	    forward = new ActionForward(stepHistoryFile);
        } else {
	    forward = new ActionForward(stratHistoryFile);
        }

        return forward;
    }
}
