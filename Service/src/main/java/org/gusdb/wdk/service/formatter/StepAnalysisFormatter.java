package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
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

import com.google.common.collect.Streams;

public class StepAnalysisFormatter {

  public static JSONArray getStepAnalysisTypesJsonWithoutParams(Collection<StepAnalysis> stepAnalyses) {
    return reduce(stepAnalyses, (arr, next) -> arr.put(getStepAnalysisTypeJsonWithoutParams(next)), new JSONArray());
  }

  public static JSONObject getStepAnalysisTypeJsonWithParams(
      DisplayablyValid<StepAnalysisFormSpec> spec, ValidationBundle validation)
      throws JSONException, WdkModelException {
    return ParamContainerFormatter.convertToValidatedParamContainerJson(
      getStepAnalysisTypeJsonWithoutParams(spec.get().getStepAnalysis()),
      spec, validation, StepAnalysisSupplementalParams.getNames(spec.get().getStepAnalysis()));
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
      StepAnalysisSupplementalParams.getNames(analysis)
    );
  }

  public static JSONObject getStepAnalysisInstanceJson(StepAnalysisInstance instance) {
    JSONObject baseObject = instance.getJsonSummary();
    // FIXME: This is a hack to transform the current DB format for params into
    //   our desired service API format.  The difference is that the DB currently
    //   stores params as a Map<String,String[]>, conforming to the previous
    //   servlet form param map type.  Instead, we want to send the client the
    //   param values in a Map<String,String>, where the values are stable values
    //   of WDK params.  See transformation below.
    //   See also: StepAnalysisInstanceService.translateParamValues()
    JSONObject paramsObject = baseObject.getJSONObject(StepAnalysisInstance.JsonKey.formParams.name());
    for (String paramName: paramsObject.keySet()) {
      JSONArray valueArray = paramsObject.getJSONArray(paramName);
      paramsObject.put(paramName, Streams
          .stream(JsonIterators.arrayIterable(valueArray))
          .map(JsonType::getString)
          .collect(Collectors.joining(",")));
    }
    return baseObject;
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
