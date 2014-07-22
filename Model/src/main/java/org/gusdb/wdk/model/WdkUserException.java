package org.gusdb.wdk.model;

import java.util.Map;

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
  Map<String, String> paramErrors = null;

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
  
  public WdkUserException(Map<String, String> paramErrors) {
    super();
    this.paramErrors = paramErrors;
  }

  public WdkUserException(String message, Map<String, String> paramErrors) {
    super(message);
    this.paramErrors = paramErrors;
  }

  public WdkUserException(Throwable cause, Map<String, String> paramErrors) {
    super(cause);
    this.paramErrors = paramErrors;
  }

  public WdkUserException(String message, Throwable cause,
      Map<String, String> paramErrors) {
    super(message, cause);
    this.paramErrors = paramErrors;
  }

  /**
   * @return Map where keys are Params and values are an tuple of (value,
   *         errMsg), one for each error param value
   */
  public Map<String, String> getParamErrors() {
    return paramErrors;
  }

  /**
   * @return A default formatting of contained errors
   */
  @Override
  public String formatErrors() {

    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer(super.formatErrors());
    buf.append(newline);
    if (paramErrors != null) {
      for (String paramPrompt : paramErrors.keySet()) {
        String details = paramErrors.get(paramPrompt);
        buf.append(paramPrompt + ": " + details + newline);
      }
    }
    return buf.toString();
  }

}
