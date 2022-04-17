package org.gusdb.wsf.plugin;

/**
 * @author Jerric
 */
public class PluginModelException extends Exception {

  private static final long serialVersionUID = 2;

  public PluginModelException() {
    super();
  }

  public PluginModelException(String message) {
    super(message);
  }

  public PluginModelException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginModelException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  public PluginModelException(String message, Throwable cause,
    boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
