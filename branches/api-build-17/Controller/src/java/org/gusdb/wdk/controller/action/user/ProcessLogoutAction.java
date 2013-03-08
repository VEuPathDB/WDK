package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.gusdb.wdk.controller.LoginCookieFactory;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * @author xingao
 * @author rdoherty
 */
public class ProcessLogoutAction extends WdkAction {

  // no params are necessary or expected, but don't mind if user sends them
  @Override protected Map<String, ParamDef> getParamDefs() { return null; }
  @Override protected boolean shouldValidateParams() { return false; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    // reset the session and replace current user with new guest
    resetSession();
    setCurrentUser(getWdkModel().getUserFactory().getGuestUser());

    // tell cookie to expire immediately
    addCookieToResponse(LoginCookieFactory.createLogoutCookie());

    return new ActionResult().setViewName(SUCCESS);
  }
}
