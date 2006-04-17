package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;
import org.gusdb.wdk.model.jspwrap.UserBean;


/**
 * This Action is process boolean expression on queryHistory.jsp page.
 *
 */

public class ProcessBooleanExpressionAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	BooleanExpressionForm beForm = (BooleanExpressionForm)form;
	String userAnswerIdStr = processBooleanExpression(request, beForm);

	ActionForward fwd = mapping.findForward(CConstants.PROCESS_BOOLEAN_EXPRESSION_MAPKEY);
	String path = fwd.getPath();
	if(path.indexOf("?") > 0 ) {
	    if(path.indexOf(CConstants.USER_ANSWER_ID) < 0) {
		path += "&" + CConstants.USER_ANSWER_ID + "=" + userAnswerIdStr; 
	    }
	} else {
	    path += "?" + CConstants.USER_ANSWER_ID + "=" + userAnswerIdStr; 
	}

	return new ActionForward(path);
    }

    private String processBooleanExpression(HttpServletRequest request, 
            BooleanExpressionForm beForm)
	    throws WdkModelException, WdkUserException
    {
	int start = 1;
	int end = 3;
	UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	UserAnswerBean userAnswer = 
        wdkUser.combineAnswers(beForm.getBooleanExpression(), start, end, 
                BooleanExpressionForm.booleanOperatorMap);
	AnswerBean wdkAnswer = userAnswer.getAnswer();
	int aid = userAnswer.getAnswerID();

	String aidStr = new Integer(aid).toString();
	request.setAttribute(CConstants.USER_ANSWER_ID, aidStr);
	wdkUser.addAnswerFuzzy(wdkAnswer);
	return new Integer(aid).toString();
    }
}
