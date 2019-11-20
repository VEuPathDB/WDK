package org.gusdb.wdk.session;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.wdk.model.WdkModelException;

/**
 * Generates, parses, and validates WDK login cookies.  When a user logs into
 * the WDK, a cookie is set on the browser containing his username (email),
 * whether to remember the user (i.e. until logout), and a checksum to help
 * ensure the cookies are not used for malicious purposes.  The cookie is of
 * the form:
 *             emailAddr[-remember]-checksum
 * For example:
 *             rdoherty@pcbi.upenn.edu-d50806e5be4399b643ee2aa56a9fa3b1
 * 
 * @author rdoherty
 */
public class LoginCookieFactory {

  // package protected for unit test access
  public static final String WDK_LOGIN_COOKIE_NAME = "wdk_check_auth";
  private static final String REMEMBER_SUFFIX = "-remember";

  /**
   * A simple container for the various parts of a WDK login cookie value
   * 
   * @author rdoherty
   */
  public static class LoginCookieParts {
    private String _username;
    private boolean _remember;
    private String _checksum;
    public LoginCookieParts(String username, boolean remember, String checksum) {
      _username = username; _remember = remember; _checksum = checksum;
    }
    public String getUsername() { return _username; }
    public boolean isRemember() { return _remember; }
    public String getChecksum() { return _checksum; }

    @Override
    public String toString() { return "{ " + _username + ", " + _remember + ", " + _checksum + " }"; }
  }
  
  private String _secretKey;
  
  /**
   * Creates a factory with the given secret key.  All parsing and generation
   * of cookies will use this key.
   * 
   * @param secretKey secret key this factory should use
   */
  public LoginCookieFactory(String secretKey) {
    _secretKey = secretKey;
  }
  
  /**
   * Creates a new login cookie using the given username and whether to
   * remember the user after the session expires.
   * 
   * @param username user name (email address)
   * @param remember whether to remember the user after session expires
   * @return new login cookie
   * @throws WdkModelException if a system problem occurs
   */
  public CookieBuilder createLoginCookie(String username, boolean remember) throws WdkModelException {
    CookieBuilder loginCookie = new CookieBuilder(WDK_LOGIN_COOKIE_NAME, "");
    loginCookie. setPath("/"); // set cookie for whole site, not just webapp
    loginCookie.setMaxAge(remember ? getDefaultMaxAge() : -1);
    loginCookie.setValue(FormatUtil.urlEncodeUtf8(getCookieValue(username, remember)));
    return loginCookie;
  }

  public static int getDefaultMaxAge() {
    return Integer.MAX_VALUE / 256;
  }

  /**
   * Creates a new login cookie using the given username and maxAge.
   * 
   * @param username user name (email address)
   * @param maxAge maxAge value to set on cookie
   * @return new login cookie
   * @throws WdkModelException if a system problem occurs
   */
  public CookieBuilder createLoginCookie(String username, int maxAge) throws WdkModelException {
    CookieBuilder loginCookie = createLoginCookie(username, false);
    loginCookie.setMaxAge(maxAge);
    return loginCookie;
  }
  
  /**
   * Creates a logout cookie.  This is a login cookie that expires immediately
   * (i.e. the browser will delete the cookie when it receives it).
   * 
   * @return logout cookie
   */
  public static CookieBuilder createLogoutCookie() {
    CookieBuilder cookie = new CookieBuilder(WDK_LOGIN_COOKIE_NAME, "");
    cookie.setMaxAge(0);
    cookie.setPath("/");
    return cookie;
  }
  
  /**
   * Parses the cookie value into username/email, remember flag, and checksum.
   * Does NOT check the validity of the checksum.  Use
   * <code>isValidCookie()</code> to confirm that checksum value is valid.
   * 
   * @param cookieValue value of a WDK auth cookie
   * @return object representing parsed cookie parts
   * @throws WdkModelException if unable to decode cookie string using UTF-8 encoding
   * @throws IllegalArgumentException if cookie value is malformed
   */
  public static LoginCookieParts parseCookieValue(String cookieValue) throws WdkModelException {
    String errorMsg = "Unparsable cookie value: " + cookieValue;
    if (cookieValue == null) throw new IllegalArgumentException(errorMsg);
    cookieValue = FormatUtil.urlDecodeUtf8(cookieValue);
    int hashDashIndex = cookieValue.lastIndexOf('-');
    if (hashDashIndex == -1) throw new IllegalArgumentException(errorMsg);
    String checksum = cookieValue.substring(hashDashIndex + 1);
    String name = cookieValue.substring(0, hashDashIndex);
    boolean remember = false;
    if (name.endsWith(REMEMBER_SUFFIX)) {
      name = name.substring(0, name.length() - REMEMBER_SUFFIX.length());
      remember = true;
    }
    if (name.isEmpty() || checksum.isEmpty()) {
      throw new IllegalArgumentException(errorMsg);
    }
    return new LoginCookieParts(name, remember, checksum);
  }
  
  /**
   * Returns true if the cookie value represented by the parameter is valid.
   * This means it will ensure the checksum is valid against the other two
   * fields using the current secret key.  If the key has changed since the
   * cookie was generated, this method will return false.  It does NOT check
   * whether the username represents a current user, however.
   * 
   * @param cookieParts parts object representing cookie value
   * @return true if value is valid as defined above, else false
   */
  public boolean isValidCookie(LoginCookieParts cookieParts) {
    String hashInput = addRemember(cookieParts.getUsername(), cookieParts.isRemember());
    String secretValue = getCookieHash(hashInput);
    return secretValue.equals(cookieParts.getChecksum());
  }

  private String getCookieValue(String username, boolean remember) {
    String uncodedCookieValue = addRemember(username, remember);
    uncodedCookieValue += "-" + getCookieHash(uncodedCookieValue);
    return uncodedCookieValue;
  }

  private String getCookieHash(String hashInput) {
    return EncryptionUtil.md5(hashInput + _secretKey);
  }
  
  private static String addRemember(String orig, boolean remember) {
    return orig + (remember ? REMEMBER_SUFFIX : "");
  }

}
