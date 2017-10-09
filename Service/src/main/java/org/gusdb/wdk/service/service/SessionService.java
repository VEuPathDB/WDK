package org.gusdb.wdk.service.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.gusdb.wdk.model.UIConfig;
import org.gusdb.wdk.model.WdkCookie;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.GuestUser;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.CookieConverter;
import org.gusdb.wdk.session.LoginCookieFactory;

@Path("/")
public class SessionService extends WdkService {

  @GET
  @Path("oauth/login")
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
	session.setAttribute(WDK_USER, user);
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
  
  @GET
  @Path("login/verification")
  public Response processLoginVerification(String body) {
    return null;
  }
  
}
