package org.gusdb.wdk.controller.action;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import org.gusdb.wdk.controller.CConstants;

/**
 * This Action is a glue action to allow display of questionSetsFlat to be handled uniformly.
 * It forwards on the control
 */

public class ShowQuestionSetsAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	ActionForward forward = mapping.findForward(CConstants.SHOW_QUESTIONSETS_MAPKEY);
	return forward;
    }
}
