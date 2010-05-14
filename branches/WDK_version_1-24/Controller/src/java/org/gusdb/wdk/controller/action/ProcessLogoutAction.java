/**
 * 
 */
package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author xingao
 * 
 */
public class ProcessLogoutAction extends Action {

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
        ActionForward forward = mapping.findForward(CConstants.PROCESS_LOGOUT_MAPKEY);
        
        // clear the session, and reset the default user to guest
        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserFactoryBean factory = wdkModel.getUserFactory();
        UserBean guest = factory.getGuestUser();
        request.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);

	// Find api_login_cookie, set it to expire immediately
	Cookie[] requestCookies = request.getCookies();

	int numCookies = requestCookies.length;

	for (int i = 0; i < numCookies; ++i) {
	    //System.out.println("checking request cookies in logout action...");
	    //System.out.println("cookie" + (i+1) + ": " + requestCookies[i].getName());
	    if (requestCookies[i].getName().equals(CConstants.WDK_LOGIN_COOKIE_KEY)) {
		requestCookies[i].setMaxAge(0);
		requestCookies[i].setPath("/");
		//System.out.println("login cookie age:" + requestCookies[i].getMaxAge());
		//System.out.println("login cookie path:" + requestCookies[i].getPath());
		response.addCookie(requestCookies[i]);
		break;
	    }
	}

        return forward;
    }
}
