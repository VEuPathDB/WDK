package org.gusdb.wdk.service.request;


public class RequestMisformatException extends Exception {

  private static final long serialVersionUID = 1L;

  public RequestMisformatException(String message) {
    super(message);
  }

  public RequestMisformatException(String message, Exception cause) {
    super(message, cause);
  }
}
