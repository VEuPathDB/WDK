package org.gusdb.wdk.service.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.UIConfig;
import org.gusdb.wdk.model.WdkCookie;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.GuestUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.CookieConverter;
import org.gusdb.wdk.session.LoginCookieFactory;
import org.gusdb.wdk.session.LoginCookieFactory.LoginCookieParts;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/")
public class SessionService extends WdkService {
	
  private static final Logger LOG = Logger.getLogger(SessionService.class);

  // input param constants
  private static final String COOKIE_KEY = "wdkLoginCookieValue";

  // json output constants
  private static final String IS_VALID_KEY = "isValid";
  private static final String USER_DATA_KEY = "userData";
  private static final String USER_ID_KEY = "id";
  private static final String DISPLAY_NAME_KEY = "displayName";
  private static final String EMAIL_KEY = "email";
	  
  @GET
  @Path("login")
  public Response processOauthLogin(String body) {
    return null;
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
  
}
