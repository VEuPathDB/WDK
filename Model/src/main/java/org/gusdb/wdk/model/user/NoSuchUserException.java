package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModelException;

public class NoSuchUserException extends WdkModelException {

  private static final long serialVersionUID = 1L;

  public NoSuchUserException(String message) {
    super(message);
  }

}
