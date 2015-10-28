package org.gusdb.wdk.service.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.param.ParamFormatter;
import org.gusdb.wdk.service.formatter.param.ParamFormatterFactory;
import org.gusdb.wdk.service.formatter.param.VocabProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionFormatter {

  public static JSONArray getQuestionsJson(List<Question> questions, boolean expandQuestions, boolean expandParams, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException {
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

  public static JSONObject getQuestionJson(Question q, boolean expandParams, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException {
      return getQuestionJson(q, expandParams, new HashSet<Param>(Arrays.asList(q.getParams())), user, dependedParamValues);
  }
  
  public static JSONObject getQuestionJson(Question q, boolean expandParams, Set<? extends Param> params, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException {
    JSONObject qJson = new JSONObject();
    qJson.put("name", q.getFullName());
    qJson.put("displayName", q.getDisplayName());
    qJson.put("class", q.getRecordClass().getFullName());
    qJson.put("params", getParamsJson(q, expandParams, params, user, dependedParamValues));
    return qJson;
  }

  public static JSONArray getParamsJson(Question q, boolean expandParams, Set<? extends Param> params, User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException {
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
