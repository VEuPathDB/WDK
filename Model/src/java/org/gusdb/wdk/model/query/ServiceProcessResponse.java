package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wsf.service.WsfResponse;
import org.gusdb.wsf.service.WsfService;

public class ServiceProcessResponse implements
    ProcessResponse {

  private final WsfService service;
  private final WsfResponse response;
  
  public ServiceProcessResponse(WsfService service, WsfResponse response) {
    this.service = service;
    this.response = response;
  }

  @Override
  public String[][] getResult() {
    return response.getResult();
  }

  @Override
  public String[][] getResult(int pageId) throws  WdkModelException {
    return service.requestResult(response.getInvokeId(), pageId).getResult();
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
    return response.getAttachments();
  }
  
}
