package org.gusdb.wdk.model.user.analysis;

public class IllegalAnswerValueException extends Exception {

  private static final long serialVersionUID = 1L;

  public IllegalAnswerValueException(String message) {
    super(message);
  }
  
  public IllegalAnswerValueException(String message, Exception cause) {
    super(message, cause);
  }
  
}
