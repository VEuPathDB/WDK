package org.gusdb.wdk.model.user.dataset;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONObject;

/**
 * Provides access to collections of user datasets.  Only provides information about
 * one user at a time.
 * @author steve
 *
 */
public interface UserDatasetStore {

  /**
   * Called at start up by the WDK.  The configuration comes from
   * properties in model XML.
   * @param configuration
   */
  void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers) throws WdkModelException;
  
  /**
   * For one user, provide a map from dataset ID to dataset.
   * @param userId
   * @return the map
   */
  Map<Integer, UserDataset>getUserDatasets(Integer userId) throws WdkModelException;
  
  /**
   * Get a user dataset.
   * @param userId
   * @param datasetId
   * @return
   * @throws WdkModelException
   */
  UserDataset getUserDataset(Integer userId, Integer datasetId) throws WdkModelException;
  
  /**
   * Get the users a dataset is shared with.
   * @param userId
   * @param datasetId
   * @return Set of userId
   * @throws WdkModelException
   */
  public Set<UserDatasetShare> getSharedWith(Integer userId, Integer datasetId) throws WdkModelException;

  
  /**
   * Get the external user datasets shared with this user.
   * @param userId
   * @return Map of datasetId -> dataset
   * @throws WdkModelException
   */
  Map<Integer, UserDataset> getExternalUserDatasets(Integer userId) throws WdkModelException;

  
  /**
   * Update user supplied meta data.  Client provides JSON to describe the new
   * meta info.  Implementors must ensure this update is atomic.
   * @param userId
   * @param datasetId
   * @param metaJson
   */
  void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson) throws WdkModelException;
  
  /**
   * Share the provided dataset with the recipient provided
   * @param ownerUserId - The id of the dataset owner
   * @param datasetId - The id of the dataset to share
   * @param recipientUserId - The user who will gain access to the dataset
   */
  void shareUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserIds) throws WdkModelException;
  
  /**
   * Share the provided dataset with the set of recipients provided
   * @param ownerUserId - The id of the dataset owner
   * @param datasetId - The id of the dataset to share
   * @param recipientUserIds - The users who will gain access to the dataset
   */
  void shareUserDataset(Integer ownerUserId, Integer datasetId, Set<Integer>recipientUserIds) throws WdkModelException;
  
  /**
   * Unshare the provided dataset with the set of users provided
   * @param ownerUserId - The id of the dataset owner
   * @param datasetId - The id of the dataset to unshare
   * @param recipeintUserIds - The users who will lose access to the dataset
   * @throws WdkModelException
   */
  void unshareUserDataset(Integer ownerUserId, Integer datasetId, Set<Integer> recipeintUserIds) throws WdkModelException;
  
  /**
   * Unshare this dataset with the specified user
   * @param ownerUserId The owner of the user dataset
   * @param datasetId The dataset to unshare
   * @param recipientUserId The user who will lose the sharing
   */
  void unshareUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException;
  
  /**
   * Unshare this dataset with all users it was shared with
   * @param ownerUserId - the dataset owner
   * @param datasetId - the dataset to unshare with everyone
   */
  void unshareWithAll(Integer ownerUserId, Integer datasetId) throws WdkModelException;
  
  /**
   * Delete the specified userDataset from the store.  Must unshare the dataset from 
   * other datasets that see it as an external dataset.
   * Implementors should ensure atomicity.
   * @param userDataset
   */
  void deleteUserDataset(Integer userId, Integer datasetId) throws WdkModelException;
  
  /**
   * Delete the specified external dataset.  Must unshare the dataset from 
   * @param userId
   * @param externalUserId
   * @param externalDatasetId
   */
  void deleteExternalUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException;
  
  /**
   * Return the type handler registered for the specified type.
   * @param type
   * @return null if not found.
   */
  UserDatasetTypeHandler getTypeHandler(UserDatasetType type);
  
  /**
   * For a particular user, the last modification time of any of their datasets.
   * Useful for quick cache checks.  We use Date as a platform neutral representation.
   * @param userId
   * @return
   */
  Long getModificationTime(Integer userId) throws WdkModelException;
  
  /**
   * Get the size of this user's quota
   * @param userId
   * @return
   */
  Integer getQuota(Integer userId) throws WdkModelException;
  
  /**
   * Check if a user has a userId directory in the store. 
   * @param userId
   * @return true if so.
   * @throws WdkModelException
   */
  boolean checkUserDirExists(Integer userId)  throws WdkModelException;

  /**
   * Check if a user has a datasets/ directory in the store. 
   * @param userId
   * @return true if so.
   * @throws WdkModelException
   */

  boolean checkUserDatasetsDirExists(Integer userId)  throws WdkModelException;
  
  UserDatasetFile getUserDatasetFile(Path path, Integer userDatasetId);

  boolean getUserDatasetExists(Integer userId, Integer datasetId) throws WdkModelException;

  /** not needed yet, and maybe never
  UserDataset getExternalUserDataset(Integer userId, Integer ownerUserId, Integer userDatasetId)
      throws WdkModelException;
  */
  
  UserDatasetStoreAdaptor getUserDatasetStoreAdaptor();
  
  String getUserDatasetStoreId();
  
}