package org.gusdb.wdk.service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.User.UserProfileProperty;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserProfileRequest;
import org.gusdb.wdk.session.OAuthUtil;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/user")
public class UserService extends WdkService {

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
  public Response setUserPrefs(@PathParam("id") String userIdStr, String body) throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    try {
      User user = userBundle.getUser();
      if(user.isGuest()) {
        throw new WdkUserException("A guest user cannot update any records");
      }
      JSONObject json = new JSONObject(body);
      UserProfileRequest request = UserProfileRequest.createFromJson(json);
      Map<UserProfileProperty, String> profileMap = request.getProfileMap();
      validateRequiredProfileProperties(profileMap, "PUT");
      validateNonDuplicateEmail(user, profileMap.get(UserProfileProperty.EMAIL));
      user.clearProfileProperties();
      for(UserProfileProperty key : profileMap.keySet()) {
        user.setProfileProperty(key, profileMap.get(key));
      }
      Map<String, String> propertiesMap = request.getApplicationSpecificPropertiesMap();
      // Only do a complete replace of profile properties if at least one such property is provided.
      if(!propertiesMap.isEmpty()) {
        user.clearGlobalPreferences();
        for(String key : propertiesMap.keySet()) {
          user.setGlobalPreference(key, propertiesMap.get(key));
        }
      }
      user.save();
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException | WdkUserException e) {
      return WdkService.getBadRequestBodyResponse(e.getMessage());
    }
  }
  
  /**
   * Web service to replace profile and profile properties of existing user with those provided in the
   * request.  If the properties object is present but not populated, the profile properties will be removed.
   * @param userIdStr
   * @param body
   * @return - 204
   * @throws WdkModelException
   */
  @PATCH
  @Path("{id}/profile")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserPrefs(@PathParam("id") String userIdStr, String body) throws WdkModelException {
    UserBundle userBundle = parseUserId(userIdStr);
    if (userBundle == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    if (!userBundle.isCurrentUser())
      return getPermissionDeniedResponse();
    try {
      User user = userBundle.getUser();
      if(user.isGuest()) {
        throw new WdkUserException("A guest user cannot update any records");
      }
      JSONObject json = new JSONObject(body);
      UserProfileRequest request = UserProfileRequest.createFromJson(json);
      Map<UserProfileProperty, String> profileMap = request.getProfileMap();
      validateRequiredProfileProperties(profileMap, "PATCH");
      validateNonDuplicateEmail(user, profileMap.get(UserProfileProperty.EMAIL));
      for(UserProfileProperty key : profileMap.keySet()) {
        user.setProfileProperty(key, profileMap.get(key));
      }
      Map<String, String> propertiesMap = request.getApplicationSpecificPropertiesMap();
      for(String key : propertiesMap.keySet()) {
        user.setGlobalPreference(key, propertiesMap.get(key));
      }
      user.save();
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException | WdkUserException e) {
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
   * Insures that the provided email address does not duplicate that of another account.
   * @param user - the subject of a put or patch
   * @param email - the provided email
   * @throws WdkModelException
   * @throws WdkUserException - thrown if the provided email address duplicates that of another account.
   */
  protected void validateNonDuplicateEmail(User user, String email) throws WdkModelException, WdkUserException {
    // Check to see if this email address matches that of the given user.  If so, no need to check further.
    if(email != null && !email.equalsIgnoreCase(user.getEmail())) {
      User emailUser = getWdkModel().getUserFactory().getUserByEmail(email);
      if (emailUser != null && emailUser.getUserId() != user.getUserId()) {
        throw new WdkUserException("This email is already in use by another account.  Please choose another.");
      }
    }
  }
}
