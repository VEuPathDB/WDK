package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.oauth2.client.veupathdb.UserInfo;
import org.gusdb.wdk.model.WdkModel;

public class UserProfileUpdateEvent extends Event {

  private final UserInfo _oldUser;
  private final UserInfo _newUser;
  private final WdkModel _wdkModel;

  public UserProfileUpdateEvent(UserInfo oldUser, UserInfo newUser, WdkModel wdkModel) {
    _oldUser = oldUser;
    _newUser = newUser;
    _wdkModel = wdkModel;
  }

  public UserInfo getOldUser() {
    return _oldUser;
  }

  public UserInfo getNewUser() {
    return _newUser;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }
}
