package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import javax.servlet.http.Cookie;

import org.gusdb.wdk.controller.LoginCookieFactory;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.UIConfig;
import org.gusdb.wdk.model.WdkCookie;

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

    // ask model if we should expire any other cookies and do so
    UIConfig uiConfig = getWdkModel().getModel().getUIConfig();
    for (WdkCookie wdkCookie : uiConfig.getExtraLogoutCookies()) {
      Cookie extraCookie = new Cookie(wdkCookie.getName(), "");
      extraCookie.setPath(wdkCookie.getPath());
      extraCookie.setMaxAge(0);
      addCookieToResponse(extraCookie);
    }
    
    return new ActionResult().setViewName(SUCCESS);
  }
}
