package org.gusdb.wsf.plugin;



/**
 * @author Cristina
 * @since Feb 21, 2017
 */
public class PluginTimeoutException extends PluginModelException {

  private static final long serialVersionUID = 2;

  public PluginTimeoutException() {
    super();
  }

  public PluginTimeoutException(String message) {
    super(message);
  }

  public PluginTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginTimeoutException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  public PluginTimeoutException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
