package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@Provider
@Priority(200)
public class RequestCompleteFilter implements ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(RequestCompleteFilter.class);

  private static final Level LOG_LEVEL = Level.INFO;

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    LOG.log(LOG_LEVEL, "Request complete");
  }
}
