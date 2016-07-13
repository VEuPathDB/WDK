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
   * For a particular user, the last modification time of any of their datasets.
   * Useful for quick cache checks.
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
  
  /**
   * User can update the meta info a dataset the own.
   * Client applications provide JSON to specify an update to meta info.  
   * The implementor must use that to construct a UserDatasetMeta object.
   * @param metaJson
   * @return
   */
  void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson);
}
