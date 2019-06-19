package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.functional.Functions.f0Swallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
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
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.service.annotation.OutSchema;
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
@Path(QuestionService.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class QuestionService extends AbstractWdkService {

  public static final String BASE_PATH = RecordService.ID_PATH + "/searches";
  public static final String ID_VAR = "questionUrlSegment";
  public static final String ID_PARAM = "{" + ID_VAR + "}";
  public static final String ID_PATH = BASE_PATH + "/" + ID_PARAM;

  private static final String FILTER_PARAM_RESOURCE = "filter parameter: ";

  private final String _recordClassUrlSegment;

  protected QuestionService(@PathParam(RecordService.ID_VAR) String recordClassUrlSegment) {
    _recordClassUrlSegment = recordClassUrlSegment;
  }

  /**
   * Returns an array of questions in this site's model.  Each question's
   * parameter information is omitted at this level; call individual question
   * endpoints for that.
   *
   * @return question json
   */
  @GET
  public JSONArray getQuestions() {
    WdkModel model = getWdkModel();
    RecordClass requestRecordClass = getRecordClassOrNotFound(_recordClassUrlSegment);
    List<Question> allQuestions = model.getAllQuestions();
    List<Question> questions = allQuestions.stream()
        .filter(q -> q.getRecordClass().getFullName().equals(requestRecordClass.getFullName()))
        .collect(Collectors.toList());
    return QuestionFormatter.getQuestionsJsonWithoutParams(questions);
  }

  /**
   * Get information about a single question.  Includes parameter information,
   * including vocabularies and metadata based on generated default values. This
   * endpoint is typically used to render a "new" question page (i.e. filled
   * with default parameter values).
   *
   * @param questionUrlSegment
   *   name of the question being requested
   *
   * @return question json
   *
   * @throws WdkModelException
   *   if unable to generate param information
   */
  @GET
  @Path(ID_PARAM)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.questions.name.get")
  public Response getQuestionNew(
      @PathParam(ID_VAR) String questionUrlSegment)
          throws WdkModelException {
    DisplayablyValid<AnswerSpec> validSpec = AnswerSpec.builder(getWdkModel())
        .setQuestionFullName(getQuestionOrNotFound(questionUrlSegment).getFullName())
        .build(
            getSessionUser(),
            StepContainer.emptyContainer(),
            ValidationLevel.DISPLAYABLE,
            FillStrategy.FILL_PARAM_IF_MISSING)
        .getDisplayablyValid()
        .getOrThrow(spec -> new WdkModelException("Default values for question '" +
            questionUrlSegment + "' are not displayable. Validation " +
            "details: " + spec.getValidationBundle().toString(2)));
    return Response.ok(QuestionFormatter.getQuestionJsonWithParamValues(validSpec,
        validSpec.get().getValidationBundle()).toString()).build();
  }

  /**
   * Returns information about a single question, given a set of parameter
   * values.  Any missing or invalid parameters are replaced with valid values
   * and the associated vocabularies.  Response includes parameter information,
   * including vocabularies and metadata based on the incoming values, and error
   * messages for any parameter values that were invalid. This endpoint is
   * typically used to render a revise question page.  Input JSON should have
   * the following form:
   * <pre>
   * {
   *   "contextParamValues": {
   *     "<each-param-name>": String (stable value for param)
   *   }
   * }
   * </pre>
   *
   * @param questionUrlSegment
   *   name of the question being requested
   * @param body
   *   body of request (see JSON above)
   *
   * @return question json
   *
   * @throws WdkModelException
   *   if unable to generate param information
   */
  @POST
  @Path(ID_PARAM)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO:  pass back error messages about invalid parameters
  public Response getQuestionRevise(
      @PathParam(ID_VAR) String questionUrlSegment,
      String body)
          throws WdkModelException, RequestMisformatException, DataValidationException {
    Question question = getQuestionOrNotFound(questionUrlSegment);
    QuestionRequest request = QuestionRequest.parse(body, question);
    AnswerSpec answerSpec = AnswerSpec.builder(getWdkModel())
        .setQuestionFullName(question.getFullName())
        .setParamValues(request.getContextParamValues())
        .build(
            getSessionUser(),
            StepContainer.emptyContainer(),
            ValidationLevel.SEMANTIC,
            FillStrategy.NO_FILL);
    // save off the validation (including errors) of the answer spec representing the passed-in values
    ValidationBundle validation = answerSpec.getValidationBundle();
    DisplayablyValid<AnswerSpec> displayableSpec =
        answerSpec.isValid() ?
        answerSpec.getDisplayablyValid().getLeft() :
        // need to generate a new, displayable answer spec so revise form can be shown
        AnswerSpec.builder(getWdkModel())
            .setQuestionFullName(question.getFullName())
            .setParamValues(request.getContextParamValues())
            .build(
                getSessionUser(),
                StepContainer.emptyContainer(),
                ValidationLevel.DISPLAYABLE,
                FillStrategy.FILL_PARAM_IF_MISSING_OR_INVALID)
            .getDisplayablyValid()
            .getOrThrow(spec -> new WdkModelException("Default values for question '" +
                questionUrlSegment + "' are not displayable. Validation " +
                "details: " + spec.getValidationBundle().toString(2)));
    return Response.ok(QuestionFormatter.getQuestionJsonWithParamValues(displayableSpec, validation).toString()).build();
  }

  /**
   * Get an updated set of vocabularies (and meta data info) for the parameters
   * that depend on the specified changed parameter. (Also validate the changed
   * parameter.)
   * <p>
   * Request must provide the parameter values of any other parameters that
   * those vocabularies depend on, as well as the changed parameter. (This
   * endpoint is typically used when a user changes a depended param.)
   * <p>
   * Sample request body:
   * <pre>
   * {
   *   "changedParam" : { "name": "height", "value": "12" },
   *   "contextParamValues" : [see /{questionUrlSegment} endpoint]
   * }
   * </pre>
   */
  @POST
  @Path(ID_PARAM + "/refreshed-dependent-params")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestionChange(@PathParam(ID_VAR) String questionUrlSegment, String body)
          throws WdkUserException, WdkModelException, DataValidationException {

    // get requested question and throw not found if invalid
    Question question = getQuestionOrNotFound(questionUrlSegment);

    // parse incoming JSON into existing and changed values
    QuestionRequest request = QuestionRequest.parse(body, question);

    // find the param object for the changed param
    Entry<String,String> changedParamEntry = request.getChangedParam()
        .orElseThrow(() -> new RequestMisformatException("'changedParam' property is required at this endpoint"));
    Param changedParam = question.getParamMap().get(changedParamEntry.getKey());

    // set incoming values to reflect changed value
    Map<String,String> contextParams = new MapBuilder<>(request.getContextParamValues()).put(changedParamEntry).toMap();

    // Build an answer spec with the passed values but replace missing/invalid
    // values with defaults.  Will remove unaffected params below.
    DisplayablyValid<AnswerSpec> answerSpec = AnswerSpec.builder(getWdkModel())
        .setQuestionFullName(question.getFullName())
        .setParamValues(contextParams)
        .build(
            getSessionUser(),
            StepContainer.emptyContainer(),
            ValidationLevel.DISPLAYABLE,
            FillStrategy.FILL_PARAM_IF_MISSING_OR_INVALID)
        .getDisplayablyValid()
        .getOrThrow(spec -> new WdkModelException("Unable to produce a valid spec from incoming param values"));

    // see if changed param value changed during build; if so, then it was invalid
    if (!answerSpec.get().getQueryInstanceSpec()
        .get(changedParam.getName()).equals(changedParamEntry.getValue())) {
      // means the build process determined the incoming changed param value to
      // be invalid and changed it to the default; this is disallowed, so throw
      // TODO: figure out an elegant way to tell the user WHY the value they entered is invalid
      throw new DataValidationException("The passed changed param value '" +
          changedParamEntry.getValue() + "' is invalid.");
    }

    // get stale params of the changed value
    Set<Param> staleDependentParams = changedParam.getStaleDependentParams();

    /* RRD 3/15/19 Not sure we need this check any more; comment for now and maybe remove later
    // if any stale params are invalid, also throw exception
    ValidationBundle validation = answerSpec.get().getValidationBundle();
    Map<String,List<String>> errors = validation.getKeyedErrors();
    if (!errors.isEmpty()) {
      for (Param param : staleDependentParams) {
        if (errors.containsKey(param.getName())) {
          throw new WdkModelException("Unable to generate valid values for question " +
              question.getFullName() + FormatUtil.NL + validation.toString());
        }
      }
    }*/

    // output JSON but tell formatter to skip non-stale params; their values
    // may have inadvertently changed (if incoming values were invalid) but the
    // client is only interested in params that depend on the changed value
    List<String> paramsToOutput = mapToList(staleDependentParams, NamedObject::getName);
    JSONArray output = QuestionFormatter.getParamsJson(
        AnswerSpec.getValidQueryInstanceSpec(answerSpec),
        param -> paramsToOutput.contains(param.getName()));
    return Response.ok(output.toString()).build();
  }

  /**
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered counts
   * for a specified ontology term.
   * <p>
   * Sample request body:
   * <pre>
   * {
   *   "ontologyId" : string
   *   "filters" : [ see raw value for FilterParamHandler ]
   *   "contextParamValues" : [see /{questionUrlSegment} endpoint]
   * }
   * </pre>
   */
  @POST
  @Path(ID_PARAM + "/{paramName}/ontology-term-summary")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamOntologyTermSummary(
      @PathParam(ID_VAR) String questionUrlSegment,
      @PathParam("paramName") String paramName,
      String body)
          throws WdkModelException, DataValidationException, RequestMisformatException {

    // parse elements of the request
    Question question = getQuestionOrNotFound(questionUrlSegment);
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
    QueryInstanceSpec spec = validSpec.get();
    OntologyItem ontologyItem = filterParam.getOntology(spec.getUser(), spec.toMap()).get(ontologyId);
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
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered
   * counts.
   * <p>
   * Sample request body:
   * <pre>
   * {
   *   "filters" : [ see raw value for FilterParamHandler ]
   *   "contextParamValues" : [see /{questionUrlSegment} endpoint]
   * }
   * </pre>
   */
  @POST
  @Path(ID_PARAM + "/{paramName}/summary-counts")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterParamSummaryCounts(
      @PathParam(ID_VAR) String questionUrlSegment,
      @PathParam("paramName") String paramName,
      String body)
          throws WdkModelException, RequestMisformatException, DataValidationException {

    // parse elements of the request
    Question question = getQuestionOrNotFound(questionUrlSegment);
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

  private static FilterParamNew getFilterParam(Question question, String paramName) {
    Param param = question.getParamMap().get(paramName);
    if (!(param instanceof FilterParamNew)) {
      throw new NotFoundException(formatNotFound(FILTER_PARAM_RESOURCE + paramName));
    }
    return (FilterParamNew)param;
  }
}
