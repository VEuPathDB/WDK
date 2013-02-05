package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.GroupBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
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
    static final String MESSAGE_TYPE_DUP_NAME_ERROR = "dup-name-error";
    static final String MESSAGE_TYPE_GENERAL_ERROR = "general-error";

    static final int TRUNCATE_LENGTH = 200;

    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowStrategyAction...");

        response.setContentType("application/json");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String strStratKeys = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String[] stratKeys = (strStratKeys == null || strStratKeys.length() == 0) ? new String[0]
                    : strStratKeys.split(",");

            String strOpen = request.getParameter(CConstants.WDK_OPEN_KEY);
            boolean open = (strOpen == null || strOpen.length() == 0) ? true
                    : Boolean.parseBoolean(strOpen);
            StrategyBean currentStrategy = (StrategyBean) request.getAttribute(CConstants.WDK_STRATEGY_KEY);

            Map<Integer, StrategyBean> displayStrategies;
            if (currentStrategy != null) {
                // this case is directly from showSummaryAction, where one step
                // is invalid
                displayStrategies = new LinkedHashMap<Integer, StrategyBean>();
                displayStrategies.put(currentStrategy.getStrategyId(),
                        currentStrategy);
            } else if (open) {
                // open all the requested strategies
                for (String strategyKey : stratKeys) {
                    wdkUser.addActiveStrategy(strategyKey);
                }
                String state = request.getParameter(CConstants.WDK_STATE_KEY);
                displayStrategies = getModifiedStrategies(wdkUser, state);

                // set the highlight to the last opened strategy
                if (stratKeys.length > 0) {
                    String stratKey = stratKeys[stratKeys.length - 1];
                    int pos = stratKey.indexOf('_');
                    if (pos >= 0) stratKey = stratKey.substring(0, pos);
                    int stratId = Integer.parseInt(stratKey);
                    StrategyBean strategy = displayStrategies.get(stratId);
                    if (strategy != null && strategy.isValid()) {
                        int stepId = strategy.getLatestStep().getStepId();
                        wdkUser.setViewResults(stratKey, stepId, 0);
                    }
                }
            } else {
                // return the details of all the requested strategies; skip the
                // state validation
                displayStrategies = new LinkedHashMap<Integer, StrategyBean>();
                for (String strategyKey : stratKeys) {
                    int pos = strategyKey.indexOf('_');
                    if (pos >= 0) strategyKey = strategyKey.substring(0, pos);
                    int strategyId = Integer.parseInt(strategyKey);
                    StrategyBean strategy = wdkUser.getStrategy(strategyId);
                    displayStrategies.put(strategyId, strategy);
                }
            }
            outputSuccessJSON(wdkModel, wdkUser, response, displayStrategies);
            logger.debug("Leaving ShowStrategyAction...");
            return null;

        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            outputErrorJSON(wdkUser, response, ex);
            logger.debug("Leaving ShowStrategyAction...");
            return null;
        }
    }

    private static Map<Integer, StrategyBean> getModifiedStrategies(
            UserBean user, String state) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        logger.debug("previous state: '" + state + "'");

        if (state == null || state.length() == 0) state = null;
        JSONObject jsState = (state == null) ? new JSONObject()
                : new JSONObject(state);
        String[] keys = JSONObject.getNames(jsState);
        Map<Integer, String> oldState = new LinkedHashMap<Integer, String>();
        if (keys != null) {
            for (String key : keys) {
                if (key.equals("length") || key.equals("count")) continue;
                JSONObject jsStrategy = jsState.getJSONObject(key);
                int strategyId = jsStrategy.getInt("id");
                String checksum = jsStrategy.getString("checksum");
                oldState.put(strategyId, checksum);
            }
        }
        Map<Integer, StrategyBean> strategies = new LinkedHashMap<Integer, StrategyBean>();
        for (StrategyBean strategy : user.getActiveStrategies()) {
            int strategyId = strategy.getStrategyId();
            if (!oldState.containsKey(strategyId)) {
                strategies.put(strategyId, strategy);
            } else {
                //String oldChecksum = oldState.get(strategy.getStrategyId());
                //String newChecksum = strategy.getChecksum();
                // if (!newChecksum.equals(oldChecksum)) {
                    strategies.put(strategyId, strategy);
                // }
            }
        }
        return strategies;
    }

    public static void outputErrorJSON(UserBean user,
            HttpServletResponse response, Exception ex) throws JSONException,
            IOException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        logger.debug("output JSON error message: " + ex);

        JSONObject jsMessage = new JSONObject();
        outputCommon(user, jsMessage);

        jsMessage.put("exception", ex.getClass().getName());
        jsMessage.put("message", ex.getMessage());

        if (ex instanceof WdkModelException) {
            WdkModelException wmex = (WdkModelException) ex;
            Map<String, String> paramexs = wmex.getParamErrors();
            if (paramexs != null) {
                JSONObject jsParams = new JSONObject();
                for (String prompt : paramexs.keySet()) {
                    String message = paramexs.get(prompt);
                    jsParams.put(prompt, message);
                }
                jsParams.put("length", paramexs.size());
                jsMessage.put("params", jsParams);
                jsMessage.put("type", MESSAGE_TYPE_PARAM_ERROR);
            } else {
                jsMessage.put("type", MESSAGE_TYPE_GENERAL_ERROR);
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

    public static void outputOutOfSyncJSON(WdkModelBean model, UserBean user,
            HttpServletResponse response, String state) throws JSONException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException, IOException {
        logger.debug("output JSON out-of-sync message...");

        Map<Integer, StrategyBean> strategies = getModifiedStrategies(user,
                state);

        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_OUT_OF_SYNC_ERROR);

        // get a list of strategy checksums
        outputCommon(user, jsMessage);
        outputStrategies(model, user, jsMessage, strategies);

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    public static void outputDuplcicateNameJSON(WdkModelBean model,
            UserBean user, HttpServletResponse response, String state)
            throws JSONException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, IOException {
        logger.debug("output JSON dup-name-error message...");

        Map<Integer, StrategyBean> strategies = getModifiedStrategies(user,
                state);

        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_DUP_NAME_ERROR);

        // get a list of strategy checksums
        outputCommon(user, jsMessage);
        outputStrategies(model, user, jsMessage, strategies);

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static private void outputSuccessJSON(WdkModelBean model, UserBean user,
            HttpServletResponse response,
            Map<Integer, StrategyBean> displayStrategies) throws JSONException,
            NoSuchAlgorithmException, WdkUserException, WdkModelException,
            SQLException, IOException {
        logger.debug("output JSON success message without strategy");

        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_SUCCESS);

        // get a list of strategy checksums
        outputCommon(user, jsMessage);
        outputStrategies(model, user, jsMessage, displayStrategies);

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static private void outputCommon(UserBean user, JSONObject jsMessage)
            throws JSONException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        outputState(user, jsMessage);
        outputCurrentView(user, jsMessage);
    }

    static void outputState(UserBean user, JSONObject jsMessage)
            throws WdkUserException, WdkModelException, JSONException,
            SQLException, NoSuchAlgorithmException {
        JSONObject jsState = new JSONObject();
        StrategyBean[] openedStrategies = user.getActiveStrategies();

        logger.debug("PRINTING STATE for " + openedStrategies.length + " strategies");
   
        for (int order = 0; order < openedStrategies.length; order++) {
            StrategyBean strat = openedStrategies[order];

            logger.debug("#" + strat.getStrategyId() + " - " + strat.getChecksum());

            int stratId = strat.getStrategyId();
            JSONObject jsStrategy = new JSONObject();
            jsStrategy.put("id", stratId);
            jsStrategy.put("checksum", strat.getChecksum());
            // System.out.println("ID: " + stratId);
            // System.out.println("Checksum: " + strat.getChecksum());
            jsState.put(Integer.toString(order + 1), jsStrategy);
        }
        jsState.put("length", openedStrategies.length);
        jsState.put("count", user.getStrategyCount());
        jsMessage.put("state", jsState);
    }

    static private void outputCurrentView(UserBean user, JSONObject jsMessage)
            throws JSONException {
        JSONObject jsView = new JSONObject();
        String viewStrategyKey = user.getViewStrategyId();
        if (viewStrategyKey != null) {
            jsView.put("strategy", viewStrategyKey);
            int viewStep = user.getViewStepId();
            if (viewStep != 0) jsView.put("step", viewStep);
            jsView.put("pagerOffset", user.getViewPagerOffset());
        }
        String frontAction = user.getFrontAction();
        if (frontAction != null) {
            jsView.put("action", frontAction);
            jsView.put("actionStrat", user.getFrontStrategy());
            jsView.put("actionStep", user.getFrontStep());
        }
        user.resetFrontAction();
        jsMessage.put("currentView", jsView);
    }

    static private void outputStrategies(WdkModelBean model, UserBean user,
            JSONObject jsMessage, Map<Integer, StrategyBean> strategies)
            throws JSONException, NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException {
        JSONObject jsStrategies = new JSONObject();

        logger.debug("PRINTING DETAIL for " + strategies.size() + " strategies");

        for (StrategyBean strategy : strategies.values()) {

            logger.debug("#" + strategy.getStrategyId() + " - " + strategy.getChecksum());

            JSONObject jsStrategy = outputStrategy(model, user, strategy);
            System.out.println("ID: " + strategy.getStrategyId());
            System.out.println("Checksum: " + strategy.getChecksum());
            jsStrategies.put(strategy.getChecksum(), jsStrategy);
        }
        jsStrategies.put("length", strategies.size());
        jsMessage.put("strategies", jsStrategies);
    }

    static private JSONObject outputStrategy(WdkModelBean model, UserBean user,
            StrategyBean strategy) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        JSONObject jsStrategy = new JSONObject();
        jsStrategy.put("name", strategy.getName());
        jsStrategy.put("id", Integer.toString(strategy.getStrategyId()));
        jsStrategy.put("saved", strategy.getIsSaved());
        jsStrategy.put("savedName", strategy.getSavedName());
        jsStrategy.put("description", strategy.getDescription());
        jsStrategy.put("importId", strategy.getImportId());
        jsStrategy.put("isValid", strategy.isValid());

        JSONObject jsSteps = new JSONObject();
        StepBean step = strategy.getFirstStep();
        int frontId = 1;
        int nonTransformLength = 0;
        while (step != null) {
            if (!step.getIsTransform()) nonTransformLength++;
            JSONObject jsStep = outputStep(model, user, step,
                    strategy.getStrategyId(), false);
            jsSteps.put(Integer.toString(frontId), jsStep);
            step = step.getNextStep();
            frontId++;
        }
        jsSteps.put("length", (frontId - 1));
        jsSteps.put("nonTransformLength", nonTransformLength);
        jsStrategy.put("steps", jsSteps);
        return jsStrategy;
    }

    static JSONObject outputStep(WdkModelBean model, UserBean user,
            StepBean step, int strategyId, boolean showSubStrategy)
            throws JSONException, NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException {
        JSONObject jsStep = new JSONObject();
        jsStep.put("name", step.getDisplayName());
        jsStep.put("customName", step.getCustomName());
        jsStep.put("id", step.getStepId());
        jsStep.put("answerId", step.getAnswerId());
        // the root of the sub-strategy should not be collapsed
        jsStep.put("isCollapsed", step.getIsCollapsible() && showSubStrategy);
        jsStep.put("isUncollapsible", step.isUncollapsible());
        jsStep.put("dataType", step.getRecordClass().getFullName());
        jsStep.put("displayType", step.getRecordClass().getDisplayName());
        jsStep.put("shortDisplayType", step.getRecordClass().getShortDisplayName());
        jsStep.put("displayTypePlural", step.getRecordClass().getDisplayNamePlural());
        jsStep.put("shortDisplayTypePlural", step.getRecordClass().getShortDisplayNamePlural());
        jsStep.put("shortName", step.getShortDisplayName());
        jsStep.put("results", step.getEstimateSize());
        jsStep.put("questionName", step.getQuestionName());
        jsStep.put("displayName", step.getDisplayName());
        jsStep.put("isboolean", step.getIsBoolean());
        jsStep.put("istransform", step.getIsTransform());
        jsStep.put("filtered", step.isFiltered());
        jsStep.put("filterName", step.getFilterDisplayName());
        jsStep.put("urlParams", step.getQuestionUrlParams());
        jsStep.put("isValid", step.getIsValid());
        jsStep.put("validationMessage", step.getValidationMessage());
        jsStep.put("assignedWeight", step.getAssignedWeight());
        jsStep.put("useweights", model.getUseWeights());
        jsStep.put("revisable", step.isRevisable());

        jsStep.put("frontId", step.getFrontId());

        outputParams(user, step, jsStep);

        // determine the types of the step
        if (showSubStrategy && step.getIsCollapsible()) {
            outputSubStrategy(model, user, step, jsStep, strategyId);
        } else if (step.isCombined()) {
            outputCombinedStep(model, user, step, jsStep, strategyId);
        }

        return jsStep;
    }

    static private void outputCombinedStep(WdkModelBean wdkModel,
            UserBean user, StepBean step, JSONObject jsStep, int strategyId)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException, SQLException {
        int childrenCount = step.getAnswerParamCount();
        jsStep.put("childrenCount", childrenCount);
        if (step.getIsBoolean()) jsStep.put("operation", step.getOperation());

        if (childrenCount > 1) {
            StepBean childStep = step.getChildStep();
            jsStep.put("step", outputStep(wdkModel, user, childStep,
                    strategyId, true));
        }
    }

    static private void outputParams(UserBean user, StepBean step,
            JSONObject jsStep) throws NoSuchAlgorithmException, JSONException,
            WdkModelException, WdkUserException, SQLException {

        JSONArray jsParams = new JSONArray();
        try {
        Map<GroupBean, Map<String, ParamBean<?>>> groups = step.getQuestion().getParamMapByGroups();
        Map<String, String> paramValues = step.getParams();
        for (GroupBean group : groups.keySet()) {
            Map<String, ParamBean<?>> params = groups.get(group);
            for (String paramName : params.keySet()) {
            	ParamBean<?> param = params.get(paramName);
                String dependentValue = getUserDependentValue(paramValues, param);
                JSONObject jsParam = new JSONObject();
                jsParam.put("name", paramName);
                if (param != null) {
                    jsParam.put("prompt", param.getPrompt());
                    jsParam.put("visible", param.getIsVisible());
                    jsParam.put("className", param.getClass().getName());
                    param.setDependentValue(dependentValue);
                    param.setUser(user);
                    param.setTruncateLength(TRUNCATE_LENGTH);
                    try {
                        jsParam.put("value", getRawValue(paramValues, param));
                        jsParam.put("internal",param.getRawValue());
                    } catch (Exception ex) {
                        throw new WdkModelException(ex);
                    }
                } else {
                    jsParam.put("value", dependentValue);
                }
                jsParams.put(jsParam);
            }
        }
        } catch(WdkModelException ex) {
            // ignore the invalid question name
        }
        jsStep.put("params", jsParams);
    }

    private static String getRawValue(Map<String, String> paramValues, ParamBean<?> param)
    		throws WdkUserException, WdkModelException {
        if (param instanceof EnumParamBean) {
        	EnumParamBean enumParam = (EnumParamBean)param;
        	if (enumParam.isDependentParam()) {
        		enumParam.setDependedValue(getUserDependentValue(paramValues, enumParam.getDependedParam()));
        	}
            return enumParam.getRawDisplayValue();
        }
        return param.getBriefRawValue();
	}

	private static String getUserDependentValue(Map<String, String> paramValues, ParamBean<?> param)
    		throws WdkUserException, WdkModelException {
    	    if (paramValues.containsKey(param.getName())) {
    	        return paramValues.get(param.getName());
            } else {
                if (param instanceof EnumParamBean) {
                    EnumParamBean enumParam = (EnumParamBean)param;
                    if (enumParam.isDependentParam()) {
                        enumParam.setDependedValue(getUserDependentValue(paramValues, enumParam.getDependedParam()));
                    }
                }
                return param.getDefault();
            }
	}

	static private void outputSubStrategy(WdkModelBean model, UserBean user,
            StepBean step, JSONObject jsStep, int strategyId)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException, SQLException {
        JSONObject jsStrategy = new JSONObject();
        String subStratId = strategyId + "_" + step.getStepId();
        Integer order = user.getStrategyOrder(subStratId);
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
        int nonTransformLength = 0;
        while (subStep != null) {
            if (!subStep.getIsTransform()) nonTransformLength++;
            JSONObject jsSubStep = outputStep(model, user, subStep, strategyId,
                    false);
            jsSteps.put(Integer.toString(frontId), jsSubStep);
            subStep = subStep.getNextStep();
            frontId++;
        }
        jsSteps.put("length", (frontId - 1));
        jsSteps.put("nonTransformLength", nonTransformLength);
        jsStrategy.put("steps", jsSteps);

        jsStep.put("strategy", jsStrategy);
    }
}
