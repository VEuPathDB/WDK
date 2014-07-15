package org.gusdb.wdk.model;

public class WdkIllegalArgumentException extends WdkModelException {

  private static final long serialVersionUID = 1L;

  public WdkIllegalArgumentException(String message) {
    super(message);
  }

  public WdkIllegalArgumentException(String message, Exception cause) {
    super(message, cause);
  }

}
