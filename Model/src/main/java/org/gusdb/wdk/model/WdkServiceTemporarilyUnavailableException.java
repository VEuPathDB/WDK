package org.gusdb.wdk.model;

public class WdkServiceTemporarilyUnavailableException extends WdkRuntimeException {

  public WdkServiceTemporarilyUnavailableException(String message) {
    super(message);
  }

  public WdkServiceTemporarilyUnavailableException(String message, Exception cause) {
    super(message, cause);
  }

}
