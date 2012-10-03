package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * @author xingao
 */
public class ShowResetPasswordAction extends WdkAction {

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
      return EMPTY_PARAMS;
    }

    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {
      // if a custom password page exists, use it; otherwise, use default one
      String customViewFile = getCustomViewDir() + CConstants.WDK_RESET_PASSWORD_PAGE;
      String customProfileViewFile = getCustomViewDir() + CConstants.WDK_PROFILE_PAGE;

      // check whether the user is logged in; if so, redirect to profile page
      return (getCurrentUser().isGuest() ?
          (wdkResourceExists(customViewFile) ?
              new ActionResult().setViewPath(customViewFile) :
              new ActionResult().setViewName(SUCCESS)) :
          (wdkResourceExists(customProfileViewFile) ?
              new ActionResult().setViewPath(customProfileViewFile).setRedirect(true) :
              new ActionResult().setViewName(CConstants.SHOW_PROFILE_MAPKEY)));
    }
}
