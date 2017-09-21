package org.gusdb.wdk.service.service.user;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

/**
 * Things old implementation supports:
 * 
 * - Get list of baskets/RCs with record counts (1)
 * - Add/Remove one record in basket (2)
 * - Add/Remove multiple records in basket (2)
 * - Clear an entire basket (3)
 * - Check whether set of records are in a basket (4)
 * - Get single basket as a step result (i.e. answer) (5)
 * - TODO Save basket to a step/strategy (RRD: should be done by step/strategy service)
 * 
 * Thus, this service provides the following service endpoints (all behind /user/{id}):
 * 
 * 1. GET    /basket                                returns list of baskets (record classes) and record count in each basket
 * 2. PATCH  /basket/{recordClassOrUrlSegment}        add or delete multiple records from this basket
 * 3. DELETE /basket/{recordClassOrUrlSegment}        clears all records from a basket
 * 4. POST   /basket/{recordClassOrUrlSegment}/query  queries basket status (presence) of multiple records at one time
 * 5. POST   /basket/{recordClassOrUrlSegment}/answer same API as answer service without answerSpec (since already defined)
 * 
 * TODO disallow answer service access to basket questions
 */
public class BasketService extends UserService {

  public BasketService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    super(userIdStr);
  }

  @GET
  @Path("basket")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBaskets() throws WdkModelException {
    User user = getPrivateRegisteredUser();
    Map<RecordClass, Integer> counts = getWdkModel().getBasketFactory().getBasketCounts(user);
    JSONObject countsJson = new JSONObject();
    for (Map.Entry<RecordClass, Integer> entry: counts.entrySet()) {
      countsJson.put(entry.getKey().getFullName(), entry.getValue());
    }
    return Response.ok(countsJson.toString()).build();
  }

}
