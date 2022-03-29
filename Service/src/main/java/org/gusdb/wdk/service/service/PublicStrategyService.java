package org.gusdb.wdk.service.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.Strategy.StrategyBuilder;
import org.gusdb.wdk.model.user.StrategyLoader.UnbuildableStrategyList;
import org.gusdb.wdk.service.formatter.StrategyFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Provides list of public strategies
 *
 * TODO: rename this class to StrategyListsService, after merge into trunk
 */
@Path("/strategy-lists/public")
@Produces(MediaType.APPLICATION_JSON)
public class PublicStrategyService extends AbstractWdkService {

  /**
   * Get a list of all valid public strategies
   * the isInvalid query param is undocumented, and for internal use only, allowing
   * developers to review invalid strategies
   */
  @GET
  public Response getPublicStrategies(
      @QueryParam("userEmail") List<String> userEmails,
      @QueryParam("invalid") @DefaultValue("false") Boolean returnInvalid)
  throws JSONException, WdkModelException {
    Stream<Strategy> strategies = getWdkModel()
      .getStepFactory()
      .getPublicStrategies()
      .stream()
      .filter(returnInvalid ?
        Functions.not(Strategy::isValid) :
        Strategy::isValid);

    if (!userEmails.isEmpty())
      strategies = strategies.filter(strat -> userEmails.stream()
        .anyMatch(userEmail -> strat.getUser()
          .getEmail()
          .equals(userEmail)));

    // slightly different response if requesting valid vs invalid; if invalid:
    //   1. include validation object as additional property on each strat
    //   2. format JSON into more human-readable string for viewing in a browser/terminal
    boolean includeValidationObjects = returnInvalid;
    JSONArray responseJson = StrategyFormatter.getStrategiesJson(
        strategies.collect(Collectors.toList()), includeValidationObjects);
    return Response.ok(returnInvalid ? responseJson.toString(2) : responseJson.toString()).build();
  }

  /**
   * Get a list of the IDs of public strategies that cannot be built due to
   * exceptions occurring during building.
   * @throws WdkModelException 
   */
  @GET
  @Path("/errors")
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getErroredPublicStrategies() throws WdkModelException {
    assertAdmin();
    TwoTuple<
      UnbuildableStrategyList<InvalidStrategyStructureException>,
      UnbuildableStrategyList<WdkModelException>
    > erroredStrats = getWdkModel()
      .getStepFactory()
      .getPublicStrategyErrors();
    return new JSONObject()
      .put("structuralErrors", formatErrors(erroredStrats.getFirst()))
      .put("buildErrors", formatErrors(erroredStrats.getSecond()));
  }

  private <T extends Exception> JSONArray formatErrors(UnbuildableStrategyList<T> list) {
    JSONArray arr = new JSONArray();
    for (TwoTuple<StrategyBuilder, T> erroredStrat : list) {
      StrategyBuilder strat = erroredStrat.getFirst();
      Exception e = erroredStrat.getSecond();
      arr.put(new JSONObject()
        .put(JsonKeys.STRATEGY_ID, strat.getStrategyId())
        .put(JsonKeys.USER_ID, strat.getUserId())
        .put(JsonKeys.EXCEPTION, new JSONObject()
          .put(JsonKeys.MESSAGE, e.getMessage())
          .put(JsonKeys.STACK_TRACE, FormatUtil.getStackTrace(e))));
    }
    return arr;
  }

}
