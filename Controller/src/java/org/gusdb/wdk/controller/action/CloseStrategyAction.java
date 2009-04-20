package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;

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
 * This action is called by the UI in order to "close" a strategy. It removes
 * the specified strategy id from the strategy id list stored in the session.
 */

public class CloseStrategyAction extends Action {

    private static Logger logger = Logger.getLogger(CloseStrategyAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering CloseStrategyAction");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            String stratIdstr = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            if (stratIdstr == null || stratIdstr.length() == 0) {
                throw new Exception("No strategy specified to close!");
            }
logger.debug("closing strategy: '" + stratIdstr + "'");
            wdkUser.removeActiveStrategy(stratIdstr);

            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?state=" + URLEncoder.encode(state, "UTF-8"));
            ActionForward forward = new ActionForward(url.toString());
            forward.setRedirect(true);
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }
}
