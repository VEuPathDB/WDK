package org.gusdb.wdk.service.filter;

import static org.gusdb.wdk.service.filter.CheckLoginRequestFilter.SESSION_COOKIE_TO_SET;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class CheckLoginResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    if (requestContext.getPropertyNames().contains(SESSION_COOKIE_TO_SET)) {
      responseContext.getHeaders().add("Set-Cookie", requestContext.getProperty(SESSION_COOKIE_TO_SET));
    }
  }
}
