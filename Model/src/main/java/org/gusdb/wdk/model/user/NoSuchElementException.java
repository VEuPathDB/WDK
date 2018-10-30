package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkUserException;

public class NoSuchElementException extends WdkUserException {

  private static final long serialVersionUID = 1L;

  public NoSuchElementException(String message) {
    super(message);
  }

}
