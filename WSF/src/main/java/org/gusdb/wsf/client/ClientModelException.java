package org.gusdb.wsf.client;


/**
 * @author Jerric
 */
public class ClientModelException extends Exception {

  private static final long serialVersionUID = 2L;

  public ClientModelException() {
    super();
  }

  public ClientModelException(String message) {
    super(message);
  }

  public ClientModelException(Throwable cause) {
    super(cause);
  }

  public ClientModelException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClientModelException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
