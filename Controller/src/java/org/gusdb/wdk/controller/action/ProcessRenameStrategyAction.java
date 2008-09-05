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
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * 
 */
public class ProcessRenameStrategyAction extends Action {
    
    private static Logger logger = Logger.getLogger( ProcessRenameStrategyAction.class );
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        
        String stratIdstr = request.getParameter( "strategy" );
        String customName = request.getParameter( "name" );
        
        // TEST
        logger.info( "Set custom name: '" + customName + "'" );
        if ( stratIdstr != null ) {
            int stratId = Integer.parseInt( stratIdstr );
            UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY );
            try {
                StrategyBean strategy = wdkUser.getStrategy( stratId );
                strategy.setName( customName );
		
                strategy.update(true);
            } catch ( Exception e ) {
                e.printStackTrace();
                // prevent refresh of page after delete from breaking
            }
        } else {
            throw new Exception( "no user history id is given for update" );
        }
        
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
