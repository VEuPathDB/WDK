package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.functional.Functions.f0Swallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.service.formatter.QuestionFormatter;
import org.gusdb.wdk.service.request.QuestionRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
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

  private static final String QUESTION_RESOURCE = "question: ";
  private static final String FILTER_PARAM_RESOURCE = "filter parameter: ";

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
    SemanticallyValid<AnswerSpec> validSpec = AnswerSpec.builder(getWdkModel())
        .setQuestionName(getQuestionFromSegment(questionName).getFullName())
        .build(
            getSessionUser(), 
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.FILL_PARAM_IF_MISSING)
        .getSemanticallyValid()
        .getOrThrow(spec -> new WdkModelException("Default values for question '" +
            questionName + "' are not semantically valid."));
    return Response.ok(QuestionFormatter.getQuestionJsonWithParamValues(validSpec).toString()).build();
  }

  /**
   * Returns information about a single question, given a set of parameter
   * values.  Any missing or invalid parameters are replaced with valid values
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
   * @throws DataValidationException 
   * @throws RequestMisformatException 
   */
  @POST
  @Path("/{questionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionRevise(
      @PathParam("questionName") String questionName,
      String body)
          throws WdkModelException, RequestMisformatException, DataValidationException {
    Question question = getQuestionFromSegment(questionName);
    SemanticallyValid<AnswerSpec> answerSpec = AnswerSpec.builder(getWdkModel())
        .setQuestionName(question.getFullName())
        .setParamValues(QuestionRequest.parse(body, question).getContextParamValues())
        .build(
            getSessionUser(), 
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.FILL_PARAM_IF_MISSING_OR_INVALID)
        .getSemanticallyValid()
        .getOrThrow(spec -> new WdkModelException("Unable to produce a valid spec from incoming param values"));
    return Response.ok(QuestionFormatter.getQuestionJsonWithParamValues(answerSpec).toString()).build();
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
   * @throws DataValidationException 
   */
  @POST
  @Path("/{questionName}/refreshed-dependent-params")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionChange(@PathParam("questionName") String questionName, String body)
          throws WdkUserException, WdkModelException, DataValidationException {

    // get requested question and throw not found if invalid
    Question question = getQuestionFromSegment(questionName);

    // parse incoming JSON into existing and changed values
    QuestionRequest request = QuestionRequest.parse(body, question);

    if (!request.getChangedParam().isPresent()) {
      throw new RequestMisformatException("'changedParam' property is required at this endpoint");
    }

    // find the param object for the changed param
    Entry<String,String> changedParamEntry = request.getChangedParam().get();
    Param changedParam = question.getParamMap().get(changedParamEntry.getKey());

    // make sure incoming values reflect changed value
    Map<String,String> contextParams = new MapBuilder<String,String>(
        request.getContextParamValues()).put(changedParamEntry).toMap();

    // Build an answer spec with the passed values but replace missing/invalid
    // values with defaults.  Will remove unaffected params below.
    SemanticallyValid<AnswerSpec> answerSpec = AnswerSpec.builder(getWdkModel())
        .setQuestionName(question.getFullName())
        .setParamValues(contextParams)
        .build(
            getSessionUser(),
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.FILL_PARAM_IF_MISSING_OR_INVALID)
        .getSemanticallyValid()
        .getOrThrow(spec -> new WdkModelException("Unable to produce a valid spec from incoming param values"));

    // see if changed param value changed during build; if so, then invalid
    if (!answerSpec.getObject().getQueryInstanceSpec()
        .get(changedParam.getName()).equals(changedParamEntry.getValue())) {
      // means the build process determined the incoming changed param value to
      // be invalid and changed it to the default; this is disallowed, so throw
      // TODO: figure out an elegant way to tell the user WHY the value is invalid
      throw new DataValidationException("The passed changed param value '" + 
          changedParamEntry.getValue() + "' is invalid.");
    }

    // get stale params of the changed value; if any of these are invalid, also throw exception
    Set<Param> staleDependentParams = changedParam.getStaleDependentParams();
    ValidationBundle validation = answerSpec.getObject().getValidationBundle();
    Map<String,List<String>> errors = validation.getKeyedErrors();
    if (!errors.isEmpty()) {
      for (Param param : staleDependentParams) {
        if (errors.containsKey(param.getName())) {
          throw new WdkModelException("Unable to generate valid values for question " +
              question.getFullName() + FormatUtil.NL + validation.toString());
        }
      }
    }

    // output JSON but tell formatter to skip non-stale params; their values
    // may have inadvertently changed (if incoming values were invalid) but the
    // client should only be modifying params that depend on the changed param
    List<String> paramsToOutput = mapToList(staleDependentParams, NamedObject::getName);
    JSONArray output = QuestionFormatter.getParamsJson(AnswerSpec.getValidQueryInstanceSpec(answerSpec),
        param -> paramsToOutput.contains(param.getName()));
    return Response.ok(output.toString()).build();
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
   * @throws WdkModelException
   * @throws DataValidationException
   * @throws RequestMisformatException 
   */
  @POST
  @Path("/{questionName}/{paramName}/ontology-term-summary")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamOntologyTermSummary(
      @PathParam("questionName") String questionName,
      @PathParam("paramName") String paramName,
      String body)
          throws WdkModelException, DataValidationException, RequestMisformatException {

    // parse elements of the request
    Question question = getQuestionFromSegment(questionName);
    FilterParamNew filterParam = getFilterParam(question, paramName);
    Map<String, String> contextParamValues = QuestionRequest.parse(body, question).getContextParamValues();
    JSONObject jsonBody = new JSONObject(body);
    String ontologyId = jsonBody.getString("ontologyId");

    // build a query instance spec from passed values
    SemanticallyValid<QueryInstanceSpec> validSpec = QueryInstanceSpec.builder()
        .putAll(contextParamValues)
        .buildValidated(
            getSessionUser(),
            question.getQuery(),
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.NO_FILL)
        .getSemanticallyValid()
        .getOrThrow(spec -> new DataValidationException(spec.getValidationBundle().toString()));

    // try to look up ontology term with this ID
    OntologyItem ontologyItem = filterParam.getOntology(validSpec).get(ontologyId);
    if (ontologyItem == null) {
      throw new DataValidationException("Requested ontology item '" + ontologyId + "' does not exist for this parameter (" + paramName + ").");
    }

    // get term summary and format
    JSONObject summaryJson = QuestionFormatter.getOntologyTermSummaryJson(
        f0Swallow(() -> filterParam.getOntologyTermSummary(validSpec, ontologyItem,
            jsonBody, ontologyItem.getType().getJavaClass())));
    return Response.ok(summaryJson.toString()).build();
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
   * @throws RequestMisformatException 
   */
  @POST
  @Path("/{questionName}/{paramName}/summary-counts")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamSummaryCounts(
      @PathParam("questionName") String questionName,
      @PathParam("paramName") String paramName,
      String body)
          throws WdkModelException, RequestMisformatException, DataValidationException {

    // parse elements of the request
    Question question = getQuestionFromSegment(questionName);
    FilterParamNew filterParam = getFilterParam(question, paramName);
    Map<String, String> contextParamValues = QuestionRequest.parse(body, question).getContextParamValues();
    JSONObject jsonBody = new JSONObject(body);

    // build a query instance spec from passed values
    SemanticallyValid<QueryInstanceSpec> validSpec = QueryInstanceSpec.builder()
        .putAll(contextParamValues)
        .buildValidated(
            getSessionUser(),
            question.getQuery(),
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.NO_FILL)
        .getSemanticallyValid()
        .getOrThrow(spec -> new DataValidationException(spec.getValidationBundle().toString()));

    FilterParamSummaryCounts counts = filterParam.getTotalsSummary(validSpec, jsonBody);
    JSONObject result = QuestionFormatter.getFilterParamSummaryJson(counts);
    return Response.ok(result.toString()).build();

  }

  private static <T extends NamedObject> Predicate<String> stringListFilter(
      String commaDelimitedStrs, List<T> validValues) {
    if (commaDelimitedStrs == null || commaDelimitedStrs.trim().isEmpty()) {
      return str -> true;
    }
    List<String> strs = Arrays.asList(commaDelimitedStrs.trim().split(","));
    List<String> validStrs = mapToList(validValues, NamedObject::getFullName);
    for (String str : strs) {
      if (!validStrs.contains(str)) {
        throw new BadRequestException("Specified filter value '" + str + "' is not valid.");
      }
    }
    return str -> strs.contains(str);
  }

  private Question getQuestionFromSegment(String questionName) {
    return getWdkModel().getQuestionByUrlSegment(questionName)
      .orElseGet(() -> getWdkModel().getQuestion(questionName)
        .orElseThrow(() ->
          // A WDK Model Exception here implies that a question of the name provided cannot be found.
          new NotFoundException(formatNotFound(QUESTION_RESOURCE + questionName))));
  }

  private static FilterParamNew getFilterParam(Question question, String paramName) {
    Param param = question.getParamMap().get(paramName);
    if (param == null || !(param instanceof FilterParamNew)) {
      throw new NotFoundException(formatNotFound(FILTER_PARAM_RESOURCE + paramName));
    }
    return (FilterParamNew)param;
  }

}
