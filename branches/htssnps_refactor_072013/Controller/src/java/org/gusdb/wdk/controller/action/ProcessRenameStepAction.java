/**
 * 
 */
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
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

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
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
	    //String strBranchId = null;
            String customName = request.getParameter("customName");

            // check the input
            if (customName == null || customName.length() == 0) {
                throw new Exception("No name was given for step.");
            }
            if (strStratId == null || strStratId.length() == 0) {
                throw new Exception("No Strategy was given for saving");
            }

            if (strStratId.indexOf("_") > 0) {
                //strBranchId = strStratId.split("_")[1];
                strStratId = strStratId.split("_")[0];
            }

            int stratId = Integer.parseInt(strStratId);
            StrategyBean strategy = wdkUser.getStrategy(stratId);

            // verify the checksum
            String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
            if (checksum != null && !strategy.getChecksum().equals(checksum)) {
                ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
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
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

            ActionForward forward = new ActionForward(url.toString());
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }

}
