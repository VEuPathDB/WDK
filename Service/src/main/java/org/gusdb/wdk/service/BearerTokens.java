package org.gusdb.wdk.service;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Either;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.exception.ExpiredTokenException;
import org.gusdb.oauth2.exception.InvalidTokenException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.UserFactory;

public class BearerTokens {

  public enum BadTokenStatus {
    MISSING,
    INVALID,
    EXPIRED;
  }

  public static class Unauthenticated extends TwoTuple<BadTokenStatus,Exception> {

    private static Unauthenticated missing() {
      return new Unauthenticated(BadTokenStatus.MISSING, new NullPointerException());
    }

    private static Unauthenticated invalid(InvalidTokenException e) {
      return new Unauthenticated(BadTokenStatus.INVALID, e);
    }

    private static Unauthenticated expired(ExpiredTokenException e) {
      return new Unauthenticated(BadTokenStatus.INVALID, e);
    }

    private Unauthenticated(BadTokenStatus status, Exception cause) {
      super(status,cause);
    }

    public BadTokenStatus getStatus() {
      return getFirst();
    }

    public Exception getCause() {
      return getSecond();
    }
  }

  public static Either<ValidatedToken,Unauthenticated> getBearerTokenStatus(String rawToken, WdkModel wdkModel) {
    if (rawToken == null) {
      return Either.right(Unauthenticated.missing());
    }
    try {
      return Either.left(new UserFactory(wdkModel).validateBearerToken(rawToken));
    }
    catch(InvalidTokenException e) {
      return Either.right(Unauthenticated.invalid(e));
    }
    catch(ExpiredTokenException e) {
      return Either.right(Unauthenticated.expired(e));
    }
  }

  public static String findRawBearerToken(RequestData request, ContainerRequestContext requestContext) {
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

}
