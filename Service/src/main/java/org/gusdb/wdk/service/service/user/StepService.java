package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.TestUtil.nullSafeEquals;
import static org.gusdb.wdk.model.answer.request.AnswerFormattingParser.DEFAULT_REPORTER_PARSER;
import static org.gusdb.wdk.model.answer.request.AnswerFormattingParser.SPECIFIED_REPORTER_PARSER;

import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.request.AnswerFormattingParser;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.NoSuchElementException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StepRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.AnswerService;
import org.json.JSONException;
import org.json.JSONObject;

public class StepService extends UserService {

  public static final String STEP_RESOURCE = "Step ID ";

  public StepService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @POST
  @Path("steps")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk/users/steps/post-request")
  public Response createStep(@QueryParam("runStep") Boolean runStep, JSONObject jsonBody) throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      StepRequest stepRequest = StepRequest.newStepFromJson(jsonBody, getWdkModel(), user);
      Step newStep = getWdkModel().getStepFactory().createStep(
          user, stepRequest.getAnswerSpec(), filter, filterOptions,
          assignedWeight, deleted, customName, isCollapsible, collapsedName, strategy);
      
      // create the step and insert into the database
      Step step = createStep(stepRequest, user, getWdkModel().getStepFactory());
      if (runStep != null && runStep) {
        if (step.isAnswerSpecComplete()) {
          AnswerSpec stepAnswerSpec = AnswerSpecServiceFormat.parse(step);
          new AnswerValueFactory(user).createFromAnswerSpec(stepAnswerSpec);
        }
        else {
          throw new DataValidationException("Cannot run a step with an incomplete answer spec.");
        }
      }
      return Response.ok(new JSONObject().put(JsonKeys.ID, step.getStepId()))
          .location(getUriInfo().getAbsolutePathBuilder().build(step.getStepId()))
          .build();
    }
    catch (JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  @GET
  @Path("steps/{stepId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStep(@PathParam("stepId") String stepId) throws WdkModelException {
    return Response.ok(StepFormatter.getStepJsonWithEstimatedSize(getStepForCurrentUser(stepId)).toString()).build();
  }

  @PATCH
  @Path("steps/{stepId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateStep(@PathParam("stepId") String stepId, String body) throws WdkModelException, DataValidationException {
    try {
      Step step = getStepForCurrentUser(stepId);
      JSONObject patchJson = new JSONObject(body);
      StepRequest stepRequest = StepRequest.patchStepFromJson(step, patchJson, getWdkModel(), getSessionUser());
      step = updateStep(step, stepRequest);

      // save parts of step that changed
      if (stepRequest.isAnswerSpecModified()) {

        // save the clob to the DB
        step.saveParamFilters();

        // TODO: don't forget to set result size dirty in this step; means
        //   we don't have to call resetEstimateSizeForThisAndDownstreamSteps() or resetEstimatedSize() any more
        
        // reset the estimated size in the database for this step and any downstream steps, if any
        getWdkModel().getStepFactory().resetEstimateSizeForThisAndDownstreamSteps(step);

        // reset the current step object estimate size
        step.resetEstimatedSize();
      }

      // always update other data
      step.update(true);

      // return updated step
      return Response.ok(StepFormatter.getStepJsonWithRawEstimateValue(step).toString()).build();
    }
    catch (WdkUserException wue) {
    	  throw new DataValidationException(wue);
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  @DELETE
  @Path("steps/{stepId}")
  public Response deleteStep(@PathParam("stepId") String stepId) throws WdkModelException, ConflictException {
    Step step = getStepForCurrentUser(stepId);
    if (step.isDeleted()) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));
    }
    if (step.getStrategy() != null) {
      throw new ConflictException("Steps that are part of strategies cannot be " +
          "deleted.  Remove the step from strategy " + step.getStrategyId() + " and try again.");
    }
    step.setDeleted(true);
    step.update(true);
    return Response.noContent().build();
  }

  @POST
  @Path("steps/{stepId}/answer")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createDefaultReporterAnswer(@PathParam("stepId") String stepId, String body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    return createAnswer(stepId, body, DEFAULT_REPORTER_PARSER);
  }

  @POST
  @Path("steps/{stepId}/answer/report")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createAnswer(@PathParam("stepId") String stepId, String body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    return createAnswer(stepId, body, SPECIFIED_REPORTER_PARSER);
  }

  private Response createAnswer(String stepId, String requestBody, AnswerFormattingParser formattingParser)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      StepFactory stepFactory = new StepFactory(getWdkModel());
      Step step = stepFactory.getStepById(Long.parseLong(stepId));
      if(!step.isAnswerSpecComplete()) {
        throw new DataValidationException("One or more parameters is missing");
      }
 
      AnswerSpec stepAnswerSpec = AnswerSpecServiceFormat.createFromStep(step);
      AnswerRequest request = new AnswerRequest(stepAnswerSpec, formattingParser.createFromTopLevelObject(requestBody));
      return AnswerService.getAnswerResponse(user, request);
    }
    catch(NumberFormatException nfe) {
      throw new NotFoundException(formatNotFound("step ID " + stepId));
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }  
  
  @GET
  @Path("steps/{stepId}/answer/filter-summary/{filterName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFilterSummary(@PathParam("stepId") String stepId, @PathParam("filterName") String filterName) throws WdkModelException, DataValidationException {
    Step step = getStepForCurrentUser(stepId);
    try {
    AnswerValue answerValue = step.getAnswerValue();
    JSONObject filterSummaryJson = answerValue.getFilterSummaryJson(filterName);
    return Response.ok(filterSummaryJson.toString()).build();
    } catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
  }

  private Step updateStep(Step step, StepRequest stepRequest) throws WdkModelException {

    StepBuilder newStep = Step.builder(step)
        .setCustomName(stepRequest.getCustomName());
    
    if (stepRequest.isAnswerSpecModified()) {
      // FIXME: this is no good- duplicate validation of the answer spec here
      newStep.setAnswerSpec(AnswerSpec.builder(stepRequest.getAnswerSpec()));
    }

    // check for metadata changes and assign new values
    if (nullSafeEquals(step.getCustomName(), stepRequest.getCustomName())) metadataChanged = true;
    step.setCustomName(stepRequest.getCustomName());
    if (nullSafeEquals(step.isCollapsible(), stepRequest.isCollapsible())) metadataChanged = true;
    step.setCollapsible(stepRequest.isCollapsible());
    if (nullSafeEquals(step.getCollapsedName(), stepRequest.getCollapsedName())) metadataChanged = true;
    step.setCollapsedName(stepRequest.getCollapsedName());

    return new StepChanges(paramFiltersChanged, metadataChanged);
  }

  private Step getStepForCurrentUser(String stepId) throws WdkModelException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      Step step = getWdkModel().getStepFactory().getStepById(Long.parseLong(stepId))
          .orElseThrow(() -> new NoSuchElementException("Cannot find step with ID " + stepId));
      if (step.getUser().getUserId() != user.getUserId()) {
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);
      }
      return step;
    }
    catch (NumberFormatException | NoSuchElementException e) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));
    }
  }

}
