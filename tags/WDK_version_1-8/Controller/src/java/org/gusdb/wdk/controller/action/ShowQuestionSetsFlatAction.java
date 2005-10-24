package org.gusdb.wdk.controller.action;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.File;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;

/**
 * This Action is called by the ActionServlet when a flat display of QuestionSets is needed.
 * It 1) gets all questions in all questionSets from the WDK model
 *    2) forwards control to a jsp page that displays all questions in all questionSets
 */

public class ShowQuestionSetsFlatAction extends ShowQuestionSetsAction {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	
	QuestionSetForm qSetForm = (QuestionSetForm)form;
	prepareQuestionSetForm(wdkModel, qSetForm);
	//request.getSession().setAttribute(CConstants.QUESTIONSETFORM_KEY, qSetForm);

	ServletContext svltCtx = getServlet().getServletContext();
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
	String customViewFile = customViewDir + File.separator
	    + CConstants.WDK_CUSTOM_QUESTIONSETS_FLAT_PAGE;

	ActionForward forward = null;
	if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
	    forward = new ActionForward(customViewFile);
	} else {
	    forward = mapping.findForward(CConstants.SHOW_QUESTIONSETSFLAT_MAPKEY);
	}

	sessionStart(request);

	return forward;
    }

    private void prepareQuestionSetForm (WdkModelBean wdkModel, QuestionSetForm qSetForm) throws Exception
    {
	ActionServlet servlet = getServlet();
	//qSetForm.setServlet(servlet);
	
	ServletContext context = servlet.getServletContext();

	Set qSets = wdkModel.getQuestionSetsMap().keySet();
	Iterator qSetsI = qSets.iterator();
	while (qSetsI.hasNext()) {
	    String qSetName = (String)qSetsI.next();
	    QuestionSetBean wdkQuestionSet = (QuestionSetBean)wdkModel.getQuestionSetsMap().get(qSetName);

	    Set questions = wdkQuestionSet.getQuestionsMap().keySet();
	    Iterator questionsI = questions.iterator();
	    while (questionsI.hasNext()) {
		String qName = (String)questionsI.next();
		QuestionBean wdkQuestion = (QuestionBean)wdkQuestionSet.getQuestionsMap().get(qName);

		ParamBean[] params = wdkQuestion.getParams();

		for (int i=0; i<params.length; i++) {
		    ParamBean p = params[i];
		    String key = qSetName + "_" + qName + "_" + p.getName();
		    if (p instanceof FlatVocabParamBean) {
			//not assuming fixed order, so call once, use twice.
			String[] flatVocab = ((FlatVocabParamBean)p).getVocab();
			qSetForm.getMyLabels().put(key, flatVocab);
			qSetForm.getMyValues().put(key, flatVocab);

			System.out.println("DEBUG: ShowQuestionSetsFlatAction:prepareQuestionSetForm: " + key + " = " + flatVocab[0] + " ...");

		    }
		    qSetForm.getMyProps().put(key, p.getDefault());
		}
	    }
	}
    }
}
