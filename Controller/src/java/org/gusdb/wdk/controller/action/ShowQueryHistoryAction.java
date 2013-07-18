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
 * This Action shows the queryHistory page.
 * 
 */

public class ShowQueryHistoryAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String historyType = request.getParameter(CConstants.WDK_HISTORY_TYPE_PARAM);
        String historyViewDir = CConstants.WDK_DEFAULT_VIEW_DIR
                + File.separator + CConstants.WDK_PAGES_DIR;
        ActionForward forward = null;

        String stratHistoryFile = historyViewDir + File.separator
                + CConstants.WDK_STRATEGY_HISTORY_PAGE;

        String stepHistoryFile = historyViewDir + File.separator
                + CConstants.WDK_STEP_HISTORY_PAGE;

        if (historyType != null && historyType.equalsIgnoreCase("step")) {
            forward = new ActionForward(stepHistoryFile);
        } else {
            forward = new ActionForward(stratHistoryFile);
        }

        return forward;
    }
}
