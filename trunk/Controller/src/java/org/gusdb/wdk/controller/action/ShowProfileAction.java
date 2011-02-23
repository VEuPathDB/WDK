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

/**
 * @author xingao
 * 
 */
public class ShowProfileAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
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

        return forward;
    }
}
