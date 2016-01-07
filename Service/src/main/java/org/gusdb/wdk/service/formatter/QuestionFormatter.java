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
 * 
 * {
 *   name: String,
 *   displayName: String,
 *   urlSegment: String,
 *   class: String,
 *   parameters: [ see ParamFormatters ],
 *   dynamicAttributes: [ see AttributeFieldFormatter ]
 * }
 * 
 * @author rdoherty
 */
public class QuestionFormatter {

  public static JSONArray getQuestionsJson(List<Question> questions, boolean expandQuestions, boolean expandParams, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONArray json = new JSONArray();
    for (Question q : questions) {
      if (expandQuestions) {
        json.put(getQuestionJson(q, expandParams, user, dependedParamValues));
      }
      else {
        json.put(q.getFullName());
      }
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
      .put(Keys.URL_SEGMENT,  q.getUrlSegment())
      .put(Keys.CLASS, q.getRecordClass().getFullName())
      .put(Keys.PARAMETERS, getParamsJson(params, expandParams, user, dependedParamValues))
      .put(Keys.DEFAULT_ATTRIBUTES, FormatUtil.stringCollectionToJsonArray(q.getSummaryAttributeFieldMap().keySet()))
      .put(Keys.DYNAMIC_ATTRIBUTES, AttributeFieldFormatter.getAttributesJson(
          q.getDynamicAttributeFieldMap(FieldScope.ALL).values(), FieldScope.ALL, true));
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
        paramsJson.put(param.getFullName());
      }
    }
    return paramsJson;
  }

}
