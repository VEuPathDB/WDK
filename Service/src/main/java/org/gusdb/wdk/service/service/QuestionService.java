package org.gusdb.wdk.service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.service.formatter.QuestionFormatter;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.WdkAnswerRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/question")
@Produces(MediaType.APPLICATION_JSON)
public class QuestionService extends WdkService {

  @GET
  public Response getQuestions(
      @QueryParam("recordClass") String recordClassStr,
      @QueryParam("expandQuestions") Boolean expandQuestions,
      @QueryParam("expandParams") Boolean expandParams)
          throws JSONException, WdkModelException {
    try {
      Map<String,String> dependerParams = null;
      return Response.ok(QuestionFormatter.getQuestionsJson(
          (recordClassStr == null || recordClassStr.isEmpty() ? getAllQuestions(getWdkModel()) :
            getQuestionsForRecordClasses(getWdkModel(), recordClassStr.split(","))),
          getFlag(expandQuestions), getFlag(expandParams), getCurrentUser(), dependerParams).toString()).build();
    }
    catch (IllegalArgumentException e) {
      return getBadRequestBodyResponse(e.getMessage());
    }
  }

  private static List<Question> getQuestionsForRecordClasses(
      WdkModel wdkModel, String[] recordClassNames) throws IllegalArgumentException {
    try {
      List<Question> questions = new ArrayList<>();
      for (String rcName : recordClassNames) {
        RecordClass rc = wdkModel.getRecordClass(rcName);
        questions.addAll(Arrays.asList(wdkModel.getQuestions(rc)));
      }
      return questions;
    }
    catch (WdkModelException e) {
      throw new IllegalArgumentException("At least one passed record class name is incorrect.", e);
    }
  }

  // TODO: seems like this should be part of the model, but we need
  //       to consider XML questions, boolean questions, etc.
  private static List<Question> getAllQuestions(WdkModel wdkModel) {
    List<Question> questions = new ArrayList<>();
    for (QuestionSet qSet : wdkModel.getAllQuestionSets()) {
      questions.addAll(Arrays.asList(qSet.getQuestions()));
    }
    return questions;
  }
  
  /**
   * Provide information about a question, given a complete set of depended param values.  (This is 
   * typically used for a revise operation.)
   * @param questionName
   * @param expandParams
   * @param body
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  @POST
  @Path("/{questionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestion(@PathParam("questionName") String questionName,
      @QueryParam("expandParams") Boolean expandParams, String body)
          throws WdkUserException, WdkModelException {

    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    
    // extract depended values from body
    Map<String, String> dependedParamValues = new HashMap<String, String>();
    try {
      JSONObject json = new JSONObject(body);
      JSONArray dependedJsonList = json.getJSONArray("dependedValues");
      for (int i = 0; i < dependedJsonList.length(); i++) {
        JSONObject dependedJson = dependedJsonList.getJSONObject(i);
        dependedParamValues.put(dependedJson.getString("name"), dependedJson.getString("name"));
      }
    }
    catch (JSONException e) {
      return getBadRequestBodyResponse(e.getMessage());
    }

    // confirm that we got all depended param values
    for (Param param : question.getParams()) {
      if (param instanceof AbstractEnumParam && ((AbstractEnumParam)param).isDependentParam()) {
        for (Param p: ((AbstractEnumParam)param).getDependedParams()) {
          if (!dependedParamValues.containsKey(p.getName()))
            throw new WdkUserException("This call to the question service requires that the body contain values for all depended params.  But it is missing one for: " + p.getName());
        }
      }
    }

    return Response.ok(QuestionFormatter.getQuestionJson(question, getFlag(expandParams), getCurrentUser(),
        dependedParamValues).toString()).build();
  }

  @GET
  @Path("/{questionName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestion(
      @PathParam("questionName") String questionName,
      @QueryParam("expandParams") Boolean expandParams)
          throws WdkUserException, WdkModelException {
    
    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    Map<String,String> dependedParamValues = new HashMap<String, String>();
    
    return Response.ok(QuestionFormatter.getQuestionJson(question,
        getFlag(expandParams), getCurrentUser(), dependedParamValues).toString()).build();
  }

  @GET
  @Path("/{questionName}/param")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getParamsForQuestion(@PathParam("questionName") String questionName)
      throws JSONException, WdkModelException, WdkUserException {
    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    Map<String,String> dependerParams = null;
    return Response.ok(QuestionFormatter.getParamsJson(question, true, getCurrentUser(), dependerParams).toString()).build();
  }
}
