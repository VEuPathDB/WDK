package org.gusdb.wdk.controller.filter;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.gusdb.fgputil.web.LoginCookieFactory.LoginCookieParts;
import org.gusdb.fgputil.web.SessionProxy;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.events.NewUserEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.UnregisteredUser.UnregisteredUserType;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

public class CheckLoginFilterShared {

  private static final Logger LOG = Logger.getLogger(CheckLoginFilterShared.class);

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

  public static Optional<CookieBuilder> calculateUserActions(
      Optional<CookieBuilder> currentCookie, ApplicationContext context, SessionProxy session) {

    WdkModel wdkModel = ContextLookup.getWdkModel(context);
    UserFactory userFactory = wdkModel.getUserFactory();

    // three-tuple is: caseNumber, sessionUser, cookieEmail
    StateBundle stateBundle = calculateCurrentState(wdkModel, session, currentCookie);

    // only enter synchronized block if action is required
    //  (i.e. a new user must be added to the session or a logout cookie returned
    if (stateBundle.getCurrentState().requiresAction()) {

      // since action cases require creation of a user and assignment on session, synchronize on session
      synchronized(session.getUnderlyingSession()) { // must sync on shared object

        // recalculate in case something changed outside the synchronized block
        stateBundle = calculateCurrentState(wdkModel, session, currentCookie);

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
          if (userToSet != null) {
            session.setAttribute(Utilities.WDK_USER_KEY, userToSet);
            Events.triggerAndWait(new NewUserEvent(userToSet, stateBundle.getSessionUser(), session),
                new WdkRuntimeException("Unable to complete WDK user assignement."));
          }
          return Optional.ofNullable(cookieToSend);
        }
        catch (Exception ex) {
          LOG.error("Caught exception while checking login cookie", ex);
          return Optional.of(LoginCookieFactory.createLogoutCookie());
        }
      }
    }
    return Optional.empty();
  }

  private static StateBundle calculateCurrentState(WdkModel wdkModel, SessionProxy session, Optional<CookieBuilder> loginCookie) {

    // get the current user in session and determine type
    User wdkUser = (User)session.getAttribute(Utilities.WDK_USER_KEY);
    boolean userPresent = (wdkUser != null);
    boolean isGuestUser = (userPresent ? wdkUser.isGuest() : false);

    // figure out what's going on with the cookie
    boolean cookiePresent = loginCookie.isPresent();
    LoginCookieParts cookieParts = null;
    boolean cookieValid = false, cookieMatches = false;
    try {
      if (cookiePresent) {
        LoginCookieFactory auth = new LoginCookieFactory(wdkModel.getModelConfig().getSecretKey());
        cookieParts = LoginCookieFactory.parseCookieValue(loginCookie.get().getValue());
        cookieValid = auth.isValidCookie(cookieParts);
        cookieMatches = cookieValid ? (wdkUser != null && cookieParts.getUsername().equals(wdkUser.getEmail())) : false;
      }
    }
    catch (IllegalArgumentException e) {
      /* negative values already set */
    }

    CurrentState state = CurrentState.calculateState(userPresent, isGuestUser, cookiePresent, cookieValid, cookieMatches);

    return new StateBundle(state, wdkUser, cookieValid ? cookieParts.getUsername() : null);
  }

}
