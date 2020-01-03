package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.functional.Functions.reduce;
import static org.gusdb.wdk.service.service.AnswerService.CUSTOM_REPORT_SEGMENT_PAIR;
import static org.gusdb.wdk.service.service.AnswerService.REPORT_NAME_PATH_PARAM;
import static org.gusdb.wdk.service.service.AnswerService.STANDARD_REPORT_SEGMENT_PAIR;
import static org.gusdb.wdk.service.service.search.SearchColumnService.NAMED_COLUMN_SEGMENT_PAIR;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.request.AnswerFormatting;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.reporter.DefaultJsonReporter;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.BasketRequests.BasketActions;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.search.ColumnReporterService;
import org.gusdb.wdk.service.service.search.SearchColumnService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Use cases this service supports:
 * <ul>
 *   <li>Get list of baskets/RCs with record counts (1)
 *   <li>Add/Remove one record in basket (2)
 *   <li>Add/Remove multiple records in basket (2)
 *   <li>Clear an entire basket (3)
 *   <li>Check whether set of records are in a basket (4)
 *   <li>Get single basket as a step result (i.e. answer) (5)
 * </ul>
 * <p>
 * Unsupported (supported by other resources):
 * <ul>
 *   <li>Create a new step/strategy that returns the IDs in a basket
 * </ul>
 * <p>
 * Thus, this service provides the following service endpoints
 * (all behind /user/{id}):
 *
 * <pre>
 * 1. GET    /baskets                                  returns list of baskets (record classes) and record count in each basket
 * 2. PATCH  /baskets/{recordClassOrUrlSegment}        add or remove multiple records from a basket
 * 3. POST   /baskets/{recordClassOrUrlSegment}/query  queries basket status (presence) of multiple records at one time
 * 4. POST   /baskets/{recordClassOrUrlSegment}/answer same API as "format" property of answer service
 * </pre>
 */
public class BasketService extends UserService {

  private static final String BASKET_NAME_PATH_PARAM = "basketName";
  private static final String BASKETS_PATH = "baskets";
  private static final String NAMED_BASKET_PATH = BASKETS_PATH + "/{" + BASKET_NAME_PATH_PARAM + "}";
  private static final String COLUMN_REPORTER_PATH =
      NAMED_BASKET_PATH + NAMED_COLUMN_SEGMENT_PAIR + CUSTOM_REPORT_SEGMENT_PAIR;

  protected static class RevisedRequest<T> extends TwoTuple<RecordClass, T> {
    
    public RevisedRequest(RecordClass recordClass, T object) {
      super(recordClass, object);
    }
    public RecordClass getRecordClass() { return getFirst(); }
    public T getObject() { return getSecond(); }
  }

