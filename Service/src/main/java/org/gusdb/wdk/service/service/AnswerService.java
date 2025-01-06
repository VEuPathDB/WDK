package org.gusdb.wdk.service.service;

import static java.util.function.Predicate.not;
import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.errors.ErrorContext;
import org.gusdb.wdk.errors.ServerErrorBundle;
import org.gusdb.wdk.events.ErrorEvent;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.request.AnswerFormatting;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.gusdb.wdk.model.report.util.ReporterFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.StepContainer.ListStepContainer;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>JSON input format:</p>
 * <pre>
 * {
 *   "searchConfig": Object (see AnswerRequestFactory for details),
 *   "viewFilters": Array of view filter configurations (optional),
 *   "reportConfig": Object (sample for JSON, XML, etc. below)
 * }
 * </pre>
 * <p>Sample input for our standard reporters:</p>
 * <pre>
 * reportConfig: {
 *   pagination: { offset: Number, numRecords: Number },   [only used by WDK standard JSON]
 *   attributes: [ attributeName: String ],
 *   tables: [ tableName: String ],
 *   sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]  [so far, only used by WDK standard JSON]
 *   attachmentType: String    [eg "excel".  optional. if not provided, return in browser (default disposition of inline), using the default content type of the reporter. if provided, disposition is attachment, of this type, and file extension reflects this type.]
 *   includeEmptyTables: true/false
 * }
 * </pre>
 */
