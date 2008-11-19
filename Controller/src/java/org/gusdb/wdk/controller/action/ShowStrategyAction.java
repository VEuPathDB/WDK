package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action handles loading a search strategy from the database. It loads the
 * strategy and forwards to a simple jsp for diplaying the strategy
 */

public class ShowStrategyAction extends ShowQuestionAction {
    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowStrategyAction...");

        // Make sure a protocol is specified
        String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
        String strBranchId = null;

        if (strStratId == null || strStratId.length() == 0) {
            throw new WdkModelException(
                    "No strategy was specified for loading!");
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

        if (activeStrategies == null) {
            activeStrategies = new ArrayList<Integer>();
        }
        if (!activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
            activeStrategies.add(0, new Integer(strategy.getStrategyId()));
        }
        wdkUser.setActiveStrategies(activeStrategies);

        if (strBranchId == null) {
            request.setAttribute(CConstants.WDK_STEP_KEY,
                    strategy.getLatestStep());
        } else {
            request.setAttribute(CConstants.WDK_STEP_KEY,
                    strategy.getStepById(Integer.parseInt(strBranchId)));
        }
        request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

        // forward to strategyPage.jsp
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?strategy=" + URLEncoder.encode(strStratId, "utf-8"));
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strStratId, "utf-8"));
        }
        String viewStep = request.getParameter("step");
        if (viewStep != null && viewStep.length() != 0) {
            url.append("&step=" + URLEncoder.encode(viewStep, "utf-8"));
        }
        String subQuery = request.getParameter("subquery");
        if (subQuery != null && subQuery.length() != 0) {
            url.append("&subquery=" + URLEncoder.encode(subQuery, "utf-8"));
        }
        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(false);
        return forward;
    }
}
