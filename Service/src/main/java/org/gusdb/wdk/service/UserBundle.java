package org.gusdb.wdk.service;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

public class UserBundle {

  private static final Logger LOG = Logger.getLogger(UserBundle.class);

  public static final String CURRENT_USER_MAGIC_STRING = "current";

  private final boolean _isValidUserId;
  private final String _requestedUserId;
  private final int _userId;
  private final boolean _isCurrentUser;
  private final User _user;

  private UserBundle(int userId, boolean isCurrentUser, User user) {
    this(true, String.valueOf(userId), userId, isCurrentUser, user);
  }

  private UserBundle(boolean isValidUserId, String requestedUserId, int userId, boolean isCurrentUser, User user) {
    _isValidUserId = isValidUserId;
    _requestedUserId = requestedUserId;
    _userId = userId;
    _isCurrentUser = isCurrentUser;
    _user = user;
  }

  private static UserBundle getBadIdBundle(String userIdStr) {
    return new UserBundle(false, userIdStr, -1, false, null);
  }

  public boolean isValidUserId() {
    return _isValidUserId;
  }

  public String getRequestedUserId() {
    return _requestedUserId;
  }

  public int getUserId() {
    return _userId;
  }

  public boolean isCurrentUser() {
    return _isCurrentUser;
  }

  public User getUser() {
    return _user;
  }

  public static UserBundle createFromId(String userIdStr, User currentUser, UserFactory userFactory) {
    try {
      if (CURRENT_USER_MAGIC_STRING.equals(userIdStr)) {
        return new UserBundle(currentUser.getUserId(), true, currentUser);
      }
      int userId = Integer.parseInt(userIdStr);
      if (userId == currentUser.getUserId()) {
        return new UserBundle(currentUser.getUserId(), true, currentUser);
      }
      User user = userFactory.getUser(userId);
      return new UserBundle(userId, false, user);
    }
    catch (WdkModelException | NumberFormatException | NullPointerException e) {
      LOG.warn("User requested by ID that is misformatted or does not exist", e);
      // userIdStr is null or misformatted, or no user by the passed ID could be found
      return UserBundle.getBadIdBundle(userIdStr);
    }
  }
}
