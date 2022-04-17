package org.gusdb.wsf.common;

import java.io.Serializable;

public class ResponseMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String message;

  public ResponseMessage(String message) {
    this.message = message;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

}
