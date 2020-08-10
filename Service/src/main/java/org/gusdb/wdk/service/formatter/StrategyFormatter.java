package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.rSwallow;
import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StrategyFormatter {

  public static JSONArray getStrategiesJson(Collection<Strategy> strategies) {
    return reduce(strategies, rSwallow(
        (strategiesJson, strategy) -> strategiesJson.put(getListingStrategyJson(strategy))
    ), new JSONArray());
  }

  public static JSONObject getListingStrategyJson(Strategy strategy) throws JSONException {
    Optional<String> recordClassName = strategy.getRecordClass().map(RecordClass::getUrlSegment);
    return new JSONObject()
        .put(JsonKeys.STRATEGY_ID, strategy.getStrategyId())
        .put(JsonKeys.DESCRIPTION, Optional.ofNullable(strategy.getDescription()).orElse(""))
        .put(JsonKeys.NAME, strategy.getName())
        .put(JsonKeys.AUTHOR, strategy.getUser().getDisplayName())
        .put(JsonKeys.ROOT_STEP_ID, strategy.getRootStepId())
        .put(JsonKeys.RECORD_CLASS_NAME, recordClassName.isPresent() ? recordClassName.get() :JSONObject.NULL)
        .put(JsonKeys.SIGNATURE, strategy.getSignature())
        .put(JsonKeys.CREATED_TIME, FormatUtil.formatDateTime(strategy.getCreatedTime()))
        .put(JsonKeys.LAST_VIEW_TIME, FormatUtil.formatDateTime(strategy.getLastViewTime()))
        .put(JsonKeys.LAST_MODIFIED, FormatUtil.formatDateTime(strategy.getLastModifiedTime()))
        .put(JsonKeys.RELEASE_VERSION, strategy.getVersion())
        .put(JsonKeys.IS_PUBLIC, strategy.isPublic())
        .put(JsonKeys.IS_SAVED, strategy.isSaved())
        .put(JsonKeys.IS_VALID, strategy.isValid())
        .put(JsonKeys.IS_DELETED, strategy.isDeleted())
        .put(JsonKeys.IS_EXAMPLE, strategy.isExample())
        .put(JsonKeys.ORGANIZATION, strategy.getUser().getProfileProperties().get("organization"))
        .put(JsonKeys.ESTIMATED_SIZE, StepFormatter.translateEstimatedSize(strategy.getEstimatedSize()))
        .put(JsonKeys.NAME_OF_FIRST_STEP, strategy.getMostPrimaryLeafStep().getDisplayName())
        .put(JsonKeys.LEAF_AND_TRANSFORM_STEP_COUNT, strategy.getLeafAndTransformStepCount());
  }

  public static JSONObject getDetailedStrategyJson(Strategy strategy) throws WdkModelException, JSONException {
    Set<Step> stepsInTree = new HashSet<Step>(); // accumulate the steps found in the tree
    JSONObject stepTreeJson = StepFormatter.formatAsStepTree(strategy.getRootStep(), stepsInTree);
    JSONObject stepDetailsMap = new JSONObject();
    for (Step step : stepsInTree) {
      stepDetailsMap.put(Long.toString(step.getStepId()), StepFormatter.getStepJsonWithEstimatedSize(step));
    }
    return getListingStrategyJson(strategy)
        .put(JsonKeys.STEP_TREE, stepTreeJson)
        .put(JsonKeys.STEPS, stepDetailsMap)
        .put(JsonKeys.ESTIMATED_SIZE, strategy.getResultSize()); // overwrite with real size
  }

}
