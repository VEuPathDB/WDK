package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.LoginCookieFactory;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;

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
      UserBean user = getCurrentUser();
      String oldEmail = user.getEmail();

      // if a custom profile page exists, use it; otherwise, use default one
      String customViewFile = getCustomViewDir() + CConstants.WDK_PROFILE_PAGE;

      ActionResult result = (wdkResourceExists(customViewFile) ?
          new ActionResult().setViewPath(customViewFile) :
          new ActionResult().setViewName(SUCCESS));

      // fails if the current use is a guest
      if (!user.isGuest()) {

        // clear the preference
        user.clearPreferences();

        for (String paramName : params.getKeys()) {
          if (paramName.equalsIgnoreCase("email")) {
            user.setEmail(params.getValue("email"));
          } else if (paramName.equalsIgnoreCase("firstName")) {
            user.setFirstName(params.getValue("firstName"));
          } else if (paramName.equalsIgnoreCase("lastName")) {
            user.setLastName(params.getValue("lastName"));
          } else if (paramName.equalsIgnoreCase("middleName")) {
            user.setMiddleName(params.getValue("middleName"));
          } else if (paramName.equalsIgnoreCase("title")) {
            user.setTitle(params.getValue("title"));
          } else if (paramName.equalsIgnoreCase("organization")) {
            user.setOrganization(params.getValue("organization"));
          } else if (paramName.equalsIgnoreCase("openId")) {
            user.setOpenId(params.getValue("openId"));
          } else if (paramName.equalsIgnoreCase("department")) {
            user.setDepartment(params.getValue("department"));
          } else if (paramName.equalsIgnoreCase("address")) {
            user.setAddress(params.getValue("address"));
          } else if (paramName.equalsIgnoreCase("city")) {
            user.setCity(params.getValue("city"));
          } else if (paramName.equalsIgnoreCase("state")) {
            user.setState(params.getValue("state"));
          } else if (paramName.equalsIgnoreCase("zipCode")) {
            user.setZipCode(params.getValue("zipCode"));
          } else if (paramName.equalsIgnoreCase("phoneNumber")) {
            user.setPhoneNumber(params.getValue("phoneNumber"));
          } else if (paramName.equalsIgnoreCase("country")) {
            user.setCountry(params.getValue("country"));
          } else if (paramName.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
            String paramValue = params.getValue(paramName);
            user.setGlobalPreference(paramName, paramValue);
          } else if (paramName.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
            String paramValue = params.getValue(paramName);
            user.setProjectPreference(paramName, paramValue);
          }
        }

        // update and save the user with user input
        try {
          user.save();
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
