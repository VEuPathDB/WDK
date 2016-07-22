package org.gusdb.wdk.service;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * Encapsulates information about the user of a requested user resource.  For example, if a request comes
 * in for /user/123, an instance of this class, given "123" will tell you the following:
 * 
 * String originalUserIdString: value used to generate this UserBundle (e.g. "123")
 * bool isValidUserId: if "123" is an integer corresponding to an existing user ID in the system
 * int parsedUserId: raw user ID after parsing (set to -1 if not valid)
 * User incomingUser: user behind the parsed user ID
 * bool isCurrentUser: if the user behind the parsed user ID is the same as the current user
 * User currentUser: a reference to the current user (same object as user if isCurrentUser == true)
 * 
 * @author rdoherty
 */
public class UserBundle {

  private static final Logger LOG = Logger.getLogger(UserBundle.class);

  public static final String CURRENT_USER_MAGIC_STRING = "current";

  private final String _incomingUserIdString;
  private final boolean _isValidUserId;
  private final int _parsedUserId;
  private final User _incomingUser;
  private final boolean _isCurrentUser;
  private final User _currentUser;

  public static UserBundle createFromId(String userIdStr, User currentUser, UserFactory userFactory) {
    try {
      if (CURRENT_USER_MAGIC_STRING.equals(userIdStr)) {
        return new UserBundle(CURRENT_USER_MAGIC_STRING, true, currentUser.getUserId(), currentUser, true, currentUser);
      }
      int userId = Integer.parseInt(userIdStr);
      if (userId == currentUser.getUserId()) {
        return new UserBundle(userIdStr, true, currentUser.getUserId(), currentUser, true, currentUser);
      }
      User user = userFactory.getUser(userId);
      return new UserBundle(userIdStr, true, user.getUserId(), user, false, currentUser);
    }
    catch (WdkModelException | NumberFormatException | NullPointerException e) {
      LOG.warn("User requested by ID that is misformatted or does not exist", e);
      // userIdStr is null or misformatted, or no user by the passed ID could be found
      return UserBundle.getBadIdBundle(userIdStr, currentUser);
    }
  }

  private UserBundle(String incomingUserIdString, boolean isValidUserId, int parsedUserId, User incomingUser, boolean isCurrentUser, User currentUser) {
    _incomingUserIdString = incomingUserIdString;
    _isValidUserId = isValidUserId;
    _parsedUserId = parsedUserId;
    _incomingUser = incomingUser;
    _isCurrentUser = isCurrentUser;
    _currentUser = currentUser;
  }

  private static UserBundle getBadIdBundle(String userIdStr, User currentUser) {
    return new UserBundle(userIdStr, false, -1, null, false, currentUser);
  }

  public String getIncomingUserIdString() {
    return _incomingUserIdString;
  }

  public boolean isValidUserId() {
    return _isValidUserId;
  }

  public int getParsedUserId() {
    return _parsedUserId;
  }

  public User getIncomingUser() {
    return _incomingUser;
  }

  public boolean isCurrentUser() {
    return _isCurrentUser;
  }

  public User getCurrentUser() {
    return _currentUser;
  }
}
