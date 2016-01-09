package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.formatter.StepFormatter;

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
}
