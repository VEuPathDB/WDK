package org.gusdb.wdk.service.service.user;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

  public PreferenceService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("preference")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserPrefs() throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    return Response.ok(UserFormatter.getPreferencesJson(user.getPreferences()).toString()).build();
  }

  /**
   * Web service to replace project preferences of existing user with those provided in the
   * request.  Existing project preferences are removed and re-populated with the contents of the request.
   * @param userIdStr
   * @param body
   * @return - 204 - Success without content
   * @throws WdkModelException, DataValidationException
   */
  @PUT
  @Path("preference")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putUserPrefs(String body) throws DataValidationException, WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    try {
      JSONObject json = new JSONObject(body);
      UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json);
      if (!request.getGlobalPreferenceDeletes().isEmpty() || !request.getProjectPreferenceDeletes().isEmpty()) {
        throw new DataValidationException("A PUT request cannot contain delete (i.e. null) instructions.  Try PATCH.");
      }
      UserPreferences prefs = user.getPreferences();
      updatePreferences(prefs, request, true);
      getWdkModel().getUserFactory().savePreferences(user);
      return Response.noContent().build();
    }
    catch(RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  private void updatePreferences(UserPreferences prefs, UserPreferencesRequest request, boolean clearFirst) throws DataValidationException {
    UserPreferenceValidator.validatePreferenceSizes(request.getGlobalPreferenceMods());
    UserPreferenceValidator.validatePreferenceSizes(request.getProjectPreferenceMods());
    if (clearFirst) {
      prefs.clearGlobalPreferences();
      prefs.clearProjectPreferences();
    }
    for (String key : request.getGlobalPreferenceMods().keySet()) {
      prefs.setGlobalPreference(key, request.getGlobalPreferenceMods().get(key));
    }
    for (String key : request.getProjectPreferenceMods().keySet()) {
      prefs.setProjectPreference(key, request.getProjectPreferenceMods().get(key));
    }
    for (String key : request.getGlobalPreferenceDeletes()) {
      prefs.unsetGlobalPreference(key);
    }
    for (String key : request.getProjectPreferenceDeletes()) {
      prefs.unsetProjectPreference(key);
    }
  }

  /**
   * Web service to replace preferences of existing user with those provided in the request.  Unchanged
   * preferences remain unchanged.
   * @param userIdStr
   * @param body
   * @return - 204 - Success without content
   * @throws WdkModelException, DataValidationException
   */
  @PATCH
  @Path("preference")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response patchUserPrefs(String body) throws WdkModelException, DataValidationException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    try {
      JSONObject json = new JSONObject(body);
      UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json);
      UserPreferences prefs = user.getPreferences();
      updatePreferences(prefs, request, false);
      getWdkModel().getUserFactory().savePreferences(user);
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }
}
