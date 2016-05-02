package org.gusdb.wdk.service.request;

/**
 * Custom service exception to be thrown when the JSON received is syntactically 
 * correct but problems with the content preclude the successful completion of
 * the web service.
 * @author crisl-adm
 *
 */
public class DataValidationException extends Exception {

  private static final long serialVersionUID = 1L;

  /** No arg constructor */
  public DataValidationException() {
    super();
  }

  /**
   * Passing the message into the superclass
   * @param message
   */
  public DataValidationException(String message) {
      super(message);
  }
  
  /**
   * Passing the throwable into the superclass
   * @param t
   */
  public DataValidationException(Throwable t) {
    super(t);
  }
  
  /**
   * Passing both a message and the throwable into the superclass
   * @param message
   * @param t
   */
  public DataValidationException(String message, Throwable t) {
    super(message, t);
  }
}
