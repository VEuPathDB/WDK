package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;

@PreMatching
public class MethodRewritingFilter implements ContainerRequestFilter {

  private static final String HTTP_METHOD_PARAM = "http-method";

  private static final String[] ALLOWABLE_OVERRIDES = { "PUT", "DELETE", "PATCH" };

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // override POST method if user passes supported method as param
    String methodOverride = getOverrideMethod(requestContext);
    if (requestContext.getMethod().equals("POST") && methodOverride != null) {
      requestContext.setMethod(methodOverride);
    }
  }

  private String getOverrideMethod(ContainerRequestContext requestContext) {
    List<String> methodOverride = requestContext.getUriInfo().getQueryParameters().get(HTTP_METHOD_PARAM);
    if (methodOverride.isEmpty()) return null;
    if (Arrays.asList(ALLOWABLE_OVERRIDES).contains(methodOverride.get(0))) {
      return methodOverride.get(0);
    }
    return null;
  }
}
