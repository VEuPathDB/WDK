/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author xingao
 * 
 */
public class ProcessRegisterAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // if a custom register page exists, use it; otherwise, use default one
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR
	    + File.separator + CConstants.WDK_PAGES_DIR;
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_REGISTER_PAGE;
        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile);
            forward.setRedirect(false);
        } else {
            forward = mapping.findForward(CConstants.SHOW_REGISTER_MAPKEY);
        }

        String email = null, firstName = null, lastName = null, middleName = null, title = null, organization = null, department = null, address = null, city = null, state = null, zipCode = null, phoneNumber = null, country = null;
        Map<String, String> globalPreferences = new LinkedHashMap<String, String>();
        Map<String, String> projectPreferences = new LinkedHashMap<String, String>();

        Set<?> params = request.getParameterMap().keySet();
        for (Object param : params) {
            String paramName = (String) param;
            if (paramName.equals(CConstants.WDK_EMAIL_KEY)) {
                email = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("firstName")) {
                firstName = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("lastName")) {
                lastName = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("middleName")) {
                middleName = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("title")) {
                title = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("organization")) {
                organization = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("department")) {
                department = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("address")) {
                address = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("city")) {
                city = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("state")) {
                state = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("zipCode")) {
                zipCode = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("phoneNumber")) {
                phoneNumber = request.getParameter(paramName);
            } else if (paramName.equalsIgnoreCase("country")) {
                country = request.getParameter(paramName);
            } else if (paramName.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
                String paramValue = request.getParameter(paramName);
                globalPreferences.put(paramName, paramValue);
            } else if (paramName.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
                String paramValue = request.getParameter(paramName);
                projectPreferences.put(paramName, paramValue);
            }
        }

        if (email != null && email.length() != 0) {
            // create the user with user input
            WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                    CConstants.WDK_MODEL_KEY);
            UserFactoryBean factory = wdkModel.getUserFactory();
            try {
                /*UserBean user = */
                factory.createUser(email, lastName, firstName,
                        middleName, title, organization, department, address,
                        city, state, zipCode, phoneNumber, country,
                        globalPreferences, projectPreferences);
                // registration succeed
                request.setAttribute("registerSucceed", true);
            } catch (WdkUserException ex) {
                // email exists, notify the user to input again
                request.setAttribute(CConstants.WDK_REGISTER_ERROR_KEY,
                        ex.getMessage());

                // push back the user input, so that the user doesn't need to
                // type again
                request.setAttribute(CConstants.WDK_EMAIL_KEY, email);
                request.setAttribute("firstName", firstName);
                request.setAttribute("lastName", lastName);
                request.setAttribute("middleName", middleName);
                request.setAttribute("title", title);
                request.setAttribute("organization", organization);
                request.setAttribute("department", department);
                request.setAttribute("address", address);
                request.setAttribute("city", city);
                request.setAttribute("state", state);
                request.setAttribute("zipCode", zipCode);
                request.setAttribute("phoneNumber", phoneNumber);
                request.setAttribute("country", country);
                for (String param : projectPreferences.keySet()) {
                    request.setAttribute(param, projectPreferences.get(param));
                }
                for (String param : globalPreferences.keySet()) {
                    request.setAttribute(param, globalPreferences.get(param));
                }
            }
        }
        return forward;
    }
}
