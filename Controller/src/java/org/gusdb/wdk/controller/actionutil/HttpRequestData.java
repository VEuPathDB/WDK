package org.gusdb.wdk.controller.actionutil;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestData implements RequestData {
  
  private HttpServletRequest _request;
  
  public HttpRequestData(HttpServletRequest request) {
    _request = request;
  }
  
  @Deprecated
  public HttpServletRequest getUnderlyingRequest() {
    return _request;
  }
  
  @Override
  public String getWebAppBaseUrl() {
    return new StringBuilder()
      .append(getNoContextUrl())
      .append(_request.getContextPath())
      .toString();
  }

  @Override
  public String getNoContextUrl() {
    return new StringBuilder()
      .append(_request.getScheme())
      .append("://")
      .append(_request.getServerName())
      .append(_request.getServerPort() == 80 ||
              _request.getServerPort() == 443 ?
              "" : ":" + _request.getServerPort())
      .toString();
  }
  
  @Override
  public String getRequestUrl() {
    return _request.getRequestURL().toString();
  }

  @Override
  public String getQueryString() {
    return _request.getQueryString();
  }

  @Override
  public String getFullRequestUrl() {
    StringBuffer buf = _request.getRequestURL();
    String qString = _request.getQueryString();
    return (buf == null ? new StringBuffer() : buf)
      .append(qString == null ? "" : "?" + qString)
      .toString();
  }

  @Override
  public String getBrowser() {
    return _request.getHeader("User-Agent");
  }

  @Override
  public String getReferrer() {
    return _request.getHeader("Referer");
  }

  @Override
  public String getUserAgent() {
    return _request.getHeader("user-agent");
  }

  @Override
  public String getIpAddress() {
    return _request.getRemoteAddr();
  }

  @Override
  public Object getRequestAttribute(String key) {
    return _request.getAttribute(key);
  }

  @Override
  public String getRequestHeader(String key) {
    return _request.getHeader(key);
  }

  @Override
  public String getRemoteHost() {
    return _request.getRemoteHost();
  }

  @Override
  public String getServerName() {
    return _request.getServerName();
  }
  
  @Override
  @SuppressWarnings("rawtypes")
  public Map<String, String[]> getTypedParamMap() {
    Map parameterMap = _request.getParameterMap();
    @SuppressWarnings({ "unchecked", "cast" })
    Map<String, String[]> parameters = (Map<String, String[]>) (parameterMap == null ?
        new HashMap<>() : new HashMap<>((Map<String, String[]>)parameterMap));
    return parameters;
  }
}
