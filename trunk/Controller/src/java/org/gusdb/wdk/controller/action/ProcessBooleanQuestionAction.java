package org.gusdb.wdk.controller.action;

import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.HashMap;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class ProcessBooleanQuestionAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	ActionForward forward = null;
	String  submitAction = request.getParameter("process_boolean_question");
	if (submitAction.equals("Get Boolean Answer")){
	    forward = mapping.findForward(CConstants.PBQ_GET_BOOLEAN_ANSWER_MAPKEY);
	}
	else {
	    if (submitAction.startsWith("Grow Boolean")){		
		forward = mapping.findForward(CConstants.PBQ_GROW_BOOLEAN_MAPKEY);
	    }
	}
	return forward;
    }
}
