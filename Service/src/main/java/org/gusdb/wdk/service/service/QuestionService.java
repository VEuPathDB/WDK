package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamHandler;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.QuestionFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
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
public class QuestionService extends AbstractWdkService {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(QuestionService.class);

  private static final String QUESTION_RESOURCE = "Question Name: ";

  /**
   * Returns an array of questions in this site's model.  Caller can filter by
   * output RecordClass and/or by question name.  Filter arguments must be
   * comma-delimited full names.  Each question's parameter information is
   * omitted at this level; call individual question endpoints for that.
   * 
   * @param recordClasses optional; only questions with the passed RecordClasses will be returned
   * @param questionNames optional; only questions with the passed name will be returned 
   * @return question json
   */
  @GET
  public JSONArray getQuestions(
      @QueryParam("outputRecordClass") String recordClasses,
      @QueryParam("questionNames") String questionNames) {
    WdkModel model = getWdkModel();
    List<Question> allQuestions = model.getAllQuestions();
    Predicate<String> recordClassFilter = stringListFilter(recordClasses, model.getAllRecordClasses());
    Predicate<String> questionNameFilter = stringListFilter(questionNames, allQuestions);
    List<Question> questions = allQuestions.stream()
        .filter(q -> questionNameFilter.test(q.getFullName()))
        .filter(q -> recordClassFilter.test(q.getRecordClass().getFullName()))
        .collect(Collectors.toList());
    return QuestionFormatter.getQuestionsJsonWithoutParams(questions);
  }

  private static <T extends NamedObject> Predicate<String> stringListFilter(String commaDelimitedStrs, List<T> validNames) {
    if (commaDelimitedStrs == null || commaDelimitedStrs.trim().isEmpty()) {
      return str -> true;
    }
    List<String> strs = Arrays.asList(commaDelimitedStrs.trim().split(","));
    List<String> validStrs = mapToList(validNames, NamedObject::getFullName);
    for (String str : strs) {
      if (!validStrs.contains(str)) {
        throw new BadRequestException("Specified filter value '" + str + "' is not valid.");
      }
    }
    return str -> strs.contains(str);
  }

  /**
   * Get information about a single question.  Includes parameter information,
   * including vocabularies and metadata based on generated default values.
   * This endpoint is typically used to render a "new" question page (i.e.
   * filled with default parameter values).
   * 
   * @param questionName name of the question being requested
   * @return question json
   * @throws WdkModelException if unable to generate param information
   */
  @GET
  @Path("/{questionName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionNew(
      @PathParam("questionName") String questionName)
          throws WdkModelException {
    AnswerSpec spec = AnswerSpec.builder(getWdkModel())
        .setQuestionName(getQuestionFromSegment(questionName).getFullName())
        .build(
            getSessionUser(), 
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.FILL_PARAM_IF_MISSING);
    return Response.ok(QuestionFormatter.getQuestionJsonWithParamValues(spec).toString()).build();
  }

  private Question getQuestionFromSegment(String questionName) {
    return getWdkModel().getQuestionByUrlSegment(questionName)
      .orElseGet(() -> getWdkModel().getQuestion(questionName)
        .orElseThrow(() ->
          // A WDK Model Exception here implies that a question of the name provided cannot be found.
          new NotFoundException(AbstractWdkService.formatNotFound(QUESTION_RESOURCE + questionName))));
  }

  /**
   * Get information about a single question, given a complete set of parameter
   * values.  Any missing or invalid parameters are replace with valid values
   * and the associated vocabularies.  Response includes parameter information,
   * including vocabularies and metadata based on the incoming values. This
   * endpoint is typically used to render a revise question page.  Input JSON
   * should have the following form:
   *
   * {
   *   "contextParamValues": {
   *     "<each-param-name>": String (stable value for param)
   *   }
   * }
   * 
   * @param questionName name of the question being requested
   * @param body body of request (see JSON above)
   * @return question json
   * @throws WdkModelException if unable to generate param information
   */
  @POST
  @Path("/{questionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionRevise(
      @PathParam("questionName") String questionName,
      String body)
          throws WdkModelException {
    Question question = getQuestionFromSegment(questionName);
    // extract context values from body
    Map<String, String> contextParamValues;
    try {
      JSONObject jsonBody = new JSONObject(body);
      contextParamValues = parseParamValuesFromJson(jsonBody, question);
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }

    // confirm that we got all param values
    for (Param param : question.getParams()) {
      if (!contextParamValues.containsKey(param.getName()))
        throw new WdkUserException("This call to the question service requires " +
            "that the body contain values for all params.  But it is missing one for: " + param.getName());
      param.validate(getSessionUser(), contextParamValues.get(param.getName()), contextParamValues);
    }

    return Response.ok(QuestionFormatter.getQuestionJson(question, true, getSessionUser(),
        contextParamValues).toString()).build();
  }

