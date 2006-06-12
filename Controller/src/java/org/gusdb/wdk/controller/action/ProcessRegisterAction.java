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
 * @author xingao
 * 
 */
public class ProcessRegisterAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // redirect to the registration page
        // get the referer link
        String referer = (String) request.getParameter(CConstants.WDK_REFERER_URL_KEY);
        if (referer == null) referer = request.getHeader("referer");

        int index = referer.lastIndexOf("/");
        referer = referer.substring(index);
        ActionForward forward = new ActionForward(referer);
        forward.setRedirect(false);

        String email = request.getParameter(CConstants.WDK_EMAIL_KEY);
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String middleName = request.getParameter("middleName");
        String title = request.getParameter("title");
        String organization = request.getParameter("organization");
        String department = request.getParameter("department");
        String address = request.getParameter("address");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String zipCode = request.getParameter("zipCode");
        String phoneNumber = request.getParameter("phoneNumber");
        String country = request.getParameter("country");

        // create the user with user input
        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserFactoryBean factory = wdkModel.getUserFactory();
        try {
            UserBean user = factory.createUser(email, lastName, firstName,
                    middleName, title, organization, department, address, city,
                    state, zipCode, phoneNumber, country);
            // registration succeed
            request.setAttribute("registerSucceed", true);
        } catch (WdkUserException ex) {
            // email exists, notify the user to input again
            request.setAttribute(CConstants.WDK_REGISTER_ERROR_KEY,
                    ex.getMessage());
            request.setAttribute(CConstants.WDK_REFERER_URL_KEY, referer);
            
            // push back the user input, so that the user doesn't need to type again
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
        }
        return forward;
    }
}
