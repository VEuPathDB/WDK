package org.gusdb.wdk.controller.action;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.wizard.Stage;
import org.gusdb.wdk.model.wizard.StageHandler;
import org.gusdb.wdk.model.wizard.Wizard;
import org.gusdb.wdk.model.wizard.WizardModel;
import org.json.JSONException;

public class WizardAction extends Action {

    private static final String PARAM_WIZARD = "wizard";
    private static final String PARAM_STAGE = "stage";
    private static final String PARAM_STRATEGY = "strategy";
    private static final String PARAM_STEP = "step";

    private static final String FORWARD_SHOW_WIZARDS = "show_wizards";

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

        // load strategy
        loadStrategy(request, user);

        WizardModel wizardModel = wdkModel.getWizardModel();

        // get the wizard
        String wizardName = request.getParameter(PARAM_WIZARD);
        if (wizardName == null || wizardName.length() == 0) {
            // no wizard specified, then show wizard list
            ActionForward forward = mapping.findForward(FORWARD_SHOW_WIZARDS);
            return forward;
        }

        Map<String, String> params = ActionUtility.getParams(request);

        Wizard wizard = wizardModel.getWizard(wizardName);

        // get the current stage, or use the first stage
        String stageName = request.getParameter(PARAM_STAGE);
        Stage stage;
        if (stageName == null || stageName.length() == 0) stage = wizard.getFirstStage();
        else stage = wizard.getStage(stageName);

        // check if there is a handler
        StageHandler handler = stage.getHandler();
        if (handler != null) {
            Map<String, Object> result = handler.execute(wdkModel.getModel(),
                    user.getUser(), params);
            for (String name : result.keySet()) {
                request.setAttribute(name, result.get(name));
            }
        }

        // get the view from the stage
        String view = stage.getView();

        return new ActionForward(view);
    }

    private void loadStrategy(HttpServletRequest request, UserBean user)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        String strStratId = request.getParameter(PARAM_STRATEGY);
        if (strStratId == null || strStratId.length() == 0)
            throw new WdkUserException("The strategy is not passed in");
        int stratId = Integer.valueOf(strStratId);
        StrategyBean strategy = user.getStrategy(stratId);

        String strStepId = request.getParameter(PARAM_STEP);
        StepBean step;
        if (strStepId == null || strStepId.length() == 0) {
            // get the last step from the strategy
            step = strategy.getLatestStep();
        } else {
            int stepId = Integer.valueOf(strStepId);
            step = strategy.getStepById(stepId);
        }

        request.setAttribute(PARAM_STRATEGY, strategy);
        request.setAttribute(PARAM_STEP, step);
    }
}
