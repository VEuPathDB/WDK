package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamDefMapBuilder;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;

/**
 * @author xingao
 */
public class ProcessResetPasswordAction extends WdkAction {

    private static final Map<String,ParamDef> PARAM_DEFS = new ParamDefMapBuilder()
        .addParam("email", new ParamDef(Required.OPTIONAL)).toMap();
  
    @Override
    protected ResponseType getResponseType() {
      return ResponseType.html;
    }

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

      // if the user is logged in, redirect him/her to change password page
      UserBean user = getCurrentUser();
      if (!user.isGuest()) {
        return new ActionResult().setViewName(CConstants.SHOW_PASSWORD_MAPKEY);
      }

      // if a custom profile page exists, use it; otherwise, use default one
      String customViewFile = getCustomViewDir() + CConstants.WDK_RESET_PASSWORD_PAGE;

      ActionResult result = 
          (wdkResourceExists(customViewFile) ?
              new ActionResult().setViewPath(customViewFile) :
              new ActionResult().setViewName(SUCCESS));

      // get user's input
      String email = params.getValueOrEmpty("email");

      UserFactoryBean factory = getWdkModel().getUserFactory();
      try {
        if (email.isEmpty()) {
          throw new WdkUserException("Please provide a valid email.");
        }
        factory.resetPassword(email);
        // resetting password succeed
        result.setRequestAttribute("resetPasswordSucceed", true);
      }
      catch (WdkUserException ex) {
        ex.printStackTrace();
        // resetting password failed, set the error message
        result.setRequestAttribute(CConstants.WDK_RESET_PASSWORD_ERROR_KEY, ex.getMessage());
      }
      
      return result;
    }
}
