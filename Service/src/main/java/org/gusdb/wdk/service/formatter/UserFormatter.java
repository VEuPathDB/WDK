package org.gusdb.wdk.service.formatter;

import java.util.Map;
import java.util.Map.Entry;

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

  public static JSONObject getUserJson(User user, boolean isOwner, boolean includePreferences) throws JSONException {
    JSONObject json = new JSONObject()
      .put(Keys.ID, user.getUserId())
      .put(Keys.IS_GUEST, user.isGuest());
    // private fields viewable only by owner
    if (isOwner) {
      json.put(Keys.EMAIL, user.getEmail());
      json.put(Keys.PROPERTIES, getPropertiesJson(user.getProfileProperties()));
      if (includePreferences) {
        json.put(Keys.PREFERENCES, getPreferencesJson(user.getPreferences()));
      }
    }
    return json;
  }

  private static JSONObject getPropertiesJson(Map<String,String> props) {
    JSONObject propsJson = new JSONObject();
    for (Entry<String,String> prop : props.entrySet()) {
      propsJson.put(prop.getKey(), prop.getValue());
    }
    return propsJson;
  }

  public static JSONObject getPreferencesJson(UserPreferences prefs) {
    JSONObject prefsJson = new JSONObject();
    prefsJson.put(Keys.GLOBAL, JsonUtil.toJsonObject(prefs.getGlobalPreferences()));
    prefsJson.put(Keys.PROJECT, JsonUtil.toJsonObject(prefs.getProjectPreferences()));
    return prefsJson;
  }

}
