package org.gusdb.wdk.service.service.user;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.service.UserBundle;

public class StepAnalysisService extends UserService {

  protected StepAnalysisService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("/steps/{stepId}/analyses/{analysisId}/properties")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getStepAnalysisProperties(
      @PathParam("stepId") String stepIdStr,
      @PathParam("analysisId") String analysisIdStr,
      @QueryParam("accessToken") String accessToken) throws WdkModelException {
    StepAnalysisContext context = getAnalysis(analysisIdStr, stepIdStr, accessToken);
    InputStream propertiesStream = getWdkModel().getStepAnalysisFactory().getProperties(context);
    return Response.ok(getStreamingOutput(propertiesStream)).build();
  }

  @PUT
  @Path("/steps/{stepId}/analyses/{analysisId}/properties")
  @Consumes(MediaType.TEXT_PLAIN)
  public Response setStepAnalysisProperties(
      @PathParam("stepId") String stepIdStr,
      @PathParam("analysisId") String analysisIdStr,
      @QueryParam("accessToken") String accessToken,
      InputStream body) throws WdkModelException {
    StepAnalysisContext context = getAnalysis(analysisIdStr, stepIdStr, accessToken);
    getWdkModel().getStepAnalysisFactory().setProperties(context, body);
    return Response.noContent().build();
  }

  /**
   * Function to use to read an analysis context into memory if the request is an instance-private
   * operation and no access token is provided.  Assumes step ID is also not provided, but TODO: figure
   * out if this will ever be the case- maybe not if URLs for future step analysis services always
   * include the step ID as well.
   * 
   * @param analysisIdStr analysis ID as a string (value passed as part of URL)
   * @return step analysis context if the ID corresponds to one
   * @throws WdkModelException if error occurs
   * @throws NotFoundException if passed string is not an existing step analysis ID
   * @throws ForbiddenException if current user does not have access to this analysis
   */
  private StepAnalysisContext getAnalysis(String analysisIdStr) throws WdkModelException {
    return getAnalysis(analysisIdStr, getUserBundle(Access.PRIVATE), null);
  }

  private StepAnalysisContext getAnalysis(String analysisIdStr, String stepIdStr, String accessToken) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    StepAnalysisContext context = getAnalysis(analysisIdStr, userBundle, accessToken);
    long stepId = parseIdOrNotFound("step", stepIdStr);
    Step step = context.getStep();
    if (userBundle.getTargetUser().getUserId() != step.getUser().getUserId()) {
      // owner of this step does not match user in URL
      throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step " + stepId);
    }
    if (stepId != step.getStepId()) {
      // step of this analysis does not match step in URL
      throw new NotFoundException("Step " + stepId + " does not contain analysis " + analysisIdStr);
    }
    return context;
  }

  private StepAnalysisContext getAnalysis(String analysisIdStr, UserBundle userBundle, String accessToken) throws WdkModelException {
    try {
      long analysisId = parseIdOrNotFound("step analysis", analysisIdStr);
      StepAnalysisContext context = getWdkModel().getStepAnalysisFactory().getSavedContext(analysisId);
      if (userBundle.getTargetUser().getUserId() != context.getStep().getUser().getUserId()) {
        // owner of this step does not match user in URL
        throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step analysis " + context.getAnalysisId());
      }
      if (userBundle.isSessionUser() || context.getAccessToken().equals(accessToken)) {
        return context;
      }
      throw new ForbiddenException();
    }
    catch (WdkUserException e) {
      throw new NotFoundException(formatNotFound("step analysis: " + analysisIdStr));
    }
  }
}
