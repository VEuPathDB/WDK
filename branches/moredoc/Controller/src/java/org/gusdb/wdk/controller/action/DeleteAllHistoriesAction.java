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
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * 
 */
public class DeleteAllHistoriesAction extends Action {
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser != null ) {
            // check if only need to delete invalid histories
            String invalid = request.getParameter( "invalid" );
            if ( invalid == null || !invalid.equalsIgnoreCase( "true" ) ) {
                wdkUser.deleteHistories();
            }
            else wdkUser.deleteInvalidHistories();
        }
        
        ActionForward forward = mapping.findForward( CConstants.DELETE_HISTORY_MAPKEY );
        
        return forward;
    }
    
}
