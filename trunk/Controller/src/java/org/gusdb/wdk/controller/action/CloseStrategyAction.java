package org.gusdb.wdk.controller.action;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
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

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();

        if (activeStrategies != null) {
            String stratIdstr = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            if (stratIdstr == null || stratIdstr.length() == 0) {
                throw new Exception("No strategy specified to close!");
            }
            if (activeStrategies.contains(Integer.parseInt(stratIdstr))) {
                activeStrategies.remove(activeStrategies.indexOf(Integer.parseInt(stratIdstr)));
                wdkUser.setActiveStrategies(activeStrategies);
            }
        }

        return null;
    }
}
