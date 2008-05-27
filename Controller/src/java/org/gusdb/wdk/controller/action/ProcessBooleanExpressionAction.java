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
			String stepKey = request.getParameter("addStep").toString();
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

			if (stepKey != null && stepKey.length() != 0) {
			    path += "&protocol=" + Integer.parseInt(request.getAttribute(CConstants.WDK_PROTOCOL_ID_KEY).toString());
			    request.removeAttribute(CConstants.WDK_PROTOCOL_ID_KEY);
			    fwd = new ActionForward(path);
			    fwd.setRedirect(true);
			    return fwd;
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

		// 1. Check for protocol id
		// 2. If exists, load protocol
		//    i. Check for Step object
		//   ii. If exists, add filter history & add step to protocol
		//  iii. Remove attributes for Step object

		String strProtoId = request.getParameter("protocol");
 	
		if (strProtoId != null && strProtoId.length() != 0) {
		    ProtocolBean protocol = null;
		    protocol = ProtocolBean.getProtocol(strProtoId, protocol, wdkUser);
		    String stepKey = request.getParameter("addStep").toString();
		    if (stepKey != null && stepKey.length() != 0) {
			StepBean step = (StepBean) request.getSession().getAttribute(stepKey);
			step.setFilterHistory(history);
			protocol.addStep(step);
			request.getSession().removeAttribute(stepKey);
			request.setAttribute(CConstants.WDK_PROTOCOL_ID_KEY, protocol.getProtocolId());
		    }
		}

		request.setAttribute(CConstants.WDK_HISTORY_ID_KEY, historyId);
		return Integer.toString(historyId);
	}
}
