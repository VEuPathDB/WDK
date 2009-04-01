package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This Action handles loading a search strategy from the database. It loads the
 * strategy and forwards to a simple jsp for diplaying the strategy
 */

public class ShowStrategyAction extends ShowQuestionAction {
    
    static final String MESSAGE_TYPE_SUCCESS = "success";
    static final String MESSAGE_TYPE_PARAM_ERROR = "param-error";
    static final String MESSAGE_TYPE_OUT_OF_SYNC_ERROR = "out-of-sync";
    static final String MESSAGE_TYPE_GENERAL_ERROR = "general-error";
    
    static final int TRUNCATE_LENGTH = 200;
    
    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowStrategyAction...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);

        try {
            // Make sure a protocol is specified
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String strBranchId = null;

            if (strStratId == null || strStratId.length() == 0) {
                outputSuccessJSON(wdkUser, response);
                return null;
            }

            if (strStratId.indexOf("_") > 0) {
                strBranchId = strStratId.split("_")[1];
                strStratId = strStratId.split("_")[0];
            }

            StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

            wdkUser.addActiveStrategy(Integer.toString(strategy.getStrategyId()));

            if (strBranchId != null)
                wdkUser.addActiveStrategy(strStratId + "_" + strBranchId);

            String output = request.getParameter("output");
            if (output != null && output.equals("xml")) {
                ActionForward forward = outputStrategyXML(strategy,
                        strBranchId, request, mapping);
                return forward;
            } else { // by default, JSON output
                outputSuccessJSON(strategy, response);
                return null;
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            outputErrorJSON(wdkUser, ex, response);
            return null;
        }
    }

    static void outputErrorJSON(UserBean user, Exception ex,
            HttpServletResponse response) throws JSONException, IOException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException {
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("exception", ex.getClass().getName());
        jsMessage.put("message", ex.getMessage());
        jsMessage.put("strategies", outputStrategyChecksums(user));

        if (ex instanceof WdkModelException) {
            WdkModelException wmex = (WdkModelException) ex;
            Map<Param, String[]> paramexs = wmex.getBooBoos();
            if (paramexs != null) {
                JSONObject jsParams = new JSONObject();
                for (Param param : paramexs.keySet()) {
                    JSONObject jsParam = new JSONObject();
                    jsParam.put("name", param.getName());
                    jsParam.put("prompt", param.getPrompt());
                    String[] messages = paramexs.get(param);
                    String message = Utilities.fromArray(messages, "; ");
                    jsParam.put("message", message);
                    jsParams.put(param.getName(), jsParam);
                }
                jsParams.put("length", paramexs.size());
                jsMessage.put("params", jsParams);
                jsMessage.put("type", MESSAGE_TYPE_PARAM_ERROR);
            }
        } else {
            jsMessage.put("type", MESSAGE_TYPE_GENERAL_ERROR);
        }

        // export the stack trace
        StringWriter buffer = new StringWriter();
        ex.printStackTrace(new PrintWriter(buffer));
        jsMessage.put("stackTrace", buffer.toString());
        buffer.close();

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static void outputOutOfSyncJSON(StrategyBean strategy,
            HttpServletResponse response) throws JSONException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException, IOException {
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_OUT_OF_SYNC_ERROR);

        // get a list of strategy checksums
        UserBean user = strategy.getUser();
        jsMessage.put("strategies", outputStrategyChecksums(user));
        jsMessage.put("strategy", outputStrategy(user, strategy));

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    private static ActionForward outputStrategyXML(StrategyBean strategy,
            String strBranchId, HttpServletRequest request,
            ActionMapping mapping) throws UnsupportedEncodingException {
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
        url.append("?strategy=" + strategy.getStrategyId());
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strBranchId, "utf-8"));
        }
        String viewStep = request.getParameter("step");
        if (viewStep != null && viewStep.length() != 0) {
            url.append("&step=" + URLEncoder.encode(viewStep, "utf-8"));
        }
        String subQuery = request.getParameter("subquery");
        if (subQuery != null && subQuery.length() != 0) {
            url.append("&subquery=" + URLEncoder.encode(subQuery, "utf-8"));
        }

        logger.debug("URL: " + url);

        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(false);
        return forward;
    }

    static void outputSuccessJSON(UserBean user,
            HttpServletResponse response) throws JSONException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException, IOException {
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_SUCCESS);

        // get a list of strategy checksums
        jsMessage.put("strategies", outputStrategyChecksums(user));

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static void outputSuccessJSON(StrategyBean strategy,
            HttpServletResponse response) throws JSONException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException, IOException {
        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_SUCCESS);

        // get a list of strategy checksums
        UserBean user = strategy.getUser();
        jsMessage.put("strategies", outputStrategyChecksums(user));
        jsMessage.put("strategy", outputStrategy(user, strategy));

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static private JSONObject outputStrategyChecksums(UserBean user)
            throws WdkUserException, WdkModelException, JSONException,
            SQLException, NoSuchAlgorithmException {
        JSONObject jsStrategies = new JSONObject();
        StrategyBean[] openedStrategies = user.getOpenedStrategies();
        for (StrategyBean strat : openedStrategies) {
            int stratId = strat.getStrategyId();
            jsStrategies.put(Integer.toString(stratId), strat.getChecksum());
        }
        jsStrategies.put("length", openedStrategies.length);
        return jsStrategies;
    }

    static private JSONObject outputStrategy(UserBean user,
            StrategyBean strategy) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
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
            JSONObject jsStep = outputStep(user, step,
                    strategy.getStrategyId(), false);
            jsSteps.put(Integer.toString(frontId), jsStep);
            step = step.getNextStep();
            frontId++;
        }
        jsSteps.put("length", (frontId - 1));
        jsStrategy.put("steps", jsSteps);
        return jsStrategy;
    }

    static private JSONObject outputStep(UserBean user, StepBean step,
            int strategyId, boolean showSubStrategy) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        JSONObject jsStep = new JSONObject();
        jsStep.put("name", step.getCustomName());
        jsStep.put("customName", step.getCustomName());
        jsStep.put("id", step.getStepId());
        jsStep.put("answerId", step.getAnswerId());
        // the root of the sub-strategy should not be collapsed
        jsStep.put("isCollapsed", step.getIsCollapsible() && showSubStrategy);
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
            outputSubStrategy(user, step, jsStep, strategyId);
        } else if (step.getIsBoolean()) {
            outputBooleanStep(user, step, jsStep, strategyId);
        } else { // both transform and normal steps
            outputNormalStep(user, step, jsStep);
        }

        return jsStep;
    }

    static private void outputBooleanStep(UserBean user, StepBean step,
            JSONObject jsStep, int strategyId) throws NoSuchAlgorithmException,
            JSONException, WdkModelException, WdkUserException, SQLException {
        jsStep.put("operation", step.getOperation());

        StepBean childStep = step.getChildStep();
        jsStep.put("step", outputStep(user, childStep, strategyId, true));
    }

    static private void outputNormalStep(UserBean user, StepBean step, JSONObject jsStep)
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
            jsParam.put("visible", param.getIsVisible());
            jsParam.put("className", param.getClass().getName());
            String dependentValue = paramValues.get(paramName);
            param.setDependentValue(dependentValue);
            param.setUser(user);
            param.setTruncateLength(TRUNCATE_LENGTH);
            jsParam.put("value", param.getBriefRawValue());
            jsParams.put(jsParam);
        }
        jsStep.put("params", jsParams);
    }

    static private void outputSubStrategy(UserBean user, StepBean step,
            JSONObject jsStep, int strategyId) throws NoSuchAlgorithmException,
            JSONException, WdkModelException, WdkUserException, SQLException {
        JSONObject jsStrategy = new JSONObject();
        String subStratId = strategyId + "_" + step.getStepId();
        Integer order = user.getActiveStrategies().get(subStratId);
        if (order == null) order = 0; // the sub-strategy is not displayed

        jsStrategy.put("name", step.getCollapsedName());
        jsStrategy.put("id", subStratId);
        jsStrategy.put("saved", "false");
        jsStrategy.put("savedName", step.getCollapsedName());
        jsStrategy.put("importId", "");
        jsStrategy.put("order", order);

        // repeat the step again
        JSONObject jsSteps = new JSONObject();
        StepBean subStep = step.getFirstStep();
        int frontId = 1;
        while (subStep != null) {
            JSONObject jsSubStep = outputStep(user, subStep, strategyId, false);
            jsSteps.put(Integer.toString(frontId), jsSubStep);
            subStep = subStep.getNextStep();
            frontId++;
        }
        jsSteps.put("length", (frontId - 1));
        jsStrategy.put("steps", jsSteps);

        jsStep.put("strategy", jsStrategy);
    }
}
