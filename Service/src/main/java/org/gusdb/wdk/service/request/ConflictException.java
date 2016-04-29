package org.gusdb.wdk.service.request;

/**
 * Custom service exception to be thrown when the JSON received is syntactically 
 * correct but problems the content provided conflicts with existing content.
 * @author crisl-adm
 *
 */
public class ConflictException extends Exception {

  private static final long serialVersionUID = 1L;

  /** No arg constructor */
  public ConflictException() {
    super();
  }

  /** Single arg constructor */
  public ConflictException(String message) {
      super(message);
  }
}
