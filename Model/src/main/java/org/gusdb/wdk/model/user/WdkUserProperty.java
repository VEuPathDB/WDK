package org.gusdb.wdk.model.user;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.gusdb.fgputil.accountdb.UserPropertyName;

/**
 * Convenience class to work around accountdb API and interface with WDK User class
 */
public class WdkUserProperty extends UserPropertyName {

  private final Function<User,String> _getter;
  private final BiConsumer<User,String> _setter;

  public WdkUserProperty(String name, String displayName, String dbKey, boolean isRequired, boolean isPublic, boolean isMultiLine, Function<User,String> getter, BiConsumer<User,String> setter) {
    UserPropertyName userProp = new UserPropertyName(name, dbKey, isRequired);
    userProp.setDisplayName(displayName);
    userProp.setPublic(isPublic);
    userProp.setMultiLine(isMultiLine);
    _getter = getter;
    _setter = setter;
  }

  public String getValue(User user) {
    return _getter.apply(user);
  }

  public void setValue(User user, String value) {
    _setter.accept(user, value);
  }
}
