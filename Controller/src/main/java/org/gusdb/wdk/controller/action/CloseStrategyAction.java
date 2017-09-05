package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This action is called by the UI in order to "close" a strategy. It removes
 * the specified strategy id from the strategy id list stored in the session.
 */

public class CloseStrategyAction extends Action {

    private static Logger logger = Logger.getLogger(CloseStrategyAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering CloseStrategyAction");

        UserBean wdkUser = ActionUtility.getUser(request);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            String strStratKeys = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String[] stratIdstr = (strStratKeys == null || strStratKeys.length() == 0) ?
                new String[0] : strStratKeys.split(",");
            String currentStrategyKey = wdkUser.getViewStrategyId();

            // Check if currentStrategyId is null. This can happen if the only
            // open strategy has an invalid step. Setting this to true will
            // short-circuit the nested if statement below.
            boolean changeViewStrategy = currentStrategyKey == null;

            if (stratIdstr.length != 0) {
                for (int i = 0; i < stratIdstr.length; ++i) {
                    logger.debug("closing strategy: '" + stratIdstr[i] + "'");
                    wdkUser.removeActiveStrategy(stratIdstr[i]);
                    if (!changeViewStrategy && currentStrategyKey.equals(stratIdstr[i])) {
                        changeViewStrategy = true;
                    }
                }
            } else {
                throw new Exception("No strategy specified to close!");
            }

            String firstStrategyKey = "";

            if (changeViewStrategy) {
                long[] activeStrategyIds = wdkUser.getUser().getSession().getActiveStrategyIds();
                if (activeStrategyIds.length > 0) {
                    long firstStrategyId = activeStrategyIds[activeStrategyIds.length - 1];
                    firstStrategyKey = Long.toString(firstStrategyId);
                }
            }

            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?state=" + FormatUtil.urlEncodeUtf8(state));
            // reset the strategy id, otherwise it will be opened again by showStrategy.do
            url.append("&" + CConstants.WDK_STRATEGY_ID_KEY + "=");
            // open first active strategy, if there is one
            url.append(firstStrategyKey);
            ActionForward forward = new ActionForward(url.toString());
            forward.setRedirect(false);

            logger.debug("Leaving, redirecting to " + url);

            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }
}
