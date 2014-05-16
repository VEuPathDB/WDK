package org.gusdb.wdk.controller.action.services;

import javax.servlet.http.HttpServletRequest;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.RequestParams;

public class ServiceRequestParams implements RequestParams {

  private final HttpServletRequest request;

  public ServiceRequestParams(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public String getParam(String name) {
    return request.getParameter(name);
  }

  @Override
  public String[] getArray(String name) {
    String[] values = request.getParameterValues(name);
    // only return the array if it has more than 1 value; otherwise, use the value from getParam().
    if (values != null & values.length > 1)
      return values;

    String value = getParam(name);
    return (value == null) ? null : value.split(",");
  }

  @Override
  public Object getAttribute(String name) {
    return request.getAttribute(name);
  }

  @Override
  public String getUploadFileContent(String name) throws WdkModelException {
    throw new UnsupportedOperationException("File upload is not supported in our Web Services.");
  }

  @Override
  public void setParam(String name, String value) {
    throw new UnsupportedOperationException("Setting params is not supported in our Web Services.");
  }

  @Override
  public void setArray(String name, String[] array) {
    throw new UnsupportedOperationException("Setting array params is not supported in our Web Services.");
  }

  @Override
  public void setAttribute(String name, Object value) {
    request.setAttribute(name, value);
  }

}
