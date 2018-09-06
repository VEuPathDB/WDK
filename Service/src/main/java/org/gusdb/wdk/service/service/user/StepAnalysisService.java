package org.gusdb.wdk.service.service.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.QuestionFormatter;
import org.gusdb.wdk.service.formatter.StepAnalysisFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONException;
import org.json.JSONObject;

@Path(StepAnalysisService.STEP_ANALYSIS_PATH)
public class StepAnalysisService extends UserService {

  protected static final String STEP_ID_PATH_PARAM = "stepId";
  protected static final String STEP_ANALYSIS_PATH = USER_PATH + "/steps/{"+STEP_ID_PATH_PARAM+"}";

  private static final String ANALYSIS_PARAMS_KEY = StepAnalysisInstance.JsonKey.formParams.name();
  private static final String ANALYSIS_NAME_KEY = StepAnalysisInstance.JsonKey.analysisName.name();
  private static final String ANALYSIS_DISPLAY_NAME_KEY = StepAnalysisInstance.JsonKey.displayName.name();
  private static final String STATUS_KEY = "status";

  private final long stepId;


  protected StepAnalysisService(
      @PathParam(USER_ID_PATH_PARAM) String uid,
      @PathParam(STEP_ID_PATH_PARAM) long stepId) {
    super(uid);
    this.stepId = stepId;
  }

  @GET
  @Path("analysis-types")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisTypes() throws WdkModelException {

    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    Step step = getStepByIdAndCheckItsUser(user, stepId);
    Map<String, StepAnalysis> stepAnalyses = step.getQuestion().getStepAnalyses();

    return StepAnalysisFormatter.getStepAnalysisTypesJson(stepAnalyses).toString();
  }

  /**
   * Get Step Analysis form building data
   *
   * @param analysisName name of the step analysis for which the form data
   *                     should be retrieved
   * @return Ok response containing JSON provided by the Analyzer instance.
   */
  @GET
  @Path("analysis-types/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisTypeDataFromName(@PathParam("name") String analysisName)
      throws WdkModelException, DataValidationException, WdkUserException {

    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    Step step = getStepByIdAndCheckItsUser(user, stepId);

    return QuestionFormatter.getParamsJson(
        getStepAnalysisFromQuestion(step.getQuestion(), analysisName).getParamMap().values(),
        true,
        user,
        Collections.emptyMap()).toString();
  }

  /**
   * Create a new step analysis
   *
   * @param body input JSON string
   * @return Details of the newly created step analysis instance as JSON
   */
  @POST
  @Path("analyses")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String createStepAnalysis(String body) throws WdkModelException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      Step step = getStepByIdAndCheckItsUser(user, stepId);

      JSONObject json = new JSONObject(body);
      String analysisName = json.getString(ANALYSIS_NAME_KEY);
      String answerValueChecksum = getAnswerValueChecksum(step);
      StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.getQuestion(), analysisName);
      StepAnalysisInstance stepAnalysisInstance = getStepAnalysisInstance(step, stepAnalysis, answerValueChecksum);

