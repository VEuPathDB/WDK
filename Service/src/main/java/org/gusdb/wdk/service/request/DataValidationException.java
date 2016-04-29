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

  /** Single arg constructor */
  public DataValidationException(String message) {
      super(message);
  }
}
