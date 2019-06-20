package org.gusdb.wdk.service.request.exception;

/**
 * Custom service exception to be thrown when the JSON received is syntactically
 * correct but problems with the content preclude the successful completion of
 * the web service.
 *
 * @author crisl-adm
 */
public class DataValidationException extends Exception {

  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_MESSAGE = "HTTP 422 Unprocessable Entity";

  /**
   * No arg constructor
   * Passing default message to superclass
   */
  public DataValidationException() {
    super(DEFAULT_MESSAGE);
  }

  /**
   * Passing the message into the superclass
   */
  public DataValidationException(String message) {
      super(message);
  }

  /**
   * Passing the throwable into the superclass
   * and applying the default message
   */
  public DataValidationException(Throwable t) {
    super(DEFAULT_MESSAGE, t);
  }

  /**
   * Passing both a message and the throwable into the superclass
   */
  public DataValidationException(String message, Throwable t) {
    super(message, t);
  }
}
