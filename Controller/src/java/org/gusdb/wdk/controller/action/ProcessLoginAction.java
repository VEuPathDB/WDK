/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.File;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author: Jerric
 * @created: May 26, 2006
 * @modified by: Jerric
 * @modified at: May 26, 2006
 * 
 */
public class ProcessLoginAction extends Action {

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.struts.action.Action#execute(org.apache.struts.action.
     * ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // get the referer link and possibly an url to the client's original
        // page if user invoked a separate login form page.
        String referer = (String) request.getParameter(CConstants.WDK_REFERER_URL_KEY);
        if (referer == null) referer = request.getHeader("referer");
        String originUrl = request.getParameter(CConstants.WDK_ORIGIN_URL_KEY);

        ActionForward forward = new ActionForward();
        forward.setRedirect(true);
        String forwardUrl;

        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserFactoryBean factory = wdkModel.getUserFactory();

        // get the current user
        UserBean guest = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        // if guest is null, means the session is timed out, create the guest
        // again
        if (guest == null) {
            guest = factory.getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);
        }

        if (!guest.isGuest()) { // user has been logged in, redirect back

            if (originUrl != null) {
                forwardUrl = originUrl;
                request.getSession().setAttribute(
			CConstants.WDK_ORIGIN_URL_KEY, null);
            } else {
                forwardUrl = referer;
            }
        }
	else {
	    // get user's input
	    String email = request.getParameter(CConstants.WDK_EMAIL_KEY);
	    String password = request.getParameter(CConstants.WDK_PASSWORD_KEY);
	    boolean remember = request.getParameter("remember") != null
                && request.getParameter("remember").equals("on");

	    // If a front end action is specified in the url, set it in the current user
	    String frontAction = request.getParameter("action");
	    Integer frontStrategy = null;
	    try {
		frontStrategy = Integer.valueOf(request.getParameter("actionStrat"));
	    }
	    catch (NumberFormatException ex) {
	    }
	    Integer frontStep = null;
	    try {
		frontStep = Integer.valueOf(request.getParameter("actionStep"));
	    }
	    catch (NumberFormatException ex) {
	    }
	    
	    guest.setFrontAction(frontAction);
	    if (frontStrategy != null) {
		guest.setFrontStrategy(frontStrategy);
	    }
	    if (frontStep != null) {
		guest.setFrontStep(frontStep);
	    }
	    
	    // authenticate
	    try {
		UserBean user = factory.login(guest, email, password);
		// Create & send cookie
		Cookie loginCookie = new Cookie(CConstants.WDK_LOGIN_COOKIE_KEY,
						URLEncoder.encode(user.getEmail(), "utf-8"));
		
		if (remember) {
		    loginCookie.setMaxAge(java.lang.Integer.MAX_VALUE / 256);
		    loginCookie.setValue(loginCookie.getValue() + "-remember");
		} else {
		    loginCookie.setMaxAge(-1);
		}

		String secretValue = wdkModel.getSecretKey();
		secretValue = UserFactoryBean.md5(loginCookie.getValue() + secretValue);

		loginCookie.setValue(loginCookie.getValue() + "-"
				     + secretValue);

		// make sure the cookie is good for whole site, not just webapp
		loginCookie.setPath("/");

		response.addCookie(loginCookie);

		request.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
		request.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");

		if (originUrl != null) {
		    forwardUrl = originUrl;
		    request.getSession().setAttribute(
						      CConstants.WDK_ORIGIN_URL_KEY, null);
		} else {
		    forwardUrl = referer;
		}

		forward.setRedirect(true);
		// history ids don't show up in url anymore, so this isn't needed
		// login succeeded, redirect to "show_history page if history_id
		// contained in the url. since the history id is invalid/changed
		// after login
		// if (forwardUrl.indexOf(CConstants.WDK_HISTORY_ID_KEY) >= 0) {
		// forward =
		// mapping.findForward(CConstants.SHOW_QUERY_HISTORY_MAPKEY);
		// return forward;
		// }
	    } catch (WdkUserException ex) {
		ex.printStackTrace();
		// user authentication failed, set the error message
		request.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY,
						  ex.getMessage());
		// use session so attributes survive the redirect
		request.getSession().setAttribute(CConstants.WDK_REFERER_URL_KEY,
						  referer);
		request.getSession().setAttribute(CConstants.WDK_ORIGIN_URL_KEY,
						  request.getParameter(CConstants.WDK_ORIGIN_URL_KEY));

		ServletContext svltCtx = getServlet().getServletContext();
		String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR
		    + File.separator + CConstants.WDK_PAGES_DIR;
		String customViewFile = customViewDir + File.separator
		    + CConstants.WDK_LOGIN_PAGE;

		ActionForward loginPage =  null;
		if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
		    loginPage = new ActionForward(customViewFile);
		    loginPage.setRedirect(false);
		} else {
		    loginPage = mapping.findForward(CConstants.SHOW_LOGIN_MAPKEY);
		}
		forwardUrl = loginPage.getPath();
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw ex;
	    }
	}
        forward.setPath(forwardUrl);
        return forward;
    }
}
