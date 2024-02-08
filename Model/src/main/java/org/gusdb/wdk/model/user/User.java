package org.gusdb.wdk.model.user;

import java.util.Collection;

import org.gusdb.oauth2.client.veupathdb.UserProperty;
import org.gusdb.wdk.model.WdkModel;

public interface User extends org.gusdb.oauth2.client.veupathdb.User {

  static Collection<UserProperty> getPropertyDefs() {
    return BasicUser.USER_PROPERTIES.values();
  }

  WdkModel getWdkModel();

}
