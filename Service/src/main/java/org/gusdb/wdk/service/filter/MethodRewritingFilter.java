package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

import org.apache.log4j.Logger;

@PreMatching
public class MethodRewritingFilter implements ContainerRequestFilter {

  private static final Logger LOG = Logger.getLogger(MethodRewritingFilter.class);

  private static final String HTTP_METHOD_PARAM = "http-method";
  private static final String[] ALLOWABLE_OVERRIDES = { "PUT", "DELETE", "PATCH" };

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // override POST method if user passes supported method as param
    String methodOverride = getOverrideMethod(requestContext);
    if (methodOverride != null) {
      LOG.info("Overriding HTTP method; was " + requestContext.getMethod() + ", now " + methodOverride);
      requestContext.setMethod(methodOverride);
    }
  }

  private String getOverrideMethod(ContainerRequestContext requestContext) {
    List<String> overrideValues = requestContext.getUriInfo().getQueryParameters().get(HTTP_METHOD_PARAM);
    if (overrideValues == null || overrideValues.isEmpty()) return null;
    String newMethod = overrideValues.get(0);
    if (Arrays.asList(ALLOWABLE_OVERRIDES).contains(newMethod)) {
      return newMethod;
    }
    LOG.warn("Request made with illegal " + HTTP_METHOD_PARAM + "' value: " + newMethod);
    return null;
  }
}
