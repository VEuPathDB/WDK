package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.User;

public class UserProfileUpdateEvent extends Event {

  private final User _oldUser;
  private final User _newUser;
  private final WdkModel _wdkModel;

  public UserProfileUpdateEvent(User oldUser, User newUser, WdkModel wdkModel) {
    _oldUser = oldUser;
    _newUser = newUser;
    _wdkModel = wdkModel;
  }

  public User getOldUser() {
    return _oldUser;
  }

  public User getNewUser() {
    return _newUser;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }
}
