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

  public static void unwrap(Exception e) throws WdkModelException {
    Throwable t = (e.getCause() != null ? e.getCause() : e);
    throw (t instanceof WdkModelException ? (WdkModelException)t : new WdkModelException(t));
  }

  public static void unwrap(Exception e, String newMessage) throws WdkModelException {
    Throwable t = (e.getCause() != null ? e.getCause() : e);
    throw (t instanceof WdkModelException ? (WdkModelException)t : new WdkModelException(newMessage, t));
  }

  /**
   * @param returnClass class this method returns
   */
  public static <T> T unwrap(Exception e, Class<T> returnClass) throws WdkModelException {
    unwrap(e);
    return null;
  }

  /**
   * @param returnClass class this method returns
   */
  public static <T> T unwrap(Exception e, String newMessage, Class<T> returnClass) throws WdkModelException {
    unwrap(e, newMessage);
    return null;
  }
}
