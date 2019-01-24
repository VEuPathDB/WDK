package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.param.ParamFormatter;
import org.gusdb.wdk.service.formatter.param.ParamFormatterFactory;
import org.gusdb.wdk.service.formatter.param.DependentParamProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Formats WDK Question objects.  Question JSON will have the following form:
 * {
 *   name: String,
 *   displayName: String,
 *   shortDisplayName: String,
 *   description: String,
 *   help: String,
 *   newBuild: Number,
 *   reviseBuild: Number,
 *   urlSegment: String,
 *   class: String,
 *   parameters: [ see ParamFormatters ],
 *   defaultAttributes: [ String ],
 *   dynamicAttributes: [ see AttributeFieldFormatter ],
 *   defaultSummaryView: String,
 *   summaryViewPlugins: [ String ],
 *   stepAnalysisPlugins: [ String ]
 * }
 *
 * @author rdoherty
 */
public class QuestionFormatter {

  public static JSONArray getQuestionsJson(List<Question> questions, boolean expandQuestions, boolean expandParams, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONArray json = new JSONArray();
    for (Question q : questions) {
      json.put(expandQuestions ?
          getQuestionJson(q, expandParams, user, dependedParamValues) :
          q.getFullName());
    }
    return json;
  }

  public static JSONObject getQuestionJson(Question q, boolean expandParams,
      User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    return getQuestionJson(q, expandParams, user, dependedParamValues, q.getParamMap().values());
  }

  public static JSONObject getQuestionJson(Question q, boolean expandParams,
      User user, Map<String, String> dependedParamValues, Collection<Param> params)
      throws JSONException, WdkModelException, WdkUserException {
    return new JSONObject()
      .put(JsonKeys.NAME, q.getFullName())
      .put(JsonKeys.DISPLAY_NAME, q.getDisplayName())
      .put(JsonKeys.SHORT_DISPLAY_NAME, q.getShortDisplayName())
      .put(JsonKeys.DESCRIPTION, q.getDescription())
      .put(JsonKeys.ICON_NAME, q.getIconName())
      .put(JsonKeys.SUMMARY, q.getSummary())
      .put(JsonKeys.HELP, q.getHelp())
      .put(JsonKeys.NEW_BUILD, q.getNewBuild())
      .put(JsonKeys.REVISE_BUILD, q.getReviseBuild())
      .put(JsonKeys.URL_SEGMENT,  q.getUrlSegment())
      .put(JsonKeys.RECORD_CLASS_NAME, q.getRecordClass().getFullName())
      .put(JsonKeys.PARAMETERS, getParamsJson(params, expandParams, user, dependedParamValues))
      .put(JsonKeys.GROUPS, getGroupsJson(q.getParamMapByGroups()))
      .put(JsonKeys.DEFAULT_ATTRIBUTES, FormatUtil.stringCollectionToJsonArray(q.getSummaryAttributeFieldMap().keySet()))
      .put(JsonKeys.DEFAULT_SORTING, formatDefaultSorting(q))
      .put(JsonKeys.DYNAMIC_ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
          q.getDynamicAttributeFieldMap(FieldScope.ALL).values(), FieldScope.ALL, true))
      .put(JsonKeys.DEFAULT_SUMMARY_VIEW, q.getDefaultSummaryView().getName())
      .put(JsonKeys.SUMMARY_VIEW_PLUGINS, FormatUtil.stringCollectionToJsonArray(q.getSummaryViews().keySet()))
      .put(JsonKeys.STEP_ANALYSIS_PLUGINS, FormatUtil.stringCollectionToJsonArray(q.getStepAnalyses().keySet()))
      .put(JsonKeys.PROPERTIES, q.getPropertyLists());
  }

  public static JSONArray getParamsJson(Collection<Param> params, boolean expandParams, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONArray paramsJson = new JSONArray();
    for (Param param : params) {
      if (expandParams) {
        ParamFormatter<?> formatter = ParamFormatterFactory.getFormatter(param);
        paramsJson.put(formatter instanceof DependentParamProvider ?
          ((DependentParamProvider)formatter).getJson(user, dependedParamValues) :
          formatter.getJson());
      }
      else {
        paramsJson.put(param.getName());
      }
    }
    return paramsJson;
  }

  private static JSONArray getGroupsJson(Map<Group, Map<String, Param>> paramsByGroup) {
    JSONArray groups = new JSONArray();
    for (Group group: paramsByGroup.keySet()) {
      Map<String, Param> entry = paramsByGroup.get(group);
      groups.put(getGroupJson(group, entry.keySet()));
    }
    return groups;
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

  private static JSONArray formatDefaultSorting(Question question) {
    return new JSONArray(
        SortDirectionSpec.convertSorting(question.getSortingAttributeMap(), question.getAttributeFieldMap())
            .stream()
            .map(spec -> new JSONObject()
                .put("attributeName", spec.getItemName())
                .put("direction", spec.getDirection()))
            .collect(Collectors.toList()));
  }

  /*
   * {
   *   "valueCounts" : [ { value: string|null; count: number; filteredCount: number; }, ... ],
   *   "internalsCount" : 12456,
   *   "internalsFilteredCount" : 4352
   * }
   */
  public static <T> JSONObject getOntologyTermSummaryJson(FilterParamNew.OntologyTermSummary<T> summary) {
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

  public static Object getInternalValueJson(String internalValue) {
    JSONObject json = new JSONObject();
    json.put("internalValue", internalValue);
    return json;
  }
}
