package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.gusdb.fgputil.logging.ThreadLocalLoggingVars;

@Priority(30)
public class MDCResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    ThreadLocalLoggingVars.clearValues();
  }

}
