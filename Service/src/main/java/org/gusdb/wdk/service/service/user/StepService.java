package org.gusdb.wdk.service.service.user;

import static org.gusdb.wdk.service.service.AnswerService.CUSTOM_REPORT_SEGMENT_PAIR;
import static org.gusdb.wdk.service.service.AnswerService.REPORT_NAME_PATH_PARAM;
import static org.gusdb.wdk.service.service.AnswerService.STANDARD_REPORT_SEGMENT_PAIR;
import static org.gusdb.wdk.service.service.search.SearchColumnService.NAMED_COLUMN_SEGMENT_PAIR;

import java.util.Date;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.request.AnswerFormatting;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserCache;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StepRequestParser;
import org.gusdb.wdk.service.request.strategy.StepRequestParser.NewStepRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.search.ColumnReporterService;
import org.gusdb.wdk.service.service.search.SearchColumnService;
import org.json.JSONException;
import org.json.JSONObject;

public class StepService extends UserService {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(StepService.class);

  private static final String STEPS_SEGMENT = "steps";
  public static final String STEP_ID_PATH_PARAM = "stepId";
  public static final String NAMED_STEP_PATH = STEPS_SEGMENT + "/{" + STEP_ID_PATH_PARAM + "}";
  private static final String COLUMN_REPORTER_PATH =
      NAMED_STEP_PATH + NAMED_COLUMN_SEGMENT_PAIR + CUSTOM_REPORT_SEGMENT_PAIR;

