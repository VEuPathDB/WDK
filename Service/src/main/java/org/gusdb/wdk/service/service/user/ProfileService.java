package org.gusdb.wdk.service.service.user;

import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
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

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.CookieConverter;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.PasswordChangeRequest;
import org.gusdb.wdk.service.request.user.UserProfileRequest;
import org.gusdb.wdk.session.LoginCookieFactory;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/users/{id}")
public class ProfileService extends UserService {

  private static final String DUPLICATE_EMAIL = "This email is already in use by another account.  Please choose another.";

  public ProfileService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  // FIXME Uncomment when overrides can be handled
  // @OutSchema("wdk.users.get-by-id")
  public JSONObject getById(@QueryParam("includePreferences") Boolean includePreferences) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    List<UserPropertyName> propDefs = getWdkModel().getModelConfig()
        .getAccountDB().getUserPropertyNames();
    return formatUser(userBundle.getTargetUser(), userBundle.isSessionUser(),
        getFlag(includePreferences), propDefs);
  }

  /**
   * Formats user object to JSON.  Factored to allow easy appending of custom properties by subclasses
   *
   * @param user user to format
   * @param isSessionUser whether the requested user is the currently logged in user
   * @param includePrefs whether to include user preferences in response
   * @param propNames user property names configured in Model Config
   * @return formatted user object
   * @throws WdkModelException if something goes wrong
   */
  protected JSONObject formatUser(User user, boolean isSessionUser, boolean includePrefs, List<UserPropertyName> propNames) throws WdkModelException {
    return UserFormatter.getUserJson(user, isSessionUser, includePrefs, propNames);
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
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setUserProfile(String body)
      throws ConflictException, DataValidationException, WdkModelException {
    try {
      User user = getPrivateRegisteredUser();
      UserProfileRequest request = UserProfileRequest.createFromJson(
          new JSONObject(body), getPropertiesConfig(), true);
      NewCookie loginCookie = processEmail(user, request.getEmail());
      // overwrite user's old email and profile
      user.setEmail(request.getEmail());
      user.setProfileProperties(request.getProfileMap());
      getWdkModel().getUserFactory().saveUser(user);
      return getProfileUpdateResponse(loginCookie);
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Web service to replace profile and profile properties of existing user with those provided in the
   * request.  If the properties object is present but not populated, the profile properties will be removed.
   * @param body
   * @return - 204 - Success without content
   * @throws DataValidationException - in the event of a
   * @throws WdkModelException - in the event of a server error
   */
  @PATCH
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserProfile(String body)
      throws ConflictException, DataValidationException, WdkModelException {
    try {
      User user = getPrivateRegisteredUser();
      UserProfileRequest request = UserProfileRequest.createFromJson(
          new JSONObject(body), getPropertiesConfig(), false);
      NewCookie loginCookie = processEmail(user, request.getEmail());
      // overwrite any provided properties
      if (request.getEmail() != null) {
        user.setEmail(request.getEmail());
      }
      for (Entry<String,String> newProp : request.getProfileMap().entrySet()) {
        user.setProfileProperty(newProp.getKey(), newProp.getValue());
      }
      getWdkModel().getUserFactory().saveUser(user);
      return getProfileUpdateResponse(loginCookie);
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  private Response getProfileUpdateResponse(NewCookie loginCookie) {
    return loginCookie == null ?
        Response.noContent().build() :
        Response.noContent().cookie(loginCookie).build();
  }

  private List<UserPropertyName> getPropertiesConfig() {
    return getWdkModel().getModelConfig().getAccountDB().getUserPropertyNames();
  }

  @PUT
  @Path("password")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changeUserPassword(String body)
      throws WdkModelException, DataValidationException {
    User user = getPrivateRegisteredUser();
    try {
      JSONObject json = new JSONObject(body);
      PasswordChangeRequest request = PasswordChangeRequest.createFromJson(json);
      UserFactory userMgr = getWdkModel().getUserFactory();
      if (userMgr.isCorrectPassword(user.getEmail(), request.getOldPassword())) {
        userMgr.changePassword(user.getUserId(), request.getNewPassword());
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
   * Ensures that a new email address, if provided, does not duplicate that of another account.  If
   * the new email passes validation, a new cookie is returned to be used for authentication following the
   * email address update.
   * @param user - the subject of a put or patch
   * @param email - the provided email
   * @throws WdkModelException
   * @throws ConflictException - thrown if the provided email address duplicates that of another account.
   */
  protected NewCookie processEmail(User user, String email) throws ConflictException, WdkModelException {
    // Check to see if this email address matches that of the given user.  If so, no need to check further.
    if (email != null && !email.equalsIgnoreCase(user.getEmail())) {
      User emailUser = getWdkModel().getUserFactory().getUserByEmail(email);
      // Check if the new email address is on record with a different user
      if (emailUser != null && emailUser.getUserId() != user.getUserId()) {
        throw new ConflictException(DUPLICATE_EMAIL);
      }
      LoginCookieFactory cookieFactory = new LoginCookieFactory(getWdkModel().getModelConfig().getSecretKey());
      Cookie oldLoginCookie = LoginCookieFactory.findLoginCookie(getCookies());
      return CookieConverter.toJaxRsCookie(cookieFactory.createLoginCookie(email, oldLoginCookie.getMaxAge()));
    }
    return null;
  }
}
