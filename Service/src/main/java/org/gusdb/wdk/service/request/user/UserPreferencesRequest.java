package org.gusdb.wdk.service.request.user;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.service.request.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON object returned by either a PUT or PATCH REST request for
 * User preferences
 * @author crisl-adm
 *
 */
public class UserPreferencesRequest {
  
  private Map<String,String> _preferencesMap;

  public Map<String, String> getPreferencesMap() {
    return _preferencesMap;
  }

  public void setPreferencesMap(Map<String, String> preferencesMap) {
    _preferencesMap = preferencesMap;
  }
  
  /**
   * * Input Format:
   * 
   * {
   *  String key: String,
   *  String key: String,
   *  ...
   * }
   * 
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static UserPreferencesRequest createFromJson(JSONObject json) throws RequestMisformatException {
    try {
      UserPreferencesRequest request = new UserPreferencesRequest();
      request.setPreferencesMap(parsePreferences(json));
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }
  
  /**
   * Provides a map of the preferences JSON objects.  Since some may
   * be dynamically determined, no attempt is made to cull the list
   * @param json - Object containing preferences
   * @return - map of key | value pairs - no filtering
   * @throws JSONException 
   */
  protected static Map<String,String> parsePreferences(JSONObject json) throws JSONException {
    Map<String, String> map = new HashMap<>();
    for(Object key : json.keySet()) {
      map.put((String) key, json.getString((String) key).trim());
    }
    return map;
  }

}
