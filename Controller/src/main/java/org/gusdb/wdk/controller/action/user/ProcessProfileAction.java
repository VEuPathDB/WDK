package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.session.LoginCookieFactory;

/**
 * @author xingao
 */
public class ProcessProfileAction extends WdkAction {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ProcessProfileAction.class.getName());

    // since wdk and custom preferences are unknown by this class, must skip validation
    @Override protected boolean shouldValidateParams() { return false; }
    @Override protected Map<String, ParamDef> getParamDefs() { return null; }

    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {
      // get the current user
      User user = getCurrentUser().getUser();
      String oldEmail = user.getEmail();

      // if a custom profile page exists, use it; otherwise, use default one
      String customViewFile = getCustomViewDir() + CConstants.WDK_PROFILE_PAGE;

      ActionResult result = (wdkResourceExists(customViewFile) ?
          new ActionResult().setViewPath(customViewFile) :
          new ActionResult().setViewName(SUCCESS));

      // fails if the current use is a guest
      if (!user.isGuest()) {

        // clear the preference
        user.getPreferences().clearPreferences();

        for (String paramName : params.getKeys()) {
          switch(paramName) {
            case "email":
              user.setEmail(params.getValue("email"));
              break;
            case "firstName":
            case "middleName":
            case "lastName":
            case "organization":
              user.setProfileProperty(paramName, params.getValue(paramName));
              break;
          }
          if (paramName.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
            String paramValue = params.getValue(paramName);
            user.getPreferences().setGlobalPreference(paramName, paramValue);
          }
          else if (paramName.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
            String paramValue = params.getValue(paramName);
            user.getPreferences().setProjectPreference(paramName, paramValue);
          }
        }

        // update and save the user with user input
        try {
          getWdkModel().getModel().getUserFactory().saveUser(user);
          // Update profile succeed
          result.setRequestAttribute("profileSucceed", true);
        }
        catch (WdkModelException ex) {
          // email exists, notify the user to input again
          result.setRequestAttribute(CConstants.WDK_PROFILE_ERROR_KEY, ex.getMessage());
        }
      }

      // if user updated email address, set new login cookie created from new email
      if (!oldEmail.equals(user.getEmail())) {
        LoginCookieFactory factory = new LoginCookieFactory(getWdkModel().getSecretKey());
        Cookie oldCookie = LoginCookieFactory.findLoginCookie(getRequestCookies());
        Cookie newCookie = factory.createLoginCookie(user.getEmail(), oldCookie.getMaxAge());
        addCookieToResponse(newCookie);
      }

      return result;
    }
}
