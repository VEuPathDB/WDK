package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModelException;

public class RecordNotFoundException extends WdkModelException {

  private static final long serialVersionUID = 1L;

  public RecordNotFoundException(String message) {
    super(message);
  }

}
