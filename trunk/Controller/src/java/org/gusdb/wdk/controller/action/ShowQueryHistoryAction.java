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

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.ApplicationInitListener;


/**
 * This Action shows the queryHistory page.
 *
 */

public class ShowQueryHistoryAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
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
