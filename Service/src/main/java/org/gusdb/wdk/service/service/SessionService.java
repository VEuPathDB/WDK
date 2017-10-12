package org.gusdb.wdk.service.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.UIConfig;
import org.gusdb.wdk.model.WdkCookie;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfig.AuthenticationMethod;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.GuestUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.service.CookieConverter;
import org.gusdb.wdk.service.statustype.MethodNotAllowedStatusType;
import org.gusdb.wdk.session.LoginCookieFactory;
import org.gusdb.wdk.session.OAuthUtil;
import org.gusdb.wdk.session.LoginCookieFactory.LoginCookieParts;
import org.gusdb.wdk.session.OAuthClient;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/")
public class SessionService extends WdkService {
	
  private static final Logger LOG = Logger.getLogger(SessionService.class);
  
  public static final String WDK_ERROR_KEY = "error";
  public static final String WDK_REFERER_KEY = "Referer";
  public static final String WDK_STATE_KEY = "state";
  public static final String WDK_CODE_KEY = "code";
  public static final String WDK_LOGIN_ERROR_KEY = "loginError";
  public static final String WDK_REDIRECT_URL_KEY = "redirectUrl";
  public static final String WDK_LOGIN_PAGE = "Login.jsp";

  // input param constants
  private static final String COOKIE_KEY = "wdkLoginCookieValue";

  // json output constants
  private static final String IS_VALID_KEY = "isValid";
  private static final String USER_DATA_KEY = "userData";
  private static final String USER_ID_KEY = "id";
  private static final String DISPLAY_NAME_KEY = "displayName";
  private static final String EMAIL_KEY = "email";
  
  private static final String HOME_URL = "home.do";
  private static final String ALREADY_LOGGED_IN_URL = "showApplicaton.do";
  private static final String ERROR_URL = "/wdkCustomization/jsp/Error.jsp?message=";
  
  @GET
  @Path("login")
  public Response processOauthLogin(
		  @HeaderParam(WDK_REFERER_KEY) String referer,
		  @QueryParam(WDK_ERROR_KEY) String error,
		  @QueryParam(WDK_STATE_KEY) String stateToken,
		  @QueryParam(WDK_CODE_KEY) String authCode,
		  @QueryParam(WDK_REDIRECT_URL_KEY) String originalUrl)
		   throws WdkModelException {
	AuthenticationMethod authMethod = getWdkModel().getModelConfig().getAuthenticationMethodEnum();
	if(!AuthenticationMethod.OAUTH2.equals(authMethod)) {
	  return Response.status(new MethodNotAllowedStatusType()).build();
	}
	WdkModelBean wdkModelBean = new WdkModelBean(getWdkModel());
	ModelConfig modelConfig = getWdkModel().getModelConfig();
	HttpSession session = getSession();
	String appUrl = modelConfig.getWebAppUrl();
	String errorMessage = "";
	
	String redirectUrl = isEmpty(originalUrl) ? appUrl + HOME_URL : originalUrl;
	
	UserBean wdkUserBean = getSessionUserBean();
	
	// Is the user already logged in?
	if(!wdkUserBean.isGuest()) {
	  redirectUrl = appUrl + ALREADY_LOGGED_IN_URL;
	}
	
	// check for error or to see if user denied us access; they won't be able to log in
    if (error != null) {
      errorMessage = error.equals("access_denied") ?
        "You did not grant permission to access identifying information so we cannot log you in." :
        "An error occurred [" + error + "] on the authentication server and you cannot log in at this time.";
      return setupResponseBuilder( appUrl + ERROR_URL + FormatUtil.urlEncodeUtf8(errorMessage)).build();
    }
    
    // Is the state token present and does it match the session state token?
    String storedStateToken = (String)getSession().getAttribute(OAuthUtil.STATE_TOKEN_KEY);
    session.removeAttribute(OAuthUtil.STATE_TOKEN_KEY);
    if (stateToken == null || storedStateToken == null || !stateToken.equals(storedStateToken)) {
      errorMessage = "Unable to log in state token missing, incorrect, or expired.  Please try again.";
      return setupResponseBuilder( appUrl + ERROR_URL + FormatUtil.urlEncodeUtf8(errorMessage)).build();
    }

    // Is there a matching user id for the auth code provided?
    UserBean userBean = null;
    UserFactoryBean userFactoryBean = wdkModelBean.getUserFactory();
    try {
      OAuthClient client = new OAuthClient(modelConfig, userFactoryBean);
      long userId = client.getUserIdFromAuthCode(authCode);
      userBean = userFactoryBean.login(wdkUserBean, userId);
      if(userBean == null) {
    	errorMessage = "Unable to find user with ID " + userId + ", returned by OAuth service for authCode " + authCode;
    	throw new WdkModelException(errorMessage);
      }  
    }
    catch(Exception ex) {
      LOG.error("Could not log user in with authCode " + authCode, ex);
      errorMessage = isEmpty(errorMessage) ? "Could not log user in with authCode " + authCode : errorMessage; 
      return setupResponseBuilder(appUrl + ERROR_URL + FormatUtil.urlEncodeUtf8(errorMessage)).build();
    }
    
    // Set up session attributes and login cookie
    LoginCookieFactory auth = new LoginCookieFactory(wdkModelBean.getModel().getSecretKey());
    Cookie loginCookie = auth.createLoginCookie(userBean.getEmail(), true);
    getSession().setAttribute(WDK_USER, userBean);
	getSession().setAttribute(WDK_LOGIN_ERROR_KEY, "");  
	ResponseBuilder builder = setupResponseBuilder(redirectUrl);
    builder.cookie(CookieConverter.toJaxRsCookie(loginCookie));
    return builder.build();
  }
	
  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response processDbLogin(String body) {
    return null;
  }
  
