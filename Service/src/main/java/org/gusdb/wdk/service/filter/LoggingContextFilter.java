package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;

import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.logging.ThreadLocalLoggingVars;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ContextLookup;

@PreMatching
@Priority(30)
public class LoggingContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final AtomicInteger requestId = new AtomicInteger(1);

  @Inject
  private Provider<HttpServletRequest> _servletRequest;

  @Inject
  private Provider<Request> _grizzlyRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());

    ThreadLocalLoggingVars.setRequestStartTime(System.currentTimeMillis());
    ThreadLocalLoggingVars.setIpAddress(request.getRemoteIpAddress());
    ThreadLocalLoggingVars.setRequestedDomain(request.getServerName());
    ThreadLocalLoggingVars.setRequestId(String.valueOf(requestId.getAndIncrement()));
    ThreadLocalLoggingVars.setSessionId(request.getSession().getId());

  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    ThreadLocalLoggingVars.clearValues();
  }

}
