package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.events.Events;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthConfig;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.client.veupathdb.OAuthQuerier;
import org.gusdb.oauth2.client.veupathdb.UserInfo;
import org.gusdb.oauth2.client.veupathdb.UserInfoImpl;
import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.oauth2.exception.ConflictException;
import org.gusdb.oauth2.exception.ExpiredTokenException;
import org.gusdb.oauth2.exception.InvalidPropertiesException;
import org.gusdb.oauth2.exception.InvalidTokenException;
import org.gusdb.oauth2.shared.IdTokenFields;
import org.gusdb.wdk.events.UserProfileUpdateEvent;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONObject;

import io.prometheus.client.Counter;

/**
 * Manages persistence of user profile and preferences and creation and
 * deletion of users, including ID assignment.
 * 
 * @author xingao
 * @author rdoherty
 */
public class UserFactory {

  @SuppressWarnings("unused")
  private static Logger LOG = Logger.getLogger(UserFactory.class);

  private static final Counter GUEST_CREATION_COUNTER = Counter.build()
      .name("wdk_guest_creation_count")
      .help("Number of guest users created by WDK services")
      .register();

  // -------------------------------------------------------------------------
  // member fields
  // -------------------------------------------------------------------------

  private final WdkModel _wdkModel;
  private final UserReferenceFactory _userRefFactory;
  private final UserPasswordEmailer _emailer;
  private final OAuthConfig _config;
  private final OAuthClient _client;

  // -------------------------------------------------------------------------
  // constructor
  // -------------------------------------------------------------------------

  public UserFactory(WdkModel wdkModel) {
    // save model for populating new users
    _wdkModel = wdkModel;
    _userRefFactory = new UserReferenceFactory(wdkModel);
    _emailer = new UserPasswordEmailer(wdkModel);
    _config = wdkModel.getModelConfig();
    _client = new OAuthClient(OAuthClient.getTrustManager(wdkModel.getModelConfig()));
  }

  // -------------------------------------------------------------------------
  // methods to manage tokens
  // -------------------------------------------------------------------------

  public ValidatedToken getBearerTokenFromAuthCode(String authCode, String redirectUri) throws InvalidPropertiesException {
    return _client.getBearerTokenFromAuthCode(_config, authCode, redirectUri);
  }

  public ValidatedToken getBearerTokenFromCredentials(String email, String password, String redirectUrl) throws InvalidPropertiesException {
    return _client.getBearerTokenFromUsernamePassword(_config,  email, password, redirectUrl);
  }

  public ValidatedToken validateBearerToken(String rawToken) throws InvalidTokenException, ExpiredTokenException {
    return _client.getValidatedEcdsaSignedToken(_config.getOauthUrl(), rawToken);
  }

  // -------------------------------------------------------------------------
  // methods to manage users
  // -------------------------------------------------------------------------

  public User convertToUser(ValidatedToken token) throws WdkModelException {
    User user = new UserImpl(_wdkModel, _client, _config, token);
    _userRefFactory.addUserReference(user);
    return user;
  }

  public TwoTuple<ValidatedToken, User> createUnregisteredUser() throws WdkModelException {
    ValidatedToken token = _client.getNewGuestToken(_config);
    GUEST_CREATION_COUNTER.inc();
    return new TwoTuple<>(token, convertToUser(token));
  }

  public UserInfo createUser(String email, Map<String, String> profileProperties)
      throws WdkModelException, InvalidPropertiesException, InvalidUsernameOrEmailException {
    try {

      // contact OAuth server to create a new user with the passed props
      Map<String,String> allProps = new HashMap<>(profileProperties);
      allProps.put(IdTokenFields.email.name(), email);
      TwoTuple<UserInfo,String> userTuple = parseExpandedUserJson(_client.createNewUser(_config, allProps));

      UserInfo user = userTuple.getFirst();
      String password = userTuple.getSecond();
  
      // add user to this user DB (will be added to other user DBs as needed during login)
      _userRefFactory.addUserReference(user);
  
      // if needed, send user temporary password via email
      if (_emailer.isSendWelcomeEmail()) {
        _emailer.emailTemporaryPassword(user, password);
      }
  
      return user;
    }
    catch (ConflictException e) {
      throw new InvalidUsernameOrEmailException(e.getMessage());
    }
  }

