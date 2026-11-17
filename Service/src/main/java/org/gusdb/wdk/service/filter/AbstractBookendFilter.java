package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.User;

public abstract class AbstractBookendFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  private static final String URI_INFO_PROP_KEY = "uriInfo";
  private static final String HTTP_RESPONSE_STATUS_PROP_KEY = "httpResponseStatus";

  protected abstract void onRequestBegin(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user, ContainerRequestContext requestContext) throws IOException;
  protected abstract void onRequestCompleteNoContent(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user, int httpStatus);
  protected abstract void performContentWrite(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user, int httpStatus, WriterInterceptorContext writerContext) throws IOException;

  @Context
  private ServletContext _servletContext;

  @Inject
  protected Provider<HttpServletRequest> _servletRequest;

  @Inject
  protected Provider<Request> _grizzlyRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());
    User user = (User)request.getAttributeMap().get(Utilities.CONTEXT_KEY_USER_OBJECT);
    WdkModel wdkModel = ContextLookup.getWdkModel(_servletContext);
    ExtendedUriInfo uriInfo = ((ContainerRequest)requestContext).getUriInfo();
    requestContext.setProperty(URI_INFO_PROP_KEY, uriInfo);

    onRequestBegin(request, uriInfo, wdkModel, user, requestContext);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());
    User user = (User)request.getAttributeMap().get(Utilities.CONTEXT_KEY_USER_OBJECT);
    WdkModel wdkModel = ContextLookup.getWdkModel(_servletContext);

    // pass along the response status to the interceptor to log after response body is written
    int httpStatus = responseContext.getStatus();
    request.setAttribute(HTTP_RESPONSE_STATUS_PROP_KEY, httpStatus);

    if (responseContext.getEntity() == null) { // no response body
      onRequestCompleteNoContent(request, ((ContainerRequest)requestContext).getUriInfo(), wdkModel, user, httpStatus);
    }
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext writerContext) throws IOException, WebApplicationException {

    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());
    User user = (User)request.getAttributeMap().get(Utilities.CONTEXT_KEY_USER_OBJECT);
    WdkModel wdkModel = ContextLookup.getWdkModel(_servletContext);
    int httpStatus = (Integer)request.getAttributeMap().get(HTTP_RESPONSE_STATUS_PROP_KEY);
    ExtendedUriInfo uriInfo = (ExtendedUriInfo)request.getAttributeMap().get(URI_INFO_PROP_KEY);

    performContentWrite(request, uriInfo, wdkModel, user, httpStatus, writerContext);
  }

  protected static <T> T getAndRemoveProp(RequestData request, String propName, Class<T> expectedClass) {
    T obj = expectedClass.cast(request.getAttributeMap().get(propName));
    request.setAttribute(propName, null);
    return obj;
  }
}