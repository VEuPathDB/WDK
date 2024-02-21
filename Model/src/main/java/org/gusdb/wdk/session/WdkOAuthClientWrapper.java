package org.gusdb.wdk.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthConfig;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.client.veupathdb.OAuthQuerier;
import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.oauth2.exception.ConflictException;
import org.gusdb.oauth2.exception.InvalidPropertiesException;
import org.gusdb.oauth2.exception.InvalidTokenException;
import org.gusdb.oauth2.shared.IdTokenFields;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.BasicUser;
import org.gusdb.wdk.model.user.BearerTokenUser;
import org.gusdb.wdk.model.user.InvalidUsernameOrEmailException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

public class WdkOAuthClientWrapper {

  private final WdkModel _wdkModel;
  private final OAuthConfig _config;
  private final OAuthClient _client;

  public WdkOAuthClientWrapper(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _config = wdkModel.getModelConfig();
    _client = new OAuthClient(OAuthClient.getTrustManager(wdkModel.getModelConfig()));
  }

  public OAuthClient getOAuthClient() {
    return _client;
  }

  public OAuthConfig getOAuthConfig() {
    return _config;
  }

  public ValidatedToken getBearerTokenFromAuthCode(String authCode, String redirectUri) throws InvalidTokenException {
    return _client.getBearerTokenFromAuthCode(_config, authCode, redirectUri);
  }

  public ValidatedToken getBearerTokenFromCredentials(String email, String password, String redirectUrl) throws InvalidTokenException {
    return _client.getBearerTokenFromUsernamePassword(_config,  email, password, redirectUrl);
  }

  public ValidatedToken validateBearerToken(String rawToken) throws InvalidTokenException {
    return _client.getValidatedEcdsaSignedToken(_config.getOauthUrl(), rawToken);
  }

  public User getUserData(ValidatedToken token) {
    return new BearerTokenUser(_wdkModel, this, token);
  }

  public Map<Long, User> getUsersById(List<Long> userIds) {
    return OAuthQuerier.getUsersById(_client, _config, userIds, json -> new BasicUser(_wdkModel, json));
  }

  public Map<String, User> getUsersByEmail(List<String> emails) {
    return OAuthQuerier.getUsersByEmail(_client, _config, emails, json -> new BasicUser(_wdkModel, json));
  }

  public ValidatedToken getNewGuestToken() {
    return _client.getNewGuestToken(_config);
  }

  private TwoTuple<User, String> parseExpandedUserJson(JSONObject userJson) {
    User user = new BasicUser(_wdkModel, userJson);
    String password = userJson.getString(IdTokenFields.password.name());
    return new TwoTuple<>(user, password);
  }

  public TwoTuple<User,String> createUser(String email, Map<String, String> profileProperties) throws InvalidPropertiesException, InvalidUsernameOrEmailException {
    try {
      Map<String,String> allProps = new HashMap<>(profileProperties);
      allProps.put(IdTokenFields.email.name(), email);
      return parseExpandedUserJson(_client.createNewUser(_config, allProps));
    }
    catch (ConflictException e) {
      throw new InvalidUsernameOrEmailException(e.getMessage());
    }
  }

  public JSONObject updateUser(User user, ValidatedToken token) throws InvalidUsernameOrEmailException, InvalidPropertiesException {
    try {
      Map<String,String> props = new HashMap<>();
      props.put(IdTokenFields.email.name(), user.getEmail());
      for (UserProperty prop : User.USER_PROPERTIES.values()) {
        props.put(prop.getName(), prop.getValue(user));
      }
      return _client.modifyUser(_config, token, props);
    }
    catch (ConflictException e) {
      throw new InvalidUsernameOrEmailException(e.getMessage());
    }
  }

  public TwoTuple<User,String> resetPassword(String loginName) throws InvalidUsernameOrEmailException {
    try {
      return parseExpandedUserJson(_client.resetPassword(_config, loginName));
    }
    catch (InvalidPropertiesException e) {
      throw new InvalidUsernameOrEmailException(e.getMessage());
    }
  }



}
