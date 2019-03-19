package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.rSwallow;
import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StrategyFormatter {

  public static JSONArray getStrategiesJson(List<Strategy> strategies) {
    return reduce(strategies, rSwallow(
        (strategiesJson, strategy) -> strategiesJson.put(getListingStrategyJson(strategy))
    ), new JSONArray());
  }

  public static JSONObject getListingStrategyJson(Strategy strategy) throws JSONException {
    return new JSONObject()
        .put(JsonKeys.STRATEGY_ID, strategy.getStrategyId())
        .put(JsonKeys.DESCRIPTION, strategy.getDescription())
        .put(JsonKeys.NAME, strategy.getName())
        .put(JsonKeys.AUTHOR, strategy.getUser().getDisplayName())
        .put(JsonKeys.LATEST_STEP_ID, strategy.getRootStepId())
        .put(JsonKeys.RECORD_CLASS_NAME, strategy.getRecordClass().map(NamedObject::getFullName).orElse(null))
        .put(JsonKeys.SIGNATURE, strategy.getSignature())
        .put(JsonKeys.LAST_MODIFIED, strategy.getLastModifiedTime())
        .put(JsonKeys.IS_PUBLIC, strategy.isPublic())
        .put(JsonKeys.IS_SAVED, strategy.isSaved())
        .put(JsonKeys.IS_VALID, strategy.isValid())
        .put(JsonKeys.IS_DELETED, strategy.isDeleted())
        .put(JsonKeys.ORGANIZATION, strategy.getUser().getProfileProperties().get("organization"))
        .put(JsonKeys.ESTIMATED_SIZE, strategy.getEstimatedSize());
  }

  public static JSONObject getDetailedStrategyJson(Strategy strategy) throws WdkModelException, JSONException {
    Set<Step> stepsInTree = new HashSet<Step>(); // accumulate the steps found in the tree
    JSONObject stepTreeJson = StepFormatter.formatAsStepTree(strategy.getRootStep(), stepsInTree);
    JSONObject stepDetailsMap = new JSONObject();
    for (Step step : stepsInTree) stepDetailsMap.put(Long.toString(step.getStepId()), StepFormatter.getStepJsonWithEstimatedSize(step));
    
    return getListingStrategyJson(strategy)
        .put(JsonKeys.STEP_TREE, stepTreeJson)
        .put(JsonKeys.STEPS_IN_TREE, stepDetailsMap)
        .put(JsonKeys.ESTIMATED_SIZE, strategy.getResultSize()); // overwrite with real size
  }

}
