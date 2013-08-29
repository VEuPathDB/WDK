package org.gusdb.wdk.controller.action.user;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;

public class ShowLoginAction extends GenericPageAction {

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    if (getCurrentUser().isGuest()) {
      ActionResult result = new ActionResult().setViewName(SUCCESS);
      addAttributeIfParamPresent(params, result, CConstants.WDK_REDIRECT_URL_KEY);
      addAttributeIfParamPresent(params, result, CConstants.WDK_LOGIN_ERROR_KEY);
      return result;
    }
    // show user profile if user is already logged in
    return new ActionResult().setViewName(CConstants.SHOW_PROFILE_MAPKEY);
  }

  private void addAttributeIfParamPresent(ParamGroup params, ActionResult result, String paramKey) {
    String paramValue = params.getValueOrEmpty(paramKey);
    if (!paramValue.isEmpty()) {
      result.setRequestAttribute(paramKey, paramValue);
    }
  }
}
