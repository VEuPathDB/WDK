package org.gusdb.wdk.events;

import org.gusdb.fgputil.accountdb.UserProfile;
import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.WdkModel;

public class UserProfileUpdateEvent extends Event {

  private final UserProfile _oldProfile;
  private final UserProfile _newProfile;
  private final WdkModel _wdkModel;

  public UserProfileUpdateEvent(UserProfile oldProfile, UserProfile newProfile, WdkModel wdkModel) {
    _oldProfile = oldProfile;
    _newProfile = newProfile;
    _wdkModel = wdkModel;
  }

  public UserProfile getOldProfile() {
    return _oldProfile;
  }

  public UserProfile getNewProfile() {
    return _newProfile;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }
}
