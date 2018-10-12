package org.gusdb.wdk.service.request.user;

import java.util.List;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class UserCreationRequest {

  private final UserProfileRequest _profileRequest;
  private final UserPreferencesRequest _preferencesRequest;

  public static UserCreationRequest createFromJson(JSONObject requestJson, List<UserPropertyName> configuredProps)
      throws RequestMisformatException, DataValidationException {
    try {
      JSONObject userRequest = requestJson.getJSONObject(JsonKeys.USER);
      JSONObject preferenceRequest = requestJson.getJSONObject(JsonKeys.PREFERENCES);
      return new UserCreationRequest(
          UserProfileRequest.createFromJson(userRequest, configuredProps, true),
          UserPreferencesRequest.createFromJson(preferenceRequest));
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  public UserCreationRequest(UserProfileRequest profileRequest, UserPreferencesRequest preferencesRequest) {
    _profileRequest = profileRequest;
    _preferencesRequest = preferencesRequest;
  }

  public UserProfileRequest getProfileRequest() {
    return _profileRequest;
  }

  public UserPreferencesRequest getPreferencesRequest() {
    return _preferencesRequest;
  }
}
