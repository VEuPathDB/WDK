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

  public static WdkModelException translateFrom(Exception e) {
    return translateFrom(e, e.getMessage());
  }

  public static WdkModelException translateFrom(Exception e, String newMessage) {
    // if passed message is null, use name of original exception
    if (newMessage == null) newMessage = e.getClass().getSimpleName();
    // if exception is already a WdkModelException, simply return
    if (e instanceof WdkModelException) return (WdkModelException)e;
    // check underlying exception; a WdkModelException may simply have been wrapped
    Throwable t = (e.getCause() != null ? e.getCause() : e);
    // if underlying exception is WdkModelException then return, else wrap
    return (t instanceof WdkModelException ? (WdkModelException)t : new WdkModelException(newMessage, t));
  }

  public static <T> T unwrap(Exception e) throws WdkModelException {
    throw translateFrom(e, e.getMessage());
  }

  public static <T> T unwrap(Exception e, String newMessage) throws WdkModelException {
    throw translateFrom(e, newMessage);
  }

}
