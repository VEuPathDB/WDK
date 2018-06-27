package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;

public class GuestUser extends User {

  public static final String GUEST_USER_PREFIX = "WDK_GUEST_";
  public static final String SYSTEM_USER_PREFIX = GUEST_USER_PREFIX + "SYSTEM_";

  public static class SystemUser extends GuestUser {
    public SystemUser(WdkModel wdkModel) {
      super(wdkModel, SYSTEM_USER_PREFIX);
    }
  }

  private final String _emailPrefix;

  public GuestUser(WdkModel wdkModel) {
    this(wdkModel, GUEST_USER_PREFIX);
  }

  private GuestUser(WdkModel wdkModel, String emailPrefix) {
    super(wdkModel, 0, emailPrefix, null, null);
    _wdkModel = wdkModel;
    _emailPrefix = emailPrefix;
  }

  @Override
  public boolean isGuest() {
    return true;
  }

  @Override
  public String getDisplayName() {
    return "WDK Guest";
  }

  @Override
  protected void checkIfSaved() {
    if (_userId == 0) {
      synchronized(this) { // double check to avoid waiting unnecessarily
        if (_userId == 0) {
          // will assign id, full email, signature, and stable ID
          GuestUser savedUser = _wdkModel.getUserFactory().saveTemporaryUser(this);
          _userId = savedUser.getUserId();
          _email = savedUser.getEmail();
          _signature = savedUser.getSignature();
          _stableId = savedUser.getStableId();
        }
      }
    }
  }

  public String getEmailPrefix() {
    return _emailPrefix;
  }
}
