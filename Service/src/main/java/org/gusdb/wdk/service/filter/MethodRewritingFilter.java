package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;

import org.apache.log4j.Logger;

/**
 * This filter serves two purposes:
 *
 * 1. To capitalize the HTTP method if it's not already.  This is needed to
 *    properly route requests to services with methods not supported by Jersey
 *    (e.g. PATCH), using custom annotations.
 *
 * 2. To allow form submission using HTTP methods not generally allowed by
 *    browsers.  To do this, the form can be submitted to a resource using GET
 *    or POST but with additional query parameter "http-method" which can
 *    contain PUT, PATCH, or DELETE.  If this parameter is found and the value
 *    valid then the method of the request will be changed to the requested
 *    method.
 *
 * TODO: See if this is still necessary (late 2019)- Jersey and browsers may now
 *       have complete support for PUT, PATCH, and DELETE
 *
 * @author rdoherty
 */
@PreMatching
@Priority(300)
public class MethodRewritingFilter implements ContainerRequestFilter {

  private static final Logger LOG = Logger.getLogger(MethodRewritingFilter.class);

  private static final String HTTP_METHOD_PARAM = "http-method";
  private static final List<String> OVERRIDEABLE_METHODS = list("GET", "POST");
  private static final List<String> ALLOWABLE_OVERRIDES = list("PUT", "DELETE", "PATCH");

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    boolean rewrite = false;
    String method = requestContext.getMethod();
    String upper = method.toUpperCase();
    if (!method.equals(upper)) {
      method = upper;
      rewrite = true;
    }

    if (OVERRIDEABLE_METHODS.contains(method)) {
      // override GET or POST method if user passes supported method as param
      String methodOverride = getOverrideMethod(requestContext);
      if (methodOverride != null) {
        LOG.info("Overriding HTTP method; was " + method + ", now " + methodOverride);
        method = methodOverride;
        rewrite = true;
      }
    }

    if (rewrite) {
      requestContext.setMethod(method);
    }
  }

  private String getOverrideMethod(ContainerRequestContext requestContext) {
    List<String> overrideValues = requestContext.getUriInfo().getQueryParameters().get(HTTP_METHOD_PARAM);
    if (overrideValues == null || overrideValues.isEmpty()) return null;
    String newMethod = overrideValues.get(0);
    if (ALLOWABLE_OVERRIDES.contains(newMethod)) {
      return newMethod;
    }
    LOG.warn("Request made with illegal '" + HTTP_METHOD_PARAM + "' value: " + newMethod);
    return null;
  }

  private static List<String> list(String... strings) {
    return Arrays.asList(strings);
  }
}
