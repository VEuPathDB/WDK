package org.gusdb.wdk.controller.action.services;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.LoginCookieFactory;
import org.gusdb.wdk.controller.LoginCookieFactory.LoginCookieParts;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Web service action that takes parts of a WDK login cookie and verifies
 * that they indeed represent a valid cookie for an existing WDK user.
 * Returns user's display name and email address for use by caller if valid.
 * 
 * A JSON object like the following is returned with the information.  If the
 * cookie is invalid, the isValid property is set to false and the userData
 * property is undefined.
 * 
 * {
 *   "isValid": true,
 *   "userData": {
 *     "displayName" : "Ryan Doherty",
 *     "email" : "rdoherty@pcbi.upenn.edu"
 *   }
 * }
 * 
 * @author rdoherty
 */
public class LoginCookieValidationService extends WdkAction {

  private static final Logger LOG = Logger.getLogger(LoginCookieValidationService.class);
  
  // input param constants
  private static final String COOKIE_KEY = "wdkLoginCookieValue";

  // json output constants
  private static final String IS_VALID_KEY = "isValid";
  private static final String USER_DATA_KEY = "userData";
  private static final String DISPLAY_NAME_KEY = "displayName";
  private static final String EMAIL_KEY = "email";
  
  @Override protected boolean shouldValidateParams() { return false; }
  @Override protected Map<String, ParamDef> getParamDefs() { return EMPTY_PARAMS; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    try {
      return new ActionResult(ResponseType.json)
        .setStream(getStreamFromString(
            getJsonResult(getUsername(params))));
    }
    catch (Exception e) { LOG.error(e); throw e; }
  }
  
  /**
   * Fetches the cookie value parameter, verifies its validity, and returns
   * the username (email) contained within the cookie value.  If the cookie is
   * invalid, null is returned
   * 
   * @param params request parameters
   * @return username, or an empty string
   * @throws WdkModelException if a system problem occurs
   */
  private String getUsername(ParamGroup params) throws WdkModelException {
    try {
      String recreatedCookie = params.getValueOrEmpty(COOKIE_KEY);
      LoginCookieParts cookieParts = LoginCookieFactory.parseCookieValue(recreatedCookie);
      LoginCookieFactory auth = new LoginCookieFactory(getWdkModel().getSecretKey());
      return (auth.isValidCookie(cookieParts) ? cookieParts.getUsername() : null);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Unable to parse cookie value param.", e);
      return null;
    }
  }
  
  /**
   * Generates the appropriate JSON object given the username parsed from the
   * cookie value (if any).  Even if a non-null username was retrieved, the
   * cookie value may still be deemed invalid if the username does not
   * correspond to an existing user.
   * 
   * @param username username to generate JSON for (or null if no username
   * was able to be parsed)
   * @return response JSON
   * @throws WdkModelException if a system problem occurs
   */
  private String getJsonResult(String username) throws WdkModelException {
    try {
      JSONObject result = new JSONObject();
      boolean isValid = (username != null);
      if (isValid) {
        try {
          // cookie seems valid; try to get user's name and email
          UserBean user = getWdkModel().getUserFactory().getUserByEmail(username);
          JSONObject userData = new JSONObject();
          userData.put(DISPLAY_NAME_KEY, user.getFirstName() + " " + user.getLastName());
          userData.put(EMAIL_KEY, user.getEmail());
          result.put(USER_DATA_KEY, userData);
          isValid = true;
        }
        catch (WdkUserException e) {
          // user does not exist; cookie is invalid
          isValid = false;
        }
      }
      result.put(IS_VALID_KEY, isValid);
      return result.toString();
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to generate JSON object from data.", e);
    }
  }
}

