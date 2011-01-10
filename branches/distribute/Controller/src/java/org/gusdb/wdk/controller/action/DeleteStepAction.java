package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action handles moving a step in a search strategy to a different
 * position. It moves the step, updates the subsequent steps, and forwards to
 * ShowSummaryAction
 **/

public class DeleteStepAction extends ProcessFilterAction {
    private static final Logger logger = Logger.getLogger(DeleteStepAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering DeleteStepAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
	WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            // Make sure a strategy is specified
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String strBranchId = null;

            if (strStratId == null || strStratId.length() == 0) {
                throw new WdkModelException(
                        "No strategy was specified for deleting a step!");
            }

            StrategyBean strategy;
            // StepBean targetStep;
            // StepBean step, newStep;
            // String boolExp;

            // Are we revising or deleting a step?
            String deleteStep = request.getParameter("step");

            if (deleteStep == null || deleteStep.length() == 0) {
                throw new WdkModelException("No step was specified to delete!");
            }

            String strategyKey = strStratId;
            if (strStratId.indexOf("_") > 0) {
                strBranchId = strStratId.split("_")[1];
                strStratId = strStratId.split("_")[0];
            }

            strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
            // verify the checksum
            String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
            if (checksum != null && !strategy.getChecksum().equals(checksum)) {
                ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
                return null;
            }

            int oldStrategyId = strategy.getStrategyId();

            if (wdkUser.getViewStrategyId() != null
                    && wdkUser.getViewStrategyId().equals(strategyKey)
                    && wdkUser.getViewStepId() == Integer.parseInt(deleteStep)) {
                wdkUser.resetViewResults();
            }

            Map<Integer, Integer> stepIdsMap = strategy.deleteStep(
                    Integer.valueOf(deleteStep), (strBranchId != null));
            // If a branch was specified, look up the new branch id in the
            // stepIdsMap
            if (strBranchId != null) {
                if (stepIdsMap.containsKey(Integer.valueOf(strBranchId))) strBranchId = stepIdsMap.get(
                        Integer.valueOf(strBranchId)).toString();
                else strBranchId = null;
            }

            // If strategy was marked for deletion as a result of deleting
            // the step, forward to DeleteStrategy
            if (strategy.getIsDeleted()) {
                ActionForward forward = mapping.findForward(CConstants.DELETE_STRATEGY_MAPKEY);
                StringBuffer url = new StringBuffer(forward.getPath());
                url.append("?strategy="
                        + URLEncoder.encode(strStratId, "utf-8"));
                forward = new ActionForward(url.toString());
                forward.setRedirect(true);
                return forward;
            }

            try {
                wdkUser.replaceActiveStrategy(oldStrategyId,
                        strategy.getStrategyId(), stepIdsMap);
            } catch (WdkUserException ex) {
                // Need to add strategy to active strategies list
                // which will be handled by ShowStrategyAction
            }

            // 5. forward to strategy page
            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

            ActionForward forward = new ActionForward(url.toString());
            forward.setRedirect(true);
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }
}
