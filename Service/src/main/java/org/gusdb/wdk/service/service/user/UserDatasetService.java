package org.gusdb.wdk.service.service.user;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
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
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserDatasetService extends UserService {
  
  /*
   *  TODO: validate
   *    - requested user exists
   *    - sharing:
   *      - target user exists
   *      - shared dataset exists
   *    - external datasets
   *      - original dataset exists, and is still shared  
   *
   */

  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("user-dataset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDatasets(@QueryParam("expandDetails") Boolean expandDatasets) throws WdkModelException {
    UserDatasetStore userDatasetStore = getUserDatasetStore();
    Map<Integer, UserDataset> userDatasets = getUserDatasetStore().getUserDatasets(getUserId());
    Map<Integer, UserDataset> externalUserDatasets = getUserDatasetStore().getExternalUserDatasets(getUserId());
    return Response.ok(UserDatasetFormatter.getUserDatasetsJson(userDatasets, externalUserDatasets, userDatasetStore, expandDatasets).toString()).build();
  }
  
  @PUT
  @Path("user-dataset/{datasetId}/meta")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetaInfo(@PathParam("datasetId") String datasetIdStr, String body) throws WdkModelException, DataValidationException {
    try {
      JSONObject metaJson = new JSONObject(body);
      getUserDatasetStore().updateMetaFromJson(getUserId(), new Integer(datasetIdStr), metaJson);
      return Response.ok("").build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  /*
{
 "targetUsers" : [12401223],
 "datasetsToShare" : [555,777]
}

   */
  @PUT
  @Path("user-dataset/{datasetId}/share")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response shareWith(@PathParam("datasetId") String datasetIdStr, String body)
      throws WdkModelException, DataValidationException {

    JSONObject jsonObj = new JSONObject(body);
    
    // put datasets to share into a set
    JSONArray jsonDatasetsToShare = jsonObj.getJSONArray("datasetsToShare");
        Set<Integer> datasetIdsToShare = new HashSet<Integer>();
    for (int i=0; i<jsonDatasetsToShare.length(); i++) 
      datasetIdsToShare.add(jsonDatasetsToShare.getInt(i));
    
    // put target users into a set
    JSONArray jsonTargetUsers = jsonObj.getJSONArray("targetUsers");
    Set<Integer> targetUserIds = new HashSet<Integer>();
    for (int i=0; i<jsonTargetUsers.length(); i++) {
      Integer targetUserId = jsonTargetUsers.getInt(i);
      validateTargetUserId(targetUserId);
      targetUserIds.add(targetUserId);
    }
    getUserDatasetStore().shareUserDatasets(getUserId(), datasetIdsToShare, targetUserIds);

    return Response.ok("").build();

  }
  
  @PUT
  @Path("user-dataset/{datasetId}/unshare")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response unshareWith(@PathParam("datasetId") String datasetIdStr, String body)
      throws WdkModelException, DataValidationException {

    Integer shareWithUserId = new Integer(body);
    getUserDataset(datasetIdStr).unshareWith(shareWithUserId);
    return Response.ok("").build();
  }
  
  @DELETE
  @Path("{id}")
  public Response deleteById(@PathParam("id") int datasetId) throws WdkModelException {
    getUserDatasetStore().deleteUserDataset(getUserId(), datasetId);
    return Response.ok().build();
  }


  
  private UserDatasetStore getUserDatasetStore() throws WdkModelException {
    UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
    if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
    return userDatasetStore;
  }
  
  private UserDataset getUserDataset(String datasetIdStr) throws WdkModelException {
    try {
      Integer datasetId = new Integer(datasetIdStr);
      UserBundle userBundle = getUserBundle(Access.PUBLIC); // TODO: temporary, for debugging
      return getUserDatasetStore().getUserDataset(userBundle.getTargetUser().getUserId(),
          datasetId);
    }
    catch (NumberFormatException e) {
      throw new BadRequestException(e);
    }
    
  }

  private Integer getUserId() throws WdkModelException {
    return getUserBundle(Access.PUBLIC).getTargetUser().getUserId();
  }
  
  private void validateTargetUserId(Integer targetUserId) throws WdkModelException {
    UserBundle targetUserBundle = UserBundle.createFromTargetId(targetUserId.toString(), getSessionUser(), getWdkModel().getUserFactory(), isSessionUserAdmin());
    if (!targetUserBundle.isValidUserId()) {
      throw new NotFoundException(WdkService.formatNotFound(UserService.USER_RESOURCE + targetUserBundle.getTargetUserIdString()));
    }
  }

}
