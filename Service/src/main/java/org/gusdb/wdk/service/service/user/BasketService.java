package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.functional.Functions.reduce;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONObject;

/**
 * Things this service accomplishes:
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
 * 1. GET    /basket                                  returns list of baskets (record classes) and record count in each basket
 * 2. PATCH  /basket/{recordClassOrUrlSegment}        add or delete multiple records from this basket
 * 3. DELETE /basket/{recordClassOrUrlSegment}        clears all records from a basket
 * 4. POST   /basket/{recordClassOrUrlSegment}/query  queries basket status (presence) of multiple records at one time
 * 5. POST   /basket/{recordClassOrUrlSegment}/answer same API as answer service without answerSpec (since already defined)
 * 
 * TODO disallow answer service access to basket questions
 */
public class BasketService extends UserService {

  private static final String BASKET_NAME_PARAM = "basketName";
  private static final String BASE_BASKET_PATH = "basket";
  private static final String NAMED_BASKET_PATH = BASE_BASKET_PATH + "/{" + BASKET_NAME_PARAM + "}";

  public BasketService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    super(userIdStr);
  }

  @GET
  @Path(BASE_BASKET_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBaskets() throws WdkModelException {
    return Response.ok(
        reduce(
            getWdkModel().getBasketFactory().getBasketCounts(getPrivateRegisteredUser()).entrySet(),
            (json, entry) -> json.put(entry.getKey().getFullName(), entry.getValue()),
            new JSONObject()
        ).toString()
    ).build();
  }

  @PATCH
  @Path(NAMED_BASKET_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response patchBasket(@PathParam(BASKET_NAME_PARAM) String basketName, String body) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    RecordClass rc = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
    // TODO finish
    return Response.ok().build();
  }

  @DELETE
  @Path(NAMED_BASKET_PATH)
  public Response clearBasket(@PathParam(BASKET_NAME_PARAM) String basketName) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    RecordClass recordClass = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
    getWdkModel().getBasketFactory().clearBasket(user, recordClass);
    return Response.noContent().build();
  }

  @POST
  @Path(NAMED_BASKET_PATH + "/query")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response queryBasket(@PathParam(BASKET_NAME_PARAM) String basketName, String body) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    RecordClass rc = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
    // TODO finish
    return Response.ok().build();
  }

  @POST
  @Path(NAMED_BASKET_PATH + "/answer")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBasketAnswer(@PathParam(BASKET_NAME_PARAM) String basketName, String body) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    RecordClass rc = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
    // TODO finish
    return Response.ok().build();
  }
}
