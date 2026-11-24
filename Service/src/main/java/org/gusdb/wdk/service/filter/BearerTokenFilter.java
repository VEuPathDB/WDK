package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;

import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.service.BearerTokens;

/**
 * Reads bearer token from request and evaluates it for validity and expiration,
 * then sets status as a request property for later access (does not reject anything).
 */
public class BearerTokenFilter implements ContainerRequestFilter {

  public static final String BEARER_TOKEN_STATUS = "bearerTokenStatus";

  @Context
  private ServletContext _servletContext;

  @Inject
  protected Provider<HttpServletRequest> _servletRequest;

  @Inject
  protected Provider<Request> _grizzlyRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());

    WdkModel wdkModel = ContextLookup.getWdkModel(_servletContext);

    requestContext.setProperty(BEARER_TOKEN_STATUS, BearerTokens.getBearerTokenStatus(
        BearerTokens.findRawBearerToken(request, requestContext), wdkModel));
  }

}
