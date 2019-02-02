package org.gusdb.wdk.service.service.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.DatasetRequestProcessor;
import org.gusdb.wdk.service.request.user.DatasetRequestProcessor.DatasetRequest;
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
   *   "sourceType": Enum<IdList,Basket,Strategy,File> // more types to come...
   *   "sourceContent": {
   *     "ids": Array<String>,        // only for IdList
   *     "basketName": String,        // record class full name, only for basket
   *     "strategyId": Number,        // strategy id, only for strategy
   *     "temporaryFileId": String,   // temporary file id, only for file
   *     "parser": String,            // file content parser, only for file
   *     "parameterName": String,     // name of parameter that contains the parser configuration, only for file
   *     "questionName": String,      // name of question that contains the parameter associated w/ parameterName, only for file
   *   }
   * }
   *
   * @param input request body (JSON)
   * @return HTTP response for this request
   * @throws RequestMisformatException
   * @throws DataValidationException
   */
  @POST
  @Path("datasets")
  @InSchema("wdk.users.datasets.post-request")
  @OutSchema("wdk.users.datasets.post-response")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addDatasetFromJson(JSONObject input)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    try {
      User user = getUserBundle(Access.PRIVATE).getSessionUser();
      DatasetFactory factory = getWdkModel().getDatasetFactory();
      DatasetRequest request = new DatasetRequest(input);
      Dataset dataset = DatasetRequestProcessor.createFromRequest(request, user, factory, getSession());
      if (request.getDisplayName().isPresent()) {
        dataset.setName(request.getDisplayName().get());
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
}
