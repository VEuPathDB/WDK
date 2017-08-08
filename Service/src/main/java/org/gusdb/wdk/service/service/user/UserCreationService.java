package org.gusdb.wdk.service.service.user;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserCreationRequest;
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/user")
public class UserCreationService extends WdkService {

  private static final Logger LOG = Logger.getLogger(UserCreationService.class);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createNewUser(String body) throws DataValidationException, WdkModelException, WdkUserException {
    try {
      JSONObject requestJson = new JSONObject(body);
      List<UserPropertyName> configuredUserProps = getWdkModel().getModelConfig().getAccountDB().getUserPropertyNames();
      UserCreationRequest request = UserCreationRequest.createFromJson(requestJson, configuredUserProps);
      User newUser = getWdkModel().getUserFactory().createUser(
          request.getProfileRequest().getEmail(),
          request.getProfileRequest().getProfileMap(),
          request.getPreferencesRequest().getGlobalPreferenceMods(),
          request.getPreferencesRequest().getProjectPreferenceMods());
      JSONObject newUserJson = UserFormatter.getUserJson(newUser, true, true, configuredUserProps);
      LOG.info("Created new user: " + newUserJson.toString(2));
      return Response.ok(newUserJson.toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }
}
