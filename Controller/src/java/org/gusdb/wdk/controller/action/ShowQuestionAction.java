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

public class ShowQuestionAction extends ShowQuestionSetsFlatAction {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	String qFullName = ((QuestionSetForm)form).getQuestionFullName();
	QuestionBean wdkQuestion = getQuestionByFullName(qFullName);

        QuestionForm qForm = prepareQuestionForm(wdkQuestion, request);

	QuestionSetForm qSetForm = (QuestionSetForm)request.getSession()
	    .getAttribute(CConstants.QUESTIONSETFORM_KEY);
	if(null == qSetForm) {
	    qSetForm = new QuestionSetForm();
	    request.getSession().setAttribute(CConstants.QUESTIONSETFORM_KEY, qSetForm);
	}
	qSetForm.setQuestionFullName(qFullName);
	prepareQuestionSetForm(getServlet(), qSetForm);

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

	String gotoSum = request.getParameter(CConstants.GOTO_SUMMARY_PARAM);
	if (qForm.getParamsFilled() && "1".equals(gotoSum)) {
            forward = mapping.findForward(CConstants.SKIPTO_SUMMARY_MAPKEY);
	    //System.out.println("SQA: form has all param vals, go to summary page " + forward.getPath() + " directly");
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

    protected QuestionForm prepareQuestionForm (QuestionBean wdkQuestion, HttpServletRequest request)
	throws Exception {
	QuestionForm qForm = new QuestionForm();

	ActionServlet servlet = getServlet();
	qForm.setServlet(servlet);
	
	ServletContext context = servlet.getServletContext();
	ParamBean[] params = wdkQuestion.getParams();

	boolean hasAllParams = true;
	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    String cgiParamVal = request.getParameter(p.getName());
	    Object pVal = null;
	    if (p instanceof FlatVocabParamBean) {
		//not assuming fixed order, so call once, use twice.
		String[] flatVocab = ((FlatVocabParamBean)p).getVocab();
		qForm.getMyValues().put(p.getName(), flatVocab);
		qForm.getMyLabels().put(p.getName(), getLengthBoundedLabels(flatVocab));
		if (cgiParamVal != null) pVal = new String[] {cgiParamVal};
	    } else {
		pVal = cgiParamVal;
	    }

	    //System.out.println("DEBUG: param " + p.getName() + " = '" + pVal + "'");
	    if (pVal == null) {
		hasAllParams = false;
		pVal = p.getDefault();
	    }
	    qForm.getMyProps().put(p.getName(), pVal);
	}
	qForm.setQuestion(wdkQuestion);
	qForm.setParamsFilled(hasAllParams);

	if (request.getParameter(CConstants.VALIDATE_PARAM) == "0") {
	    qForm.setNonValidating();
	}

	request.getSession().setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
	request.getSession().setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);

	return qForm;
    }

    static String[] getLengthBoundedLabels(String[] labels) {
	return getLengthBoundedLabels(labels, CConstants.MAX_PARAM_LABEL_LEN);
    }

    static String[] getLengthBoundedLabels(String[] labels, int maxLength) {
	Vector v = new Vector();
	int halfLen = maxLength/2;
	for (String l : labels) {
	    int len = l.length();
	    if (len > CConstants.MAX_PARAM_LABEL_LEN) {
		l = l.substring(0, halfLen) + "..." + l.substring(len - halfLen, len);
	    }
	    v.add(l);
	}
	String[] newLabels = new String[v.size()];
	v.copyInto(newLabels);
	return newLabels;
    }
}
