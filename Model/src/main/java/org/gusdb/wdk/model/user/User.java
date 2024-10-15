package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;

public interface User extends org.gusdb.oauth2.client.veupathdb.User {

  WdkModel getWdkModel();

  default String getRegistrationStatus() {
    return isGuest() ? "guest" : "registered";
  }
}
