/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.ArrayList;
import java.net.URLEncoder;

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
 * @author ctreatma
 * 
 */
public class ProcessRenameStrategyAction extends Action {
    
    private static Logger logger = Logger.getLogger( ProcessRenameStrategyAction.class );
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        
        String strStratId = request.getParameter( CConstants.WDK_STRATEGY_ID_KEY );
        String customName = request.getParameter( "name" );
        
        // TEST
	if (customName == null || customName.length() == 0) {
	    throw new Exception("No name was given for saving Strategy.");
	}
        logger.info( "Set custom name: '" + customName + "'" );
        if ( strStratId == null || strStratId.length() == 0) {
            throw new Exception( "No Strategy was given for saving" );
        }

	int stratId = Integer.parseInt( strStratId );
	UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(CConstants.WDK_USER_KEY );
	
	StrategyBean strategy = wdkUser.getStrategy( stratId );
	strategy.setName( customName );
	strategy.setIsSaved(true);
	
	ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();
	int index = -1;
	
	if (activeStrategies != null && activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
	    index = activeStrategies.indexOf(new Integer(strategy.getStrategyId()));
	    activeStrategies.remove(index);
	}
	
	strategy.update(true);
	
	if (activeStrategies != null && index >= 0) {
	    activeStrategies.add(index, new Integer(strategy.getStrategyId()));
	}
	wdkUser.setActiveStrategies(activeStrategies);

	request.setAttribute(CConstants.WDK_STEP_KEY, strategy.getLatestStep());
  	request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

	// forward to strategyPage.jsp
	ActionForward showSummary = mapping.findForward( CConstants.SHOW_STRATEGY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?strategy=" + URLEncoder.encode(Integer.toString(strategy.getStrategyId())));
	String viewStep = request.getParameter("step");
	if (viewStep != null && viewStep.length() != 0) {
	    url.append("&step=" + URLEncoder.encode(viewStep));
	}
	String subQuery = request.getParameter("subquery");
	if (subQuery != null && subQuery.length() != 0) {
	    url.append("&subquery=" + URLEncoder.encode(subQuery));
	}
	ActionForward forward = new ActionForward( url.toString() );
	forward.setRedirect( false );
	return forward;

	/*
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
	*/
    }
    
}
