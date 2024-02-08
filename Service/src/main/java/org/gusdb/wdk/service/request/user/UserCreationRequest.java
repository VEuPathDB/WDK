package org.gusdb.wdk.service.request.user;

import java.util.Collection;
import java.util.List;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class UserCreationRequest {

  private final UserProfileRequest _profileRequest;
  private final UserPreferencesRequest _globalPreferencesRequest;
  private final UserPreferencesRequest _projectPreferencesRequest;

  public static UserCreationRequest createFromJson(JSONObject requestJson, Collection<UserProperty> configuredProps)
      throws RequestMisformatException, DataValidationException {
    try {
      JSONObject userRequest = requestJson.getJSONObject(JsonKeys.USER);
      JSONObject preferenceRequest = requestJson.getJSONObject(JsonKeys.PREFERENCES);
      return new UserCreationRequest(
          UserProfileRequest.createFromJson(userRequest, configuredProps, true),
          UserPreferencesRequest.createFromJson(preferenceRequest.getJSONObject(JsonKeys.GLOBAL)),
          UserPreferencesRequest.createFromJson(preferenceRequest.getJSONObject(JsonKeys.PROJECT)));
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  public UserCreationRequest(UserProfileRequest profileRequest, UserPreferencesRequest globalPreferencesRequest, UserPreferencesRequest projectPreferencesRequest) {
    _profileRequest = profileRequest;
    _globalPreferencesRequest = globalPreferencesRequest;
    _projectPreferencesRequest = projectPreferencesRequest;

  }

  public UserProfileRequest getProfileRequest() {
    return _profileRequest;
  }

  public UserPreferencesRequest getGlobalPreferencesRequest() {
    return _globalPreferencesRequest;
  }
  public UserPreferencesRequest getProjectPreferencesRequest() {
    return _projectPreferencesRequest;
  }
}