@Path(AnswerService.REPORTS_PATH)
public class AnswerService extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(AnswerService.class);

  private static final String AVOID_CACHE_HIT_HEADER = "avoid-cache-hit";

  public static final String REPORTS_URL_SEGMENT = "reports";
  public static final String REPORTS_PATH = QuestionService.NAMED_SEARCH_PATH + "/" + REPORTS_URL_SEGMENT;
  public static final String REPORT_NAME_PATH_PARAM = "reportName";
  public static final String STANDARD_REPORT_SEGMENT = DefaultJsonReporter.RESERVED_NAME;
  public static final String CUSTOM_REPORT_SEGMENT = "{" + REPORT_NAME_PATH_PARAM + "}";
  public static final String STANDARD_REPORT_SEGMENT_PAIR = "/" + REPORTS_URL_SEGMENT + "/" + STANDARD_REPORT_SEGMENT;
  public static final String CUSTOM_REPORT_SEGMENT_PAIR = "/" + REPORTS_URL_SEGMENT + "/" + CUSTOM_REPORT_SEGMENT;

  private final String _recordClassUrlSegment;
  private final String _questionUrlSegment;
  private final boolean _avoidCacheHit;

  public AnswerService(
      @PathParam(RecordService.RECORD_TYPE_PATH_PARAM) String recordClassUrlSegment,
      @PathParam(QuestionService.SEARCH_PATH_PARAM) String questionUrlSegment,
      @HeaderParam(AVOID_CACHE_HIT_HEADER) @DefaultValue("false") boolean avoidCacheHit) {
    _recordClassUrlSegment = recordClassUrlSegment;
    _questionUrlSegment = questionUrlSegment;
    _avoidCacheHit = avoidCacheHit;
  }

  /**
   * This endpoint exists so we can provide a concrete JSON schema for the
   * response, since the /reports/{reportName} endpoint may not even return
   * JSON, depending on which reporter is specified.
   *
   * @param body
   *   JSON request body
   *
   * @return standard WDK answer JSON
   *
   * @throws RequestMisformatException
   *   if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException
   *   if JSON structure is correct but values contained are invalid
   * @throws WdkModelException
   *   if an error occurs while processing the request
   */
  @POST
  @Path(STANDARD_REPORT_SEGMENT)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.answer.post-request")
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswer(JSONObject body)
      throws RequestMisformatException, WdkModelException, DataValidationException {
    return createCustomReportAnswer(DefaultJsonReporter.RESERVED_NAME, body);
  }

  /**
   * Similar to the standard report endpoint that takes JSON, but gets its data
   * from a form instead of JSON. It is used by the client to push the provided
   * data to a new http target (ie, a tab), for example, a download report
   *
   * @param data
   *   JSON data representing an answer request, passed in the 'data' form
   *   param
   *
   * @return standard WDK answer JSON
   *
   * @throws RequestMisformatException
   *   if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException
   *   if JSON structure is correct but values contained are invalid
   * @throws WdkModelException
   *   if an error occurs while processing the request
   */
  @POST
  @Path(STANDARD_REPORT_SEGMENT)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswerFromForm(@FormParam("data") String data)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    return createCustomReportAnswerFromForm(DefaultJsonReporter.RESERVED_NAME, data);
  }

  /**
   * Processes an answer request (answer spec + formatting information) by
   * creating an answer from the answer spec, then calling the specified
   * reporter, passing a configuration, and streaming back the reporter's
   * result.
   *
   * @param body
   *   request body containing answer spec, format string, format configuration
   *
   * @return generated report
   *
   * @throws RequestMisformatException
   *   if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException
   *   if JSON structure is correct but values contained are invalid
   * @throws WdkModelException
   *   if an error occurs while processing the request
   */
  @POST
  @Path(CUSTOM_REPORT_SEGMENT)
  @Consumes(MediaType.APPLICATION_JSON)
  // Produces an unknown media type; varies depending on reporter selected
  public Response createCustomReportAnswer(@PathParam(REPORT_NAME_PATH_PARAM) String reportName, JSONObject body)
          throws WdkModelException, DataValidationException, RequestMisformatException {
    AnswerRequest request = parseAnswerRequest(getQuestionOrNotFound(_recordClassUrlSegment, _questionUrlSegment), reportName,
        body, getWdkModel(), getRequestingUser(), _avoidCacheHit);
    return getAnswerResponse(getRequestingUser(), request, getErrorContext()).getSecond();
  }

  /**
   * Similar to the custom report endpoint that takes JSON, but gets its data
   * from a form instead of JSON. It is used by the client to push the provided
   * data to a new http target (ie, a tab), for example, a download report
   *
   * @param data
   *   JSON data representing an answer request, passed in the 'data' form
   *   param
   *
   * @return generated report
   *
   * @throws RequestMisformatException
   *   if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException
   *   if JSON structure is correct but values contained are invalid
   * @throws WdkModelException
   *   if an error occurs while processing the request
   */
  @POST
  @Path(CUSTOM_REPORT_SEGMENT)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createCustomReportAnswerFromForm(
      @PathParam(REPORT_NAME_PATH_PARAM) String reportName,
      @FormParam("data") String data)
          throws WdkModelException, DataValidationException, RequestMisformatException {
    return createCustomReportAnswer(reportName, new JSONObject(data));
  }

  /**
   * {@code GET} form of the service method {@link #createStandardReportAnswer(JSONObject)}.
   * <p>
   * This method is required because of Jersey's route matching trying to run
   * {@code GET} requests through the {@code buildDefaultReporterResult} method.
   *
   * @see #createStandardReportAnswer(JSONObject)
   */
  @GET
  @Path(STANDARD_REPORT_SEGMENT)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswer()
  throws DataValidationException, WdkUserException, WdkModelException {
    return createCustomReportAnswer(DefaultJsonReporter.RESERVED_NAME);
  }

  /**
   * {@code GET} form of the {@link #createCustomReportAnswer(String, JSONObject)} service
   * method.
   *
   * @see #createCustomReportAnswer(String, JSONObject)
   */
  @GET
  @Path(CUSTOM_REPORT_SEGMENT)
  public Response createCustomReportAnswer(@PathParam(REPORT_NAME_PATH_PARAM) String report)
  throws WdkUserException, WdkModelException, DataValidationException {
    var params = getUriInfo().getQueryParameters();
    var searchConfig = new JSONObject().put(
      JsonKeys.PARAMETERS,
      params.keySet()
        .stream()
        .filter(not(JsonKeys.REPORT_CONFIG::equals))
        .reduce(
          new JSONObject(),
          (b, v) -> b.put(v, params.getFirst(v)),
          (a, b) -> a
        )
    );

    return createCustomReportAnswer(report, new JSONObject()
      .put(JsonKeys.SEARCH_CONFIG, searchConfig)
      .put(JsonKeys.REPORT_CONFIG, Optional.ofNullable(params.getFirst(JsonKeys.REPORT_CONFIG))
        .map(JSONObject::new)
        .orElseGet(JSONObject::new)));
  }

  static AnswerRequest parseAnswerRequest(Question question,
      String reporterName, JSONObject requestBody, WdkModel wdkModel, User sessionUser, boolean avoidCacheHit)
          throws RequestMisformatException, DataValidationException, WdkModelException {

    if (requestBody == null || !requestBody.has(JsonKeys.SEARCH_CONFIG) || !requestBody.has(JsonKeys.REPORT_CONFIG)) {
      throw new RequestMisformatException("Request body must not be null and must contain '" +
          JsonKeys.SEARCH_CONFIG + "' and '" + JsonKeys.REPORT_CONFIG + "' properties.");
    }

    // parse view filters
    FilterOptionListBuilder viewFilters = AnswerSpecServiceFormat.parseViewFilters(requestBody);

    // parse answer spec (question, params, etc.)
    RunnableObj<AnswerSpec> answerSpec = parseAnswerSpec(question,
        requestBody.getJSONObject(JsonKeys.SEARCH_CONFIG), wdkModel, sessionUser, viewFilters);

    // parse formatting
    AnswerFormatting formatting = new AnswerFormatting(reporterName,
        requestBody.getJSONObject(JsonKeys.REPORT_CONFIG));

    // create request
    return new AnswerRequest(answerSpec, formatting, avoidCacheHit);
  }

  private static RunnableObj<AnswerSpec> parseAnswerSpec(Question question,
      JSONObject answerSpecJson, WdkModel wdkModel, User sessionUser, FilterOptionListBuilder viewFilters)
          throws RequestMisformatException, WdkModelException, DataValidationException {
    AnswerSpecBuilder specBuilder = AnswerSpecServiceFormat
      .parse(question, answerSpecJson, wdkModel)
      .setViewFilterOptions(viewFilters);
    StepContainer stepContainer = loadContainer(specBuilder, wdkModel, sessionUser);
    return specBuilder
        .build(sessionUser, stepContainer, ValidationLevel.RUNNABLE)
        .getRunnable()
        .getOrThrow(spec -> new DataValidationException(spec.getValidationBundle()));
  }

  // TODO: now that this method is public, should find a better place for it
  public static StepContainer loadContainer(AnswerSpecBuilder specBuilder,
      WdkModel wdkModel, User sessionUser) throws WdkModelException, DataValidationException {

    // to allow a user to use steps from an existing strategy, need to get the
    // strategy they want to use as a step container to look up those steps;
    // can't do that without knowing if the question is valid
    Optional<Question> question = wdkModel.getQuestionByFullName(specBuilder.getQuestionName());

    if (question.isEmpty() || question.get().getQuery().getAnswerParamCount() == 0) {
      // question will fail validation or is valid but does not contain answer
      // params; no need for lookup
      return StepContainer.emptyContainer();
    }

    Strategy strategy = null;
    List<Step> stepsForLookup = new ArrayList<>();
    for (AnswerParam answerParam : question.get().getQuery().getAnswerParams()) {
      String stableValue = specBuilder.getParamValue(answerParam.getName());
      String notFoundMessage = "Answer Param value '" + stableValue + "' does not refer to a step.";
      if (!FormatUtil.isInteger(stableValue)) {
        throw new DataValidationException(notFoundMessage);
      }
      long stepId = Long.parseLong(stableValue);
      if (strategy == null) {
        // have not selected a strategy yet
        Step step = wdkModel.getStepFactory().getStepByIdAndUserId(
            stepId, sessionUser.getUserId(), ValidationLevel.RUNNABLE)
            .orElseThrow(() -> new DataValidationException(notFoundMessage));
        if (step.getStrategy().isEmpty()) {
          stepsForLookup.add(step); // stand-alone step; add it
        }
        else {
          strategy = step.getStrategy().get(); // this becomes our one and only strategy
        }
      }
      else {
        // strategy has been selected; see if this step is in it
        if (strategy.findFirstStep(StepContainer.withId(stepId)).isPresent()) {
          // nothing to do here; referred step lives in this strategy
        }
        else {
          Step step = wdkModel.getStepFactory().getStepById(stepId, ValidationLevel.RUNNABLE)
              .orElseThrow(() -> new DataValidationException(notFoundMessage));
          if (step.getStrategy().isEmpty()) {
            stepsForLookup.add(step); // stand-alone step; add it
          }
          else {
            throw new DataValidationException("Only one strategy at a time can be used as a source of referred steps.");
          }
        }
      }
    }

    // make sure all referred steps are owned by the session user
    if (strategy != null && strategy.getUser().getUserId() != sessionUser.getUserId()) {
      throw new DataValidationException("You do not have permission to use the steps in strategy with ID " + strategy.getStrategyId() + "'.");
    }
    for (Step step : stepsForLookup) {
      if (step.getUser().getUserId() != sessionUser.getUserId()) {
        throw new DataValidationException("You do not have permission to use step '" + step.getStepId() + "'.");
      }
    }

    // build a container that contains all needed steps
    ListStepContainer container = new ListStepContainer();
    container.addAll(strategy.getAllSteps());
    container.addAll(stepsForLookup);
    return container;

  }

  /**
   * Creates a streaming answer response as the passed user from the passed
   * answer spec and formatting configuration.  To get the default (i.e.
   * standard WDK service JSON) reporter with default configuration, pass null
   * as formatting.
   *
   * @param sessionUser
   *   user answer is to be generated as
   *
   * @return streaming response representing the formatted answer
   *
   * @throws RequestMisformatException
   *   if reporter does not support the passed formatConfig object
   * @throws DataValidationException
   *   if answerSpec or formatting are syntactically valid but the data itself
   *   is invalid
   * @throws WdkModelException
   *   if an application error occurs
   */
  public static TwoTuple<AnswerValue,Response> getAnswerResponse(User sessionUser, AnswerRequest request, ErrorContext errorContext)
      throws RequestMisformatException, WdkModelException, DataValidationException {

    // create base answer value from answer spec
    AnswerValue answerValue = AnswerValueFactory.makeAnswer(sessionUser, request.getAnswerSpec(), request.avoidCacheHit());

    // parse (optional) request details (columns, pagination, etc.- format dependent on reporter) and configure reporter
    Reporter reporter = getConfiguredReporter(answerValue, request.getFormatting());

    // RRD scrum 12/2/2022: ensure ability to cache result before beginning stream by checking ID sql
    //    This will prevent a class of in-stream errors from returning 200 HTTP status vs. the desired 500
    answerValue.getIdSql();

    // build response from stream, apply delivery details, and return
    ResponseBuilder builder = Response.ok(getAnswerAsStream(reporter, errorContext)).type(reporter.getHttpContentType());
    return new TwoTuple<>(answerValue, applyDisposition(
        builder, reporter.getContentDisposition(), reporter.getDownloadFileName()).build());
  }

  /**
   * Returns configured reporter based on passed answer value and formatting
   * JSON
   *
   * @param answerValue
   *   answer value for which reporter should be constructed
   * @param formatting
   *   formatting object if one was passed, else null
   *
   * @return configured reporter
   *
   * @throws RequestMisformatException
   *   if required property is not present or the wrong type
   * @throws DataValidationException
   *   if a value passed in the configuration is invalid
   * @throws WdkModelException
   *   if unable to create reporter due to another reason
   */
  private static Reporter getConfiguredReporter(AnswerValue answerValue, AnswerFormatting formatting)
      throws RequestMisformatException, WdkModelException, DataValidationException {

    String formatName = formatting.getFormat();
    try {

      // check to make sure format name is valid for this recordclass
      if (!answerValue.getAnswerSpec().getQuestion().getReporterMap().containsKey(formatName)) {
        throw new DataValidationException("Request for an invalid answer format: " + formatName);
      }

      // configure reporter requested
      LOG.debug("Creating and configuring reporter for format '" + formatName + "'");
      return ReporterFactory.getReporter(answerValue, formatName, formatting.getFormatConfig());
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON structure: " + e.getMessage());
    }
    catch (ReporterConfigException e) {
      switch(e.getErrorType()) {
        case MISFORMAT:
          throw new RequestMisformatException("Could not configure reporter '" + formatName + "' with passed formatConfig. " + e.getMessage());
        case DATA_VALIDATION:
        default: // added here for compilation; remove when new Java switch implemented
          throw new DataValidationException("Could not configure reporter '" + formatName + "' with passed formatConfig. " + e.getMessage());
      }
    }
  }

  public static StreamingOutput getAnswerAsStream(final Reporter reporter, final ErrorContext errorContext) {
    return stream -> {
      try {
        Timer t = new Timer();
        reporter.report(stream);
        LOG.info("Wrote report of type " + reporter.getClass().getSimpleName() + " in " + t.getElapsedString());
      }
      catch (WdkModelException | WdkRuntimeException e) {
        // send error email and log
        LOG.error("log4j marker: " + errorContext.getLogMarker());
        Events.trigger(new ErrorEvent(new ServerErrorBundle(e), errorContext));

        // write alert message to the end of the stream in hopes of alerting user
        stream.write((
            " ********************************************* " + NL + 
            " ********************************************* " + NL + 
            " *************** ERROR **************** " + NL + 
            "We're sorry, but an error occurred while streaming your result and your request cannot be completed.  " + NL + 
            "Please contact us with a description of your download." + NL + NL).getBytes());
        throw new WebApplicationException(e);
      }
    };
  }

  private static ResponseBuilder applyDisposition(ResponseBuilder response,
      ContentDisposition disposition, String filename) throws WdkModelException {
    switch(disposition) {
      case INLINE:
        response.header(HttpHeaders.CONTENT_DISPOSITION, "inline");
        break;
      case ATTACHMENT:
        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        break;
      default:
        throw new WdkModelException("Unsupported content disposition: " + disposition);
    }
    return response;
  }

}
