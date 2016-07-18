package org.gusdb.wdk.service.service.user;

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

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.factory.WdkStepFactory;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.DataValidationException;
import org.gusdb.wdk.service.request.answer.AnswerRequest;
import org.gusdb.wdk.service.request.answer.AnswerRequestFactory;
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONException;
import org.json.JSONObject;

public class StepService extends UserService {

  public StepService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  public static final String STEP_RESOURCE = "Step ID ";

  @GET
  @Path("step/{stepId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStep(@PathParam("stepId") String stepId) throws WdkModelException {
    Step step;
    try {
      step = getWdkModel().getStepFactory().getStepById(Integer.parseInt(stepId));
    }
    catch (NumberFormatException | WdkModelException e) {
      throw new NotFoundException(WdkService.formatNotFound(STEP_RESOURCE + stepId));
    }
    if (step.getUser().getUserId() != getCurrentUserId()) {
      throw new ForbiddenException(WdkService.PERMISSION_DENIED);
    }
    return Response.ok(StepFormatter.getStepJson(step).toString()).build();
  }

  @POST
  @Path("step/{stepId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createStep(String body) throws WdkModelException, DataValidationException {
    try {
      // TODO: This should NOT be the final API of a POST to /step.  The answerSpec should
      //   be a property of the input JSON, with other (optional?) properties containing other
      //   step properties like custom name, etc.
      JSONObject json = new JSONObject(body);
      AnswerRequest request = AnswerRequestFactory.createFromJson(json, getWdkModelBean(), getCurrentUser());
      Step step = WdkStepFactory.createStep(request, getCurrentUser(), getWdkModel().getStepFactory());
      return Response.ok(StepFormatter.getStepJson(step).toString()).build();
    }
    catch (JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }
}
