package org.gusdb.wdk.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.server.GrizzlyRequestData;
import org.gusdb.fgputil.server.RESTServer;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.HttpRequestData;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ServletApplicationContext;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

public class ContextLookup {


  public static ApplicationContext getApplicationContext(ServletContext servletContext) {
    return servletContext == null ?
        RESTServer.getApplicationContext() :
        new ServletApplicationContext(servletContext);
  }

  public static WdkModel getWdkModel(ServletContext servletContext) {
    ApplicationContext context =
      servletContext == null ? // not injected
      RESTServer.getApplicationContext() :
      new ServletApplicationContext(servletContext);
    return (WdkModel)context.get(Utilities.WDK_MODEL_KEY);
  }

  public static RequestData getRequest(HttpServletRequest servletRequest, Request grizzlyRequest) {
    if (servletRequest == null && grizzlyRequest == null) {
      throw new IllegalStateException("Neither Servlet nor Grizzly resources were successfully injected.");
    }
    return servletRequest == null ?
        new GrizzlyRequestData(grizzlyRequest) :
        new HttpRequestData(servletRequest);
      
  }
}