  private TwoTuple<UserInfo, String> parseExpandedUserJson(JSONObject userJson) {
    UserInfo user = new UserInfoImpl(userJson);
    String password = userJson.getString(IdTokenFields.password.name());
    return new TwoTuple<>(user, password);
  }

  /**
   * Save the basic information of a user
   * 
   * @param user
   * @param newUser 
   * @throws InvalidPropertiesException 
   */
  public UserInfo saveUser(UserInfo oldUser, UserInfo newUser, ValidatedToken authorizationToken) throws InvalidUsernameOrEmailException, InvalidPropertiesException {
    try {
      // build map of user props + email to send to OAuth
      Map<String,String> props = new HashMap<>();
      props.put(IdTokenFields.email.name(), newUser.getEmail());
      for (UserProperty prop : User.USER_PROPERTIES.values()) {
        props.put(prop.getName(), prop.getValue(newUser));
      }

      // build a user from the response
      UserInfo savedUser = new UserInfoImpl(_client.modifyUser(_config, authorizationToken, props));

      Events.trigger(new UserProfileUpdateEvent(oldUser, savedUser, _wdkModel));
      return savedUser;
    }
    catch (ConflictException e) {
      throw new InvalidUsernameOrEmailException(e.getMessage());
    }
  }

  public void resetPassword(String loginName) throws InvalidUsernameOrEmailException, WdkModelException {
    try {
      TwoTuple<UserInfo, String> user = parseExpandedUserJson(_client.resetPassword(_config, loginName));

      // email user new password
      _emailer.emailTemporaryPassword(user.getFirst(), user.getSecond());
    }
    catch (InvalidPropertiesException e) {
      throw new InvalidUsernameOrEmailException(e.getMessage());
    }
  }

  // -------------------------------------------------------------------------
  // methods to query users
  // -------------------------------------------------------------------------

  public Map<Long, UserInfo> getUsersById(List<Long> userIds) {
    // ensure a unique list
    userIds = new ArrayList<>(new HashSet<>(userIds));
    Map<Long,UserInfo> userMap = OAuthQuerier.getUsersById(_client, _config, userIds, json -> new UserInfoImpl(json));
    // FIXME: This is a temporary hack to account for guests created before the
    //   implementation of bearer tokens who still have WDK steps/strats.  Eventually
    //   these guests should be removed during regular maintenance cleanup; once all
    //   guests created before spring 2024 are removed, this code can also be removed.
    for (Long userId : userIds) {
      if (userMap.get(userId) == null) {
        // OAuth does not know about this user; trust that it is a guest
        userMap.put(userId, new UserInfoImpl(userId, true, userId.toString(), userId.toString()));
      }
    }
    return userMap;
  }

  public Map<String, UserInfo> getUsersByEmail(List<String> emails) {
    // ensure a unique list
    emails = new ArrayList<>(new HashSet<>(emails));
    return OAuthQuerier.getUsersByEmail(_client, _config, emails, json -> new UserInfoImpl(json));
  }

  /**
   * Returns user by user ID, or an empty optional if not found
   * 
   * @param userId user ID
   * @return user user object for the passed ID
   * @throws WdkModelException if an error occurs in the attempt
   */
  public Optional<UserInfo> getUserById(long userId) throws WdkModelException {
    return Optional.ofNullable(getUsersById(List.of(userId)).get(userId));
  }

  public Optional<UserInfo> getUserByEmail(String email) {
    return Optional.ofNullable(getUsersByEmail(List.of(email)).get(email));
  }

  public Map<Long, Boolean> verifyUserids(Set<Long> userIds) {
    Map<Long, UserInfo> userMap = getUsersById(new ArrayList<>(userIds));
    return userIds.stream().collect(Collectors.toMap(id -> id, id -> userMap.get(id) != null));
  }
}
