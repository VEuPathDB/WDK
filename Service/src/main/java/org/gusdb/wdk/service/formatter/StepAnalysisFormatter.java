package org.gusdb.wdk.service.formatter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.json.JSONArray;
import org.json.JSONObject;

public class StepAnalysisFormatter {

  public static JSONObject getStepAnalysisJson(StepAnalysisInstance instance) {
    return instance.getJsonSummary();
  }
  
  public static JSONArray getStepAnalysisTypesJson(Map<String, StepAnalysis> stepAnalyses) {
    JSONArray analysesJson = new JSONArray();
    for (StepAnalysis analysis : stepAnalyses.values()) {
      analysesJson.put(getStepAnalysisTypeSummaryJson(analysis));
     }
    return analysesJson;
  }
  
  private static JSONObject getStepAnalysisTypeSummaryJson(StepAnalysis analysis) {
    JSONObject analysisJson = new JSONObject();
    analysisJson.put(JsonKeys.NAME, analysis.getName());
    analysisJson.put(JsonKeys.DISPLAY_NAME, analysis.getDisplayName());
    analysisJson.put(JsonKeys.SHORT_DESCRIPTION, analysis.getShortDescription());
    analysisJson.put(JsonKeys.DESCRIPTION, analysis.getDescription());
    analysisJson.put(JsonKeys.RELEASE_VERSION, analysis.getReleaseVersion());
    analysisJson.put(JsonKeys.CUSTOM_THUMBNAIL, analysis.getCustomThumbnail());
    analysisJson.put(JsonKeys.HAS_PARAMETERS, analysis.getHasParameters());
    return analysisJson;
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
