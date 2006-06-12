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


/**
 * @author xingao
 *
 */
public class ProcessPasswordAction extends Action {

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
        // get the current user
        UserBean user = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        // fails if the current use is a guest
        if (user.getGuest()) throw new WdkUserException("You cannot changethe password as a guest.");

        // get the referer link
        String referer = (String) request.getParameter(CConstants.WDK_REFERER_URL_KEY);
        if (referer == null) referer = request.getHeader("referer");

        int index = referer.lastIndexOf("/");
        referer = referer.substring(index);
        ActionForward forward = new ActionForward(referer);
        forward.setRedirect(false);

        // get user's input
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            user.changePassword(oldPassword, newPassword, confirmPassword);
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
            // changing password succeed
            request.setAttribute("changePasswordSucceed", true);
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            // user authentication failed, set the error message
            request.setAttribute(CConstants.WDK_CHANGE_PASSWORD_ERROR_KEY,
                    ex.getMessage());
            request.setAttribute(CConstants.WDK_REFERER_URL_KEY, referer);
        }
        return forward;
    }
}
