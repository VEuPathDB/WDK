package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.query.spec.StepAnalysisFormSpec;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.model.user.analysis.StepAnalysisSupplementalParams;
import org.gusdb.wdk.service.formatter.param.ParamContainerFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StepAnalysisFormatter {

  public static JSONArray getStepAnalysisTypesJsonWithoutParams(Collection<StepAnalysis> stepAnalyses) {
    return reduce(stepAnalyses, (arr, next) -> arr.put(getStepAnalysisTypeJsonWithoutParams(next)), new JSONArray());
  }

  public static JSONObject getStepAnalysisTypeJsonWithParams(
      DisplayablyValid<StepAnalysisFormSpec> spec, ValidationBundle validation)
      throws JSONException, WdkModelException {
    return ParamContainerFormatter.convertToValidatedParamContainerJson(
      getStepAnalysisTypeJsonWithoutParams(spec.get().getStepAnalysis()),
      spec, validation, StepAnalysisSupplementalParams.getNames());
  }

  private static JSONObject getStepAnalysisTypeJsonWithoutParams(StepAnalysis analysis) {
    return ParamContainerFormatter.supplementWithBasicParamInfo(analysis,
      new JSONObject()
        .put(JsonKeys.NAME, analysis.getName())
        .put(JsonKeys.DISPLAY_NAME, analysis.getDisplayName())
        .put(JsonKeys.SHORT_DESCRIPTION, analysis.getShortDescription())
        .put(JsonKeys.DESCRIPTION, analysis.getDescription())
        .put(JsonKeys.RELEASE_VERSION, analysis.getReleaseVersion())
        .put(JsonKeys.CUSTOM_THUMBNAIL, analysis.getCustomThumbnail()),
      StepAnalysisSupplementalParams.getNames()
    );
  }

  public static JSONObject getStepAnalysisInstanceJson(StepAnalysisInstance instance) {
    return instance.getJsonSummary();
  }

  public static JSONArray getStepAnalysisInstancesJson(
      Map<Long, StepAnalysisInstance> instances) {
    return new JSONArray(instances.entrySet().stream()
      .map(StepAnalysisFormatter::instanceSummaryJson)
      .collect(Collectors.toList()));
  }

  private static JSONObject instanceSummaryJson(Entry<Long, StepAnalysisInstance> entry) {
    return new JSONObject()
      .put(JsonKeys.ANALYSIS_ID, entry.getKey())
      .put(JsonKeys.DISPLAY_NAME, entry.getValue().getDisplayName());
  }
}
