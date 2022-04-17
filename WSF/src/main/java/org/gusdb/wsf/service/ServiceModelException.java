package org.gusdb.wsf.service;

public class ServiceModelException extends Exception {

  private static final long serialVersionUID = -9161875379915097906L;

  public ServiceModelException() {
  }

  public ServiceModelException(String message) {
    super(message);
  }

  public ServiceModelException(Throwable cause) {
    super(cause);
  }

  public ServiceModelException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceModelException(String message, Throwable cause,
    boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
