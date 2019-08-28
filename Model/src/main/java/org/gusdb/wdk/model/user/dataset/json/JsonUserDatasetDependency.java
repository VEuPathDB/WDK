package org.gusdb.wdk.model.user.dataset.json;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUserDatasetDependency extends UserDatasetDependency {

  private static final String RESOURCE_IDENTIFIER = "resourceIdentifier";
  private static final String RESOURCE_VERSION = "resourceVersion";
  private static final String RESOURCE_DISPLAY_NAME = "resourceDisplayName";

  /**
   * Construct from jsonObject, eg, when info is provided from larger json file
   */
  public JsonUserDatasetDependency(JSONObject jsonObject) throws WdkModelException {
    super();
    try {
      setResourceIdentifier(jsonObject.getString(RESOURCE_IDENTIFIER));
      setResourceVersion(jsonObject.getString(RESOURCE_VERSION));
      setResourceDisplayName(jsonObject.getString(RESOURCE_DISPLAY_NAME));
    } catch (JSONException e) {
      throw new WdkModelException(e);
    }
  }

}
