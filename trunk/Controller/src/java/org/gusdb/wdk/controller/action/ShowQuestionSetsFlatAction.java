package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a flat display of
 * QuestionSets is needed. It 1) gets all questions in all questionSets from the
 * WDK model 2) forwards control to a jsp page that displays all questions in
 * all questionSets
 */

public class ShowQuestionSetsFlatAction extends ShowQuestionSetsAction {
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        QuestionSetForm qSetForm = ( QuestionSetForm ) form;
        prepareQuestionSetForm( getServlet(), qSetForm );
        
        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = ( String ) svltCtx.getAttribute( CConstants.WDK_CUSTOMVIEWDIR_KEY );
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_QUESTIONSETS_FLAT_PAGE;
        
        ActionForward forward = null;
        if ( ApplicationInitListener.resourceExists( customViewFile, svltCtx ) ) {
            forward = new ActionForward( customViewFile );
        } else {
            forward = mapping.findForward( CConstants.SHOW_QUESTIONSETSFLAT_MAPKEY );
        }
        
        sessionStart( request, getServlet() );
        
        return forward;
    }
    
    protected void prepareQuestionSetForm( ActionServlet servlet,
            QuestionSetForm qSetForm ) throws Exception {
        ServletContext context = servlet.getServletContext();
        
        WdkModelBean wdkModel = ( WdkModelBean ) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY );
        Set qSets = wdkModel.getQuestionSetsMap().keySet();
        Iterator qSetsI = qSets.iterator();
        while ( qSetsI.hasNext() ) {
            String qSetName = ( String ) qSetsI.next();
            QuestionSetBean wdkQuestionSet = ( QuestionSetBean ) wdkModel.getQuestionSetsMap().get(
                    qSetName );
            
            Set questions = wdkQuestionSet.getQuestionsMap().keySet();
            Iterator questionsI = questions.iterator();
            while ( questionsI.hasNext() ) {
                String qName = ( String ) questionsI.next();
                QuestionBean wdkQuestion = ( QuestionBean ) wdkQuestionSet.getQuestionsMap().get(
                        qName );
                
                ParamBean[ ] params = wdkQuestion.getParams();
                
                for ( int i = 0; i < params.length; i++ ) {
                    ParamBean p = params[ i ];
                    String key = qSetName + "_" + qName + "_" + p.getName();
                    if ( ( p instanceof FlatVocabParamBean )
                            || ( p instanceof EnumParamBean ) ) {
                        // not assuming fixed order, so call once, use twice.
                        String[ ] flatVocab;
                        String[ ] labels;
                        if ( p instanceof FlatVocabParamBean ) {
                            flatVocab = ( ( FlatVocabParamBean ) p ).getVocab();
                            labels = flatVocab;
                        } else {
                            flatVocab = ( ( EnumParamBean ) p ).getVocab();
                            labels = ( ( EnumParamBean ) p ).getDisplay();
                        }
                        qSetForm.getMyValues().put( p.getName(), flatVocab );
                        qSetForm.getMyLabels().put(
                                p.getName(),
                                ShowQuestionAction.getLengthBoundedLabels( labels ) );
                    }
                    qSetForm.getMyProps().put( key, p.getDefault() );
                }
            }
        }
    }
}
