package org.gusdb.wdk.controller.action;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONException;

/**
 * This Action is process boolean expression on queryStep.jsp page.
 * 
 */

public class ProcessBooleanExpressionAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessBooleanExpressionAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            BooleanExpressionForm beForm = (BooleanExpressionForm) form;
            String strategyIdStr = processBooleanExpression(request, beForm);

            ActionForward fwd = mapping.findForward(CConstants.PROCESS_BOOLEAN_EXPRESSION_MAPKEY);
            String path = fwd.getPath();
            if (path.indexOf("?") > 0) {
                if (path.indexOf(CConstants.WDK_STRATEGY_ID_KEY) < 0) {
                    path += "&" + CConstants.WDK_STRATEGY_ID_KEY + "="
                            + strategyIdStr;
                }
            } else {
                path += "?" + CConstants.WDK_STRATEGY_ID_KEY + "="
                        + strategyIdStr;
            }

            return new ActionForward(path);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private String processBooleanExpression(HttpServletRequest request,
            BooleanExpressionForm beForm) throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        String expression = beForm.getBooleanExpression();
        boolean useBooleanFilter = beForm.isUseBooleanFilter();

        StepBean step = wdkUser.combineStep(expression, useBooleanFilter);
        StrategyBean strategy = wdkUser.createStrategy(step, false);
        int strategyId = strategy.getStrategyId();

        logger.info("Boolean Expression: " + expression);
        logger.info("Use Boolean Filter: " + useBooleanFilter);

        return Integer.toString(strategyId);
    }
}
