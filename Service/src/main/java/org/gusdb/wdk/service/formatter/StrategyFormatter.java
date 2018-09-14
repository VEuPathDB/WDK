package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.rSwallow;
import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StrategyFormatter {

  public static JSONArray getStrategiesJson(List<Strategy> strategies, boolean loadEstimateSize) {
    return reduce(strategies, rSwallow(
        (strategiesJson, strategy) -> strategiesJson.put(getStrategyJson(strategy, loadEstimateSize))
    ), new JSONArray());
  }

  public static JSONObject getStrategyJson(Strategy strategy, boolean loadEstimateSize) throws WdkModelException, JSONException {
    return new JSONObject() 
        .put(JsonKeys.STRATEGY_ID, strategy.getStrategyId())
        .put(JsonKeys.DESCRIPTION, strategy.getDescription())
        .put(JsonKeys.NAME, strategy.getName())
        .put(JsonKeys.AUTHOR, strategy.getUser().getDisplayName())
        .put(JsonKeys.LATEST_STEP_ID, strategy.getLatestStepId())
        .put(JsonKeys.RECORD_CLASS_NAME_PLURAL, strategy.getLatestStep().getQuestion().getRecordClass().getDisplayNamePlural())
        .put(JsonKeys.SIGNATURE, strategy.getSignature())
        .put(JsonKeys.LAST_MODIFIED, strategy.getLastModifiedTime())
        .put(JsonKeys.IS_PUBLIC, strategy.getIsPublic())
        .put(JsonKeys.IS_SAVED, strategy.getIsSaved())
        .put(JsonKeys.IS_VALID, strategy.isValid())
        .put(JsonKeys.IS_DELETED, strategy.isDeleted())
        .put(JsonKeys.IS_PUBLIC, strategy.getIsPublic())
        .put(JsonKeys.ORGANIZATION, strategy.getUser().getProfileProperties().get("organization"))
        .put(JsonKeys.ROOT_STEP, getStepsJson(strategy.getLatestStep(), loadEstimateSize));
  }
  
  protected static JSONObject getStepsJson(Step step, boolean loadEstimateSize) throws WdkModelException, JSONException {
	if(step == null) return new JSONObject();
    return StepFormatter.getStepJson(step, loadEstimateSize)
    		.put(JsonKeys.LEFT_STEP, getStepsJson(step.getPreviousStep(), loadEstimateSize))
    		.put(JsonKeys.RIGHT_STEP, getStepsJson(step.getChildStep(), loadEstimateSize));
  }
}
