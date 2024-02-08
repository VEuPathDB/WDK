package org.gusdb.wdk.service.formatter;

import java.util.Optional;

import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.wdk.core.api.JsonKeys;
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
      Optional<UserPreferences> userPreferences) throws JSONException {
    JSONObject json = new JSONObject()
      .put(JsonKeys.ID, user.getUserId())
      .put(JsonKeys.IS_GUEST, user.isGuest());
    // private fields viewable only by owner
    if (isOwner) {
      json.put(JsonKeys.EMAIL, user.getEmail());
      json.put(JsonKeys.PROPERTIES, getPropertiesJson(user, isOwner));
      userPreferences.ifPresent(prefs ->
        json.put(JsonKeys.PREFERENCES, getPreferencesJson(prefs)));
    }
    return json;
  }

  private static JSONObject getPropertiesJson(User user, boolean isOwner) {
    JSONObject propsJson = new JSONObject();
    for (UserProperty definedProperty : User.getPropertyDefs()) {
      if (isOwner || definedProperty.isPublic()) {
        String key = definedProperty.getName();
        String value = Optional.ofNullable(definedProperty.getValue(user)).orElse("");
        propsJson.put(key, value);
      }
    }
    return propsJson;
  }

  public static JSONObject getPreferencesJson(UserPreferences prefs) {
    JSONObject prefsJson = new JSONObject();
    prefsJson.put(JsonKeys.GLOBAL, new JSONObject(prefs.getGlobalPreferences()));
    prefsJson.put(JsonKeys.PROJECT, new JSONObject(prefs.getProjectPreferences()));
    return prefsJson;
  }

}