  @GET
  @Path("logout")
  public Response processLogout(String body) throws WdkModelException {
	HttpSession session = this.getSession();
	if (session != null) {
      session.invalidate();
    }
    session = getSession(true);
	User user = new GuestUser(getWdkModel());
	UserBean userBean = new UserBean(user);
	session.setAttribute(WDK_USER, userBean);
	Set<NewCookie> logoutCookies = new HashSet<>();
	logoutCookies.add(CookieConverter.toJaxRsCookie(LoginCookieFactory.createLogoutCookie()));    
	UIConfig uiConfig = getWdkModel().getUiConfig();
    for (WdkCookie wdkCookie : uiConfig.getExtraLogoutCookies()) {
      Cookie extraCookie = new Cookie(wdkCookie.getName(), "");
      extraCookie.setPath(wdkCookie.getPath());
      extraCookie.setMaxAge(-1);
      logoutCookies.add(CookieConverter.toJaxRsCookie(extraCookie));
    }
    ResponseBuilder builder;
	try {
      builder = Response.temporaryRedirect(new URI(getWdkModel().getModelConfig().getWebAppUrl() + "/home.do"));
	} 
	catch (URISyntaxException e) {
	  throw new WdkModelException("Home page not found.");	
	}	     
    for(NewCookie logoutCookie : logoutCookies) {
      builder.cookie(logoutCookie);
    }
    return builder.build();
  }
  
  /**
  * Web service action that takes parts of a WDK login cookie and verifies
  * that they indeed represent a valid cookie for an existing WDK user.
  * Returns user's display name and email address for use by caller if valid.
  * 
  * A JSON object like the following is returned with the information.  If the
  * cookie is invalid, the isValid property is set to false and the userData
  * property is undefined.
  * 
  * {
  *   "isValid": true,
  *   "userData": {
  *     "id" : 7145453,
  *     "displayName" : "Ryan Doherty",
  *     "email" : "rdoherty@pcbi.upenn.edu"
  *   }
  * }
  * 
  */
  @GET
  @Path("login/verification")
  @Produces(MediaType.APPLICATION_JSON)
  public Response processLoginVerification(@QueryParam(COOKIE_KEY) String cookieValue) throws WdkModelException {
	return Response.ok(getJsonResult(getUsername(cookieValue))).build();
  }
  
  /**
   * Fetches the cookie value parameter, verifies its validity, and returns
   * the username (email) contained within the cookie value.  If the cookie is
   * invalid, null is returned
   * 
   * @param params request parameters
   * @return username, or an empty string
   * @throws WdkModelException if a system problem occurs
   */
  private String getUsername(String cookieValue) throws WdkModelException {
    try {
      String recreatedCookie = cookieValue == null ? "" : cookieValue;
      LoginCookieParts cookieParts = LoginCookieFactory.parseCookieValue(recreatedCookie);
      LoginCookieFactory auth = new LoginCookieFactory(getWdkModel().getSecretKey());
      return (auth.isValidCookie(cookieParts) ? cookieParts.getUsername() : null);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Unable to parse cookie value param.", e);
      return null;
    }
  }
  
  /**
   * Generates the appropriate JSON object given the username parsed from the
   * cookie value (if any).  Even if a non-null username was retrieved, the
   * cookie value may still be deemed invalid if the username does not
   * correspond to an existing user.
   * 
   * @param username username to generate JSON for (or null if no username
   * was able to be parsed)
   * @return response JSON
   * @throws WdkModelException if a system problem occurs
   */
  protected String getJsonResult(String username) throws WdkModelException {
    try {
      JSONObject result = new JSONObject();
      boolean isValid = (username != null);
      if (isValid) {
        try {
          // cookie seems valid; try to get user's name and email
          WdkModelBean wdkModelBean = new WdkModelBean(getWdkModel());
          UserBean user = wdkModelBean.getUserFactory().getUserByEmail(username);
          JSONObject userData = new JSONObject();
          userData.put(USER_ID_KEY, user.getUserId());
          userData.put(DISPLAY_NAME_KEY, user.getFirstName() + " " + user.getLastName());
          userData.put(EMAIL_KEY, user.getEmail());
          result.put(USER_DATA_KEY, userData);
          isValid = true;
        }
        catch (WdkUserException e) {
          // user does not exist; cookie is invalid
          isValid = false;
        }
      }
      result.put(IS_VALID_KEY, isValid);
      return result.toString();
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to generate JSON object from data.", e);
    }
  }
  
  /**
   * Convenience method to set up a response builder with the redirect url provided.
   * @param redirectUrl - url of page to which to redirect the user
   * @return - partially set up response builder
   * @throws WdkModelException - if the url cannot be found or is invalid.
   */
  protected ResponseBuilder setupResponseBuilder(String redirectUrl) throws WdkModelException {
	try {
      return Response.temporaryRedirect(new URI(redirectUrl));
	} 
	catch (URISyntaxException e) {
	  throw new WdkModelException("Redirect " + redirectUrl + " not found.");	
	}	     
  }
  
  
  /**
   * Convenience method to safely test whether a string has content
   * @param str - string to test
   * @return - true if empty or null and false otherwise
   */
  protected static boolean isEmpty(String str) {
	return str == null || str.trim().length() == 0;
  }
  
}
