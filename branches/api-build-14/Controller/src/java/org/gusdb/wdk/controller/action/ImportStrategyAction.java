package org.gusdb.wdk.controller.action;

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

public class ImportStrategyAction extends Action {
    private static final Logger logger = Logger.getLogger(ImportStrategyAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ImportStrategyAction...");

        // Change to importing by answer checksum
        String strategyKey = request.getParameter("strategy");

        if (strategyKey == null || strategyKey.length() == 0) {
            strategyKey = request.getParameter("s"); // try a shorter version
        }
        if (strategyKey == null || strategyKey.length() == 0) {
            throw new WdkModelException(
                    "No strategy key was specified for importing!");
        }

        // load model, user
        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        StrategyBean strategy = wdkUser.importStrategy(strategyKey);

        wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));

        // Add any substrategies to the active strategies
        addActiveSubstrategies(wdkUser, strategy.getStrategyId(),
                strategy.getLatestStep());

        /*
         * Charles Treatman 4/23/09 Add code here to set the
         * current_application_tab cookie so that user will go to the Run
         * Strategies tab after importing a strategy
         */
        ShowApplicationAction.setWdkTabStateCookie(request, response);

        ActionForward forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
        forward = new ActionForward(forward.getPath(), true);
        return forward;
    }

    private void addActiveSubstrategies(UserBean wdkUser, int strategyId,
            StepBean step) throws Exception {
        if (step == null) return;

        if (step.getIsCollapsible() && step.getParentStep() != null) {
            logger.debug("open sub-strategy: " + strategyId + "_"
                    + step.getStepId());
            wdkUser.addActiveStrategy(strategyId + "_"
                    + Integer.toString(step.getStepId()));
        }
        addActiveSubstrategies(wdkUser, strategyId, step.getPreviousStep());
        addActiveSubstrategies(wdkUser, strategyId, step.getChildStep());
    }
}
