package org.gusdb.wdk.model.user.dataset;

import java.util.Date;
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
  void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers) throws WdkModelException;
  
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
   * Update user supplied meta data.  Client provides JSON to describe the new
   * meta info.  Implementors must ensure this update is atomic.
   * @param userId
   * @param datasetId
   * @param metaJson
   */
  void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson) throws WdkModelException;
  
  /**
   * Share one or more datasets from a host user to one or more recipient users.
   * @param hostUserId
   * @param datasetsIds
   * @param recipientUserIds
   */
  void shareUserDatasets(Integer ownerUserId, Set<Integer>datasetIds, Set<Integer>recipientUserIds) throws WdkModelException;

  
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
   * Check if a dataset is compatible with this application, based on its data dependencies.
   * @param userDataset
   * @return
   */
  UserDatasetCompatibility getCompatibility(UserDataset userDataset) throws WdkModelException;
  
  /**
   * For a particular user, the last modification time of any of their datasets.
   * Useful for quick cache checks.  We use Date as a platform neutral representation.
   * @param userId
   * @return
   */
  Date getModificationTime(Integer userId) throws WdkModelException;
  
  /**
   * Get the size of this user's quota
   * @param userId
   * @return
   */
  Integer getQuota(Integer userId) throws WdkModelException;
  
}