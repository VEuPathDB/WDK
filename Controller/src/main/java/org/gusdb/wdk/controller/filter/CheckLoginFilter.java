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
    LOG.debug("Filter CheckLoginFilter initialized.");
  }

  /**
   * Looks at current session and passed WDK cookie to determine whether action needs to be taken to assign
   * logged-in or guest user to session, or remove user from session, and whether to remove or add cookies.
   * 
   * See comment below for specific cases and what we do about them.
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {

    // load model, user
    WdkModelBean wdkModel = (WdkModelBean) _context.getAttribute(CConstants.WDK_MODEL_KEY);
    UserFactoryBean userFactory = wdkModel.getUserFactory();

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    HttpSession session = request.getSession();

    synchronized(session) {
      try {
        // get the current user in session and determine type
        UserBean wdkUser = (UserBean) session.getAttribute(CConstants.WDK_USER_KEY);
        boolean userPresent = (wdkUser != null);
        boolean isGuestUser = (userPresent ? wdkUser.isGuest() : false);

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

        /*
         * Find out which of the following cases this is:
         * 
         * 1. logged user, matching wdk cookie = no action
         * 2. logged user, unmatching, invalid, or missing wdk cookie = send expired cookie, new guest
         * 3. guest user, any wdk cookie = send expired cookie, keep guest
         * 4. guest user, missing wdk cookie = no action
         * 5. no user, valid wdk cookie = log user in, but do not send updated cookie, since doing so is a bug for GBrowse
         * 6. no user, invalid wdk cookie = send expired cookie, new guest
         * 7. no user, missing wdk cookie = new guest
         */
        int thisCase =
            (userPresent ?
                // user is present cases
                (isGuestUser ?
                    // guest user cases
                    (cookiePresent ? 3 : 4) :
                    // logged user cases
                    (cookieMatches ? 1 : 2)) :
                // no user present cases
                (cookiePresent ?
                    // cookie but no user
                    (cookieValid ? 5 : 6) :
                    // no cookie, no user
                    7));

        // determine actions based on case
        Cookie cookieToSend = null;
        UserBean userToSet = null;
        switch (thisCase) {
          // note cases 1 and 4 require no action
          case 2:
            cookieToSend = LoginCookieFactory.createLogoutCookie();
            userToSet = userFactory.getGuestUser();
            break;
          case 3:
            cookieToSend = LoginCookieFactory.createLogoutCookie();
            // guest user present in session is sufficient
            break;
          case 5:
            // do not want to update max age on cookie since we have no way to tell GBrowse to also update
            userToSet = userFactory.getUserByEmail(cookieParts.getUsername());
            break;
          case 6:
            cookieToSend = LoginCookieFactory.createLogoutCookie();
            userToSet = userFactory.getGuestUser();
            break;
          case 7:
            // no cookie necessary
            userToSet = userFactory.getGuestUser();
            break;
        }

        // take action as needed
        if (cookieToSend != null)
          response.addCookie(cookieToSend);
        if (userToSet != null)
          session.setAttribute(CConstants.WDK_USER_KEY, userToSet);
      }
      catch (Exception ex) {
        LOG.error("Caught exception while checking login cookie: " + ex);
        response.addCookie(LoginCookieFactory.createLogoutCookie());
        throw new ServletException("Unable to complete check-login process", ex);
      }
    }

    // do next filter in chain
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    this._context = null;
    this._config = null;
  }
}
