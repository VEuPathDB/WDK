package org.gusdb.wdk.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Session;
import org.gusdb.fgputil.server.GrizzlyRequestData;
import org.gusdb.fgputil.server.GrizzlySessionProxy;
import org.gusdb.fgputil.server.RESTServer;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.fgputil.web.SessionProxy;
import org.gusdb.fgputil.web.servlet.HttpRequestData;
import org.gusdb.fgputil.web.servlet.HttpServletApplicationContext;
import org.gusdb.fgputil.web.servlet.HttpSessionProxy;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

public class ContextLookup {


  public static ApplicationContext getApplicationContext(ServletContext servletContext) {
    return servletContext == null ?
        RESTServer.getApplicationContext() :
        new HttpServletApplicationContext(servletContext);
  }

  public static WdkModel getWdkModel(ServletContext servletContext) {
    ApplicationContext context =
      servletContext == null ? // not injected
      RESTServer.getApplicationContext() :
      new HttpServletApplicationContext(servletContext);
    return (WdkModel)context.get(Utilities.WDK_MODEL_KEY);
  }

  public static RequestData getRequest(HttpServletRequest servletRequest, Request grizzlyRequest) {
    if (servletRequest == null && grizzlyRequest == null) {
      throw new IllegalStateException("Neither Servlet nor Grizzly request resource was successfully injected.");
    }
    return servletRequest == null ?
        new GrizzlyRequestData(grizzlyRequest) :
        new HttpRequestData(servletRequest);
      
  }

  public static WdkModel getWdkModel(ApplicationContext context) {
    return (WdkModel)context.get(Utilities.WDK_MODEL_KEY);
  }

  public static SessionProxy getSession(HttpSession servletSession, Session grizzlySession) {
    if (servletSession == null && grizzlySession == null ) {
      throw new IllegalStateException("Neither Servlet nor Grizzly session resource was successfully injected.");
    }
    return servletSession == null ?
        new GrizzlySessionProxy(grizzlySession) :
        new HttpSessionProxy(servletSession);
  }
}
