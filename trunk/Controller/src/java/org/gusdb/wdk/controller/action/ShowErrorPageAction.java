package org.gusdb.wdk.controller.action;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import java.util.Vector;
import java.io.File;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.ApplicationInitListener;

/**
 * This Action is called by the ActionServlet when an error occurred.
 * It 1) finds the type of error,
 *    3) forwards control to a jsp page specific to this error type
 */

public class ShowErrorPageAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	String type = request.getParameter(CConstants.ERROR_TYPE_PARAM);
	if (type == null || type.equals("") || type.equals(CConstants.ERROR_TYPE_MODEL)) {
            return getForward(mapping, CConstants.ERROR_TYPE_MODEL);
	} else if (type.equals(CConstants.ERROR_TYPE_USER)) {
	    return getForward(mapping, CConstants.ERROR_TYPE_USER);
	} else {
	    //really should not come here
	    return getForward(mapping, CConstants.ERROR_TYPE_MODEL);
	}
    }

    private ActionForward getForward (ActionMapping mapping, String errorType) {
	ServletContext svltCtx = getServlet().getServletContext();
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
	String customViewFile = customViewDir + File.separator;
	if (errorType.equals(CConstants.ERROR_TYPE_USER)) {
	    customViewFile += "customError.user.jsp";
	} else if (errorType.equals(CConstants.ERROR_TYPE_MODEL)) {
	    customViewFile += "customError.jsp";
	} else {}

	ActionForward forward = null;
	if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
	    System.out.println("DEBUG: SEPA: using custom page " + customViewFile);
	    forward = new ActionForward(customViewFile);
	} else {
	    if (errorType.equals(CConstants.ERROR_TYPE_USER)) {
		System.out.println("DEBUG: SEPA: using user error page");
		forward = mapping.findForward(CConstants.SHOW_ERRORPAGE_USER_MAPKEY);
	    } else {
		System.out.println("DEBUG: SEPA: using model error page");
		forward = mapping.findForward(CConstants.SHOW_ERRORPAGE_MODEL_MAPKEY);
	    }
	}
	return forward;
    }
}
