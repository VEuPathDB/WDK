package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.user.User;

public class NewUserEvent extends Event {

  private final User _newUser;
  private final User _oldUser;

  public NewUserEvent(User newUser, User oldUser) {
    _newUser = newUser;
    _oldUser = oldUser;
  }

  public User getNewUser() {
    return _newUser;
  }

  public User getOldUser() {
    return _oldUser;
  }
}
