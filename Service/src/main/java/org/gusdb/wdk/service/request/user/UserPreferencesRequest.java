package org.gusdb.wdk.service.request.user;

import static org.gusdb.wdk.model.user.UserPreferenceFactory.MAX_PREFERENCE_KEY_SIZE;
import static org.gusdb.wdk.model.user.UserPreferenceFactory.MAX_PREFERENCE_VALUE_SIZE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.service.request.exception.DataValidationException;
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
   * @throws DataValidationException 
   */
  public static UserPreferencesRequest createFromJson(JSONObject json) throws RequestMisformatException, DataValidationException {
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
      Map<String,String> updates, List<String> deletes) throws DataValidationException {

      for (String key : JsonUtil.getKeys(json)) {
        if (json.isNull(key)) {
          deletes.add(key);
        }
        else {
          if (FormatUtil.getUtf8EncodedBytes(key).length > MAX_PREFERENCE_KEY_SIZE) {
            throw new DataValidationException("User preference key size cannot be greater than " + MAX_PREFERENCE_KEY_SIZE + " bytes.");
          }
          String pref = json.getString(key);
          if (FormatUtil.getUtf8EncodedBytes(pref).length > MAX_PREFERENCE_VALUE_SIZE) {
            throw new DataValidationException("User preference value size cannot be greater than " + MAX_PREFERENCE_VALUE_SIZE + " bytes.");
          }
          updates.put(key, pref);
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
