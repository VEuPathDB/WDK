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
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.service.formatter.QuestionFormatter;
import org.json.JSONException;

@Path("/question")
@Produces(MediaType.APPLICATION_JSON)
public class QuestionService extends WdkService {

  @GET
  public Response getQuestions(
      @QueryParam("expandQuestions") Boolean expandQuestions,
      @QueryParam("expandParams") Boolean expandParams)
          throws JSONException, WdkModelException {
    return Response.ok(QuestionFormatter.getQuestionsJson(getWdkModel().getAllQuestionSets(),
        getFlag(expandQuestions), getFlag(expandParams)).toString()).build();
  }

  @GET
  @Path("/{questionName}")
  public Response getQuestion(
      @PathParam("questionName") String questionName,
      @QueryParam("expandParams") Boolean expandParams)
          throws WdkUserException, WdkModelException {
    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    return Response.ok(QuestionFormatter.getQuestionJson(question,
        getFlag(expandParams)).toString()).build();
  }

  @GET
  @Path("/{questionName}/param")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getParamsForQuestion(@PathParam("questionName") String questionName)
      throws JSONException, WdkModelException, WdkUserException {
    getWdkModelBean().validateQuestionFullName(questionName);
    Question question = getWdkModel().getQuestion(questionName);
    return Response.ok(QuestionFormatter.getParamsJson(question, true).toString()).build();
  }
}
