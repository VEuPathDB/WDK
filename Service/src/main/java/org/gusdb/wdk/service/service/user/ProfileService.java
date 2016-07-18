package org.gusdb.wdk.service.service.user;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.User.UserProfileProperty;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.UserPreferenceValidator;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.ConflictException;
import org.gusdb.wdk.service.request.DataValidationException;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.user.PasswordChangeRequest;
import org.gusdb.wdk.service.request.user.UserProfileRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileService extends UserService {

  private static final String LOGIN_COOKIE_NAME = "wdk_check_auth";
  private static final String NOT_LOGGED_IN = "The user is not logged in.";
  private static final String DUPLICATE_EMAIL = "This email is already in use by another account.  Please choose another.";
  private static final String PROFILE_VALUES_TOO_LONG = "The following profile values exceed their maximum allowed lengths ";
  private static final String REQUIRED_VALUES = "The following items must be present and non-empty: ";
  private static final String COOKIE_ENCODING_PROBLEM = "Unable to encode login cookie value: ";

  public ProfileService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(
      @QueryParam("includePreferences") Boolean includePreferences)
          throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    return Response.ok(
        UserFormatter.getUserJson(userBundle.getUser(),
            userBundle.isCurrentUser(), getFlag(includePreferences)).toString()
    ).build();
  }

  /**
   * Web service to replace profile and profile properties of existing user with those provided in the
   * request.  Existing profile information is removed and re-populated with the contents of the request.
   * If the properties object is present but not populated, the profile properties will be removed.
   * @param body
   * @return - 204 - Success without content
   * @throws WdkModelException
   */
  @PUT
  @Path("profile")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setUserProfile(String body)
      throws ConflictException, DataValidationException, WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    NewCookie loginCookie = null;
    try {
      User user = userBundle.getUser();
      if(user.isGuest()) {
        throw new ForbiddenException(NOT_LOGGED_IN);
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
        UserPreferenceValidator.validatePreferenceSizes(propertiesMap);
        user.clearGlobalPreferences();
        for(String key : propertiesMap.keySet()) {
          user.setGlobalPreference(key, propertiesMap.get(key));
        }
      }
      user.save();
      return loginCookie == null ? Response.noContent().build() : Response.noContent().cookie(loginCookie).build();
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Web service to replace profile and profile properties of existing user with those provided in the
   * request.  If the properties object is present but not populated, the profile properties will be removed.
   * @param userIdStr - id or 'current'
   * @param body
   * @return - 204 - Success without content
   * @throws DataValidationException - in the event of a
   * @throws WdkModelException - in the event of a server error
   */
  @PATCH
  @Path("profile")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserProfile(String body) 
      throws ConflictException, DataValidationException, WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    NewCookie loginCookie = null;
    try {
      User user = userBundle.getUser();
      if(user.isGuest()) {
        throw new ForbiddenException(NOT_LOGGED_IN);
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
      UserPreferenceValidator.validatePreferenceSizes(propertiesMap);
      for(String key : propertiesMap.keySet()) {
        user.setGlobalPreference(key, propertiesMap.get(key));
      }
      user.save();
      return loginCookie == null ? Response.noContent().build() : Response.noContent().cookie(loginCookie).build();
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  @PUT
  @Path("password")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response resetUserPassword(String body)
      throws WdkModelException, DataValidationException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    try {
      User user = userBundle.getUser();
      JSONObject json = new JSONObject(body);
      PasswordChangeRequest request = PasswordChangeRequest.createFromJson(json);
      UserFactory userMgr = getWdkModel().getUserFactory();
      if (userMgr.isCorrectPassword(user.getEmail(), request.getOldPassword())) {
        userMgr.savePassword(user.getEmail(), request.getNewPassword());
        return Response.noContent().build();
      }
      else {
        // user submitted wrong old password, permission denied
        throw new ForbiddenException("Old password specified is not correct.");
      }
    }
    catch (JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Validates whether are required properties are in place and non-empty.  PUT must
   * have all required properties.  PATCH only requires that any required properties
   * specified be non-empty.
   * @param profileMap - map of values of user profile properties to values
   * @throws RequestMisformatException - thrown if one or more user profile properties is required to be present and/or non-empty.
   */
  protected void validateRequiredProfileProperties(Map<UserProfileProperty, String> profileMap, String operation) throws RequestMisformatException {
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
      throw new RequestMisformatException(REQUIRED_VALUES + missing);
    }
  }

  /**
   * Validates whether all provided profile property values have string lengths at or below the maximum limit 
   * @param profileMap - map of values of user profile properties to values.  Note that this map was already scrubbed of
   * unrecognized profile properties so only the value lengths must be validated and not the property names.
   * @throws DataValidationException - thrown if one or more user profile properties exceeds its maximum allowed length.
   */
  protected void validateProfilePropertySizes(Map<UserProfileProperty, String> profileMap) throws DataValidationException {
    List<String> lengthyPropertyValues = new ArrayList<>();
    for(UserProfileProperty property : profileMap.keySet()) {
      if(property.getMaxLength() < profileMap.get(property).length()) {
        lengthyPropertyValues.add(property.getJsonPropertyName() + ":" + profileMap.get(property) + " (" + property.getMaxLength() + " max )");
      }
    }
    if(!lengthyPropertyValues.isEmpty()) {
      String lengthy = FormatUtil.join(lengthyPropertyValues.toArray(), ",");
      throw new DataValidationException(PROFILE_VALUES_TOO_LONG + lengthy);
    }
  }

  /**
   * Insures that the provided email address, if provided, does not duplicate that of another account.  If
   * the new email passes validation, a new cookie is returned to be used for authentication following the
   * email address update.
   * @param user - the subject of a put or patch
   * @param email - the provided email
   * @throws WdkModelException
   * @throws ConflictException - thrown if the provided email address duplicates that of another account.
   */
  protected NewCookie processEmail(User user, String email) throws ConflictException, WdkModelException {
    // Check to see if this email address matches that of the given user.  If so, no need to check further.
    if(email != null && !email.equalsIgnoreCase(user.getEmail())) {
      User emailUser = getWdkModel().getUserFactory().getUserByEmail(email);
      // Check if the new email address is on record with a different user
      if (emailUser != null && emailUser.getUserId() != user.getUserId()) {
        throw new ConflictException(DUPLICATE_EMAIL);
      }
      return getUpdatedCookie(user, email);
    }
    return null;
  }

  /**
   * Using the javax-rs cookie objects to create a new cookie associated with an email address change.  This
   * may be a temporary solution to the problem of email address changes invalidating the authentication cookie.
   * 
   * TODO: See LoginCookieFactory for legacy solution; should probably integrate the two together
   * 
   * @param user - the user whose email is altered
   * @param email - the new email
   * @return - an updated authentication cookie or null if the original cookie is null
   * @throws WdkModelException - thrown in the new cookie value cannot be encoded.
   */
  private NewCookie getUpdatedCookie(User user, String email) throws WdkModelException {
    String uncodedCookieValue = email + "-" + UserFactoryBean.md5(email + getWdkModel().getSecretKey());
    String codedCookieValue;
    try {
       codedCookieValue = URLEncoder.encode(uncodedCookieValue, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new WdkModelException(COOKIE_ENCODING_PROBLEM + uncodedCookieValue, e);
    }
    return new NewCookie(LOGIN_COOKIE_NAME, codedCookieValue, "/", null, null, -1, false);
  }
}
