package org.gusdb.wdk.model.user.dataset;

import java.util.Date;
import java.util.Map;

/**
 * Provides access to collections of user datasets.
 * @author steve
 *
 */
public interface UserDatasetStore {
  /**
   * For one user, provide a map from dataset ID to dataset.
   * @param userId
   * @return the map
   */
  Map<Integer, UserDataset>getUserDatasets(Integer userId);
  
  /**
   * For a particular user, the last modification time of any of their datasets.
   * Useful for quick cache checks.
   * @param userId
   * @return
   */
  Date getModificationTime(Integer userId);
  
  /**
   * Get the size of this user's quota
   * @param userId
   * @return
   */
  Integer getQuota(Integer userId);
}
