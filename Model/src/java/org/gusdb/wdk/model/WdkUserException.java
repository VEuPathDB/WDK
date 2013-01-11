package org.gusdb.wdk.model;

/**
 * This exception is thrown when a user's action causes a failure of a function,
 * such as giving an invalid parameter, access the system with a hand coded url,
 * etc.
 * 
 * @author Jerric
 * @modified Jan 6, 2006
 */
public class WdkUserException extends WdkException {

  public static String modelName;

  /**
     * 
     */
  private static final long serialVersionUID = 442861349675564533L;

  public WdkUserException() {
    super();
  }

  public WdkUserException(String message) {
    super(message);
  }

  public WdkUserException(Throwable cause) {
    super(cause);
  }

  public WdkUserException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
