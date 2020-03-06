package org.gusdb.wdk.service.formatter.param;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.FilterParamNew.OntologyTermSummary;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParameterContainer;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.gusdb.wdk.service.formatter.ValidationFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParamContainerFormatter {

  public static JSONObject formatExistingParamValues(ParameterContainerInstanceSpec<?> instanceSpec) {
    JSONObject json = new JSONObject();
    // if container present, then return the values of the container's params
    if (instanceSpec.getParameterContainer().isPresent()) {
      for (Param param : instanceSpec.getParameterContainer().get().getParams()) {
        String value = instanceSpec.get(param.getName());
        if (value != null) {
          // skip if value not present; errors should tell client it's missing
          // if present then convert to external format
          json.put(param.getName(), param.getExternalStableValue(value));
        }
      }
    }
    // if no container present then return the raw param values (no way to determine validity)
    else {
      for (Entry<String,String> entry : instanceSpec.entrySet()) {
        json.put(entry.getKey(), entry.getValue());
      }
    }
    return json;
  }

  public static <T extends ParameterContainerInstanceSpec<T>> JSONArray getParamsJson(
      DisplayablyValid<T> spec, Predicate<Param> inclusionPredicate) throws WdkModelException {
    JSONArray paramsJson = new JSONArray();
    for (Param param : spec.get().getParameterContainer().get().getParams()) {
      if (inclusionPredicate.test(param)) {
        paramsJson.put(ParamFormatterFactory.getFormatter(param).getJson(spec));
      }
    }
    return paramsJson;
  }

  /*
   * {
   *   "valueCounts" : [ { value: string|null; count: number; filteredCount: number; }, ... ],
   *   "internalsCount" : 12456,
   *   "internalsFilteredCount" : 4352
   * }
   */
  public static <T> JSONObject getOntologyTermSummaryJson(Supplier<OntologyTermSummary<T>> supplier) {
    OntologyTermSummary<T> summary = supplier.get();
    Map<T,FilterParamSummaryCounts> counts = summary.getSummaryCounts();

    JSONObject json = new JSONObject();

    JSONArray jsonarray = new JSONArray();
    for (Entry<T,FilterParamSummaryCounts> entry : counts.entrySet()) {
      T termValue = entry.getKey();
      FilterParamSummaryCounts fpsc = entry.getValue();
      JSONObject c = new JSONObject();
      c.put("value", termValue == null ? JSONObject.NULL : termValue);
      c.put("count", fpsc.unfilteredFilterItemCount);
      c.put("filteredCount", fpsc.filteredFilterItemCount);
      jsonarray.put(c);
    }

    json.put("valueCounts", jsonarray);
    json.put("internalsCount", summary.getDistinctInternal());
    json.put("internalsFilteredCount", summary.getDistinctMatchingInternal());
    return json;

  }

  /*
   * { "filtered" : 123, "unfiltered" : 234}
   */
  public static JSONObject getFilterParamSummaryJson(FilterParamSummaryCounts counts) {
    JSONObject json = new JSONObject();
    json.put("nativeFiltered", counts.filteredFilterItemCount);
    json.put("filtered", counts.filteredRecordCount);
    json.put("nativeUnfiltered", counts.unfilteredFilterItemCount);
    json.put("unfiltered", counts.unfilteredRecordCount);
    return json;
  }

  public static JSONObject supplementWithBasicParamInfo(
      ParameterContainer container, JSONObject baseObject, Set<String> paramsToExclude) {
    return baseObject
      .put(JsonKeys.GROUPS, getGroupsJson(container.getParamMapByGroups(), paramsToExclude))
      .put(JsonKeys.PARAM_NAMES, filterNames(container.getParamMap().keySet(), paramsToExclude));
  }

  private static JSONArray getGroupsJson(Map<Group, Map<String, Param>> paramsByGroup, Set<String> paramsToExclude) {
    JSONArray groups = new JSONArray();
    for (Group group: paramsByGroup.keySet()) {
      Map<String, Param> entry = paramsByGroup.get(group);
      groups.put(getGroupJson(group, filterNames(entry.keySet(), paramsToExclude)));
    }
    return groups;
  }

  private static Set<String> filterNames(Set<String> setOfNames, Set<String> namesToExclude) {
    Set<String> filteredNames = new LinkedHashSet<>();
    setOfNames.stream()
      .filter(p -> !namesToExclude.contains(p))
      .forEach(p -> filteredNames.add(p));
    return filteredNames;
  }

  private static JSONObject getGroupJson(Group group, Set<String> params) {
    JSONObject groupJson = new JSONObject();
    groupJson.put(JsonKeys.NAME, group.getName());
    groupJson.put(JsonKeys.DISPLAY_NAME, group.getDisplayName());
    groupJson.put(JsonKeys.DESCRIPTION, group.getDescription());
    groupJson.put(JsonKeys.IS_VISIBLE, group.isVisible());
    groupJson.put(JsonKeys.DISPLAY_TYPE, group.getDisplayType());
    groupJson.put(JsonKeys.PARAMETERS, params);
    return groupJson;
  }

  private static <T extends ParameterContainerInstanceSpec<T>> JSONObject supplementWithDetailedParamInfo(
      JSONObject baseObjectJson,
      DisplayablyValid<T> validSpec,
      Set<String> paramsToExclude) throws JSONException, WdkModelException {
    return baseObjectJson.put(JsonKeys.PARAMETERS, getParamsJson(
        validSpec, param -> !paramsToExclude.contains(param.getName())));
  }

  public static <T extends ParameterContainerInstanceSpec<T>> JSONObject convertToValidatedParamContainerJson(
      JSONObject baseObjectJson,
      DisplayablyValid<T> validSpec,
      ValidationBundle validation,
      Set<String> paramsToExclude) throws JSONException, WdkModelException {
    return new JSONObject()
      .put(JsonKeys.SEARCH_DATA, supplementWithDetailedParamInfo(baseObjectJson, validSpec, paramsToExclude))
      .put(JsonKeys.VALIDATION, ValidationFormatter.getValidationBundleJson(validation));
  }
}
