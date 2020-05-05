package org.gusdb.wdk.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import javax.servlet.http.Cookie;

import org.gusdb.fgputil.web.CookieBuilder;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.gusdb.fgputil.web.LoginCookieFactory.LoginCookieParts;
import org.gusdb.wdk.controller.filter.CheckLoginFilter;
import org.junit.Test;

public class LoginCookieFactoryTest {

  private static final String SECRET_KEY = "123aSecretKey!";
  private static final String ANOTHER_KEY = "345TakeADive!";

  // cookie lists
  private static final Cookie[] BAD_LIST = { new Cookie("a","1"), new Cookie("b", "2") };
  private static final Cookie[] GOOD_LIST = { new Cookie("a", "1"), new Cookie(LoginCookieFactory.WDK_LOGIN_COOKIE_NAME, "value") };

  @Test
  public void testFindCookie() {
    Optional<CookieBuilder> c;
    // test null
    c = CheckLoginFilter.findLoginCookie(null);
    assertFalse(c.isPresent());
    // test empty list
    c = CheckLoginFilter.findLoginCookie(new Cookie[0]);
    assertFalse(c.isPresent());
    // test list not containing cookie
    c = CheckLoginFilter.findLoginCookie(BAD_LIST);
    assertFalse(c.isPresent());
    // test list not containing cookie
    c = CheckLoginFilter.findLoginCookie(GOOD_LIST);
    assertTrue(c.isPresent());
    assertEquals(c.get().getName(), LoginCookieFactory.WDK_LOGIN_COOKIE_NAME);
  }

  public static final String[] EXCEPTION_CASES = {
    // failure cases
    null, "", "word", "word-", "-word", "word--", "word-word-" // others???
  };

  public static final String[][] PARSE_CASES = {
    // obvious success cases
    { "email-checksum", "email", "checksum" },

    // more tricky "success" cases
    { "--word", "-", "word" },
    { "email--checksum", "email-", "checksum" },
    { "-word-word", "-word", "word" },
    { "blah-word--checksum", "blah-word-", "checksum" }
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
      assertEquals(parts.getChecksum(), successCase[2]);
    }
  }

  public static final String REMEMBER_MAX_AGE = Integer.valueOf(java.lang.Integer.MAX_VALUE / 256).toString();
  public static final String COOKIE_PATH = "/";

  public static final String[][] COOKIE_CASES = {
    { "rdoherty@pcbi.upenn.edu", REMEMBER_MAX_AGE,
      "rdoherty%40pcbi.upenn.edu-2bcea64085894809f6828969f4ea1c27" }
  };

  @Test
  public void testCreateCookie() throws Exception {
    LoginCookieFactory factory = new LoginCookieFactory(SECRET_KEY);
    for (String[] data : COOKIE_CASES) {
      CookieBuilder c = factory.createLoginCookie(data[0]);
      assertEquals(c.getMaxAge(), Integer.parseInt(data[1]));
      assertEquals(c.getValue(), data[2]);
      assertEquals(c.getPath(), COOKIE_PATH);
    }
  }
  
  @Test
  public void testValidateCookie() throws Exception {
    LoginCookieFactory factory = new LoginCookieFactory(SECRET_KEY);
    for (String[] data : COOKIE_CASES) {
      LoginCookieParts parts = LoginCookieFactory.parseCookieValue(data[2]);
      assertTrue(factory.isValidCookie(parts));
    }
    
    // when we salt with a different key, all should fail
    factory = new LoginCookieFactory(ANOTHER_KEY);
    for (String[] data : COOKIE_CASES) {
      LoginCookieParts parts = LoginCookieFactory.parseCookieValue(data[2]);
      assertFalse(factory.isValidCookie(parts));
    }
  }
}
