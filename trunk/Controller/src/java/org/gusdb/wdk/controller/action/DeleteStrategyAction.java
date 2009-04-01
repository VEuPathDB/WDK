package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(DeleteStrategyAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering DeleteStrategyAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        try {
            String[] stratIdstr = request.getParameterValues(CConstants.WDK_STRATEGY_ID_KEY);
            boolean getXml = Boolean.valueOf(request.getParameter("getXml")).booleanValue();

            if (stratIdstr != null && stratIdstr.length != 0) {
                for (int i = 0; i < stratIdstr.length; ++i) {
                    int stratId = Integer.parseInt(stratIdstr[i]);
                    wdkUser.deleteStrategy(stratId);
                    wdkUser.removeActiveStrategy(Integer.toString(stratId));
                }
            } else {
                throw new Exception("no strategy id is given for deletion");
            }

            ActionForward forward;
            if (!getXml) forward = mapping.findForward(CConstants.DELETE_HISTORY_MAPKEY);
            else forward = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);

            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, ex, response);
            return null;
        }
    }
}
