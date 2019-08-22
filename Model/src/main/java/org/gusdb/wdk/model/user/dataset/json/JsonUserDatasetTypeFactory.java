package org.gusdb.wdk.model.user.dataset.json;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUserDatasetTypeFactory {

  public static final String NAME = "name";
  public static final String VERSION = "version";

  public static synchronized UserDatasetType getUserDatasetType(JSONObject jsonObject) throws WdkModelException {
    try {
      return UserDatasetTypeFactory.getUserDatasetType(jsonObject.getString(NAME), jsonObject.getString(VERSION));
    } catch (JSONException e) {
      throw new WdkModelException(e);
    }
  }

  public static JSONObject toJsonObject(UserDatasetType type) {
    JSONObject json = new JSONObject();
    json.put(NAME, type.getName());
    json.put(VERSION, type.getVersion());
    return json;
  }
}
