package org.gusdb.wdk.service.request.user;

import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class PasswordChangeRequest {

  public static PasswordChangeRequest createFromJson(JSONObject json)
      throws RequestMisformatException, DataValidationException {
    try {
      String oldPassword = json.getString("oldPassword");
      String newPassword = json.getString("newPassword");
      if (newPassword.isEmpty()) {
        throw new DataValidationException("New password cannot be empty.");
      }
      return new PasswordChangeRequest(oldPassword, newPassword);
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  private final String _oldPassword;
  private final String _newPassword;
  
  public PasswordChangeRequest(String oldPassword, String newPassword) {
    _oldPassword = oldPassword;
    _newPassword = newPassword;
  }
  
  public String getOldPassword() {
    return _oldPassword;
  }

  public String getNewPassword() {
    return _newPassword;
  }

}
