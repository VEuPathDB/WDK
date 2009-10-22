package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONObject;

/**
 * This Action loads the application pane with no URL arguments, so that
 * multiple strategies can be loaded by the UI
 */
public class ShowApplicationAction extends ShowSummaryAction {
    private static Logger logger = Logger.getLogger(ShowApplicationAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowApplicationAction...");

        try {
            // get user, or create one, if not exist
            WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                    CConstants.WDK_MODEL_KEY);
            UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY);
            if (wdkUser == null) {
                wdkUser = wdkModel.getUserFactory().getGuestUser();
                request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                        wdkUser);
            }

            String newStratKey = CConstants.WDK_NEW_STRATEGY_KEY;
            HttpSession session = request.getSession();
            if (session.getAttribute(newStratKey) != null) {
                boolean newStrategy = (Boolean)session.getAttribute(newStratKey);
                session.removeAttribute(newStratKey);
                request.setAttribute(newStratKey, newStrategy);
            }

            JSONObject jsMessage = new JSONObject();
            ShowStrategyAction.outputState(wdkUser, jsMessage);
            JSONObject jsState = jsMessage.getJSONObject("state");
            String activeStrategies = jsState.toString();
            request.setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY,
                    activeStrategies);

            /*
             * Charles Treatman 6/5/09 Add code here to set the
             * current_application_tab cookie so that user will go to the Browse
             * Strategies tab if no strats are opened.
             */
            StrategyBean[] openedStrategies = wdkUser.getActiveStrategies();
            if (openedStrategies.length == 0) {
                Cookie tabCookie = new Cookie("current_application_tab",
                        "search_history");
                // make sure it's only a session cookie, not persistent
                tabCookie.setMaxAge(-1);
                // make sure the cookie is good for whole site, not just webapp
                tabCookie.setPath("/");
                response.addCookie(tabCookie);
            }

	    String strategyViewFile = CConstants.WDK_CUSTOM_VIEW_DIR
		+ File.separator + CConstants.WDK_PAGES_DIR
		+ File.separator + CConstants.WDK_STRATEGY_DIR
		+ File.separator + CConstants.WDK_STRATEGY_PAGE;

            ActionForward forward = new ActionForward(strategyViewFile);

            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
