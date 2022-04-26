package org.gusdb.wdk.model;

import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;

/**
 * This exception is thrown when a user's action causes a failure of a function,
 * such as giving an invalid parameter, access the system with a hand coded url,
 * etc.
 * 
 * @author Jerric
 */
public class WdkUserException extends WdkException {

  private static final long serialVersionUID = 442861349675564533L;

  private Map<String, String> paramErrors = null;

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

  @Override
  public String toString() {
    return super.toString() + (paramErrors == null ? "" :
      FormatUtil.NL + FormatUtil.prettyPrint(paramErrors, Style.MULTI_LINE));
  }

}
