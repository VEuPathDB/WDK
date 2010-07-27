package org.gusdb.wdk.controller.action;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
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

	private static final Logger logger = Logger.getLogger(WizardAction.class);

    private static final String PARAM_WIZARD = "wizard";
    private static final String PARAM_STAGE = "stage";
    private static final String PARAM_LABEL = "label";
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

        // get the wizard
        String wizardName = request.getParameter(PARAM_WIZARD);
		logger.info("wizardname = " + wizardName);
        if (wizardName == null || wizardName.length() == 0) {
            // no wizard specified, then show wizard list
            ActionForward forward = mapping.findForward(FORWARD_SHOW_WIZARDS);
            return forward;
        }

        Stage nextStage = getNextStage(request, wdkModel, wizardName);
		logger.info("nextStage = "+nextStage.getName());
        Map<String, String> params = ActionUtility.getParams(request);

        // check if there is a handler
        StageHandler handler = nextStage.getHandler();
        if (handler != null) {
            Map<String, Object> result = handler.execute(wdkModel.getModel(),
                    user.getUser(), params);
            for (String name : result.keySet()) {
                request.setAttribute(name, result.get(name));
            }
        }
        // get the view from the stage
        String view = nextStage.getView();
		logger.info("view = " + view);
		String qfn = request.getParameter("questionFullName");
		if(qfn != null && qfn.length() != 0){
			logger.info("qfn = " + wdkModel.getQuestion(qfn).getName());
			request.setAttribute("wdkQuestion", wdkModel.getQuestion(qfn));
		}
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

    private Stage getNextStage(HttpServletRequest request,
            WdkModelBean wdkModel, String wizardName) throws WdkModelException {
        WizardModel wizardModel = wdkModel.getWizardModel();

        Wizard wizard = wizardModel.getWizard(wizardName);
		request.setAttribute("wizard", wizard);
        // get the name of the current stage
        String stageName = request.getParameter(PARAM_STAGE);
        // if the current stage is not specified, the next stage would be the
        // first stage, simply return it.
        if (stageName == null || stageName.length() == 0){
            request.setAttribute("stage", wizard.getFirstStage());
			return wizard.getFirstStage();
		}
        // get the current stage
        Stage stage = wizard.getStage(stageName);
		request.setAttribute("stage", stage);
        // get the label, which should map to the next stage
        String label = request.getParameter(PARAM_LABEL);
        Stage nextStage = stage.queryNextStage(label);
        if (nextStage == null)
            throw new WdkModelException("stage '" + stage.getName()
                    + " in wizard " + wizard.getName() + " doesn't have a "
                    + "next stage matching the label '" + label + "'");
        return nextStage;
    }
}
