package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;

public class UnregisteredUser extends User {

  // -------------------------------------------------------------------------
  //  types of unregistered users
  // -------------------------------------------------------------------------

  public enum UnregisteredUserType {
    GUEST("WDK_GUEST_"),
    SYSTEM("WDK_GUEST_SYSTEM_");
    
    private final String _stableIdPrefix;

    private UnregisteredUserType(String stableIdPrefix) {
      _stableIdPrefix = stableIdPrefix;
    }
    
    public String getStableIdPrefix() {
      return _stableIdPrefix;
    }
  }

  UnregisteredUser(WdkModel wdkModel, long userId, String email, String signature, String stableId) {
    super(wdkModel, userId, email, signature, stableId);
  }

  @Override
  public boolean isGuest() {
    return true;
  }

  @Override
  public String getDisplayName() {
    return "WDK Guest";
  }
}
