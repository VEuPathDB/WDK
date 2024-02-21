package org.gusdb.wdk.model.user;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.events.Events;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.exception.InvalidPropertiesException;
import org.gusdb.wdk.events.UserProfileUpdateEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.session.WdkOAuthClientWrapper;

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
  private final String _userSchema;
  private final WdkOAuthClientWrapper _client;

  // -------------------------------------------------------------------------
  // constructor
  // -------------------------------------------------------------------------

  public UserFactory(WdkModel wdkModel) {
    // save model for populating new users
    _wdkModel = wdkModel;
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    _client = new WdkOAuthClientWrapper(wdkModel);
  }

  // -------------------------------------------------------------------------
  // methods
  // -------------------------------------------------------------------------

  public User createUser(String email, Map<String, String> profileProperties)
      throws WdkModelException, InvalidPropertiesException, InvalidUsernameOrEmailException {

    // contact OAuth server to create a new user with the passed props
    TwoTuple<User,String> userTuple = _client.createUser(email, profileProperties);
    User user = userTuple.getFirst();

    // add user to this user DB (will be added to other user DBs as needed during login)
    addUserReference(user);

    // if needed, send user temporary password via email
    if (isSendWelcomeEmail()) {
      emailTemporaryPassword(user, userTuple.getSecond(), _wdkModel.getModelConfig());
    }

    return user;
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
          .replace(IS_GUEST_VALUE_MACRO, _wdkModel.getUserDb().getPlatform().convertBoolean(isGuest).toString());
      return new SQLRunner(_wdkModel.getUserDb().getDataSource(), sql, "insert-user-ref")
        .executeUpdate(new Object[]{ userId, userId, insertedOn }, INSERT_USER_REF_PARAM_TYPES);
    }
    catch (SQLRunnerException e) {
      throw WdkModelException.translateFrom(e);
    }
  }

  // FIXME: see if this is actually needed anywhere?  E.g. do we ever need to look up user refs by user ID to find last login?
  private Optional<UserReference> getUserReference(long userId) throws WdkModelException {
    try {
      String sql = SELECT_USER_REF_BY_ID_SQL.replace(USER_SCHEMA_MACRO, _userSchema);
      return new SQLRunner(_wdkModel.getUserDb().getDataSource(), sql, "get-user-ref").executeQuery(
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

  public Map<Long, Boolean> verifyUserids(Set<Long> userIds) {
    Map<Long, User> userMap = _client.getUsersById(new ArrayList<>(userIds));
    return userIds.stream().collect(Collectors.toMap(id -> id, id -> userMap.get(id) != null));
  }

  /**
   * Returns user by user ID, or an empty optional if not found
   * 
   * @param userId user ID
   * @return user user object for the passed ID
   * @throws WdkModelException if an error occurs in the attempt
   */
  public Optional<User> getUserById(long userId) throws WdkModelException {
    return Optional.ofNullable(_client.getUsersById(List.of(userId)).get(userId));
  }

  public Optional<User> getUserByEmail(String email) {
    return Optional.ofNullable(_client.getUsersByEmail(List.of(email)).get(email));
  }

  /**
   * Save the basic information of a user
   * 
   * @param user
   * @param newUser 
   * @throws InvalidPropertiesException 
   */
  public User saveUser(User oldUser, User newUser, ValidatedToken authorizationToken) throws InvalidUsernameOrEmailException, InvalidPropertiesException {
    User savedUser = new BasicUser(_wdkModel, _client.updateUser(newUser, authorizationToken));
    Events.trigger(new UserProfileUpdateEvent(oldUser, newUser, _wdkModel));
    return savedUser;
  }

  public void resetPassword(String loginName) throws InvalidUsernameOrEmailException, WdkModelException {
    TwoTuple<User, String> user = _client.resetPassword(loginName);

    // email user new password
    emailTemporaryPassword(user.getFirst(), user.getSecond(), _wdkModel.getModelConfig());
  }

}
