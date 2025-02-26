package org.gusdb.wdk.service.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.exception.ExpiredTokenException;
import org.gusdb.oauth2.exception.InvalidTokenException;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.service.SessionService;
import org.gusdb.wdk.service.service.SystemService;

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
    UserFactory factory = ContextLookup.getWdkModel(context).getUserFactory();

    // try to find submitted bearer token
    String rawToken = findRawBearerToken(request, requestContext);

    try {
      if (rawToken == null) {
        // no credentials submitted; automatically create a guest to use on this request
        useNewGuest(factory, request, requestContext, requestPath);
      }
      else {
        try {
          // validate submitted token
          ValidatedToken token = factory.validateBearerToken(rawToken);
          User user = factory.convertToUser(token);
          setRequestAttributes(request, token, user);
          LOG.info("Validated successfully.  Request will be processed for user " + user.getUserId());
        }
        catch (ExpiredTokenException e) {
          // token is expired; use guest token for now which should inspire them to log back in
          useNewGuest(factory, request, requestContext, requestPath);
        }
        catch (InvalidTokenException e) {
          // passed token is invalid; throw 401
          LOG.warn("Received invalid bearer token for auth: " + rawToken);
          throw new NotAuthorizedException(Response.status(Status.UNAUTHORIZED).build());
        }
      }
    }
    catch (Exception e) {
      // any other exception is fatal, but log first
      LOG.error("Unable to authenticate with Authorization header " + rawToken, e);
      throw e instanceof RuntimeException ? (RuntimeException)e : new WdkRuntimeException(e);
    }
  }

  private void useNewGuest(UserFactory factory, RequestData request, ContainerRequestContext requestContext, String requestPath) throws WdkModelException {
    TwoTuple<ValidatedToken,User> guestPair = factory.createUnregisteredUser();
    ValidatedToken token = guestPair.getFirst();
    User user = guestPair.getSecond();
    setRequestAttributes(request, token, user);

    LOG.info("Created new guest user [" + user.getUserId() + "] for request to path: /" + requestPath);

    // set flag indicating that cookies should be added to response containing the new token
    requestContext.setProperty(TOKEN_COOKIE_VALUE_TO_SET, token.getTokenValue());
  }

  private void setRequestAttributes(RequestData request, ValidatedToken token, User user) {
    // set creds and user on the request object for use by this request's processing
    request.setAttribute(Utilities.CONTEXT_KEY_VALIDATED_TOKEN_OBJECT, token);
    request.setAttribute(Utilities.CONTEXT_KEY_USER_OBJECT, user);
  }

  private String findRawBearerToken(RequestData request, ContainerRequestContext requestContext) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null) {
      // commented to avoid sensitive header value being written to logs
      //LOG.trace("Recieved Authorization header with value: " + authHeader + "; trying bearer token validation.");
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
      NewCookie authCookie = SessionService.getAuthCookie(tokenValue);
      headers.add(HttpHeaders.SET_COOKIE, authCookie.toString());
    }

    // unset legacy WDK check auth cookie in case it is present
    CookieBuilder cookie = new CookieBuilder(LEGACY_WDK_LOGIN_COOKIE_NAME, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    headers.add(HttpHeaders.SET_COOKIE, cookie.toJaxRsCookie().toString());
  }
}
