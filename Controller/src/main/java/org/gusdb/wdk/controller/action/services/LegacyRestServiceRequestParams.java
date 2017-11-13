package org.gusdb.wdk.controller.action.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.gusdb.fgputil.web.HttpRequestData;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.RequestParams;

public class LegacyRestServiceRequestParams implements RequestParams {

  private final HttpServletRequest _request;
  private final Map<String,String[]> _requestParams;

  public LegacyRestServiceRequestParams(HttpServletRequest request) {
    _request = request;
    Map<String,String[]> origParams = new HttpRequestData(request).getTypedParamMap();
    // trim parameter names in case URL translation added whitespace
    _requestParams = new HashMap<>();
    for (Entry<String,String[]> param : origParams.entrySet()) {
      _requestParams.put(param.getKey().trim(), param.getValue());
    }
  }

  @Override
  public String getParam(String name) {
    String[] value = _requestParams.get(name);
    return (value == null || value.length != 1 ? null : value[0]);
  }

  @Override
  public String[] getArray(String name) {
    String[] value = _requestParams.get(name);
    if (value == null) return null;
    switch(value.length) {
      case 0: return null;
      case 1: return value[0].split(",");
      default: return value;
    }
  }

  public Set<String> paramNames() {
    return _requestParams.keySet();
  }

  @Override
  public Object getAttribute(String name) {
    return _request.getAttribute(name);
  }

  @Override
  public String getUploadFileContent(String name) throws WdkModelException {
    throw new UnsupportedOperationException("File upload is not supported in our Web Services.");
  }

  @Override
  public RequestParams setParam(String name, String value) {
    throw new UnsupportedOperationException("Setting params is not supported in our Web Services.");
  }

  @Override
  public void setArray(String name, String[] array) {
    throw new UnsupportedOperationException("Setting array params is not supported in our Web Services.");
  }

  @Override
  public void setAttribute(String name, Object value) {
    _request.setAttribute(name, value);
  }

}
