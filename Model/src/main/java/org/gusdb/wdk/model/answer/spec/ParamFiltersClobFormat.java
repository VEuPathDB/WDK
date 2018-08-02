package org.gusdb.wdk.model.answer.spec;

import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.answer.spec.QueryInstanceSpec.QueryInstanceSpecBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParamFiltersClobFormat {

  // top level json keys
  public static final String KEY_PARAMS = "params";
  public static final String KEY_FILTERS = "filters";
  public static final String KEY_VIEW_FILTERS = "viewFilters";

  // filter json keys
  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";
  public static final String KEY_DISABLED = "disabled";

  public static JSONObject formatParamFilters(AnswerSpec answerSpec) {
    JSONObject jsContent = new JSONObject();
    jsContent.put(KEY_PARAMS, formatParams(answerSpec.getQueryInstanceSpec()));
    jsContent.put(KEY_FILTERS, formatFilters(answerSpec.getFilterOptions()));
    jsContent.put(KEY_VIEW_FILTERS, formatFilters(answerSpec.getViewFilterOptions()));
    return jsContent;
  }

  public static JSONObject formatParams(QueryInstanceSpec paramValues) {
    // convert params
    JSONObject jsParams = new JSONObject();
    for (String paramName : paramValues.keySet()) {
      jsParams.put(paramName, paramValues.get(paramName));
    }
    return jsParams;
  }

  public static JSONArray formatFilters(FilterOptionList filters) {
    JSONArray jsOptions = new JSONArray();
    if (filters == null) {
      return jsOptions;
    }
    for (FilterOption option : filters) {
      JSONObject jsFilterOption = new JSONObject();
      jsFilterOption.put(KEY_NAME, option.getKey());
      jsFilterOption.put(KEY_VALUE, option.getValue());
      jsFilterOption.put(KEY_DISABLED, option.isDisabled());
      jsOptions.put(jsFilterOption);
    }
    return jsOptions;
  }

  public static QueryInstanceSpecBuilder parseParamsJson(JSONObject paramFiltersJson) throws JSONException {
    QueryInstanceSpecBuilder builder = QueryInstanceSpec.builder();
    if (paramFiltersJson == null) {
      return builder;
    }
    JSONObject source = paramFiltersJson.has(KEY_PARAMS) ?
        paramFiltersJson.getJSONObject(KEY_PARAMS) : paramFiltersJson;
    for (String key : JsonUtil.getKeys(source)) {
      builder.put(key, source.getString(key));
    }
    return builder;
  }

  public static FilterOptionListBuilder parseFiltersJson(JSONObject paramFiltersJson) throws JSONException {
    return parseFiltersJson(paramFiltersJson, KEY_FILTERS);
  }

  public static FilterOptionListBuilder parseViewFiltersJson(JSONObject paramFiltersJson) throws JSONException {
    return parseFiltersJson(paramFiltersJson, KEY_VIEW_FILTERS);
  }

  private static FilterOptionListBuilder parseFiltersJson(JSONObject paramFiltersJson, String filtersKey) {
    FilterOptionListBuilder builder = FilterOptionList.builder();
    if (paramFiltersJson == null || !paramFiltersJson.has(KEY_PARAMS) || !paramFiltersJson.has(filtersKey)) {
      return builder;
    }
    JSONArray source = paramFiltersJson.getJSONArray(filtersKey);
    for (JsonType filter : JsonIterators.arrayIterable(source)) {
      if (filter.getType().equals(JsonType.ValueType.OBJECT)) {
        JSONObject filterData = filter.getJSONObject();
        builder.addFilterOption(FilterOption.builder()
            .setFilterName(filterData.getString(KEY_NAME))
            .setValue(filterData.getJSONObject(KEY_VALUE))
            .setDisabled(filterData.optBoolean(KEY_DISABLED, false)));
      }
      else {
        throw new JSONException(filtersKey + " value is not a JSONObject.");
      }
    }
    return builder;
  }

}
