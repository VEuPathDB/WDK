package org.gusdb.wsf.common;

import java.io.Serializable;

public class ResponseStatus implements Serializable {

  private static final long serialVersionUID = 1L;

  private int signal;

  private Exception exception;

  public void setSignal(int signal) {
    this.signal = signal;
  }

  /**
   * @return the signal
   */
  public int getSignal() {
    return signal;
  }

  /**
   * @return the exception
   */
  public Exception getException() {
    return exception;
  }

  /**
   * @param exception
   *   the exception to set
   */
  public void setException(Exception exception) {
    this.exception = exception;
  }

  @Override
  public String toString() {
    return "signal=" + signal + ", exception=" + exception;
  }
}
