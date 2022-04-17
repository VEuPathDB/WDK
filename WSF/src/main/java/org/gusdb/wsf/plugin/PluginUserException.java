package org.gusdb.wsf.plugin;

/**
 * @author Jerric
 */
public class PluginUserException extends Exception {

  private static final long serialVersionUID = 2;

  public PluginUserException() {
  }

  public PluginUserException(String message) {
    super(message);
  }

  public PluginUserException(Throwable cause) {
    super(cause);
  }

  public PluginUserException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginUserException(String message, Throwable cause,
    boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
