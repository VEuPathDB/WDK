package org.gusdb.wdk.controller;

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
import org.gusdb.wdk.controller.LoginCookieFactory.LoginCookieParts;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class CheckLoginFilter implements Filter {
  
    private static final Logger LOG = Logger.getLogger(CheckLoginFilter.class.getName());
  
    private FilterConfig config = null;
    private ServletContext context = null;
    private static Logger logger = Logger.getLogger(CheckLoginFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        this.context = config.getServletContext();
        this.context.log("Filter CheckLoginFilter initialized.");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) context.getAttribute(CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) req.getSession().getAttribute(CConstants.WDK_USER_KEY);
        Cookie loginCookie = LoginCookieFactory.findLoginCookie(req.getCookies());
        
        try {
          UserFactoryBean factory = wdkModel.getUserFactory();
          if (loginCookie == null) {
            LOG.debug("Could not find login cookie.  User is: " + wdkUser);
            if (wdkUser != null && !wdkUser.isGuest()) {
              // If there's no login cookie, but a non-guest user is
              // logged in, we should log the user out.
              UserBean guest = factory.getGuestUser();
              logger.error("Logging out non-guest user b/c no login cookie found.");
              req.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);
            }
          }
          else {
            LOG.debug("Found login cookie with value: " + loginCookie.getValue());
            
            // login cookie exists; break value into parts
            LoginCookieFactory auth = new LoginCookieFactory(wdkModel.getSecretKey());
            LoginCookieParts cookieParts = LoginCookieFactory.parseCookieValue(loginCookie.getValue());
            
            if (!auth.isValidCookie(cookieParts)) {
              logger.debug("Secret Value: " + wdkModel.getSecretKey());
              logger.debug("Cookie Hash: " + cookieParts.getChecksum());
              throw new Exception("Login cookie is invalid and must be deleted.");
            }
            
            // cookie is valid; create new auth cookie if:
            //   1. coolie exists but no one is logged in, or
            //   2. current cookie email does not match the logged-in user
            if (wdkUser == null || !cookieParts.getUsername().equals(wdkUser.getEmail())) {

              // get the user represented by the current cookie (if fails, then invalid cookie)
              UserBean cookieUser = factory.getUserByEmail(cookieParts.getUsername());

              // recreate login cookie with new timestamp
              loginCookie = auth.createLoginCookie(cookieUser.getEmail(), cookieParts.isRemember());
              res.addCookie(loginCookie);

              // make sure logged in user matches cookie
              req.getSession().setAttribute(CConstants.WDK_USER_KEY, cookieUser);
              req.getSession().setAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");
            }
          }
        }
        catch (Exception ex) {
          logger.error("Caught exception while checking login " + "cookie: " + ex);
          // tell browser to delete cookie if we had a problem
          res.addCookie(LoginCookieFactory.createLogoutCookie());
          // clear any user out of the session
          req.getSession().setAttribute(CConstants.WDK_USER_KEY, null);
        }

        // set session id
        HttpSession session = req.getSession();
        session.setAttribute("sessionId", session.getId());

        chain.doFilter(request, response);
    }

    public void destroy() {
        this.context = null;
        this.config = null;
    }
}
