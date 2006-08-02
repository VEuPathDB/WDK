/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.io.File;

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
public class ShowProfileAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // get the current user
        UserBean user = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        // fails if the current use is a guest
        if (user.getGuest())
            throw new WdkUserException(
                    "Please login first before you change your profile.");
        
        // add global & project preferences to the form
        ShowProfileForm profileForm = (ShowProfileForm) form;
        profileForm.addGlobalPreferences(user.getGlobalPreferences());
        profileForm.addProjectPreferences(user.getProjectPreferences());

        // if a custom profile page exists, use it; otherwise, use default one
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_PROFILE_PAGE;
        ActionForward forward = null;
        
        
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile);
            forward.setRedirect(true);
        } else {
            forward = mapping.findForward(CConstants.SHOW_PROFILE_MAPKEY);
        }

        return forward;
    }
}
