package org.gusdb.wdk.service.service.user;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.gusdb.wdk.service.service.WdkService;


public class StrategyService extends UserService {
	
  public static final String STRATEGY_RESOURCE = "Strategy ID ";

  public StrategyService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }
  
  @GET
  @Path("strategies/{strategyId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStrategy(@PathParam("strategyId") String strategyId) throws WdkModelException {
    return Response.ok(StrategyFormatter.getStrategyJson(getStrategyForCurrentUser(strategyId)).toString()).build();
  }

  
  protected Strategy getStrategyForCurrentUser(String strategyId) {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      // Whether the user owns this strategy or not is resolved in the getStepFactory method
      Strategy strategy = getWdkModel().getStepFactory().getStrategyById(user, Long.parseLong(strategyId));
      if (strategy.getUser().getUserId() != user.getUserId()) {
        throw new ForbiddenException(WdkService.PERMISSION_DENIED);
      }
      return strategy;
    }
    catch (NumberFormatException | WdkUserException | WdkModelException e) {
      throw new NotFoundException(WdkService.formatNotFound(STRATEGY_RESOURCE + strategyId));
    }
  }
}
