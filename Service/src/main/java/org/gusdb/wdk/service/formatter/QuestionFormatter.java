package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.formatter.param.ParamFormatter;
import org.gusdb.wdk.service.formatter.param.ParamFormatterFactory;
import org.gusdb.wdk.service.formatter.param.VocabProvider;
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
      .put(Keys.NAME, q.getFullName())
      .put(Keys.DISPLAY_NAME, q.getDisplayName())
      .put(Keys.SHORT_DISPLAY_NAME, q.getShortDisplayName())
      .put(Keys.DESCRIPTION, q.getDescription())
      .put(Keys.HELP, q.getHelp())
      .put(Keys.NEW_BUILD, q.getNewBuild())
      .put(Keys.REVISE_BUILD, q.getReviseBuild())
      .put(Keys.URL_SEGMENT,  q.getUrlSegment())
      .put(Keys.RECORD_CLASS_NAME, q.getRecordClass().getFullName())
      .put(Keys.PARAMETERS, getParamsJson(params, expandParams, user, dependedParamValues))
      .put(Keys.DEFAULT_ATTRIBUTES, FormatUtil.stringCollectionToJsonArray(q.getSummaryAttributeFieldMap().keySet()))
      .put(Keys.DYNAMIC_ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
          q.getDynamicAttributeFieldMap(FieldScope.ALL).values(), FieldScope.ALL, true))
      .put(Keys.DEFAULT_SUMMARY_VIEW, q.getDefaultSummaryView().getName())
      .put(Keys.SUMMARY_VIEW_PLUGINS, FormatUtil.stringCollectionToJsonArray(q.getSummaryViews().keySet()))
      .put(Keys.STEP_ANALYSIS_PLUGINS, FormatUtil.stringCollectionToJsonArray(q.getStepAnalyses().keySet()))
      .put(Keys.PROPERTIES, q.getPropertyLists());
  }

  public static JSONArray getParamsJson(Collection<Param> params, boolean expandParams, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONArray paramsJson = new JSONArray();
    for (Param param : params) {
      if (expandParams) {
        ParamFormatter<?> formatter = ParamFormatterFactory.getFormatter(param);
        paramsJson.put(formatter instanceof VocabProvider ?
          ((VocabProvider)formatter).getJson(user, dependedParamValues) :
          formatter.getJson());
      }
      else {
        paramsJson.put(param.getName());
      }
    }
    return paramsJson;
  }

}
