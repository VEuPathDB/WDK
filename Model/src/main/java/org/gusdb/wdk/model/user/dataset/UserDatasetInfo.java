package org.gusdb.wdk.model.user.dataset;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModel;
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

  private final WdkModel _wdkModel;
  private final UserDataset _userDataset;
  private final boolean _isInstalled;
  private final User _owner;
  private final long _ownerQuota;
  private final List<String> _relevantQuestionNames;
  private final List<UserDatasetShareUser> _shares;
  private final UserDatasetTypeHandler _handler;
  private JsonType _typeSpecificData;
  private JsonType _detailedTypeSpecificData;
  
  public UserDatasetInfo(UserDataset dataset, boolean isInstalled, UserDatasetStore store,
      UserDatasetSession session, final UserFactory userFactory, WdkModel wdkModel) {
    try {
      long ownerId = dataset.getOwnerId();
      final Map<Long,User> userCache = new HashMap<>();
      _wdkModel = wdkModel;
      _userDataset = dataset;
      _isInstalled = isInstalled;
      _owner = getUser(userCache, ownerId, userFactory);
      _ownerQuota = session.getQuota(ownerId);
      _relevantQuestionNames = Arrays.asList(store.getTypeHandler(dataset.getType()).getRelevantQuestionNames());
      _shares = mapToList(session.getSharedWith(ownerId, dataset.getUserDatasetId()), share ->
          new UserDatasetShareUser(getUser(userCache, share.getUserId(), userFactory), share.getTimeShared()));
      _handler = store.getTypeHandler(dataset.getType());
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Could not collect user dataset information for dataset ID " + dataset.getUserDatasetId(), e);
    }
  }

  public void loadDetailedTypeSpecificData(User user) throws WdkModelException {
	if(_isInstalled) {  
      _detailedTypeSpecificData = _handler.getDetailedTypeSpecificData(_wdkModel, _userDataset, user);
	}
  }

  public void setTypeSpecificData(JsonType typeSpecificData) {
    _typeSpecificData = typeSpecificData;
  }

  private static User getUser(Map<Long, User> userCache, long userId, UserFactory userFactory) {
    try {
      User user = userCache.get(userId);
      if (user == null) {
        user = userFactory.getUserById(userId);
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

  public long getOwnerQuota() {
    return _ownerQuota;
  }
  
  public String getTypeDisplay() {
	return _handler.getDisplay();
  }

  public List<String> getRelevantQuestionNames() {
    return _relevantQuestionNames;
  }

  public JsonType getTrackSpecificData() {
    return _typeSpecificData;
  }

  public JsonType getDetailedTrackSpecificData() {
    return _detailedTypeSpecificData;
  }

  public List<UserDatasetShareUser> getShares() {
    return _shares;
  }
  
  public UserDatasetCompatibility getUserDatasetCompatibility() throws WdkModelException {
	return _userDataset.getProjects().contains(_wdkModel.getProjectId()) ?
     _handler.getCompatibility(_userDataset, _wdkModel.getAppDb().getDataSource()) : null;
  }

  public class UserDatasetShareUser extends TwoTuple<User,Long> implements UserDatasetShare {

    public UserDatasetShareUser(User user, Long timeShared) {
      super(user, timeShared);
    }

    public User getUser() {
      return getFirst();
    }

    @Override
    public Long getUserId() {
      return getUser().getUserId();
    }

    @Override
    public Long getTimeShared() {
      return getSecond();
    }
    
  }
}
