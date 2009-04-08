/**
 * 
 */
package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * 
 */
public class ProcessRenameStepAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessRenameStepAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessRenameStepAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        try {
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String customName = request.getParameter("customName");

            // check the input
            if (customName == null || customName.length() == 0) {
                throw new Exception("No name was given for step.");
            }
            if (strStratId == null || strStratId.length() == 0) {
                throw new Exception("No Strategy was given for saving");
            }

            int stratId = Integer.parseInt(strStratId);
            StrategyBean strategy = wdkUser.getStrategy(stratId);

            // verify the checksum
            String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
            if (checksum != null && !strategy.getChecksum().equals(checksum)) {
                ShowStrategyAction.outputOutOfSyncJSON(strategy, response);
                return null;
            }

            String stepIdstr = request.getParameter("stepId");

            // TEST
            logger.info("Set custom name: '" + customName + "'");
            if (stepIdstr != null) {
                int stepId = Integer.parseInt(stepIdstr);
                    StepBean step = strategy.getStepById(stepId);
                    if (step.getIsCollapsible()) {
                        step.setCollapsedName(customName);
                    } else {
                        step.setCustomName(customName);
                    }
                    step.update(false);
            } else {
                throw new Exception("no step id is given for update");
            }

            request.setAttribute(CConstants.WDK_STEP_KEY,
                    strategy.getLatestStep());
            request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            return showStrategy;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, ex, response);
            return null;
        }
    }

}
