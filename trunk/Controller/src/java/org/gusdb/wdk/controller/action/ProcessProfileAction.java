/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.Set;

import javax.servlet.ServletContext;
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

/**
 * @author xingao
 * 
 */
public class ProcessProfileAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // get the current user
        UserBean user = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);

        // if a custom profile page exists, use it; otherwise, use default one
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR
	    + File.separator + CConstants.WDK_PAGES_DIR;
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_PROFILE_PAGE;
        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile);
            forward.setRedirect(false);
        } else {
            forward = mapping.findForward(CConstants.SHOW_PROFILE_MAPKEY);
        }
        // fails if the current use is a guest
        if (user != null && !user.isGuest()) {

            // clear the preference
            user.clearPreferences();

            Set<?> params = request.getParameterMap().keySet();
            for (Object param : params) {
                String paramName = (String) param;
                if (paramName.equalsIgnoreCase("email")) {
                    user.setEmail(request.getParameter("email"));
                } else if (paramName.equalsIgnoreCase("firstName")) {
                    user.setFirstName(request.getParameter("firstName"));
                } else if (paramName.equalsIgnoreCase("lastName")) {
                    user.setLastName(request.getParameter("lastName"));
                } else if (paramName.equalsIgnoreCase("middleName")) {
                    user.setMiddleName(request.getParameter("middleName"));
                } else if (paramName.equalsIgnoreCase("title")) {
                    user.setTitle(request.getParameter("title"));
                } else if (paramName.equalsIgnoreCase("organization")) {
                    user.setOrganization(request.getParameter("organization"));
                } else if (paramName.equalsIgnoreCase("department")) {
                    user.setDepartment(request.getParameter("department"));
                } else if (paramName.equalsIgnoreCase("address")) {
                    user.setAddress(request.getParameter("address"));
                } else if (paramName.equalsIgnoreCase("city")) {
                    user.setCity(request.getParameter("city"));
                } else if (paramName.equalsIgnoreCase("state")) {
                    user.setState(request.getParameter("state"));
                } else if (paramName.equalsIgnoreCase("zipCode")) {
                    user.setZipCode(request.getParameter("zipCode"));
                } else if (paramName.equalsIgnoreCase("phoneNumber")) {
                    user.setPhoneNumber(request.getParameter("phoneNumber"));
                } else if (paramName.equalsIgnoreCase("country")) {
                    user.setCountry(request.getParameter("country"));
                } else if (paramName.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
                    String paramValue = request.getParameter(paramName);
                    user.setGlobalPreference(paramName, paramValue);
                } else if (paramName.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
                    String paramValue = request.getParameter(paramName);
                    user.setProjectPreference(paramName, paramValue);
                }
            }

            // update and save the user with user input
            try {
                user.save();
                // Update profile succeed
                request.setAttribute("profileSucceed", true);
            } catch (WdkUserException ex) {
                // email exists, notify the user to input again
                request.setAttribute(CConstants.WDK_PROFILE_ERROR_KEY,
                        ex.getMessage());
            }
        }

        return forward;
    }
}
