package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;


/**
 * This Action is called by the ActionServlet when a WDK record is requested.
 * It 1) reads param values from request,
 *    2) makes record
 *    3) forwards control to a jsp page that displays a record
 */

public class ShowRecordAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	ServletContext svltCtx = getServlet().getServletContext();
	WdkModelBean wdkModel = (WdkModelBean)svltCtx.getAttribute(CConstants.WDK_MODEL_KEY);
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
    
//* modified by mheiges to make backward compatible with older url
//* formats which only had an 'id' parameter (which was renamed 
//* 'primary_key' as part of the federation code integration).
    String id = (request.getParameter("id") != null) ? 
           request.getParameter("id") :
           request.getParameter("primary_key");
    String projectID = null;
    if (request.getParameter("project_id") != null) {
        projectID = request.getParameter("project_id").trim();
        if (projectID.length() == 0) projectID = null;
    }

    RecordClassBean wdkRecordClass = wdkModel.findRecordClass(request.getParameter("name"));
    RecordBean wdkRecord = wdkRecordClass.makeRecord(projectID, id);

	request.setAttribute(CConstants.WDK_RECORD_KEY, wdkRecord);

	String customViewFile1 = customViewDir + File.separator
	    + wdkRecordClass.getFullName() + ".jsp";
	String customViewFile2 = customViewDir + File.separator
	    + CConstants.WDK_CUSTOM_RECORD_PAGE;
	ActionForward forward = null;
	if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
	    forward = new ActionForward(customViewFile1 + "?id=" + id, false);
	} else if (ApplicationInitListener.resourceExists(customViewFile2, svltCtx)) {
	    forward = new ActionForward(customViewFile2 + "?id=" + id, false);
	} else {
	    forward = mapping.findForward(CConstants.SHOW_RECORD_MAPKEY);
	}
	return forward;
    }
}
