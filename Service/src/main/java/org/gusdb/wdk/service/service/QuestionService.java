package org.gusdb.wdk.service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.values.StableValues;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.ValidStableValues;
import org.gusdb.wdk.model.query.param.values.WriteableStableValues;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.QuestionFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides access to Question data configured in the WDK Model.  All question
 * name path params can be either the configured question URL segment (which
 * defaults to the short name but can be overridden in the XML), or the
 * question's full, two-part name, made by joining the question set name and
 * question short name with a '.'.
 * 
 * @author rdoherty
 */
@Path("/questions")
@Produces(MediaType.APPLICATION_JSON)
public class QuestionService extends WdkService {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(QuestionService.class);

  /**
   * Get a list of all questions for a recordClass. Does not supply details of the questions (use another endpoint for that).
   */
  @GET
  public Response getQuestions(
      @QueryParam("recordClass") String recordClassStr,
      @QueryParam("expandQuestions") Boolean expandQuestions,
      @QueryParam("expandParams") Boolean expandParams)
          throws JSONException, WdkModelException, WdkUserException {
    try {
      return Response.ok(QuestionFormatter.getQuestionsJson(
          recordClassStr == null || recordClassStr.isEmpty() ?
              getAllQuestions(getWdkModel()) :
              getQuestionsForRecordClasses(getWdkModel(), recordClassStr.split(",")),
          getFlag(expandQuestions),
          getFlag(expandParams),
          getSessionUser()
      ).toString()).build();
    }
    catch (IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
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
   * Get the information about a specific question.  Use expandParams=true to get the details of each parameter, including vocabularies and metadata info.
   * This endpoint is typically used to display a question page (using default values). 
   */
  @GET
  @Path("/{questionName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestion(
      @PathParam("questionName") String questionName,
      @QueryParam("expandParams") Boolean expandParams)
          throws WdkUserException, WdkModelException {
    Question question = getQuestionOrNotFound(questionName);
    return Response.ok(QuestionFormatter.getQuestionJson(
        question,
        getFlag(expandParams),
        getSessionUser(),
        ValidStableValuesFactory.createDefault(getSessionUser(), question.getQuery())
    ).toString()).build();
  }

  /**
   * Get information about a question, given a complete set of param values.  (This endpoint is 
   * typically used for a revise operation.)  Throw a WdkUserException if any parameter value
   * is missing or invalid.  (The exception only describes the first invalid parameter, not all such.)
   * 
   * Sample request body:
   * {
   *   "contextParamValues": {
   *     "size": "5",
   *     "people": "Sam,Sue"
   *   }
   * }
   * 
   * @param questionName
   * @param expandParams
   * @param body
   * @return
   * @throws WdkModelException
   * @throws DataValidationException 
   * @throws RequestMisformatException 
   */
  @POST
  @Path("/{questionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionRevise(@PathParam("questionName") String questionName, String body)
          throws WdkModelException, DataValidationException, RequestMisformatException {
    try {
      Question question = getQuestionOrNotFound(questionName);

      // extract context values from body
      StableValues incomingValues = parseContextParamValuesFromJson(new JSONObject(body), question);

      // confirm that we got all param values
      CompleteValidStableValues validSet = ValidStableValuesFactory.createFromCompleteValues(getSessionUser(), incomingValues, true);

      return Response.ok(QuestionFormatter.getQuestionJson(question, true, getSessionUser(), validSet).toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
  }

  /**
   * Get an updated set of vocabularies (and meta data info) for the parameters that depend on the specified changed parameter.
   * (Also validate the changed parameter.)
   * Request must provide the parameter values of any other parameters that those vocabularies depend on, as well as the changed parameter.
   * (This endpoint is typically used when a user changes a depended param.)
   *
   * Sample request body:
   *
   * {
   *   "changedParam" : { "name": "height", "value": "12" },
   *   "contextParamValues" : [see /{questionName} endpoint]
   * }
   *
   * @param questionName
   * @param expandParams
   * @param body
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  @POST
  @Path("/{questionName}/refreshed-dependent-params")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionChange(@PathParam("questionName") String questionName, String body)
          throws DataValidationException, RequestMisformatException, WdkModelException {
    try {
      Question question = getQuestionOrNotFound(questionName);
      JSONObject jsonBody = new JSONObject(body);
      JSONObject changedParamJson = jsonBody.getJSONObject("changedParam");
      String changedParamName = changedParamJson.getString("name");
      String changedParamValue = changedParamJson.getString("value");
      StableValues originalValues = parseContextParamValuesFromJson(jsonBody, question);
      ValidStableValues updatedValues = ValidStableValuesFactory.createFromChangedValue(
          changedParamName, changedParamValue, originalValues);
      return Response.ok(QuestionFormatter.getParamsJson(updatedValues, true, getSessionUser()).toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
  }

  private StableValues parseContextParamValuesFromJson(JSONObject bodyJson, Question question)
      throws DataValidationException, RequestMisformatException {
    try {
      JSONObject contextJson = bodyJson.getJSONObject("contextParamValues");
      WriteableStableValues contextParamValues = new WriteableStableValues(question.getQuery());

      for (String paramName : JsonUtil.getKeys(contextJson)) {
        String paramValue = contextJson.get(paramName).toString();
        if (!question.getParamMap().containsKey(paramName))
          throw new DataValidationException("Parameter '" + paramName + "' is not in question '" + question.getFullName() + "'.");
        contextParamValues.put(paramName, paramValue);
      }
      return contextParamValues;
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  /**
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered counts for a specified ontology term.
   *
   * Sample request body:
   *
   * {
   *   "ontologyId" : string
   *   "filters" : [ see raw value for FilterParamHandler ]
   *   "contextParamValues" : [see /{questionName} endpoint]
   * }
   *
   * @param questionName
   * @param paramName
   * @param body
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   * @throws DataValidationException 
   */
  @POST
  @Path("/{questionName}/{paramName}/ontology-term-summary")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamOntologyTermSummary(@PathParam("questionName") String questionName, @PathParam("paramName") String paramName, String body)
          throws WdkUserException, WdkModelException, DataValidationException {
    try {
      Question question = getQuestionOrNotFound(questionName);
      FilterParamNew filterParam = getFilterParam(questionName, question, paramName);
      User user = getSessionUser();
      JSONObject jsonBody = new JSONObject(body);
      String ontologyId = jsonBody.getString("ontologyId");
      StableValues contextParamValues = parseContextParamValuesFromJson(jsonBody, question);
      CompleteValidStableValues validatedParamStableValues =
          ValidStableValuesFactory.createFromCompleteValues(user, contextParamValues, true);
      OntologyItem ontologyItem = filterParam.getOntology(user, validatedParamStableValues).get(ontologyId);
      if (ontologyItem == null) {
        throw new DataValidationException("Requested ontology item '" + ontologyId + "' does not exist for this parameter (" + paramName + ").");
      }
      JSONObject summaryJson = getOntologyTermSummaryJson(user, validatedParamStableValues, filterParam,
          ontologyItem, jsonBody, ontologyItem.getType().getJavaClass());
      return Response.ok(summaryJson.toString()).build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  private <T> JSONObject getOntologyTermSummaryJson(User user, CompleteValidStableValues contextParamValues,
      FilterParamNew param, OntologyItem ontologyItem, JSONObject jsonBody, Class<T> ontologyItemClass)
          throws WdkModelException {
    
    FilterParamNew.OntologyTermSummary<T> summary = param.getOntologyTermSummary(
        user, contextParamValues, ontologyItem, jsonBody, ontologyItemClass);

    return QuestionFormatter.getOntologyTermSummaryJson(summary);
  }

  /**
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered counts.
   *
   * Sample request body:
   *
   * {
   *   "filters" : [ see raw value for FilterParamHandler ]
   *   "contextParamValues" : [see /{questionName} endpoint]
   * }
   *
   * @param questionName
   * @param paramName
   * @param body
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   * @throws DataValidationException 
   */
  @POST
  @Path("/{questionName}/{paramName}/summary-counts")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamSummaryCounts(@PathParam("questionName") String questionName, @PathParam("paramName") String paramName, String body)
          throws WdkModelException, DataValidationException {
    try {
      Question question = getQuestionOrNotFound(questionName);
      FilterParamNew filterParam = getFilterParam(questionName, question, paramName);
      User user = getSessionUser();
      JSONObject jsonBody = new JSONObject(body);
      StableValues contextParamValues = parseContextParamValuesFromJson(jsonBody, question);
      CompleteValidStableValues validatedParamStableValues =
          ValidStableValuesFactory.createFromCompleteValues(user, contextParamValues, true);
      FilterParamSummaryCounts counts = filterParam.getTotalsSummary(user, validatedParamStableValues, jsonBody);
      return Response.ok(QuestionFormatter.getFilterParamSummaryJson(counts).toString()).build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
  }

  private Param getParam(String questionName, Question question, String paramName) {
    if (question == null)
      throw new NotFoundException(WdkService.NOT_FOUND + questionName);
    Param param = question.getQuery().getParamMap().get(paramName);
    if (param == null)
      throw new NotFoundException(WdkService.NOT_FOUND + paramName);
    return param;
  }

  private FilterParamNew getFilterParam(String questionName, Question question, String paramName) throws WdkUserException {
    Param param = getParam(questionName, question, paramName);
    if (!(param instanceof FilterParamNew)) throw new WdkUserException(paramName + " is not a FilterParam");
    return (FilterParamNew)param;
  }

  private Question getQuestionOrNotFound(String questionName) {
    try {
      WdkModel model = getWdkModel();
      Question q = model.getQuestionByUrlSegment(questionName);
      return (q == null ? model.getQuestion(questionName) : q);
    }
    catch(WdkModelException e) {
      // A WDK Model Exception here implies that a question of the name provided cannot be found.
      throw new NotFoundException(formatNotFound("question: " + questionName));
    }
  }
}
