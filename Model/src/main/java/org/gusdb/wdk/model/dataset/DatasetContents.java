package org.gusdb.wdk.model.dataset;

import org.gusdb.wdk.model.WdkModelException;

import java.io.Reader;

public abstract class DatasetContents {

  protected final String fileName;

  protected DatasetContents(String fileName) {
    this.fileName = fileName;
  }

  public String getUploadFileName() {
    return fileName;
  }

  public abstract String getChecksum();

  public abstract Reader getContentReader() throws WdkModelException;
}
