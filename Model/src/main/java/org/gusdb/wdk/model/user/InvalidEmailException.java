package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkUserException;

public class InvalidEmailException extends WdkUserException {

  private static final long serialVersionUID = 1L;

  public InvalidEmailException(String message) {
    super(message);
  }

}
