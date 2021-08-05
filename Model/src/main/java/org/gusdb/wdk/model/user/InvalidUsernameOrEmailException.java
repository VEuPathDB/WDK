package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkUserException;

public class InvalidUsernameOrEmailException extends WdkUserException {

  private static final long serialVersionUID = 1L;

  public InvalidUsernameOrEmailException(String message) {
    super(message);
  }

}
