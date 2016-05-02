package org.gusdb.wdk.service.request;

/**
 * Custom service exception to be thrown when the JSON received is syntactically 
 * correct but problems the content provided conflicts with existing content.  The
 * default message is HTTP 409 Conflict.
 * @author crisl-adm
 *
 */
public class ConflictException extends Exception {

  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_MESSAGE = "HTTP 409 Conflict";

  /** 
   * No arg constructor
   * Passing default message to superclass
   */
  public ConflictException() {
    super(DEFAULT_MESSAGE);
  }

  /**
   * Passing the message into the superclass
   * @param message
   */
  public ConflictException(String message) {
      super(message);
  }
  
  /**
   * Passing the throwable into the superclass
   * and applying the default message 
   * @param t
   */
  public ConflictException(Throwable t) {
    super(DEFAULT_MESSAGE, t);
  }
  
  /**
   * Passing both a message and the throwable into the superclass
   * @param message
   * @param t
   */
  public ConflictException(String message, Throwable t) {
    super(message, t);
  }
}
