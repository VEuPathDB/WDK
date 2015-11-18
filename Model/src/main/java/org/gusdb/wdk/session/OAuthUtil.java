package org.gusdb.wdk.session;

import java.util.Date;
import java.util.UUID;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class OAuthUtil {

  public static final String STATE_TOKEN_KEY = "OAUTH_STATE_TOKEN";

  /**
   * Generates a new state token based on the current time, a random UUID, and
   * WDK model's current secret key value.  This value should be added to the
   * session when a user is about to attempt a login; it will then be checked
   * against the state token accompanying the authentication token returned by
   * the OAuth server after the user has been authenticated.  This is to prevent
   * cross-site request forgery attacks.
   * 
   * @param wdkModel WDK Model (used to fetch secret key)
   * @return generated state token
   * @throws WdkModelException if unable to access secret key
   */
  public static String generateStateToken(WdkModel wdkModel) throws WdkModelException {
    String saltedString =
        UUID.randomUUID() + ":::" +
        String.valueOf(new Date().getTime()) + ":::" +
        wdkModel.getSecretKey();
    return EncryptionUtil.encryptNoCatch(saltedString);
  }

}
