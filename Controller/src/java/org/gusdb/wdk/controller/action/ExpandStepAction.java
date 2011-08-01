package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
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
 * This Action handles expanding a step in a search strategy (i.e., turning the
 * step into a substrategy) by setting the isCollapsible and collapsedName (if
 * not set already) and returning the expanded step to strategy.jsp.
 **/
public class ExpandStepAction extends Action {
    
    private static final String PARAM_UNCOLLAPSE = "uncollapse";
    
    private static final Logger logger = Logger.getLogger(ExpandStepAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ExpandStepAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
	WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String strStepId = request.getParameter(CConstants.WDK_STEP_ID_KEY);
            String strBranchId = null;

            if (strStratId == null || strStratId.length() == 0) {
                throw new WdkModelException(
                        "No strategy was specified for expanding a step!");
            }
            if (strStepId == null || strStepId.length() == 0) {
                throw new WdkModelException("No step specified to expand!");
            }

            if (strStratId.indexOf("_") > 0) {
                strBranchId = strStratId.split("_")[1];
                strStratId = strStratId.split("_")[0];
            }

            StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
            // verify the checksum
            String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
            if (checksum != null && !strategy.getChecksum().equals(checksum)) {
                ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
                return null;
            }

            StepBean latestStep;
            if (strBranchId == null) {
                latestStep = strategy.getLatestStep();
            } else {
                latestStep = strategy.getStepById(Integer.parseInt(strBranchId));
            }

            StepBean step = latestStep.getStepByDisplayId(Integer.parseInt(strStepId));

            if (step.getParentStep() == null) {
                throw new WdkModelException(
                        "Only top-row steps can be expanded!");
            }
            
            String strUncollapse = request.getParameter(PARAM_UNCOLLAPSE);
            boolean uncollapse = false;
            if (strUncollapse != null && strUncollapse.equalsIgnoreCase("true"))
                uncollapse = true;

            if (uncollapse && step.isUncollapsible()) {
                // uncollapse a single-step nested strategy
                step.setCollapsedName(null);
                step.setIsCollapsible(false);
                step.update(false);
            } else if (!step.getIsCollapsible()) {
                // collapse a step into a single-step nested strategy
                String branch = request.getParameter("collapsedName");
                if (branch == null || branch.length() == 0) {
                    throw new WdkModelException(
                            "No collapsed name given for newly expanded step!");
                }
                step.setIsCollapsible(true);
                step.setCollapsedName(branch);
                step.update(false);
            }
            String strategyKey = strategy.getStrategyId() + "_" + step.getStepId();
            wdkUser.addActiveStrategy(strategyKey);

            // Add branch (Step object) to request as strategy
            request.setAttribute(CConstants.WDK_STEP_KEY, step);
            request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

            // forward to strategyPage.jsp
            ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showSummary.getPath());
            url.append("?state=" + URLEncoder.encode(state, "UTF-8"));
            ActionForward forward = new ActionForward(url.toString());
            forward.setRedirect(false);
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }
}
