package org.gusdb.wdk.model;

/**
 * This exception should be thrown out if the cause is not related to user's
 * input. For example, the cause of the exception can be a mistake in the model
 * file, the database resource is unavailable, etc.
 * 
 * @author jerric
 * 
 */
public class WdkModelException extends WdkException {

  private static final long serialVersionUID = 877548355767390313L;

  public WdkModelException() {
    super();
  }

  public WdkModelException(String msg) {
    super(msg);
  }

  public WdkModelException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public WdkModelException(Throwable cause) {
    super(cause);
  }

  public static <T> T unwrap(Exception e) throws WdkModelException {
    unwrap(e, e.getMessage());
    return null;
  }

  public static <T> T unwrap(Exception e, String newMessage) throws WdkModelException {
    Throwable t = (e.getCause() != null ? e.getCause() : e);
    throw (t instanceof WdkModelException ? (WdkModelException)t : new WdkModelException(newMessage, t));
  }

}
