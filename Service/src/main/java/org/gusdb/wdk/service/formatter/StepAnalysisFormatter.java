package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.query.spec.StepAnalysisFormSpec;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
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
    StepAnalysis sa = spec.get().getStepAnalysis().get();
    return ParamContainerFormatter.convertToValidatedParamContainerJson(
      getStepAnalysisTypeJsonWithoutParams(sa),
      spec, validation, StepAnalysisSupplementalParams.getNames(sa));
  }

  private static JSONObject getStepAnalysisTypeJsonWithoutParams(StepAnalysis analysis) {
    return ParamContainerFormatter.supplementWithBasicParamInfo(analysis,
      new JSONObject()
        .put(JsonKeys.NAME, analysis.getName())
        .put(JsonKeys.DISPLAY_NAME, analysis.getDisplayName())
        .put(JsonKeys.SHORT_DESCRIPTION, analysis.getShortDescription())
        .put(JsonKeys.DESCRIPTION, analysis.getDescription())
        .put(JsonKeys.RELEASE_VERSION, analysis.getReleaseVersion())
        .put(JsonKeys.CUSTOM_THUMBNAIL, analysis.getCustomThumbnail())
        .put(JsonKeys.IS_CACHEABLE, !analysis.hasParamWithUncacheableQuery()),
      StepAnalysisSupplementalParams.getNames(analysis)
    );
  }

  /**
   * Returns JSON of the following spec (for public consumption):
   * {
   *   analysisId: int
   *   stepId: int
   *   analysisName: string
   *   displayName: string
   *   shortDescription: string
   *   description: string
   *   userNotes: string
   *   status: enumerated string, see org.gusdb.wdk.model.user.analysis.ExecutionStatus
   *   formParams: key-value object of param stable values
   *   validation: validation bundle for the step analysis instance
   * }
   * @throws WdkModelException 
   */
  public static JSONObject getStepAnalysisInstanceJson(StepAnalysisInstance instance, ExecutionStatus runStatus) throws WdkModelException {
    Optional<StepAnalysis> analysis = instance.getStepAnalysis();
    return new JSONObject()
        .put(JsonKeys.ANALYSIS_ID, instance.getAnalysisId())
        .put(JsonKeys.STEP_ID, instance.getStep().getStepId())
        .put(JsonKeys.ANALYSIS_NAME, instance.getAnalysisName())
        .put(JsonKeys.DISPLAY_NAME, instance.getDisplayName())
        .put(JsonKeys.SHORT_DESCRIPTION, analysis.map(a -> a.getShortDescription()).orElse(null))
        .put(JsonKeys.DESCRIPTION, analysis.map(a -> a.getDescription()).orElse(null))
        .put(JsonKeys.USER_NOTES, instance.getUserNotes())
        .put(JsonKeys.STATUS, runStatus.name())
        .put(JsonKeys.PARAMETERS, instance.getFormSpecJson())
        .put(JsonKeys.VALIDATION, ValidationFormatter.getValidationBundleJson(instance.getValidationBundle()));
    
  }

  public static JSONArray getStepAnalysisInstancesJson(
      List<StepAnalysisInstance> instances) {
    return new JSONArray(instances.stream()
      .map(StepAnalysisFormatter::instanceSummaryJson)
      .collect(Collectors.toList()));
  }

  private static JSONObject instanceSummaryJson(StepAnalysisInstance instance) {
    return new JSONObject()
      .put(JsonKeys.ANALYSIS_ID, instance.getAnalysisId())
      .put(JsonKeys.DISPLAY_NAME, instance.getDisplayName());
  }
}
