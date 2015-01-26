package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionFormatter {

  public static JSONArray getQuestionsJson(QuestionSet[] questionSets, boolean expandQuestions, boolean expandParams)
      throws JSONException, WdkModelException {
    JSONArray json = new JSONArray();
    for (QuestionSet qSet : questionSets) {
      for (Question q : qSet.getQuestions()) {
        if (expandQuestions) {
          json.put(getQuestionJson(q, expandParams));
        }
        else {
          json.put(q.getFullName());
        }
      }
    }
    return json;
  }

  public static JSONObject getQuestionJson(Question q, boolean expandParams)
      throws JSONException, WdkModelException {
    JSONObject qJson = new JSONObject();
    qJson.put("name", q.getFullName());
    qJson.put("displayName", q.getDisplayName());
    qJson.put("class", q.getRecordClass().getFullName());
    qJson.put("params", getParamsJson(q, expandParams));
    return qJson;
  }

  public static JSONArray getParamsJson(Question q, boolean expandParams)
      throws JSONException, WdkModelException {
    JSONArray params = new JSONArray();
    for (Param param : q.getParams()) {
      if (expandParams) {
        params.put(getParamJson(param));
      }
      else {
        params.put(param.getFullName());
      }
    }
    return params;
  }

  public static JSONObject getParamJson(Param param)
      throws JSONException, WdkModelException {
    JSONObject pJson = new JSONObject();
    pJson.put("name", param.getName());
    pJson.put("displayName", param.getName());
    pJson.put("prompt", param.getPrompt());
    pJson.put("help", param.getHelp());
    pJson.put("defaultValue", param.getDefault());
    return pJson;
  }
}
