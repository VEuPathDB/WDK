package org.gusdb.wdk.service.request.user;

import java.util.Map;

import org.gusdb.fgputil.json.JsonUtil;
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
      request.setPreferencesMap(JsonUtil.parseProperties(json));
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

}
