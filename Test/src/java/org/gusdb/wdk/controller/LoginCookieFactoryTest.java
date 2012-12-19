package org.gusdb.wdk.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.Cookie;

import org.gusdb.wdk.controller.LoginCookieFactory.LoginCookieParts;
import org.gusdb.wdk.model.WdkModelException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LoginCookieFactoryTest {

  private static final String SECRET_KEY = "123aSecretKey!";
  private static final String ANOTHER_KEY = "345TakeADive!";

  private static final String TRUE = "true";
  private static final String FALSE = "false";
  
  // cookie lists
  private static final Cookie[] BAD_LIST = { new Cookie("a","1"), new Cookie("b", "2") };
  private static final Cookie[] GOOD_LIST = { new Cookie("a", "1"), new Cookie(LoginCookieFactory.WDK_LOGIN_COOKIE_NAME, "value") };
  
  @Test
  public void testFindCookie() throws Exception {
    Cookie c;
    // test null
    c = LoginCookieFactory.findLoginCookie(null);
    assertNull(c);
    // test empty list
    c = LoginCookieFactory.findLoginCookie(new Cookie[0]);
    assertNull(c);
    // test list not containing cookie
    c = LoginCookieFactory.findLoginCookie(BAD_LIST);
    assertNull(c);
    // test list not containing cookie
    c = LoginCookieFactory.findLoginCookie(GOOD_LIST);
    assertNotNull(c);
    assertEquals(c.getName(), LoginCookieFactory.WDK_LOGIN_COOKIE_NAME);
  }

  public static final String[] EXCEPTION_CASES = {
    // failure cases
    null, "", "word", "word-", "-word", "word--", "word-word-" // others???
  };

  public static final String[][] PARSE_CASES = {
    // obvious success cases
    { "email-checksum", "email", FALSE, "checksum" },
    { "email-remember-checksum", "email", TRUE, "checksum" },

    // more tricky "success" cases
    { "--word", "-", FALSE, "word" },
    { "email--checksum", "email-", FALSE, "checksum" },
    { "-word-word", "-word", FALSE, "word" },
    { "blah-word--remember-checksum", "blah-word-", TRUE, "checksum" }
  };
  
  @Test
  public void testParseCookie() throws Exception {
    for (String failCase : EXCEPTION_CASES) {
      try {
        LoginCookieFactory.parseCookieValue(failCase);
        throw new Exception("IllegalArgumentException should have been thrown before here for case: " + failCase);
      }
      catch (IllegalArgumentException expectedException) {
        // do nothing; this was expected
      }
    }
    for (String[] successCase : PARSE_CASES) {
      LoginCookieParts parts = LoginCookieFactory.parseCookieValue(successCase[0]);
      assertEquals(parts.getUsername(), successCase[1]);
      assertEquals(new Boolean(parts.isRemember()).toString(), successCase[2]);
      assertEquals(parts.getChecksum(), successCase[3]);
    }
  }
  
  public static final String REMEMBER_MAX_AGE = new Integer(java.lang.Integer.MAX_VALUE / 256).toString();
  public static final String NO_REMEMBER_MAX_AGE = "-1";
  public static final String COOKIE_PATH = "/";
  
  public static final String[][] COOKIE_CASES = {
    { "rdoherty@pcbi.upenn.edu", "true", REMEMBER_MAX_AGE,
      "rdoherty%40pcbi.upenn.edu-remember-b9f3b04fca9893df7a486b68f42db8e8" },
    { "rdoherty@pcbi.upenn.edu", "false", NO_REMEMBER_MAX_AGE,
      "rdoherty%40pcbi.upenn.edu-2bcea64085894809f6828969f4ea1c27" }
  };
  
  @Test
  public void testCreateCookie() throws Exception {
    LoginCookieFactory factory = new LoginCookieFactory(SECRET_KEY);
    for (String[] data : COOKIE_CASES) {
      Cookie c = factory.createLoginCookie(data[0], Boolean.parseBoolean(data[1]));
      assertEquals(c.getMaxAge(), Integer.parseInt(data[2]));
      assertEquals(c.getValue(), data[3]);
      assertEquals(c.getPath(), COOKIE_PATH);
    }
  }
  
  @Test
  public void testValidateCookie() throws Exception {
    LoginCookieFactory factory = new LoginCookieFactory(SECRET_KEY);
    for (String[] data : COOKIE_CASES) {
      LoginCookieParts parts = LoginCookieFactory.parseCookieValue(data[3]);
      assertTrue(factory.isValidCookie(parts));
    }
    
    // when we salt with a different key, all should fail
    factory = new LoginCookieFactory(ANOTHER_KEY);
    for (String[] data : COOKIE_CASES) {
      LoginCookieParts parts = LoginCookieFactory.parseCookieValue(data[3]);
      assertFalse(factory.isValidCookie(parts));
    }
  }
  
  //public Cookie createLoginCookie(String username, boolean remember) throws WdkModelException;
  //public static Cookie findLoginCookie(Cookie[] cookies);
  //public static LoginCookieParts parseCookieValue(String cookieValue) throws WdkModelException;
  //public boolean isValidCookie(LoginCookieParts cookieParts);  
}
