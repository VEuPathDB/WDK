package org.gusdb.wdk.controller.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.LoginCookieFactory;
import org.gusdb.wdk.controller.LoginCookieFactory.LoginCookieParts;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class CheckLoginFilter implements Filter {

  private static final Logger LOG = Logger.getLogger(CheckLoginFilter.class);

  private FilterConfig _config = null;
  private ServletContext _context = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    _config = filterConfig;
    _context = _config.getServletContext();
    _context.log("Filter CheckLoginFilter initialized.");
  }

  /**
   * Looks at currently session and passed WDK cookie to determine whether action needs to be taken to assign
   * logged-in or guest user to session, remove user from session, and remove or add cookies.
   * 
   * Handles the following cases: logged user, matching wdk cookie = no action logged user, unmatching,
   * invalid, or missing wdk cookie = send expired cookie, new guest guest user, any wdk cookie = send expired
   * cookie, keep guest guest user, missing wdk cookie = no action no user, valid wdk cookie = log user in,
   * send updated cookie <- THIS IS A BUG FOR GBROWSE no user, invalid wdk cookie = send expired cookie, new
   * guest no user, missing wdk cookie = new guest
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    HttpSession session = request.getSession();

    try {

      // clear any login error from previous request; if user is not trying to
      // log in with multiple requests simultaneously, he probably doesn't
      // care about the reason his logins are failing
      session.setAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");

      // load model, user
      WdkModelBean wdkModel = (WdkModelBean) _context.getAttribute(CConstants.WDK_MODEL_KEY);
      UserFactoryBean userFactory = wdkModel.getUserFactory();
      UserBean wdkUser = (UserBean) session.getAttribute(CConstants.WDK_USER_KEY);

      // figure out what's going on with the cookie
      Cookie loginCookie = LoginCookieFactory.findLoginCookie(request.getCookies());
      boolean cookiePresent = (loginCookie != null);
      LoginCookieParts cookieParts = null;
      boolean cookieValid = false, cookieMatches = false;
      try {
        if (cookiePresent) {
          LoginCookieFactory auth = new LoginCookieFactory(wdkModel.getSecretKey());
          cookieParts = LoginCookieFactory.parseCookieValue(loginCookie.getValue());
          cookieValid = auth.isValidCookie(cookieParts);
          cookieMatches = (wdkUser != null && cookieParts.getUsername().equals(wdkUser.getEmail()));
        }
      }
      catch (IllegalArgumentException | WdkModelException e) {
        /* negative values already set */
      }

      // handle cases
      // find the cases where the current user is ok
      if (wdkUser != null && (wdkUser.isGuest() || cookieMatches)) {
        if (wdkUser.isGuest() && cookiePresent) {
          // Weird situation: guest on back end but cookie on front end?
          // Could be spammer; just use guest and remove cookie
          response.addCookie(LoginCookieFactory.createLogoutCookie());
        }
      }
      else { // otherwise, a user needs to be created or loaded
        synchronized (session) {
          // get the user and do the check again, in case a user was loaded after last check, but before we
          // enter the synchronized block
          wdkUser = (UserBean) session.getAttribute(CConstants.WDK_USER_KEY);
          cookieMatches = (wdkUser != null && cookieParts.getUsername().equals(wdkUser.getEmail()));
          if (wdkUser != null && (wdkUser.isGuest() || cookieMatches)) {
            // do nothing,user is good, and will use this user
          }
          else { // will create or load a user, and store into session
            // need to clean the cookie if something is wrong
            if (wdkUser == null) {
              if (!cookieValid && cookiePresent)
                response.addCookie(LoginCookieFactory.createLogoutCookie());
            }
            else if ((wdkUser.isGuest() && cookiePresent) || (!wdkUser.isGuest() && !cookieMatches)) {
              response.addCookie(LoginCookieFactory.createLogoutCookie());
            }
            wdkUser = (wdkUser == null && cookieValid)
                ? userFactory.getUserByEmail(cookieParts.getUsername()) : userFactory.getGuestUser();
            session.setAttribute(CConstants.WDK_USER_KEY, wdkUser);
          }
        }
      }

      //
      // if (wdkUser == null) {
      // if (cookieValid) {
      // // get the user represented by the current cookie and set in session
      // setUser(session, userFactory.getUserByEmail(cookieParts.getUsername()));
      // }
      // else {
      // if (cookiePresent) {
      // // cookie is not valid; remove it
      // response.addCookie(LoginCookieFactory.createLogoutCookie());
      // }
      // // give session a new guest user
      // setUser(session, userFactory.getGuestUser());
      // }
      // }
      // else if (wdkUser.isGuest()) {
      // if (cookiePresent) {
      // // Weird situation: guest on back end but cookie on front end?
      // // Could be spammer; just use guest and remove cookie
      // response.addCookie(LoginCookieFactory.createLogoutCookie());
      // }
      // }
      // else if (!cookieMatches) {
      // // clear old cookie and log out current user; give session new guest
      // response.addCookie(LoginCookieFactory.createLogoutCookie());
      // setUser(session, userFactory.getGuestUser());
      // }
      //
    }
    catch (Exception ex) {
      LOG.error("Caught exception while checking login cookie: " + ex);
      response.addCookie(LoginCookieFactory.createLogoutCookie());
      throw new ServletException("Unable to complete check-login process", ex);
    }

    // do next filter in chain
    chain.doFilter(request, response);
  }

  // private void setUser(HttpSession session, UserBean user) {
  // session.setAttribute(CConstants.WDK_USER_KEY, user);
  // }

  @Override
  public void destroy() {
    this._context = null;
    this._config = null;
  }
}
