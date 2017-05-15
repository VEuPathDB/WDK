package org.gusdb.wdk.service;

import javax.ws.rs.core.NewCookie;

/**
 * I can't believe this class is needed.
 * 
 * @author rdoherty
 */
public class CookieConverter {

  public static NewCookie toJaxRsCookie(javax.servlet.http.Cookie cookie) {
    return new NewCookie(
        cookie.getName(),
        cookie.getValue(),
        cookie.getPath(),
        cookie.getDomain(),
        cookie.getVersion(),
        cookie.getComment(),
        cookie.getMaxAge(),
        cookie.getSecure()
    );
  }

}
