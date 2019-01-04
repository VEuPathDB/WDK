package org.gusdb.wdk.events;

import javax.servlet.http.HttpSession;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.user.User;

public class NewUserEvent extends Event {

  private final User _newUser;
  private final User _oldUser;
  private final HttpSession _session;

  public NewUserEvent(User newUser, User oldUser, HttpSession session) {
    _newUser = newUser;
    _oldUser = oldUser;
    _session = session;
  }

  public User getNewUser() {
    return _newUser;
  }

  public User getOldUser() {
    return _oldUser;
  }

  public HttpSession getSession() {
    return _session;
  }
}
