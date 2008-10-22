package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is process the download of Answers on queryHistory.jsp page.
 * 
 */

public class DeleteHistoryAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String[]  histIdstrs = request.getParameterValues(CConstants.WDK_HISTORY_ID_KEY);
        if (histIdstrs != null) {
	    for (String histIdstr : histIdstrs) {
		if (histIdstr != null && histIdstr.length() > 0) {
		    int histId = Integer.parseInt(histIdstr);
		    UserBean wdkUser = (UserBean) request.getSession().getAttribute(
						             CConstants.WDK_USER_KEY);
		    try {
			wdkUser.deleteHistory(histId);
		    } catch (Exception e) {
			e.printStackTrace();
			// prevent refresh of page after delete from breaking
		    }
		}
	    }
        } else {
	    // do we really throw an exception here?  why not just do nothing?
            throw new Exception("no history id was given for deletion");
        }

        ActionForward forward = mapping.findForward(CConstants.DELETE_HISTORY_MAPKEY);

        return forward;
    }
}
