package org.gusdb.wdk.controller.actionutil;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkUserException;

/**
 * Generic Spam detection tools.
 *
 * Each static method will take a param value and verify that it is in the
 * expected format. If it isn't, then a WdkUserException will be thrown.
 * See http://www.usertesting.com/blog/2014/04/09/think-your-site-needs-captcha-try-these-user-friendly-alternatives/
 * for spam detection method.
 *
 * @author dfalke
 */
public final class SpamUtils {

  private static final Logger LOG = Logger.getLogger(SpamUtils.class.getName());

  /* Elapsed time in seconds */
  private static int MINIMUM_ELAPSED_TIME = 5;

  /* placeholders
   public static void checkHoneyPot(ParamGroup params) { }
   public static void checkIsLoggedIn() { }
  */

  /**
   * Check that value is an Integer greater than MINIMUM_ELAPSED_TIME.
   *
   * See http://stackoverflow.com/questions/8472/practical-non-image-based-captcha-approaches
   * for methodology
   */
  public static void verifyTimeStamp(String value) throws WdkUserException {
    boolean isSpam = true;
    try {
      Integer elapsedTime = Integer.parseInt(value);

      // If value is less than MINIMUM_ELAPSED_TIME, consider it spam
      isSpam = elapsedTime.compareTo(MINIMUM_ELAPSED_TIME) < 0;
    }
    catch (NumberFormatException e) { /* pass - leave isSpam true */ }

    if (isSpam) {
      LOG.debug("Treating request as spam. Timestamp value is " + value);
      throw new WdkUserException("Your request contains invalid parameters.");
    }
  }
}
