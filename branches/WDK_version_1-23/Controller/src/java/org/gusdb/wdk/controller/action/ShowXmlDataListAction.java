package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.XmlQuestionSetBean;

/**
 * This Action is called by the ActionServlet when a list of xml data contents is requested.
 * It 1) gets available xml data list from the WDK model and save in the request scope
 *    2) forwards control to a jsp page that displays a list of the xml data content
 */

public class ShowXmlDataListAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	XmlQuestionSetBean[] wdkXmlQuestionSets = wdkModel.getXmlQuestionSets();
	request.setAttribute(CConstants.WDK_XMLQUESTIONSETS_KEY, wdkXmlQuestionSets);

	String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR
	    + File.separator + CConstants.WDK_PAGES_DIR;

	String defaultViewFile = customViewDir
	    + File.separator + CConstants.WDK_XMLDATALIST_PAGE;

	System.out.println(defaultViewFile);

	ActionForward forward = new ActionForward(defaultViewFile);

	return forward;

    }

}
