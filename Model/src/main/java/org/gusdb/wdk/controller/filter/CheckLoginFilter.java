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
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.HttpSessionProxy;
import org.gusdb.wdk.events.NewUserEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.UnregisteredUser.UnregisteredUserType;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.session.LoginCookieFactory;
import org.gusdb.wdk.session.LoginCookieFactory.LoginCookieParts;

public class CheckLoginFilter implements Filter {

  private static final Logger LOG = Logger.getLogger(CheckLoginFilter.class);

  private enum CurrentState {

    REGISTERED_USER_MATCHING_COOKIE, // 1. registered user, matching wdk cookie = no action
    REGISTERED_USER_BAD_COOKIE,      // 2. registered user, unmatching, invalid, or missing wdk cookie = send expired cookie, new guest
    GUEST_USER_COOKIE_PRESENT,       // 3. guest user, any wdk cookie = send expired cookie, keep guest
    GUEST_USER_COOKIE_MISSING,       // 4. guest user, missing wdk cookie = no action
    NO_USER_VALID_COOKIE,            // 5. no user, valid wdk cookie = log user in, but do not send updated cookie, since doing so is a bug for GBrowse
    NO_USER_INVALID_COOKIE,          // 6. no user, invalid wdk cookie = send expired cookie, new guest
    NO_USER_MISSING_COOKIE;          // 7. no user, missing wdk cookie = new guest

    public boolean requiresAction() {
      return !equals(REGISTERED_USER_MATCHING_COOKIE) &&
             !equals(GUEST_USER_COOKIE_MISSING);
    }

    public static CurrentState calculateState(
        boolean userPresent,
        boolean isGuestUser,
        boolean cookiePresent,
        boolean cookieValid,
        boolean cookieMatches) {
      return
        (userPresent ?
          // user is present cases
          (isGuestUser ?
            // guest user cases
            (cookiePresent ? GUEST_USER_COOKIE_PRESENT : GUEST_USER_COOKIE_MISSING) :
            // logged user cases
            (cookieMatches ? REGISTERED_USER_MATCHING_COOKIE : REGISTERED_USER_BAD_COOKIE)) :
          // no user present cases
          (cookiePresent ?
            // cookie but no user
            (cookieValid ? NO_USER_VALID_COOKIE : NO_USER_INVALID_COOKIE) :
            // no cookie, no user
            NO_USER_MISSING_COOKIE));
    }
  }

  private static class StateBundle extends ThreeTuple<CurrentState, User, String> {

    public StateBundle(CurrentState state, User sessionUser, String cookieEmail) {
      super(state, sessionUser, cookieEmail);
    }

    public CurrentState getCurrentState() { return getFirst(); }
    public User getSessionUser() { return getSecond(); }
    public String getCookieEmail() { return getThird(); }

  }

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
    WdkModel wdkModel = (WdkModel)_context.getAttribute(Utilities.WDK_MODEL_KEY);
    UserFactory userFactory = wdkModel.getUserFactory();

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    HttpSession session = request.getSession();

    // three-tuple is: caseNumber, sessionUser, cookieEmail
    StateBundle stateBundle = calculateCurrentState(wdkModel, session, request);

    // only enter synchronized block if action is required
    //  (i.e. a new user must be added to the session or a logout cookie returned
    if (stateBundle.getCurrentState().requiresAction()) {

      // since action cases require creation of a user and assignment on session, synchronize on session
      synchronized(session) {

        // recalculate in case something changed outside the synchronized block
        stateBundle = calculateCurrentState(wdkModel, session, request);

        try {
          // determine actions based on state
          CookieBuilder cookieToSend = null;
          User userToSet = null;
          switch (stateBundle.getCurrentState()) {
            case REGISTERED_USER_BAD_COOKIE:
              cookieToSend = LoginCookieFactory.createLogoutCookie();
              userToSet = userFactory.createUnregistedUser(UnregisteredUserType.GUEST);
              break;
            case GUEST_USER_COOKIE_PRESENT:
              cookieToSend = LoginCookieFactory.createLogoutCookie();
              // guest user present in session is sufficient
              break;
            case NO_USER_VALID_COOKIE:
              // do not want to update max age on cookie since we have no way to tell GBrowse to also update
              userToSet = userFactory.getUserByEmail(stateBundle.getCookieEmail());
              break;
            case NO_USER_INVALID_COOKIE:
              cookieToSend = LoginCookieFactory.createLogoutCookie();
              userToSet = userFactory.createUnregistedUser(UnregisteredUserType.GUEST);
              break;
            case NO_USER_MISSING_COOKIE:
              // no cookie necessary
              userToSet = userFactory.createUnregistedUser(UnregisteredUserType.GUEST);
              break;
            default:
              // other cases require no action
              break;
          }
  
          // take action as needed
          if (cookieToSend != null) {
            response.addCookie(cookieToSend.toHttpCookie());
          }
          if (userToSet != null) {
            session.setAttribute(Utilities.WDK_USER_KEY, userToSet);
            Events.triggerAndWait(new NewUserEvent(userToSet, stateBundle.getSessionUser(), new HttpSessionProxy(session)),
                new WdkRuntimeException("Unable to complete WDK user assignement."));
          }
        }
        catch (Exception ex) {
          LOG.error("Caught exception while checking login cookie: " + ex);
          response.addCookie(LoginCookieFactory.createLogoutCookie().toHttpCookie());
          throw new ServletException("Unable to complete check-login process", ex);
        }
      }
    }

    // do next filter in chain
    chain.doFilter(request, response);
  }

  private StateBundle calculateCurrentState(WdkModel wdkModel, HttpSession session, HttpServletRequest request) {

    // get the current user in session and determine type
    User wdkUser = (User)session.getAttribute(Utilities.WDK_USER_KEY);
    boolean userPresent = (wdkUser != null);
    boolean isGuestUser = (userPresent ? wdkUser.isGuest() : false);

    // figure out what's going on with the cookie
    Cookie loginCookie = findLoginCookie(request.getCookies());
    boolean cookiePresent = (loginCookie != null);
    LoginCookieParts cookieParts = null;
    boolean cookieValid = false, cookieMatches = false;
    try {
      if (cookiePresent) {
        LoginCookieFactory auth = new LoginCookieFactory(wdkModel.getModelConfig().getSecretKey());
        cookieParts = LoginCookieFactory.parseCookieValue(loginCookie.getValue());
        cookieValid = auth.isValidCookie(cookieParts);
        cookieMatches = cookieValid ? (wdkUser != null && cookieParts.getUsername().equals(wdkUser.getEmail())) : false;
      }
    }
    catch (IllegalArgumentException | WdkModelException e) {
      /* negative values already set */
    }

    CurrentState state = CurrentState.calculateState(userPresent, isGuestUser, cookiePresent, cookieValid, cookieMatches);

    return new StateBundle(state, wdkUser, cookieValid ? cookieParts.getUsername() : null);
  }

  public static Cookie findLoginCookie(Cookie[] cookies) {
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(LoginCookieFactory.WDK_LOGIN_COOKIE_NAME)) {
        return cookie;
      }
    }
    return null;
  }
  
  @Override
  public void destroy() {
    this._context = null;
    this._config = null;
  }
}
