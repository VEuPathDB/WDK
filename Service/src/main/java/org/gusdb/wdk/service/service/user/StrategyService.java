package org.gusdb.wdk.service.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.*;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StrategyRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.gusdb.fgputil.functional.Functions.not;

public class StrategyService extends UserService {

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
      .getStrategies(getPrivateRegisteredUser().getUserId(), false, false));
  }

  @POST
  @Path("strategies")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategies.post-request")
  @OutSchema("wdk.users.strategies.post-response")
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
    catch (RequestMisformatException rmfe) {
      throw new BadRequestException(rmfe);
    }
    catch (WdkUserException wue) {
      throw new DataValidationException(wue);
    }
  }

  @PATCH
  @Path("strategies/{strategyId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategy.patch-request")
  public void updateStrategy(@PathParam("strategyId") long strategyId,
      JSONObject body) throws WdkModelException {
    final StepFactory fac = getWdkModel().getStepFactory();
    final Strategy strat = fac.getStrategyById(strategyId)
      .orElseThrow(NotFoundException::new);

    if (strat.isSaved()) {
      validateSavedStratChange(body).ifPresent(err -> {
        throw new BadRequestException(
          Response.status(Response.Status.BAD_REQUEST).entity(err).build());
      });
    }

    fac.updateStrategy(applyStrategyChange(strat, body));
  }

  @GET
  @Path("strategies/{strategyId}")
  @Produces(MediaType.APPLICATION_JSON)
  // TODO: @OutSchema(...)
  public JSONObject getStrategy(@PathParam("strategyId") long strategyId)
      throws WdkModelException {
    return StrategyFormatter.getDetailedStrategyJson(
      getStrategyForCurrentUser(strategyId));
  }

  private Strategy getStrategyForCurrentUser(long strategyId) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      // Whether the user owns this strategy or not is resolved in the getStepFactory method
      Strategy strategy = getWdkModel().getStepFactory()
        .getStrategyById(strategyId)
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
      throws WdkModelException, DataValidationException, WdkUserException {
    StrategyRequest strategyRequest = StrategyRequest.createFromJson(json,
      stepFactory, user, getWdkModel().getProjectId());
    TreeNode<Step> stepTree = strategyRequest.getStepTree();
    Step rootStep = stepTree.getContents();

    // Pull all the steps out of the tree
    Collection<Step> steps = stepTree.flatten();

    // Create the strategy
    Strategy strategy = stepFactory.createStrategy(user, rootStep,
        strategyRequest.getName(), strategyRequest.getSavedName(),
        strategyRequest.isSaved(), strategyRequest.getDescription(),
        strategyRequest.isHidden(), strategyRequest.isPublic());

    final StepFactoryHelpers.UserCache uc = new StepFactoryHelpers.UserCache(user);

    // Add new strategy to all the embedded steps
    // TODO: Review this because it's probably wrong
    steps.stream()
      .map(Step::builder)
      .peek(step -> step.setStrategyId(strategy.getStrategyId()))
      .forEach(step -> step.build(uc, validationLevel, strategy.getStrategyId()));

    // Update those steps in the database with the strategyId
    stepFactory.setStrategyIdForThisAndUpstreamSteps(rootStep, strategy.getStrategyId());
    return strategy;
  }

  /**
   * Parse the new values from a change request json object and apply them to
   * given strategy.
   *
   * @param strat  Strategy to update.
   * @param change JSON object containing change set to apply.
   *
   * @return New strategy instance with the given changes applied.
   */
  private Strategy applyStrategyChange(
    Strategy strat,
    JSONObject change
  ) throws WdkModelException {
    final Strategy.StrategyBuilder build = new Strategy.StrategyBuilder(strat);

    for(final String key : change.keySet()) {
      switch (key) {
        case JsonKeys.NAME:
          build.setName(change.getString(key));
          break;
        case JsonKeys.SAVED_NAME:
          build.setSavedName(change.getString(key));
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

    return build.build(getUserBundle(Access.PRIVATE).getSessionUser(),
        /*TODO: Validation level*/);
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
