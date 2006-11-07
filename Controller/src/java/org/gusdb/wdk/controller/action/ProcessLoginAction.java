/**
 * 
 */
package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
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
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
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

        if (!guest.getGuest()) { // user has been logged in, redirect back
            response.sendRedirect(response.encodeRedirectURL(request.getRequestURL().toString()));
        }

        // get user's input
        String email = request.getParameter(CConstants.WDK_EMAIL_KEY);
        String password = request.getParameter(CConstants.WDK_PASSWORD_KEY);

        // authenticate
        try {
            UserBean user = factory.login(guest, email, password);
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
            request.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY,
                    "");
            if (originUrl != null) {
                forwardUrl = originUrl;
                request.getSession().setAttribute(
                        CConstants.WDK_ORIGIN_URL_KEY, null);
            } else {
                forwardUrl = referer;
            }
            // login succeeded, redirect to "show_history page if history_id
            // contained in the url. since the history id is invalid/changed
            // after login
            if (forwardUrl.indexOf(CConstants.WDK_HISTORY_ID_KEY) >= 0) {
                forward = mapping.findForward(CConstants.SHOW_QUERY_HISTORY_MAPKEY);
                return forward;
            }
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
            forwardUrl = referer;
        }
        forward.setPath(forwardUrl);
        return forward;
    }
}
