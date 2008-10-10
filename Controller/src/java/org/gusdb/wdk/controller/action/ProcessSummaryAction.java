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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.user.User;

/**
 * @author Jerric
 * 
 */
public class ProcessSummaryAction extends Action {
    
    private static Logger logger = Logger.getLogger( ProcessSummaryAction.class );
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        // get user, or create one, if not exist
        WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY );
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser == null ) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
        }
        
        // get the query string
        String queryString = request.getQueryString();
        
        // get question
        String questionName = request.getParameter( CConstants.QUESTION_FULLNAME_PARAM );
        if ( questionName == null || questionName.length() == 0 ) {
            // for boolean questions only; get userAnswer id
            String userAnswerId = request.getParameter( CConstants.WDK_HISTORY_ID_KEY );
            if ( userAnswerId == null || userAnswerId.length() == 0 ) {
		// Check for strategy in here?  Sure, why not.
		String strategyId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
		String stepIx = request.getParameter(CConstants.WDK_STEP_IX_KEY);
		if (strategyId == null || strategyId.length() == 0) 
		    throw new WdkModelException("Missing parameters for this action" );

		StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strategyId));

		StepBean step;
		if (stepIx == null || stepIx.length() == 0)
		    step = strategy.getLatestStep();
		else 
		    step = strategy.getStep(Integer.parseInt(stepIx));
		
		userAnswerId = Integer.toString(step.getStepId());
	    }
            StepBean userAnswer = wdkUser.getStep( Integer.parseInt( userAnswerId ) );
            AnswerValueBean answer = userAnswer.getAnswerValue();
            questionName = answer.getQuestion().getFullName();
            String[ ] summaryAttributes = answer.getSummaryAttributeNames();
            wdkUser.applySummaryChecksum( questionName, summaryAttributes );
        }
        
        // get summary checksum, if have
        String summaryChecksum = request.getParameter( CConstants.WDK_SUMMARY_KEY );
        if ( summaryChecksum != null && summaryChecksum.length() > 0 ) {
            // apply the current summary to the question first, then do other
            // command
            String summaryKey = questionName + User.SUMMARY_ATTRIBUTES_SUFFIX;
            wdkUser.setProjectPreference( summaryKey, summaryChecksum );
        } else summaryChecksum = null;
        
        // get sorting checksum, if have
        String sortingChecksum = request.getParameter( CConstants.WDK_SORTING_KEY );
        if ( sortingChecksum != null && sortingChecksum.length() > 0 ) {
            // apply the current sorting to the question first, then do other
            // command
            wdkUser.applySortingChecksum( questionName, sortingChecksum );
        } else sortingChecksum = null;
        
        // get command
        String command = request.getParameter( CConstants.WDK_SUMMARY_COMMAND_KEY );
        if ( command != null ) {
            
            if ( command.equalsIgnoreCase( "sort" ) ) { // sorting
                String attributeName = request.getParameter( CConstants.WDK_SUMMARY_ATTRIBUTE_KEY );
                String sortingOrder = request.getParameter( CConstants.WDK_SUMMARY_SORTING_ORDER_KEY );
                
                boolean ascending = !sortingOrder.equalsIgnoreCase( "DESC" );
                String checksum = wdkUser.addSortingAttribute( questionName,
                        attributeName, ascending );
                
                // add/replace sorting key
                String sortingParam = "&" + CConstants.WDK_SORTING_KEY + "="
                        + checksum;
                if ( sortingChecksum == null ) {
                    queryString += sortingParam;
                } else {
                    queryString = queryString.replaceAll( "&"
                            + CConstants.WDK_SORTING_KEY + "=[^&]*",
                            sortingParam );
                }
            } else { // summary modification
                if ( command.equalsIgnoreCase( "reset" ) ) {
                    wdkUser.resetSummaryAttribute( questionName );
                    // remove summary key from query string
                    queryString = queryString.replaceAll( "&"
                            + CConstants.WDK_SUMMARY_KEY + "=[^&]*", "" );
                } else {
                    String attributeName = request.getParameter( CConstants.WDK_SUMMARY_ATTRIBUTE_KEY );
                    String checksum;
                    if ( command.equalsIgnoreCase( "add" ) ) {
                        checksum = wdkUser.addSummaryAttribute( questionName,
                                attributeName );
                    } else if ( command.equalsIgnoreCase( "remove" ) ) {
                        checksum = wdkUser.removeSummaryAttribute(
                                questionName, attributeName );
                    } else if ( command.equalsIgnoreCase( "arrange" ) ) {
                        String arrangeOrder = request.getParameter( CConstants.WDK_SUMMARY_ARRANGE_ORDER_KEY );
                        
                        boolean moveLeft = arrangeOrder.equalsIgnoreCase( "true" );
                        checksum = wdkUser.arrangeSummaryAttribute(
                                questionName, attributeName, moveLeft );
                    } else {
                        throw new WdkModelException( "Unknown command: "
                                + command );
                    }
                    // add/replace summary key
                    String summaryParam = "&" + CConstants.WDK_SUMMARY_KEY
                            + "=" + checksum;
                    if ( summaryChecksum == null ) {
                        queryString += summaryParam;
                    } else {
                        queryString = queryString.replaceAll( "&"
                                + CConstants.WDK_SUMMARY_KEY + "=[^&]*",
                                summaryParam );
                    }
                }
            }
            
            wdkUser.save();
        }
        
        // remove unneeded parameters from the url
        queryString = queryString.replaceAll( "&"
                + CConstants.WDK_SUMMARY_COMMAND_KEY + "=[^&]*", "" );
        queryString = queryString.replaceAll( "&"
                + CConstants.WDK_SUMMARY_ATTRIBUTE_KEY + "=[^&]*", "" );
        queryString = queryString.replaceAll( "&"
                + CConstants.WDK_SUMMARY_ARRANGE_ORDER_KEY + "=[^&]*", "" );
        queryString = queryString.replaceAll( "&"
                + CConstants.WDK_SUMMARY_SORTING_ORDER_KEY + "=[^&]*", "" );
        
        // construct url to show summary action
        ActionForward showSummary = mapping.findForward( CConstants.PQ_SHOW_SUMMARY_MAPKEY );
        StringBuffer url = new StringBuffer( showSummary.getPath() );
        url.append( "?" );
        url.append( queryString );
        
        ActionForward forward = new ActionForward( url.toString() );
        forward.setRedirect( true );
        return forward;
    }
}
