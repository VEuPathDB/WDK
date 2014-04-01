package org.gusdb.wdk.model.user.analysis;

public class IllegalStepException extends Exception {

  private static final long serialVersionUID = 1L;

  public IllegalStepException(String message) {
    super(message);
  }
  
  public IllegalStepException(String message, Exception cause) {
    super(message, cause);
  }
  
}
