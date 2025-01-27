package org.gusdb.wdk.model.user;

import java.util.Optional;

import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.model.WdkModel;

public interface User extends org.gusdb.oauth2.client.veupathdb.User {

  WdkModel getWdkModel();

  default String getRegistrationStatus() {
    return isGuest() ? "guest" : "registered";
  }

  // override if subclass supports token retention
  default Optional<ValidatedToken> getUserToken() {
    return Optional.empty();
  }
}
