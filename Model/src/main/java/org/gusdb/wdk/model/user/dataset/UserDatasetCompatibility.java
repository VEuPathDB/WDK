package org.gusdb.wdk.model.user.dataset;

/**
 * Describes compatibility of a dataset with an application.
 * @author steve
 *
 */
public class UserDatasetCompatibility {
  private boolean isCompatible;
  private String notCompatibleReason;
  
  public UserDatasetCompatibility(boolean isCompatible, String notCompatibleReason) {
    this.isCompatible = isCompatible;
    this.notCompatibleReason = notCompatibleReason;
  }
  /**
   * If this dataset is compatible
   * @return
   */
  boolean isCompatible() { return isCompatible; }
  
  /**
   * If not compatible, the reason why not
   * @return
   */
  String notCompatibleReason() { return notCompatibleReason; }
}
