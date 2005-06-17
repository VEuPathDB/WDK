package org.gusdb.wdk.controller.action;

import java.util.Map;
//import java.util.ArrayList;
//import java.util.Enumeration;
import java.util.Iterator;

//import java.util.HashMap;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gusdb.wdk.controller.CConstants;
//import org.gusdb.wdk.model.jspwrap.WdkModelBean;
//import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
//HACK: this class is outside of jspwrap
//      This will not be necessary when we move the download result formatting into AnswerBean
import org.gusdb.wdk.model.AttributeFieldValue;
//import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

/**
 * This Action is called by the ActionServlet when a download submit is made.
 * It 1) find selected fields (may be all fields in answer bean)
 *    2) use AnswerBean in session score to get and format results
 *    3) forward control to a jsp page that displays the result
 */

public class GetDownloadResultAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	StringBuffer downloadResult = new StringBuffer("#");
	AnswerBean wdkAnswer = (AnswerBean)request.getSession().getAttribute(CConstants.WDK_ANSWER_KEY);
	int resultSize = wdkAnswer.getResultSize();
	Map params = (Map)request.getSession().getAttribute(CConstants.WDK_QUESTION_PARAMS_KEY);
	QuestionBean wdkQuestion = (QuestionBean)wdkAnswer.getQuestion();

	String newLine = System.getProperty("line.separator");
	String tab = "\t";
	String[] downloadAttrs = wdkAnswer.getDownloadAttributeNames();
	for (int i=0; i<downloadAttrs.length; i++) {
	    downloadResult.append(downloadAttrs[i] + tab);
	}
	downloadResult.append(newLine);

	//wdkAnswer.resetAnswerRowCursor();
	int pageSize = 100;
	for (int i=1; i<=resultSize; i+=pageSize) {
	    int j = i+pageSize;
	    if (j>resultSize+1) { j = resultSize+1; }
	    wdkAnswer = wdkQuestion.makeAnswer(params, i, j-1); 
	    Iterator records = wdkAnswer.getRecords();
	    while (records.hasNext()) {
		RecordBean rec = (RecordBean)records.next();
		Map attribs = rec.getAttributes();
		for (int k=0; k<downloadAttrs.length; k++) {
		    downloadResult.append(((AttributeFieldValue)attribs.get(downloadAttrs[k])).getValue() + tab);
		}
		downloadResult.append(newLine);
	    }
	}

	request.setAttribute(CConstants.DOWNLOAD_RESULT_KEY, downloadResult.toString());
	//System.err.println("*** GDA: forward to " + CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
	ActionForward forward = mapping.findForward(CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
	return forward;
    }
}
