package org.gusdb.wdk.controller.action.user;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;

/**
 * @author xingao
 */
public class ProcessRegisterAction extends WdkAction {

  private static Logger logger = Logger.getLogger(ProcessRegisterAction.class.getName());

  // since wdk and custom preferences are unknown by this class, must skip validation
  @Override protected boolean shouldValidateParams() { return false; }
  @Override protected boolean shouldCheckSpam() { return false; } // FIXME: need a better solution; should be returning true here
  @Override protected Map<String, ParamDef> getParamDefs() { return null; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    // if a custom register page exists, use it; otherwise, use default one
    String customViewFile = getCustomViewDir() + CConstants.WDK_REGISTER_PAGE;

    ActionResult result = (wdkResourceExists(customViewFile) ?
        new ActionResult().setViewPath(customViewFile) :
        new ActionResult().setViewName(SUCCESS));

    String email = null, firstName = null, lastName = null, middleName = null, organization = null;
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
      else if (paramName.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
        String paramValue = params.getValue(paramName);
        globalPreferences.put(paramName, paramValue);
      }
      else if (paramName.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
        String paramValue = params.getValue(paramName);
        projectPreferences.put(paramName, paramValue);
      }
    }

    if (email != null && !email.isEmpty()) {
      // create the user with user input
      UserFactoryBean factory = getWdkModel().getUserFactory();
      /* UserBean user = */
      logger.info("Creating new non-temp user: " + firstName + " " + lastName + " (" + email + ")");
      Map<String,String> profile = new MapBuilder<String,String>()
          .put("firstName", firstName)
          .put("middleName", middleName)
          .put("lastName", lastName)
          .put("organization", organization).toMap();
      factory.createUser(email, profile, globalPreferences, projectPreferences);
      // registration succeed
      result.setRequestAttribute("registerSucceed", true);
      return result;
    }
    else {
      // FIXME: temporary hack to handle redirection from OAuth server
      //   assume a request to here without params is a redirect from OAuth and
      //   redirect back to the profile page
      return new ActionResult().setViewName("profile");
    }
  }
}
