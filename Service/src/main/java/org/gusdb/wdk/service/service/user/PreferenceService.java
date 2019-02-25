package org.gusdb.wdk.service.service.user;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserPreferences;
import org.gusdb.wdk.service.UserPreferenceValidator;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserPreferencesRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class PreferenceService extends UserService {
  
  private enum Scope { GLOBAL, PROJECT };
  private enum Action { CLEAR, UPDATE } ;

  public PreferenceService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  // JsonUtil.toJsonObject(user.getPreferences().getGlobalPreferences())
  @GET
  @Path("preferences")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserPrefs() throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    return Response.ok(UserFormatter.getPreferencesJson(user.getPreferences()).toString()).build();
  }

  @GET
  @Path("preferences/global")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGlobalUserPrefs() throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    return Response.ok(JsonUtil.toJsonObject(user.getPreferences().getGlobalPreferences()).toString()).build();
  }
 
  @GET
  @Path("preferences/project")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjectUserPrefs() throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    return Response.ok(JsonUtil.toJsonObject(user.getPreferences().getProjectPreferences()).toString()).build();
  }

  /**
   * Web service to update preferences of existing user with those provided in the request.  Null values cause a delete of the preference
   * @param userIdStr
   * @param body
   * @return - 204 - Success without content
   * @throws WdkModelException, DataValidationException
   * 
   * { action: 'clear' | 'update'
   *   updates: { json object }
   * }
   */
  @PATCH
  @Path("preferences/global")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response patchGlobalUserPrefs(String body) throws WdkModelException, DataValidationException {
    return patchUserPrefs(body, Scope.GLOBAL);
  }
  
  @PATCH
  @Path("preferences/project")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response patchProjectUserPrefs(String body) throws WdkModelException, DataValidationException {
    return patchUserPrefs(body, Scope.PROJECT);
  }
  
  private Response patchUserPrefs(String body, Scope scope) throws WdkModelException, DataValidationException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    try {
      JSONObject json = new JSONObject(body);
      Action action = Action.valueOf(json.getString(JsonKeys.ACTION));  // IllegalArgumentException
      UserPreferences prefs = user.getPreferences();
      
      if (scope == Scope.GLOBAL) {
        if (action == Action.CLEAR) prefs.clearGlobalPreferences();
        else {
          UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json.getJSONObject(JsonKeys.UPDATES));
          updateGlobalPreferences(prefs, request);
        }
      }
      
      else {
        if (action == Action.CLEAR) prefs.clearProjectPreferences();
        else {
          UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json.getJSONObject(JsonKeys.UPDATES));
          updateProjectPreferences(prefs, request);
        }
      }
      
      getWdkModel().getUserFactory().savePreferences(user);
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException | IllegalArgumentException e) {
      throw new BadRequestException(e);
    }
  }

  private void updateGlobalPreferences(UserPreferences prefs, UserPreferencesRequest request) throws DataValidationException {
    UserPreferenceValidator.validatePreferenceSizes(request.getPreferenceUpdates());

    for (String key : request.getPreferenceUpdates().keySet()) {
      prefs.setGlobalPreference(key, request.getPreferenceUpdates().get(key));
    }

    for (String key : request.getPreferenceDeletes()) {
      prefs.unsetGlobalPreference(key);
    }
  }
  
  private void updateProjectPreferences(UserPreferences prefs, UserPreferencesRequest request) throws DataValidationException {
    UserPreferenceValidator.validatePreferenceSizes(request.getPreferenceUpdates());

    for (String key : request.getPreferenceUpdates().keySet()) {
      prefs.setProjectPreference(key, request.getPreferenceUpdates().get(key));
    }

    for (String key : request.getPreferenceDeletes()) {
      prefs.unsetProjectPreference(key);
    }
  }

}


