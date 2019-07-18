package org.gusdb.wdk.service.service.user;

import static org.gusdb.wdk.service.service.AnswerService.CUSTOM_REPORT_SEGMENT_PAIR;
import static org.gusdb.wdk.service.service.AnswerService.REPORT_NAME_PATH_PARAM;
import static org.gusdb.wdk.service.service.AnswerService.STANDARD_REPORT_SEGMENT_PAIR;
import static org.gusdb.wdk.service.service.search.SearchColumnService.NAMED_COLUMN_SEGMENT_PAIR;

import java.util.Date;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.request.AnswerFormatting;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactory;
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

import com.fasterxml.jackson.databind.JsonNode;

public class StepService extends UserService {

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
      throws WdkModelException, DataValidationException {
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
    catch (JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
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

  @POST
  @Path(NAMED_STEP_PATH + CUSTOM_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_JSON)
  // Produces an unknown media type; varies depending on reporter selected
  public Response createCustomReportAnswer(
      @PathParam(STEP_ID_PATH_PARAM) long stepId,
      @PathParam(REPORT_NAME_PATH_PARAM) String reporterName,
      JSONObject requestJson)
          throws WdkModelException, RequestMisformatException, DataValidationException {

    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    StepFactory stepFactory = getWdkModel().getStepFactory();
    Step step = stepFactory
        .getStepByIdAndUserId(stepId, user.getUserId(), ValidationLevel.NONE)
        .orElseThrow(() -> new NotFoundException(formatNotFound(STEP_RESOURCE + stepId)));
    if (!step.getStrategy().isPresent()) {
      throw new DataValidationException("Step " + step.getStepId() + " is not part of a strategy, so cannot run.");
    }

    RunnableObj<Step> runnableStep = stepFactory
        .getStepById(stepId, ValidationLevel.RUNNABLE)
        .orElseThrow(() -> new NotFoundException(formatNotFound(STEP_RESOURCE + stepId)))
        .getRunnable()
        .getOrThrow(StepService::getNotRunnableException);

    AnswerRequest request = new AnswerRequest(
        Step.getRunnableAnswerSpec(runnableStep),
        new AnswerFormatting(reporterName, requestJson));

    TwoTuple<AnswerValue, Response> result = AnswerService.getAnswerResponse(user, request);

    // update the estimated size and last-run time on this step
    stepFactory.updateStep(
        Step.builder(runnableStep.get())
        .setEstimatedSize(result.getFirst().getResultSizeFactory().getDisplayResultSize())
        .setLastRunTime(new Date())
        .build(new UserCache(runnableStep.get().getUser()),
            ValidationLevel.NONE, runnableStep.get().getStrategy()));

    return result.getSecond();
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
      allowInvalid // allow PUTing of invalid steps so we can test how we handle them elsewhere
      ? AnswerSpecServiceFormat
          .parse(existingStep.getAnswerSpec().getQuestion(), body, getWdkModel())
          .build(user, existingStep.getContainer(), ValidationLevel.SEMANTIC)
      : StepRequestParser
          .getReplacementAnswerSpec(existingStep, body, getWdkModel(), user)
          .get();

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
            .build(new UserCache(user), ValidationLevel.SEMANTIC)
            .getSemanticallyValid()
            .getOrThrow(strat -> new DataValidationException(
                "The passed answer spec is not semantically valid."))
            .get());
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
      final JsonNode reporterConfig)
          throws WdkModelException, DataValidationException, NotFoundException, WdkUserException {
    RunnableObj<Step> existingStep = getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE)
        .getRunnable().getOrThrow(StepService::getNotRunnableException);
    Question question = existingStep.get().getAnswerSpec().getQuestion();
    AttributeField attribute = requireColumn(question, columnName);
    return ColumnReporterService.wrapReporter(
        attribute.prepareReporter(
            reporterName,
            AnswerValueFactory.makeAnswer(existingStep),
            reporterConfig
        ).orElseThrow(ColumnReporterService.makeNotFound(attribute, reporterName))
    );
  }

  private static DataValidationException getNotRunnableException(Step badStep) {
    return new DataValidationException(
        "This step is not runnable for the following reasons: " + badStep.getValidationBundle().toString());
  }

}
