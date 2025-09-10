package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.oauth2.client.veupathdb.UserInfo;
import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.oauth2.exception.InvalidPropertiesException;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.InvalidUsernameOrEmailException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferenceFactory;
import org.gusdb.wdk.model.user.UserPreferences;
import org.gusdb.wdk.service.formatter.ValidationFormatter;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserCreationRequest;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/")
public class UserUtilityServices extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(UserUtilityServices.class);

  public static final String USERS_PATH = "/users";

  private static final String NO_USER_BY_THAT_LOGIN = "No user exists with the login name or email you submitted.";

  /**
   * Creates a new user (i.e. user registration)
   *
   * @param body JSON body representing a registration form
   * @return JSON representing the new user
   * @throws RequestMisformatException if JSON is misformatted
   * @throws DataValidationException if request contains invalid information
   * @throws WdkModelException if error occurs creating user
   * @throws ConflictException if input conflicts with existing data (i.e. duplicate email or username)
   */
  @POST
  @Path(USERS_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createNewUser(String body) throws RequestMisformatException, DataValidationException, WdkModelException, ConflictException {
    try {
      JSONObject requestJson = new JSONObject(body);
      Collection<UserProperty> configuredUserProps = User.USER_PROPERTIES.values();
      UserCreationRequest request = UserCreationRequest.createFromJson(requestJson, configuredUserProps);

      // create the user, saving to OAuth
      UserInfo newUser = getWdkModel().getUserFactory().createUser(
          request.getProfileRequest().getEmail(),
          request.getProfileRequest().getProfileMap());

      // log which guest created this new user for tracking
      LOG.info("Guest user " + getRequestingUser().getUserId() + " registered a new user: " + newUser.getUserId());

      // add user preferences to the new user
      new UserPreferenceFactory(getWdkModel()).savePreferences(newUser.getUserId(), new UserPreferences(
          request.getGlobalPreferencesRequest().getPreferenceUpdates(),
          request.getProjectPreferencesRequest().getPreferenceUpdates()));

      return Response.ok(new JSONObject().put(JsonKeys.ID, newUser.getUserId()))
          .location(getUriInfo().getAbsolutePathBuilder().build(newUser.getUserId()))
          .build();
    }
    catch (InvalidPropertiesException e) {
      // convert to use validation bundle JSON formatting
      throw new DataValidationException(ValidationFormatter.getValidationBundleJson(e.getMessage()).toString());
    }
    catch (InvalidUsernameOrEmailException e) {
      // convert to use validation bundle JSON formatting
      throw new ConflictException(ValidationFormatter.getValidationBundleJson(e.getMessage()).toString());
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  /**
   * Resets a user's forgotten password to a random string.  Request contains a user's email or login name who
   * forgot their password.  No response is returned but an email is sent to the user with their new password.
   *
   * @param body JSON object with email property containing email or login name of user whose password needs reset
   * @return no content or error code corresponding to a problem
   * @throws WdkModelException if error occurs checking for user or sending email
   * @throws DataValidationException if no user exists with that email
   * @throws RequestMisformatException if submitted request JSON is in the wrong format
   */
  @POST
  @Path("user-password-reset")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response resetUserPassword(String body)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    try {
      JSONObject json = new JSONObject(body);
      String loginName = json.getString(JsonKeys.EMAIL); // email, but also now supports username
      getWdkModel().getUserFactory().resetPassword(loginName);
      return Response.noContent().build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.toString());
    }
    catch (WdkUserException e) {
      throw new DataValidationException(NO_USER_BY_THAT_LOGIN);
    }
  }

  /**
   * Queries user IDs from a set of emails.  Used by user datasets page to share datasets
   * with users when they know their friends' emails but not user IDs.
   *
   * @param body JSON array of user emails
   * @return JSON array of objects, where each object is an email -> ID mapping
   * @throws RequestMisformatException if request is not an array or is otherwise misformatted
   */
  @POST
  @Path("user-id-query")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response lookupUserId(String body) throws RequestMisformatException {
    try {
      Set<String> userEmails = new HashSet<>();
      JSONArray userEmailsJsonArray = new JSONObject(body).getJSONArray("emails");
      for (JsonType jsonValue : JsonIterators.arrayIterable(userEmailsJsonArray)) {
        if (jsonValue.getType().equals(ValueType.STRING)) {
          userEmails.add(jsonValue.getString());
        }
        else {
          throw new RequestMisformatException("The user email list provided must be an array of strings.");
        }
      }
      Map<String,UserInfo> userMap = getWdkModel().getUserFactory().getUsersByEmail(new ArrayList<>(userEmails));
      Map<String,Long> idMap = Functions.getMapFromKeys(userMap.keySet(),
          email -> Optional.ofNullable(userMap.get(email)).map(UserInfo::getUserId).orElse(null));
      return Response.ok(new JSONObject().put("results", idMap).toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  /**
   * Deletes a set of users by ID.
   * @param userIdsParam
   * @return
   */
  @GET
  @Path("delete-users")
  @Produces(MediaType.TEXT_PLAIN)
  public Response deleteUsers(@QueryParam("userIds") String userIdsParam) {
    if (userIdsParam == null || userIdsParam.trim().isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("No IDs specified.").build();
    }
    List<Long> ids = new ArrayList<>();
    for (String idStr : userIdsParam.split(",")) {
      try {
        ids.add(Long.valueOf(idStr));
      }
      catch (NumberFormatException e) {
        return Response.status(Status.BAD_REQUEST).entity("At least one ID is not an integer.").build();
      }
    }

    // checked as well as we could; delete the users
    getWdkModel().getUserFactory().deleteUsers(
        getRequestingUser().getAuthenticationToken(), ids);

    return Response.ok(ids.size() + " users deleted.").build();  }

}
