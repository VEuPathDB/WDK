package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.AbstractDatasetParser;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.DatasetParamHandler;
import org.gusdb.wdk.model.query.param.MapBasedRequestParams;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatasetService extends UserService {

  public DatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  /**
   * Input JSON is:
   * {
   *   "displayName": String (optional),
   *   "sourceType": Enum<IdList,Basket> // more types to come...
   *   "sourceContent": {
   *     "ids": Array<String>, // only for IdList
   *     "basketName": String  // record class full name, only for basket
   *   }
   * }
   *
   * @param body request body (JSON)
   * @return HTTP response for this request
   * @throws RequestMisformatException
   * @throws DataValidationException
   */
  @POST
  @Path("datasets")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addDatasetFromJson(String body)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      JSONObject input = new JSONObject(body);
      DatasetFactory factory = getWdkModel().getDatasetFactory();
      Dataset dataset = createFromSource(
          JsonUtil.getStringOrDefault(input, JsonKeys.SOURCE_TYPE, JsonKeys.ID_LIST), user,
          input.getJSONObject(JsonKeys.SOURCE_CONTENT), factory);
      String displayName = JsonUtil.getStringOrDefault(input, JsonKeys.DISPLAY_NAME, dataset.getName());
      if (!displayName.equals(dataset.getName())) {
        dataset.setName(displayName);
        factory.saveDatasetMetadata(dataset);
      }
      return Response.ok(new JSONObject().put(JsonKeys.ID, dataset.getDatasetId())).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.toString());
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
  }

  private Dataset createFromSource(String sourceType, User user, JSONObject sourceConfig, DatasetFactory factory)
      throws WdkModelException, WdkUserException, DataValidationException {
    switch(sourceType) {
      case JsonKeys.ID_LIST: return createFromIdList(user, sourceConfig, factory);
      case JsonKeys.BASKET:  return createFromBasket(user, sourceConfig, factory);
      default:
        throw new DataValidationException("Unrecognized " + JsonKeys.SOURCE_TYPE + ": " + sourceType);
    }
  }

  private static Dataset createFromIdList(User user, JSONObject sourceConfig, DatasetFactory factory)
      throws DataValidationException, WdkUserException, WdkModelException {
    JSONArray jsonIds = sourceConfig.getJSONArray(JsonKeys.IDS);
    if (jsonIds.length() == 0)
      throw new DataValidationException("At least 1 ID must be submitted");
    final List<String> ids = new ArrayList<>();
    for (int i = 0; i < jsonIds.length(); i++) {
      ids.add(jsonIds.getString(i));
    }
    // FIXME: this is a total hack to comply with the dataset factory API
    //   We are closing over the JSON array we already parsed and will return
    //   a List<String> version of that array
    DatasetParser parser = new AbstractDatasetParser() {
      @Override
      public List<String[]> parse(String content) {
        return Functions.mapToList(ids, str -> new String[]{ str });
      }
      @Override
      public String getName() {
        return "anonymous";
      }
    };
    return factory.createOrGetDataset(user, parser, FormatUtil.join(ids.toArray(), " "), "");
  }

  public static Dataset createFromBasket(User user, JSONObject sourceConfig, DatasetFactory factory)
      throws WdkModelException, WdkUserException {
    WdkModel wdkModel = factory.getWdkModel();
    String recordClassName = sourceConfig.getString(JsonKeys.BASKET_NAME);
    RecordClass recordClass = wdkModel.getRecordClassByUrlSegment(recordClassName);
    String questionName = BasketFactory.getSnapshotBasketQuestionName(recordClass);
    Question question = wdkModel.getQuestion(questionName);
    DatasetParam param = (DatasetParam) question.getParamMap().get(BasketFactory.getDatasetParamName(recordClass));
    DatasetParamHandler handler = (DatasetParamHandler) param.getParamHandler();
    String datasetId = handler.getStableValue(user, new MapBasedRequestParams()
        .setParam(param.getTypeSubParam(), DatasetParam.TYPE_BASKET));
    return factory.getDataset(user, Long.parseLong(datasetId));
  }

  @POST
  @Path("datasets-from-basket")
  @Produces(MediaType.TEXT_PLAIN)
  public Response createDatasetFromBasket(@QueryParam("rc") String recordClassName) throws WdkModelException, WdkUserException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    WdkModel wdkModel = getWdkModel();
    RecordClass recordClass = wdkModel.getRecordClassByUrlSegment(recordClassName);
    String questionName = BasketFactory.getSnapshotBasketQuestionName(recordClass);
    Question question = wdkModel.getQuestion(questionName);
    DatasetParam param = (DatasetParam) question.getParamMap().get(BasketFactory.getDatasetParamName(recordClass));
    DatasetParamHandler handler = (DatasetParamHandler) param.getParamHandler();
    String datasetId = handler.getStableValue(user, new MapBasedRequestParams()
        .setParam(param.getTypeSubParam(), DatasetParam.TYPE_BASKET));
    return Response.ok(new JSONObject().put(JsonKeys.ID, datasetId)).build();
  }

  @POST
  @Path("datasets")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addDatasetFromFile() throws WdkModelException
      //@FormParam("file") InputStream fileInputStream,
      //@FormParam("file") FormDataContentDisposition contentDispositionHeader)
  {
    getUserBundle(Access.PRIVATE); // makes sure only current user can access this endpoint
    return Response.ok("{ }").build();

  }
}
