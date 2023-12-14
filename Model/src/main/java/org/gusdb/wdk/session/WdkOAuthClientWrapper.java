package org.gusdb.wdk.session;

import org.apache.log4j.Logger;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthClient.ValidatedToken;
import org.gusdb.oauth2.client.OAuthConfig;
import org.gusdb.oauth2.shared.token.IdTokenFields;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.RegisteredUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONObject;

import io.jsonwebtoken.Claims;

public class WdkOAuthClientWrapper {

  private static final Logger LOG = Logger.getLogger(WdkOAuthClientWrapper.class);

  private final WdkModel _wdkModel;
  private final OAuthConfig _config;
  private final OAuthClient _client;

  public WdkOAuthClientWrapper(WdkModel wdkModel) throws WdkModelException {
    try {
      _wdkModel = wdkModel;
      _config = wdkModel.getModelConfig();
      _client = new OAuthClient(OAuthClient.getTrustManager(wdkModel.getModelConfig()));
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to instantiate OAuthClient from config", e);
    }
  }

  public ValidatedToken getAuthTokenFromAuthCode(String authCode, String redirectUri) {
    return _client.getAuthTokenFromAuthCode(_config, authCode, redirectUri);
  }

  public ValidatedToken getAuthTokenFromCredentials(String email, String password, String redirectUrl) {
    return _client.getAuthTokenFromUsernamePassword(_config,  email, password, redirectUrl);
  }

  public ValidatedToken getBearerTokenFromAuth(String authCode, String redirectUri) {
    return _client.getBearerTokenFromAuthCode(_config, authCode, redirectUri);
  }

  public ValidatedToken getBearerTokenFromCredentials(String email, String password, String redirectUrl) {
    return _client.getBearerTokenFromUsernamePassword(_config,  email, password, redirectUrl);
  }

  public User getUserFromValidatedToken(ValidatedToken token, UserFactory userFactory) {
    Claims claims = token.getTokenContents();

    User user = new RegisteredUser(_wdkModel,
        Long.parseLong(claims.getSubject()),
        claims.get(IdTokenFields.email.name(), String.class),
        claims.get(IdTokenFields.signature.name(), String.class),
        claims.get(IdTokenFields.preferred_username.name(), String.class));

    // have to hit the user data endpoint for this for user details; bearer token does not include all fields
    JSONObject userData = _client.getUserData(_config, token);

    // user UserFactory to create user from the JSON object, adding to DB if necessary for FKs on other userlogins5 tables
    User user = new UserFactory(wdkModel).
    //long userId = client.getUserIdFromAuthCode(authCode, appUrl);
    //User newUser = wdkModel.getUserFactory().login(userId);
    if (newUser == null) {
      throw new WdkModelException("Unable to find user with ID " + userId +
          ", returned by OAuth service with auth code " + authCode);
    }
  }

  /* Leaving here temporarily as a reference
  private static long getUserIdFromIdToken(String idToken, String clientSecret) throws WdkModelException {
    try {
      LOG.debug("Attempting parse of id token [" + idToken + "] using client secret '" + clientSecret +"'");
      String encodedKey = TextCodec.BASE64.encode(clientSecret);
      Claims claims = Jwts.parser().setSigningKey(encodedKey).parseClaimsJws(idToken).getBody();
      // TODO: verify additional claims for security
      String userIdStr = claims.getSubject();
      LOG.debug("Received token for sub '" + userIdStr + "' and preferred_username '" + claims.get("preferred_username"));
      if (FormatUtil.isInteger(userIdStr)) {
        return Long.valueOf(userIdStr);
      }
      throw new WdkModelException("Subject returned by OAuth server [" + userIdStr + "] is not a valid user ID.");
    }
    catch (JSONException e) {
      throw new WdkModelException("JWT body returned is not a valid JSON object.");
    }
  }
  */

}
