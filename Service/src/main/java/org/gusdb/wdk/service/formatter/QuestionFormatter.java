package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.FilterParamNew.OntologyTermSummary;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.service.formatter.param.ParamFormatterFactory;
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

  public static JSONArray getQuestionsJsonWithoutParams(List<Question> questions) {
    return reduce(questions, (arr, next) -> arr.put(getQuestionJson(next)), new JSONArray());
  }

  public static JSONObject getQuestionJsonWithParamValues(AnswerSpec spec)
      throws JSONException, WdkModelException {
    return getQuestionJson(spec.getQuestion())
        .put(JsonKeys.PARAMETERS, getParamsJson(spec.getQueryInstanceSpec()));
  }

  public static JSONObject getQuestionJson(Question q) {
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
      .put(JsonKeys.OUTPUT_RECORD_CLASS_NAME, q.getRecordClass().getFullName())
      .put(JsonKeys.GROUPS, getGroupsJson(q.getParamMapByGroups()))
      .put(JsonKeys.DEFAULT_ATTRIBUTES, new JSONArray(q.getSummaryAttributeFieldMap().keySet()))
      .put(JsonKeys.DYNAMIC_ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
          q.getDynamicAttributeFieldMap(FieldScope.ALL).values(), FieldScope.ALL, true))
      .put(JsonKeys.DEFAULT_SUMMARY_VIEW, q.getDefaultSummaryView().getName())
      .put(JsonKeys.SUMMARY_VIEW_PLUGINS, new JSONArray(q.getSummaryViews().keySet()))
      .put(JsonKeys.STEP_ANALYSIS_PLUGINS, new JSONArray(q.getStepAnalyses().keySet()))
      // NOTE: if null returned by getAllowedRecordClasses, property will be omitted in returned JSON
      .put(JsonKeys.ALLOWED_PRIMARY_INPUT_RECORD_CLASS_NAMES, getAllowedRecordClasses(q.getQuery().getPrimaryAnswerParam()))
      .put(JsonKeys.ALLOWED_SECONDARY_INPUT_RECORD_CLASS_NAMES, getAllowedRecordClasses(q.getQuery().getSecondaryAnswerParam()))
      .put(JsonKeys.PROPERTIES, q.getPropertyLists());
  }

  /**
   * Returns the names of any allowed recordclasses for the param as a
   * JSONArray, or null if the optional is empty.
   * 
   * @param answerParam answer param for which allowed RCs should be returned
   * @return array of allowed names or null if no param present
   */
  private static JSONArray getAllowedRecordClasses(Optional<AnswerParam> answerParam) {
    return answerParam.map(param -> new JSONArray(param.getAllowedRecordClasses().values())).orElse(null);
  }

  public static JSONArray getParamsJson(QueryInstanceSpec spec) throws WdkModelException {
    JSONArray paramsJson = new JSONArray();
    for (Param param : spec.getQuery().getParams()) {
      paramsJson.put(ParamFormatterFactory.getFormatter(param).getJson(spec));
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

  /*
   * {
   *   "valueCounts" : [ { value: string|null; count: number; filteredCount: number; }, ... ],
   *   "internalsCount" : 12456,
   *   "internalsFilteredCount" : 4352
   * }
   */
  public static <T> JSONObject getOntologyTermSummaryJson(OntologyTermSummary<T> summary) {
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
