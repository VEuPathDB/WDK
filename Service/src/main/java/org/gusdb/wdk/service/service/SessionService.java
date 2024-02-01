package org.gusdb.wdk.service.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.gusdb.fgputil.web.SessionProxy;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfig.AuthenticationMethod;
import org.gusdb.wdk.model.user.BearerTokenUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.LoginRequest;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.statustype.MethodNotAllowedStatusType;
import org.gusdb.wdk.session.WdkOAuthClientWrapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.net.HttpHeaders;

@Path("/")
public class SessionService extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(SessionService.class);

  public static final int EXPIRATION_3_YEARS_SECS = 3 * 365 * 24 * 60 * 60;

  private static final String REFERRER_HEADER_KEY = "Referer";

  // OAuth2 parameter keys
  private static final String OAUTH_ERROR_KEY = "error";
  private static final String OAUTH_STATE_KEY = "state";
  private static final String OAUTH_CODE_KEY = "code";
  private static final String OAUTH_REDIRECT_URL_KEY = "redirectUrl";

  // input param constants
  private static final String COOKIE_KEY = "wdkLoginCookieValue";

  // json output constants for cookie verification endpoint
  private static final String IS_VALID_KEY = "isValid";
  private static final String USER_DATA_KEY = "userData";
  private static final String USER_ID_KEY = "id";
  private static final String DISPLAY_NAME_KEY = "displayName";
  private static final String EMAIL_KEY = "email";

  // redirect URLs
  private static final String OAUTH_ERROR_URL = "/app/user/message/login-error?requestUrl=";

  // state token session property
  public static final String STATE_TOKEN_KEY = "OAUTH_STATE_TOKEN";

  // ===== OAuth 2.0 + OpenID Connect Support =====
  /**
   * Create anti-forgery state token, add to session, and return.  This is
   * requested by the client when the user tries to log in using an OAuth2
   * server.  The value generated will be used to check the state token passed
   * to the oauth processing action.  They must match for the login to succeed.
   * We generate a new value each time; all but one of "overlapping" login
   * attempts by the same user will thus fail.
   * 
   * @return OAuth 2.0 state token
   * @throws WdkModelException if unable to read WDK secret key from file
   */
  @GET
  @Path("oauth/state-token")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOauthStateToken() throws WdkModelException {
    String newToken = generateStateToken(getWdkModel());
    getSession().setAttribute(STATE_TOKEN_KEY, newToken);
    JSONObject json = new JSONObject();
    json.put(JsonKeys.OAUTH_STATE_TOKEN, newToken);
    return Response.ok(json.toString()).build();
  }

  /**
   * Generates a new state token based on the current time, a random UUID, and
   * WDK model's current secret key value.  This value should be added to the
   * session when a user is about to attempt a login; it will then be checked
   * against the state token accompanying the authentication token returned by
   * the OAuth server after the user has been authenticated.  This is to prevent
   * cross-site request forgery attacks.
   * 
   * @param wdkModel WDK Model (used to fetch secret key)
   * @return generated state token
   * @throws WdkModelException if unable to access secret key
   */
  private static String generateStateToken(WdkModel wdkModel) throws WdkModelException {
    String saltedString =
        UUID.randomUUID() + ":::" +
        String.valueOf(new Date().getTime()) + ":::" +
        wdkModel.getModelConfig().getSecretKey();
    return EncryptionUtil.encrypt(saltedString);
  }

  @GET
  @Path("login")
  public Response processOauthLogin(
      @QueryParam(OAUTH_ERROR_KEY) String error,
      @QueryParam(OAUTH_STATE_KEY) String stateToken,
      @QueryParam(OAUTH_CODE_KEY) String authCode,
      @QueryParam(OAUTH_REDIRECT_URL_KEY) String originalUrl)
      throws WdkModelException {

    WdkModel wdkModel = getWdkModel();
    ModelConfig modelConfig = wdkModel.getModelConfig();

    if (!AuthenticationMethod.OAUTH2.equals(modelConfig.getAuthenticationMethodEnum())) {
      return Response.status(new MethodNotAllowedStatusType()).build();
    }

    String appUrl = getContextUri();
    String redirectUrl = isEmpty(originalUrl) ? appUrl : originalUrl;

    // Was existing bearer token submitted with this request?
    User oldUser = getRequestingUser();
    if (!oldUser.isGuest()) {
      return createRedirectResponse(redirectUrl).build();
    }

    try {
      // check for error or to see if user denied us access; they won't be able to log in
      if (error != null) {
        String errorMessage = error.equals("access_denied") ?
            "User did not grant permission to access identifying information so we cannot log user in." :
            error;
        throw new WdkModelException(errorMessage);
      }

      // get state token off session and remove; only needed for this request
      String storedStateToken = (String) getSession().getAttribute(STATE_TOKEN_KEY);
      getSession().removeAttribute(STATE_TOKEN_KEY);

      // Is the state token present and does it match the session state token?
      if (stateToken == null || !stateToken.equals(storedStateToken)) {
        throw new WdkModelException("Unable to log in; state token missing, incorrect, or expired.");
      }

      // Use auth code to get the bearer token, then convert to User
      WdkOAuthClientWrapper client = new WdkOAuthClientWrapper(wdkModel);
      ValidatedToken bearerToken = client.getBearerTokenFromAuthCode(authCode, appUrl);
      User newUser = new BearerTokenUser(wdkModel, client, bearerToken);
      wdkModel.getUserFactory().insertUserToUserDb(newUser);

      // transfer ownership from guest to logged-in user
      transferOwnership(oldUser, newUser, wdkModel);

      // login successful; create redirect response
      return getSuccessResponse(bearerToken, newUser, oldUser, redirectUrl, true);
    }
    catch (Exception ex) {
      LOG.error("Unsuccessful login attempt:  " + ex.getMessage(), ex);
      String oauthFailureUrl = appUrl + OAUTH_ERROR_URL + FormatUtil.urlEncodeUtf8(originalUrl);
      return createRedirectResponse(oauthFailureUrl).build();
    }
  }

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response processDbLogin(@HeaderParam(REFERRER_HEADER_KEY) String referrer, String body)
      throws RequestMisformatException {
    try {
      WdkModel wdkModel = getWdkModel();

      // RRD 11/17: Allow sites configured to use OAuth (e.g. live sites) to log in users with this endpoint;
      //     Reason: logging in this way will be easier for programmatic service access that requires login
      //if (!AuthenticationMethod.USER_DB.equals(modelConfig.getAuthenticationMethodEnum())) {
      //  return Response.status(new MethodNotAllowedStatusType()).build();
      //}

      LoginRequest request = LoginRequest.createFromJson(new JSONObject(body));
      String appUrl = getContextUri();
      String originalUrl = request.getRedirectUrl();
      String redirectUrl = !isEmpty(originalUrl) ? originalUrl : !isEmpty(referrer) ? referrer : appUrl;

      // Was existing bearer token submitted with this request?
      User oldUser = getRequestingUser();
      if (!oldUser.isGuest()) {
        return createRedirectResponse(redirectUrl).build();
      }

      // Use passed credentials to get the bearer token, then convert to User
      WdkOAuthClientWrapper client = new WdkOAuthClientWrapper(wdkModel);
      ValidatedToken bearerToken = client.getBearerTokenFromCredentials(request.getEmail(), request.getPassword(), appUrl);
      User newUser = new BearerTokenUser(wdkModel, client, bearerToken);
      wdkModel.getUserFactory().insertUserToUserDb(newUser);

      // transfer ownership from guest to logged-in user
      transferOwnership(oldUser, newUser, wdkModel);

      return getSuccessResponse(bearerToken, newUser, oldUser, redirectUrl, false);

    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
    catch (Exception ex) {
      LOG.error("Could not authenticate user's identity.  Exception thrown: ", ex);
      return createJsonResponse(false, "Invalid username or password", null).build();
    }
  }

  protected void transferOwnership(User oldUser, User newUser, WdkModel wdkModel) throws WdkModelException {
    // transfer dataset ownership
    wdkModel.getDatasetFactory().transferDatasetOwnership(oldUser, newUser);
    // transfer strategy ownership
    wdkModel.getStepFactory().transferStrategyOwnership(oldUser, newUser);
  }

  /**
   * Sets the passed user on the session and packages a successful login response with a login cookie
   * @param bearerToken 
   *
   * @param bearerToken bearer token for the new user
   * @param newUser newly logged in user
   * @param oldUser user previously on session, if any
   * @param redirectUrl incoming original page
   * @param isRedirectResponse whether to return redirect or JSON response
   * @return success response
   * @throws WdkModelException
   */
  private Response getSuccessResponse(ValidatedToken bearerToken, User newUser, User oldUser,
      String redirectUrl, boolean isRedirectResponse) throws WdkModelException {
    SessionProxy session = getSession();

    // synchronize on the underlying session object (SessionProxy is request-local)
    synchronized(session.getUnderlyingSession()) {

      session.setAttribute(Utilities.WDK_USER_KEY, newUser);

      Events.triggerAndWait(new NewUserEvent(newUser, oldUser, session),
          new WdkRuntimeException("Unable to complete WDK user assignement."));

      // TODO: until client is updated, must still return WDK login cookie
      LoginCookieFactory baker = new LoginCookieFactory(getWdkModel().getModelConfig().getSecretKey());
      CookieBuilder loginCookie = baker.createLoginCookie(newUser.getEmail());
      loginCookie.setMaxAge(EXPIRATION_3_YEARS_SECS);

      // 3-year expiration (should change secret key before then)
      CookieBuilder bearerTokenCookie = new CookieBuilder(
          HttpHeaders.AUTHORIZATION,
          bearerToken.getTokenValue());
      bearerTokenCookie.setMaxAge(EXPIRATION_3_YEARS_SECS);

      redirectUrl = getSuccessRedirectUrl(redirectUrl, newUser, loginCookie, bearerTokenCookie);

      return (isRedirectResponse ?
        createRedirectResponse(redirectUrl) :
        createJsonResponse(true, null, redirectUrl)
      )
      .cookie(loginCookie.toJaxRsCookie(), bearerTokenCookie.toJaxRsCookie())
      .build();
    }
  }

  /**
   * Does any conversion from passed redirect URL to application-specific redirect URL.  May be overridden.
   *
   * @param redirectUrl incoming original page
   * @param user newly logged in user
   * @param cookie login cookie to be sent to the browser
   * @return page user should be redirected to after successful login
   */
  protected String getSuccessRedirectUrl(String redirectUrl, User user, CookieBuilder cookie, CookieBuilder bearerTokenCookie) {
    return redirectUrl;
  }

  @GET
  @Path("logout")
  public Response processLogout() throws WdkModelException {

    // get the current session's user, then invalidate the session
    User oldUser = getRequestingUser();
    getSession().invalidate();
    
    // get a new session and add new guest user to it
    SessionProxy session = getSession();
    User newUser = getWdkModel().getUserFactory().createUnregistedUser();
    session.setAttribute(Utilities.WDK_USER_KEY, newUser);

    // throw new user event
    Events.triggerAndWait(new NewUserEvent(newUser, oldUser, session), 
        new WdkRuntimeException("Unable to complete WDK user assignement."));

    // create and append logout cookies to response
    Set<CookieBuilder> logoutCookies = new HashSet<>();
    logoutCookies.add(LoginCookieFactory.createLogoutCookie());
    for (CookieBuilder extraCookie : getWdkModel().getUiConfig().getExtraLogoutCookies()) {
      extraCookie.setValue("");
      extraCookie.setMaxAge(-1);
      logoutCookies.add(extraCookie);
    }
    ResponseBuilder builder = createRedirectResponse(getContextUri());
    for (CookieBuilder logoutCookie : logoutCookies) {
      builder.cookie(logoutCookie.toJaxRsCookie());
    }
    return builder.build();
  }

  /**
   * Convenience method to set up a response builder that returns JSON containing request result
   *
   * @param success whether the login was successful
   * @param message a failure message if not successful
   * @param redirectUrl url to which to redirect the user if successful
   * @return partially constructed response
   */
  private static ResponseBuilder createJsonResponse(boolean success, String message, String redirectUrl) {
    return Response.ok(new JSONObject()
        .put(JsonKeys.SUCCESS, success)
        .put(JsonKeys.MESSAGE, message)
        .put(JsonKeys.REDIRECT_URL, redirectUrl)
        .toString());
  }

  /**
   * Convenience method to set up a response builder with the redirect url provided
   *
   * @param redirectUrl url of page to which to redirect the user
   * @return partially constructed response
   * @throws WdkModelException if the url is invalid.
   */
  private static ResponseBuilder createRedirectResponse(String redirectUrl) throws WdkModelException {
    try {
      return Response.temporaryRedirect(new URI(redirectUrl));
    }
    catch (URISyntaxException e) {
      throw new WdkModelException("Redirect " + redirectUrl + " not a valid URI.");
    }
  }

  /**
   * Convenience method to safely test whether a string has content
   *
   * @param str string to test
   * @return true if empty or null and false otherwise
   */
  private static boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * Web service action that takes parts of a WDK login cookie and verifies that they indeed represent a valid
   * cookie for an existing WDK user. Returns user's display name and email address for use by caller if
   * valid.
   *
   * A JSON object like the following is returned with the information. If the cookie is invalid, the isValid
   * property is set to false and the userData property is undefined.
   *
   * {
   *   "isValid": true,
   *   "userData": {
   *     "id": 7145453,
   *     "displayName": "Ryan Doherty",
   *     "email": "rdoherty@pcbi.upenn.edu"
   *   }
   * }
   */
  @GET
  @Path("login/verification")
  @Produces(MediaType.APPLICATION_JSON)
  public Response processLoginVerification(@QueryParam(COOKIE_KEY) String cookieValue) throws WdkModelException {
    WdkModel wdkModel = getWdkModel();
    return Response.ok(
      getVerificationJsonResult(
        getVerifiedUsername(cookieValue, wdkModel.getModelConfig().getSecretKey()), wdkModel)
    ).build();
  }

  /**
   * Fetches the cookie value parameter, verifies its validity, and returns the username (email) contained
   * within the cookie value. If the cookie is invalid, null is returned
   *
   * @param cookieValue value of the cookie
   * @param secretKey key used to create user hash
   * @return username, or null if not valid
   * @throws WdkModelException if a system problem occurs
   */
  private static String getVerifiedUsername(String cookieValue, String secretKey) throws WdkModelException {
    try {
      String recreatedCookie = cookieValue == null ? "" : cookieValue;
      LoginCookieParts cookieParts = LoginCookieFactory.parseCookieValue(recreatedCookie);
      LoginCookieFactory auth = new LoginCookieFactory(secretKey);
      return (auth.isValidCookie(cookieParts) ? cookieParts.getUsername() : null);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Unable to parse cookie value param.", e);
      return null;
    }
  }

  /**
   * Generates the appropriate JSON object given the username parsed from the cookie value (if any). Even if a
   * non-null username was retrieved, the cookie value may still be deemed invalid if the username does not
   * correspond to an existing user.
   *
   * @param username username to generate JSON for (or null if no username was able to be parsed)
   * @param wdkModel WDK model
   * @return response JSON
   * @throws WdkModelException if a system problem occurs
   */
  private static String getVerificationJsonResult(String username, WdkModel wdkModel) throws WdkModelException {
    try {
      JSONObject result = new JSONObject();
      boolean isValid = (username != null);
      if (isValid) {
        // cookie seems valid; try to get user's name and email
        User user = wdkModel.getUserFactory().getUserByEmail(username);
        if (user == null) {
          // user does not exist; cookie is invalid
          isValid = false;
        }
        else {
          result
            .put(USER_DATA_KEY, new JSONObject()
            .put(USER_ID_KEY, user.getUserId())
            .put(DISPLAY_NAME_KEY, user.getDisplayName())
            .put(EMAIL_KEY, user.getEmail()));
          isValid = true;
        }
      }
      return result.put(IS_VALID_KEY, isValid).toString();
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to generate JSON object from data.", e);
    }
  }
}
