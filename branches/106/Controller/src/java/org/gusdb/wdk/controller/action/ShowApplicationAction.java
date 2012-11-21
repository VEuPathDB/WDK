package org.gusdb.wdk.controller.action;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONObject;

/**
 * This Action loads the application pane with no URL arguments, so that
 * multiple strategies can be loaded by the UI
 */
public class ShowApplicationAction extends ShowSummaryAction {

    private static final Logger logger = Logger.getLogger(ShowApplicationAction.class);

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

            // get the tab info, if present
//            String tab = request.getParameter(PARAM_TAB);
//            if (tab != null && tab.length() > 0) {
//
//            }

	    String strategyViewFile = CConstants.WDK_CUSTOM_VIEW_DIR
		+ File.separator + CConstants.WDK_PAGES_DIR
		//+ File.separator + CConstants.WDK_STRATEGY_DIR
		+ File.separator + CConstants.WDK_STRATEGY_PAGE;

            ActionForward forward = new ActionForward(strategyViewFile);

            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    protected static void setWdkTabStateCookie(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    String cookieValue = null;
	    Cookie[] cookies = request.getCookies();
        if (cookies == null) return;
	    for (Cookie cookie : cookies) {
		if (cookie.getName().compareTo(CConstants.WDK_TAB_STATE_COOKIE) == 0) {
		    cookieValue = URLDecoder.decode(cookie.getValue(), "utf-8");
		    break;
		}
	    }
	    if (cookieValue == null || cookieValue.trim().length() == 0) {
		cookieValue = "application=strategy_results";
	    }
	    else {
		String[] tabs = cookieValue.split("&");
		StringBuilder newValue = new StringBuilder();
		for (String tab : tabs) {
		    if (tab.startsWith("application=")) {
			tab = "application=strategy_results";
		    }
		    newValue.append("&" + tab);
		}
		cookieValue = newValue.toString();
	    }
            Cookie tabCookie = new Cookie(CConstants.WDK_TAB_STATE_COOKIE,
                    URLEncoder.encode(cookieValue, "utf-8"));
            // make sure it's only a session cookie, not persistent
            tabCookie.setMaxAge(-1);
            // make sure the cookie is good for whole site, not just webapp
            tabCookie.setPath("/");

            response.addCookie(tabCookie);
    }
}
