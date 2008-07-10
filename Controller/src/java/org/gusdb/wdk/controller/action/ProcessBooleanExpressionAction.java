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
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;
import org.gusdb.wdk.model.jspwrap.UserStrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is process boolean expression on queryUserAnswer.jsp page.
 * 
 */

public class ProcessBooleanExpressionAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			BooleanExpressionForm beForm = (BooleanExpressionForm) form;
			String userRecordPageIdStr = processBooleanExpression(request, beForm);

			ActionForward fwd = mapping
					.findForward(CConstants.PROCESS_BOOLEAN_EXPRESSION_MAPKEY);
			String path = fwd.getPath();
			if (path.indexOf("?") > 0) {
			    if (path.indexOf(CConstants.WDK_HISTORY_ID_KEY) < 0) {
				path += "&" + CConstants.WDK_HISTORY_ID_KEY + "="
				    + userRecordPageIdStr;
			    }
			} else {
			    path += "?" + CConstants.WDK_HISTORY_ID_KEY + "="
				+ userRecordPageIdStr;
			}

			/* Step code
			String stepKey = request.getParameter("addStep");
			if (stepKey != null && stepKey.length() != 0) {
			    path += "&strategy=" + Integer.parseInt(request.getAttribute(CConstants.WDK_STRATEGY_ID_KEY).toString());
			    request.removeAttribute(CConstants.WDK_STRATEGY_ID_KEY);
			    fwd = new ActionForward(path);
			    fwd.setRedirect(true);
			    return fwd;
			}
			*/
			return new ActionForward(path);
		} catch (Exception ex) {
		    ex.printStackTrace();
		    throw ex;
		}
	}
    
	private String processBooleanExpression(HttpServletRequest request,
			BooleanExpressionForm beForm) throws WdkModelException,
			WdkUserException {
		UserBean wdkUser = (UserBean) request.getSession().getAttribute(
				CConstants.WDK_USER_KEY);
		UserAnswerBean userAnswer = wdkUser.combineUserAnswer(beForm
				.getBooleanExpression());
		int userAnswerId = userAnswer.getUserAnswerId();
		/*
		// 1. Check for strategy id
		// 2. If exists, load strategy
		//    i. Check for Step object
		//   ii. If exists, add filter userAnswer & add step to strategy
		//  iii. Remove attributes for Step object

		String strProtoId = request.getParameter("strategy");
 	
		if (strProtoId != null && strProtoId.length() != 0) {
		    UserStrategyBean strategy = wdkUser.getUserStrategy(Integer.parseInt(strProtoId));
		    String stepKey = request.getParameter("addStep");
		    if (stepKey != null && stepKey.length() != 0) {
			StepBean subQuery = (StepBean) request.getSession().getAttribute(stepKey);
			StepBean step = new StepBean(userAnswer);
			step.setChildStep(subQuery);
			strategy.addStep(step);
			strategy.update();
			request.getSession().removeAttribute(stepKey);
			request.setAttribute(CConstants.WDK_STRATEGY_ID_KEY, strategy.getStrategyId());
		    }
		}
		*/
		request.setAttribute(CConstants.WDK_HISTORY_ID_KEY, userAnswerId);
		return Integer.toString(userAnswerId);
	}
}
