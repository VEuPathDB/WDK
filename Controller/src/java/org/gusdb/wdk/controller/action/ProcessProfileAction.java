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
public class ProcessProfileAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // get the current user
        UserBean user = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        // fails if the current use is a guest
        if (user.getGuest()) throw new WdkUserException("Please login first before you change your profile.");

        // redirect to the profile page
        // get the referer link
        String referer = (String) request.getParameter(CConstants.WDK_REFERER_URL_KEY);
        if (referer == null) referer = request.getHeader("referer");

        int index = referer.lastIndexOf("/");
        referer = referer.substring(index);
        ActionForward forward = new ActionForward(referer);
        forward.setRedirect(false);

        user.setFirstName(request.getParameter("firstName"));
        user.setLastName(request.getParameter("lastName"));
        user.setMiddleName( request.getParameter("middleName"));
        user.setTitle(request.getParameter("title"));
        user.setOrganization(request.getParameter("organization"));
        user.setDepartment(request.getParameter("department"));
        user.setAddress(request.getParameter("address"));
        user.setCity(request.getParameter("city"));
        user.setState(request.getParameter("state"));
        user.setZipCode(request.getParameter("zipCode"));
        user.setPhoneNumber(request.getParameter("phoneNumber"));
        user.setCountry(request.getParameter("country"));

        // update and save the user with user input
        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserFactoryBean factory = wdkModel.getUserFactory();
        try {
            factory.saveUser(user);
            // Update profile succeed
            request.setAttribute("profileSucceed", true);
        } catch (WdkUserException ex) {
            // email exists, notify the user to input again
            request.setAttribute(CConstants.WDK_PROFILE_ERROR_KEY,
                    ex.getMessage());
            request.setAttribute(CConstants.WDK_REFERER_URL_KEY, referer);
        }
        return forward;
    }
}
