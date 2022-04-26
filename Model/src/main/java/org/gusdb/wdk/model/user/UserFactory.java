package org.gusdb.wdk.model.user;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.accountdb.AccountManager;
import org.gusdb.fgputil.accountdb.UserProfile;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler.Status;
import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.events.UserProfileUpdateEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigAccountDB;
import org.gusdb.wdk.model.user.UnregisteredUser.UnregisteredUserType;

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

  private static final String COUNT_USER_REF_BY_ID_SQL =
      "select count(*)" +
      "  from " + USER_SCHEMA_MACRO + TABLE_USERS +
      "  where " + COL_USER_ID + " = ?";
  private static final Integer[] COUNT_USER_REF_BY_ID_PARAM_TYPES = { Types.BIGINT };

  private static final String INSERT_USER_REF_SQL =
      "insert into " + USER_SCHEMA_MACRO + TABLE_USERS +
      "  (" + COL_USER_ID + "," + COL_IS_GUEST + "," + COL_FIRST_ACCESS +")" +
      "  values (?, " + IS_GUEST_VALUE_MACRO + ", ?)";
  private static final Integer[] INSERT_USER_REF_PARAM_TYPES = { Types.BIGINT, Types.TIMESTAMP };

  private static final String SELECT_GUEST_USER_REF_BY_ID_SQL =
      "select " + COL_FIRST_ACCESS +
      "  from " + USER_SCHEMA_MACRO + TABLE_USERS +
      "  where " + COL_IS_GUEST + " = " + IS_GUEST_VALUE_MACRO + " and " + COL_USER_ID + " = ?";
  private static final Integer[] SELECT_GUEST_USER_REF_BY_ID_PARAM_TYPES = { Types.BIGINT };

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
  private final AccountManager _accountManager;
  private final UserPreferenceFactory _preferenceFactory;

  // -------------------------------------------------------------------------
  // constructor
  // -------------------------------------------------------------------------

  public UserFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _preferenceFactory = new UserPreferenceFactory(wdkModel);
    _userDb = wdkModel.getUserDb();
    ModelConfig modelConfig = wdkModel.getModelConfig();
    _userSchema = modelConfig.getUserDB().getUserSchema();
    ModelConfigAccountDB accountDbConfig = modelConfig.getAccountDB();
    _accountManager = new AccountManager(wdkModel.getAccountDb(),
        accountDbConfig.getAccountSchema(), accountDbConfig.getUserPropertyNames());
  }

  // -------------------------------------------------------------------------
  // methods
  // -------------------------------------------------------------------------

  public User createUser(String email,
      Map<String, String> profileProperties,
      Map<String, String> globalPreferences,
      Map<String, String> projectPreferences)
          throws WdkModelException, InvalidUsernameOrEmailException {
    String dontEmailProp = _wdkModel.getProperties().get("DONT_EMAIL_NEW_USER");
    boolean sendWelcomeEmail = (dontEmailProp == null || !dontEmailProp.equals("true"));
    return createUser(email, profileProperties, globalPreferences, projectPreferences, true, sendWelcomeEmail);
  }

  public User createUser(String email,
      Map<String, String> profileProperties,
      Map<String, String> globalPreferences,
      Map<String, String> projectPreferences,
      boolean addUserDbReference, boolean sendWelcomeEmail)
          throws WdkModelException, InvalidUsernameOrEmailException {
    try {
      // check email for uniqueness and format
      email = validateAndFormatEmail(email, _accountManager);

      // if user supplied a username, make sure it is unique
      if (profileProperties.containsKey(AccountManager.USERNAME_PROPERTY_KEY)) {
        String username = profileProperties.get(AccountManager.USERNAME_PROPERTY_KEY);
        // check whether the username exists in the database already; if so, the operation fails
        if (_accountManager.getUserProfileByUsername(username) != null) {
          throw new InvalidUsernameOrEmailException("The username '" + username + "' is already in use. " + "Please choose another one.");
        }
      }

      // generate temporary password for user
      String password = generateTemporaryPassword();

      // add user to account DB
      UserProfile profile = _accountManager.createAccount(email, password, profileProperties);

      // add user to this user DB (will be added to other user DBs as needed during login)
      if (addUserDbReference) {
        addUserReference(profile.getUserId(), false);
      }

      // create new user object and add profile properties
      RegisteredUser user = new RegisteredUser(_wdkModel, profile.getUserId(),
          profile.getEmail(), profile.getSignature(), profile.getStableId());
      user.setProfileProperties(profile.getProperties());

      // set and save preferences
      UserPreferences prefs = new UserPreferences();
      if (globalPreferences != null) prefs.setGlobalPreferences(globalPreferences);
      if (projectPreferences != null) prefs.setProjectPreferences(projectPreferences);
      user.setPreferences(prefs);
      _preferenceFactory.savePreferences(user);

      // if needed, send user temporary password via email
      if (sendWelcomeEmail) {
        emailTemporaryPassword(user, password, _wdkModel.getModelConfig());
      }

      return user;
    }
    catch (WdkModelException | InvalidUsernameOrEmailException e) {
      // do not wrap known exceptions
      throw e;
    }
    catch (Exception e) {
      // wrap unknown exceptions with WdkModelException
      throw new WdkModelException("Could not completely create new user", e);
    }
  }

  public void savePreferences(User user) throws WdkModelException {
    _preferenceFactory.savePreferences(user);
  }

  private void addUserReference(Long userId, boolean isGuest) {
    Timestamp insertedOn = new Timestamp(new Date().getTime());
    String sql = INSERT_USER_REF_SQL
        .replace(USER_SCHEMA_MACRO, _userSchema)
        .replace(IS_GUEST_VALUE_MACRO, _userDb.getPlatform().convertBoolean(isGuest).toString());
    new SQLRunner(_userDb.getDataSource(), sql, "insert-user-ref")
      .executeStatement(new Object[]{ userId, insertedOn }, INSERT_USER_REF_PARAM_TYPES);
  }

  private boolean hasUserReference(long userId) {
    String sql = COUNT_USER_REF_BY_ID_SQL.replace(USER_SCHEMA_MACRO, _userSchema);
    SingleLongResultSetHandler handler = new SingleLongResultSetHandler();
    new SQLRunner(_userDb.getDataSource(), sql, "check-user-ref")
      .executeQuery(new Object[]{ userId }, COUNT_USER_REF_BY_ID_PARAM_TYPES, handler);
    if (handler.getStatus().equals(Status.NON_NULL_VALUE)) {
      switch (handler.getRetrievedValue().intValue()) {
        case 0: return false;
        case 1: return true;
        default: throw new IllegalStateException("More than one user reference in userDb for user " + userId);
      }
    }
    throw new WdkRuntimeException("User reference count query did not return count for user id " +
        userId + ". Status = " + handler.getStatus() + ", sql = " + sql);
  }

  private Date getGuestUserRefFirstAccess(long userId) {
    String sql = SELECT_GUEST_USER_REF_BY_ID_SQL
        .replace(USER_SCHEMA_MACRO, _userSchema)
        .replace(IS_GUEST_VALUE_MACRO, _userDb.getPlatform().convertBoolean(true).toString());
    return new SQLRunner(_userDb.getDataSource(), sql, "get-guest-user-ref")
      .executeQuery(new Object[]{ userId }, SELECT_GUEST_USER_REF_BY_ID_PARAM_TYPES, rs ->
          !rs.next() ? null : new Date(rs.getTimestamp(COL_FIRST_ACCESS).getTime()));
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

  private static String validateAndFormatEmail(String email, AccountManager accountMgr) throws InvalidUsernameOrEmailException {
    // trim and validate passed email address and extract stable name
    if (email == null)
      throw new InvalidUsernameOrEmailException("The user's email cannot be empty.");
    // format the info
    email = AccountManager.trimAndLowercase(email);
    if (email.isEmpty())
      throw new InvalidUsernameOrEmailException("The user's email cannot be empty.");
    int atSignIndex = email.indexOf("@");
    if (atSignIndex < 1) // must be present and not the first char
      throw new InvalidUsernameOrEmailException("The user's email address is invalid.");
    // check whether the user exist in the database already; if email exists, the operation fails
    if (accountMgr.getUserProfileByEmail(email) != null)
      throw new InvalidUsernameOrEmailException("The email '" + email + "' has already been registered. " + "Please choose another one.");
    return email;
  }

  private static String generateTemporaryPassword() {
    // generate a random password of 8 characters long, the range will be
    // [0-9A-Za-z]
    StringBuilder buffer = new StringBuilder();
    Random rand = new Random();
    for (int i = 0; i < 8; i++) {
      int value = rand.nextInt(36);
      if (value < 10) { // number
        buffer.append(value);
      } else { // lower case letters
        buffer.append((char) ('a' + value - 10));
      }
    }
    return buffer.toString();
  }

  /**
   * Create an unregistered user of the specified type and persist in the database
   * 
   * @param userType unregistered user type to persist
   * @return new unregistered user with remaining fields populated
   * @throws WdkRuntimeException if unable to persist temporary user
   */
  public UnregisteredUser createUnregistedUser(UnregisteredUserType userType) throws WdkRuntimeException {
    try {
      UserProfile profile = _accountManager.createGuestAccount(userType.getStableIdPrefix());
      addUserReference(profile.getUserId(), true);
      return new UnregisteredUser(_wdkModel, profile.getUserId(), profile.getEmail(), profile.getSignature(), profile.getStableId());
    }
    catch (Exception e) {
      throw new WdkRuntimeException("Unable to save temporary user", e);
    }
  }

  private User completeLogin(User user) {
    if (user == null)
      return user;

    // make sure user has reference in this user DB (needs to happen before merging)
    if (!hasUserReference(user.getUserId())) {
      addUserReference(user.getUserId(), false);
    }

    // update user active timestamp
    _accountManager.updateLastLogin(user.getUserId());

    return user;
  }

  public User login(User guest, String email, String password)
      throws WdkUserException, WdkModelException {
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

  private User authenticate(String usernameOrEmail, String password) throws WdkModelException {
    return populateRegisteredUser(_accountManager.getUserProfile(usernameOrEmail, password));
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
      return Optional.of(populateRegisteredUser(profile));
    }
    else {
      // cannot find user in account DB; however, the passed ID may represent a guest local to this userDb
      Date accessDate = getGuestUserRefFirstAccess(userId);
      if (accessDate != null) {
        // guest user was found in local user Db; create UnregisteredUser and populate
        profile = AccountManager.createGuestProfile(UnregisteredUserType.GUEST.getStableIdPrefix(), userId, accessDate);
        return Optional.of(new UnregisteredUser(_wdkModel, profile.getUserId(),
            profile.getEmail(), profile.getSignature(), profile.getStableId()));
        
      }
      else {
        // user does not exist in account or user DBs
        return Optional.empty();
      }
    }
  }

  public User getUserByEmail(String email) throws WdkModelException {
    return populateRegisteredUser(_accountManager.getUserProfileByEmail(email));
  }

  private User getUserProfileByUsernameOrEmail(String usernameOrEmail) throws WdkModelException {
    return populateRegisteredUser(_accountManager.getUserProfileByUsernameOrEmail(usernameOrEmail));
  }

  public User getUserBySignature(String signature) throws WdkModelException, WdkUserException {
    User user = populateRegisteredUser(_accountManager.getUserProfileBySignature(signature));
    if (user == null) {
      // signature is rarely sent in by user; if User cannot be found, it's probably an error
      throw new WdkUserException("Unable to find user with signature: " + signature);
    }
    return user;
  }

  private User populateRegisteredUser(UserProfile profile) throws WdkModelException {
    if (profile == null) return null;
    User user = new RegisteredUser(_wdkModel, profile.getUserId(), profile.getEmail(),
        profile.getSignature(), profile.getStableId());
    user.setProfileProperties(profile.getProperties());
    user.setPreferences(_preferenceFactory.getPreferences(user));
    return user;
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
      _accountManager.saveUserProfile(user.getUserId(), user.getEmail(), user.getProfileProperties());

      // get updated profile and trigger profile update event
      UserProfile newProfile = _accountManager.getUserProfile(user.getUserId());
      Events.trigger(new UserProfileUpdateEvent(oldProfile, newProfile, _wdkModel));

      // save preferences
      _preferenceFactory.savePreferences(user);
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

  public void changePassword(long userId, String newPassword) {
    _accountManager.updatePassword(userId, newPassword);
  }
}
