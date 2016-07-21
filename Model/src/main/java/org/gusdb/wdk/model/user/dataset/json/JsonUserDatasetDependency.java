package org.gusdb.wdk.model.user.dataset.json;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUserDatasetDependency implements UserDatasetDependency {

  private static final String RESOURCE_IDENTIFIER = "resourceIdentifier";
  private static final String RESOURCE_VERSION = "resourceVersion";
  private static final String RESOURCE_DISPLAY_NAME = "resourceDisplayName";
  
  private String resourceIdentifier;
  private String resourceVersion;
  private String resourceDisplayName;
  
  /**
   * Construct from jsonObject, eg, when info is provided from larger json file
   * @param jsonObject
   * @throws WdkModelException
   */
  public JsonUserDatasetDependency(JSONObject jsonObject) throws WdkModelException {
    try {
      this.resourceIdentifier = jsonObject.getString(RESOURCE_IDENTIFIER);
      this.resourceVersion = jsonObject.getString(RESOURCE_VERSION);
      this.resourceDisplayName = jsonObject.getString(RESOURCE_DISPLAY_NAME);
    } catch (JSONException e) {
      throw new WdkModelException(e);
    }
  }
  
  @Override
  public String getResourceIdentifier() {
    return resourceIdentifier;
  }

  @Override
  public String getResourceVersion() {
    return resourceVersion;
  }

  @Override
  public String getResourceDisplayName() {
    return resourceDisplayName;
  }
 
}
