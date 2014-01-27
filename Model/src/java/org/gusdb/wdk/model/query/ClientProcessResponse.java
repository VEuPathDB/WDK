package org.gusdb.wdk.model.query;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.client.WsfResponse;
import org.gusdb.wsf.client.WsfService;

public class ClientProcessResponse implements ProcessResponse {

  private static Logger LOG = Logger.getLogger(ClientProcessResponse.class);
  private long totalProcessingTime = 0;

  private final WsfService service;
  private final WsfResponse response;

  public ClientProcessResponse(WsfService service, WsfResponse response) {
    this.service = service;
    this.response = response;
  }

  @Override
  public String[][] getResult() {
    return response.getResult();
  }
  
  @Override
  public String[][] getResult(int pageId) throws WdkModelException {
    try {
      long start = System.currentTimeMillis();
      String[][] result = service.requestResult(response.getInvokeId(), pageId).getResult();
      logProcessingTime(start);
      return result;
    } catch (RemoteException ex) {
      throw new WdkModelException(ex);
    }
  }

  private void logProcessingTime(long start) {
    totalProcessingTime += (System.currentTimeMillis() - start);
    LOG.debug("Cumulative processing time in ClientProcessResponse: " + (0.0 + totalProcessingTime) / 1000);
  }

  @Override
  public String getMessage() {
    return response.getMessage();
  }

  @Override
  public int getSignal() {
    return response.getSignal();
  }

  @Override
  public int getInvokeId() {
    return response.getInvokeId();
  }

  @Override
  public int getPageCount() {
    return response.getPageCount();
  }

  @Override
  public int getCurrentPage() {
    return response.getCurrentPage();
  }

  @Override
  public Map<String, String> getAttachments() {
    HashMap<?, ?> attachments = response.getAttachments();
    Map<String, String> map = new LinkedHashMap<>();
    for (Object key : attachments.keySet()) {
      map.put(key.toString(), map.get(key).toString());
    }
    return map;
  }

}
