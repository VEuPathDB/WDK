package org.gusdb.wdk.session;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.oauth2.client.OAuthConfig;
import org.gusdb.oauth2.shared.token.IdTokenFields;
import org.gusdb.oauth2.client.OAuthClient.ValidatedToken;
import org.gusdb.oauth2.client.KeyStoreTrustManager;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.RegisteredUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;

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

  public ValidatedToken getValidatedIdTokenFromAuth(String authCode, String redirectUri) {
    return _client.getValidatedAuthToken(_config, authCode, redirectUri);
  }

  public ValidatedToken getValidatedIdTokenFromCredentials(String email, String password, String redirectUrl) {
    return _client.getValidatedAuthToken(_config,  email, password, redirectUrl);
  }

  public ValidatedToken getValidatedBearerTokenFromAuth(String authCode, String redirectUri) {
    return _client.getValidatedBearerToken(_config, authCode, redirectUri);
  }

  public ValidatedToken getValidatedBearerTokenFromCredentials(String email, String password, String redirectUrl) {
    return _client.getValidatedBearerToken(_config,  email, password, redirectUrl);
  }

  public User getUserFromValidatedToken(ValidatedToken token, UserFactory userFactory) {
    Claims claims = token.getTokenContents();
    User user = new RegisteredUser(_wdkModel,
        Long.parseLong(claims.getSubject()),
        claims.get("email", String.class),
        "deprecated", // user signature
        claims.get(IdTokenFields.preferred_username.name(), String.class));
    user.set
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
