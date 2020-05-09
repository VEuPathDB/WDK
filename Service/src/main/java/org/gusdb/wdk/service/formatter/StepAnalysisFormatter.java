package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.StepAnalysisFormSpec;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance.JsonKey;
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
        .put(JsonKeys.IS_CACHEABLE, analysis.hasParamWithUncacheableQuery()),
      StepAnalysisSupplementalParams.getNames(analysis)
    );
  }

  /**
   * Returns JSON of the following spec (for public consumption):
   * {
   *   analysisId: int
   *   analysisName: string
   *   stepId: int
   *   displayName: string
   *   shortDescription: string
   *   description: string
   *   userNotes: string
   *   hasParams: boolean
   *   status: enumerated string, see org.gusdb.wdk.model.user.analysis.ExecutionStatus
   *   invalidStepReason: string | null
   *   formParams: key-value object of param values
   * }
   * @throws WdkModelException 
   */
  public static JSONObject getStepAnalysisInstanceJson(StepAnalysisInstance instance, ValidationLevel level) throws WdkModelException {
    StepAnalysis analysis = instance.getStepAnalysis();
    String invalidStepReason = instance.getInvalidStepReason();
    StepAnalysisFormSpec formSpec = instance.getFormSpec(level, FillStrategy.FILL_PARAM_IF_MISSING);
    return new JSONObject()
        .put(JsonKey.analysisName.name(), analysis.getName())
        .put(JsonKey.analysisId.name(), instance.getAnalysisId())
        .put(JsonKey.stepId.name(), instance.getStep().getStepId())
        .put(JsonKey.answerValueHash.name(), instance.getAnswerValueHash())
        .put(JsonKey.displayName.name(), instance.getDisplayName())
        .put(JsonKey.shortDescription.name(), analysis.getShortDescription())
        .put(JsonKey.description.name(), analysis.getDescription())
        .put(JsonKey.userNotes.name(), instance.getUserNotes())
        .put(JsonKey.hasParams.name(), instance.hasParams())
        .put(JsonKey.status.name(), instance.getStatus().name())
        .put(JsonKey.invalidStepReason.name(), (invalidStepReason == null ? JSONObject.NULL : invalidStepReason))
        .put(JsonKey.formParams.name(), ParamContainerFormatter.formatExistingParamValues(formSpec));
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