  public BasketService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    super(userIdStr);
  }

  @GET
  @Path(BASKETS_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBaskets() throws WdkModelException {
    WdkModel wdkModel = getWdkModel();
    return Response.ok(
      reduce(
        wdkModel.getBasketFactory().getBasketCounts(getPrivateRegisteredUser()).entrySet(),
        (json, entry) -> {
          String urlSegment = wdkModel.getRecordClassByName(entry.getKey()).get().getUrlSegment();
          JSONObject countsObj = new JSONObject()
            .put(JsonKeys.TOTAL_COUNT, entry.getValue().getFirst())
            .put(JsonKeys.INVALID_COUNT, entry.getValue().getSecond());
          return json.put(urlSegment, countsObj);
        },
        new JSONObject()
      ).toString()
    ).build();
  }

  @PATCH
  @Path(NAMED_BASKET_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO @InSchema(...)
  public Response patchBasket(@PathParam(BASKET_NAME_PATH_PARAM) String basketName, JSONObject body)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    try {
      User user = getPrivateRegisteredUser();
      RecordClass recordClass = getRecordClassOrNotFound(basketName);
      RevisedRequest<BasketActions> revisedRequest = translatePatchRequest(
          recordClass, new BasketActions(body, recordClass));
      BasketFactory factory = getWdkModel().getBasketFactory();

      switch (revisedRequest.getObject().getAction()) {
        case ADD:
          factory.addPksToBasket(user, revisedRequest.getRecordClass(),
              revisedRequest.getObject().getIdentifiers());
          break;
        case ADD_FROM_STEP_ID:
          factory.addEntireResultToBasket(user, revisedRequest.getObject().getRunnableAnswerSpec(user, getWdkModel()));
          break;
        case REMOVE:
          factory.removePksFromBasket(user, revisedRequest.getRecordClass(),
              revisedRequest.getObject().getIdentifiers());
          break;
        case REMOVE_ALL:
          factory.clearBasket(user, revisedRequest.getRecordClass());
          break;
      }

      return Response.noContent().build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  /**
   * Present so subclasses can override if desired
   * 
   * @throws DataValidationException if input actions are not compatible with subclass's translation
   * @throws WdkModelException if error occurs while producing revised request
   */
  protected RevisedRequest<BasketActions> translatePatchRequest(
      RecordClass recordClass, BasketActions actions)
          throws WdkModelException, DataValidationException {
    return new RevisedRequest<>(recordClass, actions);
  }


  /**
   * Input is a array of record primary keys
   * <pre>
   * [
   *   [
   *     { name: pk_col_1_name, value: pk_col_1_value },
   *     { name: pk_col_2_name, value: pk_col_2_value },
   *     ...
   *   ]
   * ]
   * </pre>
   * <p>
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
  public Response queryBasket(@PathParam(BASKET_NAME_PATH_PARAM) String basketName, String body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    try {
      User user = getPrivateRegisteredUser();
      RecordClass recordClass = getRecordClassOrNotFound(basketName);
      JSONArray inputArray = new JSONArray(body);
      List<PrimaryKeyValue> pksToQuery = new ArrayList<>();
      for (JsonType pkArray : JsonIterators.arrayIterable(inputArray)) {
        if (!pkArray.getType().equals(JsonType.ValueType.ARRAY)) {
          throw new RequestMisformatException("All input array elements must be arrays.");
        }
        pksToQuery.add(RecordRequest.parsePrimaryKey(pkArray.getJSONArray(), recordClass));
      }
      RevisedRequest<List<PrimaryKeyValue>> translatedRequest = translateQueryRequest(recordClass, pksToQuery);
      List<Boolean> result = getWdkModel().getBasketFactory().queryBasketStatus(user,
          translatedRequest.getRecordClass(), translatedRequest.getObject());
      return Response.ok(new JSONArray(result).toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  /**
   * Present so subclasses can override if desired
   * @throws WdkModelException
   */
  protected RevisedRequest<List<PrimaryKeyValue>> translateQueryRequest(
      RecordClass recordClass, List<PrimaryKeyValue> pksToQuery) throws WdkModelException {
    return new RevisedRequest<>(recordClass, pksToQuery);
  }

  @POST
  @Path(NAMED_BASKET_PATH + STANDARD_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.answer.post-response")
  public Response createStandardReportAnswer(
      @PathParam(BASKET_NAME_PATH_PARAM) String basketName,
      JSONObject requestJson)
          throws WdkModelException, RequestMisformatException, DataValidationException {
    return createCustomReportAnswer(basketName, DefaultJsonReporter.RESERVED_NAME, requestJson);
  }

  @POST
  @Path(NAMED_BASKET_PATH + CUSTOM_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_JSON)
  // Produces an unknown media type; varies depending on reporter selected
  public Response createCustomReportAnswer(
      @PathParam(BASKET_NAME_PATH_PARAM) final String basketName,
      @PathParam(REPORT_NAME_PATH_PARAM) final String reportName,
      final JSONObject requestJson)
          throws WdkModelException, RequestMisformatException, DataValidationException {
    User user = getPrivateRegisteredUser();
    RecordClass recordClass = getRecordClassOrNotFound(basketName);
    RunnableObj<AnswerSpec> basketAnswerSpec = AnswerSpec
      .builder(getWdkModel())
      .setQuestionFullName(recordClass.getRealtimeBasketQuestion().getFullName())
      .setViewFilterOptions(AnswerSpecServiceFormat.parseViewFilters(requestJson))
      .buildRunnable(getSessionUser(), StepContainer.emptyContainer());
    AnswerRequest request = new AnswerRequest(basketAnswerSpec,
        new AnswerFormatting(reportName, requestJson.getJSONObject(JsonKeys.REPORT_CONFIG)));
    return AnswerService.getAnswerResponse(user, request).getSecond();
  }

  /**
   * Similar to the custom report endpoint that takes JSON, but gets its data
   * from a form instead of JSON. It is used by the client to push the provided
   * data to a new http target (ie, a tab), for example, a download report
   *
   * @param data
   *   JSON data representing an answer request, passed in the 'data' form
   *   param
   *
   * @return generated report
   *
   * @throws RequestMisformatException
   *   if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException
   *   if JSON structure is correct but values contained are invalid
   * @throws WdkModelException
   *   if an error occurs while processing the request
   */
  @POST
  @Path(NAMED_BASKET_PATH + CUSTOM_REPORT_SEGMENT_PAIR)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createCustomReportAnswerFromForm(
      @PathParam(BASKET_NAME_PATH_PARAM) final String basketName,
      @PathParam(REPORT_NAME_PATH_PARAM) final String reportName,
      @FormParam("data") String data)
          throws WdkModelException, DataValidationException, RequestMisformatException {
    AnswerService.preHandleFormRequest(getUriInfo(), data);
    return createCustomReportAnswer(basketName, reportName, new JSONObject(data));
  }

  @POST
  @Path(COLUMN_REPORTER_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  public StreamingOutput getColumnReporterResponse(
      @PathParam(BASKET_NAME_PATH_PARAM) final String basketName,
      @PathParam(SearchColumnService.COLUMN_PATH_PARAM) final String columnName,
      @PathParam(REPORT_NAME_PATH_PARAM) final String reportName,
      final JSONObject requestJson)
          throws WdkModelException, NotFoundException, WdkUserException {
    User user = getPrivateRegisteredUser();
    RecordClass recordClass = getRecordClassOrNotFound(basketName);
    AttributeField attribute = requireColumn(recordClass, columnName);
    RunnableObj<AnswerSpec> basketAnswerSpec = AnswerSpec
      .builder(getWdkModel())
      .setQuestionFullName(recordClass.getRealtimeBasketQuestion().getFullName())
      .setViewFilterOptions(AnswerSpecServiceFormat.parseViewFilters(requestJson))
      .buildRunnable(getSessionUser(), StepContainer.emptyContainer());
    return AnswerService.getAnswerAsStream(
        attribute.makeReporterInstance(
            reportName,
            AnswerValueFactory.makeAnswer(user, basketAnswerSpec),
            JsonUtil.toJsonNode(requestJson.getJSONObject(JsonKeys.REPORT_CONFIG))
        ).orElseThrow(ColumnReporterService.makeNotFound(attribute, reportName))
    );
  }
}
