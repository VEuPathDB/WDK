package org.gusdb.wdk.service.service.user;

import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.oauth2.exception.InvalidPropertiesException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.BasicUser;
import org.gusdb.wdk.model.user.InvalidUsernameOrEmailException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferenceFactory;
import org.gusdb.wdk.model.user.UserPreferences;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserProfileRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileService extends UserService {

  public ProfileService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  // FIXME Uncomment when overrides can be handled
  // @OutSchema("wdk.users.get-by-id")
  public JSONObject getById(@QueryParam("includePreferences") Boolean includePreferences) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    return formatUser(userBundle.getTargetUser(), userBundle.isSessionUser(),
        getFlag(includePreferences));
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
  protected JSONObject formatUser(User user, boolean isSessionUser, boolean includePrefs) throws WdkModelException {
    Optional<UserPreferences> userPrefs = !includePrefs ? Optional.empty() :
        Optional.of(new UserPreferenceFactory(getWdkModel()).getPreferences(user.getUserId()));
    return UserFormatter.getUserJson(user, isSessionUser, userPrefs);
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
      User oldUser = getPrivateRegisteredUser();
      UserProfileRequest request = UserProfileRequest.createFromJson(
          new JSONObject(body), User.USER_PROPERTIES.values(), true);

      // overwrite user's old email and profile
      User newUser = new BasicUser(oldUser);
      newUser.setEmail(request.getEmail());
      newUser.setPropertyValues(request.getProfileMap());

      // save user to OAuth
      newUser = getWdkModel().getUserFactory().saveUser(oldUser, newUser, getAuthorizationToken());

      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
    catch (InvalidPropertiesException e) {
      throw new DataValidationException(e.getMessage());
    }
    catch (InvalidUsernameOrEmailException e) {
      throw new ConflictException(e.getMessage());
    }
  }
}
