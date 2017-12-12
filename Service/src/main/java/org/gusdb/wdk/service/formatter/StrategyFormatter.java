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
        .put(Keys.STRATEGY_ID, strategy.getStrategyId())
        .put(Keys.DESCRIPTION, strategy.getDescription())
        .put(Keys.NAME, strategy.getName())
        .put(Keys.AUTHOR, strategy.getUser().getDisplayName())
        .put(Keys.LATEST_STEP_ID, strategy.getLatestStepId())
        .put(Keys.RECORD_CLASS_NAME_PLURAL, strategy.getLatestStep().getQuestion().getRecordClass().getDisplayNamePlural())
        .put(Keys.SIGNATURE, strategy.getSignature())
        .put(Keys.LAST_MODIFIED, strategy.getLastModifiedTime())
        .put(Keys.IS_PUBLIC, strategy.getIsPublic())
        .put(Keys.IS_SAVED, strategy.getIsSaved())
        .put(Keys.IS_VALID, strategy.isValid())
        .put(Keys.IS_DELETED, strategy.isDeleted())
        .put(Keys.IS_PUBLIC, strategy.getIsPublic())
        .put(Keys.ORGANIZATION, strategy.getUser().getProfileProperties().get("organization"))
        .put(Keys.ROOT_STEP, getStepsJson(strategy.getLatestStep(), loadEstimateSize));
  }
  
  protected static JSONObject getStepsJson(Step step, boolean loadEstimateSize) throws WdkModelException, JSONException {
	if(step == null) return new JSONObject();
    return StepFormatter.getStepJson(step, loadEstimateSize)
    		.put(Keys.LEFT_STEP, getStepsJson(step.getPreviousStep(), loadEstimateSize))
    		.put(Keys.RIGHT_STEP, getStepsJson(step.getChildStep(), loadEstimateSize));
  }
}
