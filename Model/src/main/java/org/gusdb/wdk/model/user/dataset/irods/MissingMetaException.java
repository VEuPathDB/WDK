package org.gusdb.wdk.model.user.dataset.irods;

import org.gusdb.wdk.model.WdkModelException;

public class MissingMetaException extends WdkModelException {
  public final String path;

  public MissingMetaException(String msg, String path) {
    super(msg);
    this.path = path;
  }
}