package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.controller.filter.CheckLoginFilterShared;

@PreMatching
@Priority(200)
public class CheckLoginRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

  public static final String SESSION_COOKIE_TO_SET = "sessionCookieToSet";

  @Context
  protected
  ServletContext _servletContext;

  @Inject
  protected Provider<HttpServletRequest> _servletRequest;

  @Inject
  protected Provider<Request> _grizzlyRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    ApplicationContext context = ContextLookup.getApplicationContext(_servletContext);
    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());

    Optional<CookieBuilder> newCookie =
        CheckLoginFilterShared.calculateUserActions(
            findLoginCookie(requestContext.getCookies()), context, request.getSession());

    if (newCookie.isPresent()) {
      requestContext.setProperty(SESSION_COOKIE_TO_SET, newCookie.get().toJaxRsCookie().toString());
    }
  }

  protected Optional<CookieBuilder> findLoginCookie(Map<String, Cookie> cookies) {
    return Optional.ofNullable(cookies.get(LoginCookieFactory.WDK_LOGIN_COOKIE_NAME))
        .map(cookie -> new CookieBuilder(cookie.getName(), cookie.getValue()));
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    if (requestContext.getPropertyNames().contains(SESSION_COOKIE_TO_SET)) {
      responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, requestContext.getProperty(SESSION_COOKIE_TO_SET));
    }
  }
}
