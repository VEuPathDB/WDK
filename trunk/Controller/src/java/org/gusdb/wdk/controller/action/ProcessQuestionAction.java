package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ProcessQuestionAction extends ShowQuestionAction {
    
    private static final Logger logger = Logger.getLogger( ProcessQuestionAction.class );
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        ActionForward forward = null;
        String submitAction = request.getParameter( CConstants.PQ_SUBMIT_KEY );
        logger.info( "submitAction=" + submitAction );
        if ( submitAction == null
                || submitAction.equals( CConstants.PQ_SUBMIT_GET_ANSWER ) ) {
            String qFullName = request.getParameter( CConstants.QUESTION_FULLNAME_PARAM );
            QuestionBean wdkQuestion = getQuestionByFullName( qFullName );
            boolean fromQS = "1".equals( request.getParameter( CConstants.FROM_QUESTIONSET_PARAM ) );
            if ( !fromQS ) {
                QuestionForm qForm = prepareQuestionForm( wdkQuestion, request,
                        ( QuestionForm ) form );
                // QuestionForm qForm = (QuestionForm)form;
                request.setAttribute( "parentURI", request.getRequestURI() );
                
                // convert/handle the dataset params
                handleDatasetParams( request, wdkQuestion, qForm );
            }
            /*
             * else { QuestionForm qForm = (QuestionForm)form;
             * qForm.setQuestion(wdkQuestion); qForm.cleanup(); }
             */
            forward = mapping.findForward( CConstants.PQ_SHOW_SUMMARY_MAPKEY );
        } else if ( submitAction.equals( CConstants.PQ_SUBMIT_EXPAND_QUERY ) ) {
            forward = mapping.findForward( CConstants.PQ_START_BOOLEAN_MAPKEY );
        }
        return forward;
    }
    
    private void handleDatasetParams( HttpServletRequest request,
            QuestionBean wdkQuestion, QuestionForm form )
            throws WdkModelException, WdkUserException {
        
        Map< String, Object > params = form.getMyProps();
        Map< String, Object > paramObjects = form.getMyPropObjects();
        
        try {
            UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY );
            if ( wdkUser == null ) {
                WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(
                        CConstants.WDK_MODEL_KEY );
                wdkUser = wdkModel.getUserFactory().getGuestUser();
                request.getSession().setAttribute( CConstants.WDK_USER_KEY,
                        wdkUser );
            }
            
            // check dataset params
            ParamBean[ ] paramDefinitions = wdkQuestion.getParams();
            for ( ParamBean param : paramDefinitions ) {
                if ( param instanceof DatasetParamBean ) {
                    String paramName = param.getName();
                    // get the input type
                    String type = request.getParameter( paramName + "_type" );
                    
                    if ( type.equalsIgnoreCase( "dataset" ) ) {
                        // do nothing, since the value is already a compound
                    } else if ( type.equalsIgnoreCase( "data" ) ) {
                        String data = request.getParameter( paramName + "_data" );
                        data = data.replaceAll( "[,;]+", " " ).trim();
                        String[ ] values = data.split( "\\s+" );
                        DatasetBean dataset = wdkUser.createDataset( "", values );
                        params.put( paramName, wdkUser.getSignature() + ":"
                                + dataset.getDatasetId() );
                    } else if ( type.equalsIgnoreCase( "file" ) ) {
                        FormFile file = ( FormFile ) paramObjects.get( paramName
                                + "_file" );
                        
                        // TEST
                        // logger.info("FILE: " + file.getFileName() + " ("
                        // + file.getFileSize() + ")");
                        
                        String[ ] values = new String( file.getFileData() ).split( "[,\t\n]+" );
                        DatasetBean dataset = wdkUser.createDataset(
                                file.getFileName(), values );
                        params.put( paramName, wdkUser.getSignature() + ":"
                                + dataset.getDatasetId() );
                    } else {
                        throw new WdkModelException( "Invalid input type for "
                                + "Dataset " + paramName + ": " + type );
                    }
                }
            }
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        }
    }
}
