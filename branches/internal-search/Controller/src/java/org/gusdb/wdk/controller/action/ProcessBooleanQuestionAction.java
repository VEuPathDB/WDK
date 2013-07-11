package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;

/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class ProcessBooleanQuestionAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	ActionForward forward = null;
	String  submitAction = request.getParameter(CConstants.PBQ_SUBMIT_KEY);
	if (submitAction.equals(CConstants.PBQ_SUBMIT_GET_BOOLEAN_ANSWER)){
	    forward = mapping.findForward(CConstants.PBQ_GET_BOOLEAN_ANSWER_MAPKEY);
	}
	else {
	    if (submitAction.startsWith(CConstants.PBQ_SUBMIT_GROW_BOOLEAN)){		
		forward = mapping.findForward(CConstants.PBQ_GROW_BOOLEAN_MAPKEY);
	    }
	}
	return forward;
    }
}
