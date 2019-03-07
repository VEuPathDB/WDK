package org.gusdb.wdk.service.request.answer;

import java.util.Optional;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.ParamsAndFiltersFormat;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerSpecServiceFormat {

  /**
   * Creates an AnswerSpecBuilder object using the passed JSON.  "questionName"
   * and "parameters" are the only required properties; legacy and modern
   * filters are optional; omission means no filters will be applied.  Weight is
   * optional and defaults to 0.  Question name will be specified separately
   * and thus is passed in.
   * 
   * Input Format:
   * {
   *   "parameters": Object (map from paramName -> paramValue),
   *   "legacyFilterName": (optional) String,
   *   "filters": (optional) [ {
   *     "name": String, value: Any
   *   } ],
   *   "viewFilters": (optional) [ {
   *     "name": String, value: Any
   *   } ],
   *   "wdk_weight": (optional) Integer
   * }
   * 
   * @param json JSON representation of an answer spec
   * @param wdkModel WDK model
   * @return constructed answer spec builder
   * @throws RequestMisformatException if JSON is malformed
   */
  public static AnswerSpecBuilder parse(Question question, JSONObject json, WdkModel wdkModel) throws RequestMisformatException {
    try {
      // get question name, validate, and create instance with valid Question
      AnswerSpecBuilder specBuilder = AnswerSpec.builder(wdkModel)
          .setQuestionName(question.getFullName())
          .setParamValues(JsonUtil.parseProperties(json.getJSONObject(JsonKeys.PARAMETERS)));

      // all filter fields and weight are optional
      if (json.has(JsonKeys.LEGACY_FILTER_NAME)) {
        specBuilder.setLegacyFilterName(Optional.of(json.getString(JsonKeys.LEGACY_FILTER_NAME)));
      }
      if (json.has(JsonKeys.FILTERS)) {
        specBuilder.setFilterOptions(ParamsAndFiltersFormat.parseFiltersJson(json, JsonKeys.FILTERS));
      }
      if (json.has(JsonKeys.VIEW_FILTERS)) {
        specBuilder.setViewFilterOptions(ParamsAndFiltersFormat.parseFiltersJson(json, JsonKeys.VIEW_FILTERS));
      }
      if (json.has(JsonKeys.WDK_WEIGHT)) {
        specBuilder.setAssignedWeight(json.getInt(JsonKeys.WDK_WEIGHT));
      }
      return specBuilder;
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
  }

  /**
   * Formats the passed answer spec into JSON.  Output format is the same as
   * input format except for an additional property, "questionName", which is
   * included when formatting an existing answer spec.
   * 
   * @param answerSpec answer spec to format
   * @return passed answer spec in JSON format
   */
  public static JSONObject format(AnswerSpec answerSpec) {
    return new JSONObject()
        // params and filters are sent with the same format as in the DB
        .put(JsonKeys.PARAMETERS, ParamsAndFiltersFormat.formatParams(answerSpec.getQueryInstanceSpec()))
        .put(JsonKeys.FILTERS, ParamsAndFiltersFormat.formatFilters(answerSpec.getFilterOptions()))
        .put(JsonKeys.VIEW_FILTERS, ParamsAndFiltersFormat.formatFilters(answerSpec.getViewFilterOptions()))
        .put(JsonKeys.WDK_WEIGHT, answerSpec.getQueryInstanceSpec().getAssignedWeight())
        .put(JsonKeys.LEGACY_FILTER_NAME, answerSpec.getLegacyFilterName().orElse(null));
  }

}
