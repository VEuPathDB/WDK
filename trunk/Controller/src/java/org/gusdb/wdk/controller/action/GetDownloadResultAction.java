package org.gusdb.wdk.controller.action;

//import java.util.Map;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
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
//import org.gusdb.wdk.model.jspwrap.QuestionBean;
//import org.gusdb.wdk.model.jspwrap.AnswerBean;
//import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

/**
 * This Action is called by the ActionServlet when a download submit is made.
 * It 1) find selected fields (may be all fields in answer bean)
 *    2) use AnserBean in session score to get and format results
 *    3) forward control to a jsp page that displays the result
 */

public class GetDownloadResultAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	String downloadResult = "TODO: get and format result string";
	request.setAttribute(CConstants.DOWNLOAD_RESULT_KEY, downloadResult);
	System.err.println("*** GDA: forward to " + CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
	ActionForward forward = mapping.findForward(CConstants.GET_DOWNLOAD_RESULT_MAPKEY);
	return forward;
    }
}
