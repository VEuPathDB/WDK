package org.gusdb.wdk.service.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.User.UserProfileProperty;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserPreferencesRequest;
import org.gusdb.wdk.service.request.user.UserProfileRequest;
import org.gusdb.wdk.session.OAuthUtil;
import org.json.JSONException;
import org.json.JSONObject;


@Path("/user")
public class UserService extends WdkService {
  
  private static final String LOGIN_COOKIE_NAME = "wdk_check_auth";
  private static final int PREFERENCE_NAME_MAX_LENGTH = 200;
  private static final int PREFERENCE_VALUE_MAX_LENGTH = 4000;

  // ===== OAuth 2.0 + OpenID Connect Support =====
  /**
   * Create anti-forgery state token, add to session, and return.  This is
   * requested by the client when the user tries to log in using an OAuth2
   * server.  The value generated will be used to check the state token passed
   * to the oauth processing action.  They must match for the login to succeed.
   * We generate a new value each time; all but one of "overlapping" login
   * attempts by the same user will thus fail.
   * 
   * @return OAuth 2.0 state token
   * @throws WdkModelException if unable to read WDK secret key from file
   */
  @GET
  @Path("oauthStateToken")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getOauthStateToken() throws WdkModelException {
    String newToken = OAuthUtil.generateStateToken(getWdkModel());
    getSession().setAttribute(OAuthUtil.STATE_TOKEN_KEY, newToken);
    return Response.ok(newToken).build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(
      @PathParam("id") String userIdStr,
      @QueryParam("includePreferences") Boolean includePreferences)
          throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    return Response.ok(
        UserFormatter.getUserJson(userBundle.getUser(),
            userBundle.isCurrentUser(), getFlag(includePreferences)).toString()
    ).build();
  }

  @GET
  @Path("{id}/preference")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserPrefs(@PathParam("id") String userIdStr) {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    return Response.ok(UserFormatter.getUserPrefsJson(
        userBundle.getUser().getProjectPreferences()).toString()).build();
  }
  
