package org.gusdb.wdk.model.user;

import java.util.Optional;
import java.util.regex.Matcher;

import org.gusdb.wdk.model.Attachment;
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

  private final WdkModel _wdkModel;

  public UserPasswordEmailer(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  public boolean isSendWelcomeEmail() {
    // whether or not WDK is configured to send a welcome email to new registered users (defaults to true)
    String dontEmailProp = _wdkModel.getProperties().get("DONT_EMAIL_NEW_USER");
    return dontEmailProp == null || !dontEmailProp.equals("true");
  }

  public void emailTemporaryPassword(User user, String password) throws WdkModelException {
    if (!isSendWelcomeEmail()) return;

    ModelConfig wdkModelConfig = _wdkModel.getModelConfig();
    String smtpServer = wdkModelConfig.getSmtpServer();
    String supportEmail = wdkModelConfig.getSupportEmail();
    String emailSubject = wdkModelConfig.getEmailSubject();

    // Unwrap optionals here to maintain consistency with nullable arguments in Utilities.sendEmail method.
    String smtpUser = wdkModelConfig.getSmtpUserName().orElse(null);
    String smtpPass = wdkModelConfig.getSmtpPassword().orElse(null);

    // populate email content macros with user data
    String emailContent = wdkModelConfig.getEmailContent()
        .replaceAll("\\$\\$" + EMAIL_MACRO_USER_NAME + "\\$\\$",
            Matcher.quoteReplacement(user.getDisplayName()))
        .replaceAll("\\$\\$" + EMAIL_MACRO_EMAIL + "\\$\\$",
            Matcher.quoteReplacement(user.getEmail()))
        .replaceAll("\\$\\$" + EMAIL_MACRO_PASSWORD + "\\$\\$",
            Matcher.quoteReplacement(password));

    Utilities.sendEmail(smtpServer, smtpUser, smtpPass, user.getEmail(), supportEmail, emailSubject, emailContent, null, null, new Attachment[]{});
  }
}
