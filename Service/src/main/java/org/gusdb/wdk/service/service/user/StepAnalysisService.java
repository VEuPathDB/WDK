package org.gusdb.wdk.service.service.user;

import static org.gusdb.wdk.service.service.user.StepService.STEP_ID_PATH_PARAM;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.StepAnalysisFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StepAnalysisService extends UserService {

  // endpoints to handle analysis types for a given step
  private static final String ANALYSIS_TYPES_PATH = StepService.NAMED_STEP_PATH + "/analysis-types";
  private static final String ANALYSIS_TYPE_PATH_PARAM = "analysisTypeName";
  private static final String NAMED_ANALYSIS_TYPE_PATH = ANALYSIS_TYPES_PATH + "/{" + ANALYSIS_TYPE_PATH_PARAM + "}";

  // endpoints to handle analysis instances for a given step
  private static final String ANALYSES_PATH = StepService.NAMED_STEP_PATH + "/analyses";
  private static final String ANALYSIS_ID_PATH_PARAM = "analysisId";
  private static final String NAMED_ANALYSIS_PATH = ANALYSES_PATH + "/{" + ANALYSIS_ID_PATH_PARAM + "}";

  // sub-endpoints for an analysis instance
  private static final String NAMED_ANALYSIS_RESULT_PATH = NAMED_ANALYSIS_PATH + "/result";
  private static final String NAMED_ANALYSIS_RESULT_STATUS_PATH = NAMED_ANALYSIS_RESULT_PATH + "/status";
  private static final String NAMED_ANALYSIS_RESOURCES_PATH = NAMED_ANALYSIS_PATH + "/resources";
  private static final String NAMED_ANALYSIS_PROPERTIES_PATH = NAMED_ANALYSIS_PATH + "/properties";

  private static final String ACCESS_TOKEN_QUERY_PARAM = "accessToken";

  private static final String ANALYSIS_PARAMS_KEY = StepAnalysisInstance.JsonKey.formParams.name();
  private static final String ANALYSIS_NAME_KEY = StepAnalysisInstance.JsonKey.analysisName.name();
  private static final String ANALYSIS_DISPLAY_NAME_KEY = StepAnalysisInstance.JsonKey.displayName.name();
  private static final String STATUS_KEY = "status";
  private static final String CONTEXT_HASH_KEY = "contextHash";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String DOWNLOAD_URL_KEY = "downloadUrl";
  private static final String PROPERTIES_URL_KEY = "propertiesUrl";

  private final long _stepId;

  protected StepAnalysisService(
      @PathParam(USER_ID_PATH_PARAM) String uid,
      @PathParam(STEP_ID_PATH_PARAM) long stepId) {
    super(uid);
    _stepId = stepId;
  }

  @GET
  @Path(ANALYSIS_TYPES_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisTypes() throws WdkModelException, DataValidationException {
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    Map<String, StepAnalysis> stepAnalyses = step.get().getAnswerSpec().getQuestion().getStepAnalyses();
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
  @Path(NAMED_ANALYSIS_TYPE_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public String getStepAnalysisTypeDataFromName(@PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName)
      throws WdkModelException, DataValidationException {

    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis analysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
    Map<String, Param> paramMap = analysis.getParamMap();
    Map<String,String> context = new HashMap<>();

    // TODO: this is a hack.  We could fix it by introducing a dedicated
    // param type called something like <stepAnalysisIdSqlParam> that would
    // have no attributes, and be dedicated to this need.
    if (paramMap.containsKey("answerIdSql")) {
      if (context.isEmpty()) {
        context = new HashMap<>();
      }
      context.put("answerIdSql", AnswerValueFactory.makeAnswer(step).getIdSql());
    }

    // TODO: also a hack; PostgreSQL only
    // VALUES list is a SQL construct that creates a temporary table
    // this case, with two fields, one for the param name, one for the param value
    // allowing stepAnalysis parameters to be depended on step parameter values
    if (paramMap.containsKey("stepParamValuesSql")) {
      if (getWdkModel().getAppDb().getPlatform() instanceof PostgreSQL) {
        if (context.isEmpty()) {
          context = new HashMap<>();
        }
        ArrayList<String> values = new ArrayList<String>();
        for (Entry<String, String> param : step.get().getAnswerSpec().getQueryInstanceSpec().entrySet()) {
          String row = "('" + param.getKey() + "', '" + param.getValue() + "')";
          values.add(row);
        }
        context.put("stepParamValuesSql", "SELECT * FROM ( VALUES " + String.join(",", values) + " ) AS p (name, value)");
      } else {
        throw new WdkModelException("Invalid step analysis parameter: stepParamValuesSql only valid for PostgreSQL.");
      }
    }

    /* FIXME: currently broken; need to do some work still to integrate step analysis param containers
    return QuestionFormatter.getParamsJson(
        paramMap.values(),
        true,
        user,
        context).toString();*/

    return "";
  }

  /**
   * Create a new step analysis
   *
   * @param body input JSON string
   * @return Details of the newly created step analysis instance as JSON
   */
  @POST
  @Path(ANALYSES_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String createStepAnalysis(JSONObject json) throws WdkModelException {
    try {
      RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
      String analysisName = json.getString(ANALYSIS_NAME_KEY);
      String answerValueChecksum = AnswerValueFactory.makeAnswer(step).getChecksum();
      StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
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
   * @throws DataValidationException
   */
  // TODO: IS THIS NEEDED?  Maybe this should be in the step details...
  @GET
  @Path(ANALYSES_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getStepAnalysisInstanceList() throws WdkModelException, DataValidationException {
    getUserBundle(Access.PRIVATE); // make sure session user matches target user
    final Map<Long, StepAnalysisInstance> analyses = getWdkModel()
        .getStepAnalysisFactory()
        .getAppliedAnalyses(getRunnableStepForCurrentUser(_stepId).get());
    return StepAnalysisFormatter.getStepAnalysisInstancesJson(analyses);
  }

  @GET
  @Path(NAMED_ANALYSIS_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getStepAnalysisInstance(
      @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
      @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken) throws WdkModelException {
    return StepAnalysisFormatter.getStepAnalysisJson(getAnalysis(analysisId, accessToken));
  }

  //  TODO: Why is this so slow?
  @DELETE
  @Path(NAMED_ANALYSIS_PATH)
  public void deleteStepAnalysisInstance(
      @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
      @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken) throws WdkModelException {
    getWdkModel().getStepAnalysisFactory().deleteAnalysis(getAnalysis(analysisId, accessToken));
  }

  @PATCH
  @Path(NAMED_ANALYSIS_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateStepAnalysisInstance(
      @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
      @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken,
      String body) throws WdkModelException, WdkUserException {
    final ObjectMapper mapper = new ObjectMapper();
    final StepAnalysisFactory factory = getWdkModel().getStepAnalysisFactory();
    final StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);
    final JsonNode json;
    try {
      json = mapper.readTree(body);
      if (json.has(ANALYSIS_PARAMS_KEY)) {
        final Map<String, String[]> inputParams = mapper.readerFor(
            new TypeReference<Map<String, String[]>>() {}).readValue(json.get(ANALYSIS_PARAMS_KEY));

        instance.getFormParams().clear();
        instance.getFormParams().putAll(inputParams);

        ValidationBundle validation = factory.validateFormParams(instance);

        if(!validation.getStatus().isValid()) {
          throw new WdkUserException(validation.toString(2));
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
  @Path(NAMED_ANALYSIS_RESULT_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStepAnalysisResult(
    @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
    @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken
  ) throws WdkModelException, WdkUserException {

    final StepAnalysisFactory fac = getWdkModel().getStepAnalysisFactory();
    final JSONObject value = fac.getAnalysisResult(getAnalysis(analysisId, accessToken))
      .getResultViewModelJson();

    if(value == null)
      return Response.noContent().build();

    // This should be moved upstream.
    StepAnalysisInstance inst = fac.getSavedAnalysisInstance(analysisId);
    String analysisUrl = getAnalysisUrl(inst);
    value.put(CONTEXT_HASH_KEY, inst.createHash())
        .put(ACCESS_TOKEN_KEY, inst.getAccessToken())
        .put(DOWNLOAD_URL_KEY, analysisUrl + "/resources")
        .put(PROPERTIES_URL_KEY, analysisUrl + "/properties");

    return Response.ok(value).build();
  }

  private String getAnalysisUrl(StepAnalysisInstance inst) {
    return String.format("%s/users/%d/steps/%d/analyses/%d",
        getServiceUri(), inst.getStep().getUser().getUserId(),
        inst.getStep().getStepId(), inst.getAnalysisId());
  }

  @POST
  @Path(NAMED_ANALYSIS_RESULT_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response runAnalysis(
    @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
    @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken
  ) throws WdkModelException {
    final StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);

    getWdkModel().getStepAnalysisFactory().runAnalysis(instance);

    return Response.accepted()
        .entity(new JSONObject().put(STATUS_KEY, instance.getStatus().name()))
        .build();
  }

  @GET
  @Path(NAMED_ANALYSIS_RESULT_STATUS_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getStepAnalysisResultStatus(
    @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId
  ) throws WdkModelException {
    try {
      return new JSONObject().put(
        STATUS_KEY,
        getWdkModel().getStepAnalysisFactory()
          .getSavedAnalysisInstance(analysisId)
          .getStatus()
          .name()
      );
    } catch (WdkUserException e) {
      throw new NotFoundException(e);
    }
  }

  @GET
  @Path(NAMED_ANALYSIS_RESOURCES_PATH)
  public Response getStepAnalysisResource(
    @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
    @QueryParam("path") String path
  ) throws Exception {
    StepAnalysisFactory stepAnalysisFactory = getWdkModel().getStepAnalysisFactory();
    StepAnalysisInstance instance = StepAnalysisInstance.createFromId(
      analysisId,
      stepAnalysisFactory
    );
    java.nio.file.Path resourcePath = stepAnalysisFactory.getResourcePath(
      instance,
      path
    );

    File resourceFile = resourcePath.toFile();
    if (resourceFile.exists() && resourceFile.isFile() && resourceFile.canRead()) {
      InputStream resourceStream = new BufferedInputStream(new FileInputStream(resourceFile));
      return Response.ok(getStreamingOutput(resourceStream))
        .type(Files.probeContentType(resourcePath))
        .header(
          "Content-Disposition",
          ContentDisposition.type("attachment").fileName(
            resourceFile.getName()
          ).build()
        )
        .build();
    }

    throw new NotFoundException("Could not find resource " + path + " for step analysis " + analysisId);
  }

  @GET
  @Path(NAMED_ANALYSIS_PROPERTIES_PATH)
  @Produces(MediaType.TEXT_PLAIN)
  public Response getStepAnalysisProperties(
    @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
    @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken
  ) throws WdkModelException {
    StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);
    InputStream propertiesStream = getWdkModel().getStepAnalysisFactory()
        .getProperties(instance);
    return Response.ok(getStreamingOutput(propertiesStream)).build();
  }

  // TODO: this should 404 if the analysis or step id are not found, presently it 500s
  @PUT
  @Path(NAMED_ANALYSIS_PROPERTIES_PATH)
  @Consumes(MediaType.TEXT_PLAIN)
  public void setStepAnalysisProperties(
      @PathParam(ANALYSIS_ID_PATH_PARAM) long analysisId,
      @QueryParam(ACCESS_TOKEN_QUERY_PARAM) String accessToken,
      InputStream body) throws WdkModelException {
    StepAnalysisInstance instance = getAnalysis(analysisId, accessToken);
    getWdkModel().getStepAnalysisFactory().setProperties(instance, body);
  }

  private RunnableObj<Step> getRunnableStepForCurrentUser(long stepId) throws WdkModelException, DataValidationException {
    return getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE)
        .getRunnable()
        .getOrThrow(step -> new DataValidationException(
            "Step analysis operations can only be performed " +
            "on valid steps.  Revise your step and try again."));
  }

  /**
   * Creates StepAnalysisInstance from given step, analysis name, and answer
   * value checksum.
   *
   * @param step          The step for which a new analysis instance will be
   *                      created
   * @param analysis      The analysis type for the new analysis instance
   * @param answerValHash Hash of the relevant current state of the answer that
   *                      this analysis is based on.
   *
   * @return A new StepAnalysisInstance
   */
  private StepAnalysisInstance getStepAnalysisInstance(
      RunnableObj<Step> step,
      StepAnalysis analysis,
      String answerValHash) throws DataValidationException {
    try {
      return getWdkModel().getStepAnalysisFactory()
          .createAnalysisInstance(step.get(), analysis, answerValHash);
    }
    catch (WdkUserException | IllegalAnswerValueException | WdkModelException e) {
      throw new DataValidationException("Can't create valid step analysis", e);
    }
  }

  private StepAnalysis getStepAnalysisFromQuestion(Question question,
      String analysisName) throws DataValidationException {

    DataValidationException badStepAnalExcep = new DataValidationException(
        String.format("No step analysis with name %s exists for question %s",
        analysisName, question.getFullName()));

    try {
      return Optional.ofNullable(question.getStepAnalysis(analysisName))
        .orElseThrow(() -> badStepAnalExcep);
    } catch (WdkUserException e) {
      throw badStepAnalExcep;
    }
  }

  private StepAnalysisInstance getAnalysis(long analysisId, String accessToken)
      throws WdkModelException {

    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    StepAnalysisInstance instance = getAnalysis(analysisId, userBundle, accessToken);
    Step step = instance.getStep();
    long targetUser = userBundle.getTargetUser().getUserId();

    // Step cannot be found under the current user id path.
    if (targetUser != step.getUser().getUserId())
      throw new NotFoundException(String.format("User %d does not own step %d",
          targetUser, _stepId));

    // Analysis cannot be found under the current step id path.
    if (_stepId != step.getStepId())
      throw new NotFoundException(String.format(
        "Step %d does not contain analysis %d", _stepId, analysisId));

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
    String accessToken
  ) throws WdkModelException {
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
