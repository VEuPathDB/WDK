package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.BearerTokenUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.service.SessionService;
import org.gusdb.wdk.service.service.SystemService;
import org.gusdb.wdk.session.WdkOAuthClientWrapper;

@Priority(30)
public class CheckLoginFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(CheckLoginFilter.class);

  private static final String TOKEN_COOKIE_VALUE_TO_SET = "tokenCookieValueToSet";

  private static final String LEGACY_WDK_LOGIN_COOKIE_NAME = "wdk_check_auth";

  @Context
  protected ServletContext _servletContext;

  @Inject
  protected Provider<HttpServletRequest> _servletRequest;

  @Inject
  protected Provider<Request> _grizzlyRequest;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // skip endpoints which do not require a user; prevents guests from being unnecessarily created
    String requestPath = requestContext.getUriInfo().getPath();
    if (isPathToSkip(requestPath)) return;

    ApplicationContext context = ContextLookup.getApplicationContext(_servletContext);
    RequestData request = ContextLookup.getRequest(_servletRequest.get(), _grizzlyRequest.get());
    WdkModel wdkModel = ContextLookup.getWdkModel(context);
    WdkOAuthClientWrapper oauth = new WdkOAuthClientWrapper(wdkModel);

    // try to find submitted bearer token
    String rawToken = findRawBearerToken(request, requestContext);

    try {
      ValidatedToken token;
      User user;
      if (rawToken != null) {
        // validate submitted token
        token = oauth.validateBearerToken(rawToken);
        user = new BearerTokenUser(wdkModel, oauth, token);

        LOG.info("Validated successfully.  Request will be processed for user " + user.getUserId() + " / " + user.getEmail());
      }
      else {
        // no credentials submitted; automatically create a guest to use on this request
        UserFactory factory = wdkModel.getUserFactory();
        TwoTuple<ValidatedToken,User> guestPair = factory.createUnregisteredUser();
        token = guestPair.getFirst();
        user = guestPair.getSecond();

        LOG.info("Created new guest user [" + user.getUserId() + "] for request to path: /" + requestPath);

        // set flag indicating that cookies should be added to response containing the new token
        requestContext.setProperty(TOKEN_COOKIE_VALUE_TO_SET, token.getTokenValue());
      }

      // insert reference to this user into user DB for tracking and for foreign keys on other user DB tables
      wdkModel.getUserFactory().addUserReference(user);

      // set creds and user on the request object for use by this request's processing
      request.setAttribute(Utilities.BEARER_TOKEN_KEY, token);
      request.setAttribute(Utilities.WDK_USER_KEY, user);
    }
    catch (Exception e) {
      // for now, log and let this go, deferring to legacy authentication
      LOG.error("Unable to authenticate with Authorization header " + rawToken, e);
      throw e instanceof RuntimeException ? (RuntimeException)e : new WdkRuntimeException(e);
    }
  }

  private String findRawBearerToken(RequestData request, ContainerRequestContext requestContext) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null) {
      LOG.info("Recieved Authorization header with value: " + authHeader + "; trying bearer token validation.");
      return OAuthClient.getTokenFromAuthHeader(authHeader);
    }
    // otherwise try Authorization cookie
    Cookie cookie = requestContext.getCookies().get(HttpHeaders.AUTHORIZATION);
    return cookie == null ? null : cookie.getValue();
  }

  protected boolean isPathToSkip(String path) {
    // skip user check for prometheus metrics requests
    return SystemService.PROMETHEUS_ENDPOINT_PATH.equals(path);
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    MultivaluedMap<String,Object> headers = responseContext.getHeaders();
    if (requestContext.getPropertyNames().contains(TOKEN_COOKIE_VALUE_TO_SET)) {
      String tokenValue = (String)requestContext.getProperty(TOKEN_COOKIE_VALUE_TO_SET);

      // set cookie value for both Authorization cookie
      CookieBuilder cookie = new CookieBuilder(HttpHeaders.AUTHORIZATION, tokenValue);
      cookie.setMaxAge(SessionService.EXPIRATION_3_YEARS_SECS);
      cookie.setPath("/");
      headers.add(HttpHeaders.SET_COOKIE, cookie.toJaxRsCookie().toString());
    }

    // unset legacy WDK check auth cookie in case it is present
    CookieBuilder cookie = new CookieBuilder(LEGACY_WDK_LOGIN_COOKIE_NAME, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    headers.add(HttpHeaders.SET_COOKIE, cookie.toJaxRsCookie().toString());
  }
}
