package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.TestUtil.nullSafeEquals;
import static org.gusdb.fgputil.json.JsonUtil.serialize;
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
import org.gusdb.wdk.model.answer.spec.ParamValue;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.factory.AnswerValueFactory;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpecFactory;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StepRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.AnswerService;
import org.json.JSONException;
import org.json.JSONObject;

public class StepService extends UserService {

  private static class StepChanges extends TwoTuple<Boolean,Boolean> {
    public StepChanges(boolean paramFiltersChanged, boolean metadataChanged) {
      super(paramFiltersChanged, metadataChanged);
    }
    public boolean paramFiltersChanged() { return getFirst(); }
    public boolean metadataChanged() { return getSecond(); }
  }

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

      // validate the step and throw a DataValidation exception if not valid
      // new step are, by definition, not part of a strategy
//      validateStep(stepRequest.getAnswerSpec(), false);

      // create the step and insert into the database
      Step step = createStep(stepRequest, user, getWdkModel().getStepFactory());
      if(runStep != null && runStep) {
    	    if(step.isAnswerSpecComplete()) {
    	      AnswerSpec stepAnswerSpec = AnswerSpecFactory.createFromStep(step);
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
    return Response.ok(StepFormatter.getStepJsonWithRawEstimateValue(getStepForCurrentUser(stepId)).toString()).build();
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
      StepChanges changes = updateStep(step, stepRequest);

      // save parts of step that changed
      if (changes.paramFiltersChanged()) {
        step.saveParamFilters();
      }
      if (changes.metadataChanged()) {
        step.update(true);
      }

      // reset the estimated size in the database for this step and any downstream steps, if any
      getWdkModel().getStepFactory().resetEstimateSizeForThisAndDownstreamSteps(step);

      // reset the current step object estimate size
      step.setEstimateSize(-1);

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
  public Response deleteStep(@PathParam("stepId") String stepId) throws WdkModelException {
    Step step = getStepForCurrentUser(stepId);
    if (step.isDeleted()) throw new NotFoundException(AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));
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
      Step step = stepFactory.getStepById(Long.parseLong(stepId)).orElseThrow(() -> new NotFoundException("Step ID not found: " + stepId));
      if(!step.isAnswerSpecComplete()) {
        throw new DataValidationException("One or more parameters is missing");
      }

      AnswerSpec stepAnswerSpec = AnswerSpecFactory.createFromStep(step);
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

  private StepChanges updateStep(Step step, StepRequest stepRequest) throws WdkModelException {

    boolean paramFiltersChanged = false;
    boolean metadataChanged = false;

    // check for param or filter changes
    AnswerSpec answerSpec = stepRequest.getAnswerSpec();
    Map<String,ParamValue> newParamValues = answerSpec.getParamValues();
    Map<String,String> oldParamValues = step.getParamValues();
    for (String paramName : newParamValues.keySet()) {
      if (nullSafeEquals(oldParamValues.get(paramName), newParamValues.get(paramName).getObjectValue())) paramFiltersChanged = true;
      step.setParamValue(paramName, (String)newParamValues.get(paramName).getObjectValue());
    }
    if (nullSafeEquals(step.getFilter(), answerSpec.getLegacyFilter())) paramFiltersChanged = true;
    step.setFilterName(answerSpec.getLegacyFilter() == null ? null : answerSpec.getLegacyFilter().getName());
    if (nullSafeEquals(step.getFilterOptions(), answerSpec.getFilterValues())) paramFiltersChanged = true;
    step.setFilterOptions(answerSpec.getFilterValues());
    if (nullSafeEquals(step.getViewFilterOptions(), answerSpec.getViewFilterValues())) paramFiltersChanged = true;
    step.setViewFilterOptions(answerSpec.getViewFilterValues());

    // check for metadata changes and assign new values
    if (nullSafeEquals(step.getCustomName(), stepRequest.getCustomName())) metadataChanged = true;
    step.setCustomName(stepRequest.getCustomName());
    if (nullSafeEquals(step.isCollapsible(), stepRequest.isCollapsible())) metadataChanged = true;
    step.setCollapsible(stepRequest.isCollapsible());
    if (nullSafeEquals(step.getCollapsedName(), stepRequest.getCollapsedName())) metadataChanged = true;
    step.setCollapsedName(stepRequest.getCollapsedName());
    if (nullSafeEquals(
        step.getDisplayPrefs() == null ? null : serialize(step.getDisplayPrefs()),
        stepRequest.getDisplayPrefs() == null ? null : serialize(stepRequest.getDisplayPrefs())
    )) metadataChanged = true;
    step.setDisplayPrefs(stepRequest.getDisplayPrefs());

    return new StepChanges(paramFiltersChanged, metadataChanged);
  }

  private Step getStepForCurrentUser(String stepId) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      Step step = getWdkModel().getStepFactory().getStepById(Integer.parseInt(stepId)).orElseThrow(() -> new NotFoundException("Step ID not found: " + stepId));
      if (step.getUser().getUserId() != user.getUserId()) {
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);
      }
      return step;
    }
    catch (NumberFormatException | WdkModelException e) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));
    }
  }

  private static Step createStep(StepRequest stepRequest, User user, StepFactory stepFactory) throws WdkModelException {
    try {
      // new step must be created from raw spec
      AnswerSpec answerSpec = stepRequest.getAnswerSpec();
      Step step = stepFactory.createStep(user, answerSpec.getQuestion(),
          AnswerValueFactory.convertParams(answerSpec.getParamValues()),
          answerSpec.getLegacyFilter(), 1, -1, false, true, answerSpec.getWeight(),
          answerSpec.getFilterValues(), stepRequest.getCustomName(),
          stepRequest.isCollapsible(), stepRequest.getCollapsedName());
      step.setViewFilterOptions(answerSpec.getViewFilterValues());
      step.saveParamFilters();

      // once created, additional user-provided fields can be applied
      //step.setCustomName(stepRequest.getCustomName());
      //step.setCollapsible(stepRequest.isCollapsible());
      //step.setCollapsedName(stepRequest.getCollapsedName());
      //step.update(true);
      return step;
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to create step", e);
    }
  }

  /**
   * Step services do not necessarily run the steps that are created/patched but as long as the answerSpec is
   * complete, we want to insure that a step is valid prior to inserting or updating it in the database.
   * @param answerSpec - the answerSpec that will underlie the step to be checked for validity.
   * @throws WdkModelException
   * @throws DataValidationException
   */
//  protected void validateStep(AnswerSpec answerSpec, boolean inStrategy) throws WdkModelException, DataValidationException {
//	Question question = answerSpec.getQuestion();
//	if(question.hasAnswerParams() ? inStrategy : true) {
//	  Map<String, String> context = new LinkedHashMap<String, String>();
//      context.put(Utilities.QUERY_CTX_QUESTION, question.getFullName());
//	  try {
//	    User user = getUserBundle(Access.PRIVATE).getSessionUser();
//	    //Map<String, String> params = AnswerValueFactory.convertParams(answerSpec.getParamValues());
//	  }
//      catch(WdkUserException wue) {
//        throw new DataValidationException(wue);
//      }
//	}
//  }
}
