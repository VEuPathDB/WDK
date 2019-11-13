package org.gusdb.wdk.controller.filter;

import java.io.IOException;
import java.util.Optional;

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

import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.HttpRequestData;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.session.LoginCookieFactory;

public class CheckLoginFilter implements Filter {

  private ServletContext _context = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    _context = filterConfig.getServletContext();
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

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    ApplicationContext context = ContextLookup.getApplicationContext(_context);
    RequestData requestData = new HttpRequestData(request);

    // note this is not a pure function and may add/change the user on the session
    Optional<CookieBuilder> newCookie =
        CheckLoginFilterShared.calculateUserActions(
            findLoginCookie(request.getCookies()), context, requestData.getSession());

    // add/replace user cookie
    if (newCookie.isPresent()) {
      response.addCookie(newCookie.get().toHttpCookie());
    }

    // do next filter in chain
    chain.doFilter(request, response);
  }

  public static Optional<CookieBuilder> findLoginCookie(Cookie[] cookies) {
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(LoginCookieFactory.WDK_LOGIN_COOKIE_NAME)) {
          return Optional.of(new CookieBuilder(cookie.getName(), cookie.getValue()));
        }
      }
    }
    return Optional.empty();
  }

  @Override
  public void destroy() {
    this._context = null;
  }
}
