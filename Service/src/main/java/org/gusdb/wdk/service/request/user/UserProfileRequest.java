package org.gusdb.wdk.service.request.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.user.User.UserProfileProperty;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON object returned by either a PUT or PATCH REST request for
 * a User profile.
 * @author crisl-adm
 *
 */
public class UserProfileRequest {
  
  private final static String APPLICATION_SPECIFIC_PROPERTIES = "applicationSpecificProperties";
  
  private Map<UserProfileProperty,String> _profileMap;
  private Map<String,String> _applicationSpecificPropertiesMap;

  public Map<UserProfileProperty,String> getProfileMap() {
    return _profileMap;
  }

  public void setProfileMap(Map<UserProfileProperty,String> profileMap) {
    _profileMap = profileMap;
  }

  public Map<String,String> getApplicationSpecificPropertiesMap() {
    return _applicationSpecificPropertiesMap;
  }

  public void setApplicationSpecificPropertiesMap(Map<String,String> applicationSpecificPropertiesMap) {
    _applicationSpecificPropertiesMap = applicationSpecificPropertiesMap;
  }
  
  /**
   * * Input Format:
   * 
   * {
   *  String key: String,
   *  String key: String,
   *  ...
   *  applicationSpecificProperties: { key: String, key; String ... }
   * }
   * 
   * The 'properties' key is optional
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static UserProfileRequest createFromJson(JSONObject json) throws RequestMisformatException {
    try {
      UserProfileRequest request = new UserProfileRequest();
      request.setProfileMap(parseProfile(json));
      JSONObject properties = json.keySet().contains(APPLICATION_SPECIFIC_PROPERTIES)
          ? (JSONObject) json.get(APPLICATION_SPECIFIC_PROPERTIES) : new JSONObject();
      request.setApplicationSpecificPropertiesMap(parseProperties(properties));
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }
  
  /**
   * Provides a map of the profile related JSON objects.  Only retrieves those
   * key/value pairs that belong in a user profile.
   * @param json
   * @return - map of user profile property enum | value pairs
   * @throws JSONException - caught & re-thrown so as to allow exception chaining so we can grab the root cause
   * detail message.
   */
  protected static Map<UserProfileProperty,String> parseProfile(JSONObject json) throws JSONException {
    List<String> profileNames = UserProfileProperty.JSON_PROPERTY_NAMES;
    Map<UserProfileProperty,String> map = new HashMap<>();
    //try {
      for(Object key : json.keySet()) {
        if(profileNames.contains(key)) {
          map.put(UserProfileProperty.fromJsonPropertyName((String) key), json.getString((String) key));
        }
      }
      return map;
    //}
    //catch(JSONException e) {
    //  throw new JSONException(e);
    //}
  }
  
  /**
   * Provides a map of the application specific property JSON objects.  Since some may
   * be dynamically determined, no attempt is made to cull the list
   * @param json - Object containing application specific properties
   * @return - map of key | value pairs - no filtering
   * @throws JSONException - caught & re-thrown so as to allow exception chaining so we can grab the root cause
   * detail message.
   */
  protected static Map<String,String> parseProperties(JSONObject json) throws JSONException {
    Map<String, String> map = new HashMap<>();
    try {
      for(Object key : json.keySet()) {
        map.put((String) key, json.getString((String) key));
      }
      return map;
    }
    catch(JSONException e) {
      throw new JSONException(e);
    }
  }

}
