package org.gusdb.wdk.model.user.dataset.json;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A data container for user-editable meta data.  Constructed from
 *
 * @author steve
 */
public class JsonUserDatasetMeta implements UserDatasetMeta {

  private static final String NAME = "name";
  private static final String SUMMARY = "summary";
  private static final String DESCRIPTION = "description";

  private String name;
  private String summary;
  private String description;
  private JSONObject jsonObject;

  /**
   * Construct from jsonObject, eg, when info is provided from larger json file
   */
  public JsonUserDatasetMeta(JSONObject jsonObject) throws WdkModelException {
    this.jsonObject = jsonObject;
    try {
      this.name = jsonObject.getString(NAME);
      this.summary = jsonObject.getString(SUMMARY);
      this.description = jsonObject.getString(DESCRIPTION);
    } catch (JSONException e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * the underlying json object
   */
  public JSONObject getJsonObject() {
    return jsonObject;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getName() {
    return name;
  }
}
