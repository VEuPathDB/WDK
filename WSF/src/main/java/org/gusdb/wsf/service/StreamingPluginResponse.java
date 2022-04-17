package org.gusdb.wsf.service;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.gusdb.wsf.common.ResponseAttachment;
import org.gusdb.wsf.common.ResponseMessage;
import org.gusdb.wsf.common.ResponseRow;
import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginResponse;

public class StreamingPluginResponse implements PluginResponse {

  private final ObjectOutputStream outStream;

  private int rowCount;
  private int attachmentCount;

  public StreamingPluginResponse(ObjectOutputStream outStream) {
    this.outStream = outStream;
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getAttachmentCount() {
    return attachmentCount;
  }

  @Override
  public void addRow(String[] row) throws PluginModelException {
    ResponseRow responseRow = new ResponseRow(row);
    try {
      outStream.writeObject(responseRow);
      rowCount++;
    }
    catch (IOException ex) {
      throw new PluginModelException(ex);
    }
  }

  @Override
  public void addAttachment(String key, String content)
  throws PluginModelException {
    ResponseAttachment attachment = new ResponseAttachment(key, content);
    try {
      outStream.writeObject(attachment);
      attachmentCount++;
    }
    catch (IOException ex) {
      throw new PluginModelException(ex);
    }
  }

  @Override
  public void setMessage(String message) throws PluginModelException {
    ResponseMessage responseMessage = new ResponseMessage(message);
    try {
      outStream.writeObject(responseMessage);
    }
    catch (IOException ex) {
      throw new PluginModelException(ex);
    }
  }

}