      return StepAnalysisFormatter.getStepAnalysisJson(stepAnalysisInstance).toString();
    }
    catch (JSONException | DataValidationException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * List applied step analysis instances
   *
   * @return JSON response containing an array of basic analysis instance
   * details
   */
  // TODO: IS THIS NEEDED?  Maybe this should be in the step details...
  @GET
  @Path("analyses")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisInstanceList() throws WdkModelException {
    final User user = getUserBundle(Access.PRIVATE).getSessionUser();
    final Map<Long, StepAnalysisInstance> analyses = getWdkModel()
        .getStepAnalysisFactory()
        .getAppliedAnalyses(getStepByIdAndCheckItsUser(user, stepId));

    return StepAnalysisFormatter.getStepAnalysisInstancesJson(analyses).toString();
  }

  @GET
  @Path("analyses/{analysisId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisInstance(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken) throws WdkModelException {
    return StepAnalysisFormatter.getStepAnalysisJson(getAnalysis(analysisId, accessToken)).toString();
  }

  //  TODO: Why is this so slow?
  @DELETE
  @Path("analyses/{analysisId}")
  public Response deleteStepAnalysisInstance(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken) throws WdkModelException {
    getWdkModel().getStepAnalysisFactory().deleteAnalysis(getAnalysis(analysisId, accessToken));
    return Response.noContent().build();
  }

  @PATCH
  @Path("analyses/{analysisId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateStepAnalysisInstance(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken,
      String body) throws WdkModelException, WdkUserException {
    final ObjectMapper mapper = new ObjectMapper();
    final StepAnalysisFactory factory = getWdkModel().getStepAnalysisFactory();
    final StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);
    final JsonNode json;
    try {
      json = mapper.readTree(body);
      if (json.has(ANALYSIS_PARAMS_KEY)) {
        final Map<String, String[]> inputParams = mapper.readerFor(new TypeReference<Map<String, String[]>>() {}).readValue(json.get(ANALYSIS_PARAMS_KEY));

        instance.getFormParams().clear();
        instance.getFormParams().putAll(inputParams);

        final List<String> errors = factory.validateFormParams(instance);

        if(!errors.isEmpty()) {
          throw new WdkUserException(mapper.writeValueAsString(errors));
        }

        factory.setFormParams(instance);
      }
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }

    if (json.has(ANALYSIS_DISPLAY_NAME_KEY)) {
      instance.setDisplayName(json.get(ANALYSIS_DISPLAY_NAME_KEY).asText());
      factory.renameInstance(instance);
    }
  }

  @GET
  @Path("analyses/{analysisId}/result")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStepAnalysisResult(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken)
      throws WdkModelException, WdkUserException {

    final JSONObject value = getWdkModel().getStepAnalysisFactory()
        .getAnalysisResult(getAnalysis(analysisId, accessToken))
        .getResultViewModelJson();

    return value == null
        ? Response.noContent().build()
        : Response.ok(value.toString()).build();
  }

  @POST
  @Path("analyses/{analysisId}/result")
  @Produces(MediaType.APPLICATION_JSON)
  public Response runAnalysis(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken)
      throws WdkModelException {
    final StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);

    getWdkModel().getStepAnalysisFactory().runAnalysis(instance);

    return Response.accepted()
        .entity(new JSONObject().put(STATUS_KEY, instance.getStatus().name()).toString())
        .build();
  }

  @GET
  @Path("analyses/{analysisId}/result/status")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisResultStatus(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken)
      throws WdkModelException {
    final String status;

    try {
      status = getWdkModel().getStepAnalysisFactory()
          .getSavedAnalysisInstance(analysisId)
          .getStatus()
          .name();
    } catch (WdkUserException e) {
      throw new NotFoundException(e);
    }

    return new JSONObject().put(STATUS_KEY, status).toString();
  }

  @GET
  @Path("analyses/{analysisId}/properties")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getStepAnalysisProperties(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken) throws WdkModelException {
    StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);
    InputStream propertiesStream = getWdkModel().getStepAnalysisFactory().getProperties(instance);
    return Response.ok(getStreamingOutput(propertiesStream)).build();
  }

  // TODO: this should 404 if the analysis or step id are not found, presently it 500s
  @PUT
  @Path("analyses/{analysisId}/properties")
  @Consumes(MediaType.TEXT_PLAIN)
  public Response setStepAnalysisProperties(
      @PathParam("analysisId") long analysisId,
      @QueryParam("accessToken") String accessToken,
      InputStream body) throws WdkModelException {
    StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);
    getWdkModel().getStepAnalysisFactory().setProperties(instance, body);
    return Response.noContent().build();
  }

  /**
   * Creates StepAnalysisInstance from given step, analysis name, and answer
   * value checksum.
   *
   * @param step                The step for which a new analysis instance will be created
   * @param stepAnalysis        The analysis type for the new analysis instance
   * @param answerValueChecksum Hash of the relevant current state of the answer
   *                            that this analysis is based on.
   * @return A new StepAnalysisInstance
   */
  private StepAnalysisInstance getStepAnalysisInstance(
      Step step,
      StepAnalysis stepAnalysis,
      String answerValueChecksum) throws DataValidationException {
    try {
      return getWdkModel().getStepAnalysisFactory()
          .createAnalysisInstance(step, stepAnalysis, answerValueChecksum);
    }
    catch (WdkUserException | IllegalAnswerValueException | WdkModelException e) {
      throw new DataValidationException("Can't create valid step analysis", e);
    }
  }

  private StepAnalysis getStepAnalysisFromQuestion(
      Question question,
      String analysisName) throws DataValidationException {
    DataValidationException badStepAnalExcep = new DataValidationException("No step analysis with name " + analysisName + " exists for question " + question.getFullName());
    try {
      return Optional.ofNullable(question.getStepAnalysis(analysisName))
        .orElseThrow(() -> badStepAnalExcep);
    }
    catch (WdkUserException e) {
      throw badStepAnalExcep;
    }
  }

  private StepAnalysisInstance getAnalysis(long analysisId, String accessToken) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    StepAnalysisInstance instance = getAnalysis(analysisId, userBundle, accessToken);
    Step step = instance.getStep();
    if (userBundle.getTargetUser().getUserId() != step.getUser().getUserId()) {
      // owner of this step does not match user in URL
      throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step " + stepId);
    }
    if (stepId != step.getStepId()) {
      // step of this analysis does not match step in URL
      throw new NotFoundException("Step " + stepId + " does not contain analysis " + analysisId);
    }
    return instance;
  }

  /**
   * Retrieve and validate the step analysis instance identified by the given
   * analysis id.
   *
   * @param analysisId Analysis ID from user input
   * @param userBundle    User details
   * @param accessToken   ?
   * @return The step analysis instance that matches the input criteria
   * @throws WdkModelException if the step analysis instance could not be
   *                           loaded, the user could not be loaded, or the access token could not be
   *                           loaded.
   */
  private StepAnalysisInstance getAnalysis(
      long analysisId,
      UserBundle userBundle,
      String accessToken) throws WdkModelException {
    try {
      StepAnalysisInstance instance = getWdkModel().getStepAnalysisFactory()
          .getSavedAnalysisInstance(analysisId);
      if (userBundle.getTargetUser().getUserId() != instance.getStep().getUser().getUserId()) {
        // owner of this step does not match user in URL
        throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step analysis " + instance.getAnalysisId());
      }
      if (userBundle.isSessionUser() || instance.getAccessToken().equals(accessToken)) {
        return instance;
      }
      throw new ForbiddenException();
    } catch (WdkUserException e) {
      throw new NotFoundException(formatNotFound("step analysis: " + analysisId));
    }
  }
}
