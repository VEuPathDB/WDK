package org.gusdb.wdk.controller.wizard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.controller.action.QuestionForm;
import org.gusdb.wdk.controller.action.ShowQuestionAction;
import org.gusdb.wdk.controller.action.WizardForm;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class StrategyStageHandler implements StageHandler {

    private static final String PARAM_ADD_STRATEGY_ID = "addStrategy";

    private static final String ATTR_STEP = "step";

    private static final Logger logger = Logger.getLogger(StrategyStageHandler.class);

    public void execute(ActionServlet servlet, HttpServletRequest request,
            HttpServletResponse response, WizardForm wizardForm)
            throws Exception {
        logger.debug("Entering StrategyStageHandler....");

        String strStrategyId = request.getParameter(PARAM_ADD_STRATEGY_ID);
        if (strStrategyId == null || strStrategyId.length() == 0)
            throw new WdkUserException("Required " + PARAM_ADD_STRATEGY_ID
                    + " parameter is missing");

        UserBean user = ActionUtility.getUser(servlet, request);
        int strategyId = Integer.parseInt(strStrategyId);
        StrategyBean strategy = user.getStrategy(strategyId);
        StepBean step = strategy.getLatestStep();

        request.setAttribute(ATTR_STEP, step);

        logger.debug("Leaving StrategyStageHandler....");
    }

}
