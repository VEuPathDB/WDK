package org.gusdb.wdk.model.user.dataset;

import java.util.Date;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

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
  void initialize(Map<String, String> configuration) throws WdkModelException;
  
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
}
