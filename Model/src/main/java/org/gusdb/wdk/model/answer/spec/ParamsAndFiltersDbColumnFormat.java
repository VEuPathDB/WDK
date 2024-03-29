package org.gusdb.wdk.model.answer.spec;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.answer.spec.ColumnFilterConfigSet.ColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles conversion back and forth from the JSON contained in the
 * param_filters CLOB column of the STEPS table (in user DB) to the aggregate
 * parts of an answer spec (param values, filter values, column filter values).
 * 
 * Note there are subtle differences between this JSON format and the one
 * handled by AnswerSpecServiceFormat (in WDK/Service).
 * 
 * @author rdoherty
 */
public class ParamsAndFiltersDbColumnFormat {

  // top level json keys
  public static final String KEY_PARAMS = "params";
  public static final String KEY_FILTERS = "filters";
  public static final String KEY_VIEW_FILTERS = "viewFilters";
  public static final String KEY_COLUMN_FILTERS = "columnFilters";

  // filter json keys
  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";
  public static final String KEY_DISABLED = "disabled";

  public static JSONObject formatParamFilters(AnswerSpec answerSpec) {
    JSONObject jsContent = new JSONObject();
    jsContent.put(KEY_PARAMS, formatParams(answerSpec.getQueryInstanceSpec()));
    jsContent.put(KEY_FILTERS, formatFilters(answerSpec.getFilterOptions()));
    // TODO: As of 8/20/19 we do not write view filters to the database; should purge their existence at some point
    //jsContent.put(KEY_VIEW_FILTERS, formatFilters(answerSpec.getViewFilterOptions()));
    jsContent.put(KEY_COLUMN_FILTERS, formatColumnFilters(answerSpec.getColumnFilterConfig()));
    return jsContent;
  }

  public static JSONObject formatParams(ParameterContainerInstanceSpec<?> paramValues) {
    return formatParams(paramValues, Collections.emptySet());
  }

  public static JSONObject formatParams(ParameterContainerInstanceSpec<?> paramValues, Set<String> paramsToExclude) {
    // convert params
    JSONObject jsParams = new JSONObject();
    for (String paramName : paramValues.keySet()) {
      if (!paramsToExclude.contains(paramName)) {
        jsParams.put(paramName, paramValues.get(paramName));
      }
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

  public static FilterOptionListBuilder parseFiltersJson(JSONObject paramFiltersJson, String filtersKey) throws JSONException {
    FilterOptionListBuilder builder = FilterOptionList.builder();
    if (paramFiltersJson == null || !paramFiltersJson.has(filtersKey)) {
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

  public static ColumnFilterConfigSetBuilder parseColumnFilters(JSONObject paramFiltersJson) throws JSONException {
    ColumnFilterConfigSetBuilder builder = new ColumnFilterConfigSetBuilder();
    if (paramFiltersJson == null || !paramFiltersJson.has(KEY_COLUMN_FILTERS)) {
      return builder;
    }
    JSONObject columnsObject = paramFiltersJson.getJSONObject(KEY_COLUMN_FILTERS);
    for (Entry<String,JsonType> columnEntry : JsonIterators.objectIterable(columnsObject)) {
      if (columnEntry.getValue().getType().equals(JsonType.ValueType.OBJECT)) {
        JSONObject filtersObject = columnEntry.getValue().getJSONObject();
        for (Entry<String,JsonType> filterEntry : JsonIterators.objectIterable(filtersObject)) {
          if (filterEntry.getValue().getType().equals(JsonType.ValueType.OBJECT)) {
            JSONObject config = filterEntry.getValue().getJSONObject();
            builder.setFilterConfig(columnEntry.getKey(), filterEntry.getKey(), config);
          }
          else throw new JSONException(filterEntry.getValue() + " is not a JSON object.");
        }
      }
      else throw new JSONException(columnEntry.getValue() + " is not a JSON object.");
    }
    return builder;
  }

  public static JSONObject formatColumnFilters(ColumnFilterConfigSet columnFilterConfig) {
    JSONObject json = new JSONObject();
    // entry is column -> 
    for (Entry<String, ColumnFilterConfig> column: columnFilterConfig.entrySet()) {
      JSONObject columnObj = new JSONObject();
      for (Entry<String, JSONObject> filter : column.getValue().entrySet()) {
        columnObj.put(filter.getKey(), filter.getValue());
      }
      json.put(column.getKey(), columnObj);
    }
    return json;
  }

}
