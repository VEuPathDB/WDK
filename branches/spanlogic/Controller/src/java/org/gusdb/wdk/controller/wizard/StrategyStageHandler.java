package org.gusdb.wdk.controller.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.controller.action.WizardAction;
import org.gusdb.wdk.controller.action.WizardForm;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class StrategyStageHandler implements StageHandler {

    private static final String PARAM_IMPORT_STRATEGY_ID = "insertStrategyId";

    public static final String ATTR_IMPORT_STEP = "importStep";
    public static final String ATTR_IMPORT_STRATEGY = "importStrategy";
    private static final String ATTR_ALLOW_BOOLEAN = "allowBoolean";

    private static final Logger logger = Logger.getLogger(StrategyStageHandler.class);

    public Map<String, Object> execute(ActionServlet servlet,
            HttpServletRequest request, HttpServletResponse response,
            WizardForm wizardForm) throws Exception {
        logger.debug("Entering StrategyStageHandler....");

        String strStrategyId = request.getParameter(PARAM_IMPORT_STRATEGY_ID);
        if (strStrategyId == null || strStrategyId.length() == 0)
            throw new WdkUserException("Required " + PARAM_IMPORT_STRATEGY_ID
                    + " parameter is missing");

        UserBean user = ActionUtility.getUser(servlet, request);
        int strategyId = Integer.parseInt(strStrategyId);
        StrategyBean strategy = user.getStrategy(strategyId);
        StepBean importStep = strategy.getLatestStep();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(ATTR_IMPORT_STEP, importStep);
        attributes.put(ATTR_IMPORT_STRATEGY, strategy);

        // need to check if the boolean is allowed
        String action = wizardForm.getAction();
        StepBean currentStep = (StepBean) request.getAttribute(WizardAction.ATTR_STEP);
        StepBean inputStep;
        if (action.equals(WizardForm.ACTION_ADD)) {
            // add, the current step is the last step of a strategy or a
            // sub-strategy, use it as the input;
            inputStep = currentStep;
        } else { // revise or insert,
            // the current step is always the lower step in the graph, no
            // matter whether it's a boolean, or a combined step. Use the
            // previous step as the input.
            inputStep = currentStep.getPreviousStep();
        }
        // check if boolean is allowed
        String importType = importStep.getType();
        boolean allowBoolean = importType.equals(inputStep.getType());
        attributes.put(ATTR_ALLOW_BOOLEAN, allowBoolean);

        logger.debug("Leaving StrategyStageHandler....");
        return attributes;
    }

}
