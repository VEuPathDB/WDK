package org.gusdb.wdk.model.user.dataset;

import org.json.JSONObject;

/**
 * Describes compatibility of a dataset with an application.
 *
 * @author steve
 */
public class UserDatasetCompatibility {
  private boolean isCompatible;
  private String notCompatibleReason;
  private JSONObject _compatibilityInfoJson;

  public UserDatasetCompatibility(boolean isCompatible, String notCompatibleReason) {
    this(isCompatible, new JSONObject(), notCompatibleReason);
  }

  public UserDatasetCompatibility(boolean isCompatible, JSONObject compatibilityInfoJson, String notCompatibleReason) {
    this.isCompatible = isCompatible;
    this.notCompatibleReason = notCompatibleReason;
    _compatibilityInfoJson = compatibilityInfoJson;
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

  /**
   * Information that may be useful to the client.
   * @return
   */
  public JSONObject getCompatibilityInfoJson() { return _compatibilityInfoJson; }
}
