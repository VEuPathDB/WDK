package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/question")
@Produces(MediaType.APPLICATION_JSON)
public class QuestionService extends WdkService {

  @GET
  public Response getQuestions(
      @QueryParam("expandQuestions") Boolean expandQuestions,
      @QueryParam("expandParams") Boolean expandParams)
          throws JSONException, WdkModelException {
    return Response.ok(getQuestionsJson(getBool(expandQuestions),
        getBool(expandParams)).toString()).build();
  }

  @GET
  @Path("/{questionName}")
  public Response getQuestion(
      @PathParam("questionName") String questionName,
      @QueryParam("expandParams") Boolean expandParams)
          throws WdkUserException, WdkModelException {
    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    return Response.ok(getQuestionJson(question,
        getBool(expandParams)).toString()).build();
  }

  @GET
  @Path("/{questionName}/params")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getParamsForQuestion(@PathParam("questionName") String questionName)
      throws JSONException, WdkModelException, WdkUserException {
    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    return Response.ok(getParamsJson(question, true).toString()).build();
  }

  private boolean getBool(Boolean boolValue) {
    return (boolValue == null ? false : boolValue);
  }
  
  private JSONArray getQuestionsJson(boolean expandQuestions, boolean expandParams)
      throws JSONException, WdkModelException {
    JSONArray json = new JSONArray();
    for (QuestionSet qSet : getWdkModel().getAllQuestionSets()) {
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

  private JSONObject getQuestionJson(Question q, boolean expandParams)
      throws JSONException, WdkModelException {
    JSONObject qJson = new JSONObject();
    qJson.put("name", q.getFullName());
    qJson.put("displayName", q.getDisplayName());
    qJson.put("class", q.getRecordClass().getFullName());
    qJson.put("params", getParamsJson(q, expandParams));
    return qJson;
  }

  private JSONArray getParamsJson(Question q, boolean expandParams)
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

  private JSONObject getParamJson(Param param)
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
