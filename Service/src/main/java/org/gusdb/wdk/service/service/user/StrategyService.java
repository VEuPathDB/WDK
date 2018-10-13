package org.gusdb.wdk.service.service.user;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.strategy.StrategyRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONObject;


public class StrategyService extends UserService {

  public static final String STRATEGY_RESOURCE = "Strategy ID ";

  public StrategyService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("strategies")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStrategies() throws WdkModelException {
    User user = getPrivateRegisteredUser();
    List<Strategy> strategies = getWdkModel().getStepFactory().getStrategies(user.getUserId(), false, false);
    return Response.ok(StrategyFormatter.getStrategiesJson(strategies).toString()).build();
  }

  @POST
  @Path("strategies")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createStrategy(String body) throws WdkModelException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      JSONObject json = new JSONObject(body);
      StepFactory stepFactory = getWdkModel().getStepFactory();

      Strategy strategy;
      if (json.has(JsonKeys.SOURCE_SIGNATURE)) {
        Strategy sourceStrategy = stepFactory.loadStrategy( json.getString(JsonKeys.SOURCE_SIGNATURE));
        strategy = stepFactory.copyStrategy(user, sourceStrategy, sourceStrategy.getName());       
      } else {
        strategy = createNewStrategy(user, stepFactory, json);
      }

      return Response.ok(new JSONObject().put(JsonKeys.ID, strategy.getStrategyId()))
          .location(getUriInfo().getAbsolutePathBuilder().build(strategy.getStrategyId()))
          .build();
     }
     catch(WdkModelException wme) {
           throw new WdkModelException("Unable to create the strategy.", wme);
     }
     catch(RequestMisformatException rmfe) {
           throw new BadRequestException(rmfe);
     }
     catch(WdkUserException wue) {
           throw new DataValidationException(wue);
     }
  }
    
  private Strategy createNewStrategy(User user, StepFactory stepFactory, JSONObject json)
      throws WdkModelException, DataValidationException, WdkUserException {

    StrategyRequest strategyRequest = StrategyRequest.createFromJson(json, stepFactory, user,
        getWdkModel().getProjectId());
    TreeNode<Step> stepTree = strategyRequest.getStepTree();
    Step rootStep = stepTree.getContents();

    // Pull all the steps out of the tree
    List<Step> steps = stepTree.findAll(step -> true).stream().map(node -> node.getContents()).collect(
        Collectors.toList());

    // Update steps with filled in answer params and save.
    for (Step step : steps) {
      stepFactory.patchAnswerParams(step);
    }

    // Create the strategy
    Strategy strategy = stepFactory.createStrategy(user, rootStep, strategyRequest.getName(),
        strategyRequest.getSavedName(), strategyRequest.isSaved(), strategyRequest.getDescription(),
        strategyRequest.isHidden(), strategyRequest.isPublic());

    // Add new strategy to all the embedded steps
    steps.forEach(step -> step.setStrategyId(strategy.getStrategyId()));

    // Update left/right child ids in db first
    // rootStep.update(true);

    // Update those steps in the database with the strategyId
    stepFactory.setStrategyIdForThisAndUpstreamSteps(rootStep, strategy.getStrategyId());
    return strategy;
  }

  @GET
  @Path("strategies/{strategyId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStrategy(@PathParam("strategyId") String strategyId) throws WdkModelException {
    return Response.ok(StrategyFormatter.getDetailedStrategyJson(getStrategyForCurrentUser(strategyId)).toString()).build();
  }

  protected Strategy getStrategyForCurrentUser(String strategyId) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      // Whether the user owns this strategy or not is resolved in the getStepFactory method
      Strategy strategy = getWdkModel().getStepFactory().getStrategyById(Long.parseLong(strategyId));
      if (strategy.getUser().getUserId() != user.getUserId()) {
        throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);
      }
      return strategy;
    }
    catch (NumberFormatException | WdkUserException | WdkModelException e) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(STRATEGY_RESOURCE + strategyId));
    }
  }

}
