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
public class ProcessResetPasswordAction extends Action {

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
        // get the referer link
        String referer = (String) request.getParameter(CConstants.WDK_REFERER_URL_KEY);
        if (referer == null) referer = request.getHeader("referer");

        int index = referer.lastIndexOf("/");
        referer = referer.substring(index);
        ActionForward forward = new ActionForward(referer);
        forward.setRedirect(false);

        // get user's input
        String email = request.getParameter("email");

        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserFactoryBean factory = wdkModel.getUserFactory();
        try {
            UserBean user = factory.loadUser(email);
            factory.resetPassword(user);
            // resetting password succeed
            request.setAttribute("resetPasswordSucceed", true);
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            // resetting password failed, set the error message
            request.setAttribute(CConstants.WDK_RESET_PASSWORD_ERROR_KEY,
                    ex.getMessage());
            request.setAttribute(CConstants.WDK_REFERER_URL_KEY, referer);
        }
        return forward;
    }

}
