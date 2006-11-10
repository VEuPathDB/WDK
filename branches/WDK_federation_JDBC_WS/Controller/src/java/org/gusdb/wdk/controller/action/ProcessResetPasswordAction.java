/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
 * @author xingao
 *
 */
public class ProcessResetPasswordAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessResetPasswordAction.class);
    
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
        // if a custom profile page exists, use it; otherwise, use default one
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_RESET_PASSWORD_PAGE;
        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile);
            forward.setRedirect(false);
        } else {
            forward = mapping.findForward(CConstants.SHOW_RESET_PASSWORD_MAPKEY);
        }

        // get user's input
        String email = request.getParameter("email");
        
        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserFactoryBean factory = wdkModel.getUserFactory();
        try {
            if (email == null || email.length() == 0)
                throw new WdkUserException("Please provide a valid email.");
            UserBean user = factory.loadUser(email);
            factory.resetPassword(user);
            // resetting password succeed
            request.setAttribute("resetPasswordSucceed", true);
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            // resetting password failed, set the error message
            request.setAttribute(CConstants.WDK_RESET_PASSWORD_ERROR_KEY,
                    ex.getMessage());
        }
        return forward;
    }

}
