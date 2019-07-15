package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserCache;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.strategy.StrategyRequest;
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
  @Path(BASE_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.users.strategies.get-response")
  public JSONArray getStrategies() throws WdkModelException {
    return StrategyFormatter.getStrategiesJson(getWdkModel().getStepFactory()
      .getStrategies(getUserBundle(Access.PRIVATE).getSessionUser().getUserId(),
          ValidationLevel.SYNTACTIC).values());
  }

  @POST
  @Path(BASE_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategies.post-request")
  @OutSchema("wdk.standard-post-response")
  public Response createStrategy(JSONObject body)
      throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      StepFactory stepFactory = getWdkModel().getStepFactory();
      Strategy strategy = body.has(JsonKeys.SOURCE_STRATEGY_SIGNATURE)
        ? copyStrategy(user, stepFactory, body)
        : createNewStrategy(user, stepFactory, body);

      return Response.ok(new JSONObject().put(JsonKeys.ID, strategy.getStrategyId()))
        .location(getUriInfo().getAbsolutePathBuilder().build(strategy.getStrategyId()))
        .build();
    }
    catch (WdkModelException wme) {
      throw new WdkModelException("Unable to create the strategy.", wme);
    }
    catch (WdkUserException | InvalidStrategyStructureException wue) {
      throw new DataValidationException(wue);
    }
  }

  @PATCH
  @Path(BASE_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategy.patch-request")
  public void deleteStrategies(JSONObject[] strats) // TODO: Find a better name for me
      throws WdkModelException {
    final Collection<Strategy> toUpdate = new ArrayList<>(strats.length);

    try {
      for (JSONObject action : strats) {
        final long id = action.getLong(JsonKeys.STRATEGY_ID);
        final boolean del = action.getBoolean(JsonKeys.IS_DELETED);
        final Strategy strat = getStrategyForCurrentUser(id, ValidationLevel.NONE);

        if (del != strat.isDeleted())
          toUpdate.add(Strategy.builder(strat).setDeleted(del)
              .build(new UserCache(strat.getUser()), ValidationLevel.NONE));
      }
    } catch (InvalidStrategyStructureException e) {
      throw new WdkModelException(e);
    }

    getWdkModel().getStepFactory().updateStrategies(toUpdate);
  }

  @GET
  @Path(ID_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.users.strategies.id.get-response")
  public JSONObject getStrategy(@PathParam(ID_PARAM) long strategyId)
      throws WdkModelException {
    Strategy strategy = getStrategyForCurrentUser(strategyId, ValidationLevel.RUNNABLE);
    // update result sizes for all runnable steps that need refreshing
    strategy.updateStaleResultSizesOnRunnableSteps();
    return StrategyFormatter.getDetailedStrategyJson(strategy);
  }

  @PATCH
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategies.id.patch-request")
  public void updateStrategy(@PathParam(ID_PARAM) long strategyId,
      JSONObject body) throws WdkModelException, DataValidationException {
    final StepFactory fac = getWdkModel().getStepFactory();
    final Strategy strat = getNotDeletedStrategyForCurrentUser(strategyId, ValidationLevel.NONE);
    
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
  
  @DELETE
  @Path(ID_PATH)
  public void deleteStrategy(@PathParam(ID_PARAM) long strategyId)
      throws WdkModelException {
    try {
      Strategy strat = getNotDeletedStrategyForCurrentUser(strategyId, ValidationLevel.NONE); // confirm not already deleted
      strat = Strategy.builder(strat).setDeleted(true)
              .build(new UserCache(strat.getUser()), ValidationLevel.NONE);
      
      getWdkModel().getStepFactory().updateStrategy(strat);
    }
    catch (InvalidStrategyStructureException e) {
      throw new WdkModelException(e);
    }
  }

  @PUT
  @Path(ID_PATH + "/step-tree")
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.strategies.id.put-request")
  public void replaceStepTree(@PathParam(ID_PARAM) long stratId, JSONObject body)
      throws WdkModelException, DataValidationException {

    final Strategy oldStrat = getNotDeletedStrategyForCurrentUser(stratId, ValidationLevel.NONE);

    JSONObject stepTree = body.getJSONObject(JsonKeys.STEP_TREE);
     final StepFactory stepFactory = getWdkModel().getStepFactory();
    final TwoTuple<Long, Collection<StepBuilder>> parsedTree =
        StrategyRequest.treeToSteps(Optional.of(oldStrat), stepTree, stepFactory);
    try {

      // build and validate modified strategy
      Strategy newStrat = Strategy.builder(oldStrat)
        .clearSteps()
        .setRootStepId(parsedTree.getFirst())
        .addSteps(parsedTree.getSecond())
        .build(new UserCache(oldStrat.getUser()), ValidationLevel.NONE);
  
      // filter the list of original steps down to only steps that do not appear
      // in the new tree
      Set<Long> retainedStepIds = newStrat.getAllSteps().stream().map(st -> st.getStepId()).collect(Collectors.toSet());
      List<Step> orphanedSteps = oldStrat.getAllSteps().stream()
          // only put in orphaned if not used in new strategy
          .filter(step -> !retainedStepIds.contains(step.getStepId()))
          // remove strategy and answer param values from each step
          .map(fSwallow(orphan -> Step.builder(orphan).removeStrategy().build(
              new UserCache(oldStrat.getUser()), ValidationLevel.NONE, Optional.empty())))
          .collect(Collectors.toList());

      // update strategy and newly orphaned steps
      stepFactory.updateStrategyAndOtherSteps(newStrat, orphanedSteps);

    }
    catch (InvalidStrategyStructureException e) {
      throw new DataValidationException(e.getMessage());
    }
  }

  /**
   * duplicate a strategies step tree by recreating the tree structure, but with new copies of the steps.
   *  Used to add a copy of this strategy as a nested step in another strategy.
   * @param stratId
   * @return
   * @throws WdkModelException
   */
  @POST
  @Path(ID_PATH + "/duplicated-step-tree")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @InSchema("wdk.empty-post-request") 
  @OutSchema("wdk.users.strategies.id.duplicated-step-tree-request")
  public JSONObject duplicateAsBranch(@PathParam(ID_PARAM) long stratId)
      throws WdkModelException {

    getNotDeletedStrategyForCurrentUser(stratId, ValidationLevel.NONE); // confirm it is not deleted

    return new JSONObject().put(JsonKeys.STEP_TREE,
      StepFormatter.formatAsStepTree(
        getWdkModel().getStepFactory().copyStrategyToBranch(
          getSessionUser(),
          getStrategyForCurrentUser(stratId, ValidationLevel.NONE)
        ),
        new HashSet<Step>() // we don't need to consume the list of step IDs found in the tree
      )
    );
  }

  // get a strategy, but throw not found if it is already deleted.
  private Strategy getNotDeletedStrategyForCurrentUser(long strategyId, ValidationLevel level) {
    Strategy strat = getStrategyForCurrentUser(strategyId, level);
    if (strat.isDeleted()) throw new NotFoundException(formatNotFound(STRATEGY_RESOURCE + strategyId));
    return strat;
  }
  
  private Strategy getStrategyForCurrentUser(long strategyId, ValidationLevel level) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();

      Strategy strategy = getWdkModel().getStepFactory()
        .getStrategyById(strategyId, level)
        .orElseThrow(() -> new NotFoundException(
            formatNotFound(STRATEGY_RESOURCE + strategyId)));

      if (strategy.getUser().getUserId() != user.getUserId())
        throw new ForbiddenException(PERMISSION_DENIED);

      return strategy;
    }
    catch (WdkModelException e) {
      throw new NotFoundException(formatNotFound(STRATEGY_RESOURCE + strategyId));
    }
  }

  private Strategy copyStrategy(User user, StepFactory stepFactory, JSONObject json)
      throws WdkModelException, WdkUserException, JSONException {
    String signature = json.getString(JsonKeys.SOURCE_STRATEGY_SIGNATURE);
    Strategy sourceStrategy = stepFactory.getStrategyBySignature(signature)
      .orElseThrow(() -> new WdkUserException(
        "No strategy exists with signature " + signature));
    return stepFactory.copyStrategy(user, sourceStrategy, new LinkedHashMap<>());
  }

  private Strategy createNewStrategy(User user, StepFactory stepFactory, JSONObject json)
      throws WdkModelException, DataValidationException, InvalidStrategyStructureException {
    StrategyRequest strategyRequest = StrategyRequest.createFromJson(Optional.empty(), json, stepFactory);
    long strategyId = stepFactory.getNewStrategyId();
    String signature = Strategy.createSignature(getWdkModel().getProjectId(), user.getUserId(), strategyId);

    RunnableObj<Strategy> strategy = Strategy.builder(getWdkModel(), user.getUserId(), strategyId)
      .addSteps(strategyRequest.getSteps())
      .setRootStepId(strategyRequest.getRootStepId())
      .setName(strategyRequest.getName())
      .setSavedName(strategyRequest.getSavedName())
      .setSaved(strategyRequest.isSaved())
      .setDescription(strategyRequest.getDescription())
      .setIsPublic(strategyRequest.isPublic())
      .setSignature(signature)
      .build(new UserCache(user), ValidationLevel.RUNNABLE)
      .getRunnable()
      .getOrThrow(strat -> new DataValidationException(strat.getValidationBundle().toString()));

    stepFactory.insertStrategy(strategy.get());

    return strategy.get();
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
    final Strategy.StrategyBuilder build = Strategy.builder(strat);

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
          build.setIsPublic(change.getBoolean(key));
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
