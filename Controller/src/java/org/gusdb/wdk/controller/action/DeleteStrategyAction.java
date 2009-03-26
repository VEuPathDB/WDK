package org.gusdb.wdk.controller.action;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action processes the delete action on the history page
 * 
 */

public class DeleteStrategyAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String[] stratIdstr = request.getParameterValues(CConstants.WDK_STRATEGY_ID_KEY);
	boolean getXml = Boolean.valueOf(request.getParameter("getXml")).booleanValue();
		
	if (stratIdstr != null && stratIdstr.length != 0) {
	    for (int i = 0; i < stratIdstr.length; ++i) {
		int stratId = Integer.parseInt(stratIdstr[i]);
		UserBean wdkUser = (UserBean) request.getSession().getAttribute(CConstants.WDK_USER_KEY);
		try {
		    wdkUser.deleteStrategy(stratId);
		    wdkUser.removeActiveStrategy(Integer.toString(stratId));
		} catch (Exception e) {
		    e.printStackTrace();
		    // prevent refresh of page after delete from breaking
		}
	    }
	}
	else {
	    throw new Exception("no strategy id is given for deletion");
	}
	
        ActionForward forward;
	if (!getXml)
	    forward = mapping.findForward(CConstants.DELETE_HISTORY_MAPKEY);
	else
	    forward = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
	
        return forward;
    }
}
