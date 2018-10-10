package org.gusdb.wdk.model;

import javax.activation.DataHandler;

public class Attachment {
  private DataHandler dataHandler;
  private String fileName;
  
  public Attachment(DataHandler dataHandler, String fileName) {
    this.dataHandler = dataHandler;
    this.fileName = fileName;
  }
  
  public DataHandler getDataHandler() {
    return dataHandler;
  }
  
  public String getFileName() {
    return fileName;
  }
}
