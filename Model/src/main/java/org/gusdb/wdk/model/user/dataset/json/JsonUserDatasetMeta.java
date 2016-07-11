package org.gusdb.wdk.model.user.dataset.json;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A data container for user-editable meta data.  Constructed from    
 * @author steve
 *
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
   * @param jsonObject
   * @throws WdkModelException
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
   * Construct from json string, eg, when info is provided client, when user edits this info
   * (perhaps we don't need this, can let the service do it)
   * @param jsonString
   * @throws WdkModelException
   */
  public JsonUserDatasetMeta(String jsonString) throws WdkModelException {
    this(new JSONObject(jsonString));
  }

  /**
   * the serialized version.  for now, spit out the string acquired in construction.
   * maybe later pretty print json from state.
   */
  public JSONObject getJsonObject() {
    return jsonObject;
  }

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
