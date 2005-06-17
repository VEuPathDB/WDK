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
 * This Action is called by the ActionServlet when a download request is made.
 * It 1) reads param values from request,
 *    2a) forwards control to a GetDownloadResult if chooseFields is off or
 *    2b) forwards control to a jsp page that allows fields to selected in download
 */

public class ProcessDownloadAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	ActionForward forward = null;
	String  chooseFields = request.getParameter(CConstants.PD_CHOOSE_KEY);
	if (new String("on").equals(chooseFields)){
	    //System.err.println("*** PDA: forward to " + CConstants.PD_CONFIG_DOWNLOAD_MAPKEY);
	    forward = mapping.findForward(CConstants.PD_CONFIG_DOWNLOAD_MAPKEY);
	}
	else {
	    //System.err.println("*** PDA: forward to " + CConstants.PD_GET_DOWNLOAD_RESULT_MAPKEY);
	    forward = mapping.findForward(CConstants.PD_GET_DOWNLOAD_RESULT_MAPKEY);
	}
	return forward;
    }
}
