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
  public boolean isCompatible() { return isCompatible; }
  
  /**
   * If not compatible, the reason why not
   * @return
   */
  public String notCompatibleReason() { return notCompatibleReason; }
}
