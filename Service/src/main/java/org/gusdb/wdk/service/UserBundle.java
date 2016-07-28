package org.gusdb.wdk.service;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.NoSuchUserException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * Encapsulates information about the user of a requested user resource.  For example, if a request comes
 * in for /user/123, an instance of this class, given target user ID "123" will tell you the following:
 * 
 * String targetUserIdString: value used to generate this UserBundle (e.g. "123")
 * bool isValidTargetUserId: if "123" is an integer corresponding to an existing user ID in the system
 * int parsedUserId: raw user ID after parsing (set to -1 if not valid)
 * User targetUser: user behind the parsed target user ID
 * bool isSessionUser: whether the user behind the parsed user ID is the same as the current user
 * User sessionUser: a reference to the session's current user (same object as targetUser if isSessionUser == true)
 * 
 * @author rdoherty
 */
public class UserBundle {

  private static final Logger LOG = Logger.getLogger(UserBundle.class);

  public static final String SESSSION_USER_MAGIC_STRING = "current";

  private final String _targetUserIdString;
  private final boolean _isValidUserId;
  private final User _targetUser;
  private final boolean _isSessionUser;
  private final User _sessionUser;

  public static UserBundle createFromTargetId(String userIdStr, User sessionUser, UserFactory userFactory) throws WdkModelException {
    try {
      if (SESSSION_USER_MAGIC_STRING.equals(userIdStr)) {
        return getSessionUserBundle(SESSSION_USER_MAGIC_STRING, sessionUser);
      }
      int userId = Integer.parseInt(userIdStr);
      if (userId == sessionUser.getUserId()) {
        return getSessionUserBundle(userIdStr, sessionUser);
      }
      User user = userFactory.getUser(userId);
      return new UserBundle(userIdStr, true, user, false, sessionUser);
    }
    catch (NoSuchUserException | NumberFormatException | NullPointerException e) {
      LOG.warn("User requested by ID that is misformatted or does not exist", e);
      // userIdStr is null or misformatted, or no user by the passed ID could be found
      return getBadIdBundle(userIdStr, sessionUser);
    }
  }

  private UserBundle(String targetUserIdString, boolean isValidUserId, User targetUser, boolean isSessionUser, User sessionUser) {
    _targetUserIdString = targetUserIdString;
    _isValidUserId = isValidUserId;
    _targetUser = targetUser;
    _isSessionUser = isSessionUser;
    _sessionUser = sessionUser;
  }

  private static UserBundle getSessionUserBundle(String userIdStr, User sessionUser) {
    return new UserBundle(userIdStr, true, sessionUser, true, sessionUser);
  }

  private static UserBundle getBadIdBundle(String userIdStr, User currentUser) {
    return new UserBundle(userIdStr, false, null, false, currentUser);
  }

  public String getTargetUserIdString() {
    return _targetUserIdString;
  }

  public boolean isValidUserId() {
    return _isValidUserId;
  }

  public User getTargetUser() {
    return _targetUser;
  }

  public boolean isSessionUser() {
    return _isSessionUser;
  }

  public User getSessionUser() {
    return _sessionUser;
  }
}
