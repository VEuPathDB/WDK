package org.gusdb.wdk.service.formatter;

import java.util.Map;
import java.util.stream.Collectors;

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
    analysisJson.put("name", analysis.getName());
    analysisJson.put("displayName", analysis.getDisplayName());
    analysisJson.put("shortDescription", analysis.getShortDescription());
    analysisJson.put("description", analysis.getDescription());
    analysisJson.put("releaseVersion", analysis.getReleaseVersion());
    analysisJson.put("customThumbnail", analysis.getCustomThumbnail());
    analysisJson.put("hasParameters", analysis.getHasParameters());
    return analysisJson;
  }

  public static JSONArray getStepAnalysisInstancesJson(
      Map<Long, StepAnalysisInstance> instances) {
    return new JSONArray(instances.entrySet().stream()
        .map(StepAnalysisFormatter::instanceSummaryJson)
        .collect(Collectors.toList()));
  }

  private static JSONObject instanceSummaryJson(
      Map.Entry<Long, StepAnalysisInstance> entry) {
    return new JSONObject()
        .put(StepAnalysisInstance.JsonKey.analysisId.name(), entry.getKey())
        .put(
            StepAnalysisInstance.JsonKey.displayName.name(),
            entry.getValue().getDisplayName()
        );
  }
}
