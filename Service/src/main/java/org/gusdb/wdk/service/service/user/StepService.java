package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.TestUtil.nullSafeEquals;

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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.ParamValue;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.factory.AnswerValueFactory;
import org.gusdb.wdk.service.factory.WdkStepFactory;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpecFactory;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StepRequest;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.AbstractWdkService;
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
  public Response createStep(@QueryParam("runStep") Boolean runStep, String body) throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      JSONObject json = new JSONObject(body);
      StepRequest stepRequest = StepRequest.newStepFromJson(json, getWdkModelBean(), user);
      
      // validate the step and throw a DataValidation exception if not valid
      // new step are, by definition, not part of a strategy
//      validateStep(stepRequest.getAnswerSpec(), false);
      
      // create the step and insert into the database
      Step step = WdkStepFactory.createStep(stepRequest, user, getWdkModel().getStepFactory());
      if(runStep != null && runStep) {
    	    if(step.isAnswerSpecComplete()) {
    	      AnswerSpec stepAnswerSpec = AnswerSpecFactory.createFromStep(step);
    	    	  new AnswerValueFactory(user).createFromAnswerSpec(stepAnswerSpec);
    	    }
    	    else {
    	    	  throw new DataValidationException("Cannot run a step with an incomplete answer spec.");
    	    }
      }
      return Response.ok(StepFormatter.getStepJsonWithCalculatedEstimateValue(step).toString()).build();
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
      StepRequest stepRequest = StepRequest.patchStepFromJson(step, patchJson, getWdkModelBean(), getSessionUser());
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
  public Response createAnswer(@PathParam("stepId") String stepId, String body) throws WdkModelException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      StepFactory stepFactory = new StepFactory(getWdkModel());
      Step step = stepFactory.getStepById(Long.parseLong(stepId));
      if(!step.isAnswerSpecComplete()) throw new DataValidationException("One or more parameters is missing");
      AnswerSpec stepAnswerSpec = AnswerSpecFactory.createFromStep(step);
      JSONObject formatting = new JSONObject(body).getJSONObject(JsonKeys.FORMATTING);
      return AnswerService.getAnswerResponse(user, stepAnswerSpec, formatting);
    }
    catch(NumberFormatException nfe) {
    	  throw new BadRequestException("The step id " + stepId + " is not a valid id ", nfe);
    }
    catch (JSONException | RequestMisformatException | DataValidationException e) {
      throw new BadRequestException(e);
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

    return new StepChanges(paramFiltersChanged, metadataChanged);
  }

  private Step getStepForCurrentUser(String stepId) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      Step step = getWdkModel().getStepFactory().getStepById(Integer.parseInt(stepId));
      if (step.getUser().getUserId() != user.getUserId()) {
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);
      }
      return step;
    }
    catch (NumberFormatException | WdkModelException e) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(STEP_RESOURCE + stepId));
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
