package org.gusdb.wdk.controller.action;

import java.util.Map;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
import java.util.HashMap;


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
import org.gusdb.wdk.model.jspwrap.AnswerBean;
//import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

/**
 * This Action is called by the ActionServlet when a download config request is made.
 * It 1) reads download attributes param
 *    2a) set selected attributes for download in the answer bean
 *    2b) forwards control to a GetDownloadResult action
 */

public class ConfigDownloadAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	Map downloadConfigMap = new HashMap();
	DownloadConfigForm downloadConfigForm = (DownloadConfigForm)form;
	String [] selectedFields = downloadConfigForm.getSelectedFields();
	for (int i=0; i<selectedFields.length; i++) {
	    //System.err.println("DEBUG: ConfigDownloadAction: selected field: " + selectedFields[i]);
	    downloadConfigMap.put(selectedFields[i], new Integer(1));
	}

	AnswerBean wdkAnswer = (AnswerBean)request.getSession().getAttribute(CConstants.WDK_ANSWER_KEY);
	wdkAnswer.setDownloadConfigMap(downloadConfigMap);

	ActionForward forward = mapping.findForward(CConstants.CONFIG_DOWNLOAD_MAPKEY);
	return forward;
    }
}
