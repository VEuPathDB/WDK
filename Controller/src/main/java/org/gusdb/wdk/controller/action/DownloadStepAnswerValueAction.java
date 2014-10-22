package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.Map;

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
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is process the download of AnswerValues on queryStep.jsp page.
 * 
 */

public class DownloadStepAnswerValueAction extends Action {
    
    private static final Logger logger = Logger.getLogger( DownloadStepAnswerValueAction.class );

    @Override
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        StepBean step = getStep(request);
        
        // get a list of supported reporters
        RecordClassBean recordClass = step.getQuestion().getRecordClass();
        String fullName = recordClass.getFullName();
        Map< String, String > reporters = recordClass.getReporters();
        
        // get the current selected reporter
        String reporter = request.getParameter( CConstants.WDK_REPORT_FORMAT_KEY );
        if ( reporter != null && reporter.trim().length() == 0 )
            reporter = null;
        
        request.setAttribute( CConstants.WDK_REPORT_FORMATS_KEY, reporters );
        if ( reporter != null ) {
            request.setAttribute( CConstants.WDK_REPORT_FORMAT_KEY, reporter );
        }
        request.setAttribute( CConstants.WDK_STEP_KEY, step );
        
        // get forward
        ActionForward forward;
        
        if ( reporter == null ) {
            // get the default configuration page
	    String defaultViewFile = CConstants.WDK_CUSTOM_VIEW_DIR
		+ File.separator + CConstants.WDK_PAGES_DIR
		+ File.separator + CConstants.WDK_DOWNLOAD_CONFIG_PAGE;
            forward = new ActionForward(defaultViewFile);
        } else {
            ServletContext svltCtx = getServlet().getServletContext();

	    String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR
		+ File.separator + CConstants.WDK_PAGES_DIR
		+ File.separator + CConstants.WDK_REPORTERS_DIR;

            String customViewFile1 = customViewDir + File.separator + fullName
                    + "." + reporter + "ReporterConfig.jsp";
            String customViewFile2 = customViewDir + File.separator + reporter
                    + "ReporterConfig.jsp";
            
            if ( ApplicationInitListener.resourceExists( customViewFile1,
                    svltCtx ) ) {
                forward = new ActionForward( customViewFile1 );
            } else if ( ApplicationInitListener.resourceExists(
                    customViewFile2, svltCtx ) ) {
                forward = new ActionForward( customViewFile2 );
            } else {
                throw new WdkModelException( "No configuration form can be "
                        + "found for the selected format: " + reporter );
            }
        }
        logger.info( "The download config: " + forward.getPath() );
        
        return forward;
    }
    
    protected StepBean getStep( HttpServletRequest request )
            throws WdkUserException, WdkModelException {
        String stepIdstr = request.getParameter( "step_id" );
        if ( stepIdstr == null ) {
            stepIdstr = ( String ) request.getAttribute( "step_id" );
        }
        if ( stepIdstr != null ) {
          int stepId;
          try{
            stepId = Integer.parseInt( stepIdstr );
          } catch(NumberFormatException ex) {
            throw new WdkUserException("The step id is invalid: " + stepIdstr);
          }
            request.setAttribute( "step_id", stepId );
            request.setAttribute( "history_id", stepId );

            // check if we need to get user by signature
            String signature = request.getParameter("signature");
            UserBean wdkUser;
            if (signature != null && signature.length() > 0) {
                WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
                wdkUser = wdkModel.getUserFactory().getUser(signature);
            } else {
                wdkUser = ActionUtility.getUser(servlet, request);
            }
            // also set the user as the attribute of the request, so that the jsp can use it.
            request.setAttribute("wdkUser", wdkUser);
            
            StepBean step = wdkUser.getStep( stepId );
            
            // get new result count, in case the count may have been changed
            int size = step.getResultSize();
            logger.debug("step size: " + size);
            
            return step;
        } else {
            throw new WdkUserException(
                    "no step id is given for which to download the result" );
        }
    }
}
