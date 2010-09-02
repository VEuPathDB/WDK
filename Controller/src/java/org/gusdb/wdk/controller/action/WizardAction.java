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
import org.gusdb.wdk.controller.wizard.Result;
import org.gusdb.wdk.controller.wizard.Stage;
import org.gusdb.wdk.controller.wizard.StageHandler;
import org.gusdb.wdk.controller.wizard.Wizard;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;

public class WizardAction extends Action {

    private static final Logger logger = Logger.getLogger(WizardAction.class);

    private static final String PARAM_STAGE = "stage";
    private static final String PARAM_STRATEGY = "strategy";
    private static final String PARAM_STEP = "step";

    private static final String ATTR_STRATEGY = "wdkStrategy";
    private static final String ATTR_STEP = "wdkStep";

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.struts.action.Action#execute(org.apache.struts.action.
     * ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        UserBean user = ActionUtility.getUser(servlet, request);
        WizardForm wizardForm = (WizardForm) form;

        // load strategy
        loadStrategy(request, wizardForm, user);

        // get stage
        Wizard wizard = (Wizard) servlet.getServletContext().getAttribute(
                CConstants.WDK_WIZARD_KEY);
        String stageName = request.getParameter(PARAM_STAGE);
        Stage stage;
        if (stageName == null || stageName.length() == 0) {
            stage = wizard.getDefaultStage();
            stageName = stage.getName();
        } else {
            stage = wizard.queryStage(stageName);
        }
        logger.info("stage: " + stageName);


        // check if there is a handler
        StageHandler handler = stage.getHandler();
        if (handler != null) {
            handler.execute(servlet, request, response, wizardForm);
        }

        Result result = stage.getResult();
        String type = result.getType();
        if (type.equals(Result.TYPE_VIEW)) {
            // forward to a jsp.
            String view = result.getText();
            logger.debug("wizard view: " + view);
            return new ActionForward(result.getText());
        } else if (type.equals(Result.TYPE_ACTION)) {
            // forward to an action
            String className = result.getText();
            logger.debug("wizard forward to action: " + className);
            Class<Action> actionClass = (Class<Action>) Class.forName(className);
            Action action = actionClass.newInstance();
            return action.execute(mapping, form, request, response);
        } else {
            throw new WdkModelException("Invalid result type: " + type);
        }
    }

    private void loadStrategy(HttpServletRequest request,
            WizardForm wizardForm, UserBean user) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            SQLException {
        int stratId = wizardForm.getStrategy();
        if (stratId == 0)
            throw new WdkUserException("The required strategy id is missing");
        StrategyBean strategy = user.getStrategy(stratId);

        int stepId = wizardForm.getStep();
        StepBean step;
        if (stepId == 0) {
            // get the last step from the strategy
            step = strategy.getLatestStep();
        } else {
            step = strategy.getStepById(stepId);
        }

        request.setAttribute(ATTR_STRATEGY, strategy);
        request.setAttribute(ATTR_STEP, step);
    }
}
