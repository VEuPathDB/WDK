package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.functional.Functions.not;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.StepFactoryHelpers.UserCache;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.strategy.StrategyRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class StrategyService extends UserService {

  public static final String BASE_PATH = "strategies";
  public static final String ID_PARAM  = "strategyId";
  public static final String ID_PATH   = BASE_PATH + "/{" + ID_PARAM + "}";

  public static final String STRATEGY_RESOURCE = "Strategy ID ";

  private static final ObjectMapper JSON = new ObjectMapper();

  public StrategyService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("strategies")
  @Produces(MediaType.APPLICATION_JSON)
  // TODO: @OutSchema(...)
  public JSONArray getStrategies() throws WdkModelException {
    return StrategyFormatter.getStrategiesJson(getWdkModel().getStepFactory()
      .getStrategies(getUserBundle(Access.PRIVATE).getSessionUser().getUserId(), false, false));
  }

  @POST
  @Path(BASE_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategies.post-request")
  @OutSchema("wdk.creation-post-response")
  public Response createStrategy(JSONObject body)
      throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      StepFactory stepFactory = getWdkModel().getStepFactory();
      Strategy strategy = body.has(JsonKeys.SOURCE_SIGNATURE)
        ? copyStrategy(user, stepFactory, body)
        : createNewStrategy(user, stepFactory, body);

      return Response.ok(new JSONObject().put(JsonKeys.ID, strategy.getStrategyId()))
        .location(getUriInfo().getAbsolutePathBuilder().build(strategy.getStrategyId()))
        .build();
    }
    catch (WdkModelException wme) {
      throw new WdkModelException("Unable to create the strategy.", wme);
    }
    catch (WdkUserException wue) {
      throw new DataValidationException(wue);
    }
    catch (InvalidStrategyStructureException e) {
      throw new DataValidationException(e);
    }
  }

  @PATCH
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategy.patch-request")
  public void updateStrategy(@PathParam(ID_PARAM) long strategyId,
      JSONObject body) throws WdkModelException, DataValidationException {
    final StepFactory fac = getWdkModel().getStepFactory();
    final Strategy strat = fac.getStrategyById(strategyId, ValidationLevel.SYNTACTIC)
      .orElseThrow(() -> new NotFoundException(formatNotFound(STRATEGY_RESOURCE + strategyId)));

    if (strat.isSaved()) {
      validateSavedStratChange(body).ifPresent(err -> {
        throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(err).build());
      });
    }

    try {
      fac.updateStrategy(applyStrategyChange(strat, body));
    }
    catch (InvalidStrategyStructureException e) {
      throw new DataValidationException("Invalid strategy structure; " + e.getMessage(), e);
    }
  }

  @GET
  @Path(ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO: @OutSchema(...)
  public JSONObject getStrategy(@PathParam(ID_PARAM) long strategyId)
      throws WdkModelException {
    return StrategyFormatter.getDetailedStrategyJson(
      getStrategyForCurrentUser(strategyId, ValidationLevel.RUNNABLE));
  }

  @PUT
  @Path(ID_PATH + "/stepTree")
  @Consumes(MediaType.APPLICATION_JSON)
  public void replaceStepTree(@PathParam(ID_PARAM) long stratId, JSONObject body) {
    /*
     * spec:
     * - look up strategy
     * - save off list of steps in strat
     * - create a new strategy builder from the orig
     * - clear existing steps from the builder
     * - look up and add steps specified in tree to the builder
     *     - use the strategy itself to look for steps; if not there, then look up in DB (must be unattached!)
     *     - need to set answer params to new values first
     * - update strategy in DB
     * - clear strategy from no-longer-used steps
     * - update no-longer-used steps in DB
     */
    throw new InternalServerErrorException("Method not implemented");
  }

  private Strategy getStrategyForCurrentUser(long strategyId, ValidationLevel level) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      // Whether the user owns this strategy or not is resolved in the getStepFactory method
      Strategy strategy = getWdkModel().getStepFactory()
        .getStrategyById(strategyId, level)
        .orElseThrow(() -> new NotFoundException(
          AbstractWdkService.formatNotFound(STRATEGY_RESOURCE + strategyId)));

      if (strategy.getUser().getUserId() != user.getUserId())
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);

      return strategy;
    }
    catch (WdkModelException e) {
      throw new NotFoundException(
        AbstractWdkService.formatNotFound(STRATEGY_RESOURCE + strategyId));
    }
  }

  private Strategy copyStrategy(User user, StepFactory stepFactory, JSONObject json)
      throws WdkModelException, WdkUserException, JSONException {
    String signature = json.getString(JsonKeys.SOURCE_SIGNATURE);
    Strategy sourceStrategy = stepFactory.getStrategyBySignature(signature)
      .orElseThrow(() -> new WdkUserException(
        "No strategy exists with signature " + signature));
    return stepFactory.copyStrategy(user, sourceStrategy, new LinkedHashMap<>());
  }

  private Strategy createNewStrategy(User user, StepFactory stepFactory,
      JSONObject json)
      throws WdkModelException, DataValidationException, InvalidStrategyStructureException {
    StrategyRequest strategyRequest = StrategyRequest.createFromJson(json,
      stepFactory, user, getWdkModel().getProjectId());
    TreeNode<Step> stepTree = strategyRequest.getStepTree();
    Step rootStep = stepTree.getContents();

    // Pull all the steps out of the tree
    Collection<Step> steps = stepTree.flatten();

    RunnableObj<Strategy> strategy = Strategy.builder(getWdkModel(), user.getUserId(), stepFactory.getNewStrategyId())
      .addSteps(steps.stream().map(Step::builder).collect(Collectors.toList()))
      .setRootStepId(rootStep.getId())
      .setName(strategyRequest.getName())
      .setSavedName(strategyRequest.getSavedName())
      .setSaved(strategyRequest.isSaved())
      .setDescription(strategyRequest.getDescription())
      .setIsPublic(strategyRequest.isPublic())
      .build(new UserCache(user), ValidationLevel.RUNNABLE)
      .getRunnable()
      .getOrThrow(strat -> new DataValidationException(strat.getValidationBundle().toString()));

    stepFactory.insertStrategy(strategy.getObject());

    return strategy.getObject();
  }

  /**
   * Parse the new values from a change request json object and apply them to
   * given strategy.
   *
   * @param strat  Strategy to update.
   * @param change JSON object containing change set to apply.
   *
   * @return New strategy instance with the given changes applied.
   * @throws InvalidStrategyStructureException 
   */
  private Strategy applyStrategyChange(
    Strategy strat,
    JSONObject change
  ) throws WdkModelException, InvalidStrategyStructureException {
    final Strategy.StrategyBuilder build = new Strategy.StrategyBuilder(strat);

    for(final String key : change.keySet()) {
      switch (key) {
        case JsonKeys.NAME:
          build.setName(change.getString(key));
          break;
        case JsonKeys.SAVED_NAME:
          build.setSavedName(change.getString(key));
          break;
        case JsonKeys.IS_SAVED:
          build.setSaved(change.getBoolean(key));
          break;
        case JsonKeys.IS_PUBLIC:
          build.setSaved(change.getBoolean(key));
          break;
        case JsonKeys.DESCRIPTION:
          build.setDescription(change.getString(key));
          break;
      }
    }

    return build.build(new UserCache(getUserBundle(Access.PRIVATE).getSessionUser()),
        ValidationLevel.NONE);
  }

  /**
   * Validate the change request on a saved strategy
   *
   * @param body Change request
   *
   * @return an option of an error report.
   */
  private static Optional<JsonNode> validateSavedStratChange(JSONObject body) {
    final UnaryOperator<String> toErrorMessage = s -> String.format(
        "Property \"%s\" cannot be changed on a saved strategy", s);

    final ArrayNode invalid = body.keySet()
        .stream()
        .filter(not(StrategyService::isAllowedOnSavedStrategy))
        .map(toErrorMessage)
        .collect(JSON::createArrayNode, ArrayNode::add, ArrayNode::addAll);

    return invalid.size() == 0 ? Optional.empty() :
        Optional.of(JSON.createObjectNode().set(JsonKeys.ERRORS, invalid));
  }

  /**
   * Returns whether or not data stored at the given key is legal to change on
   * a saved strategy.
   *
   * @param key Key to check.
   *
   * @return Whether or not data stored at the given key can be changed on a
   *         saved strategy.
   */
  private static boolean isAllowedOnSavedStrategy(final String key) {
    return JsonKeys.NAME.equals(key) || JsonKeys.IS_PUBLIC.equals(key);
  }
}
