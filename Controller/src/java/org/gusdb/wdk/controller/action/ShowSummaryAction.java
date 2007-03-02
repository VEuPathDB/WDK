package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a WDK question is asked. It
 * 1) reads param values from input form bean, 2) runs the query and saves the
 * answer 3) forwards control to a jsp page that displays a summary
 */

public class ShowSummaryAction extends ShowQuestionAction {
    
    private static Logger logger = Logger.getLogger( ShowSummaryAction.class );
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        AnswerBean wdkAnswer = null;
        HistoryBean history = null;
        
        // get user, or create one, if not exist
        WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY );
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser == null ) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
        }
        
        // get sorting info
        String sortingAttr = request.getParameter( CConstants.WDK_SORTING_ATTRIBUTE_KEY );
        if ( sortingAttr != null ) {
            String questionName = request.getParameter( CConstants.WDK_SORTING_QUESTION_KEY );
            String sortingOrder = request.getParameter( CConstants.WDK_SORTING_ORDER_KEY );
            boolean ascending = !sortingOrder.equalsIgnoreCase( "DESC" );
            wdkUser.addSortingAttribute( questionName, sortingAttr, ascending );
        }
        
        String strHistId = request.getParameter( CConstants.WDK_HISTORY_ID_KEY );
        if ( strHistId == null ) {
            strHistId = ( String ) request.getAttribute( CConstants.WDK_HISTORY_ID_KEY );
        }
        if ( strHistId != null ) { // use existing history
            int historyId = Integer.parseInt( strHistId );
            history = wdkUser.getHistory( historyId );
            wdkAnswer = history.getAnswer();
            // update the estimate size, in case the database has changed
            history.setEstimateSize( wdkAnswer.getResultSize() );
            history.update();
            wdkAnswer = summaryPaging( request, null, null, wdkAnswer );
        } else {
            QuestionForm qForm = ( QuestionForm ) form;
            // TRICKY: this is for action forward from
            // ProcessQuestionSetsFlatAction
            qForm.cleanup();
            
            QuestionBean wdkQuestion = ( QuestionBean ) request.getAttribute( CConstants.WDK_QUESTION_KEY );
            if ( wdkQuestion == null ) {
                wdkQuestion = qForm.getQuestion();
            }
            if ( wdkQuestion == null ) {
                String qFullName = request.getParameter( CConstants.QUESTION_FULLNAME_PARAM );
                if ( qFullName != null )
                    wdkQuestion = getQuestionByFullName( qFullName );
            }
            if ( wdkQuestion == null ) {
                throw new RuntimeException(
                        "Unexpected error: answer maker (wdkQuestion) is null" );
            }
            
            Map< String, Object > params = handleMultiPickParams( new LinkedHashMap< String, Object >(
                    qForm.getMyProps() ) );
            
            wdkAnswer = summaryPaging( request, wdkQuestion, params );
            
            request.setAttribute( CConstants.WDK_QUESTION_PARAMS_KEY, params );
            
            // add AnswerBean to User for query history
            wdkUser = ( UserBean ) request.getSession().getAttribute(
                    CConstants.WDK_USER_KEY );
            if ( wdkUser == null ) {
                ShowQuestionSetsAction.sessionStart( request, getServlet() );
                wdkUser = ( UserBean ) request.getSession().getAttribute(
                        CConstants.WDK_USER_KEY );
            }
            history = wdkUser.createHistory( wdkAnswer );
            
            strHistId = Integer.toString( history.getHistoryId() );
        }
        
        // delete empty history
        if ( history != null && history.getEstimateSize() == 0 )
            wdkUser.deleteHistory( history.getHistoryId() );
        
        // clear boolean root from session to prevent interference
        request.getSession().setAttribute( CConstants.CURRENT_BOOLEAN_ROOT_KEY,
                null );
        
        request.setAttribute( CConstants.WDK_ANSWER_KEY, wdkAnswer );
        
        // store history too
        request.setAttribute( CConstants.WDK_HISTORY_KEY, history );
        
        request.setAttribute( CConstants.WDK_HISTORY_ID_KEY, strHistId );
        
        boolean alwaysGoToSummary = false;
        if ( CConstants.YES.equalsIgnoreCase( ( String ) getServlet().getServletContext().getAttribute(
                CConstants.WDK_ALWAYSGOTOSUMMARY_KEY ) )
                || CConstants.YES.equalsIgnoreCase( request.getParameter( CConstants.ALWAYS_GOTO_SUMMARY_PARAM ) ) ) {
            alwaysGoToSummary = true;
        }
        if ( CConstants.NO.equalsIgnoreCase( request.getParameter( CConstants.ALWAYS_GOTO_SUMMARY_PARAM ) ) ) {
            alwaysGoToSummary = false;
        }
        ActionForward forward = getForward( wdkAnswer, mapping, strHistId,
                alwaysGoToSummary );
        // System.out.println("SSA: control going to " + forward.getPath());
        return forward;
    }
    
    private ActionForward getForward( AnswerBean wdkAnswer,
            ActionMapping mapping, String strHistoryId,
            boolean alwaysGoToSummary ) {
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = ( String ) svltCtx.getAttribute( CConstants.WDK_CUSTOMVIEWDIR_KEY );
        String customViewFile1 = customViewDir + File.separator
                + wdkAnswer.getQuestion().getFullName() + ".summary.jsp";
        String customViewFile2 = customViewDir + File.separator
                + wdkAnswer.getRecordClass().getFullName() + ".summary.jsp";
        String customViewFile3 = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_SUMMARY_PAGE;
        ActionForward forward = null;
        
        if ( wdkAnswer.getResultSize() == 1 && !wdkAnswer.getIsDynamic()
                && !alwaysGoToSummary ) {
            RecordBean rec = ( RecordBean ) wdkAnswer.getRecords().next();
            forward = mapping.findForward( CConstants.SKIPTO_RECORD_MAPKEY );
            String path = forward.getPath() + "?name="
                    + rec.getRecordClass().getFullName() + "&primary_key="
                    + rec.getPrimaryKey().getRecordId();
            if ( rec.getPrimaryKey().getProjectId() != null ) {
                path += "&project_id=" + rec.getPrimaryKey().getProjectId();
            }
            return new ActionForward( path );
        }
        
        if ( ApplicationInitListener.resourceExists( customViewFile1, svltCtx ) ) {
            forward = new ActionForward( customViewFile1 );
        } else if ( ApplicationInitListener.resourceExists( customViewFile2,
                svltCtx ) ) {
            forward = new ActionForward( customViewFile2 );
        } else if ( ApplicationInitListener.resourceExists( customViewFile3,
                svltCtx ) ) {
            forward = new ActionForward( customViewFile3 );
        } else {
            forward = mapping.findForward( CConstants.SHOW_SUMMARY_MAPKEY );
        }
        
        if ( strHistoryId == null ) {
            return forward;
        }
        
        String path = forward.getPath();
        // if (path.indexOf("?") >= 0) {
        // if (path.indexOf(CConstants.WDK_HISTORY_ID_KEY) < 0) {
        // path += "&" + CConstants.WDK_HISTORY_ID_KEY + "="
        // + strHistoryId;
        // }
        // } else {
        // path += "?" + CConstants.WDK_HISTORY_ID_KEY + "=" + strHistoryId;
        // }
        return new ActionForward( path );
    }
    
    protected Map< String, Object > handleMultiPickParams(
            Map< String, Object > params ) {
        Iterator< String > newParamNames = params.keySet().iterator();
        while ( newParamNames.hasNext() ) {
            String paramName = newParamNames.next();
            Object paramVal = params.get( paramName );
            String paramValStr = null;
            if ( paramVal instanceof String[ ] ) {
                String[ ] pVals = ( String[ ] ) paramVal;
                paramValStr = pVals[ 0 ];
                for ( int i = 1; i < pVals.length; i++ ) {
                    paramValStr += "," + pVals[ i ];
                }
                params.put( paramName, paramValStr );
            } else {
                paramValStr = ( paramVal == null ? null : paramVal.toString() );
            }
            // System.err.println("*** debug params: (k, v) = " + paramName + ",
            // " + paramValStr);
        }
        return params;
    }
    
    protected AnswerBean booleanAnswerPaging( HttpServletRequest request,
            Object answerMaker ) throws WdkModelException, WdkUserException {
        return summaryPaging( request, answerMaker, null, null );
    }
    
    protected AnswerBean summaryPaging( HttpServletRequest request,
            Object answerMaker, Map< String, Object > params )
            throws WdkModelException, WdkUserException {
        return summaryPaging( request, answerMaker, params, null );
    }
    
    private AnswerBean summaryPaging( HttpServletRequest request,
            Object answerMaker, Map< String, Object > params,
            AnswerBean wdkAnswer ) throws WdkModelException, WdkUserException {
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        int start = 1;
        if ( request.getParameter( "pager.offset" ) != null ) {
            start = Integer.parseInt( request.getParameter( "pager.offset" ) );
            start++;
        }
        int pageSize = wdkUser.getItemsPerPage();
        String pageSizeKey = request.getParameter( CConstants.WDK_PAGE_SIZE_KEY );
        if ( pageSizeKey != null ) {
            pageSize = Integer.parseInt( pageSizeKey );
            wdkUser.setItemsPerPage( pageSize );
        } else {
            String altPageSizeKey = request.getParameter( CConstants.WDK_ALT_PAGE_SIZE_KEY );
            if ( altPageSizeKey != null )
                pageSize = Integer.parseInt( altPageSizeKey );
        }
        
        if ( wdkAnswer != null ) {
            answerMaker = wdkAnswer.getQuestion();
            params = wdkAnswer.getInternalParams();
            // if (wdkAnswer.getPageSize() == wdkAnswer.getResultSize()) {
            // pageSize = wdkAnswer.getResultSize();
            // }
        }
        if ( start < 1 ) {
            start = 1;
        }
        int end = start + pageSize - 1;
        
        logger.info( "Make answer with start=" + start + ", end=" + end );
        
        if ( answerMaker instanceof QuestionBean ) {
            QuestionBean question = ( QuestionBean ) answerMaker;
            Map< String, Boolean > sortingAttributes = wdkUser.getSortingAttributes( question.getFullName() );
            wdkAnswer = question.makeAnswer( params, start, end,
                    sortingAttributes );
        } else if ( answerMaker instanceof BooleanQuestionNodeBean ) {
            wdkAnswer = ( ( BooleanQuestionNodeBean ) answerMaker ).makeAnswer(
                    start, end );
        } else {
            throw new RuntimeException( "unexpected answerMaker: "
                    + answerMaker );
        }
        
        int totalSize = wdkAnswer.getResultSize();
        
        if ( end > totalSize ) {
            end = totalSize;
        }
        
        List< String > editedParamNames = new ArrayList< String >();
        for ( Enumeration en = request.getParameterNames(); en.hasMoreElements(); ) {
            String key = ( String ) en.nextElement();
            if ( !key.equals( CConstants.WDK_PAGE_SIZE_KEY )
                    && !key.equals( CConstants.WDK_ALT_PAGE_SIZE_KEY )
                    && !"start".equals( key ) && !"pager.offset".equals( key ) ) {
                editedParamNames.add( key );
            }
        }
        
        String parentUriString = request.getRequestURI();
        if ( parentUriString.indexOf( "?" ) >= 0 )
            parentUriString.substring( 0, parentUriString.indexOf( "?" ) );
        request.setAttribute( "wdk_paging_total", new Integer( totalSize ) );
        request.setAttribute( "wdk_paging_pageSize", new Integer( pageSize ) );
        request.setAttribute( "wdk_paging_start", new Integer( start ) );
        request.setAttribute( "wdk_paging_end", new Integer( end ) );
        request.setAttribute( "wdk_paging_url", parentUriString );
        request.setAttribute( "wdk_paging_params", editedParamNames );
        return wdkAnswer;
    }
}
