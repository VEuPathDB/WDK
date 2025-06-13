package org.gusdb.wdk.service.request.answer;

import java.util.Optional;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.answer.spec.ParamsAndFiltersDbColumnFormat;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.service.formatter.param.ParamContainerFormatter;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.filter.ColumnFilterServiceFormat;
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
   *   "columnFilters": (optional) {
   *     "columnName": {
   *       "toolName": [ any ]
   *     }
   *   },
   *   "wdkWeight": (optional) Integer
   * }
   *
   * @param json JSON representation of an answer spec
   * @param wdkModel WDK model
   * @return constructed answer spec builder
   * @throws RequestMisformatException if JSON is malformed
   */
  public static AnswerSpecBuilder parse(Question question, JSONObject json, WdkModel wdkModel) throws RequestMisformatException {
    try {

      QueryInstanceSpecBuilder qiSpecBuilder = QueryInstanceSpec.builder()
        .putAll(JsonUtil.parseProperties(json.getJSONObject(JsonKeys.PARAMETERS)));

      // apply weight if present
      if (json.has(JsonKeys.WDK_WEIGHT)) {
        qiSpecBuilder.setAssignedWeight(json.getInt(JsonKeys.WDK_WEIGHT));
      }

      // get question name, validate, and create instance with valid Question
      AnswerSpecBuilder specBuilder = AnswerSpec.builder(wdkModel)
          .setQuestionFullName(question.getFullName())
          .setQueryInstanceSpec(qiSpecBuilder);

      // apply filter and view filter options if present
      specBuilder.setFilterOptions(ParamsAndFiltersDbColumnFormat.parseFiltersJson(json, JsonKeys.FILTERS));

      // NOTE: As of 8/20/19 we do not parse view filters with other answer spec properties
      //specBuilder.setViewFilterOptions(ParamsAndFiltersDbColumnFormat.parseFiltersJson(json, JsonKeys.VIEW_FILTERS));

      // apply column filter configurations if present
      if (json.has(JsonKeys.COLUMN_FILTERS)) {
        specBuilder.setColumnFilters(
          ColumnFilterServiceFormat.parse(question,
            json.getJSONObject(JsonKeys.COLUMN_FILTERS)));
      }

      return specBuilder;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
  }

  /**
   * Parses view filters out of the passed parent JSON object.  If present,
   * view filters should have the following format:
   * {
   *   "viewFilters": (optional) [ {
   *     "name": String, value: Any
   *   } ]
   * }
   * @param parentJson object possibly containing a viewFilters property
   * @return builder for the parsed view filters (may be an empty builder if none present)
   */
  public static FilterOptionListBuilder parseViewFilters(JSONObject parentJson) {
    return ParamsAndFiltersDbColumnFormat.parseFiltersJson(parentJson, JsonKeys.VIEW_FILTERS);
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
        .put(JsonKeys.PARAMETERS, ParamContainerFormatter.formatExistingParamValues(answerSpec.getQueryInstanceSpec()))
        .put(JsonKeys.FILTERS, ParamsAndFiltersDbColumnFormat.formatFilters(answerSpec.getFilterOptions()))
        // NOTE: As of 8/20/19 we do not include view filters as part of the normal answer spec (StepService)
        //.put(JsonKeys.VIEW_FILTERS, ParamsAndFiltersDbColumnFormat.formatFilters(answerSpec.getViewFilterOptions()))
        .put(JsonKeys.COLUMN_FILTERS, ParamsAndFiltersDbColumnFormat.formatColumnFilters(answerSpec.getColumnFilterConfig()))
        .put(JsonKeys.WDK_WEIGHT, answerSpec.getQueryInstanceSpec().getAssignedWeight());
  }
}
