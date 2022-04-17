package org.gusdb.wsf.client;

/**
 * @author Jerric
 */
public class ClientUserException extends Exception {

  private static final long serialVersionUID = 2L;

  public ClientUserException() {
    super();
  }

  public ClientUserException(String message) {
    super(message);
  }

  public ClientUserException(Throwable cause) {
    super(cause);
  }

  public ClientUserException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClientUserException(String message, Throwable cause,
    boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
