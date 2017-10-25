package org.gusdb.wdk.model;

/**
 * This is a top level exception class, and all other WDK generated exceptions
 * should extend from this exception.
 * 
 * @author jerric
 * 
 */
public class WdkException extends Exception {

  private static final long serialVersionUID = 877548355767390313L;

  public WdkException() {
    super();
  }

  public WdkException(String msg) {
    super(msg);
  }

  public WdkException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public WdkException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

}
