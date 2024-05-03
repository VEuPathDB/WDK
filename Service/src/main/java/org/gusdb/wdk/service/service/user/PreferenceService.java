package org.gusdb.wdk.service.service.user;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferenceFactory;
import org.gusdb.wdk.model.user.UserPreferences;
import org.gusdb.wdk.service.annotation.InSchema;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserPreferencesRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class PreferenceService extends UserService {

  private enum Scope { GLOBAL, PROJECT }
  private enum Action { CLEAR, UPDATE }

  public PreferenceService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("preferences")
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.users.preferences.get-response")
  public Response getUserPrefs() throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    UserPreferences userPrefs = new UserPreferenceFactory(getWdkModel()).getPreferences(user.getUserId());
    return Response.ok(UserFormatter.getPreferencesJson(userPrefs).toString()).build();
  }

  /**
   * Web service to update preferences of existing user with those provided in the request.
   * Null values cause a delete of the preference.
   *
   * { action: 'clear' | 'update'
   *   updates: { json object }
   * }
   * 
   * @param body
   * @return 204 response - Success without content
   * @throws WdkModelException
   * @throws DataValidationException
   */
  @PATCH
  @Path("preferences/global")
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.preferences.patch-request")
  public Response patchGlobalUserPrefs(JSONObject body) throws WdkModelException, DataValidationException {
    return patchUserPrefs(body, Scope.GLOBAL);
  }

  @PATCH
  @Path("preferences/project")
  @Consumes(MediaType.APPLICATION_JSON)
  @InSchema("wdk.users.preferences.patch-request")
  public Response patchProjectUserPrefs(JSONObject body) throws WdkModelException, DataValidationException {
    return patchUserPrefs(body, Scope.PROJECT);
  }

  private Response patchUserPrefs(JSONObject body, Scope scope) throws WdkModelException, DataValidationException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    try {
      Action action = Action.valueOf(body.getString(JsonKeys.ACTION));  // IllegalArgumentException
      UserPreferenceFactory prefsFactory = new UserPreferenceFactory(getWdkModel());
      UserPreferences prefs = prefsFactory.getPreferences(user.getUserId());

      if (scope == Scope.GLOBAL) {
        if (action == Action.CLEAR) prefs.clearGlobalPreferences();
        else {
          if (body.has(JsonKeys.UPDATES)) {  // schema doesn't require this because it is hard to model in json schema
            UserPreferencesRequest request = UserPreferencesRequest.createFromJson(body.getJSONObject(JsonKeys.UPDATES));
            updateGlobalPreferences(prefs, request);
          }
        }
      }
      else {
        if (action == Action.CLEAR) prefs.clearProjectPreferences();
        else {
          if (body.has(JsonKeys.UPDATES)) {
            UserPreferencesRequest request = UserPreferencesRequest.createFromJson(body.getJSONObject(JsonKeys.UPDATES));
            updateProjectPreferences(prefs, request);
          }
        }
      }

      prefsFactory.savePreferences(user.getUserId(), prefs);
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException | IllegalArgumentException e) {
      throw new BadRequestException(e);
    }
  }

  private void updateGlobalPreferences(UserPreferences prefs, UserPreferencesRequest request) {
    for (String key : request.getPreferenceUpdates().keySet()) {
      prefs.setGlobalPreference(key, request.getPreferenceUpdates().get(key));
    }
    for (String key : request.getPreferenceDeletes()) {
      prefs.unsetGlobalPreference(key);
    }
  }
  
  private void updateProjectPreferences(UserPreferences prefs, UserPreferencesRequest request) {
    for (String key : request.getPreferenceUpdates().keySet()) {
      prefs.setProjectPreference(key, request.getPreferenceUpdates().get(key));
    }
    for (String key : request.getPreferenceDeletes()) {
      prefs.unsetProjectPreference(key);
    }
  }

}
