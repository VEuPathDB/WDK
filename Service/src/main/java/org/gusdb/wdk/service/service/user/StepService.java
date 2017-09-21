package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.TestUtil.nullSafeEquals;

import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.factory.WdkStepFactory;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpec;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StepRequest;
import org.gusdb.wdk.service.service.WdkService;
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
  @Path("step")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createStep(String body) throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      JSONObject json = new JSONObject(body);
      StepRequest stepRequest = StepRequest.newStepFromJson(json, getWdkModelBean(), user);
      Step step = WdkStepFactory.createStep(stepRequest, user, getWdkModel().getStepFactory());
      return Response.ok(StepFormatter.getStepJson(step).toString()).build();
    }
    catch (JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  @GET
  @Path("step/{stepId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStep(@PathParam("stepId") String stepId) throws WdkModelException {
    return Response.ok(StepFormatter.getStepJson(getStepForCurrentUser(stepId)).toString()).build();
  }

  @PATCH
  @Path("step/{stepId}")
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

      // return updated step
      return Response.ok(StepFormatter.getStepJson(step).toString()).build();
    }
    catch (JSONException | RequestMisformatException e) {
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
        throw new ForbiddenException(WdkService.PERMISSION_DENIED);
      }
      return step;
    }
    catch (NumberFormatException | WdkModelException e) {
      throw new NotFoundException(WdkService.formatNotFound(STEP_RESOURCE + stepId));
    }
  }
}
