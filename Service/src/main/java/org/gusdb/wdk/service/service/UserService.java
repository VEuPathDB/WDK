package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.formatter.UserFormatter;
import org.gusdb.wdk.session.OAuthUtil;

@Path("/user")
public class UserService extends WdkService {

  private static final String CURRENT_USER_MAGIC_STRING = "current";

  // ===== OAuth 2.0 + OpenID Connect Support =====
  /**
   * Create anti-forgery state token, add to session, and return.  This is
   * requested by the client when the user tries to log in using an OAuth2
   * server.  The value generated will be used to check the state token passed
   * to the oauth processing action.  They must match for the login to succeed.
   * We generate a new value each time; all but one of "overlapping" login
   * attempts by the same user will thus fail.
   * 
   * @return OAuth 2.0 state token
   * @throws WdkModelException if unable to read WDK secret key from file
   */
  @GET
  @Path("oauthStateToken")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getOauthStateToken() throws WdkModelException {
    String newToken = OAuthUtil.generateStateToken(getWdkModel());
    getSession().setAttribute(OAuthUtil.STATE_TOKEN_KEY, newToken);
    return Response.ok(newToken).build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(
      @PathParam("id") String userIdStr,
      @QueryParam("includePreferences") Boolean includePreferences)
          throws WdkModelException {
    Integer userId = getUserId(userIdStr);
    if (userId == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    boolean isOwner = (userId == getCurrentUserId());
    return Response.ok(
        UserFormatter.getUserJson(getWdkModel().getUserFactory().getUser(userId),
            isOwner, getFlag(includePreferences)).toString()
    ).build();
  }

  @GET
  @Path("{id}/preference")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserPrefs(@PathParam("id") String userIdStr) throws WdkModelException {
    Integer userId = getUserId(userIdStr);
    if (userId == null)
      return getNotFoundResponse("Unable to find user with ID " + userIdStr);
    boolean isOwner = (userId == getCurrentUserId());
    if (!isOwner)
      return getPermissionDeniedResponse();
    return Response.ok(
        UserFormatter.getUserPrefsJson(
            getWdkModel().getUserFactory().getUser(userId).getProjectPreferences()).toString()
    ).build();
  }

  private Integer getUserId(String userIdStr) throws WdkModelException {
    if (CURRENT_USER_MAGIC_STRING.equals(userIdStr)) {
      return getCurrentUserId();
    }
    try {
      return Integer.parseInt(userIdStr);
    }
    catch (NumberFormatException | NullPointerException e) {
      return null;
    }
  }
}