  /**
   * Web service to replace profile and profile properties of existing user with those provided in the
   * request.  Existing profile information is removed and re-populated with the contents of the request.
   * If the properties object is present but not populated, the profile properties will be removed.
   * @param userIdStr
   * @param body
   * @return - 204
   * @throws WdkModelException
   */
  @PUT
  @Path("{id}/profile")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setUserProfile(@PathParam("id") String userIdStr, String body, @CookieParam(LOGIN_COOKIE_NAME) Cookie cookie) throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    NewCookie loginCookie = null;
    try {
      User user = userBundle.getUser();
      if(user.isGuest()) {
        throw new WdkUserException("A guest user cannot update any records");
      }
      JSONObject json = new JSONObject(body);
      UserProfileRequest request = UserProfileRequest.createFromJson(json);
      Map<UserProfileProperty, String> profileMap = request.getProfileMap();
      validateRequiredProfileProperties(profileMap, "PUT");
      validateProfilePropertySizes(profileMap);
      loginCookie = processEmail(user, profileMap.get(UserProfileProperty.EMAIL));
      user.clearProfileProperties();
      for(UserProfileProperty key : profileMap.keySet()) {
        user.setProfileProperty(key, profileMap.get(key));
      }
      Map<String, String> propertiesMap = request.getApplicationSpecificPropertiesMap();
      // Only do a complete replace of profile properties if at least one such property is provided.
      if(!propertiesMap.isEmpty()) {
        validatePreferenceSizes(propertiesMap);
        user.clearGlobalPreferences();
        for(String key : propertiesMap.keySet()) {
          user.setGlobalPreference(key, propertiesMap.get(key));
        }
      }
      user.save();
      return loginCookie == null ? Response.noContent().build() : Response.noContent().cookie(loginCookie).build();
    }
    catch(JSONException | RequestMisformatException | WdkUserException e) {
      return WdkService.getBadRequestBodyResponse(e.getMessage());
    }
  }
  
  /**
   * Web service to replace profile and profile properties of existing user with those provided in the
   * request.  If the properties object is present but not populated, the profile properties will be removed.
   * @param userIdStr - id or 'current'
   * @param body
   * @return - 204
   * @throws WdkModelException
   */
  @PATCH
  @Path("{id}/profile")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserProfile(@PathParam("id") String userIdStr, String body, @CookieParam(LOGIN_COOKIE_NAME) Cookie cookie) throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    NewCookie loginCookie = null;
    try {
      User user = userBundle.getUser();
      if(user.isGuest()) {
        throw new WdkUserException("A guest user cannot update any records");
      }
      JSONObject json = new JSONObject(body);
      UserProfileRequest request = UserProfileRequest.createFromJson(json);
      Map<UserProfileProperty, String> profileMap = request.getProfileMap();
      validateRequiredProfileProperties(profileMap, "PATCH");
      validateProfilePropertySizes(profileMap);
      loginCookie = processEmail(user, profileMap.get(UserProfileProperty.EMAIL));
      for(UserProfileProperty key : profileMap.keySet()) {
        user.setProfileProperty(key, profileMap.get(key));
      }
      Map<String, String> propertiesMap = request.getApplicationSpecificPropertiesMap();
      validatePreferenceSizes(propertiesMap);
      for(String key : propertiesMap.keySet()) {
        user.setGlobalPreference(key, propertiesMap.get(key));
      }
      user.save();
      return loginCookie == null ? Response.noContent().build() : Response.noContent().cookie(loginCookie).build();
    }
    catch(JSONException | RequestMisformatException | WdkUserException e) {
      return WdkService.getBadRequestBodyResponse(e.getMessage());
    }
  }
  
  /**
   * Web service to replace project preferences of existing user with those provided in the
   * request.  Existing project preferences are removed and re-populated with the contents of the request.
   * @param userIdStr
   * @param body
   * @return
   * @throws WdkModelException
   */
  @PUT
  @Path("{id}/preference")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putUserPrefs(@PathParam("id") String userIdStr, String body) throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    try {
      User user = userBundle.getUser();
      JSONObject json = new JSONObject(body);
      UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json);
      Map<String, String> preferencesMap = request.getPreferencesMap();
      validatePreferenceSizes(preferencesMap);
      user.clearProjectPreferences();
      for(String key : preferencesMap.keySet()) {
        user.setProjectPreference(key, preferencesMap.get(key));
      }
      user.save();
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException | WdkUserException  e) {
      return WdkService.getBadRequestBodyResponse(e.getMessage());
    }
  }

  /**
   * Web service to replace preferences of existing user with those provided in the request.  Unchanged
   * preferences remain unchanged.
   * @param userIdStr
   * @param body
   * @return
   * @throws WdkModelException
   */
  @PATCH
  @Path("{id}/preference")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response patchUserPrefs(@PathParam("id") String userIdStr, String body) throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    try {
      User user = userBundle.getUser();
      JSONObject json = new JSONObject(body);
      UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json);
      Map<String, String> preferencesMap = request.getPreferencesMap();
      validatePreferenceSizes(preferencesMap);
      for(String key : preferencesMap.keySet()) {
        user.setProjectPreference(key, preferencesMap.get(key));
      }
      user.save();
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException | WdkUserException  e) {
      return WdkService.getBadRequestBodyResponse(e.getMessage());
    }
  }
  
  
  /**
   * Validates whether are required properties are in place and non-empty.  PUT must
   * have all required properties.  PATCH only requires that any required properties
   * specified be non-empty.
   * @param profileMap - map of values of user profile properties to values
   * @throws WdkUserException - thrown if one or more user profile properties is required to be present and/or non-empty.
   */
  protected void validateRequiredProfileProperties(Map<UserProfileProperty, String> profileMap, String operation) throws WdkUserException {
    List<UserProfileProperty> requiredProperties = UserProfileProperty.REQUIRED_PROPERTIES;
    List<String> missingProperties = new ArrayList<>();
    for(UserProfileProperty property : requiredProperties) {
      String propertyValue = profileMap.get(property);
      // Missing required properties are OK for PATCH since those aren't altered.
      if(("PUT".equalsIgnoreCase(operation) && propertyValue == null) || (propertyValue != null && propertyValue.trim().isEmpty())) {
        missingProperties.add(property.getJsonPropertyName());
      }
    }
    if(!missingProperties.isEmpty()) {
      String missing = FormatUtil.join(missingProperties.toArray(), ",");
      throw new WdkUserException("The following properties must be present and non-empty: " + missing);
    }
  }
  
  /**
   * Validates whether all provided profile property values have string lengths at or below the maximum limit 
   * @param profileMap - map of values of user profile properties to values.  Note that this map was already scrubbed of
   * unrecognized profile properties so only the value lengths must be validated and not the property names.
   * @throws WdkUserException - thrown if one or more user profile properties exceeds its maximum allowed length.
   */
  protected void validateProfilePropertySizes(Map<UserProfileProperty, String> profileMap) throws WdkUserException {
    List<String> lengthyPropertyValues = new ArrayList<>();
    for(UserProfileProperty property : profileMap.keySet()) {
      if(property.getMaxLength() < profileMap.get(property).length()) {
        lengthyPropertyValues.add(property.getJsonPropertyName() + ":" + profileMap.get(property) + " (" + property.getMaxLength() + " max )");
      }
    }
    if(!lengthyPropertyValues.isEmpty()) {
      String lengthy = FormatUtil.join(lengthyPropertyValues.toArray(), ",");
      throw new WdkUserException("The following property values exceed their maximum allowed lengths " + lengthy);
    }
  }
  
  protected void validatePreferenceSizes(Map<String, String> propertiesMap) throws WdkUserException {
    List<String> lengthyData = new ArrayList<>();
    for(String property : propertiesMap.keySet()) {
      if(property.length() > PREFERENCE_NAME_MAX_LENGTH || propertiesMap.get(property).length() > PREFERENCE_VALUE_MAX_LENGTH) {
        lengthyData.add(property + " : " + propertiesMap.get(property));
      }
    }
    if(!lengthyData.isEmpty()) {
      String lengthy = FormatUtil.join(lengthyData.toArray(), ",");
      throw new WdkUserException("The following property names and/or values exceed their maximum allowed legnths of " +
          PREFERENCE_NAME_MAX_LENGTH + " / " + PREFERENCE_VALUE_MAX_LENGTH + ": " + lengthy);
    }
  }
  
  /**
   * Insures that the provided email address, if provided, does not duplicate that of another account.  If
   * the new email passes validation, a new cookie is returned to be used for authentication following the
   * email address update.
   * @param user - the subject of a put or patch
   * @param email - the provided email
   * @throws WdkModelException
   * @throws WdkUserException - thrown if the provided email address duplicates that of another account.
   */
  protected NewCookie processEmail(User user, String email) throws WdkModelException, WdkUserException {
    // Check to see if this email address matches that of the given user.  If so, no need to check further.
    if(email != null && !email.equalsIgnoreCase(user.getEmail())) {
      User emailUser = getWdkModel().getUserFactory().getUserByEmail(email);
      // Check if the new email address is on record with a different user
      if (emailUser != null && emailUser.getUserId() != user.getUserId()) {
        throw new WdkUserException("This email is already in use by another account.  Please choose another.");
      }
      return setUpdatedCookie(user, email);
    }
    return null;
  }
  
  /**
   * Using the javax-rs cookie objects to create a new cookie associated with an email address change.  This
   * may be a temporary solution to the problem of email address changes invalidating the authentication cookie.
   * @param user - the user whose email is altered
   * @param email - the new email
   * @return - an updated authentication cookie or null if the original cookie is null
   * @throws WdkModelException - thrown in the new cookie value cannot be encoded.
   */
  private NewCookie setUpdatedCookie(User user, String email) throws WdkModelException {
    String uncodedCookieValue = email + "-" + UserFactoryBean.md5(email + getWdkModel().getSecretKey());
    String codedCookieValue;
    try {
       codedCookieValue = URLEncoder.encode(uncodedCookieValue, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new WdkModelException("Unable to encode login cookie value: " + uncodedCookieValue, e);
    }
    return new NewCookie(LOGIN_COOKIE_NAME, codedCookieValue, "/", null, null, -1, false);
  }
}
