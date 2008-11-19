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
    private static final Logger logger = Logger.getLogger(ExpandStepAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ExpandStepAction...");

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
        StepBean latestStep;
        if (strBranchId == null) {
            latestStep = strategy.getLatestStep();
        } else {
            latestStep = strategy.getStepById(Integer.parseInt(strBranchId));
        }

        StepBean step = latestStep.getStepByDisplayId(Integer.parseInt(strStepId));
        System.out.println("Target step: " + strStepId);
        System.out.println("Actual step: " + step.getStepId());
        System.out.println("Step collapsed name: " + step.getCollapsedName());
        System.out.println("Step collapsible? : " + step.getIsCollapsible());

        if (step.getParentStep() == null) {
            throw new WdkModelException("Only top-row steps can be expanded!");
        }

        if (!step.getIsCollapsible()) {
            String branch = request.getParameter("collapsedName");
            if (branch == null || branch.length() == 0) {
                throw new WdkModelException(
                        "No collapsed name given for newly expanded step!");
            }
            step.setIsCollapsible(true);
            step.setCollapsedName(branch);
            step.update(false);
        }

        // Add branch (Step object) to request as strategy
        request.setAttribute(CConstants.WDK_STEP_KEY, step);
        request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

        // forward to strategyPage.jsp
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?strategy=" + URLEncoder.encode(strStratId, "UTF-8"));
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strBranchId, "UTF-8"));
        }
        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(false);
        return forward;

    }
}
