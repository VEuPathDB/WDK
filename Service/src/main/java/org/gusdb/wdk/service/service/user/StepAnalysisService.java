package org.gusdb.wdk.service.service.user;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
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

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.formatter.StepAnalysisFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONException;
import org.json.JSONObject;

public class StepAnalysisService extends UserService {
  
  private static final String ANALYSIS_NAME_KEY = "analysisName";

  protected StepAnalysisService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }
  
  @GET
  @Path("/steps/{stepId}/analysis-types")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getStepAnalysisTypes(
      @PathParam("stepId") String stepIdStr,
      @QueryParam("accessToken") String accessToken) throws WdkModelException, DataValidationException {

    long stepId = parseIdOrNotFound("step", stepIdStr);
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    Step step = getStepByIdAndCheckItsUser(user, stepId);     

    Map<String, StepAnalysis> stepAnalyses = step.getQuestion().getStepAnalyses();
    return Response.ok(StepAnalysisFormatter.getStepAnalysisTypesJson(stepAnalyses).toString()).build();
  }


  
  /**
   * Create a new step analysis
   * @param stepIdStr
   * @param body
   * @return
   * @throws WdkModelException
   * @throws DataValidationException
   */
  @POST
  @Path("/steps/{stepId}/analyses")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createStepAnalysis(@PathParam("stepId") String stepIdStr,
      String body) throws WdkModelException, DataValidationException {
    try {
      long stepId = parseIdOrNotFound("step", stepIdStr);
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      Step step = getStepByIdAndCheckItsUser(user, stepId);     

      JSONObject json = new JSONObject(body);      
      String analysisName = json.getString(ANALYSIS_NAME_KEY);
      String answerValueChecksum = getAnswerValueChecksum(step);
      StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.getQuestion(), analysisName);
      StepAnalysisInstance stepAnalysisInstance = getStepAnalysisInstance(step, stepAnalysis, answerValueChecksum);
      return Response.ok(StepAnalysisFormatter.getStepAnalysisJson(stepAnalysisInstance).toString()).build();
    }
    catch (JSONException | DataValidationException e) {
      throw new BadRequestException(e);
    }
  }
  
  private StepAnalysisInstance getStepAnalysisInstance(Step step, StepAnalysis stepAnalysis, String answerValueChecksum) throws DataValidationException {
    StepAnalysisInstance stepAnalysisInstance; 

    try {
      stepAnalysisInstance = getWdkModel().getStepAnalysisFactory().createAnalysisInstance(step, stepAnalysis, answerValueChecksum);
    } catch ( WdkUserException | IllegalAnswerValueException | WdkModelException e) {
      throw new DataValidationException("Can't create valid step analysis", e);
    }
    return stepAnalysisInstance;
  }
  
  private StepAnalysis getStepAnalysisFromQuestion(Question question, String analysisName) throws DataValidationException {
    StepAnalysis stepAnalysis;
    DataValidationException badStepAnalExcep = new DataValidationException("No step analysis with name " + analysisName +
        " exists for question " + question.getFullName());
    try {
      stepAnalysis = question.getStepAnalysis(analysisName);
    } catch (WdkUserException e) {
      throw badStepAnalExcep;
    }

    if (stepAnalysis == null) throw badStepAnalExcep;
    return stepAnalysis;
  }

  @GET
  @Path("/steps/{stepId}/analyses/{analysisId}/properties")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getStepAnalysisProperties(
      @PathParam("stepId") String stepIdStr,
      @PathParam("analysisId") String analysisIdStr,
      @QueryParam("accessToken") String accessToken) throws WdkModelException {
    StepAnalysisInstance instance = getAnalysis(analysisIdStr, stepIdStr, accessToken);
    InputStream propertiesStream = getWdkModel().getStepAnalysisFactory().getProperties(instance);
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
    StepAnalysisInstance instance = getAnalysis(analysisIdStr, stepIdStr, accessToken);
    getWdkModel().getStepAnalysisFactory().setProperties(instance, body);
    return Response.noContent().build();
  }

  /**
   * Function to use to read an analysis instance into memory if the request is an instance-private
   * operation and no access token is provided.  Assumes step ID is also not provided, but TODO: figure
   * out if this will ever be the case- maybe not if URLs for future step analysis services always
   * include the step ID as well.
   * 
   * @param analysisIdStr analysis ID as a string (value passed as part of URL)
   * @return step analysis instance if the ID corresponds to one
   * @throws WdkModelException if error occurs
   * @throws NotFoundException if passed string is not an existing step analysis ID
   * @throws ForbiddenException if current user does not have access to this analysis
   */
  private StepAnalysisInstance getAnalysis(String analysisIdStr) throws WdkModelException {
    return getAnalysis(analysisIdStr, getUserBundle(Access.PRIVATE), null);
  }

  private StepAnalysisInstance getAnalysis(String analysisIdStr, String stepIdStr, String accessToken) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    StepAnalysisInstance instance = getAnalysis(analysisIdStr, userBundle, accessToken);
    long stepId = parseIdOrNotFound("step", stepIdStr);
    Step step = instance.getStep();
    if (userBundle.getTargetUser().getUserId() != step.getUser().getUserId()) {
      // owner of this step does not match user in URL
      throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step " + stepId);
    }
    if (stepId != step.getStepId()) {
      // step of this analysis does not match step in URL
      throw new NotFoundException("Step " + stepId + " does not contain analysis " + analysisIdStr);
    }
    return instance;
  }

  private StepAnalysisInstance getAnalysis(String analysisIdStr, UserBundle userBundle, String accessToken) throws WdkModelException {
    try {
      long analysisId = parseIdOrNotFound("step analysis", analysisIdStr);
      StepAnalysisInstance instance = getWdkModel().getStepAnalysisFactory().getSavedAnalysisInstance(analysisId);
      if (userBundle.getTargetUser().getUserId() != instance.getStep().getUser().getUserId()) {
        // owner of this step does not match user in URL
        throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step analysis " + instance.getAnalysisId());
      }
      if (userBundle.isSessionUser() || instance.getAccessToken().equals(accessToken)) {
        return instance;
      }
      throw new ForbiddenException();
    }
    catch (WdkUserException e) {
      throw new NotFoundException(formatNotFound("step analysis: " + analysisIdStr));
    }
  }
}
