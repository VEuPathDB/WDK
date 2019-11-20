package org.gusdb.wdk.service.request.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON object returned by either a PATCH REST request for
 * User preferences
 * @author crisl-adm
 *
 */
public class UserPreferencesRequest {

  private Map<String,String> _updates = new HashMap<>();
  private List<String> _deletes = new ArrayList<>();

  /**
   * 
   * @param json
   * @return
   * @throws RequestMisformatException
   */
  public static UserPreferencesRequest createFromJson(JSONObject json) throws RequestMisformatException {
    try {
      UserPreferencesRequest request = new UserPreferencesRequest();
      loadPreferenceChanges(json, request._updates, request._deletes);
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  private static void loadPreferenceChanges(JSONObject json,
      Map<String,String> updates, List<String> deletes) {

      for (String key : JsonUtil.getKeys(json)) {
        if (json.isNull(key)) {
          deletes.add(key);
        }
        else {
          updates.put(key, json.getString(key));
        }
      }
    
  }

  public Map<String,String> getPreferenceUpdates() {
    return _updates;
  }

  public List<String> getPreferenceDeletes() {
    return _deletes;
  }

}
