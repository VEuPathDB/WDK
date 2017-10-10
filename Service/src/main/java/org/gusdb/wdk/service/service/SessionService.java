package org.gusdb.wdk.service.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.gusdb.wdk.model.UIConfig;
import org.gusdb.wdk.model.WdkCookie;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.GuestUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.CookieConverter;
import org.gusdb.wdk.session.LoginCookieFactory;
import org.gusdb.wdk.session.LoginCookieFactory.LoginCookieParts;
import org.json.JSONObject;

@Path("/")
public class SessionService extends WdkService {
	
  private static final String COOKIE_KEY = "wdkLoginCookieValue";

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
    String recreatedCookie = cookieValue == null ? "" : cookieValue;
    LoginCookieParts cookieParts = LoginCookieFactory.parseCookieValue(recreatedCookie);
    LoginCookieFactory auth = new LoginCookieFactory(getWdkModel().getSecretKey());
    User user = getWdkModel().getUserFactory().getUserByEmail(cookieParts.getUsername());
    JSONObject loginVerificationJson = new JSONObject()
    		.put("isValid", auth.isValidCookie(cookieParts))
    		.put("userData", new JSONObject()
    			    .put("id", user.getUserId())
    				.put("displayName", user.getDisplayName())
    				.put("email", cookieParts.getUsername()));
	return Response.ok(loginVerificationJson.toString()).build();
  }
  
}
