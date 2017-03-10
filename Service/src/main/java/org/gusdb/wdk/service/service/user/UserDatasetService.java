package org.gusdb.wdk.service.service.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetInfo;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserDatasetFormatter;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserDatasetShareRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *  TODO: validate
 *    - requested user exists
 *    - sharing:
 *      - target user exists
 *      - shared dataset exists
 *    - external datasets
 *      - original dataset exists, and is still shared  
 *
 */
public class UserDatasetService extends UserService {

  private static Logger LOG = Logger.getLogger(UserDatasetService.class);

  // TODO: this should be changed to PRIVATE after initial development testing
  private static final Access DATA_ACCESS = Access.PUBLIC;
  
  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("user-dataset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllUserDatasets(@QueryParam("expandDetails") Boolean expandDatasets) throws WdkModelException {
    expandDatasets = getFlag(expandDatasets, false);
    User user = getUserBundle(DATA_ACCESS).getTargetUser();
    UserFactory userFactory = getWdkModel().getUserFactory();
    UserDatasetStore dsStore = getUserDatasetStore();
    Set<Integer> installedUserDatasets = getWdkModel().getUserDatasetFactory().getInstalledUserDatasets(getUserId());
    List<UserDatasetInfo> userDatasets = getDatasetInfo(dsStore.getUserDatasets(user.getUserId()).values(),
        installedUserDatasets, dsStore, userFactory);
    List<UserDatasetInfo> sharedDatasets = getDatasetInfo(dsStore.getExternalUserDatasets(user.getUserId()).values(),
        installedUserDatasets, dsStore, userFactory);
    return Response.ok(UserDatasetFormatter.getUserDatasetsJson(
        userDatasets, sharedDatasets, expandDatasets).toString()).build();
  }

  private static List<UserDatasetInfo> getDatasetInfo(final Collection<UserDataset> datasets,
      final Set<Integer> installedUserDatasets, final UserDatasetStore dsStore, final UserFactory userFactory) {
    return Functions.mapToList(datasets, new Function<UserDataset,UserDatasetInfo>() {
      @Override public UserDatasetInfo apply(UserDataset dataset) {
        return new UserDatasetInfo(dataset, installedUserDatasets.contains(dataset.getUserDatasetId()), dsStore, userFactory);
      }});
  }

  @GET
  @Path("user-dataset/{datasetId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDataset(@PathParam("datasetId") String datasetIdStr) throws WdkModelException {
    int datasetId = parseIntegerId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    UserDatasetStore userDatasetStore = getUserDatasetStore();
    UserDataset userDataset =
        userDatasetStore.getUserDatasetExists(getUserId(), datasetId) ?
        userDatasetStore.getUserDataset(getUserId(), datasetId) :
        userDatasetStore.getExternalUserDatasets(getUserId()).get(datasetId);
    if (userDataset == null) {
      throw new NotFoundException("user-dataset/" + datasetIdStr);
    }
    Set<Integer> installedUserDatasets = getWdkModel().getUserDatasetFactory().getInstalledUserDatasets(getUserId());
    return Response.ok(UserDatasetFormatter.getUserDatasetJson(
        new UserDatasetInfo(userDataset, installedUserDatasets.contains(datasetId), userDatasetStore,
            getWdkModel().getUserFactory()), userDataset.getOwnerId().equals(getUserId())).toString()).build();
  }

