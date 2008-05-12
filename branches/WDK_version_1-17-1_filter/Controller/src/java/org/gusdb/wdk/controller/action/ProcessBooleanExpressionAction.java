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
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.ProtocolBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is process boolean expression on queryHistory.jsp page.
 * 
 */

public class ProcessBooleanExpressionAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			BooleanExpressionForm beForm = (BooleanExpressionForm) form;
			String userAnswerIdStr = processBooleanExpression(request, beForm);

			ActionForward fwd = mapping
					.findForward(CConstants.PROCESS_BOOLEAN_EXPRESSION_MAPKEY);
			String path = fwd.getPath();
			if (path.indexOf("?") > 0) {
				if (path.indexOf(CConstants.WDK_HISTORY_ID_KEY) < 0) {
					path += "&" + CConstants.WDK_HISTORY_ID_KEY + "="
							+ userAnswerIdStr;
				}
			} else {
				path += "?" + CConstants.WDK_HISTORY_ID_KEY + "="
						+ userAnswerIdStr;
			}

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
		HistoryBean history = wdkUser.combineHistory(beForm
				.getBooleanExpression());
		int historyId = history.getHistoryId();

		// 1. Check for StepBean
		// 2. If exists, update step w/ filter history
		String stepKey = request.getParameter("step");
		if (stepKey != null && stepKey.length() != 0) {
		    StepBean step = (StepBean) request.getSession().getAttribute(stepKey);
		    step.setFilterHistory(history);
		    request.getSession().setAttribute(stepKey, step);
		}

		request.setAttribute(CConstants.WDK_HISTORY_ID_KEY, historyId);
		return Integer.toString(historyId);
	}
}
