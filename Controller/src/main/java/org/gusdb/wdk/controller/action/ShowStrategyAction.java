package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.gusdb.wdk.model.filter.FilterOption;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.GroupBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.StepUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This Action handles loading a search strategy from the database. It loads the
 * strategy and forwards to a simple JSP for displaying the strategy
 */

public class ShowStrategyAction extends ShowQuestionAction {

    static final String MESSAGE_TYPE_SUCCESS = "success";
    static final String MESSAGE_TYPE_PARAM_ERROR = "param-error";
    static final String MESSAGE_TYPE_OUT_OF_SYNC_ERROR = "out-of-sync";
    static final String MESSAGE_TYPE_DUP_NAME_ERROR = "dup-name-error";
    static final String MESSAGE_TYPE_GENERAL_ERROR = "general-error";
    static final String PARAM_UPDATE_RESULTS = "updateResults";

    static final int TRUNCATE_LENGTH = 200;

    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowStrategyAction...");

        response.setContentType("application/json");

        UserBean wdkUser = ActionUtility.getUser(request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String strStratKeys = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            List<String> stratKeys = (strStratKeys == null || strStratKeys.length() == 0) ?
                new ArrayList<>() : Arrays.asList(strStratKeys.split(","));

            boolean updateResults = Boolean.parseBoolean(
                    request.getParameter(PARAM_UPDATE_RESULTS));
            
            String strOpen = request.getParameter(CConstants.WDK_OPEN_KEY);
            boolean open = (strOpen == null || strOpen.length() == 0) ? true
                    : Boolean.parseBoolean(strOpen);
            StrategyBean currentStrategy = (StrategyBean) request.getAttribute(CConstants.WDK_STRATEGY_KEY);

            Map<Long, StrategyBean> displayStrategies;
            if (currentStrategy != null) {
                logger.info("OPEN single strategy...");
                // this case is directly from showSummaryAction, where one step
                // is invalid
                displayStrategies = new LinkedHashMap<>();
                // reload the current strategy to make sure all the data is up to date.
                displayStrategies.put(currentStrategy.getStrategyId(),
                    new StrategyBean(wdkUser, StepUtilities.getStrategy(wdkUser.getUser(), currentStrategy.getStrategyId())));
            }
            else if (open) {
                logger.info("OPEN requested strategies...");
                // first, see which strategies can be opened; do not make strat active if
                //   exception thrown during opening will prevent panel from even being displayed
                // FIXME: THIS CODE DOES NOT WORK; HERE AS PLACEHOLDER- maybe we will fix before this action is purged or maybe not
                //TwoTuple<List<Strategy>,List<String>> openableResult = StepUtilities.getOpenableStrategies(wdkModel.getModel(), stratKeys);
                //stratKeys = filter(stratKeys, key -> !openableResult.getSecond().contains(key));

                // TODO: we already have the openable strategies above- USE THEM rather than load again
                // open all the requested strategies
                for (String strategyKey : stratKeys) {
                    wdkUser.addActiveStrategy(strategyKey);
                }
                String state = request.getParameter(CConstants.WDK_STATE_KEY);
                displayStrategies = getModifiedStrategies(wdkUser, state);

                // set the highlight to the last opened strategy
                if (!stratKeys.isEmpty()) {
                    String stratKey = stratKeys.get(stratKeys.size() - 1);
                    int pos = stratKey.indexOf('_');
                    if (pos >= 0) stratKey = stratKey.substring(0, pos);
                    long stratId = Long.parseLong(stratKey);
                    StrategyBean strategy = displayStrategies.get(stratId);
                    if (strategy != null && strategy.isValid()) {
                        long stepId = strategy.getLatestStep().getStepId();
                        wdkUser.getUser().getSession().setViewResults(stratKey, stepId, 0);
                    }
                }
            }
            else {
                logger.info("GET all strategies...");
                // return the details of all the requested strategies; skip the
                // state validation
                displayStrategies = new LinkedHashMap<>();
                for (String strategyKey : stratKeys) {
                    int pos = strategyKey.indexOf('_');
                    if (pos >= 0) strategyKey = strategyKey.substring(0, pos);
                    long strategyId = Long.parseLong(strategyKey);
                    StrategyBean strategy = new StrategyBean(wdkUser, StepUtilities.getStrategy(wdkUser.getUser(), strategyId));
                    displayStrategies.put(strategyId, strategy);

                }
            }
            outputSuccessJSON(wdkModel, wdkUser, response, displayStrategies, updateResults);
            logger.debug("Leaving ShowStrategyAction...");
            return null;

        } catch (Exception ex) {
            logger.error("Error trying to retrieve strategy", ex);
            outputErrorJSON(wdkUser, response, ex);
            logger.debug("Leaving ShowStrategyAction...");
            return null;
        }
    }

    private static Map<Long, StrategyBean> getModifiedStrategies(
            UserBean user, String state) throws JSONException, WdkUserException, WdkModelException {
        logger.debug("previous state: '" + state + "'");

        if (state == null || state.length() == 0) state = null;
        
        // In newer versions of supported tomcats, braces must be escaped to avoid a
        // security vulnerability.  Hence the query string is encoded.  Therefore, we need
        // to decode the string representing the JSON object.
        JSONObject jsState = null;
        try {
          jsState = (state == null) ? new JSONObject()
                  : new JSONObject(URLDecoder.decode(state, "UTF-8"));
        }
        catch(UnsupportedEncodingException uee) {
          throw new WdkModelException(uee);
        }
        
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
        Map<Long, StrategyBean> strategies = new LinkedHashMap<>();
        for (StrategyBean strategy : user.getActiveStrategies()) {
            long strategyId = strategy.getStrategyId();
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
            IOException, WdkUserException,
            WdkModelException {
        logger.debug("output JSON error message: " + ex);

        response.setHeader("Content-Type", "application/json");

        JSONObject jsMessage = new JSONObject();
        outputCommon(user, jsMessage);

        jsMessage.put("exception", ex.getClass().getName());
        jsMessage.put("message", ex.getMessage());

        if (ex instanceof WdkUserException) {
            WdkUserException wmex = (WdkUserException) ex;
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

        response.setHeader("Content-Type", "application/json");

        Map<Long, StrategyBean> strategies = getModifiedStrategies(user, state);

        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_OUT_OF_SYNC_ERROR);

        // get a list of strategy checksums
        outputStrategies(model, user, jsMessage, strategies, false);
        outputCommon(user, jsMessage);

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    public static void outputDuplicateNameJSON(WdkModelBean model,
            UserBean user, HttpServletResponse response, String state, boolean isDupPublic)
            throws JSONException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, IOException {
        logger.debug("output JSON dup-name-error message...");

        response.setHeader("Content-Type", "application/json");

        Map<Long, StrategyBean> strategies = getModifiedStrategies(user, state);

        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_DUP_NAME_ERROR);
        jsMessage.put("isPublicDup", isDupPublic);

        // get a list of strategy checksums
        outputStrategies(model, user, jsMessage, strategies, false);
        outputCommon(user, jsMessage);

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static private void outputSuccessJSON(WdkModelBean model, UserBean user,
            HttpServletResponse response, Map<Long,
            StrategyBean> displayStrategies, boolean updateResults) throws
            JSONException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, IOException {
        logger.debug("output JSON success message without strategy");

        response.setHeader("Content-Type", "application/json");

        JSONObject jsMessage = new JSONObject();
        jsMessage.put("type", MESSAGE_TYPE_SUCCESS);

        // get a list of strategy checksums
        outputStrategies(model, user, jsMessage, displayStrategies, updateResults);
        outputCommon(user, jsMessage);

        PrintWriter writer = response.getWriter();
        writer.print(jsMessage.toString());
    }

    static private void outputCommon(UserBean user, JSONObject jsMessage)
            throws JSONException, WdkUserException,
            WdkModelException {
        outputState(user, jsMessage);
        outputCurrentView(user, jsMessage);
    }

    static void outputState(UserBean user, JSONObject jsMessage)
            throws WdkUserException, WdkModelException, JSONException {
        JSONObject jsState = new JSONObject();
        StrategyBean[] openedStrategies = user.getActiveStrategies();

        logger.debug("PRINTING STATE for " + openedStrategies.length + " strategies");
   
        for (int order = 0; order < openedStrategies.length; order++) {
            StrategyBean strat = openedStrategies[order];
            String checksum = strat.getChecksum();
            logger.debug("#" + strat.getStrategyId() + " - " + checksum);

            long stratId = strat.getStrategyId();
            JSONObject jsStrategy = new JSONObject();
            jsStrategy.put("id", stratId);
            jsStrategy.put("checksum", checksum);
            // System.out.println("ID: " + stratId);
            // System.out.println("Checksum: " + checksum);
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
            long viewStep = user.getViewStepId();
            if (viewStep != 0) jsView.put("step", viewStep);
            jsView.put("pagerOffset", user.getViewPagerOffset());
        }
        String frontAction = user.getFrontAction();
        if (frontAction != null) {
            jsView.put("action", frontAction);
            jsView.put("actionStrat", user.getFrontStrategy());
            jsView.put("actionStep", user.getFrontStep());
        }
        user.getUser().getSession().resetFrontAction();
        jsMessage.put("currentView", jsView);
    }

    static private void outputStrategies(WdkModelBean model, UserBean user,
            JSONObject jsMessage, Map<Long, StrategyBean> strategies, boolean updateResults)
            throws JSONException, NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException {
        JSONObject jsStrategies = new JSONObject();

        logger.debug("PRINTING DETAIL for " + strategies.size() + " strategies");

        for (StrategyBean strategy : strategies.values()) {
            String checksum = strategy.getChecksum();
            logger.debug("#" + strategy.getStrategyId() + " - " + checksum);

            JSONObject jsStrategy = outputStrategy(model, user, strategy, updateResults);
            System.out.println("ID: " + strategy.getStrategyId());
            System.out.println("Checksum: " + checksum);
            jsStrategies.put(checksum, jsStrategy);
        }
        jsStrategies.put("length", strategies.size());
        jsMessage.put("strategies", jsStrategies);
    }

    static private JSONObject outputStrategy(WdkModelBean model, UserBean user,
            StrategyBean strategy, boolean updateResults) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        JSONObject jsStrategy = new JSONObject();
        jsStrategy.put("name", strategy.getName());
        jsStrategy.put("id", Long.toString(strategy.getStrategyId()));
        jsStrategy.put("saved", strategy.getIsSaved());
        jsStrategy.put("savedName", strategy.getSavedName());
        jsStrategy.put("description", strategy.getDescription());
        jsStrategy.put("importId", strategy.getImportId());
        jsStrategy.put("isValid", strategy.isValid());
        jsStrategy.put("isPublic", strategy.getIsPublic());

        JSONObject jsSteps = new JSONObject();
        StepBean step = strategy.getFirstStep();
        int frontId = 1;
        int nonTransformLength = 0;
        while (step != null) {
            if (!step.getIsTransform()) nonTransformLength++;
            JSONObject jsStep = outputStep(model, user, step,
                    strategy.getStrategyId(), false, updateResults);
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
        StepBean step, long strategyId, boolean showSubStrategy, boolean updateResults)
            throws JSONException, NoSuchAlgorithmException, WdkModelException, WdkUserException, SQLException {

      JSONObject jsStep = new JSONObject();
      jsStep.put("name", step.getDisplayName());
      jsStep.put("customName", step.getCustomName());
      jsStep.put("id", step.getStepId());
      // the root of the sub-strategy should not be collapsed
      jsStep.put("isCollapsed", step.getIsCollapsible() && showSubStrategy);
      jsStep.put("isUncollapsible", step.isUncollapsible());
      jsStep.put("hasCompleteAnalyses", step.getHasCompleteAnalyses());

      // some properties depend on the existence of a valid question name in this model
      boolean isValidQuestion = false;
      QuestionBean question = null;
      RecordClassBean recordClass = null;
      try {
        question = step.getQuestion();
        recordClass = step.getRecordClass();
        isValidQuestion = true;
      }
      catch (WdkModelException e) { }

      if (isValidQuestion) {
        jsStep.put("dataType", recordClass.getFullName());
        jsStep.put("displayType", recordClass.getDisplayName());
        jsStep.put("shortDisplayType", recordClass.getShortDisplayName());
        jsStep.put("displayTypePlural", recordClass.getDisplayNamePlural());
        jsStep.put("shortDisplayTypePlural", recordClass.getShortDisplayNamePlural());
        jsStep.put("isAnalyzable", question.getStepAnalyses().size() > 0 ? true : false);
      }
      else {
        jsStep.put("dataType", "unknown");
        jsStep.put("displayType", "unknown");
        jsStep.put("shortDisplayType", "unknown");
        jsStep.put("displayTypePlural", "unknown");
        jsStep.put("shortDisplayTypePlural", "unknown");
        jsStep.put("invalidQuestion", "true");
      }

      jsStep.put("shortName", step.getShortDisplayName());
      jsStep.put("results", updateResults ? step.getResultSize() : step.getEstimateSize());
      jsStep.put("questionName", step.getQuestionName());
      jsStep.put("displayName", step.getDisplayName());
      jsStep.put("isboolean", step.getIsBoolean());
      jsStep.put("istransform", step.getIsTransform());
      jsStep.put("filtered", step.isFiltered());
      jsStep.put("filterName", step.getFilterDisplayName());
      // Add array of filter names for use in Edit Step dialog.
      //    If able, include filter display names for non-view-only step filters
      //    Otherwise, just display raw names of all filters; some info better than none
      FilterOptionList filterOptions = step.getFilterOptions();
      JSONArray stepFilterDisplayNames = new JSONArray();
      for (FilterOption option : filterOptions) {
        if (isValidQuestion) {
          if (!option.getFilter().getIsViewOnly()) {
            stepFilterDisplayNames.put(option.getFilter().getDisplay());
          }
        }
        else {
          stepFilterDisplayNames.put(option.getKey());
        }
      }
      jsStep.put("stepFilterDisplayNames", stepFilterDisplayNames);
      jsStep.put("urlParams", step.getQuestionUrlParams());
      jsStep.put("isValid", step.getIsValid());
      jsStep.put("validationMessage", // validation message deprecated
          "Strategy is invalid and cannot be loaded at this time");
      jsStep.put("assignedWeight", step.getAssignedWeight());
      jsStep.put("useweights", model.getUseWeights());
      jsStep.put("revisable", step.isRevisable());

      jsStep.put("frontId", step.getFrontId());

      outputParams(user, step, jsStep);

      // determine the types of the step
      if (showSubStrategy && step.getIsCollapsible()) {
        outputSubStrategy(model, user, step, jsStep, strategyId, updateResults);
      }
      else if (step.isCombined()) {
        outputCombinedStep(model, user, step, jsStep, strategyId, updateResults);
      }

      return jsStep;
    }

    static private void outputCombinedStep(WdkModelBean wdkModel,
            UserBean user, StepBean step, JSONObject jsStep, long strategyId, boolean updateResults)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException, SQLException {
        int childrenCount = step.getAnswerParamCount();
        jsStep.put("childrenCount", childrenCount);
        if (step.getIsBoolean()) jsStep.put("operation", step.getOperation());

        if (childrenCount > 1) {
            StepBean childStep = step.getChildStep();
            jsStep.put("step", outputStep(wdkModel, user, childStep,
                    strategyId, true, updateResults));
        }
    }

  static private void outputParams(UserBean user, StepBean step, JSONObject jsStep) throws
      WdkUserException, WdkModelException {

    JSONArray jsParams = new JSONArray();
    QuestionBean question;
    try {
      question = step.getQuestion();
    }
    catch (WdkModelException ex) {
      // ignore the invalid question name
      try {
        jsStep.put("params", jsParams);
      }
      catch (JSONException ex1) {
        throw new WdkModelException(ex);
      }
      return;
    }

    try {
      Map<GroupBean, Map<String, ParamBean<?>>> groups = question.getParamMapByGroups();
      Map<String, String> paramValues = step.getParams();
      for (GroupBean group : groups.keySet()) {
        Map<String, ParamBean<?>> params = groups.get(group);
        for (String paramName : params.keySet()) {
          ParamBean<?> param = params.get(paramName);
          String stableValue = getStableValue(paramValues, param);
          JSONObject jsParam = new JSONObject();
          jsParam.put("name", paramName);
          if (param != null) {
            jsParam.put("prompt", param.getPrompt());
            jsParam.put("visible", param.getIsVisible());
            jsParam.put("className", param.getClass().getName());
            param.setUser(user);
            param.setStableValue(stableValue);
            param.setTruncateLength(TRUNCATE_LENGTH);
            try {
              jsParam.put("value", getRawValue(paramValues, param));
              jsParam.put("internal", !param.getIsVisible());
              jsParam.put("display", param.getDisplayValue());
            }
            catch (Exception ex) {
							// instead of throwing exception we print eception in logs. 
							// the exception prevents WDK from loading the strategy (that needs to be revised)
							// but the straegy is considered opened by WDK, and this prevents the user from using the strategy interface

							// throw new WdkModelException(ex);
							logger.error( ex.getMessage(),ex );
							//step.setValid(false);
            }
          }
          else {
            jsParam.put("value", stableValue);
            jsParam.put("display", stableValue);
          }
          jsParams.put(jsParam);
        }
      }
      jsStep.put("params", jsParams);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }

    private static String getRawValue(Map<String, String> paramValues, ParamBean<?> param)
    		throws WdkUserException, WdkModelException {
        if (param instanceof EnumParamBean) {
        	EnumParamBean enumParam = (EnumParamBean)param;
        	if (enumParam.isDependentParam()) {
        	  Map<String, String> dependedValues = new LinkedHashMap<>();
        	  for (ParamBean<?> dependedParam : enumParam.getDependedParams()) {
        	    String dependedValue = getStableValue(paramValues, dependedParam);
        	    dependedValues.put(dependedParam.getName(), dependedValue);
        	  }
        	  enumParam.setContextValues(dependedValues);
        	}
            return enumParam.getRawDisplayValue();
        }
        return param.getBriefRawValue();
	}

	private static String getStableValue(Map<String, String> paramValues, ParamBean<?> param)
    		throws WdkUserException, WdkModelException {
    	    if (paramValues.containsKey(param.getName())) {
    	        return paramValues.get(param.getName());
            } else {
                if (param instanceof EnumParamBean) {
                    EnumParamBean enumParam = (EnumParamBean)param;
                    if (enumParam.isDependentParam()) {
                      Map<String, String> dependedValues = new LinkedHashMap<>();
                      for (ParamBean<?> dependedParam : enumParam.getDependedParams()) {
                        String dependedValue = getStableValue(paramValues, dependedParam);
                        dependedValues.put(dependedParam.getName(), dependedValue);
                      }
                      enumParam.setContextValues(dependedValues);
                    }
                }
                return param.getDefault();
            }
	}

	static private void outputSubStrategy(WdkModelBean model, UserBean user,
            StepBean step, JSONObject jsStep, long strategyId, boolean updateResults)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException, SQLException {
        JSONObject jsStrategy = new JSONObject();
        String subStratId = strategyId + "_" + step.getStepId();
        int order = user.getStrategyOrder(subStratId);

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
                    false, updateResults);
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
