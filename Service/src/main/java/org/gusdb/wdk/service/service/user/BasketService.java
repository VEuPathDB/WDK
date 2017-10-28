package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.BasketRequests;
import org.gusdb.wdk.service.request.user.BasketRequests.BasketActions;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Use cases this service supports:
 * 
 * - Get list of baskets/RCs with record counts (1)
 * - Add/Remove one record in basket (2)
 * - Add/Remove multiple records in basket (2)
 * - Clear an entire basket (3)
 * - Check whether set of records are in a basket (4)
 * - Get single basket as a step result (i.e. answer) (5)
 * 
 * Unsupported (supported by other resources):
 * 
 * - Create a new step/strategy that returns the IDs in a basket
 * 
 * Thus, this service provides the following service endpoints (all behind /user/{id}):
 * 
 * 1. GET    /baskets                                  returns list of baskets (record classes) and record count in each basket
 * 2. PATCH  /baskets/{recordClassOrUrlSegment}        add or remove multiple records from a basket
 * 3. DELETE /baskets/{recordClassOrUrlSegment}        clears all records from a basket
 * 4. POST   /baskets/{recordClassOrUrlSegment}/query  queries basket status (presence) of multiple records at one time
 * 5. POST   /baskets/{recordClassOrUrlSegment}/answer same API as "format" property of answer service
 * 
 * TODO #1: Need to add option in POST /dataset endpoint to create from basket (i.e. basket snapshot)
 *            (Also- change RecordsByBasketSnapshot question to take dataset ID, maybe generalize to GenesByDataset, etc)
 * TODO #2: Disallow answer service access to basket questions (supported by /basket/{id}/answer)
 */
public class BasketService extends UserService {

  private static final String BASKET_NAME_PARAM = "basketName";
  private static final String BASE_BASKET_PATH = "baskets";
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
  public Response patchBasket(@PathParam(BASKET_NAME_PARAM) String basketName, String body)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    try {
      User user = getPrivateRegisteredUser();
      RecordClass recordClass = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
      BasketActions actions = BasketRequests.parseBasketActionsJson(new JSONObject(body), recordClass);
      BasketFactory factory = getWdkModel().getBasketFactory();
      factory.addPksToBasket(user, recordClass, actions.getRecordsToAdd());
      factory.removePksFromBasket(user, recordClass, actions.getRecordsToRemove());
      return Response.noContent().build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  @DELETE
  @Path(NAMED_BASKET_PATH)
  public Response clearBasket(@PathParam(BASKET_NAME_PARAM) String basketName) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    RecordClass recordClass = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
    getWdkModel().getBasketFactory().clearBasket(user, recordClass);
    return Response.noContent().build();
  }

  /**
   * Input is a array of record primary keys
   * [
   *   [
   *     { name: pk_col_1_name, value: pk_col_1_value },
   *     { name: pk_col_2_name, value: pk_col_2_value },
   *     ...
   *   ]
   * ]
   * 
   * Output is a boolean array of identical size representing whether
   * each ID is present in the requested basket.  Ordering is the same
   * as the incoming array (i.e. output element at index N is the status
   * of incoming primary key at index N).
   * 
   * @param body
   * @return
   * @throws WdkModelException
   * @throws RequestMisformatException
   * @throws DataValidationException 
   */
  @POST
  @Path(NAMED_BASKET_PATH + "/query")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response queryBasket(@PathParam(BASKET_NAME_PARAM) String basketName, String body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    try {
      User user = getPrivateRegisteredUser();
      RecordClass recordClass = RecordService.getRecordClassOrNotFound(basketName, getWdkModel());
      JSONArray inputArray = new JSONArray(body);
      List<PrimaryKeyValue> pksToQuery = new ArrayList<>();
      for (JsonType pkArray : JsonIterators.arrayIterable(inputArray)) {
        if (!pkArray.getType().equals(JsonType.ValueType.ARRAY)) {
          throw new RequestMisformatException("All input array elements must be arrays.");
        }
        pksToQuery.add(RecordRequest.parsePrimaryKey(pkArray.getJSONArray(), recordClass));
      }
      List<Boolean> result = getWdkModel().getBasketFactory().queryBasketStatus(user, recordClass, pksToQuery);
      return Response.ok(new JSONArray(result).toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
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