  /**
   * Get an updated set of vocabularies (and meta data info) for the parameters
   * that depend on the specified changed parameter.
   * (Also validate the changed parameter.)
   *
   * Request must provide the parameter values of any other parameters that
   * those vocabularies depend on, as well as the changed parameter.
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
          throws WdkUserException, WdkModelException {

    // get requested question and throw not found if invalid
    Question question = getQuestionFromSegment(questionName);

    // parse incoming JSON into existing and changed values
    Map<String, String> contextParamValues;
    String changedParamName;
    String changedParamValue;
    try {
      JSONObject jsonBody = new JSONObject(body);
      JSONObject changedParam = jsonBody.getJSONObject("changedParam");
      changedParamName = changedParam.getString("name");
      changedParamValue = changedParam.getString("value");
      contextParamValues = parseParamValuesFromJson(jsonBody, question);
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }

    // find the param object for the changed param
    Param changedParam = findParam(question, changedParamName);

    // validate the changed param value (will also validate the paramValuesContext it needs, if dependent)
    changedParam.validate(getSessionUser(), changedParamValue, contextParamValues);

    // get stale params
    Set<Param> staleDependentParams = changedParam.getStaleDependentParams();

    // remove stale param values from the context
    for (Param dependentParam : staleDependentParams) {
      contextParamValues.remove(dependentParam.getName());
    }

    // set the new value on the contextValues map so new dependent values can be generated
    contextParamValues.put(changedParamName, changedParamValue);

    // format JSON response (will fill missing values with defaults based on context)
    return Response.ok(QuestionFormatter.getParamsJson(
        staleDependentParams,
        true,
        getSessionUser(),
        contextParamValues).toString()).build();
  }

  private static Param findParam(Question question, String changedParamName) throws WdkUserException {
    List<Param> changedParamList = filter(question.getParamMap().values(),
        param -> param.getName().equals(changedParamName));
    if (changedParamList.isEmpty()) {
      throw new WdkUserException("Param with name '" + changedParamName +
          "' is no longer valid for question '" + question.getName() + "'");
    }
    return changedParamList.get(0);
  }

  private static Map<String, String> parseParamValuesFromJson(JSONObject bodyJson, Question question)
      throws RequestMisformatException {

    Map<String, String> contextParamValues = new HashMap<>();
    JSONObject contextJson = bodyJson.getJSONObject("contextParamValues");

    for (Iterator<?> keys = contextJson.keys(); keys.hasNext();) {
      String keyName = (String) keys.next();
      String keyValue = contextJson.get(keyName).toString();

      if (keyValue == null)
        throw new WdkUserException("Parameter name '" + keyName + "' has null value");

      if (!question.getParamMap().containsKey(keyName))
        throw new WdkUserException("Parameter '" + keyName + "' is not in question '" + question.getFullName() + "'.");

      contextParamValues.put(keyName, keyValue);
    }

    return contextParamValues;
  }

  /**
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered counts
   * for a specified ontology term.
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
  public Response getFilterParamOntologyTermSummary(
      @PathParam("questionName") String questionName,
      @PathParam("paramName") String paramName,
      String body)
          throws WdkUserException, WdkModelException, DataValidationException {
    try {
      Question question = getQuestionFromSegment(questionName);
      FilterParamNew filterParam = getFilterParam(questionName, question, paramName);
      User user = getSessionUser();
      JSONObject jsonBody = new JSONObject(body);
      String ontologyId = jsonBody.getString("ontologyId");
      Map<String, String> contextParamValues = parseParamValuesFromJson(jsonBody, question);
      OntologyItem ontologyItem = filterParam.getOntology(user, contextParamValues).get(ontologyId);
      if (ontologyItem == null) {
        throw new DataValidationException("Requested ontology item '" + ontologyId + "' does not exist for this parameter (" + paramName + ").");
      }
      JSONObject summaryJson = getOntologyTermSummaryJson(user, contextParamValues, filterParam,
          ontologyItem, jsonBody, ontologyItem.getType().getJavaClass());
      return Response.ok(summaryJson.toString()).build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  private <T> JSONObject getOntologyTermSummaryJson(User user, Map<String, String> contextParamValues,
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
   */
  @POST
  @Path("/{questionName}/{paramName}/summary-counts")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamSummaryCounts(
      @PathParam("questionName") String questionName,
      @PathParam("paramName") String paramName,
      String body) throws WdkUserException, WdkModelException {

    Question question = getQuestionFromSegment(questionName);
    FilterParamNew filterParam = getFilterParam(questionName, question, paramName);

    Map<String, String> contextParamValues;

    try {
      JSONObject jsonBody = new JSONObject(body);
      contextParamValues = parseParamValuesFromJson(jsonBody, question);
      FilterParamSummaryCounts counts = filterParam.getTotalsSummary(getSessionUser(), contextParamValues, jsonBody);
      return Response.ok(QuestionFormatter.getFilterParamSummaryJson(counts).toString()).build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  private Param getParam(String questionName, Question question, String paramName) {
    if (question == null)
      throw new NotFoundException(AbstractWdkService.NOT_FOUND + questionName);
    Param param = question.getQuery().getParamMap().get(paramName);
    if (param == null)
      throw new NotFoundException(AbstractWdkService.NOT_FOUND + paramName);
    return param;
  }

  private FilterParamNew getFilterParam(String questionName, Question question, String paramName) throws WdkUserException {
    Param param = getParam(questionName, question, paramName);
    if (!(param instanceof FilterParamNew)) throw new WdkUserException(paramName + " is not a FilterParam");
    return (FilterParamNew)param;
  }

}
