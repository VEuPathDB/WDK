package org.gusdb.wdk.service.formatter;

import java.util.Map;

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

  private static JSONObject getStepAnalysisTypeDetailsJson(StepAnalysis analysis) {
    JSONObject analysisJson = getStepAnalysisTypeSummaryJson(analysis);
    return analysisJson;
  }

}
