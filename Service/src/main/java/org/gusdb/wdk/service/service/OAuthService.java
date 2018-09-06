package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.session.OAuthUtil;
import org.json.JSONObject;

@Path("/oauth")
public class OAuthService extends AbstractWdkService {

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
  @Path("state-token")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOauthStateToken() throws WdkModelException {
    String newToken = OAuthUtil.generateStateToken(getWdkModel());
    getSession().setAttribute(OAuthUtil.STATE_TOKEN_KEY, newToken);
    JSONObject json = new JSONObject();
    json.put(Keys.OAUTH_STATE_TOKEN, newToken);
    return Response.ok(json.toString()).build();
  }
}
