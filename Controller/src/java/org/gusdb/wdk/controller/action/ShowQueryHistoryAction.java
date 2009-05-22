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
        String customStepFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_STEP_HISTORY_PAGE;
        String customStrategyFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_STRATEGY_HISTORY_PAGE;
        ActionForward forward = null;

        if (historyType != null && historyType.equalsIgnoreCase("step")) {
            if (ApplicationInitListener.resourceExists(customStepFile, svltCtx)) {
                forward = new ActionForward(customStepFile);
            } else {
                forward = mapping.findForward(CConstants.SHOW_STEP_HISTORY_MAPKEY);
            }
        } else {
            if (ApplicationInitListener.resourceExists(customStrategyFile,
                    svltCtx)) {
                forward = new ActionForward(customStrategyFile);
            } else {
                forward = mapping.findForward(CConstants.SHOW_STRAT_HISTORY_MAPKEY);
            }
        }

        return forward;
    }
}
