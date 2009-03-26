package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        try {
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
                request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                        wdkUser);
            }

            if (strStratId.indexOf("_") > 0) {
                strBranchId = strStratId.split("_")[1];
                strStratId = strStratId.split("_")[0];
            }

            StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

	    wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));

            if (strBranchId == null) {
                request.setAttribute(CConstants.WDK_STEP_KEY,
                        strategy.getLatestStep());
            } else {
                request.setAttribute(CConstants.WDK_STEP_KEY,
                        strategy.getStepById(Integer.parseInt(strBranchId)));
            }
            request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

            String output = request.getParameter("output");
            if (output != null && output.equals("xml")) {
                // forward to strategyPage.jsp
                ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
                StringBuffer url = new StringBuffer(showSummary.getPath());
                url.append("?strategy="
                        + URLEncoder.encode(strStratId, "utf-8"));
                if (strBranchId != null) {
                    url.append("_" + URLEncoder.encode(strBranchId, "utf-8"));
                }
                String viewStep = request.getParameter("step");
                if (viewStep != null && viewStep.length() != 0) {
                    url.append("&step=" + URLEncoder.encode(viewStep, "utf-8"));
                }
                String subQuery = request.getParameter("subquery");
                if (subQuery != null && subQuery.length() != 0) {
                    url.append("&subquery="
                            + URLEncoder.encode(subQuery, "utf-8"));
                }

                logger.debug("URL: " + url);

                ActionForward forward = new ActionForward(url.toString());
                forward.setRedirect(false);
                return forward;
            } else {    // by default, JSON output
                outputStrategyJSON(strategy, response);
                return null;
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            outputErrorJSON(ex, response);
            return null;
        }
    }

    static void outputErrorJSON(Exception ex, HttpServletResponse response)
            throws JSONException, IOException {
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", "error");
        jsMessage.put("exception", ex.getClass().getName());
        jsMessage.put("message", ex.getMessage());

        if (ex instanceof WdkModelException) {
            WdkModelException wmex = (WdkModelException) ex;
            Map<Param, String[]> paramexs = wmex.getBooBoos();
            if (paramexs != null) {
                JSONObject jsParams = new JSONObject();
                for (Param param : paramexs.keySet()) {
                    JSONObject jsParam = new JSONObject();
                    jsParam.put("name", param.getName());
                    jsParam.put("prompt", param.getPrompt());
                    JSONArray jsMessages = new JSONArray();
                    for (String message : paramexs.get(param)) {
                        jsMessages.put(message);
                    }
                    jsParam.put("messages", jsMessages);
                    jsParams.put(param.getName(), jsParam);
                }
                jsParams.put("length", paramexs.size());
                jsMessage.put("params", jsParams);
            }
        }

        // export the stack trace
        StringWriter buffer = new StringWriter();
        ex.printStackTrace(new PrintWriter(buffer));
        jsMessage.put("stackTrace", buffer.toString());
        buffer.close();

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static void outputStrategyJSON(StrategyBean strategy,
            HttpServletResponse response) throws JSONException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException, IOException {
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", "strategy");

        // get a list of strategy checksums
        UserBean user = strategy.getUser();
        JSONObject jsStrategies = new JSONObject();
        for (StrategyBean strat : user.getOpenedStrategies()) {
            int stratId = strat.getStrategyId();
            jsStrategies.put(Integer.toString(stratId), strat.getChecksum());
        }
        jsMessage.put("strategies", jsStrategies);
        jsMessage.put("strategy", outputStrategy(strategy));

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static private JSONObject outputStrategy(StrategyBean strategy)
            throws JSONException, NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException {
        JSONObject jsStrategy = new JSONObject();
        jsStrategy.put("name", strategy.getName());
        jsStrategy.put("id", Integer.toString(strategy.getStrategyId()));
        jsStrategy.put("saved", strategy.getIsSaved());
        jsStrategy.put("savedName", strategy.getSavedName());
        jsStrategy.put("importId", strategy.getImportId());

        JSONObject jsSteps = new JSONObject();
        StepBean step = strategy.getFirstStep();
        int frontId = 1;
        while (step != null) {
            JSONObject jsStep = outputStep(step, strategy.getStrategyId(),
                    false);
            jsSteps.put(Integer.toString(frontId), jsStep);
            step = step.getNextStep();
            frontId++;
        }
        jsSteps.put("length", (frontId - 1));
        jsStrategy.put("steps", jsSteps);
        return jsStrategy;
    }

    static private JSONObject outputStep(StepBean step, int strategyId,
            boolean showSubStrategy) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        JSONObject jsStep = new JSONObject();
        jsStep.put("name", step.getCustomName());
        jsStep.put("customName", step.getCustomName());
        jsStep.put("id", Integer.toString(step.getStepId()));
        jsStep.put("answerId", step.getAnswerId());
        jsStep.put("isCollapsed", step.getIsCollapsible());
        jsStep.put("dataType", step.getDataType());
        jsStep.put("shortName", step.getShortDisplayName());
        jsStep.put("results", step.getResultSize());
        jsStep.put("questionName", step.getQuestionName());
        jsStep.put("displayName",
                step.getAnswerValue().getQuestion().getDisplayName());
        jsStep.put("isboolean", step.getIsBoolean());
        jsStep.put("istransform", step.getIsTransform());
        jsStep.put("filtered", step.isFiltered());
        jsStep.put("filterName", step.getFilterDisplayName());
        jsStep.put("urlParams", step.getAnswerValue().getQuestionUrlParams());

        // determine the types of the step
        if (showSubStrategy && step.getIsCollapsible()) {
            outputSubStrategy(step, jsStep, strategyId);
        } else if (step.getIsBoolean()) {
            outputBooleanStep(step, jsStep, strategyId);
        } else { // both transform and normal steps
            outputNormalStep(step, jsStep);
        }

        return jsStep;
    }

    static private void outputBooleanStep(StepBean step, JSONObject jsStep,
            int strategyId) throws NoSuchAlgorithmException, JSONException,
            WdkModelException, WdkUserException, SQLException {
        jsStep.put("operation", step.getOperation());

        StepBean childStep = step.getChildStep();
        jsStep.put("step", outputStep(childStep, strategyId, true));
    }

    static private void outputNormalStep(StepBean step, JSONObject jsStep)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException, SQLException {

        JSONArray jsParams = new JSONArray();
        Map<String, ParamBean> params = step.getAnswerValue().getQuestion().getParamsMap();
        Map<String, String> paramValues = step.getParams();
        for (String paramName : paramValues.keySet()) {
            ParamBean param = params.get(paramName);
            JSONObject jsParam = new JSONObject();
            jsParam.put("name", paramName);
            jsParam.put("prompt", param.getPrompt());
            jsParam.put("value", paramValues.get(paramName));
            jsParam.put("visible", param.getIsVisible());
            jsParam.put("className", param.getClass().getName());
            jsParams.put(jsParam);
        }
        jsStep.put("params", jsParams);
    }

    static private void outputSubStrategy(StepBean step, JSONObject jsStep,
            int strategyId) throws NoSuchAlgorithmException, JSONException,
            WdkModelException, WdkUserException, SQLException {
        JSONObject jsStrategy = new JSONObject();

        jsStrategy.put("name", step.getCollapsedName());
        jsStrategy.put("id", strategyId + "_" + step.getStepId());
        jsStrategy.put("saved", "false");
        jsStrategy.put("savedName", step.getCollapsedName());
        jsStrategy.put("importId", "");

        // repeat the step again
        JSONObject jsSteps = new JSONObject();
        StepBean subStep = step.getFirstStep();
        int frontId = 1;
        while (subStep != null) {
            JSONObject jsSubStep = outputStep(subStep, strategyId, false);
            jsSteps.put(Integer.toString(frontId), jsSubStep);
            subStep = subStep.getNextStep();
            frontId++;
        }
        jsSteps.put("length", (frontId - 1));
        jsStrategy.put("steps", jsSteps);

        jsStep.put("strategy", jsStrategy);
    }
}
