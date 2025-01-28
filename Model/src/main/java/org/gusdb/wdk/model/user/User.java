package org.gusdb.wdk.model.user;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.model.WdkModel;

public interface User extends org.gusdb.oauth2.client.veupathdb.User {

  static final Logger LOG = Logger.getLogger(User.class);

  WdkModel getWdkModel();

  default String getRegistrationStatus() {
    return isGuest() ? "guest" : "registered";
  }

  // override if subclass supports token retention
  default Optional<ValidatedToken> getUserToken() {
    LOG.info("What kind of user am I? " + getClass().getName());
    return Optional.empty();
  }
}
