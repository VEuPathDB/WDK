package org.gusdb.wsf.common;

import java.io.Serializable;

public class ResponseAttachment implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String key;
  private final String content;

  public ResponseAttachment(String key, String content) {
    this.key = key;
    this.content = content;
  }

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @return the content
   */
  public String getContent() {
    return content;
  }

}
