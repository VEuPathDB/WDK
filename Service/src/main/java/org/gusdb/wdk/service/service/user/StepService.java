package org.gusdb.wdk.service.service.user;

import static org.gusdb.wdk.model.answer.request.AnswerFormattingParser.DEFAULT_REPORTER_PARSER;
import static org.gusdb.wdk.model.answer.request.AnswerFormattingParser.SPECIFIED_REPORTER_PARSER;

import java.util.Date;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
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

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.request.AnswerFormattingParser;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.NoSuchElementException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.StepFactoryHelpers.UserCache;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StepRequestParser;
import org.gusdb.wdk.service.request.strategy.StepRequestParser.NewStepRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.AnswerService;
import org.json.JSONException;
import org.json.JSONObject;

public class StepService extends UserService {

  private static final String BASE_PATH = "steps";
  private static final String ID_PARAM = "stepId";
  private static final String ID_PATH = BASE_PATH + "/{" + ID_PARAM + "}";

  public static final String STEP_RESOURCE = "Step ID ";

  public StepService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @POST
  @Path(BASE_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.steps.post-request")
  @OutSchema("wdk.creation-post-response")
  public Response createStep(JSONObject jsonBody)
      throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      NewStepRequest stepRequest = StepRequestParser.newStepFromJson(jsonBody, getWdkModel(), user);
      Step step = getWdkModel().getStepFactory().createStep(
          user,
          stepRequest.getAnswerSpec(),
          stepRequest.getCustomName(),
          stepRequest.isCollapsible(),
          stepRequest.getCollapsedName());
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
  @Path(ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.users.steps.id.get-response")
  public JSONObject getStep(
    @PathParam(ID_PARAM) long stepId,
    @QueryParam("validationLevel") String validationLevelStr
  ) throws WdkModelException {
    ValidationLevel validationLevel = Functions.defaultOnException(
      () -> ValidationLevel.valueOf(validationLevelStr),
      ValidationLevel.RUNNABLE);
    return StepFormatter.getStepJsonWithResultSize(getStepForCurrentUser(
        stepId, validationLevel));
  }

  /**
   * @param stepId ID of the step to update
   * @param body JSON body containing only fields to update on the step
   * @throws RequestMisformatException 
   */
  @PATCH
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.steps.id.patch-request")
  public void updateStepMeta(@PathParam(ID_PARAM) long stepId,
      JSONObject body) throws WdkModelException, RequestMisformatException {
    if (body.length() != 0) {
      Step step = StepRequestParser.updateStepMeta(getStepForCurrentUser(stepId, ValidationLevel.NONE), body);
      getWdkModel().getStepFactory().updateStep(step);
    }
  }

  @DELETE
  @Path(ID_PATH)
  public void deleteStep(@PathParam(ID_PARAM) long stepId)
      throws WdkModelException, ConflictException {

    Step step = getStepForCurrentUser(stepId, ValidationLevel.NONE);
    if (step.isDeleted())
      throw new NotFoundException(
          AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));

    if (step.getStrategy() != null)
      throw new ConflictException(
        "Steps that are part of strategies cannot be deleted.  Remove the " +
          "step from strategy " + step.getStrategyId() + " and try again.");

    getWdkModel().getStepFactory()
      .updateStep(Step.builder(step)
        .setDeleted(true)
        .build(new UserCache(step.getUser()), ValidationLevel.NONE, null));
  }

  @POST
  @Path(ID_PATH + "/answer")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.steps.answer.post-request")
  public Response createDefaultReporterAnswer(@PathParam(ID_PARAM) long stepId, JSONObject body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    return createAnswer(stepId, body, DEFAULT_REPORTER_PARSER);
  }

  @POST
  @Path(ID_PATH + "/answer/report")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createAnswer(@PathParam(ID_PARAM) long stepId, JSONObject body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    return createAnswer(stepId, body, SPECIFIED_REPORTER_PARSER);
  }

  @PUT
  @Path(ID_PATH + "/answerSpec")
  @Consumes(MediaType.APPLICATION_JSON)
  // TODO: @InSchema(...)
  public void putAnswerSpec(
      @PathParam(ID_PARAM) long stepId,
      JSONObject body
  ) throws WdkModelException, DataValidationException, RequestMisformatException {

    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    Step existingStep = getStepForCurrentUser(stepId, ValidationLevel.NONE);
    SemanticallyValid<AnswerSpec> newSpec = StepRequestParser.getReplacementAnswerSpec(
        existingStep, body, getWdkModel(), user);
    StepBuilder replacementBuilder = Step.builder(existingStep)
        .setAnswerSpec(AnswerSpec.builder(newSpec));

    if (existingStep.hasStrategy()) {
      try {
        // need to replace and update whole strategy to cover effects
        getWdkModel().getStepFactory().updateStrategy(Strategy
            .builder(existingStep.getStrategy())
            .addStep(replacementBuilder)
            .build(new UserCache(user), ValidationLevel.SEMANTIC)
            .getSemanticallyValid()
            .getOrThrow(strat -> new DataValidationException(
                "The passed answer spec is not semantically valid."))
            .getObject());
      }
      catch (InvalidStrategyStructureException e) {
        throw new DataValidationException("Invalid strategy structure passed. " + e.getMessage(), e);
      }
    }
    else {
      // no strategy present; only need to update the step
      getWdkModel().getStepFactory().updateStep(replacementBuilder.build(
          new UserCache(user), ValidationLevel.SEMANTIC, null));
    }
  }

  @GET
  @Path(ID_PATH + "/answer/filter-summary/{filterName}")
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getFilterSummary(
    @PathParam(ID_PATH) long stepId,
    @PathParam("filterName") String filterName
  ) throws WdkModelException, DataValidationException {
    return AnswerValueFactory.makeAnswer(
        getUserBundle(Access.PRIVATE).getSessionUser(),
        Step.getRunnableAnswerSpec(
          getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE)
            .getRunnable()
            .getOrThrow(StepService::getNotRunnableException)))
      .getFilterSummaryJson(filterName);
  }

