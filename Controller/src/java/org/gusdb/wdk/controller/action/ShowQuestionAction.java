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
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.Summary;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.FlatVocabParam;

/**
 * This Action is called by the ActionServlet when a WDK question is requested.
 * It 1) finds the full name from the form,
 *    2) gets the question from the WDK model
 *    3) forwards control to a jsp page that displays a question form
 */

public class ShowQuestionAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	String qFullName = ((QuestionSetForm)form).getQuestionFullName();
	int dotI = qFullName.indexOf('.');
	String qSetName = qFullName.substring(0, dotI);
	String qName = qFullName.substring(dotI+1, qFullName.length());

	WdkModel wdkModel = (WdkModel)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	Summary wdkQuestion = wdkModel.getSummarySet(qSetName).getSummary(qName);
	request.getSession().setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);

	QuestionForm qForm = prepareQuestionForm(wdkQuestion);
	request.getSession().setAttribute(CConstants.QUESTIONFORM_KEY, qForm);

	ActionForward forward = mapping.findForward(CConstants.SHOW_QUESTION_MAPKEY);
	return forward;
    }

    private QuestionForm prepareQuestionForm (Summary wdkQuestion) throws Exception
    {
	QuestionForm qForm = new QuestionForm();

	ActionServlet servlet = getServlet();
	qForm.setServlet(servlet);
	
	ServletContext context = servlet.getServletContext();
	Param[] params = wdkQuestion.getQuery().getParams();

	qForm.setMyProps(new HashMap());
	qForm.setMyLabels(new HashMap());
	qForm.setMyValues(new HashMap());

	for (int i=0; i<params.length; i++) {
	    Param p = params[i];
	    if (p instanceof FlatVocabParam) {
		//not assuming fixed order, so call once, use twice.
		String[] flatVocab = ((FlatVocabParam)p).getVocab();
		qForm.getMyLabels().put(p.getName(), flatVocab);
		qForm.getMyValues().put(p.getName(), flatVocab);
	    }
	    qForm.getMyProps().put(p.getName(), p.getDefault());
	}
	qForm.setQuestion(wdkQuestion);
	return qForm;
    }
}
