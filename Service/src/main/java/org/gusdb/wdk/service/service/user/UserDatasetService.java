package org.gusdb.wdk.service.service.user;

import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.formatter.UserDatasetFormatter;
import org.gusdb.wdk.service.request.DataValidationException;
import org.json.JSONException;
import org.json.JSONObject;

public class UserDatasetService extends UserService {

  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("user-dataset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDatasets(@QueryParam("expandDetails") Boolean expandDatasets) throws WdkModelException {
    UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
    if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
    UserBundle userBundle = getUserBundle(Access.PUBLIC); // TODO: temporary, for debugging
    Map<Integer, UserDataset> userDatasets = userDatasetStore.getUserDatasets(userBundle.getTargetUser().getUserId());
    return Response.ok(UserDatasetFormatter.getUserDatasetsJson(userDatasets, userDatasetStore, expandDatasets).toString()).build();
  }

  @POST
  @Path("user-dataset/{datasetId}/meta")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetaInfo(@PathParam("datasetId") String datasetIdStr, String body) throws WdkModelException, DataValidationException {
    try {
      Integer datasetId = Integer.parseInt(datasetIdStr);
      JSONObject json = new JSONObject(body);
      UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
      if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
      UserBundle userBundle = getUserBundle(Access.PUBLIC); // TODO: temporary, for debugging
      UserDataset userDataset = userDatasetStore.getUserDataset(userBundle.getTargetUser().getUserId(), datasetId);
      userDataset.updateMetaFromJson(json);
      return Response.ok("").build();
    }
    catch (JSONException | NumberFormatException e) {
      throw new BadRequestException(e);
    }
  }
}