  private static DataValidationException getNotRunnableException(Step badStep) {
    return new DataValidationException(
        "This step is not runnable for the following reasons: " + badStep.getValidationBundle().toString());
  }

  private Response createAnswer(long stepId, JSONObject requestBody, AnswerFormattingParser formattingParser)
      throws WdkModelException, RequestMisformatException, DataValidationException {

    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    StepFactory stepFactory = new StepFactory(getWdkModel());
    RunnableObj<Step> runnableStep = stepFactory
        .getStepById(stepId, ValidationLevel.RUNNABLE)
        .orElseThrow(NotFoundException::new)
        .getRunnable()
        .getOrThrow(StepService::getNotRunnableException);

    AnswerRequest request = new AnswerRequest(
        Step.getRunnableAnswerSpec(runnableStep),
        formattingParser.createFromTopLevelObject(requestBody));
    TwoTuple<AnswerValue, Response> result = AnswerService.getAnswerResponse(user, request);

    // update the estimated size and last-run time on this step
    stepFactory.updateStep(
      Step.builder(runnableStep.getObject())
        .setEstimatedSize(result.getFirst().getResultSizeFactory().getDisplayResultSize())
        .setLastRunTime(new Date())
        .build(new UserCache(runnableStep.getObject().getUser()),
            ValidationLevel.NONE, runnableStep.getObject().getStrategy()));

    return result.getSecond();
  }

  private Step getStepForCurrentUser(long stepId, ValidationLevel level) throws WdkModelException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      Step step = getWdkModel().getStepFactory()
          .getStepById(stepId, level)
          .orElseThrow(() -> new NoSuchElementException("Cannot find step with ID " + stepId));

      if (step.getUser().getUserId() != user.getUserId())
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);

      return step;
    }
    catch (NoSuchElementException e) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));
    }
  }
}
