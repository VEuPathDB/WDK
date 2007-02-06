package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;

/**
 * This Action is called by the ActionServlet when a flat display of QuestionSets is needed.
 * It 1) gets all questions in all questionSets from the WDK model
 *    2) forwards control to a jsp page that displays all questions in all questionSets
 */

public class ProcessQuestionSetsFlatAction extends ShowQuestionAction {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	QuestionSetForm qSetForm = (QuestionSetForm)form;
	//System.err.println("DEBUG: PQSFA: qFullName from form: " + qSetForm.getQuestionFullName());
	QuestionBean wdkQuestion = getQuestionByFullName(qSetForm.getQuestionFullName());
	//System.err.println("DEBUG: PQSFA: qFullName from question: " + wdkQuestion.getFullName());
	QuestionForm qForm = prepareQuestionForm(wdkQuestion, qSetForm);

	request.setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
	request.setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);
	request.setAttribute("parentURI", request.getRequestURI());

	ActionForward forward = mapping.findForward(CConstants.PROCESS_QUESTIONSETSFLAT_MAPKEY);
	
	return forward;
    }

    private QuestionForm prepareQuestionForm (QuestionBean wdkQuestion, QuestionSetForm qSetForm) throws Exception
    {
	QuestionForm qForm = new QuestionForm();

	ActionServlet servlet = getServlet();
	qForm.setServlet(servlet);

	String qFullName = qSetForm.getQuestionFullName();
	int dotI = qFullName.indexOf('.');
	String qSetName = qFullName.substring(0, dotI);
	String qName = qFullName.substring(dotI+1, qFullName.length());
	String pref = qSetName + "_" + qName + "_";
	ParamBean[] params = wdkQuestion.getParams();
	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    if (p instanceof FlatVocabParamBean) {
		qForm.getMyLabels().put(p.getName(), qSetForm.getLabels(pref + p.getName()));
		qForm.getMyValues().put(p.getName(), qSetForm.getValues(pref + p.getName()));
	    }
	    qForm.getMyProps().put(p.getName(), qSetForm.getMyProp(pref + p.getName()));
	}
	qForm.setQuestion(wdkQuestion);

	/*
	java.util.Iterator it = qForm.getMyProps().keySet().iterator();
        while (it.hasNext()) {
	    String key = (String)it.next();
            System.err.println("DEBUG: PQSFA: qForm myProp(" + key + ") = " + qForm.getMyPropObject(key));
	}
	*/
	//System.err.println("DEBUG: qForm created in PQSFA: " + qForm);

	return qForm;
    }
}
