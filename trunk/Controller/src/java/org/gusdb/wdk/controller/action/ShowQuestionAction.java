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
import java.util.Vector;
import java.io.File;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.ApplicationInitListener;

import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;

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
	QuestionBean wdkQuestion = getQuestionByFullName(qFullName);

	request.getSession().setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);

	QuestionForm qForm = prepareQuestionForm(wdkQuestion);
	request.getSession().setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
	request.getSession().setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);

	ServletContext svltCtx = getServlet().getServletContext();
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
	String customViewFile1 = customViewDir + File.separator
	    + wdkQuestion.getFullName() + ".jsp";
	String customViewFile2 = customViewDir + File.separator
	    + wdkQuestion.getRecordClass().getFullName() + ".question.jsp";
	String customViewFile3 = customViewDir + File.separator
	    + CConstants.WDK_CUSTOM_QUESTION_PAGE;
	ActionForward forward = null;
	if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
	    forward = new ActionForward(customViewFile1);
	} else if (ApplicationInitListener.resourceExists(customViewFile2, svltCtx)) {
	    forward = new ActionForward(customViewFile2);
 	} else if (ApplicationInitListener.resourceExists(customViewFile3, svltCtx)) {
	    forward = new ActionForward(customViewFile3);
	} else {
	    forward = mapping.findForward(CConstants.SHOW_QUESTION_MAPKEY);
	}
	return forward;

    }

    protected QuestionBean getQuestionByFullName(String qFullName) {
	int dotI = qFullName.indexOf('.');
	String qSetName = qFullName.substring(0, dotI);
	String qName = qFullName.substring(dotI+1, qFullName.length());

	WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	
	QuestionSetBean wdkQuestionSet = (QuestionSetBean)wdkModel.getQuestionSetsMap().get(qSetName);
	QuestionBean wdkQuestion = (QuestionBean)wdkQuestionSet.getQuestionsMap().get(qName);
	return wdkQuestion;
    }

    private QuestionForm prepareQuestionForm (QuestionBean wdkQuestion) throws Exception
    {
	QuestionForm qForm = new QuestionForm();

	ActionServlet servlet = getServlet();
	qForm.setServlet(servlet);
	
	ServletContext context = servlet.getServletContext();
	ParamBean[] params = wdkQuestion.getParams();

	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    if (p instanceof FlatVocabParamBean) {
		//not assuming fixed order, so call once, use twice.
		String[] flatVocab = ((FlatVocabParamBean)p).getVocab();
		qForm.getMyValues().put(p.getName(), flatVocab);
		qForm.getMyLabels().put(p.getName(), getLengthBoundedLabels(flatVocab));
	    }
	    qForm.getMyProps().put(p.getName(), p.getDefault());
	}
	qForm.setQuestion(wdkQuestion);
	return qForm;
    }


    static String[] getLengthBoundedLabels(String[] labels) {
	return getLengthBoundedLabels(labels, 99);
    }

    static String[] getLengthBoundedLabels(String[] labels, int maxLength) {
	Vector v = new Vector();
	for (String l : labels) {
	    int len = l.length();
	    if (len > 99) {
		l = l.substring(0, 48) + "..." + l.substring(len - 48, len);
	    }
	    v.add(l);
	}
	String[] newLabels = new String[v.size()];
	v.copyInto(newLabels);
	return newLabels;
    }
}
