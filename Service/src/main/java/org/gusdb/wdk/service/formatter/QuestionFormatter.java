package org.gusdb.wdk.service.formatter;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.gusdb.wdk.service.formatter.param.ParamContainerFormatter;
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
 *   summaryViewPlugins: [ see SummaryViewPluginFormatter ],
 *   stepAnalysisPlugins: [ String ],
 *   paramNames: [ String ]
 * }
 *
 * @author rdoherty
 */
public class QuestionFormatter {

  public static JSONArray getQuestionsJsonWithoutParams(List<Question> questions) {
    return reduce(questions, (arr, next) -> arr.put(getQuestionJsonWithoutParams(next)), new JSONArray());
  }

  public static JSONObject getQuestionJsonWithParams(
      DisplayablyValid<AnswerSpec> spec, ValidationBundle validation)
      throws JSONException, WdkModelException {
    return ParamContainerFormatter.convertToValidatedParamContainerJson(
      getQuestionJsonWithoutParams(spec.get().getQuestion()),
      AnswerSpec.getValidQueryInstanceSpec(spec),
      validation, Collections.emptySet());
  }

  private static JSONObject getQuestionJsonWithoutParams(Question q) {
    return ParamContainerFormatter.supplementWithBasicParamInfo(q.getQuery(),
      new JSONObject()
        .put(JsonKeys.URL_SEGMENT, q.getName())
        .put(JsonKeys.FULL_NAME, q.getFullName())
        .put(JsonKeys.DISPLAY_NAME, q.getDisplayName())
        .put(JsonKeys.SHORT_DISPLAY_NAME, q.getShortDisplayName())
        .put(JsonKeys.DESCRIPTION, q.getDescription())
        .put(JsonKeys.ICON_NAME, q.getIconName())
        .put(JsonKeys.SUMMARY, q.getSummary())
        .put(JsonKeys.HELP, q.getHelp())
        .put(JsonKeys.NEW_BUILD, q.getNewBuild())
        .put(JsonKeys.REVISE_BUILD, q.getReviseBuild())
        .put(JsonKeys.OUTPUT_RECORD_CLASS_NAME, q.getRecordClass().getUrlSegment())
        .put(JsonKeys.FILTERS, new JSONArray(q.getFilters().keySet()))
        .put(JsonKeys.DEFAULT_ATTRIBUTES, new JSONArray(q.getSummaryAttributeFieldMap().keySet()))
        .put(JsonKeys.DEFAULT_SORTING, DefaultJsonReporter.formatSorting(q.getSortingAttributeMap(), q.getAttributeFieldMap()))
        .put(JsonKeys.DYNAMIC_ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
            q.getDynamicAttributeFieldMap(FieldScope.ALL).values(), FieldScope.ALL, true))
        .put(JsonKeys.DEFAULT_SUMMARY_VIEW, q.getDefaultSummaryView().getName())
        .put(JsonKeys.SUMMARY_VIEW_PLUGINS, SummaryViewPluginFormatter.getSummaryViewPluginsJson(q.getOrderedSummaryViews()))
        // NOTE: if null returned by getAllowedRecordClasses, property will be omitted in returned JSON
        .put(JsonKeys.ALLOWED_PRIMARY_INPUT_RECORD_CLASS_NAMES, getAllowedRecordClasses(q.getQuery().getPrimaryAnswerParam()))
        .put(JsonKeys.ALLOWED_SECONDARY_INPUT_RECORD_CLASS_NAMES, getAllowedRecordClasses(q.getQuery().getSecondaryAnswerParam()))
        .put(JsonKeys.QUERY_NAME, q.getQuery().getName())
        .put(JsonKeys.FILTERS, getFiltersJson(q.getFilters()))
        .put(JsonKeys.PROPERTIES, q.getPropertyLists()),
      Collections.emptySet()
    );
  }

  /**
   * Returns the names of any allowed recordclasses for the param as a
   * JSONArray, or null if the optional is empty.
   * 
   * @param answerParam answer param for which allowed RCs should be returned
   * @return array of allowed names or null if no param present
   */
  private static JSONArray getAllowedRecordClasses(Optional<AnswerParam> answerParam) {
    return answerParam.map(param -> new JSONArray(
        param.getAllowedRecordClasses().values().stream().map(RecordClass::getUrlSegment).toArray())).orElse(null);
  }

  private static JSONArray getFiltersJson(Map<String, Filter> filtersMap) {
    return new JSONArray(filtersMap.values().stream()
      .map(filter -> new JSONObject()
        .put(JsonKeys.NAME, filter.getKey())
        .put(JsonKeys.DISPLAY_NAME, filter.getDisplay())
        .put(JsonKeys.DESCRIPTION, filter.getDescription())
        .put(JsonKeys.IS_VIEW_ONLY, filter.getFilterType().isViewOnly())
      ).collect(Collectors.toList()));
  }
}
