package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;

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
	String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR
	    + File.separator + CConstants.WDK_PAGES_DIR;
	String customViewFile = customViewDir + File.separator;
	if (errorType.equals(CConstants.ERROR_TYPE_USER)) {
	    customViewFile += CConstants.WDK_USER_ERROR_PAGE;
	} else if (errorType.equals(CConstants.ERROR_TYPE_MODEL)) {
	    customViewFile += CConstants.WDK_MODEL_ERROR_PAGE;
	} else {}

	ActionForward forward = new ActionForward(customViewFile);

	return forward;
    }
}