  @PUT
  @Path("user-dataset/{datasetId}/meta")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetaInfo(@PathParam("datasetId") String datasetIdStr, String body) throws WdkModelException {
    int datasetId = parseIntegerId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    if (!getUserDatasetStore().getUserDatasetExists(getUserId(), datasetId)) throw new NotFoundException("user-dataset/" + datasetIdStr);
    try {
      JSONObject metaJson = new JSONObject(body);
      getUserDatasetStore().updateMetaFromJson(getUserId(), datasetId, metaJson);
      return Response.noContent().build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }
  
  /*
   * This service allows a WDK user to share/unshare owned datasets with
   * other WDK users.  The JSON object accepted by the service should have the following form:
   *    {
   *	  "add": {
   *	    "dataset_id1": [ "user1", "user2" ]
   *	    "dataset_id2": [ "user1" ]
   *	  },
   *	  "delete" {
   *	    "dataset_id3": [ "user1", "user3" ]
   *	  }
   *	}
   */	
  @PATCH
  @Path("user-dataset/share")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response manageShares(String body) throws WdkModelException {
    JSONObject jsonObj = new JSONObject(body);
    try {
      UserDatasetShareRequest request = UserDatasetShareRequest.createFromJson(jsonObj);
      Map<String, Map<Integer, Set<Integer>>> userDatasetShareMap = request.getUserDatasetShareMap();
      Set<Integer> installedDatasetIds = getWdkModel().getUserDatasetFactory().getInstalledUserDatasets(getUserId());
      for(String key : userDatasetShareMap.keySet()) {
        // Find datasets to share  
        if("add".equals(key)) {
          Set<Integer> targetDatasetIds = userDatasetShareMap.get(key).keySet();
          // Ignore any provided dataset ids not owned by this user.
          targetDatasetIds.retainAll(installedDatasetIds);
          for(Integer targetDatasetId : targetDatasetIds) {
            Set<Integer> targetUserIds = identifyTargetUsers(userDatasetShareMap.get(key).get(targetDatasetId));  
            // Since each dataset may be shared with different users, we share datasets one by one
            getUserDatasetStore().shareUserDataset(getUserId(), targetDatasetId, targetUserIds);
          }
        }
        // Fine datasets to unshare
        if("delete".equals(key)) {
          Set<Integer> targetDatasetIds = userDatasetShareMap.get(key).keySet();
          // Ignore any provided dataset ids not owned by this user.
          targetDatasetIds.retainAll(installedDatasetIds);
          for(Integer targetDatasetId : targetDatasetIds) {
            Set<Integer> targetUserIds = identifyTargetUsers(userDatasetShareMap.get(key).get(targetDatasetId));  
            // Since each dataset may unshared with different users, we unshare datasets one by one.
            getUserDatasetStore().unshareUserDataset(getUserId(), targetDatasetId, targetUserIds);
          }
        }
      }
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Convenience method to whittle out any non-valid target users
   * @param providedUserIds - set of user ids provided to the service
   * @return - subset of those provided user ids that belong to valid users.
   * @throws WdkModelException
   */
  protected Set<Integer> identifyTargetUsers(Set<Integer> providedUserIds) throws WdkModelException {
    Set<Integer> targetUserIds = new HashSet<>();
    for(Integer providedUserId : providedUserIds) {
      if(validateTargetUserId(providedUserId)) {
        targetUserIds.add(providedUserId);
      }
    }
    return targetUserIds;
  }

  @DELETE
  @Path("user-dataset/{datasetId}")
  public Response deleteById(@PathParam("datasetId") String datasetIdStr) throws WdkModelException {
    int datasetId = parseIntegerId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    getUserDatasetStore().deleteUserDataset(getUserId(), datasetId);
    return Response.noContent().build();
  }

  private UserDatasetStore getUserDatasetStore() throws WdkModelException {
    UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
    if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
    return userDatasetStore;
  }

  /* not used yet.
  private UserDataset getUserDatasetObj(String datasetIdStr) throws WdkModelException {
    try {
      Integer datasetId = new Integer(datasetIdStr);
      UserBundle userBundle = getUserBundle(Access.PUBLIC); // TODO: temporary, for debugging
      return getUserDatasetStore().getUserDataset(userBundle.getTargetUser().getUserId(), datasetId);
    }
    catch (NumberFormatException e) {
      throw new BadRequestException(e);
    }   
  }
  */

  private int parseIntegerId(String idStr, RuntimeException exception) {
    if (FormatUtil.isInteger(idStr)) {
      return Integer.parseInt(idStr);
    }
    throw exception;
  }

  // TODO: if user is a guest, throw a 403 or similar
  private Integer getUserId() throws WdkModelException {
    return getUserBundle(DATA_ACCESS).getTargetUser().getUserId();
  }

  /**
   * Determines whether the target user is valid.  Any invalid user is noted in the logs.  Seems extreme to trash the whole operation
   * over one wayward user id.
   * @param targetUserId - id of target user to check for validity
   * @return - true is target user is valid and false otherwise.
   * @throws WdkModelException
   */
  private boolean validateTargetUserId(Integer targetUserId) throws WdkModelException {
    UserBundle targetUserBundle = UserBundle.createFromTargetId(targetUserId.toString(), getSessionUser(), getWdkModel().getUserFactory(), isSessionUserAdmin());
    if (!targetUserBundle.isValidUserId()) {
      //throw new NotFoundException(WdkService.formatNotFound(UserService.USER_RESOURCE + targetUserBundle.getTargetUserIdString()));
      LOG.warn("This user dataset share service request contains the following invalid user: " + targetUserId);
      return false;	
    }
    return true;
  }
}
