package org.gusdb.wdk.service.request;

import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginRequest {
	
  private String email;
  private String password;
  private String redirectUrl;	

  /**
   * Input Format:
   * {
   *  "email": String,
   *  "password": String,
   *  "redirectUrl": String, String
   * }
   * 
   * @param json request json in the format above
   * @return object representing request
   * @throws RequestMisformatException
   */
  public static LoginRequest createFromJson(JSONObject json) throws RequestMisformatException {
    LoginRequest loginRequest = new LoginRequest();  
    try {
      loginRequest.email = json.getString("email");
      loginRequest.password = json.getString("password");
      loginRequest.redirectUrl = json.getString("redirectUrl");
      return loginRequest;
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

}
