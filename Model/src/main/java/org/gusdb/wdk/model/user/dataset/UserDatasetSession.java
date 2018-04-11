package org.gusdb.wdk.model.user.dataset;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONObject;

public interface UserDatasetSession extends AutoCloseable {

  /**
   * For one user, provide a map from dataset ID to dataset.
   * @param userId
   * @return the map
   */
  Map<Long, UserDataset> getUserDatasets(Long userId) throws WdkModelException;

  /**
   * Get a user dataset.
   * @param userId
   * @param datasetId
   * @return
   * @throws WdkModelException
   */
  UserDataset getUserDataset(Long userId, Long datasetId) throws WdkModelException;

  /**
   * Get the users a dataset is shared with.
   * @param userId
   * @param datasetId
   * @return Set of userId
   * @throws WdkModelException
   */
  public Set<UserDatasetShare> getSharedWith(Long userId, Long datasetId) throws WdkModelException;

  /**
   * Get the external user datasets shared with this user.
   * @param userId
   * @return Map of datasetId -> dataset
   * @throws WdkModelException
   */
  Map<Long, UserDataset> getExternalUserDatasets(Long userId) throws WdkModelException;

  /**
   * Update user supplied meta data.  Client provides JSON to describe the new
   * meta info.  Implementors must ensure this update is atomic.
   * @param userId
   * @param datasetId
   * @param metaJson
   */
  void updateMetaFromJson(Long userId, Long datasetId, JSONObject metaJson) throws WdkModelException;

  /**
   * Share the provided dataset with the recipient provided
   * @param ownerUserId - The id of the dataset owner
   * @param datasetId - The id of the dataset to share
   * @param recipientUserId - The user who will gain access to the dataset
   */
  void shareUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException;

  /**
   * Share the provided dataset with the set of recipients provided
   * @param ownerUserId - The id of the dataset owner
   * @param datasetId - The id of the dataset to share
   * @param recipientUserIds - The users who will gain access to the dataset
   */
  void shareUserDataset(Long ownerUserId, Long datasetId, Set<Long>recipientUserIds) throws WdkModelException;

  /**
   * Unshare the provided dataset with the set of users provided
   * @param ownerUserId - The id of the dataset owner
   * @param datasetId - The id of the dataset to unshare
   * @param recipeintUserIds - The users who will lose access to the dataset
   * @throws WdkModelException
   */
  void unshareUserDataset(Long ownerUserId, Long datasetId, Set<Long> recipeintUserIds) throws WdkModelException;

  /**
   * Unshare this dataset with the specified user
   * @param ownerUserId The owner of the user dataset
   * @param datasetId The dataset to unshare
   * @param recipientUserId The user who will lose the sharing
   */
  void unshareUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException;

  /**
   * Unshare this dataset with all users it was shared with - may be done with a prelude
   * to deleting it.
   * @param ownerUserId - the dataset owner
   * @param datasetId - the dataset to unshare with everyone
   */
  void unshareWithAll(Long ownerUserId, Long datasetId) throws WdkModelException;

  /**
   * In the case where the user provided is the owner of the dataset provided, delete that
   * userDataset from the store.  Must unshare the dataset from other datasets that see it
   * as an external dataset.  In the case where the user provided is a share recipient of the
   * dataset provided, remove the share only.
   * Implementors should ensure atomicity.
   * @param userDataset
   */
  void deleteUserDataset(Long userId, Long datasetId) throws WdkModelException;

  /**
   * Delete the specified external dataset.  Must unshare the dataset from 
   * @param userId
   * @param externalUserId
   * @param externalDatasetId
   */
  void deleteExternalUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException;

  /**
   * For a particular user, the last modification time of any of their datasets.
   * Useful for quick cache checks.  We use Date as a platform neutral representation.
   * @param userId
   * @return null if no user datasets
   */
  Long getModificationTime(Long userId) throws WdkModelException;

  /**
   * Get the size of this user's quota
   * @param userId
   * @return
   */
  Long getQuota(Long userId) throws WdkModelException;
  
  /**
   * Gets the size of the default quota.  Option to always grab from the store so that
   * this could be used in a service to check health of store.
   * @param getFromStore
   * @return
   * @throws WdkModelException
   */
  Long getDefaultQuota(boolean getFromStore) throws WdkModelException;

  /**
   * Check if a user has a userId directory in the store. 
   * @param userId
   * @return true if so.
   * @throws WdkModelException
   */
  boolean checkUserDirExists(Long userId)  throws WdkModelException;

  /**
   * Check if a user has a datasets/ directory in the store. 
   * @param userId
   * @return true if so.
   * @throws WdkModelException
   */
  boolean checkUserDatasetsDirExists(Long userId)  throws WdkModelException;

  UserDatasetFile getUserDatasetFile(Path path, Long userDatasetId);

  boolean getUserDatasetExists(Long userId, Long datasetId) throws WdkModelException;

  /** not needed yet, and maybe never
  UserDataset getExternalUserDataset(Long userId, Long ownerUserId, Long userDatasetId)
      throws WdkModelException;
  */
  UserDatasetStoreAdaptor getUserDatasetStoreAdaptor();

  /**
   * The user dataset store id is initially set and kept so that
   * whenever a user dataset store event occurs, the dataset store id
   * provided by the event trigger can be compared with the kept dataset
   * store id to insure that the two match and that the triggers event
   * data may be accepted.
   * @return
   * @throws WdkModelException
   */
  String initializeUserDatasetStoreId() throws WdkModelException;
  
  /**
   * This method grabs a subset of event paths from the events folder.  The subset is based upon
   * the lastHandledEventId provided.
   * @param eventDirectory - string representing the absolute path to the events directory
   * @param lastHandledEventId - id of the last handled event (a timestamp in msec)
   * @return - list of absolute paths for each event within the subset
   * @throws WdkModelException
   */
  List<Path> getRecentEvents(String eventDirectory, Long lastHandledEventId) throws WdkModelException;

  /**
   * IRODS Sessions need to be closed to recover connections.  So
   * this is a necessary evil.
   */
  @Override
  void close();
}
