package org.gusdb.wdk.service.service.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
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
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserDatasetFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserDatasetShareRequest;
import org.gusdb.wdk.service.request.user.UserProfileRequest;
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONArray;
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

  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("user-dataset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDatasets(@QueryParam("expandDetails") Boolean expandDatasets) throws WdkModelException {
    if (expandDatasets == null) expandDatasets = false;
    UserDatasetStore userDatasetStore = getUserDatasetStore();
    Map<Integer, UserDataset> userDatasets = getUserDatasetStore().getUserDatasets(getUserId());
    Map<Integer, UserDataset> externalUserDatasets = getUserDatasetStore().getExternalUserDatasets(getUserId());
    String userSchema = getWdkModel().getModelConfig().getUserDB().getUserSchema();
    Set<Integer> installedUserDatasets = getInstalledUserDatasets(getUserId(), getWdkModel().getAppDb().getDataSource(), getUserDatasetSchemaName());
    return Response.ok(UserDatasetFormatter.getUserDatasetsJson(userDatasets, externalUserDatasets, userDatasetStore, installedUserDatasets, getWdkModel().getUserDb().getDataSource(), userSchema, expandDatasets).toString()).build();
  }

  @GET
  @Path("user-dataset/{datasetId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDataset(@PathParam("datasetId") String datasetIdStr) throws WdkModelException {
    UserDatasetStore userDatasetStore = getUserDatasetStore();
    Integer datasetId = new Integer(datasetIdStr);
    UserDataset userDataset;
    if (getUserDatasetStore().getUserDatasetExists(getUserId(), datasetId)) 
      userDataset = getUserDatasetStore().getUserDataset(getUserId(), datasetId);
    else
      userDataset = userDatasetStore.getExternalUserDatasets(getUserId()).get(datasetId);
    if (userDataset == null) throw new NotFoundException("user-dataset/" + datasetIdStr);
    String userSchema = getWdkModel().getModelConfig().getUserDB().getUserSchema();
    Set<Integer> installedUserDatasets = getInstalledUserDatasets(getUserId(), getWdkModel().getAppDb().getDataSource(), getUserDatasetSchemaName());
    return Response.ok(UserDatasetFormatter.getUserDatasetJson(userDataset, userDatasetStore, installedUserDatasets, getWdkModel().getUserDb().getDataSource(), userSchema, false).toString()).build();
  }

  @PUT
  @Path("user-dataset/{datasetId}/meta")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetaInfo(@PathParam("datasetId") String datasetIdStr, String body) throws WdkModelException {
    Integer datasetId = new Integer(datasetIdStr);
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
	  Set<Integer> installedDatasetIds = getInstalledUserDatasets(getUserId(), getWdkModel().getAppDb().getDataSource(), getUserDatasetSchemaName());
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

  /**
   * {
   *   "targetUsers" : [12401223],
   *   "datasetsToShare" : [555,777]
   * }
   */
  @PUT
  @Path("user-dataset/{datasetId}/share")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response shareWith(@PathParam("datasetId") String datasetIdStr, String body) throws WdkModelException {

    JSONObject jsonObj = new JSONObject(body);
    
    // put datasets to share into a set
    JSONArray jsonDatasetsToShare = jsonObj.getJSONArray("datasetsToShare");
        Set<Integer> datasetIdsToShare = new HashSet<Integer>();
    for (int i=0; i<jsonDatasetsToShare.length(); i++) 
      datasetIdsToShare.add(jsonDatasetsToShare.getInt(i));
    
    // put target users into a set
    JSONArray jsonTargetUsers = jsonObj.getJSONArray("targetUsers");
    Set<Integer> targetUserIds = new HashSet<>();
    for (int i=0; i<jsonTargetUsers.length(); i++) {
      Integer targetUserId = jsonTargetUsers.getInt(i);
      if(validateTargetUserId(targetUserId)) {
        targetUserIds.add(targetUserId);
      }  
    }
    getUserDatasetStore().shareUserDatasets(getUserId(), datasetIdsToShare, targetUserIds);

    return Response.noContent().build();

  }
  
  @PUT
  @Path("user-dataset/{datasetId}/unshare")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response unshareWith(@PathParam("datasetId") String datasetIdStr, String body)
      throws WdkModelException, DataValidationException {

    JSONObject jsonObj = new JSONObject(body);

    Integer shareWithUserId = new Integer(jsonObj.getString("targetUser"));
    getUserDatasetStore().unshareUserDataset(getUserId(), new Integer(datasetIdStr), shareWithUserId);

    return Response.noContent().build();
  }
  
  @DELETE
  @Path("user-dataset/{datasetId}")
  public Response deleteById(@PathParam("datasetId") int datasetId) throws WdkModelException {
    getUserDatasetStore().deleteUserDataset(getUserId(), datasetId);
    return Response.noContent().build();
  }
  
  private UserDatasetStore getUserDatasetStore() throws WdkModelException {
    UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
    if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
    return userDatasetStore;
  }
  
  // TODO: get this from config
  private String getUserDatasetSchemaName() throws WdkModelException {
    return "ApiDBUserDatasets.";
  }

  /* not used yet.
  private UserDataset getUserDatasetObj(String datasetIdStr) throws WdkModelException {
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
  */

  private Integer getUserId() throws WdkModelException {
    return getUserBundle(Access.PUBLIC).getTargetUser().getUserId();
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
  
  // this probably doesn't belong here, but it is not obvious where it does belong
  /**
   * Return the dataset IDs the provided user can see in the provided app db, ie, that are installed and has access to
   * @param userId
   * @param userDatasetSchema
   * @param appDbDataSource
   * @return
   * @throws WdkModelException
   */
  private static Set<Integer> getInstalledUserDatasets(Integer userId, DataSource appDbDataSource, String userDatasetSchema) throws WdkModelException {
    
    final Set<Integer> datasetIds = new HashSet<>();
    ResultSetHandler handler = new ResultSetHandler() {
      @Override
      public void handleResult(ResultSet rs) throws SQLException {
        while (rs.next()) datasetIds.add(rs.getInt(1)); 
      }
    };

    String sql = "select user_dataset_id from " + userDatasetSchema + "userDatasetAccessControl where user_id = ?";
    SQLRunner runner = new SQLRunner(appDbDataSource, sql, "installed-datasets-ud-svc");
    Object[] args = {userId};
    runner.executeQuery(args, handler);
    return datasetIds;
  }


}
