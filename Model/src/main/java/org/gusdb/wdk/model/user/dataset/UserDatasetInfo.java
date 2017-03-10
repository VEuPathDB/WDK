package org.gusdb.wdk.model.user.dataset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * Aggregation of information about a user dataset
 * 
 * @author rdoherty
 */
public class UserDatasetInfo {

  private final UserDataset _userDataset;
  private final boolean _isInstalled;
  private final User _owner;
  private final int _ownerQuota;
  private final List<String> _relevantQuestionNames;
  private final List<UserDatasetShareUser> _shares;
  
  public UserDatasetInfo(UserDataset dataset, boolean isInstalled, UserDatasetStore store,
      final UserFactory userFactory) {
    try {
      int ownerId = dataset.getOwnerId();
      final Map<Integer,User> userCache = new HashMap<>();
      _userDataset = dataset;
      _isInstalled = isInstalled;
      _owner = getUser(userCache, ownerId, userFactory);
      _ownerQuota = store.getQuota(ownerId);
      _relevantQuestionNames = Arrays.asList(store.getTypeHandler(dataset.getType()).getRelevantQuestionNames());
      _shares = Functions.mapToList(store.getSharedWith(ownerId, dataset.getUserDatasetId()),
          new Function<UserDatasetShare,UserDatasetShareUser>() {
            @Override public UserDatasetShareUser apply(UserDatasetShare share) {
              return new UserDatasetShareUser(getUser(userCache, share.getUserId(), userFactory), share.getTimeShared());
            }});
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Could not collect user dataset information for dataset ID " + dataset.getUserDatasetId(), e);
    }
  }

  private static User getUser(Map<Integer, User> userCache, int userId, UserFactory userFactory) {
    try {
      User user = userCache.get(userId);
      if (user == null) {
        user = userFactory.getUser(userId);
        userCache.put(userId, user);
      }
      return user;
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Could not load user with ID " + userId + " from DB.", e);
    }
  }

  public UserDataset getDataset() {
    return _userDataset;
  }

  public boolean isInstalled() {
    return _isInstalled;
  }

  public User getOwner() {
    return _owner;
  }

  public int getOwnerQuota() {
    return _ownerQuota;
  }

  public List<String> getRelevantQuestionNames() {
    return _relevantQuestionNames;
  }

  public List<UserDatasetShareUser> getShares() {
    return _shares;
  }

  public class UserDatasetShareUser extends TwoTuple<User,Long> implements UserDatasetShare {

    public UserDatasetShareUser(User user, Long timeShared) {
      super(user, timeShared);
    }

    public User getUser() {
      return getFirst();
    }

    @Override
    public Integer getUserId() {
      try {
        return getUser().getUserId();
      }
      catch (WdkModelException e) {
        throw new WdkRuntimeException("Unable to fetch existing user's ID");
      }
    }

    @Override
    public Long getTimeShared() {
      return getSecond();
    }
  }
}
