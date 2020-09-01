package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.model.MDCUtil;

@PreMatching
@Priority(30)
public class MDCRequestFilter implements ContainerRequestFilter {

  private static final AtomicInteger requestId = new AtomicInteger(1);

  @Inject
  private Provider<HttpServletRequest> _servletRequest;

  @Inject
  private Provider<Request> _grizzlyRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());

    MDCUtil.setRequestStartTime(System.currentTimeMillis());
    MDCUtil.setIpAddress(request.getRemoteIpAddress());
    MDCUtil.setRequestedDomain(request.getServerName());
    MDCUtil.setRequestId(String.valueOf(requestId.getAndIncrement()));
    MDCUtil.setSessionId(request.getSession().getId());

  }
}
