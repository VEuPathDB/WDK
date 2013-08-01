package org.gusdb.wdk.model.query;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wsf.client.WsfResponse;
import org.gusdb.wsf.client.WsfService;
import org.gusdb.wsf.plugin.WsfServiceException;

public class ClientProcessResponse implements ProcessResponse {

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
  public String[][] getResult(int pageId) throws WsfServiceException {
    try {
      return service.requestResult(response.getInvokeId(), pageId).getResult();
    } catch (RemoteException ex) {
      throw new WsfServiceException(ex);
    }
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