  public StepService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @POST
  @Path(STEPS_SEGMENT)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.steps.post-request")
  @OutSchema("wdk.standard-post-response")
  public Response createStep(JSONObject jsonBody)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      NewStepRequest stepRequest = StepRequestParser.newStepFromJson(jsonBody, getWdkModel(), user);
      Step step = getWdkModel().getStepFactory().createStep(
          user,
          stepRequest.getAnswerSpec(),
          stepRequest.getCustomName(),
          stepRequest.isExpanded(),
          stepRequest.getExpandedName(),
          stepRequest.getDisplayPrefs()).get();
      return Response.ok(new JSONObject()
          .put(JsonKeys.ID, step.getStepId()))
          .location(getUriInfo()
              .getAbsolutePathBuilder()
              .build(step.getStepId()))
          .build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  @GET
  @Path(NAMED_STEP_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.users.steps.id.get-response")
  public JSONObject getStep(
    @PathParam(STEP_ID_PATH_PARAM) long stepId,
    @QueryParam("validationLevel") String validationLevelStr
  ) throws WdkModelException {
    ValidationLevel validationLevel = Functions.defaultOnException(
      () -> ValidationLevel.valueOf(validationLevelStr),
      ValidationLevel.RUNNABLE);
    return StepFormatter.getStepJsonWithEstimatedSize(getStepForCurrentUser(
        stepId, validationLevel));
  }

  /**
   * @param stepId ID of the step to update
   * @param body JSON body containing only fields to update on the step
   * @throws RequestMisformatException
   */
  @PATCH
  @Path(NAMED_STEP_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.steps.id.patch-request")
  public void updateStepMeta(@PathParam(STEP_ID_PATH_PARAM) long stepId,
      JSONObject body) throws WdkModelException, RequestMisformatException {
    if (body.length() != 0) {
      Step step = StepRequestParser.updateStepMeta(getStepForCurrentUser(stepId, ValidationLevel.NONE), body);
      getWdkModel().getStepFactory().updateStep(step);
    }
  }

  @DELETE
  @Path(NAMED_STEP_PATH)
  public void deleteStep(@PathParam(STEP_ID_PATH_PARAM) long stepId)
      throws WdkModelException, ConflictException {

    Step step = getStepForCurrentUser(stepId, ValidationLevel.NONE);
    if (step.isDeleted())
      throw new NotFoundException(
          AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));

    if (step.getStrategy().isPresent())
      throw new ConflictException(
        "Steps that are part of strategies cannot be deleted.  Remove the " +
          "step from strategy " + step.getStrategyId().get() + " and try again.");

    getWdkModel().getStepFactory()
      .updateStep(Step.builder(step)
        .setDeleted(true)
        .build(new UserCache(step.getUser()), ValidationLevel.NONE, Optional.empty()));
  }

  @GET
  @Path(NAMED_STEP_PATH + STANDARD_REPORT_SEGMENT_PAIR)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswerFromGet(
      @PathParam(STEP_ID_PATH_PARAM) long stepId)
          throws WdkModelException, RequestMisformatException, DataValidationException {
    return createCustomReportAnswerFromGet(stepId, DefaultJsonReporter.RESERVED_NAME);
  }

  @GET
  @Path(NAMED_STEP_PATH + CUSTOM_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_JSON)
  // Produces an unknown media type; varies depending on reporter selected
  public Response createCustomReportAnswerFromGet(
      @PathParam(STEP_ID_PATH_PARAM) long stepId,
      @PathParam(REPORT_NAME_PATH_PARAM) String reporterName)
          throws WdkModelException, RequestMisformatException, DataValidationException {

    var params = getUriInfo().getQueryParameters();
    return createCustomReportAnswer(stepId, reporterName,
      new JSONObject().put(JsonKeys.REPORT_CONFIG,
        Optional.ofNullable(params.getFirst(JsonKeys.REPORT_CONFIG))
          .map(JSONObject::new)
          .orElseGet(JSONObject::new)));
  }

  @POST
  @Path(NAMED_STEP_PATH + STANDARD_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.steps.answer.post-request")
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswer(
      @PathParam(STEP_ID_PATH_PARAM) long stepId, JSONObject requestJson)
          throws WdkModelException, RequestMisformatException, DataValidationException {
    return createCustomReportAnswer(stepId, DefaultJsonReporter.RESERVED_NAME, requestJson);
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
  @Path(NAMED_STEP_PATH + STANDARD_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswerFromForm(@PathParam(STEP_ID_PATH_PARAM) long stepId,
      @FormParam("data") String data)
          throws WdkModelException, DataValidationException, RequestMisformatException {
    return createCustomReportAnswerFromForm(stepId, DefaultJsonReporter.RESERVED_NAME, data);
  }

  @POST
  @Path(NAMED_STEP_PATH + CUSTOM_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_JSON)
  // Produces an unknown media type; varies depending on reporter selected
  public Response createCustomReportAnswer(
      @PathParam(STEP_ID_PATH_PARAM) long stepId,
      @PathParam(REPORT_NAME_PATH_PARAM) String reporterName,
      JSONObject requestJson)
          throws WdkModelException, RequestMisformatException, DataValidationException {

    if (requestJson == null) {
      throw new RequestMisformatException("A request body is required at this endpoint.");
    }

    // don't validate step right away; do it after we clean filters and add any view filters
    Step step = getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE);

    // only allow step to be run from service if part of a strategy (even if otherwise runnable)
    if (!step.getStrategy().isPresent()) {
      throw new DataValidationException("Step " + step.getStepId() + " is not part of a strategy, so cannot run.");
    }

    // create a runnable answer spec with view filters applied (if present)
    RunnableObj<AnswerSpec> runnableSpec = AnswerSpec
        .builder(step.getAnswerSpec())
        .setViewFilterOptions(AnswerSpecServiceFormat.parseViewFilters(requestJson))
        .build(step.getUser(), step.getContainer(), ValidationLevel.RUNNABLE)
        .getRunnable()
        .getOrThrow(StepService::getNotRunnableException);

    // execute reporter against the answer spec
    AnswerRequest request = new AnswerRequest(runnableSpec,
        new AnswerFormatting(reporterName, requestJson.getJSONObject(JsonKeys.REPORT_CONFIG)));
    TwoTuple<AnswerValue, Response> result = AnswerService.getAnswerResponse(step.getUser(), request);

    // update the estimated size and last-run time on this step
    getWdkModel().getStepFactory().updateStep(
        Step.builder(step)
        .setEstimatedSize(result.getFirst().getResultSizeFactory().getDisplayResultSize())
        .setLastRunTime(new Date())
        .build(new UserCache(step.getUser()),
            ValidationLevel.NONE, step.getStrategy()));

    return result.getSecond();
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
  @Path(NAMED_STEP_PATH + CUSTOM_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createCustomReportAnswerFromForm(
      @PathParam(STEP_ID_PATH_PARAM) long stepId,
      @PathParam(REPORT_NAME_PATH_PARAM) String reportName,
      @FormParam("data") String data)
          throws WdkModelException, DataValidationException, RequestMisformatException {
    AnswerService.preHandleFormRequest(getUriInfo(), data);
    return createCustomReportAnswer(stepId, reportName, new JSONObject(data));
  }

  @PUT
  @Path(NAMED_STEP_PATH + "/search-config")
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.answer.answer-spec-request")
  public void putAnswerSpec(
      @PathParam(STEP_ID_PATH_PARAM) long stepId,
      @QueryParam("allowInvalid") @DefaultValue("false") Boolean allowInvalid,  // undocumented.  for use by developers
      JSONObject body
  ) throws WdkModelException, DataValidationException, RequestMisformatException {

    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    Step existingStep = getStepForCurrentUser(stepId, ValidationLevel.NONE);

    AnswerSpec newSpec =
      allowInvalid
      // allow PUTing of invalid steps so we can test how we handle them elsewhere
      ? AnswerSpecServiceFormat
          .parse(existingStep.getAnswerSpec().getQuestion(), body, getWdkModel())
          .build(user, existingStep.getContainer(), ValidationLevel.SEMANTIC)
      : StepRequestParser
          .getReplacementAnswerSpec(existingStep, body, getWdkModel(), user)
          .get();

    StepRequestParser.assertAnswerParamsUnmodified(existingStep, newSpec);

    StepBuilder replacementBuilder = Step.builder(existingStep)
        .setAnswerSpec(AnswerSpec.builder(newSpec))
        .setResultSizeDirty(true);

    // allows subclasses to apply follow-up modifications to the new version
    applyAdditionalChanges(existingStep, replacementBuilder);

    if (existingStep.getStrategy().isPresent()) {
      try {
        // need to replace and update whole strategy to cover effects
        getWdkModel().getStepFactory().updateStrategy(Strategy
            .builder(existingStep.getStrategy().get())
            .addStep(replacementBuilder)
            .setLastModifiedTime(new Date())
            .build(new UserCache(user), ValidationLevel.SEMANTIC));
      }
      catch (InvalidStrategyStructureException e) {
        throw new DataValidationException("Invalid strategy structure passed. " + e.getMessage(), e);
      }
    }
    else {
      // no strategy present; only need to update the step
      getWdkModel().getStepFactory().updateStep(replacementBuilder.build(
          new UserCache(user), ValidationLevel.SEMANTIC, Optional.empty()));
    }
  }

  /**
   * Apply any desired changes based on the modifications made already
   *
   * @param existingStep previous version of the step
   * @param replacementBuilder a replacement builder with client-requested changes applied
   * @throws WdkModelException if unable to make the appropriate changes
   */
  protected void applyAdditionalChanges(Step existingStep, StepBuilder replacementBuilder) throws WdkModelException {
    // do nothing by default
  }

  @GET
  @Path(NAMED_STEP_PATH + "/filter-summary/{filterName}")
  @Produces(MediaType.APPLICATION_JSON)
  @Deprecated
  public JSONObject getFilterSummary(
    @PathParam(STEP_ID_PATH_PARAM) long stepId,
    @PathParam("filterName") String filterName
  ) throws WdkModelException, DataValidationException, WdkUserException {
    return AnswerValueFactory.makeAnswer(
        getUserBundle(Access.PRIVATE).getSessionUser(),
        Step.getRunnableAnswerSpec(
          getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE)
            .getRunnable()
            .getOrThrow(StepService::getNotRunnableException)))
      .getFilterSummaryJson(filterName);
  }

  @POST
  @Path(COLUMN_REPORTER_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  public StreamingOutput getColumnReporterResponse(
      @PathParam(STEP_ID_PATH_PARAM) final long stepId,
      @PathParam(SearchColumnService.COLUMN_PATH_PARAM) final String columnName,
      @PathParam(REPORT_NAME_PATH_PARAM) final String reporterName,
      final JSONObject requestJson)
          throws WdkModelException, DataValidationException, NotFoundException, WdkUserException {

    // validate step at runnable level so that its container will contain runnable steps
    Step step = getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE);

    // only allow step to be run from service if part of a strategy (even if otherwise runnable)
    if (!step.getStrategy().isPresent()) {
      throw new DataValidationException("Step " + step.getStepId() + " is not part of a strategy, so cannot run.");
    }

    // trim off a filter linked to this reporter if present and apply view filters
    AnswerSpecBuilder specBuilder = ColumnReporterService.trimColumnFilter(
        new AnswerSpecBuilder(step.getAnswerSpec()), columnName, reporterName);
    RunnableObj<AnswerSpec> trimmedSpec = specBuilder
        .setViewFilterOptions(AnswerSpecServiceFormat.parseViewFilters(requestJson))
        .build(step.getUser(), step.getContainer(), ValidationLevel.RUNNABLE)
        .getRunnable()
        .getOrThrow(StepService::getNotRunnableException);

    // make sure passed column is valid for this question
    Question question = trimmedSpec.get().getQuestion();
    AttributeField attribute = requireColumn(question, columnName);

    // execute reporter against the runnable answer spec
    return AnswerService.getAnswerAsStream(
        attribute.makeReporterInstance(
            reporterName,
            AnswerValueFactory.makeAnswer(step.getUser(), trimmedSpec),
            JsonUtil.toJsonNode(requestJson.getJSONObject(JsonKeys.REPORT_CONFIG))
        ).orElseThrow(ColumnReporterService.makeNotFound(attribute, reporterName))
    );
  }

  private static DataValidationException getNotRunnableException(Validateable<?> badSpec) {
    return new DataValidationException(
        "This step is not runnable for the following reasons: " + badSpec.getValidationBundle().toString());
  }
}
