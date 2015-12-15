package org.gusdb.wdk.service.formatter;

import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class UserFormatter {

  public static JSONObject getUserJson(User user, boolean isOwner) throws JSONException, WdkModelException {
    JSONObject json = new JSONObject();
    json.put("id", user.getUserId());
    json.put("firstName", user.getFirstName());
    json.put("middleName", user.getMiddleName());
    json.put("lastName", user.getLastName());
    json.put("organization", user.getOrganization());
    // private fields viewable only by owner
    if (isOwner) {
      json.put("email", user.getEmail());
      json.put("properties", getUserPrefsJson(user.getGlobalPreferences()));
    }
    return json;
  }

  public static JSONObject getUserPrefsJson(Map<String,String> preferenceMap) {
    JSONObject prefs = new JSONObject();
    for (Entry<String,String> pref : preferenceMap.entrySet()) {
      prefs.put(pref.getKey(), pref.getValue());
    }
    return prefs;
  }

}
