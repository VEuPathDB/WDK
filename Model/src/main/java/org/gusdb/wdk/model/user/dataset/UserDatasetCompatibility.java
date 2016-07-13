package org.gusdb.wdk.model.user.dataset;

/**
 * Describes compatibility of a dataset with an application.
 * @author steve
 *
 */
public interface UserDatasetCompatibility {
  /**
   * If this dataset is compatible
   * @return
   */
  boolean isCompatbile();
  
  /**
   * If not compatible, the reason why not
   * @return
   */
  String notCompatibleReason();
}
