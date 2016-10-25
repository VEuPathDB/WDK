package org.gusdb.wdk.service.request.exception;

import org.gusdb.wdk.model.WdkUserException;

public class RequestMisformatException extends WdkUserException {

  private static final long serialVersionUID = 1L;

  public RequestMisformatException(String message) {
    super(message);
  }

  public RequestMisformatException(String message, Exception cause) {
    super(message, cause);
  }
}
