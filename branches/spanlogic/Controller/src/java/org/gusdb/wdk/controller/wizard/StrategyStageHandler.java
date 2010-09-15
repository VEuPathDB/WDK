package org.gusdb.wdk.controller.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.controller.action.WizardForm;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class StrategyStageHandler implements StageHandler {

    private static final String PARAM_INSERT_STRATEGY_ID = "insertStrategy";

    private static final String ATTR_INSERT_STEP = "insertStep";

    private static final Logger logger = Logger.getLogger(StrategyStageHandler.class);

    public Map<String, Object> execute(ActionServlet servlet, HttpServletRequest request,
            HttpServletResponse response, WizardForm wizardForm)
            throws Exception {
        logger.debug("Entering StrategyStageHandler....");

        String strStrategyId = request.getParameter(PARAM_INSERT_STRATEGY_ID);
        if (strStrategyId == null || strStrategyId.length() == 0)
            throw new WdkUserException("Required " + PARAM_INSERT_STRATEGY_ID
                    + " parameter is missing");

        UserBean user = ActionUtility.getUser(servlet, request);
        int strategyId = Integer.parseInt(strStrategyId);
        StrategyBean strategy = user.getStrategy(strategyId);
        StepBean step = strategy.getLatestStep();
        
        Map<String, Object> results = new HashMap<String, Object>();
        results.put(ATTR_INSERT_STEP, step);

        logger.debug("Leaving StrategyStageHandler....");
        
        return results;
    }

}
