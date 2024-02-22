package org.gusdb.wdk.model.user;

import java.util.regex.Matcher;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;

public class UserPasswordEmailer {

  // -------------------------------------------------------------------------
  // the macros used by the registration email
  // -------------------------------------------------------------------------

  private static final String EMAIL_MACRO_USER_NAME = "USER_NAME";
  private static final String EMAIL_MACRO_EMAIL = "EMAIL";
  private static final String EMAIL_MACRO_PASSWORD = "PASSWORD";

  private final boolean _sendWelcomeEmail;
  private final ModelConfig _wdkModelConfig;

  public UserPasswordEmailer(WdkModel wdkModel) {
    _wdkModelConfig = wdkModel.getModelConfig();

    // whether or not WDK is configured to send a welcome email to new registered users (defaults to true)
    String dontEmailProp = wdkModel.getProperties().get("DONT_EMAIL_NEW_USER");
    _sendWelcomeEmail = dontEmailProp == null || !dontEmailProp.equals("true");
  }

  public boolean isSendWelcomeEmail() {
    return _sendWelcomeEmail;
  }

  public void emailTemporaryPassword(User user, String password) throws WdkModelException {
    if (!_sendWelcomeEmail) return;

    String smtpServer = _wdkModelConfig.getSmtpServer();
    String supportEmail = _wdkModelConfig.getSupportEmail();
    String emailSubject = _wdkModelConfig.getEmailSubject();

    // populate email content macros with user data
    String emailContent = _wdkModelConfig.getEmailContent()
        .replaceAll("\\$\\$" + EMAIL_MACRO_USER_NAME + "\\$\\$",
            Matcher.quoteReplacement(user.getDisplayName()))
        .replaceAll("\\$\\$" + EMAIL_MACRO_EMAIL + "\\$\\$",
            Matcher.quoteReplacement(user.getEmail()))
        .replaceAll("\\$\\$" + EMAIL_MACRO_PASSWORD + "\\$\\$",
            Matcher.quoteReplacement(password));

    Utilities.sendEmail(smtpServer, user.getEmail(), supportEmail, emailSubject, emailContent);
  }
}
