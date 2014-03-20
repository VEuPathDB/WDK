package org.gusdb.wdk.controller.actionutil;

import java.util.Map;

/**
 * Provides standard information that came in on the current request
 * 
 * @author rdoherty
 */
public interface RequestData {
  public String getRequestUri();
  public String getNoContextUrl();
  public String getWebAppBaseUrl();
  public String getRequestUrl();
  public String getQueryString();
  /** returns the full request URL (including the query string) */
  public String getFullRequestUrl();
  public String getBrowser();
  public String getReferrer();
  public String getIpAddress();
  public Object getRequestAttribute(String key);
  public String getRequestHeader(String key);
  public String getRemoteHost();
  public String getServerName();
  public String getUserAgent();
  public HttpMethod getMethod();
  public Map<String, String[]> getTypedParamMap();
}
