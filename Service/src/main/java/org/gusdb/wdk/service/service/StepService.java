package org.gusdb.wdk.service.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import org.gusdb.wdk.service.request.answer.AnswerRequest;
import org.gusdb.wdk.service.request.answer.AnswerRequestFactory;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/step")
public class StepService extends WdkService {

  @GET
  @Path("{stepId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStep(@PathParam("stepId") String stepId) throws WdkModelException {
    Step step;
    try {
      step = getWdkModel().getStepFactory().getStepById(Integer.parseInt(stepId));
    }
    catch (NumberFormatException | WdkModelException e) {
      return getNotFoundResponse(stepId);
    }
    if (step.getUser().getUserId() != getCurrentUserId()) {
      return getPermissionDeniedResponse();
    }
    return Response.ok(StepFormatter.getStepJson(step).toString()).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createStep(String body) throws WdkModelException {
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
      return getBadRequestBodyResponse(e.toString());
    }
  }
}
