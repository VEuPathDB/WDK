package org.gusdb.wdk.controller.action;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class ImportStrategyAction extends Action {
    private static final Logger logger = Logger.getLogger(ImportStrategyAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ImportStrategyAction...");

        // Change to importing by answer checksum
        String strategyKey = request.getParameter("strategy");

        if (strategyKey == null || strategyKey.length() == 0) {
            strategyKey = request.getParameter("s");    // try a shorter version
        }
        if (strategyKey == null || strategyKey.length() == 0) {
            throw new WdkModelException(
                    "No strategy key was specified for importing!");
        }

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        StrategyBean strategy = wdkUser.importStrategy(strategyKey);

	wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));

	/* Charles Treatman 4/23/09
         * Add code here to set the current_application_tab cookie
	 * so that user will go to the Run Strategies tab after
	 * importing a strategy
	 */
	Cookie tabCookie = new Cookie("current_application_tab","strategy_results");
	// make sure it's only a session cookie, not persistent
	tabCookie.setMaxAge(-1);
	// make sure the cookie is good for whole site, not just webapp
	tabCookie.setPath("/");
	
	response.addCookie(tabCookie);
        ActionForward forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
        forward = new ActionForward(forward.getPath(), true);
        return forward;
    }
}
