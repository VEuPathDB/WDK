package org.gusdb.wdk.service.service.user;

import java.util.Map;

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
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.UserPreferenceValidator;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.DataValidationException;
import org.gusdb.wdk.service.request.RequestMisformatException;
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
  public Response getUserPrefs() {
    UserBundle userBundle = getTargetUserBundle(Access.PRIVATE);
    return Response.ok(UserFormatter.getUserPrefsJson(
        userBundle.getTargetUser().getProjectPreferences()).toString()).build();
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
    UserBundle userBundle = getTargetUserBundle(Access.PRIVATE);
    try {
      User user = userBundle.getTargetUser();
      JSONObject json = new JSONObject(body);
      UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json);
      Map<String, String> preferencesMap = request.getPreferencesMap();
      UserPreferenceValidator.validatePreferenceSizes(preferencesMap);
      user.clearProjectPreferences();
      for(String key : preferencesMap.keySet()) {
        user.setProjectPreference(key, preferencesMap.get(key));
      }
      user.save();
      return Response.noContent().build();
    }
    catch(RequestMisformatException e) {
      throw new BadRequestException(e);
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
    UserBundle userBundle = getTargetUserBundle(Access.PRIVATE);
    try {
      User user = userBundle.getTargetUser();
      JSONObject json = new JSONObject(body);
      UserPreferencesRequest request = UserPreferencesRequest.createFromJson(json);
      Map<String, String> preferencesMap = request.getPreferencesMap();
      UserPreferenceValidator.validatePreferenceSizes(preferencesMap);
      for(String key : preferencesMap.keySet()) {
        user.setProjectPreference(key, preferencesMap.get(key));
      }
      user.save();
      return Response.noContent().build();
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }
}
