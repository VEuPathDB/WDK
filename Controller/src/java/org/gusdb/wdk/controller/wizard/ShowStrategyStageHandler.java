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

public class ShowStrategyStageHandler implements StageHandler {

    private static final String PARAM_IMPORT_STRATEGY = "insertStrategy";

    public static final String ATTR_IMPORT_STEP = "importStep";
    public static final String ATTR_IMPORT_STRATEGY = "importStrategy";
    private static final String ATTR_ALLOW_BOOLEAN = "allowBoolean";

    private static final Logger logger = Logger.getLogger(ShowStrategyStageHandler.class);

    public Map<String, Object> execute(ActionServlet servlet,
            HttpServletRequest request, HttpServletResponse response,
            WizardForm wizardForm) throws Exception {
        logger.debug("Entering StrategyStageHandler....");

        String strStrategyId = request.getParameter(PARAM_IMPORT_STRATEGY);
        if (strStrategyId == null || strStrategyId.length() == 0)
            throw new WdkUserException("Required " + PARAM_IMPORT_STRATEGY
                    + " parameter is missing");

        UserBean user = ActionUtility.getUser(servlet, request);
        int strategyId = Integer.parseInt(strStrategyId);
        StrategyBean strategy = user.getStrategy(strategyId);
        StepBean childStep = strategy.getLatestStep();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(ATTR_IMPORT_STEP, childStep);
        attributes.put(ATTR_IMPORT_STRATEGY, strategy);

        // need to check if the boolean is allowed
        StepBean previousStep = StageHandlerUtility.getPreviousStep(servlet,
                request, wizardForm);

        // insert strategy before first step. use current step (the first step
        // as childStep, and the root step of the imported strategy as previous
        // step
        if (previousStep == null)
            previousStep = StageHandlerUtility.getCurrentStep(request);

        // check if boolean is allowed
        String childType = childStep.getType();
        boolean allowBoolean = childType.equals(previousStep.getType());
        attributes.put(ATTR_ALLOW_BOOLEAN, allowBoolean);

        logger.debug("Leaving StrategyStageHandler....");
        return attributes;
    }

}
