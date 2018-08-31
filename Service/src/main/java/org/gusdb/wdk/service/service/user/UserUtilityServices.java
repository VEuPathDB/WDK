package org.gusdb.wdk.service.service.user;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.accountdb.AccountManager;
import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigAccountDB;
import org.gusdb.wdk.model.user.InvalidEmailException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserCreationRequest;
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

@Path("/")
public class UserUtilityServices extends WdkService {

  private static final Logger LOG = Logger.getLogger(UserUtilityServices.class);

  private static final String NO_USER_BY_THAT_EMAIL = "No user exists with the email you submitted.";

  /**
   * Creates a new user (i.e. user registration)
   * 
   * @param body JSON body representing a registration form
   * @return JSON representing the new user
   * @throws RequestMisformatException if JSON is misformatted
   * @throws DataValidationException if request contains invalid information
   * @throws WdkModelException if error occurs creating user
   */
  @POST
  @Path("users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createNewUser(String body) throws RequestMisformatException, DataValidationException, WdkModelException {
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
    catch (InvalidEmailException e) {
      throw new DataValidationException(e.getMessage(), e);
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }

  /**
   * Resets a user's forgotten password to a random string.  Request contains a user's email who
   * forgot their password.  No response is returned but an email is sent to the user with their new password.
   * 
   * @param body JSON object with email property containing email of user whose password needs reset
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
      String email = json.getString(Keys.EMAIL);
      UserFactory userMgr = getWdkModel().getUserFactory();
      try {
        userMgr.resetPassword(email);
      }
      catch (WdkUserException e) {
        throw new DataValidationException(NO_USER_BY_THAT_EMAIL);
      }
      return Response.noContent().build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.toString());
    }
  }

  /**
   * Queries user IDs from a set of emails.  Used by user datasets page to share datasets
   * with users when they know their friends' emails but not user IDs.
   * 
   * @param body JSON array of user emails
   * @return JSON array of user IDs
   * @throws RequestMisformatException if request is not an array or is otherwise misformatted
   */
  @POST
  @Path("user-id-query")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response lookupUserId(String body) throws RequestMisformatException {
    Set<String> userEmails = new HashSet<>();
    JSONArray userEmailsJsonArray = new JSONObject(body).getJSONArray("emails");
    ObjectMapper mapper = new ObjectMapper();
    String userEmailsJson = userEmailsJsonArray.toString();
    CollectionType setType = mapper.getTypeFactory().constructCollectionType(Set.class, String.class);
    try {
      userEmails = mapper.readValue(userEmailsJson, setType);
    }
    catch(IOException ioe) {
      throw new RequestMisformatException("The user email list provided could not be parsed.", ioe);
    }
    ModelConfig modelConfig = getWdkModel().getModelConfig();
    ModelConfigAccountDB accountDbConfig = modelConfig.getAccountDB();
    AccountManager accountManager = new AccountManager(getWdkModel().getAccountDb(),
        accountDbConfig.getAccountSchema(), accountDbConfig.getUserPropertyNames());
    Map<String,Long> userEmailIdMap = accountManager.lookUpUserIdsByEmail(userEmails);
    JSONArray jsonUserList = new JSONArray();
    for (String userEmail : userEmails) {
      if (userEmailIdMap.keySet().contains(userEmail)) {
        jsonUserList.put(new JSONObject().put(userEmail,userEmailIdMap.get(userEmail)));
      }
    }
    return Response.ok(new JSONObject().put("results", jsonUserList).toString()).build();
  }

}
