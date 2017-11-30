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
      @PathParam("analysisId") String analysisIdStr,
      @QueryParam("accessToken") String accessToken) throws WdkModelException {
    StepAnalysisContext context = getAnalysis(analysisIdStr, accessToken);
    InputStream propertiesStream = getWdkModel().getStepAnalysisFactory().getProperties(context);
    return Response.ok(getStreamingOutput(propertiesStream)).build();
  }

  @PUT
  @Path("/step-analyses/{analysisId}/properties")
  @Consumes(MediaType.TEXT_PLAIN)
  public Response setStepAnalysisProperties(
      @PathParam("analysisId") String analysisIdStr,
      @QueryParam("accessToken") String accessToken,
      InputStream body) throws WdkModelException {
    StepAnalysisContext context = getAnalysis(analysisIdStr, accessToken);
    getWdkModel().getStepAnalysisFactory().setProperties(context, body);
    return Response.noContent().build();
  }

  private StepAnalysisContext getAnalysis(String analysisIdStr, String accessToken) throws WdkModelException {
    try {
      long analysisId = parseIdOrNotFound(analysisIdStr);
      UserBundle userBundle = getUserBundle(Access.PUBLIC);
      StepAnalysisContext context = getWdkModel().getStepAnalysisFactory().getSavedContext(analysisId);
      if (userBundle.isSessionUser() || context.getAccessToken().equals(accessToken)) {
        return context;
      }
      throw new ForbiddenException();
    }
    catch (WdkUserException e) {
      throw new NotFoundException(formatNotFound(analysisIdStr));
    }
  }
}
