/**
 * 
 */
package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamDefMapBuilder;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 */
public class ProcessPasswordAction extends WdkAction {

    private static final String OLD_PASSWORD_KEY = "oldPassword";
    private static final String NEW_PASSWORD_KEY = "newPassword";
    private static final String CONFIRM_PASSWORD_KEY = "confirmPassword";
  
    private static final Map<String, ParamDef> PARAM_DEFS = new ParamDefMapBuilder()
      .addParam(OLD_PASSWORD_KEY, new ParamDef(Required.REQUIRED))
      .addParam(NEW_PASSWORD_KEY, new ParamDef(Required.REQUIRED))
      .addParam(CONFIRM_PASSWORD_KEY, new ParamDef(Required.REQUIRED)).toMap();

    @Override
    protected boolean shouldValidateParams() {
      return true;
    }

    @Override
    protected Map<String, ParamDef> getParamDefs() {
      return PARAM_DEFS;
    }

    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {
      // get the current user
      UserBean user = getCurrentUser();
      
      // if a custom profile page exists, use it; otherwise, use default one
      String customViewFile = getCustomViewDir() + CConstants.WDK_PASSWORD_PAGE;

      ActionResult result = (wdkResourceExists(customViewFile) ?
        new ActionResult().setViewPath(customViewFile) :
        new ActionResult().setViewName(SUCCESS));

      // get user's input
      String oldPassword = params.getValue(OLD_PASSWORD_KEY);
      String newPassword = params.getValue(NEW_PASSWORD_KEY);
      String confirmPassword = params.getValue(CONFIRM_PASSWORD_KEY);

      try {
        // fails if the current use is a guest
        if (user.isGuest()) {
          throw new WdkUserException("You cannot change the password as a guest.");
        }
        user.changePassword(oldPassword, newPassword, confirmPassword);
        setSessionAttribute(CConstants.WDK_USER_KEY, user);
        // changing password succeed
        result.setRequestAttribute("changePasswordSucceed", true);
      }
      catch (WdkUserException ex) {
        ex.printStackTrace();
        // user authentication failed, set the error message
        result.setRequestAttribute(CConstants.WDK_CHANGE_PASSWORD_ERROR_KEY, ex.getMessage());
      }
      return result;
    }
}
