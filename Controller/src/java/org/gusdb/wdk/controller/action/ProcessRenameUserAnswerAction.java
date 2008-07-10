/**
 * 
 */
package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * 
 */
public class ProcessRenameUserAnswerAction extends Action {
    
    private static Logger logger = Logger.getLogger( ProcessRenameUserAnswerAction.class );
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        
        String histIdstr = request.getParameter( CConstants.WDK_HISTORY_ID_KEY );
        String customName = request.getParameter( CConstants.WDK_HISTORY_CUSTOM_NAME_KEY );
        
        // TEST
        logger.info( "Set custom name: '" + customName + "'" );
        if ( histIdstr != null ) {
            int histId = Integer.parseInt( histIdstr );
            UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY );
            try {
                UserAnswerBean userAnswer = wdkUser.getUserAnswer( histId );
                userAnswer.setCustomName( customName );
                userAnswer.update( false );
            } catch ( Exception e ) {
                e.printStackTrace();
                // prevent refresh of page after delete from breaking
            }
        } else {
            throw new Exception( "no user userAnswer id is given for update" );
        }
        
        // ActionForward forward =
        // mapping.findForward(CConstants.RENAME_HISTORY_MAPKEY);
        
        // get the referer link and possibly an url to the client's original
        // page if user invoked a separate login form page.
        String referer = ( String ) request.getParameter( CConstants.WDK_REFERER_URL_KEY );
        if ( referer == null ) referer = request.getHeader( "referer" );
        String originUrl = request.getParameter( CConstants.WDK_ORIGIN_URL_KEY );
        
        ActionForward forward = new ActionForward();
        forward.setRedirect( true );
        String forwardUrl;
        if ( originUrl != null ) {
            forwardUrl = originUrl;
            request.getSession().setAttribute( CConstants.WDK_ORIGIN_URL_KEY,
                    null );
        } else {
            forwardUrl = referer;
        }
        forward.setPath( forwardUrl );
        
        return forward;
    }
    
}
