package org.gusdb.wdk.service.request.user;

import static org.gusdb.fgputil.FormatUtil.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON object returned by either a PUT or PATCH REST request for
 * User preferences
 * @author crisl-adm
 *
 */
public class UserPreferencesRequest {

  private static final List<String> VALID_SCOPES = Arrays.asList(JsonKeys.GLOBAL, JsonKeys.PROJECT);

  private Map<String,String> _globalPrefs = new HashMap<>();
  private List<String> _globalPrefsToDelete = new ArrayList<>();
  private Map<String,String> _projectPrefs = new HashMap<>();
  private List<String> _projectPrefsToDelete = new ArrayList<>();

  /**
   * Input Format:
   * {
   *   global:  { String key: String },
   *   project: { String key: String }
   * }
   * 
   * @param json
   * @return
   * @throws RequestMisformatException
   */
  public static UserPreferencesRequest createFromJson(JSONObject json) throws RequestMisformatException {
    try {
      UserPreferencesRequest request = new UserPreferencesRequest();
      validateRequestJson(json);
      loadPreferenceChanges(json, JsonKeys.GLOBAL, request._globalPrefs, request._globalPrefsToDelete);
      loadPreferenceChanges(json, JsonKeys.PROJECT, request._projectPrefs, request._projectPrefsToDelete);
      return request;
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  private static void validateRequestJson(JSONObject json) throws RequestMisformatException {
    for (String key : JsonUtil.getKeys(json)) {
      if (!VALID_SCOPES.contains(key)) {
        throw new RequestMisformatException("Preference service request JSON can contain " +
            "only the following properties: [ '" + join(VALID_SCOPES, "', '") + "'].");
      }
    }
  }

  private static void loadPreferenceChanges(JSONObject parent, String objectKey,
      Map<String,String> prefChanges, List<String> prefDeletes) {
    if (parent.has(objectKey)) {
      JSONObject prefs = parent.getJSONObject(objectKey);
      for (String key : JsonUtil.getKeys(prefs)) {
        if (prefs.isNull(key)) {
          prefDeletes.add(key);
        }
        else {
          prefChanges.put(key, prefs.getString(key));
        }
      }
    }
  }

  public Map<String,String> getGlobalPreferenceMods() {
    return _globalPrefs;
  }

  public List<String> getGlobalPreferenceDeletes() {
    return _globalPrefsToDelete;
  }

  public Map<String,String> getProjectPreferenceMods() {
    return _projectPrefs;
  }

  public List<String> getProjectPreferenceDeletes() {
    return _projectPrefsToDelete;
  }
}
