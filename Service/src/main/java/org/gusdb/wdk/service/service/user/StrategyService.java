package org.gusdb.wdk.service.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.gusdb.fgputil.SetBuilder;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
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
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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
  public JSONArray getStrategies() throws WdkModelException {
    User user = getPrivateRegisteredUser();
    List<Strategy> strategies = getWdkModel().getStepFactory()
        .getStrategies(user.getUserId(), false, false);
    return StrategyFormatter.getStrategiesJson(strategies);
  }

  @POST
  @Path("strategies")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createStrategy(String body)
      throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      JSONObject json = new JSONObject(body);
      StepFactory stepFactory = getWdkModel().getStepFactory();

      Strategy strategy = json.has(JsonKeys.SOURCE_SIGNATURE)
          ? copyStrategy(user, stepFactory, json)
          : createNewStrategy(user, stepFactory, json);

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
  // TODO: @InSchema(...)
  public void updateStrategy(@PathParam("strategyId") long strategyId,
      JSONObject body) throws WdkModelException {
    final StepFactory fac = getWdkModel().getStepFactory();
    final Strategy strat = fac.getStrategyById(strategyId)
        .orElseThrow(NotFoundException::new);

    if (strat.isSaved()) {
      Optional<JsonNode> err = validateSavedStratChange(body);
      if (err.isPresent())
        throw new BadRequestException(
            Response.status(Response.Status.BAD_REQUEST)
              .entity(err)
              .build()
        );
    }

    fac.updateStrategy(applyStrategyChange(strat, body));
  }

  @PUT
  @Path("strategies/{strategyId}/answerSpec")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO: @InSchema(...)
  public void putAnswerSpec(
    @PathParam("strategyId") long strategyId,
    ObjectNode body
  ) {
    throw new InternalServerErrorException("method not implemented");
  }

  @GET
  @Path("strategies/{strategyId}")
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getStrategy(@PathParam("strategyId") String strategyId)
      throws WdkModelException {
    return StrategyFormatter.getDetailedStrategyJson(
        getStrategyForCurrentUser(strategyId));
  }

  protected Strategy getStrategyForCurrentUser(String strategyId) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      // Whether the user owns this strategy or not is resolved in the getStepFactory method
      Strategy strategy = getWdkModel().getStepFactory()
          .getStrategyById(Long.parseLong(strategyId))
          .orElseThrow(() -> new NotFoundException(
              AbstractWdkService.formatNotFound(STRATEGY_RESOURCE + strategyId)));

      if (strategy.getUser().getUserId() != user.getUserId())
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);

      return strategy;
    }
    catch (NumberFormatException | WdkModelException e) {
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
    List<Step> steps = stepTree.findAll(step -> true).stream()
        .map(TreeNode::getContents)
        .collect(Collectors.toList());

    // Update steps with filled in answer params and save.
    for (Step step : steps) {
      stepFactory.patchAnswerParams(step);
    }

    // Create the strategy
    Strategy strategy = stepFactory.createStrategy(user, rootStep,
        strategyRequest.getName(), strategyRequest.getSavedName(),
        strategyRequest.isSaved(), strategyRequest.getDescription(),
        strategyRequest.isHidden(), strategyRequest.isPublic());

    // Add new strategy to all the embedded steps
    steps.forEach(step -> step.setStrategyId(strategy.getStrategyId()));

    // Update left/right child ids in db first
    // rootStep.update(true);

    // Update those steps in the database with the strategyId
    stepFactory.setStrategyIdForThisAndUpstreamSteps(rootStep, strategy.getStrategyId());
    return strategy;
  }

  private Strategy applyStrategyChange(
    Strategy strat,
    JSONObject change
  ) {
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

    return build.build(); // TODO: Should this even go here?
  }

  /**
   * Validate the change request on a saved strategy
   *
   * @param body Change request
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

  private static boolean isAllowedOnSavedStrategy(final String key) {
    return JsonKeys.NAME.equals(key) || JsonKeys.IS_PUBLIC.equals(key);
  }
}
