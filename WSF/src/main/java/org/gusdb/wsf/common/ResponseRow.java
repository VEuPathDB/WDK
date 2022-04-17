package org.gusdb.wsf.common;

import java.io.Serializable;

public class ResponseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String[] row;

  public ResponseRow(String[] row) {
    this.row = row;
  }

  /**
   * @return the row
   */
  public String[] getRow() {
    return row;
  }

}
