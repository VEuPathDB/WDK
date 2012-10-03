package org.gusdb.wdk.controller.action.user;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;

/**
 * @author xingao
 */
public class ProcessRegisterAction extends WdkAction {

  private static Logger logger = Logger.getLogger(ProcessRegisterAction.class.getName());

  @Override
  protected ResponseType getResponseType() {
    return ResponseType.html;
  }

  // since wdk and custom preferences are unknown by this class, must skip validation
  @Override protected boolean shouldValidateParams() { return false; }
  @Override protected Map<String, ParamDef> getParamDefs() { return null; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    // if a custom register page exists, use it; otherwise, use default one
    String customViewFile = getCustomViewDir() + CConstants.WDK_REGISTER_PAGE;

    ActionResult result = (wdkResourceExists(customViewFile) ?
        new ActionResult().setViewPath(customViewFile) :
        new ActionResult().setViewName(SUCCESS));

    String email = null, firstName = null, lastName = null, middleName = null, organization = null, openId = null;
    Map<String, String> globalPreferences = new LinkedHashMap<String, String>();
    Map<String, String> projectPreferences = new LinkedHashMap<String, String>();

    for (String paramName : params.getKeys()) {
      if (paramName.equals(CConstants.WDK_EMAIL_KEY)) {
        email = params.getValue(paramName);
      }
      else if (paramName.equalsIgnoreCase("firstName")) {
        firstName = params.getValue(paramName);
      }
      else if (paramName.equalsIgnoreCase("lastName")) {
        lastName = params.getValue(paramName);
      }
      else if (paramName.equalsIgnoreCase("middleName")) {
        middleName = params.getValue(paramName);
      }
      else if (paramName.equalsIgnoreCase("organization")) {
        organization = params.getValue(paramName);
      }
      else if (paramName.equalsIgnoreCase("openId")) {
        openId = params.getValue(paramName);
      }
      else if (paramName.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
        String paramValue = params.getValue(paramName);
        globalPreferences.put(paramName, paramValue);
      }
      else if (paramName.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
        String paramValue = params.getValue(paramName);
        projectPreferences.put(paramName, paramValue);
      }
    }

    if (email != null && email.length() != 0) {
      // create the user with user input
      UserFactoryBean factory = getWdkModel().getUserFactory();
      try {
        /* UserBean user = */
        logger.info("Creating new non-temp user: " + firstName + " " + lastName + " (" + email + ")");
        factory.createUser(email, lastName, firstName, middleName, null,
            organization, null, null, null, null, null, null, null, openId,
            globalPreferences, projectPreferences);
        // registration succeed
        result.setRequestAttribute("registerSucceed", true);
      }
      catch (WdkUserException ex) {
        // email exists, notify the user to input again
        result.setRequestAttribute(CConstants.WDK_REGISTER_ERROR_KEY, ex.getMessage());

        // push back the user input, so that the user doesn't need to type again
        result.setRequestAttribute(CConstants.WDK_EMAIL_KEY, email);
        result.setRequestAttribute("firstName", firstName);
        result.setRequestAttribute("lastName", lastName);
        result.setRequestAttribute("middleName", middleName);
        result.setRequestAttribute("organization", organization);
        result.setRequestAttribute("openId", openId);
        for (String param : projectPreferences.keySet()) {
          result.setRequestAttribute(param, projectPreferences.get(param));
        }
        for (String param : globalPreferences.keySet()) {
          result.setRequestAttribute(param, globalPreferences.get(param));
        }
      }
    }
    return result;
  }
}
