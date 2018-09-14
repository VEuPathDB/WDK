package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferences;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Formats WDK User profile and preference data.  User profile JSON will have
 * the following form:
 * 
 * {
 *   id: Integer,
 *   isGuest: Boolean
 *   email: String (private),
 *   properties: Object (private)
 * }
 * 
 * User preferences are formatted as a JSON Object representing a map from
 * preference key (String) to preference value (String)
 * 
 * @author rdoherty
 */
public class UserFormatter {

  public static JSONObject getUserJson(User user, boolean isOwner,
      boolean includePreferences, List<UserPropertyName> propDefs) throws JSONException {
    JSONObject json = new JSONObject()
      .put(JsonKeys.ID, user.getUserId())
      .put(JsonKeys.IS_GUEST, user.isGuest());
    // private fields viewable only by owner
    if (isOwner) {
      json.put(JsonKeys.EMAIL, user.getEmail());
      json.put(JsonKeys.PROPERTIES, getPropertiesJson(user.getProfileProperties(), propDefs, isOwner));
      if (includePreferences) {
        json.put(JsonKeys.PREFERENCES, getPreferencesJson(user.getPreferences()));
      }
    }
    return json;
  }

  private static JSONObject getPropertiesJson(Map<String,String> props, List<UserPropertyName> propDefs, boolean isOwner) {
    JSONObject propsJson = new JSONObject();
    for (UserPropertyName definedProperty : propDefs) {
      if (isOwner || definedProperty.isPublic()) {
        String key = definedProperty.getName();
        String value = (props.containsKey(key) ? props.get(key) : "");
        propsJson.put(key, value);
      }
    }
    return propsJson;
  }

  public static JSONObject getPreferencesJson(UserPreferences prefs) {
    JSONObject prefsJson = new JSONObject();
    prefsJson.put(JsonKeys.GLOBAL, JsonUtil.toJsonObject(prefs.getGlobalPreferences()));
    prefsJson.put(JsonKeys.PROJECT, JsonUtil.toJsonObject(prefs.getProjectPreferences()));
    return prefsJson;
  }

}
