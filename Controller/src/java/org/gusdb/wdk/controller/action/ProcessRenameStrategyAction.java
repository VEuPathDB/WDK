/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.ArrayList;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author ctreatma
 * 
 */
public class ProcessRenameStrategyAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessRenameStrategyAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
        String customName = request.getParameter("name");
        boolean checkName = Boolean.valueOf(request.getParameter("checkName")).booleanValue();
        // TEST
        if (customName == null || customName.length() == 0) {
            throw new Exception("No name was given for saving Strategy.");
        }
        logger.info("Set custom name: '" + customName + "'");
        if (strStratId == null || strStratId.length() == 0) {
            throw new Exception("No Strategy was given for saving");
        }

        int stratId = Integer.parseInt(strStratId);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);

        StrategyBean strategy = wdkUser.getStrategy(stratId);
        strategy.setName(customName);
        strategy.setIsSaved(true);

        ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();
        int index = -1;

        if (checkName) {
            if (wdkUser.checkNameExists(strategy, customName)) {
                // if we've been asked to check the user-specified name, and it
                // already exists, do
                // nothing. we need to confirm that the user wants to overwrite
                // the existing strategy
                return null;
            }
        }

        if (activeStrategies != null
                && activeStrategies.contains(new Integer(
                        strategy.getStrategyId()))) {
            index = activeStrategies.indexOf(new Integer(
                    strategy.getStrategyId()));
            activeStrategies.remove(index);
        }

        strategy.update(true);

        if (activeStrategies != null && index >= 0) {
            activeStrategies.add(index, new Integer(strategy.getStrategyId()));
        }
        wdkUser.setActiveStrategies(activeStrategies);

        request.setAttribute(CConstants.WDK_STEP_KEY, strategy.getLatestStep());
        request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

        // forward to strategyPage.jsp
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?strategy="
                + URLEncoder.encode(Integer.toString(strategy.getStrategyId()),
                        "UTF-8"));
        String viewStep = request.getParameter("step");
        if (viewStep != null && viewStep.length() != 0) {
            url.append("&step=" + URLEncoder.encode(viewStep, "UTF-8"));
        }
        String subQuery = request.getParameter("subquery");
        if (subQuery != null && subQuery.length() != 0) {
            url.append("&subquery=" + URLEncoder.encode(subQuery, "UTF-8"));
        }
        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(false);
        return forward;
    }

}
