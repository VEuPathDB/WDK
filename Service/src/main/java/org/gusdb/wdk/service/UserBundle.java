package org.gusdb.wdk.service;

import org.apache.log4j.Logger;
import org.gusdb.oauth2.client.veupathdb.UserInfo;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.NoSuchElementException;
import org.gusdb.wdk.model.user.User;

/**
 * Encapsulates information about the users involved in this request.  The requesting user
 * is the user identified by the authentication token passed with the request and is created
 * by CheckLoginFilter.  The target user is the focus of the request, or the owner of the
 * targeted resource.  For example, if a request comes in for /user/123, an instance of this
 * class, given target user ID "123" will tell you the following:
 *
 * bool isValidTargetUserId: if "123" is an integer corresponding to an existing user ID in the system
 * UserInfo targetUser: user behind the parsed target user ID (null if isValidTargetUserId = false)
 * bool isTargetRequestingUser: whether the user behind the parsed target user ID is the same as the requesting user
 * User requestingUser: a reference to the session's current user (same object as targetUser if isTargetRequestingUser == true)
 *
 * @author rdoherty
 */
public class UserBundle {

  private static final Logger LOG = Logger.getLogger(UserBundle.class);

  public static final String USE_REQUEST_USER_MAGIC_STRING = "current";

  private final boolean _isValidTargetUserId;
  private final UserInfo _targetUser;
  private final boolean _isTargetRequestingUser;
  private final User _requestingUser;

  public static UserBundle createFromTargetId(String targetUserIdStr, User requestingUser) throws WdkModelException {
    try {
      long userId;
      if (USE_REQUEST_USER_MAGIC_STRING.equals(targetUserIdStr) ||
          (userId = Long.parseLong(targetUserIdStr)) == requestingUser.getUserId()) {
        return new UserBundle(true, requestingUser, true, requestingUser);
      }

      UserInfo targetUserInfo = requestingUser.getWdkModel().getUserFactory().getUserById(userId)
          .orElseThrow(() -> new NoSuchElementException("No user exists with ID " + userId));

      return new UserBundle(true, targetUserInfo, false, requestingUser);
    }
    catch (NoSuchElementException | NumberFormatException | NullPointerException e) {
      LOG.warn("User requested by ID that is misformatted or does not exist", e);
      // userIdStr is null or misformatted, or no user by the passed ID could be found
      return new UserBundle(false, null, false, requestingUser);
    }
  }

  private UserBundle(boolean isValidTargetUserId, UserInfo targetUser, boolean isTargetRequestingUser, User requestingUser) {
    _isValidTargetUserId = isValidTargetUserId;
    _targetUser = targetUser;
    _isTargetRequestingUser = isTargetRequestingUser;
    _requestingUser = requestingUser;
  }

  public boolean isValidTargetUserId() {
    return _isValidTargetUserId;
  }

  public UserInfo getTargetUser() {
    return _targetUser;
  }

  public boolean isTargetRequestingUser() {
    return _isTargetRequestingUser;
  }

  public User getRequestingUser() {
    return _requestingUser;
  }

}
