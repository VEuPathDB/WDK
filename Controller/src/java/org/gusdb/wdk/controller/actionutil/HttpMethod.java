package org.gusdb.wdk.controller.actionutil;

public enum HttpMethod {
  POST,
  PUT,
  GET,
  DELETE;

  public static HttpMethod getValueOf(String method) {
    for (HttpMethod value : values()) {
      if (value.name().equalsIgnoreCase(method)) return value;
    }
    throw new IllegalArgumentException("Method string '" + method +
        "' does not represent a supported HTTP method.");
  }

  
}
