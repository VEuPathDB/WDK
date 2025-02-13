package org.gusdb.wdk.model.user;

import java.util.HashMap;
import java.util.List;

import org.gusdb.oauth2.client.veupathdb.UserInfo;
import org.gusdb.wdk.model.OwnedObject;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

public class UserInfoCache extends HashMap<Long,UserInfo> {

  private static final long serialVersionUID = 1L;

  private final UserFactory _userFactory;

  public UserInfoCache(UserFactory userFactory) {
    _userFactory = userFactory;
  }

  public UserInfoCache(UserFactory userFactory, UserInfo seedUser) {
    this(userFactory);
    put(seedUser.getUserId(), seedUser);
  }

  public UserInfoCache(WdkModel wdkModel, OwnedObject seedUserOwner) {
    this(wdkModel.getUserFactory(), seedUserOwner.getOwningUser());
  }

  /**
   * Creates a user cache with an initial value.
   *
   * @param user a user to place in the cache.
   */
  public UserInfoCache(User user) {
    this(user.getWdkModel().getUserFactory(), user);
  }

  public void loadUsersByIds(List<Long> userIds) {
    putAll(_userFactory.getUsersById(userIds));
  }

  @Override
  public UserInfo get(Object id) {
    try {
      if (id == null) {
        throw new WdkRuntimeException("User ID cannot be null.");
      }
      if (!(id instanceof Long)) {
        throw new IllegalArgumentException("Only Long objects should be passed to this method, not " + id.getClass().getName());
      }
      Long userId = (Long)id;
      if (!containsKey(userId)) {
        synchronized(this) {
          put(userId, _userFactory.getUserById(userId)
              .orElseThrow(() -> new WdkRuntimeException("User with ID " + userId + " does not exist.")));
        }
      }
      return super.get(userId);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to execute user lookup query.", e);
    }
  }
}