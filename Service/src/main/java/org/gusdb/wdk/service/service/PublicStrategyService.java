package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.pSwallow;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.json.JSONException;

/**
 * Provides list of public strategies
 * 
 */
@Path("/strategy-lists/public")
@Produces(MediaType.APPLICATION_JSON)
public class PublicStrategyService extends AbstractWdkService {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(PublicStrategyService.class);

  /**
   * Get a list of all valid public strategies
   */
  @GET
  public Response getPublicStrategies() throws JSONException, WdkModelException {
    List<Strategy> publicStrategies = getWdkModel().getStepFactory().getPublicStrategies();
    List<Strategy> validPublicStrategies = filter(publicStrategies, pSwallow(strategy -> strategy.isValid()));
    return Response.ok(StrategyFormatter.getStrategiesJson(validPublicStrategies, false).toString()).build();
  }

}
