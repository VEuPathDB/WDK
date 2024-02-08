package org.gusdb.wdk.model.user;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.accountdb.AccountManager;
import org.gusdb.fgputil.accountdb.UserProfile;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler.Status;
import org.gusdb.fgputil.events.Events;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.shared.IdTokenFields;
import org.gusdb.wdk.events.UserProfileUpdateEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.session.WdkOAuthClientWrapper;
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
  // database table and column definitions
  // -------------------------------------------------------------------------

  public static final String TABLE_USERS = "users";
  public static final String COL_USER_ID = "user_id";
  public static final String COL_IS_GUEST = "is_guest";
  public static final String COL_FIRST_ACCESS = "first_access";

  // -------------------------------------------------------------------------
  // sql and sql macro definitions
  // -------------------------------------------------------------------------

  static final String USER_SCHEMA_MACRO = "$$USER_SCHEMA$$";
  private static final String IS_GUEST_VALUE_MACRO = "$$IS_GUEST$$";

  // SQL and types to insert previously unknown user refs into the users table
  private static final String INSERT_USER_REF_SQL =
      "insert" +
      "  when not exists (select 1 from " + USER_SCHEMA_MACRO + TABLE_USERS + " where " + COL_USER_ID + " = ?)" +
      "  then" +
      "  into " + USER_SCHEMA_MACRO + TABLE_USERS + " (" + COL_USER_ID + "," + COL_IS_GUEST + "," + COL_FIRST_ACCESS +")" +
      "  select ?, " + IS_GUEST_VALUE_MACRO + ", ? from dual";

  private static final Integer[] INSERT_USER_REF_PARAM_TYPES = { Types.BIGINT, Types.BIGINT, Types.TIMESTAMP };

  // SQL and types to select user ref by ID
  private static final String SELECT_USER_REF_BY_ID_SQL =
      "select " + COL_USER_ID + ", " + COL_IS_GUEST + ", " + COL_FIRST_ACCESS +
      "  from " + USER_SCHEMA_MACRO + TABLE_USERS +
      "  where " + COL_USER_ID + " = ?";

  private static final Integer[] SELECT_USER_REF_BY_ID_PARAM_TYPES = { Types.BIGINT };

  private static class UserReference extends ThreeTuple<Long, Boolean, Date> {
    public UserReference(Long userId, Boolean isGuest, Date firstAccess) {
      super(userId, isGuest, firstAccess);
    }
    public Long getUserId() { return getFirst(); }
    public Boolean isGuest() { return getSecond(); }
    public Date getFirstAccess() { return getThird(); }
  }

  // -------------------------------------------------------------------------
  // the macros used by the registration email
  // -------------------------------------------------------------------------

  private static final String EMAIL_MACRO_USER_NAME = "USER_NAME";
  private static final String EMAIL_MACRO_EMAIL = "EMAIL";
  private static final String EMAIL_MACRO_PASSWORD = "PASSWORD";

  // -------------------------------------------------------------------------
  // member variables
  // -------------------------------------------------------------------------

  private final WdkModel _wdkModel;
  private final DatabaseInstance _userDb;
  private final String _userSchema;
  private final WdkOAuthClientWrapper _client;

  // -------------------------------------------------------------------------
  // constructor
  // -------------------------------------------------------------------------

  public UserFactory(WdkModel wdkModel) {
    // save model for populating new users
    _wdkModel = wdkModel;
    _userDb = wdkModel.getUserDb();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    _client = new WdkOAuthClientWrapper(wdkModel);
  }

  // -------------------------------------------------------------------------
  // methods
  // -------------------------------------------------------------------------

  public User createUser(String email, Map<String, String> profileProperties)
      throws WdkModelException, WdkUserException {

    // contact OAuth server to create a new user with the passed props
    TwoTuple<User,String> userTuple = parseExpandedUserJson(_client.createUser(email, profileProperties));
    User user = userTuple.getFirst();

    // add user to this user DB (will be added to other user DBs as needed during login)
    addUserReference(user);

    // if needed, send user temporary password via email
    if (isSendWelcomeEmail()) {
      emailTemporaryPassword(user, userTuple.getSecond(), _wdkModel.getModelConfig());
    }

    return user;

  }

  private TwoTuple<User, String> parseExpandedUserJson(JSONObject userJson) {
    User user = new BasicUser(_wdkModel,
        Long.valueOf(userJson.getString(IdTokenFields.sub.name())),
        userJson.getBoolean(IdTokenFields.is_guest.name()),
        userJson.getString(IdTokenFields.signature.name()),
        userJson.getString(IdTokenFields.preferred_username.name())
    );
    user.setPropertyValues(userJson);
    String password = userJson.getString(IdTokenFields.password.name());
    return new TwoTuple<>(user, password);
  }

  /**
   * @return whether or not WDK is configured to send a welcome email to new registered users (defaults to true)
   */
  private boolean isSendWelcomeEmail() {
    String dontEmailProp = _wdkModel.getProperties().get("DONT_EMAIL_NEW_USER");
    return dontEmailProp == null || !dontEmailProp.equals("true");
  }

  private static void emailTemporaryPassword(User user, String password,
      ModelConfig modelConfig) throws WdkModelException {

    String smtpServer = modelConfig.getSmtpServer();
    String supportEmail = modelConfig.getSupportEmail();
    String emailSubject = modelConfig.getEmailSubject();

    // populate email content macros with user data
    String emailContent = modelConfig.getEmailContent()
        .replaceAll("\\$\\$" + EMAIL_MACRO_USER_NAME + "\\$\\$",
            Matcher.quoteReplacement(user.getDisplayName()))
        .replaceAll("\\$\\$" + EMAIL_MACRO_EMAIL + "\\$\\$",
            Matcher.quoteReplacement(user.getEmail()))
        .replaceAll("\\$\\$" + EMAIL_MACRO_PASSWORD + "\\$\\$",
            Matcher.quoteReplacement(password));

    Utilities.sendEmail(smtpServer, user.getEmail(), supportEmail, emailSubject, emailContent);
  }

  /**
   * Adds a user reference row to the UserDB users table if one does not exist.
   * Note is_guest and first_access are immutable fields and once set will not be
   * changed by this code.
   *
   * @param user user to add
   * @throws WdkModelException 
   */
  public int addUserReference(User user) throws WdkModelException {
    try {
      long userId = user.getUserId();
      boolean isGuest = user.isGuest();
      Timestamp insertedOn = new Timestamp(new Date().getTime());
      String sql = INSERT_USER_REF_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(IS_GUEST_VALUE_MACRO, _userDb.getPlatform().convertBoolean(isGuest).toString());
      return new SQLRunner(_userDb.getDataSource(), sql, "insert-user-ref")
        .executeUpdate(new Object[]{ userId, userId, insertedOn }, INSERT_USER_REF_PARAM_TYPES);
    }
    catch (SQLRunnerException e) {
      throw WdkModelException.translateFrom(e);
    }
  }

  private Optional<UserReference> getUserReference(long userId) throws WdkModelException {
    try {
      String sql = SELECT_USER_REF_BY_ID_SQL.replace(USER_SCHEMA_MACRO, _userSchema);
      return new SQLRunner(_userDb.getDataSource(), sql, "get-user-ref").executeQuery(
          new Object[]{ userId },
          SELECT_USER_REF_BY_ID_PARAM_TYPES,
          rs ->
              !rs.next()
              ? Optional.empty()
              : Optional.of(new UserReference(
                  rs.getLong(COL_USER_ID),
                  rs.getBoolean(COL_IS_GUEST),
                  new Date(rs.getTimestamp(COL_FIRST_ACCESS).getTime()))));
    }
    catch (SQLRunnerException e) {
      throw WdkModelException.translateFrom(e);
    }
  }

  public TwoTuple<ValidatedToken, User> createUnregisteredUser() {
    ValidatedToken token = _client.getNewGuestToken();
    User user = new BearerTokenUser(_wdkModel, _client, token);
    GUEST_CREATION_COUNTER.inc();
    return new TwoTuple<>(token, user);
  }

  
  public User login(User guest, String email, String password)
      throws WdkUserException {
    // make sure the guest is really a guest
    if (!guest.isGuest())
      throw new WdkUserException("User has been logged in.");

    // authenticate the user; if fails, a WdkUserException will be thrown out
    User user = authenticate(email, password);
    if (user == null) {
      throw new WdkUserException("Invalid email or password.");
    }
    return completeLogin(user);
  }

  public User login(long userId) throws WdkModelException {
    return completeLogin(getUserById(userId)
        .orElseThrow(() -> new WdkModelException("User with ID " + userId + " could not be found.")));
  }

  /**
   * Returns whether email and password are a correct credentials combination.
   * 
   * @param usernameOrEmail user email
   * @param password user password
   * @return true email corresponds to user and password is correct, else false
   * @throws WdkModelException if error occurs while determining result
   */
  public boolean isCorrectPassword(String usernameOrEmail, String password) throws WdkModelException {
    return authenticate(usernameOrEmail, password) != null;
  }

  private User authenticate(String usernameOrEmail, String password) {
    return getUserFromProfile(_accountManager.getUserProfile(usernameOrEmail, password));
  }

  /**
   * Returns user by user ID, or an empty optional if not found
   * 
   * @param userId user ID
   * @return user user object for the passed ID
   * @throws WdkModelException if an error occurs in the attempt
   */
  public Optional<User> getUserById(long userId) throws WdkModelException {
    UserProfile profile = _accountManager.getUserProfile(userId);
    if (profile != null) {
      // found registered user in account DB; create RegisteredUser and populate
      return Optional.of(getUserFromProfile(profile));
    }
    else {
      // cannot find user in account DB; however, the passed ID may represent a guest local to this userDb
      Date accessDate = getGuestUserRefFirstAccess(userId);
      if (accessDate != null) {
        // guest user was found in local user Db; create UnregisteredUser and populate
        profile = AccountManager.createGuestProfile("GUEST_", userId, accessDate);
        return Optional.of(new User(_wdkModel, profile.getUserId(), true,
            profile.getSignature(), profile.getStableId()).setEmail(profile.getEmail()));
        
      }
      else {
        // user does not exist in account or user DBs
        return Optional.empty();
      }
    }
  }

  public User getUserByEmail(String email) {
    return getUserFromProfile(_accountManager.getUserProfileByEmail(email));
  }

  private User getUserProfileByUsernameOrEmail(String usernameOrEmail) {
    return getUserFromProfile(_accountManager.getUserProfileByUsernameOrEmail(usernameOrEmail));
  }

  /**
   * Save the basic information of a user
   * 
   * @param user
   */
  public void saveUser(User user) throws WdkModelException, InvalidUsernameOrEmailException {
    try {

      // Three integrity checks:

      // 1. Check if user exists in the database. if not, fail and ask to create the user first
      UserProfile oldProfile = _accountManager.getUserProfile(user.getUserId());
      if (oldProfile == null) {
        throw new WdkModelException("Cannot update user; no user exists with ID " + user.getUserId());
      }

      // 2. Check if another user exists with this email (PK will protect us but want better message)
      UserProfile emailUser = _accountManager.getUserProfileByEmail(user.getEmail());
      if (emailUser != null && emailUser.getUserId() != user.getUserId()) {
        throw new InvalidUsernameOrEmailException("This email is already in use by another account.  Please choose another.");
      }

      // 3. Check if another user exists with this username (if supplied)
      if (user.getProfileProperties().containsKey(AccountManager.USERNAME_PROPERTY_KEY)) {
        String username = user.getProfileProperties().get(AccountManager.USERNAME_PROPERTY_KEY);
        
        UserProfile usernameUser = _accountManager.getUserProfileByUsername(username);
        if (usernameUser != null && user.getUserId() != usernameUser.getUserId()) {
          throw new InvalidUsernameOrEmailException("The username '" + username + "' is already in use. " + "Please choose another one.");
        }
      }

      // save off other data to user profile
      _accountManager.saveUserProfile(user.getUserId(), user.getEmail(), new MapBuilder<String,String>()
          .put("firstName", user.getFirstName())
          .put("middleName", user.getMiddleName())
          .put("lastName", user.getLastName())
          .put("organization", user.getOrganization())
          .toMap()
      );

      // get updated profile and trigger profile update event
      UserProfile newProfile = _accountManager.getUserProfile(user.getUserId());
      Events.trigger(new UserProfileUpdateEvent(oldProfile, newProfile, _wdkModel));

    }
    catch (InvalidUsernameOrEmailException e) {
      throw e;
    }
    // wrap any other exception in WdkModelException
    catch (Exception e) {
      throw new WdkModelException("Unable to update user profile for ID " + user.getUserId(), e);
    }
  }

  public void resetPassword(String emailOrLoginName) throws WdkUserException, WdkModelException {
    User user = getUserProfileByUsernameOrEmail(emailOrLoginName);
    if (user == null) {
      throw new WdkUserException("Cannot find user with email or login name: " + emailOrLoginName);
    }
    // create new temporary password
    String newPassword = generateTemporaryPassword();
    // set new password on user
    _accountManager.updatePassword(user.getUserId(), newPassword);
    // email user new password
    emailTemporaryPassword(user, newPassword, _wdkModel.getModelConfig());
  }



}
