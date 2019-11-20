package org.gusdb.wdk.model.user;

import java.util.HashMap;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

public class UserCache extends HashMap<Long,User> {

  private static final long serialVersionUID = 1L;

  private final UserFactory _userFactory;

  public UserCache(UserFactory userFactory) {
    _userFactory = userFactory;
  }

  /**
   * Creates a user cache with an initial value.
   *
   * @param user a user to place in the cache.
   */
  public UserCache(User user) {
    put(user.getUserId(), user);
    _userFactory = null;
  }

  @Override
  public User get(Object id) {
    try {
      Long userId = (Long)id;
      if (userId == null) {
        throw new WdkRuntimeException("User ID cannot be null.");
      }
      if (!containsKey(userId)) {
        if (_userFactory != null) {
          put(userId, _userFactory.getUserById(userId)
              .orElseThrow(() -> new WdkRuntimeException("User with ID " + id + " does not exist.")));
        }
        else {
          throw new WdkRuntimeException("No-lookup cache does not contain the requested user (" + id + ").");
        }
      }
      return super.get(userId);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to execute user lookup query.", e);
    }
  }
}