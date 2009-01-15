package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action handles moving a step in a search strategy to a different
 * position. It moves the step, updates the relevant filter userAnswers, and
 * forwards to ShowSummaryAction
 **/

public class MoveStepAction extends ProcessFilterAction {

    private static final Logger logger = Logger.getLogger(MoveStepAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering MoveStepAction...");

        // Make sure strategy, step, and moveto are defined
        String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
        String strBranchId = null;
        String strMoveFromId = request.getParameter("movefrom");
        String op = request.getParameter("op");
        String strMoveToId = request.getParameter("moveto");

        // Make sure necessary arguments are provided
        if (strStratId == null || strStratId.length() == 0) {
            throw new WdkModelException(
                    "No strategy was specified for moving steps!");
        }
        if (strMoveFromId == null || strMoveFromId.length() == 0) {
            throw new WdkModelException("No step was specified for moving!");
        } else if (op == null || op.length() == 0) {
            throw new WdkModelException(
                    "No operation specified for moving first step.");
        }
        if (strMoveToId == null || strMoveToId.length() == 0) {
            throw new WdkModelException(
                    "No destination was specified for moving!");
        }

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }
        if (strStratId.indexOf("_") > 0) {
            strBranchId = strStratId.split("_")[1];
            strStratId = strStratId.split("_")[0];
        }

        StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

        ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();
        int index = -1;

        if (activeStrategies != null
                && activeStrategies.contains(new Integer(
                        strategy.getStrategyId()))) {
            index = activeStrategies.indexOf(new Integer(
                    strategy.getStrategyId()));
            activeStrategies.remove(index);
        }

        int moveFromId = Integer.valueOf(strMoveFromId);
        int moveToId = Integer.valueOf(strMoveToId);

	Map<Integer,Integer> stepIdsMap = strategy.moveStep(moveFromId, moveToId, strBranchId);
	
	if (strBranchId != null) {
	    strBranchId = stepIdsMap.get(Integer.valueOf(strBranchId)).toString();
	}
        
        if (activeStrategies != null && index >= 0) {
            activeStrategies.add(index, new Integer(strategy.getStrategyId()));
        }
        // request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY,
        // activeStrategies);
        wdkUser.setActiveStrategies(activeStrategies);

        // Forward to ShowStrategyAction
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?strategy="
                + URLEncoder.encode(Integer.toString(strategy.getStrategyId()),
                        "UTF-8"));
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strBranchId, "UTF-8"));
        }
        String viewStep = request.getParameter("step");
        if (viewStep != null && viewStep.length() != 0) {
            url.append("&step=" + URLEncoder.encode(viewStep, "UTF-8"));
        }
        String subQuery = request.getParameter("subquery");
        if (subQuery != null && subQuery.length() != 0) {
            url.append("&subquery=" + URLEncoder.encode(subQuery, "UTF-8"));
        }
        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(true);
        return forward;
    }
}
